package marte.agentes;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import marte.modelo.EstadoRover;
import marte.modelo.Exploracion;
import marte.modelo.Lectura;
import marte.modelo.Mensajes;
import marte.modelo.Observacion;
import marte.modelo.OntologiaMarte;
import marte.modelo.Propuesta;
import marte.modelo.Resultado;

/** Conserva energía, elige el siguiente tramo y trae la muestra a base. */
public class Rover extends Agent {
    private static final int COSTO_REGRESO = 20;
    private static final String[] RUTA = {"Falda", "Ladera", "Caldera"};
    private int bateria = 80;
    private int muestras;
    private int tramo;
    private int esperas;
    private String misionActiva;
    private String escenario;
    private boolean ocupado;

    @Override protected void setup() {
        OntologiaMarte.registrar(this);
        addBehaviour(new Presentarse());
        addBehaviour(new Escuchar());
        addBehaviour(new Telemetria(this, 5000));
        System.out.println("[rover] Listo para ascender. Batería=" + bateria);
    }

    private class Presentarse extends OneShotBehaviour {
        @Override public void action() { informarEstado("inicio"); }
    }

    private class Telemetria extends TickerBehaviour {
        Telemetria(Agent a, long t) { super(a, t); }
        @Override protected void onTick() { informarEstado("telemetria"); }
    }

    private void informarEstado(String id) {
        EstadoRover e = new EstadoRover();
        e.setMision(id); e.setBateria(bateria); e.setMuestras(muestras);
        send(Mensajes.crear(this, ACLMessage.INFORM, "entorno", id, e));
    }

    private void pedirLectura(boolean espera, boolean desvio, boolean confirmacion) {
        Exploracion orden = new Exploracion(RUTA[tramo]);
        orden.setEscenario(escenario);
        orden.setEspera(espera);
        orden.setDesvio(desvio);
        orden.setConfirmacion(confirmacion);
        send(Mensajes.crear(this, ACLMessage.QUERY_REF, "entorno", misionActiva, orden));
    }

    private class Escuchar extends CyclicBehaviour {
        private final MessageTemplate propuesta = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
            MessageTemplate.MatchSender(new AID("coordinador", AID.ISLOCALNAME)));
        private final MessageTemplate entorno = MessageTemplate.and(
            MessageTemplate.MatchSender(new AID("entorno", AID.ISLOCALNAME)),
            MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)));

        @Override public void action() {
            ACLMessage mensaje = myAgent.receive(MessageTemplate.and(
                MessageTemplate.MatchOntology(OntologiaMarte.NOMBRE),
                MessageTemplate.or(propuesta, entorno)));
            if (mensaje == null) { block(); return; }
            try {
                String id = mensaje.getConversationId();
                if (mensaje.getPerformative() == ACLMessage.REQUEST) {
                    if ("consulta".equals(id)) { informarEstado(id); return; }
                    Exploracion inicio = Mensajes.leer(myAgent, mensaje, Exploracion.class);
                    if (misionActiva != null) return;
                    misionActiva = id;
                    escenario = inicio.getEscenario();
                    tramo = 0; esperas = 0; muestras = 0;
                    bateria = "BATERIA".equals(inicio.getEscenario()) ? 50 : 80;
                    informarEstado(id);
                    pedirLectura(false, false, false);
                    return;
                }
                if (!id.equals(misionActiva)) return;
                if (mensaje.getPerformative() == ACLMessage.INFORM) {
                    Observacion datos = Mensajes.leer(myAgent, mensaje, Observacion.class);
                    if (id.equals(datos.getLectura().getMision())) {
                        send(Mensajes.crear(myAgent, ACLMessage.REQUEST, "coordinador", id, datos));
                    }
                    return;
                }
                Propuesta p = Mensajes.leer(myAgent, mensaje, Propuesta.class);
                if (!id.equals(p.getMision())) throw new IllegalArgumentException("Misión distinta");
                boolean agotada = "ESPERAR".equals(p.getAccion()) && esperas >= 2;
                boolean aceptada = !ocupado && !agotada && bateria >= costo(p.getAccion()) + COSTO_REGRESO;
                int tipo = aceptada ? ACLMessage.ACCEPT_PROPOSAL : ACLMessage.REJECT_PROPOSAL;
                send(Mensajes.crear(myAgent, tipo, "coordinador", id, p));
                if (ocupado) return;
                ocupado = true;
                if (aceptada) {
                    avisarAccion(p, p.getAccion(), "Aceptada por el rover");
                    addBehaviour(new EjecutarAccion(Rover.this, p));
                } else {
                    String motivo = agotada ? "Polvo alto tras dos esperas"
                        : "Disponibles " + bateria + "; necesarias "
                            + (costo(p.getAccion()) + COSTO_REGRESO)
                            + " (" + costo(p.getAccion()) + " de acción + 20 de regreso)";
                    iniciarRegreso(p, false, p.getAccion() + " rechazada. " + motivo);
                }
            } catch (RuntimeException e) { System.err.println("[rover] " + e.getMessage()); }
        }
    }

    private int costo(String accion) {
        switch (accion) {
            case "AVANZAR": return 8;
            case "RODEAR": return 12;
            case "ESPERAR": return 1;
            case "ESCANEAR": return 5;
            case "MUESTRA": return 15;
            default: throw new IllegalArgumentException("Acción desconocida: " + accion);
        }
    }

    private void informarResultado(Resultado r) {
        r.setBateria(bateria);
        r.setMuestras(muestras);
        send(Mensajes.crear(this, ACLMessage.INFORM, "entorno", r.getMision(), r));
        System.out.println("[rover] " + r.getMision() + " " + r.getAccion()
            + ", batería=" + bateria + ", muestras=" + muestras);
    }

    private void regresar(Propuesta p, boolean objetivo, String motivo) {
        bateria = Math.max(0, bateria - COSTO_REGRESO);
        Resultado r = new Resultado();
        r.setMision(p.getMision()); r.setLectura(p.getLectura());
        r.setAccion("REGRESAR"); r.setFinalizado(true); r.setObjetivo(objetivo);
        r.setDetalle(motivo);
        informarResultado(r);
        misionActiva = null;
        ocupado = false;
    }

    private void avisarAccion(Propuesta p, String accion, String detalle) {
        Resultado inicio = new Resultado();
        inicio.setMision(p.getMision());
        inicio.setLectura(p.getLectura());
        inicio.setAccion("EN CURSO " + accion);
        inicio.setDetalle(detalle);
        informarResultado(inicio);
    }

    private void iniciarRegreso(Propuesta p, boolean objetivo, String detalle) {
        avisarAccion(p, "REGRESAR", detalle);
        addBehaviour(new WakerBehaviour(this, 3000) {
            @Override protected void onWake() {
                regresar(p, objetivo, detalle + ". Llegada a base");
            }
        });
    }

    void completarAccion(Propuesta p) {
        bateria -= costo(p.getAccion());
        Resultado r = new Resultado();
        r.setMision(p.getMision()); r.setLectura(p.getLectura());
        r.setAccion(p.getAccion()); r.setDetalle(p.getMotivo());
        r.setFinalizado(false); r.setObjetivo(false);
        if ("MUESTRA".equals(p.getAccion())) muestras = 1;
        informarResultado(r);
        ocupado = false;
        switch (p.getAccion()) {
            case "AVANZAR":
                tramo++;
                esperas = 0;
                pedirLectura(false, false, false);
                break;
            case "RODEAR": pedirLectura(false, true, false); break;
            case "ESPERAR":
                esperas++;
                pedirLectura(true, false, false);
                break;
            case "ESCANEAR": pedirLectura(false, false, true); break;
            case "MUESTRA":
                ocupado = true;
                iniciarRegreso(p, true, "Muestra a bordo");
                break;
            default: throw new IllegalStateException("Acción no prevista");
        }
    }
}

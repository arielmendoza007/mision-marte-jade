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
import marte.modelo.Mensajes;
import marte.modelo.OntologiaMarte;
import marte.modelo.Propuesta;
import marte.modelo.Exploracion;
import marte.modelo.Observacion;
import marte.modelo.Resultado;

/** Decide si dispone de energ�a y ejecuta una acci�n simulada. */
public class Rover extends Agent {
    private int bateria = 80;
    private int muestras = 0;
    private boolean ocupado = false;

    @Override protected void setup() {
        OntologiaMarte.registrar(this);
        addBehaviour(new Presentarse());
        addBehaviour(new Escuchar());
        addBehaviour(new Telemetria(this, 5000));
        System.out.println("[rover] Listo. Bater�a inicial: " + bateria);
    }

    private class Presentarse extends OneShotBehaviour {
        @Override public void action() { informarEstado("inicio"); }
    }

    private class Telemetria extends TickerBehaviour {
        Telemetria(Agent agente, long intervalo) { super(agente, intervalo); }
        @Override protected void onTick() { informarEstado("telemetria"); }
    }

    private void informarEstado(String mision) {
        EstadoRover e = new EstadoRover();
        e.setMision(mision);
        e.setBateria(bateria);
        e.setMuestras(muestras);
        send(Mensajes.crear(this, ACLMessage.INFORM, "entorno", mision, e));
    }

    private class Escuchar extends CyclicBehaviour {
        private final MessageTemplate propuesta = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
            MessageTemplate.MatchSender(new AID("coordinador", AID.ISLOCALNAME)));
        private final MessageTemplate consulta = MessageTemplate.and(
            MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchPerformative(ACLMessage.INFORM)),
            MessageTemplate.MatchSender(new AID("entorno", AID.ISLOCALNAME)));

        @Override public void action() {
            ACLMessage mensaje = myAgent.receive(MessageTemplate.or(propuesta, consulta));
            if (mensaje == null) { block(); return; }
            try {
                String id = mensaje.getConversationId();
                if (mensaje.getPerformative() == ACLMessage.INFORM) {
                    Observacion datos = Mensajes.leer(myAgent, mensaje, Observacion.class);
                    if (id.equals(datos.getLectura().getMision())) {
                        send(Mensajes.crear(myAgent, ACLMessage.REQUEST, "coordinador", id, datos));
                    }
                    return;
                }
                if (mensaje.getPerformative() == ACLMessage.REQUEST) {
                    if ("consulta".equals(id)) informarEstado(id);
                    else {
                        Exploracion orden = Mensajes.leer(myAgent, mensaje, Exploracion.class);
                        send(Mensajes.crear(myAgent, ACLMessage.QUERY_REF, "entorno", id, orden));
                    }
                    return;
                }
                Propuesta p = Mensajes.leer(myAgent, mensaje, Propuesta.class);
                if (!p.getMision().equals(mensaje.getConversationId())) {
                    throw new IllegalArgumentException("El identificador de misi�n no coincide");
                }
                int costo = costo(p.getAccion());
                boolean aceptada = !ocupado && bateria >= costo + 20;
                int tipo = aceptada ? ACLMessage.ACCEPT_PROPOSAL : ACLMessage.REJECT_PROPOSAL;
                myAgent.send(Mensajes.crear(myAgent, tipo, "coordinador", p.getMision(), p));
                if (aceptada) {
                    ocupado = true;
                    addBehaviour(new Ejecutar(myAgent, 700, p, false));
                } else if (!ocupado) {
                    ocupado = true;
                    addBehaviour(new Ejecutar(myAgent, 700, p, true));
                }
            } catch (RuntimeException e) { System.err.println("[rover] " + e.getMessage()); }
        }
    }

    private int costo(String accion) {
        switch (accion) {
            case "MUESTRA": return 15;
            case "ESCANEAR": return 5;
            case "ESPERAR": return 1;
            default: throw new IllegalArgumentException("Acci�n desconocida: " + accion);
        }
    }

    /** Espera breve visible en la interfaz; no bloquea el hilo del agente. */
    private class Ejecutar extends WakerBehaviour {
        private final Propuesta propuesta;
        private final boolean regreso;
        Ejecutar(Agent agente, long retraso, Propuesta propuesta, boolean regreso) {
            super(agente, retraso);
            this.propuesta = propuesta;
            this.regreso = regreso;
        }

        @Override protected void onWake() {
            Resultado r = new Resultado();
            r.setMision(propuesta.getMision());
            r.setLectura(propuesta.getLectura());
            if (regreso) {
                r.setAccion("REGRESAR");
                r.setDetalle("R5: bater�a insuficiente para la acci�n y reserva de 20; regreso a base");
                bateria = Math.max(0, bateria - 8);
            } else {
                r.setAccion(propuesta.getAccion());
                r.setDetalle(propuesta.getMotivo());
                if ("ESCANEAR".equals(propuesta.getAccion())) {
                    if (propuesta.getLectura().getObstaculo()) {
                        r.setDetalle("Escaneo: paso bloqueado en el punto "
                            + propuesta.getLectura().getSector().getPunto()
                            + ". Se cartografi� el obst�culo; la pr�xima visita examinar� otro punto.");
                    } else {
                        r.setDetalle("Escaneo del punto " + propuesta.getLectura().getSector().getPunto()
                            + ": se�al mineral " + propuesta.getLectura().getMineral()
                            + "/100. La pr�xima visita examinar� otro punto.");
                    }
                }
                bateria -= costo(propuesta.getAccion());
                if ("MUESTRA".equals(propuesta.getAccion())) { muestras++; }
            }
            r.setBateria(bateria);
            r.setMuestras(muestras);
            myAgent.send(Mensajes.crear(myAgent, ACLMessage.INFORM, "coordinador", r.getMision(), r));
            myAgent.send(Mensajes.crear(myAgent, ACLMessage.INFORM, "entorno", r.getMision(), r));
            System.out.println("[rover] " + r.getMision() + ": " + r.getAccion()
                + ", bater�a=" + bateria + ", muestras=" + muestras);
            ocupado = false;
            if (regreso) {
                // Se descarga la muestra en base y se recarga para la siguiente salida.
                // El contador representa las muestras acumuladas de toda la sesi�n.
                bateria = 80;
                informarEstado("recarga");
            }
        }
    }
}

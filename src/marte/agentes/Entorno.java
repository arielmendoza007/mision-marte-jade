package marte.agentes;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;
import marte.gui.ConsolaMision;
import marte.modelo.EstadoRover;
import marte.modelo.Lectura;
import marte.modelo.Mensajes;
import marte.modelo.Metricas;
import marte.modelo.OntologiaMarte;
import marte.modelo.Propuesta;
import marte.modelo.Resultado;
import marte.modelo.Sector;
import marte.modelo.Exploracion;
import marte.modelo.Observacion;

/** Intermediario entre el operador y los agentes autónomos. */
public class Entorno extends Agent {
    private volatile ConsolaMision ventana;
    private int siguienteMision = 1;
    private final Map<String, Long> inicios = new HashMap<>();
    private final Map<String, Lectura> pendientes = new HashMap<>();
    private final Map<String, String> escenarios = new HashMap<>();
    private final Map<String, Integer> esperas = new HashMap<>();
    private final Map<String, Boolean> desvios = new HashMap<>();

    @Override protected void setup() {
        OntologiaMarte.registrar(this);
        addBehaviour(new Escuchar());
        try { SwingUtilities.invokeAndWait(() -> {
            ventana = new ConsolaMision(this::iniciar, this::consultarRover);
            ventana.setVisible(true);
        }); } catch (Exception ex) { throw new IllegalStateException("No se pudo abrir la consola", ex); }
        System.out.println("[entorno] Consola de misión iniciada");
    }

    private void iniciar(String escenario) {
        addBehaviour(new OneShotBehaviour() {
            @Override public void action() {
                String id = "M-" + siguienteMision++;
                inicios.put(id, System.nanoTime());
                escenarios.put(id, escenario);
                addBehaviour(new jade.core.behaviours.WakerBehaviour(myAgent, 45000) {
                    protected void onWake() {
                        if (inicios.remove(id) == null) return;
                        pendientes.remove(id);
                        escenarios.remove(id); esperas.remove(id); desvios.remove(id);
                        ventana.estado(id + " | Sin respuesta. Consulta el rover y comprueba su conexión.");
                        ventana.agregar(id + " venció después de 45 segundos. Verifica la segunda terminal.");
                        ventana.terminar();
                    }
                });
                Exploracion orden = new Exploracion("Falda");
                orden.setEscenario(escenario);
                send(Mensajes.crear(myAgent, ACLMessage.REQUEST, "rover", id, orden));
                ventana.estado(id + " | Ascenso iniciado");
                ventana.agregar("Entorno → Rover REQUEST " + id + " | Olympus Mons: " + escenario);
            }
        });
    }

    private void consultarRover() {
        addBehaviour(new OneShotBehaviour() {
            @Override public void action() {
                EstadoRover consulta = new EstadoRover();
                consulta.setMision("consulta");
                send(Mensajes.crear(myAgent, ACLMessage.REQUEST, "rover", "consulta", consulta));
                ventana.agregar("Entorno → Rover REQUEST estado");
            }
        });
    }

    // Un recorrido corto y reproducible por el flanco hasta el borde de la caldera.
    private Lectura medir(Exploracion orden, String id) {
        String nombre = orden.getSector().getNombre();
        if (orden.getEspera()) esperas.merge(id, 1, Integer::sum);
        if (orden.getDesvio()) desvios.put(id, true);
        if (orden.getConfirmacion()) {
            Lectura original = pendientes.remove(id);
            if (original == null || !original.getSector().getNombre().equals(nombre)) {
                throw new IllegalArgumentException("No hay lectura pendiente para confirmar en " + id);
            }
            Lectura confirmada = new Lectura();
            Sector sector = new Sector(nombre);
            sector.setPunto(original.getSector().getPunto());
            confirmada.setMision(id);
            confirmada.setSector(sector);
            confirmada.setPolvo(original.getPolvo());
            confirmada.setMineral(Math.max(0, original.getMineral() - 3));
            confirmada.setObstaculo(original.getObstaculo());
            confirmada.setConfirmada(true);
            return confirmada;
        }
        int punto, polvo, mineral;
        boolean obstaculo;
        switch (nombre) {
            case "Falda":
                punto = 1; polvo = 20; mineral = 25; obstaculo = false; break;
            case "Ladera":
                punto = 2; polvo = 27; mineral = 42;
                obstaculo = !desvios.getOrDefault(id, false); break;
            case "Caldera":
                punto = 3; mineral = 85; obstaculo = false;
                polvo = "TORMENTA".equals(escenarios.get(id)) ? 85
                    : esperas.getOrDefault(id, 0) == 0 ? 85 : 55;
                break;
            default: throw new IllegalArgumentException("Tramo desconocido: " + nombre);
        }
        Lectura l = new Lectura();
        orden.getSector().setPunto(punto);
        l.setSector(orden.getSector()); l.setMision(id);
        l.setPolvo(polvo); l.setMineral(mineral); l.setObstaculo(obstaculo);
        l.setConfirmada(false);
        pendientes.put(id, l);
        return l;
    }

    private class Escuchar extends CyclicBehaviour {
        private final MessageTemplate coordinador = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchSender(new AID("coordinador", AID.ISLOCALNAME)));
        private final MessageTemplate rover = MessageTemplate.and(
            MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF)),
            MessageTemplate.MatchSender(new AID("rover", AID.ISLOCALNAME)));

        @Override public void action() {
            ACLMessage mensaje = myAgent.receive(MessageTemplate.and(
                MessageTemplate.MatchOntology(OntologiaMarte.NOMBRE),
                MessageTemplate.or(coordinador, rover)));
            if (mensaje == null) { block(); return; }
            try {
                if (mensaje.getPerformative() == ACLMessage.QUERY_REF) {
                    String id = mensaje.getConversationId();
                    if (!inicios.containsKey(id)) return;
                    Lectura lectura = medir(Mensajes.leer(myAgent, mensaje, Exploracion.class), id);
                    send(Mensajes.crear(myAgent, ACLMessage.INFORM, "rover", id, new Observacion(lectura)));
                    ventana.agregar("Rover → Entorno QUERY_REF " + id + " | Entorno devuelve "
                        + (lectura.getConfirmada() ? "la confirmación" : "la lectura inicial"));
                    return;
                }
                if ("coordinador".equals(mensaje.getSender().getLocalName())) {
                    Object contenido = myAgent.getContentManager().extractContent(mensaje);
                    if (contenido instanceof Metricas) {
                        Metricas m = (Metricas) contenido;
                        ventana.agregar("Coordinador -> Entorno INFORM " + m.getMision()
                            + " | respuestas " + m.getRespuestas() + "/" + m.getPropuestas()
                            + ", promedio " + m.getPromedioUs() + " µs");
                        return;
                    }
                    if (!inicios.containsKey(mensaje.getConversationId())) return;
                    Propuesta p = Mensajes.leer(myAgent, mensaje, Propuesta.class);
                    ventana.sensores(p.getLectura());
                    ventana.regla(p.getAccion() + " · " + p.getMotivo());
                    ventana.agregar("Coordinador → Entorno INFORM " + p.getMision()
                        + ": " + p.getAccion() + " | " + p.getMotivo());
                } else {
                    Object contenido = myAgent.getContentManager().extractContent(mensaje);
                    if (contenido instanceof EstadoRover) {
                        EstadoRover e = (EstadoRover) contenido;
                        ventana.telemetria(e.getBateria(), e.getMuestras());
                        if ("consulta".equals(e.getMision())) {
                            ventana.agregar("Rover → Entorno INFORM batería=" + e.getBateria());
                        }
                    } else if (contenido instanceof Resultado) {
                        Resultado r = (Resultado) contenido;
                        if (!inicios.containsKey(mensaje.getConversationId())) return;
                        ventana.telemetria(r.getBateria(), r.getMuestras());
                        ventana.resultado(r);
                        ventana.estado(r.getMision() + " | " + (r.getFinalizado()
                            ? (r.getObjetivo() ? "OBJETIVO CUMPLIDO" : "REGRESO PREVENTIVO")
                            : r.getAccion()));
                        ventana.agregar("Rover → Entorno INFORM " + r.getMision()
                            + ": " + r.getAccion() + " | " + r.getDetalle());
                        if (r.getAccion().startsWith("EN CURSO ")) return;
                        send(Mensajes.crear(myAgent, ACLMessage.INFORM,
                            "coordinador", r.getMision(), r));
                        ventana.agregar("Entorno → Coordinador INFORM " + r.getMision()
                            + " | resultado " + (r.getFinalizado() ? "final" : "parcial"));
                        if (!r.getFinalizado()) {
                            ventana.estado(r.getMision() + " | " + r.getAccion() + " → siguiente etapa");
                            return;
                        }
                        Long inicio = inicios.remove(r.getMision());
                        pendientes.remove(r.getMision());
                        escenarios.remove(r.getMision()); esperas.remove(r.getMision()); desvios.remove(r.getMision());
                        if (inicio != null) ventana.tiempo((System.nanoTime() - inicio) / 1_000_000);
                        ventana.terminar();
                    }
                }
            } catch (Exception e) { System.err.println("[entorno] " + e.getMessage()); }
        }
    }
}

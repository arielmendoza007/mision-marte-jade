package marte.comportamientos.entorno;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import marte.agentes.Entorno;
import marte.modelo.EstadoRover;
import marte.modelo.Lectura;
import marte.modelo.Mensajes;
import marte.modelo.Metricas;
import marte.modelo.OntologiaMarte;
import marte.modelo.Propuesta;
import marte.modelo.Resultado;
import marte.modelo.Exploracion;
import marte.modelo.Observacion;

/** Atiende los mensajes del Coordinador y del Rover. */
public class EscucharEntorno extends CyclicBehaviour {
    private final Entorno entorno;
    private final MessageTemplate coordinador = MessageTemplate.and(
        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
        MessageTemplate.MatchSender(new AID("coordinador", AID.ISLOCALNAME)));
    private final MessageTemplate rover = MessageTemplate.and(
        MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF)),
        MessageTemplate.MatchSender(new AID("rover", AID.ISLOCALNAME)));

    public EscucharEntorno(Entorno entorno) {
        super(entorno);
        this.entorno = entorno;
    }

    @Override public void action() {
        ACLMessage mensaje = myAgent.receive(MessageTemplate.and(
            MessageTemplate.MatchOntology(OntologiaMarte.NOMBRE),
            MessageTemplate.or(coordinador, rover)));
        if (mensaje == null) { block(); return; }
        try {
            if (mensaje.getPerformative() == ACLMessage.QUERY_REF) {
                String id = mensaje.getConversationId();
                if (!entorno.hayMision(id)) return;
                Lectura lectura = entorno.medir(Mensajes.leer(myAgent, mensaje, Exploracion.class), id);
                myAgent.send(Mensajes.crear(myAgent, ACLMessage.INFORM, "rover", id, new Observacion(lectura)));
                entorno.getVentana().agregar("Rover → Entorno QUERY_REF " + id + " | Entorno devuelve "
                    + (lectura.getConfirmada() ? "la confirmación" : "la lectura inicial"));
                return;
            }
            if ("coordinador".equals(mensaje.getSender().getLocalName())) {
                Object contenido = myAgent.getContentManager().extractContent(mensaje);
                if (contenido instanceof Metricas) {
                    Metricas m = (Metricas) contenido;
                    entorno.getVentana().agregar("Coordinador -> Entorno INFORM " + m.getMision()
                        + " | respuestas " + m.getRespuestas() + "/" + m.getPropuestas()
                        + ", promedio " + m.getPromedioUs() + " µs");
                    return;
                }
                if (!entorno.hayMision(mensaje.getConversationId())) return;
                Propuesta p = Mensajes.leer(myAgent, mensaje, Propuesta.class);
                entorno.getVentana().sensores(p.getLectura());
                entorno.getVentana().regla(p.getAccion() + " · " + p.getMotivo());
                entorno.getVentana().agregar("Coordinador → Entorno INFORM " + p.getMision()
                    + ": " + p.getAccion() + " | " + p.getMotivo());
            } else {
                Object contenido = myAgent.getContentManager().extractContent(mensaje);
                if (contenido instanceof EstadoRover) {
                    EstadoRover e = (EstadoRover) contenido;
                    entorno.getVentana().telemetria(e.getBateria(), e.getMuestras());
                    if ("consulta".equals(e.getMision())) {
                        entorno.getVentana().agregar("Rover → Entorno INFORM batería=" + e.getBateria());
                    }
                } else if (contenido instanceof Resultado) {
                    Resultado r = (Resultado) contenido;
                    if (!entorno.hayMision(mensaje.getConversationId())) return;
                    entorno.getVentana().telemetria(r.getBateria(), r.getMuestras());
                    entorno.getVentana().resultado(r);
                    entorno.getVentana().estado(r.getMision() + " | " + (r.getFinalizado()
                        ? (r.getObjetivo() ? "OBJETIVO CUMPLIDO" : "REGRESO PREVENTIVO")
                        : r.getAccion()));
                    entorno.getVentana().agregar("Rover → Entorno INFORM " + r.getMision()
                        + ": " + r.getAccion() + " | " + r.getDetalle());
                    if (r.getAccion().startsWith("EN CURSO ")) return;
                    myAgent.send(Mensajes.crear(myAgent, ACLMessage.INFORM,
                        "coordinador", r.getMision(), r));
                    entorno.getVentana().agregar("Entorno → Coordinador INFORM " + r.getMision()
                        + " | resultado " + (r.getFinalizado() ? "final" : "parcial"));
                    if (!r.getFinalizado()) {
                        entorno.getVentana().estado(r.getMision() + " | " + r.getAccion() + " → siguiente etapa");
                        return;
                    }
                    Long inicio = entorno.finalizarMision(r.getMision());
                    if (inicio != null) entorno.getVentana().tiempo((System.nanoTime() - inicio) / 1_000_000);
                    entorno.getVentana().terminar();
                }
            }
        } catch (Exception e) { System.err.println("[entorno] " + e.getMessage()); }
    }
}

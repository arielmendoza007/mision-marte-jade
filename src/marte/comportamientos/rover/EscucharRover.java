package marte.comportamientos.rover;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import marte.agentes.Rover;
import marte.modelo.Exploracion;
import marte.modelo.Mensajes;
import marte.modelo.Observacion;
import marte.modelo.OntologiaMarte;
import marte.modelo.Propuesta;

/** Recibe órdenes del Entorno y propuestas del Coordinador. */
public class EscucharRover extends CyclicBehaviour {
    private final Rover rover;
    private final MessageTemplate propuesta = MessageTemplate.and(
        MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
        MessageTemplate.MatchSender(new AID("coordinador", AID.ISLOCALNAME)));
    private final MessageTemplate entorno = MessageTemplate.and(
        MessageTemplate.MatchSender(new AID("entorno", AID.ISLOCALNAME)),
        MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM)));

    public EscucharRover(Rover rover) {
        super(rover);
        this.rover = rover;
    }

    @Override public void action() {
        ACLMessage mensaje = myAgent.receive(MessageTemplate.and(
            MessageTemplate.MatchOntology(OntologiaMarte.NOMBRE),
            MessageTemplate.or(propuesta, entorno)));
        if (mensaje == null) { block(); return; }
        try {
            String id = mensaje.getConversationId();
            if (mensaje.getPerformative() == ACLMessage.REQUEST) {
                if ("consulta".equals(id)) { rover.informarEstado(id); return; }
                Exploracion inicio = Mensajes.leer(myAgent, mensaje, Exploracion.class);
                if (!rover.comenzarMision(id, inicio)) return;
                rover.informarEstado(id);
                rover.pedirLectura(false, false, false);
                return;
            }
            if (!rover.esMisionActiva(id)) return;
            if (mensaje.getPerformative() == ACLMessage.INFORM) {
                Observacion datos = Mensajes.leer(myAgent, mensaje, Observacion.class);
                if (id.equals(datos.getLectura().getMision())) {
                    myAgent.send(Mensajes.crear(myAgent, ACLMessage.REQUEST, "coordinador", id, datos));
                }
                return;
            }
            Propuesta p = Mensajes.leer(myAgent, mensaje, Propuesta.class);
            if (!id.equals(p.getMision())) throw new IllegalArgumentException("Misión distinta");
            boolean agotada = "ESPERAR".equals(p.getAccion()) && rover.getEsperas() >= 2;
            boolean aceptada = !rover.isOcupado() && !agotada
                && rover.getBateria() >= rover.costo(p.getAccion()) + rover.getCostoRegreso();
            int tipo = aceptada ? ACLMessage.ACCEPT_PROPOSAL : ACLMessage.REJECT_PROPOSAL;
            myAgent.send(Mensajes.crear(myAgent, tipo, "coordinador", id, p));
            if (rover.isOcupado()) return;
            rover.setOcupado(true);
            if (aceptada) {
                rover.avisarAccion(p, p.getAccion(), "Aceptada por el rover");
                myAgent.addBehaviour(new EjecutarAccion(rover, p));
            } else {
                String motivo = agotada ? "Polvo alto tras dos esperas"
                    : "Disponibles " + rover.getBateria() + "; necesarias "
                        + (rover.costo(p.getAccion()) + rover.getCostoRegreso())
                        + " (" + rover.costo(p.getAccion()) + " de acción + 20 de regreso)";
                rover.iniciarRegreso(p, false, p.getAccion() + " rechazada. " + motivo);
            }
        } catch (RuntimeException e) { System.err.println("[rover] " + e.getMessage()); }
    }
}

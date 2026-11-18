package marte.comportamientos.coordinador;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import marte.agentes.Coordinador;
import marte.modelo.Lectura;
import marte.modelo.Mensajes;
import marte.modelo.Metricas;
import marte.modelo.Observacion;
import marte.modelo.OntologiaMarte;
import marte.modelo.Propuesta;
import marte.modelo.Resultado;

/** Filtra solicitudes de lecturas, respuestas a propuestas y resultados. */
public class EscucharCoordinador extends CyclicBehaviour {
    private final Coordinador coordinador;
    private final MessageTemplate lectura = MessageTemplate.and(
        MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
        MessageTemplate.MatchSender(new AID("rover", AID.ISLOCALNAME)));
    private final MessageTemplate respuesta = MessageTemplate.and(
        MessageTemplate.MatchSender(new AID("rover", AID.ISLOCALNAME)),
        MessageTemplate.or(
            MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
            MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL)));
    private final MessageTemplate resultado = MessageTemplate.and(
        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
        MessageTemplate.MatchSender(new AID("entorno", AID.ISLOCALNAME)));

    public EscucharCoordinador(Coordinador coordinador) {
        super(coordinador);
        this.coordinador = coordinador;
    }

    @Override public void action() {
        ACLMessage mensaje = myAgent.receive(MessageTemplate.and(
            MessageTemplate.MatchOntology(OntologiaMarte.NOMBRE),
            MessageTemplate.or(lectura, MessageTemplate.or(respuesta, resultado))));
        if (mensaje == null) { block(); return; }
        try {
            if (mensaje.getPerformative() == ACLMessage.REQUEST) {
                Lectura datos = Mensajes.leer(myAgent, mensaje, Observacion.class).getLectura();
                myAgent.addBehaviour(new EvaluarLectura(coordinador, datos));
            } else if (mensaje.getPerformative() == ACLMessage.INFORM) {
                Resultado fin = Mensajes.leer(myAgent, mensaje, Resultado.class);
                System.out.println("[coordinador] " + fin.getMision()
                    + (fin.getFinalizado() ? " terminó: " : " sigue tras: ") + fin.getAccion());
                if (fin.getFinalizado()) {
                    String id = fin.getMision();
                    Metricas m = coordinador.finalizarMision(id);
                    myAgent.send(Mensajes.crear(myAgent, ACLMessage.INFORM, "entorno", id, m));
                    System.out.println("[coordinador] " + id + ": respuestas="
                        + m.getRespuestas() + "/" + m.getPropuestas()
                        + ", latencia media=" + m.getPromedioUs() + " µs");
                }
            } else {
                Propuesta p = Mensajes.leer(myAgent, mensaje, Propuesta.class);
                String estado = mensaje.getPerformative() == ACLMessage.REJECT_PROPOSAL ? "RECHAZADA" : "ACEPTADA";
                coordinador.registrarRespuesta(p);
                System.out.println("[coordinador] " + p.getMision() + " " + estado + " por rover");
            }
        } catch (RuntimeException e) { System.err.println("[coordinador] " + e.getMessage()); }
    }
}

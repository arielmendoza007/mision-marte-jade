package marte.agentes;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import marte.modelo.Lectura;
import marte.modelo.Mensajes;
import marte.modelo.OntologiaMarte;
import marte.modelo.Propuesta;
import marte.modelo.Resultado;

/** Sistema experto: decide con reglas visibles y solicita la ejecuci�n. */
public class Coordinador extends Agent {
    @Override protected void setup() {
        OntologiaMarte.registrar(this);
        addBehaviour(new Escuchar());
        System.out.println("[coordinador] Listo para evaluar sectores");
    }

    private class Escuchar extends CyclicBehaviour {
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
            MessageTemplate.MatchSender(new AID("rover", AID.ISLOCALNAME)));

        @Override public void action() {
            ACLMessage mensaje = myAgent.receive(MessageTemplate.or(lectura,
                MessageTemplate.or(respuesta, resultado)));
            if (mensaje == null) { block(); return; }
            try {
                if (mensaje.getPerformative() == ACLMessage.REQUEST) {
                    Lectura datos = Mensajes.leer(myAgent, mensaje, marte.modelo.Observacion.class).getLectura();
                    addBehaviour(new Evaluar(datos));
                } else if (mensaje.getPerformative() == ACLMessage.INFORM) {
                    Resultado fin = Mensajes.leer(myAgent, mensaje, Resultado.class);
                    System.out.println("[coordinador] " + fin.getMision() + " termin�: " + fin.getAccion());
                } else {
                    Propuesta p = Mensajes.leer(myAgent, mensaje, Propuesta.class);
                    String estado = mensaje.getPerformative() == ACLMessage.REJECT_PROPOSAL ? "RECHAZADA" : "ACEPTADA";
                    System.out.println("[coordinador] " + p.getMision() + " " + estado + " por rover");

                }
            } catch (RuntimeException e) { System.err.println("[coordinador] " + e.getMessage()); }
        }
    }

    /** SimpleBehaviour termina al procesar exactamente una lectura. */
    private class Evaluar extends SimpleBehaviour {
        private final Lectura lectura;
        private boolean terminado;
        Evaluar(Lectura lectura) { this.lectura = lectura; }

        @Override public void action() {
            Propuesta p = new Propuesta();
            p.setMision(lectura.getMision());
            p.setLectura(lectura);
            if (lectura.getPolvo() >= 70) {
                p.setAccion("ESPERAR");
                p.setMotivo("R1: polvo >= 70; tomar muestras no es seguro");
            } else if (lectura.getObstaculo()) {
                p.setAccion("ESCANEAR");
                p.setMotivo("R2: obst�culo presente; examinar antes de actuar");
            } else if (lectura.getMineral() >= 65) {
                p.setAccion("MUESTRA");
                p.setMotivo("R3: se�al mineral >= 65 y entorno seguro");
            } else {
                p.setAccion("ESCANEAR");
                p.setMotivo("R4: se�al mineral baja; recopilar m�s datos");
            }
            myAgent.send(Mensajes.crear(myAgent, ACLMessage.PROPOSE, "rover",
                p.getMision(), p));
            myAgent.send(Mensajes.crear(myAgent, ACLMessage.INFORM, "entorno",
                p.getMision(), p));
            System.out.println("[coordinador] " + p.getMision() + ": " + p.getMotivo());
            terminado = true;
        }
        @Override public boolean done() { return terminado; }
    }
}

package marte.agentes;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.HashMap;
import java.util.Map;
import marte.modelo.Lectura;
import marte.modelo.Metricas;
import marte.modelo.Mensajes;
import marte.modelo.OntologiaMarte;
import marte.modelo.Propuesta;
import marte.modelo.Resultado;

/** Sistema experto: decide con reglas visibles y solicita la ejecución. */
public class Coordinador extends Agent {
    private final Map<String, Long> enviada = new HashMap<>();
    private final Map<String, Integer> propuestas = new HashMap<>();
    private final Map<String, Integer> respuestas = new HashMap<>();
    private final Map<String, Long> sumaUs = new HashMap<>();
    @Override protected void setup() {
        OntologiaMarte.registrar(this);
        addBehaviour(new Escuchar());
        System.out.println("[coordinador] Reglas de ascenso listas");
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
            MessageTemplate.MatchSender(new AID("entorno", AID.ISLOCALNAME)));

        @Override public void action() {
            ACLMessage mensaje = myAgent.receive(MessageTemplate.and(
                MessageTemplate.MatchOntology(OntologiaMarte.NOMBRE),
                MessageTemplate.or(lectura, MessageTemplate.or(respuesta, resultado))));
            if (mensaje == null) { block(); return; }
            try {
                if (mensaje.getPerformative() == ACLMessage.REQUEST) {
                    Lectura datos = Mensajes.leer(myAgent, mensaje, marte.modelo.Observacion.class).getLectura();
                    addBehaviour(new Evaluar(datos));
                } else if (mensaje.getPerformative() == ACLMessage.INFORM) {
                    Resultado fin = Mensajes.leer(myAgent, mensaje, Resultado.class);
                    System.out.println("[coordinador] " + fin.getMision()
                        + (fin.getFinalizado() ? " terminó: " : " sigue tras: ") + fin.getAccion());
                    if (fin.getFinalizado()) {
                        String id = fin.getMision();
                        Metricas m = new Metricas();
                        m.setMision(id);
                        m.setPropuestas(propuestas.getOrDefault(id, 0));
                        m.setRespuestas(respuestas.getOrDefault(id, 0));
                        m.setPromedioUs(sumaUs.getOrDefault(id, 0L) / Math.max(1, m.getRespuestas()));
                        send(Mensajes.crear(myAgent, ACLMessage.INFORM, "entorno", id, m));
                        System.out.println("[coordinador] " + id + ": respuestas="
                            + m.getRespuestas() + "/" + m.getPropuestas()
                            + ", latencia media=" + m.getPromedioUs() + " µs");
                        propuestas.remove(id); respuestas.remove(id); sumaUs.remove(id); enviada.remove(id);
                    }
                } else {
                    Propuesta p = Mensajes.leer(myAgent, mensaje, Propuesta.class);
                    String estado = mensaje.getPerformative() == ACLMessage.REJECT_PROPOSAL ? "RECHAZADA" : "ACEPTADA";
                    String id = p.getMision();
                    respuestas.merge(id, 1, Integer::sum);
                    Long inicio = enviada.remove(id);
                    if (inicio != null) sumaUs.merge(id, (System.nanoTime() - inicio) / 1_000, Long::sum);
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
                p.setMotivo("R1: polvo >= 70; esperar antes de avanzar");
            } else if (lectura.getObstaculo()) {
                p.setAccion("RODEAR");
                p.setMotivo("R2: bloque de lava en la ladera; usar desvío");
            } else if ("Caldera".equals(lectura.getSector().getNombre())
                       && lectura.getMineral() >= 65 && !lectura.getConfirmada()) {
                p.setAccion("ESCANEAR");
                p.setMotivo("R3: señal en la caldera; confirmar antes de extraer");
            } else if ("Caldera".equals(lectura.getSector().getNombre())
                       && lectura.getMineral() >= 65) {
                p.setAccion("MUESTRA");
                p.setMotivo("R4: lectura confirmada y terreno seguro; tomar muestra");
            } else {
                p.setAccion("AVANZAR");
                p.setMotivo("R5: tramo seguro; continuar hacia la caldera");
            }
            myAgent.send(Mensajes.crear(myAgent, ACLMessage.PROPOSE, "rover",
                p.getMision(), p));
            propuestas.merge(p.getMision(), 1, Integer::sum);
            enviada.put(p.getMision(), System.nanoTime());
            myAgent.send(Mensajes.crear(myAgent, ACLMessage.INFORM, "entorno",
                p.getMision(), p));
            System.out.println("[coordinador] " + p.getMision() + ": " + p.getMotivo());
            terminado = true;
        }
        @Override public boolean done() { return terminado; }
    }
}

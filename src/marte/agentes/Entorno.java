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
import marte.modelo.OntologiaMarte;
import marte.modelo.Propuesta;
import marte.modelo.Resultado;
import marte.modelo.Exploracion;
import marte.modelo.Observacion;

/** Intermediario entre el operador y los agentes aut�nomos. */
public class Entorno extends Agent {
    private volatile ConsolaMision ventana;
    private int siguienteMision = 1;
    private final Map<String, Long> inicios = new HashMap<>();

    @Override protected void setup() {
        OntologiaMarte.registrar(this);
        addBehaviour(new Escuchar());
        try { SwingUtilities.invokeAndWait(() -> {
            ventana = new ConsolaMision(this::iniciar, this::consultarRover);
            ventana.setVisible(true);
        }); } catch (Exception ex) { throw new IllegalStateException("No se pudo abrir la consola", ex); }
        System.out.println("[entorno] Consola de misi�n iniciada");
    }

    private void iniciar(String sector) {
        addBehaviour(new OneShotBehaviour() {
            @Override public void action() {
                String id = "M-" + siguienteMision++;
                inicios.put(id, System.nanoTime());
                addBehaviour(new jade.core.behaviours.WakerBehaviour(myAgent, 8000) {
                    protected void onWake() {
                        if (inicios.remove(id) == null) return;
                        ventana.estado(id + " | Sin respuesta. Consulta el rover y comprueba su conexi�n.");
                        ventana.agregar(id + " venci� despu�s de 8 segundos. Verifica la segunda terminal.");
                        ventana.terminar();
                    }
                });
                send(Mensajes.crear(myAgent, ACLMessage.REQUEST, "rover", id, new Exploracion(sector)));
                ventana.estado(id + " | Explorando " + sector);
                ventana.agregar("Entorno  Rover REQUEST " + id + " explorar " + sector);
            }
        });
    }

    private void consultarRover() {
        addBehaviour(new OneShotBehaviour() {
            @Override public void action() {
                EstadoRover consulta = new EstadoRover();
                consulta.setMision("consulta");
                send(Mensajes.crear(myAgent, ACLMessage.REQUEST, "rover", "consulta", consulta));
                ventana.agregar("Entorno  Rover REQUEST estado");
            }
        });
    }

    private final Map<String,Integer> visitas = new HashMap<>();

    // Tres puntos por sector, en orden c�clico para poder repetir la demostraci�n.
    private Lectura medir(Exploracion orden, String id) {
        String nombre = orden.getSector().getNombre();
        int punto = (visitas.merge(nombre, 1, Integer::sum) - 1) % 3;
        int[] polvo, mineral;
        boolean obstaculo = false;
        switch (nombre.charAt(0)) {
            case 'A':
                polvo = new int[]{15, 19, 22}; mineral = new int[]{30, 70, 42};
                obstaculo = punto == 1; break;
            case 'B':
                polvo = new int[]{20, 24, 18}; mineral = new int[]{85, 78, 91}; break;
            case 'C':
                polvo = new int[]{85, 72, 55}; mineral = new int[]{90, 83, 77}; break;
            default: throw new IllegalArgumentException("Sector desconocido: " + nombre);
        }
        Lectura l = new Lectura();
        orden.getSector().setPunto(punto + 1);
        l.setSector(orden.getSector()); l.setMision(id);
        l.setPolvo(polvo[punto]); l.setMineral(mineral[punto]); l.setObstaculo(obstaculo);
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
            ACLMessage mensaje = myAgent.receive(MessageTemplate.or(coordinador, rover));
            if (mensaje == null) { block(); return; }
            try {
                if (mensaje.getPerformative() == ACLMessage.QUERY_REF) {
                    String id = mensaje.getConversationId();
                    if (!inicios.containsKey(id)) return;
                    Lectura lectura = medir(Mensajes.leer(myAgent, mensaje, Exploracion.class), id);
                    send(Mensajes.crear(myAgent, ACLMessage.INFORM, "rover", id, new Observacion(lectura)));
                    ventana.agregar("Rover  Entorno QUERY_REF " + id + " | Entorno devuelve la lectura");
                    return;
                }
                if ("coordinador".equals(mensaje.getSender().getLocalName())) {
                    if (!inicios.containsKey(mensaje.getConversationId())) return;
                    Propuesta p = Mensajes.leer(myAgent, mensaje, Propuesta.class);
                    ventana.sensores(p.getLectura());
                    ventana.regla(p.getMotivo());
                    ventana.agregar("Coordinador  Entorno INFORM " + p.getMision()
                        + ": " + p.getAccion() + " | " + p.getMotivo());
                } else {
                    Object contenido = myAgent.getContentManager().extractContent(mensaje);
                    if (contenido instanceof EstadoRover) {
                        EstadoRover e = (EstadoRover) contenido;
                        ventana.telemetria(e.getBateria(), e.getMuestras());
                        if ("recarga".equals(e.getMision())) ventana.agregar("Rover en base. Recarga completada: 80 unidades.");
                        if ("consulta".equals(e.getMision())) {
                            ventana.agregar("Rover  Entorno INFORM bater�a=" + e.getBateria());
                        }
                    } else if (contenido instanceof Resultado) {
                        Resultado r = (Resultado) contenido;
                        if (!inicios.containsKey(mensaje.getConversationId())) return;
                        ventana.telemetria(r.getBateria(), r.getMuestras());
                        ventana.estado(r.getMision() + " | " + r.getAccion());
                        ventana.agregar("Rover  Entorno INFORM " + r.getMision()
                            + ": " + r.getAccion() + " | " + r.getDetalle());
                        Long inicio = inicios.remove(r.getMision());
                        if (inicio != null) ventana.tiempo((System.nanoTime() - inicio) / 1_000_000);
                        ventana.terminar();
                    }
                }
            } catch (Exception e) { System.err.println("[entorno] " + e.getMessage()); }
        }
    }
}

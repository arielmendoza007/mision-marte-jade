package marte.comportamientos.coordinador;

import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import marte.agentes.Coordinador;
import marte.modelo.Lectura;
import marte.modelo.Mensajes;
import marte.modelo.Propuesta;

/** Decide una acción para una lectura y finaliza tras una sola evaluación. */
public class EvaluarLectura extends SimpleBehaviour {
    private final Coordinador coordinador;
    private final Lectura lectura;
    private boolean terminado;

    public EvaluarLectura(Coordinador coordinador, Lectura lectura) {
        super(coordinador);
        this.coordinador = coordinador;
        this.lectura = lectura;
    }

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
        myAgent.send(Mensajes.crear(myAgent, ACLMessage.PROPOSE, "rover", p.getMision(), p));
        coordinador.registrarPropuesta(p);
        myAgent.send(Mensajes.crear(myAgent, ACLMessage.INFORM, "entorno", p.getMision(), p));
        System.out.println("[coordinador] " + p.getMision() + ": " + p.getMotivo());
        terminado = true;
    }

    @Override public boolean done() { return terminado; }
}

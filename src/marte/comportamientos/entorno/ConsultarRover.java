package marte.comportamientos.entorno;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import marte.agentes.Entorno;
import marte.modelo.EstadoRover;
import marte.modelo.Mensajes;

/** Solicita la telemetría actual al Rover. */
public class ConsultarRover extends OneShotBehaviour {
    private final Entorno entorno;
    public ConsultarRover(Entorno entorno) {
        super(entorno);
        this.entorno = entorno;
    }
    @Override public void action() {
        EstadoRover consulta = new EstadoRover();
        consulta.setMision("consulta");
        myAgent.send(Mensajes.crear(myAgent, ACLMessage.REQUEST, "rover", "consulta", consulta));
        entorno.getVentana().agregar("Entorno → Rover REQUEST estado");
    }
}

package marte.comportamientos.rover;

import jade.core.behaviours.OneShotBehaviour;
import marte.agentes.Rover;

/** Envía el primer estado al iniciar el agente. */
public class PresentarseRover extends OneShotBehaviour {
    private final Rover rover;
    public PresentarseRover(Rover rover) {
        super(rover);
        this.rover = rover;
    }
    @Override public void action() { rover.informarEstado("inicio"); }
}

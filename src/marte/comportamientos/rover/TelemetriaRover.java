package marte.comportamientos.rover;

import jade.core.behaviours.TickerBehaviour;
import marte.agentes.Rover;

/** Notifica el estado del Rover periódicamente. */
public class TelemetriaRover extends TickerBehaviour {
    private final Rover rover;
    public TelemetriaRover(Rover rover, long periodo) {
        super(rover, periodo);
        this.rover = rover;
    }
    @Override protected void onTick() { rover.informarEstado("telemetria"); }
}

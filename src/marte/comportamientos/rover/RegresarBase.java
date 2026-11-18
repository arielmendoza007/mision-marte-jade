package marte.comportamientos.rover;

import jade.core.behaviours.WakerBehaviour;
import marte.agentes.Rover;
import marte.modelo.Propuesta;

/** Completa el regreso a la base después de 4200 ms. */
public class RegresarBase extends WakerBehaviour {
    private final Rover rover;
    private final Propuesta propuesta;
    private final boolean objetivo;
    private final String detalle;

    public RegresarBase(Rover rover, Propuesta propuesta, boolean objetivo, String detalle) {
        super(rover, 4200);
        this.rover = rover;
        this.propuesta = propuesta;
        this.objetivo = objetivo;
        this.detalle = detalle;
    }

    @Override protected void onWake() {
        rover.regresar(propuesta, objetivo, detalle + ". Llegada a base");
    }
}

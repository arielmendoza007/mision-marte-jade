package marte.agentes;

import jade.core.behaviours.WakerBehaviour;
import marte.modelo.Propuesta;

/** La acción ocupa un turno simulado. El buzón sigue atendiendo consultas. */
class EjecutarAccion extends WakerBehaviour {
    private final Rover rover;
    private final Propuesta propuesta;

    EjecutarAccion(Rover rover, Propuesta propuesta) {
        super(rover, 2200);
        this.rover = rover;
        this.propuesta = propuesta;
    }

    @Override protected void onWake() {
        rover.completarAccion(propuesta);
    }
}

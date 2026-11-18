package marte.comportamientos.rover;

import jade.core.behaviours.WakerBehaviour;
import marte.agentes.Rover;
import marte.modelo.Propuesta;

/** La acción ocupa un turno simulado. El buzón sigue atendiendo consultas. */
public class EjecutarAccion extends WakerBehaviour {
    private final Rover rover;
    private final Propuesta propuesta;

    public EjecutarAccion(Rover rover, Propuesta propuesta) {
        super(rover, 3200);
        this.rover = rover;
        this.propuesta = propuesta;
    }

    @Override protected void onWake() { rover.completarAccion(propuesta); }
}

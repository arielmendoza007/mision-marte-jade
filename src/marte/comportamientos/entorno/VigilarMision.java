package marte.comportamientos.entorno;

import jade.core.behaviours.WakerBehaviour;
import marte.agentes.Entorno;

/** Cancela una misión que supera su plazo máximo. */
public class VigilarMision extends WakerBehaviour {
    private final Entorno entorno;
    private final String id;

    public VigilarMision(Entorno entorno, String id, long plazo) {
        super(entorno, plazo);
        this.entorno = entorno;
        this.id = id;
    }

    @Override protected void onWake() { entorno.caducarMision(id); }
}

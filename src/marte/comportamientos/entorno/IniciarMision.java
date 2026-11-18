package marte.comportamientos.entorno;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import marte.agentes.Entorno;
import marte.modelo.Exploracion;
import marte.modelo.Mensajes;

/** Inicia una misión desde el botón de la interfaz. */
public class IniciarMision extends OneShotBehaviour {
    private final Entorno entorno;
    private final String escenario;

    public IniciarMision(Entorno entorno, String escenario) {
        super(entorno);
        this.entorno = entorno;
        this.escenario = escenario;
    }

    @Override public void action() {
        String id = entorno.registrarMision(escenario);
        myAgent.addBehaviour(new VigilarMision(entorno, id, 45000));
        Exploracion orden = new Exploracion("Falda");
        orden.setEscenario(escenario);
        myAgent.send(Mensajes.crear(myAgent, ACLMessage.REQUEST, "rover", id, orden));
        entorno.getVentana().estado(id + " | Ascenso iniciado");
        entorno.getVentana().agregar("Entorno → Rover REQUEST " + id + " | Olympus Mons: " + escenario);
    }
}

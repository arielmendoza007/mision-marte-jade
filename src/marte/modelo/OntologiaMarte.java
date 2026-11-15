package marte.modelo;

import jade.content.lang.sl.SLCodec;
import jade.content.onto.BasicOntology;
import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.core.Agent;

/** Esquema com�n que registran los tres agentes antes de hablar. */
public final class OntologiaMarte extends BeanOntology {
    public static final String NOMBRE = "MisionMarte";
    public static final String LENGUAJE = "fipa-sl";
    private static final OntologiaMarte INSTANCIA = new OntologiaMarte();

    private OntologiaMarte() {
        super(NOMBRE, BasicOntology.getInstance());
        try {
            add(Sector.class); add(Exploracion.class);
            add(Lectura.class); add(Observacion.class);
            add(Propuesta.class);
            add(Resultado.class);
            add(EstadoRover.class);
        } catch (BeanOntologyException e) {
            throw new IllegalStateException("No se pudo crear la ontolog�a", e);
        }
    }

    public static void registrar(Agent agente) {
        agente.getContentManager().registerLanguage(new SLCodec());
        agente.getContentManager().registerOntology(INSTANCIA);
    }

    public static OntologiaMarte getInstance() { return INSTANCIA; }
}

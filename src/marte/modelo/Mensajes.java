package marte.modelo;

import jade.content.ContentElement;
import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

/** Un solo lugar para construir y leer mensajes de la ontología. */
public final class Mensajes {
    private Mensajes() { }

    public static ACLMessage crear(Agent origen, int performativa, String destino,
                                   String mision, ContentElement contenido) {
        ACLMessage mensaje = new ACLMessage(performativa);
        mensaje.addReceiver(new AID(destino, AID.ISLOCALNAME));
        mensaje.setConversationId(mision);
        mensaje.setLanguage(OntologiaMarte.LENGUAJE);
        mensaje.setOntology(OntologiaMarte.NOMBRE);
        try {
            origen.getContentManager().fillContent(mensaje, contenido);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo codificar " + contenido.getClass().getSimpleName(), e);
        }
        return mensaje;
    }

    public static <T> T leer(Agent destino, ACLMessage mensaje, Class<T> tipo) {
        try {
            return tipo.cast(destino.getContentManager().extractContent(mensaje));
        } catch (Exception e) {
            throw new IllegalArgumentException("Contenido inválido de " + mensaje.getSender(), e);
        }
    }
}

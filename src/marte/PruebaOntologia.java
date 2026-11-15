package marte;

import jade.content.ContentManager;
import jade.content.lang.sl.SLCodec;
import jade.lang.acl.ACLMessage;
import marte.modelo.Lectura;
import marte.modelo.OntologiaMarte;
import marte.modelo.Propuesta;
import marte.modelo.Resultado;
import marte.modelo.Sector;

/** Prueba corta: el receptor recupera objetos anidados despu�s de codificarlos. */
public class PruebaOntologia {
    public static void main(String[] args) throws Exception {
        ContentManager gestor = new ContentManager();
        gestor.registerLanguage(new SLCodec());
        gestor.registerOntology(OntologiaMarte.getInstance());
        Lectura lectura = new Lectura();
        lectura.setMision("M-prueba");
        lectura.setSector(new Sector("B - Dep�sito"));
        lectura.setPolvo(20);
        lectura.setMineral(85);
        Propuesta propuesta = new Propuesta();
        propuesta.setMision("M-prueba");
        propuesta.setAccion("MUESTRA");
        propuesta.setMotivo("R3");
        propuesta.setLectura(lectura);
        Resultado resultado = new Resultado();
        resultado.setMision("M-prueba");
        resultado.setAccion("MUESTRA");
        resultado.setDetalle("R3");
        resultado.setBateria(65);
        resultado.setMuestras(1);
        resultado.setLectura(lectura);
        Propuesta recuperada = null;
        for (jade.content.ContentElement elemento : new jade.content.ContentElement[] {
                new marte.modelo.Observacion(lectura), propuesta, resultado }) {
            ACLMessage mensaje = new ACLMessage(ACLMessage.INFORM);
            mensaje.setLanguage(OntologiaMarte.LENGUAJE);
            mensaje.setOntology(OntologiaMarte.NOMBRE);
            gestor.fillContent(mensaje, elemento);
            Object recibido = gestor.extractContent(mensaje);
            if (!elemento.getClass().equals(recibido.getClass())) {
                throw new AssertionError("Tipo de objeto distinto al enviado");
            }
            if (recibido instanceof Propuesta) recuperada = (Propuesta) recibido;
            System.out.println("OK " + recibido.getClass().getSimpleName());
        }
        if (!"B - Dep�sito".equals(recuperada.getLectura().getSector().getNombre())) {
            throw new AssertionError("Sector anidado incorrecto");
        }
    }
}

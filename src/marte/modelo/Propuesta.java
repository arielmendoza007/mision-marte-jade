package marte.modelo;

import jade.content.Predicate;

/** Decisión del coordinador, pendiente de aceptación por el rover. */
public class Propuesta implements Predicate {
    private String mision;
    private String accion;
    private String motivo;
    private Lectura lectura;

    public Propuesta() { }
    public String getMision() { return mision; }
    public void setMision(String mision) { this.mision = mision; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Lectura getLectura() { return lectura; }
    public void setLectura(Lectura lectura) { this.lectura = lectura; }
}



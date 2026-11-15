package marte.modelo;

import jade.content.Predicate;

/** Acci�n realmente ejecutada, que puede diferir de la propuesta. */
public class Resultado implements Predicate {
    private String mision;
    private String accion;
    private String detalle;
    private int bateria;
    private int muestras;
    private Lectura lectura;

    public Resultado() { }
    public String getMision() { return mision; }
    public void setMision(String mision) { this.mision = mision; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public int getBateria() { return bateria; }
    public void setBateria(int bateria) { this.bateria = bateria; }
    public int getMuestras() { return muestras; }
    public void setMuestras(int muestras) { this.muestras = muestras; }
    public Lectura getLectura() { return lectura; }
    public void setLectura(Lectura lectura) { this.lectura = lectura; }
}

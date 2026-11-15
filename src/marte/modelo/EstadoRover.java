package marte.modelo;

import jade.content.Predicate;

/** Respuesta a una consulta directa Entorno -> Rover. */
public class EstadoRover implements Predicate {
    private String mision;
    private int bateria;
    private int muestras;

    public EstadoRover() { }
    public String getMision() { return mision; }
    public void setMision(String mision) { this.mision = mision; }
    public int getBateria() { return bateria; }
    public void setBateria(int bateria) { this.bateria = bateria; }
    public int getMuestras() { return muestras; }
    public void setMuestras(int muestras) { this.muestras = muestras; }
}

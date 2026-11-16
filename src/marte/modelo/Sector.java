package marte.modelo;

import jade.content.Concept;

/** Un lugar de la misión; viaja dentro de Lectura. */
public class Sector implements Concept {
    private String nombre;
    private int punto;

    public Sector() { }
    public Sector(String nombre) { this.nombre = nombre; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getPunto() { return punto; }
    public void setPunto(int punto) { this.punto = punto; }
}

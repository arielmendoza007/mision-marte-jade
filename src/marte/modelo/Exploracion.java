package marte.modelo;

import jade.content.Predicate;

/** Identifica el destino de una orden o solicitud de medici�n. */
public class Exploracion implements Predicate {
    private Sector sector;
    public Exploracion() { }
    public Exploracion(String nombre) { sector = new Sector(nombre); }
    public Sector getSector() { return sector; }
    public void setSector(Sector sector) { this.sector = sector; }
}

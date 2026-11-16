package marte.modelo;

import jade.content.Predicate;

/** Identifica el destino de una orden o solicitud de medición. */
public class Exploracion implements Predicate {
    private Sector sector;
    private boolean confirmacion;
    private boolean espera;
    private boolean desvio;
    private String escenario;
    public Exploracion() { }
    public Exploracion(String nombre) { sector = new Sector(nombre); }
    public Sector getSector() { return sector; }
    public void setSector(Sector sector) { this.sector = sector; }
    public boolean getConfirmacion() { return confirmacion; }
    public void setConfirmacion(boolean confirmacion) { this.confirmacion = confirmacion; }
    public boolean getEspera() { return espera; }
    public void setEspera(boolean espera) { this.espera = espera; }
    public boolean getDesvio() { return desvio; }
    public void setDesvio(boolean desvio) { this.desvio = desvio; }
    public String getEscenario() { return escenario; }
    public void setEscenario(String escenario) { this.escenario = escenario; }
}

package marte.modelo;

import jade.content.Concept;

/** Datos simulados del entorno para una misión. */
public class Lectura implements Concept {
    private String mision;
    private Sector sector;
    private int polvo;
    private int mineral;
    private boolean obstaculo;
    private boolean confirmada;

    public Lectura() { }
    public String getMision() { return mision; }
    public void setMision(String mision) { this.mision = mision; }
    public Sector getSector() { return sector; }
    public void setSector(Sector sector) { this.sector = sector; }
    public int getPolvo() { return polvo; }
    public void setPolvo(int polvo) { this.polvo = polvo; }
    public int getMineral() { return mineral; }
    public void setMineral(int mineral) { this.mineral = mineral; }
    public boolean getObstaculo() { return obstaculo; }
    public void setObstaculo(boolean obstaculo) { this.obstaculo = obstaculo; }
    public boolean getConfirmada() { return confirmada; }
    public void setConfirmada(boolean confirmada) { this.confirmada = confirmada; }
}

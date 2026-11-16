package marte.modelo;

import jade.content.Predicate;

/** Medidas observadas por el coordinador durante una expedición. */
public class Metricas implements Predicate {
    private String mision;
    private int propuestas;
    private int respuestas;
    private long promedioUs;

    public String getMision() { return mision; }
    public void setMision(String v) { mision = v; }
    public int getPropuestas() { return propuestas; }
    public void setPropuestas(int v) { propuestas = v; }
    public int getRespuestas() { return respuestas; }
    public void setRespuestas(int v) { respuestas = v; }
    public long getPromedioUs() { return promedioUs; }
    public void setPromedioUs(long v) { promedioUs = v; }
}

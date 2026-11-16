package marte.modelo;

import jade.content.Predicate;

/** Hecho comunicado al coordinador: hay una lectura para evaluar. */
public class Observacion implements Predicate {
    private Lectura lectura;
    public Observacion() { }
    public Observacion(Lectura lectura) { this.lectura = lectura; }
    public Lectura getLectura() { return lectura; }
    public void setLectura(Lectura lectura) { this.lectura = lectura; }
}

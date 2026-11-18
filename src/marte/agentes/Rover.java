package marte.agentes;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import marte.comportamientos.rover.EscucharRover;
import marte.comportamientos.rover.PresentarseRover;
import marte.comportamientos.rover.RegresarBase;
import marte.comportamientos.rover.TelemetriaRover;
import marte.modelo.EstadoRover;
import marte.modelo.Exploracion;
import marte.modelo.Mensajes;
import marte.modelo.OntologiaMarte;
import marte.modelo.Propuesta;
import marte.modelo.Resultado;

/** Mantiene la batería, posición y estado del Rover durante cada misión. */
public class Rover extends Agent {
    private static final int COSTO_REGRESO = 20;
    private static final String[] RUTA = {"Falda", "Ladera", "Caldera"};
    private int bateria = 80;
    private int muestras;
    private int tramo;
    private int esperas;
    private String misionActiva;
    private String escenario;
    private boolean ocupado;

    @Override protected void setup() {
        OntologiaMarte.registrar(this);
        addBehaviour(new PresentarseRover(this));
        addBehaviour(new EscucharRover(this));
        addBehaviour(new TelemetriaRover(this, 5000));
        System.out.println("[rover] Listo para ascender. Batería=" + bateria);
    }

    public boolean comenzarMision(String id, Exploracion inicio) {
        if (misionActiva != null) return false;
        misionActiva = id;
        escenario = inicio.getEscenario();
        tramo = 0; esperas = 0; muestras = 0;
        bateria = "BATERIA".equals(escenario) ? 50 : 80;
        return true;
    }

    public boolean esMisionActiva(String id) { return id != null && id.equals(misionActiva); }
    public boolean isOcupado() { return ocupado; }
    public void setOcupado(boolean ocupado) { this.ocupado = ocupado; }
    public int getEsperas() { return esperas; }
    public int getBateria() { return bateria; }
    public int getCostoRegreso() { return COSTO_REGRESO; }

    public void informarEstado(String id) {
        EstadoRover e = new EstadoRover();
        e.setMision(id); e.setBateria(bateria); e.setMuestras(muestras);
        send(Mensajes.crear(this, ACLMessage.INFORM, "entorno", id, e));
    }

    public void pedirLectura(boolean espera, boolean desvio, boolean confirmacion) {
        Exploracion orden = new Exploracion(RUTA[tramo]);
        orden.setEscenario(escenario);
        orden.setEspera(espera);
        orden.setDesvio(desvio);
        orden.setConfirmacion(confirmacion);
        send(Mensajes.crear(this, ACLMessage.QUERY_REF, "entorno", misionActiva, orden));
    }

    public int costo(String accion) {
        switch (accion) {
            case "AVANZAR": return 8;
            case "RODEAR": return 12;
            case "ESPERAR": return 1;
            case "ESCANEAR": return 5;
            case "MUESTRA": return 15;
            default: throw new IllegalArgumentException("Acción desconocida: " + accion);
        }
    }

    public void informarResultado(Resultado r) {
        r.setBateria(bateria);
        r.setMuestras(muestras);
        send(Mensajes.crear(this, ACLMessage.INFORM, "entorno", r.getMision(), r));
        System.out.println("[rover] " + r.getMision() + " " + r.getAccion()
            + ", batería=" + bateria + ", muestras=" + muestras);
    }

    public void regresar(Propuesta p, boolean objetivo, String motivo) {
        bateria = Math.max(0, bateria - COSTO_REGRESO);
        Resultado r = new Resultado();
        r.setMision(p.getMision()); r.setLectura(p.getLectura());
        r.setAccion("REGRESAR"); r.setFinalizado(true); r.setObjetivo(objetivo);
        r.setDetalle(motivo);
        informarResultado(r);
        misionActiva = null;
        ocupado = false;
    }

    public void avisarAccion(Propuesta p, String accion, String detalle) {
        Resultado inicio = new Resultado();
        inicio.setMision(p.getMision());
        inicio.setLectura(p.getLectura());
        inicio.setAccion("EN CURSO " + accion);
        inicio.setDetalle(detalle);
        informarResultado(inicio);
    }

    public void iniciarRegreso(Propuesta p, boolean objetivo, String detalle) {
        avisarAccion(p, "REGRESAR", detalle);
        addBehaviour(new RegresarBase(this, p, objetivo, detalle));
    }

    /** Actualiza el estado tras la acción del WakerBehaviour. */
    public void completarAccion(Propuesta p) {
        bateria -= costo(p.getAccion());
        Resultado r = new Resultado();
        r.setMision(p.getMision()); r.setLectura(p.getLectura());
        r.setAccion(p.getAccion()); r.setDetalle(p.getMotivo());
        r.setFinalizado(false); r.setObjetivo(false);
        if ("MUESTRA".equals(p.getAccion())) muestras = 1;
        informarResultado(r);
        ocupado = false;
        switch (p.getAccion()) {
            case "AVANZAR":
                tramo++;
                esperas = 0;
                pedirLectura(false, false, false);
                break;
            case "RODEAR": pedirLectura(false, true, false); break;
            case "ESPERAR":
                esperas++;
                pedirLectura(true, false, false);
                break;
            case "ESCANEAR": pedirLectura(false, false, true); break;
            case "MUESTRA":
                ocupado = true;
                iniciarRegreso(p, true, "Muestra a bordo");
                break;
            default: throw new IllegalStateException("Acción no prevista");
        }
    }
}

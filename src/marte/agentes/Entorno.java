package marte.agentes;

import jade.core.Agent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;
import marte.comportamientos.entorno.ConsultarRover;
import marte.comportamientos.entorno.EscucharEntorno;
import marte.comportamientos.entorno.IniciarMision;
import marte.gui.ConsolaMision;
import marte.modelo.Exploracion;
import marte.modelo.Lectura;
import marte.modelo.OntologiaMarte;
import marte.modelo.Sector;

/** Mantiene el estado de los escenarios y la interfaz de la misión. */
public class Entorno extends Agent {
    private volatile ConsolaMision ventana;
    private int siguienteMision = 1;
    private final Map<String, Long> inicios = new HashMap<>();
    private final Map<String, Lectura> pendientes = new HashMap<>();
    private final Map<String, String> escenarios = new HashMap<>();
    private final Map<String, Integer> esperas = new HashMap<>();
    private final Map<String, Boolean> desvios = new HashMap<>();

    @Override protected void setup() {
        OntologiaMarte.registrar(this);
        addBehaviour(new EscucharEntorno(this));
        try {
            SwingUtilities.invokeAndWait(() -> {
                ventana = new ConsolaMision(this::iniciar, this::consultarRover);
                ventana.setVisible(true);
            });
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo abrir la consola", ex);
        }
        System.out.println("[entorno] Consola de misión iniciada");
    }

    private void iniciar(String escenario) {
        addBehaviour(new IniciarMision(this, escenario));
    }

    private void consultarRover() {
        addBehaviour(new ConsultarRover(this));
    }

    public ConsolaMision getVentana() { return ventana; }

    public String registrarMision(String escenario) {
        String id = "M-" + siguienteMision++;
        inicios.put(id, System.nanoTime());
        escenarios.put(id, escenario);
        return id;
    }

    public boolean hayMision(String id) { return inicios.containsKey(id); }

    public void caducarMision(String id) {
        if (inicios.remove(id) == null) return;
        borrarDatos(id);
        ventana.estado(id + " | Sin respuesta. Consulta el rover y comprueba su conexión.");
        ventana.agregar(id + " venció después de 45 segundos. Verifica la segunda terminal.");
        ventana.terminar();
    }

    private void borrarDatos(String id) {
        pendientes.remove(id);
        escenarios.remove(id);
        esperas.remove(id);
        desvios.remove(id);
    }

    public Long finalizarMision(String id) {
        Long inicio = inicios.remove(id);
        borrarDatos(id);
        return inicio;
    }

    /** Genera lecturas reproducibles a partir de la orden enviada por Rover. */
    public Lectura medir(Exploracion orden, String id) {
        String nombre = orden.getSector().getNombre();
        if (orden.getEspera()) esperas.merge(id, 1, Integer::sum);
        if (orden.getDesvio()) desvios.put(id, true);
        if (orden.getConfirmacion()) {
            Lectura original = pendientes.remove(id);
            if (original == null || !original.getSector().getNombre().equals(nombre)) {
                throw new IllegalArgumentException("No hay lectura pendiente para confirmar en " + id);
            }
            Lectura confirmada = new Lectura();
            Sector sector = new Sector(nombre);
            sector.setPunto(original.getSector().getPunto());
            confirmada.setMision(id);
            confirmada.setSector(sector);
            confirmada.setPolvo(original.getPolvo());
            confirmada.setMineral(Math.max(0, original.getMineral() - 3));
            confirmada.setObstaculo(original.getObstaculo());
            confirmada.setConfirmada(true);
            return confirmada;
        }
        int punto, polvo, mineral;
        boolean obstaculo;
        switch (nombre) {
            case "Falda":
                punto = 1; polvo = 20; mineral = 25; obstaculo = false; break;
            case "Ladera":
                punto = 2; polvo = 27; mineral = 42;
                obstaculo = !desvios.getOrDefault(id, false); break;
            case "Caldera":
                punto = 3; mineral = 85; obstaculo = false;
                polvo = "TORMENTA".equals(escenarios.get(id)) ? 85
                    : esperas.getOrDefault(id, 0) == 0 ? 85 : 55;
                break;
            default: throw new IllegalArgumentException("Tramo desconocido: " + nombre);
        }
        Lectura l = new Lectura();
        orden.getSector().setPunto(punto);
        l.setSector(orden.getSector()); l.setMision(id);
        l.setPolvo(polvo); l.setMineral(mineral); l.setObstaculo(obstaculo);
        l.setConfirmada(false);
        pendientes.put(id, l);
        return l;
    }
}

package marte.agentes;

import jade.core.Agent;
import java.util.HashMap;
import java.util.Map;
import marte.comportamientos.coordinador.EscucharCoordinador;
import marte.modelo.Metricas;
import marte.modelo.OntologiaMarte;
import marte.modelo.Propuesta;

/** Guarda las métricas del sistema experto; las decisiones se ejecutan en los comportamientos. */
public class Coordinador extends Agent {
    private final Map<String, Long> enviada = new HashMap<>();
    private final Map<String, Integer> propuestas = new HashMap<>();
    private final Map<String, Integer> respuestas = new HashMap<>();
    private final Map<String, Long> sumaUs = new HashMap<>();

    @Override protected void setup() {
        OntologiaMarte.registrar(this);
        addBehaviour(new EscucharCoordinador(this));
        System.out.println("[coordinador] Reglas de ascenso listas");
    }

    public void registrarPropuesta(Propuesta p) {
        propuestas.merge(p.getMision(), 1, Integer::sum);
        enviada.put(p.getMision(), System.nanoTime());
    }

    public void registrarRespuesta(Propuesta p) {
        String id = p.getMision();
        respuestas.merge(id, 1, Integer::sum);
        Long inicio = enviada.remove(id);
        if (inicio != null) sumaUs.merge(id, (System.nanoTime() - inicio) / 1_000, Long::sum);
    }

    public Metricas finalizarMision(String id) {
        Metricas m = new Metricas();
        m.setMision(id);
        m.setPropuestas(propuestas.getOrDefault(id, 0));
        m.setRespuestas(respuestas.getOrDefault(id, 0));
        m.setPromedioUs(sumaUs.getOrDefault(id, 0L) / Math.max(1, m.getRespuestas()));
        propuestas.remove(id); respuestas.remove(id); sumaUs.remove(id); enviada.remove(id);
        return m;
    }
}

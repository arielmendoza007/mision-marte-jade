package marte.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import javax.swing.Timer;
import marte.modelo.Lectura;
import marte.modelo.Resultado;

/** Vista esquemática de las acciones informadas por el rover. */
public class MapaMarte extends JPanel {
    private String accion = "En base";
    private String ensayo = "NORMAL";
    private String sector = "Sin lectura";
    private boolean roca;
    private int polvo;
    private double posicion, origen, destino;
    private long inicio;
    private boolean animando;
    private final Timer cuadros = new Timer(40, e -> repaint());

    public MapaMarte() { setBackground(Color.WHITE); }

    public void seleccionarSector(String escenario) {
        cuadros.stop();
        animando = false;
        ensayo = escenario;
        accion = "Esperando lectura";
        sector = "Sin lectura";
        posicion = 0;
        polvo = 0;
        roca = false;
        repaint();
    }

    public void mostrarLectura(Lectura lectura) {
        sector = lectura.getSector().getNombre();
        polvo = lectura.getPolvo();
        roca = lectura.getObstaculo();
        repaint();
    }

    public void mostrarResultado(Resultado resultado) {
        if (resultado.getAccion().startsWith("EN CURSO ")) {
            accion = resultado.getAccion().substring(9);
            origen = posicion;
            destino = "REGRESAR".equals(accion) ? 0
                : "AVANZAR".equals(accion) ? Math.min(2, posicion + 1) : posicion;
            inicio = System.currentTimeMillis();
            animando = true;
            cuadros.start();
        } else {
            // El mensaje confirma la posición; el temporizador sólo dibuja.
            if (animando) posicion = destino;
            animando = false;
            cuadros.stop();
            accion = resultado.getAccion();
            if (resultado.getFinalizado()) {
                posicion = 0;
                roca = false;
                accion = resultado.getObjetivo() ? "En base: muestra entregada" : "En base: regreso preventivo";
            }
        }
        repaint();
    }

    @Override public void removeNotify() {
        cuadros.stop();
        super.removeNotify();
    }

    @Override protected void paintComponent(Graphics grafico) {
        super.paintComponent(grafico);
        Graphics2D g = (Graphics2D) grafico.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int ancho = getWidth(), alto = getHeight();
        int izquierda = 60, derecha = ancho - 65;
        int suelo = alto - 90, cima = 120;
        int medio = (izquierda + derecha) / 2, alturaMedia = (suelo + cima) / 2;
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.setColor(Color.DARK_GRAY);
        g.drawString("Recorrido simulado · " + ensayo, 14, 24);
        g.drawString("Última lectura: " + sector + " | Polvo: " + polvo + "/100", 14, 46);
        g.setColor(new Color(225, 207, 186));
        g.fillPolygon(new int[]{izquierda, derecha, ancho - 15, ancho - 15, izquierda},
            new int[]{suelo, cima, cima, suelo + 22, suelo + 22}, 5);
        g.setColor(new Color(120, 105, 90));
        g.setStroke(new BasicStroke(2));
        g.drawLine(izquierda, suelo, derecha, cima);
        int[] xs = {izquierda, medio, derecha};
        int[] ys = {suelo, alturaMedia, cima};
        String[] nombres = {"Base / Falda", "Ladera", "Caldera"};
        for (int i = 0; i < 3; i++) {
            g.setColor(Color.WHITE);
            g.fillOval(xs[i] - 5, ys[i] - 5, 10, 10);
            g.setColor(Color.DARK_GRAY);
            g.drawOval(xs[i] - 5, ys[i] - 5, 10, 10);
            int texto = g.getFontMetrics().stringWidth(nombres[i]);
            g.drawString(nombres[i], xs[i] - texto / 2, ys[i] + 35);
        }
        if (roca) {
            g.setColor(new Color(100, 90, 80));
            g.fillRect(medio + 20, alturaMedia - 20, 15, 14);
            g.drawString("Obstáculo", medio - 30, alturaMedia - 45);
        }
        if (polvo >= 70) {
            g.setColor(new Color(145, 100, 40));
            g.drawString("Polvo alto: esperar", Math.max(14, derecha - 140), cima - 35);
        }
        double progreso = animando ? Math.min(1, (System.currentTimeMillis() - inicio)
            / ("REGRESAR".equals(accion) ? 4200.0 : 3200.0)) : 0;
        double punto = animando ? origen + (destino - origen) * progreso : posicion;
        int x = izquierda + (int)((derecha - izquierda) * punto / 2);
        int y = suelo + (int)((cima - suelo) * punto / 2);
        // Rodear conserva el tramo: el desvío habilita el siguiente avance.
        if (animando && "RODEAR".equals(accion)) y -= (int)(32 * Math.sin(Math.PI * progreso));
        g.setColor(new Color(70, 90, 105));
        g.fillRect(x - 17, y - 23, 34, 16);
        g.drawLine(x, y - 23, x, y - 34);
        g.setColor(Color.DARK_GRAY);
        g.fillOval(x - 15, y - 10, 10, 10);
        g.fillOval(x + 5, y - 10, 10, 10);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("Rover: " + accion, 14, alto - 38);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.drawString("Campamento en la falda. Dibujo sin escala.", 14, alto - 16);
        g.dispose();
    }
}

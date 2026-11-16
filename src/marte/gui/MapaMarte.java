package marte.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.JPanel;
import javax.swing.Timer;
import marte.modelo.Lectura;
import marte.modelo.Resultado;

/** Perfil esquemático de Olympus Mons; el movimiento refleja mensajes de Entorno. */
public class MapaMarte extends JPanel {
    private static final double[] X = {.12, .32, .54, .78};
    private static final double[] Y = {.77, .67, .53, .38};
    private static final String[] NOMBRES = {"BASE", "FALDA", "LADERA", "CALDERA"};
    private static final Color CLARO = new Color(245, 234, 215);
    private static final Color ACENTO = new Color(243, 182, 111);
    private final char[] marcas = {'-', '-', '-'};
    private final Timer animacion;
    private double roverX = X[0], roverY = Y[0], destinoX = X[0], destinoY = Y[0];
    private int tramo = -1, pulso;
    private String accion = "EN BASE";

    public MapaMarte() {
        setPreferredSize(new Dimension(680, 440));
        animacion = new Timer(45, e -> {
            roverX += (destinoX - roverX) * .13;
            roverY += (destinoY - roverY) * .13;
            pulso = (pulso + 1) % 40;
            repaint();
        });
        animacion.start();
    }

    public void seleccionarSector(String escenario) {
        for (int i = 0; i < marcas.length; i++) marcas[i] = '-';
        roverX = destinoX = X[0]; roverY = destinoY = Y[0];
        tramo = -1; accion = "ASCENSO INICIADO";
        repaint();
    }

    public void mostrarLectura(Lectura lectura) {
        tramo = lectura.getSector().getPunto() - 1;
        if (tramo < 0 || tramo >= 3) return;
        destinoX = X[tramo + 1]; destinoY = Y[tramo + 1];
        accion = lectura.getConfirmada() ? "LECTURA CONFIRMADA" : "EXPLORANDO";
        repaint();
    }

    public void mostrarResultado(Resultado r) {
        int i = r.getLectura().getSector().getPunto() - 1;
        if (i >= 0 && i < 3 && !r.getFinalizado()) {
            switch (r.getAccion()) {
                case "AVANZAR": if (marcas[i] != 'D') marcas[i] = 'A'; break;
                case "RODEAR": marcas[i] = 'D'; break;
                case "ESPERAR": marcas[i] = 'T'; break;
                case "ESCANEAR": marcas[i] = 'E'; break;
                case "MUESTRA": marcas[i] = 'M'; break;
                default: break;
            }
        }
        accion = r.getFinalizado() ? (r.getObjetivo() ? "MUESTRA EN BASE" : "REGRESO PREVENTIVO")
            : r.getAccion();
        if (r.getFinalizado()) { destinoX = X[0]; destinoY = Y[0]; }
        repaint();
    }

    @Override protected void paintComponent(Graphics original) {
        super.paintComponent(original);
        Graphics2D g = (Graphics2D) original.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g.setPaint(new GradientPaint(0, 0, new Color(42, 35, 43), 0, h,
            new Color(121, 68, 57)));
        g.fillRect(0, 0, w, h);

        Path2D perfil = new Path2D.Double();
        perfil.moveTo(0, h);
        perfil.lineTo(0, h * .80);
        perfil.curveTo(w * .22, h * .79, w * .41, h * .57, w * .62, h * .43);
        perfil.curveTo(w * .72, h * .34, w * .79, h * .33, w * .88, h * .35);
        perfil.lineTo(w, h * .37);
        perfil.lineTo(w, h);
        perfil.closePath();
        g.setColor(new Color(150, 83, 65));
        g.fill(perfil);
        g.setColor(new Color(226, 166, 129, 125));
        g.setStroke(new BasicStroke(2f));
        g.draw(perfil);

        g.setColor(new Color(235, 205, 172, 90));
        g.setStroke(new BasicStroke(1f));
        for (int i = 1; i <= 3; i++) {
            int y = (int)(h * (.38 + i * .13));
            g.drawLine(20, y, w - 18, y);
        }
        g.setColor(CLARO);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString("OLYMPUS MONS  /  PERFIL DE RUTA SIMULADA", 22, 30);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.drawString("Del campamento en la ladera al borde de la caldera", 22, 47);

        g.setColor(new Color(255, 222, 184, 165));
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            1f, new float[]{7f, 7f}, 0f));
        for (int i = 0; i < 3; i++) {
            g.drawLine((int)(X[i] * w), (int)(Y[i] * h),
                (int)(X[i + 1] * w), (int)(Y[i + 1] * h));
        }
        g.setStroke(new BasicStroke(1f));

        for (int i = 0; i < 4; i++) {
            int x = (int)(X[i] * w), y = (int)(Y[i] * h);
            boolean activo = i == tramo + 1;
            g.setColor(new Color(34, 39, 47));
            g.fillOval(x - 17, y - 17, 34, 34);
            g.setColor(activo ? ACENTO : new Color(226, 210, 189));
            g.setStroke(new BasicStroke(activo ? 2.5f : 1.5f));
            g.drawOval(x - 17, y - 17, 34, 34);
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            centrar(g, i == 0 ? "B" : marcas[i - 1] == '-' ? "" + i : "" + marcas[i - 1], x, y + 5);
            g.setColor(CLARO);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            centrar(g, NOMBRES[i], x, y - 29);
        }

        if (tramo == 2 && ("ESCANEAR".equals(accion) || "LECTURA CONFIRMADA".equals(accion))) {
            int x = (int)(roverX * w), y = (int)(roverY * h);
            int radio = 27 + pulso / 2;
            g.setColor(new Color(117, 219, 217, 150 - pulso * 3));
            g.setStroke(new BasicStroke(2f));
            g.drawOval(x - radio, y - radio, 2 * radio, 2 * radio);
        }
        dibujarRover(g, (int)(roverX * w), (int)(roverY * h));
        g.setColor(new Color(25, 31, 39, 220));
        g.fillRoundRect(19, h - 42, Math.min(330, w - 38), 25, 8, 8);
        g.setColor(CLARO);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString("ROVER  /  " + accion, 29, h - 25);
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.drawString("A avance   D desvío   T espera   E escaneo   M muestra", 22, h - 54);
        g.dispose();
    }

    private void dibujarRover(Graphics2D g, int x, int y) {
        g.setColor(new Color(25, 33, 40));
        g.fillOval(x - 19, y + 8, 12, 12);
        g.fillOval(x + 7, y + 8, 12, 12);
        g.setColor(new Color(224, 228, 222));
        g.fillRoundRect(x - 22, y - 9, 44, 23, 6, 6);
        g.setColor(new Color(55, 71, 79));
        g.fillRect(x - 12, y - 4, 24, 9);
        g.setColor(ACENTO);
        g.fillOval(x + 6, y - 2, 5, 5);
        g.drawLine(x, y - 9, x, y - 21);
        g.fillOval(x - 2, y - 24, 5, 5);
    }

    private void centrar(Graphics2D g, String t, int x, int y) {
        FontMetrics m = g.getFontMetrics();
        g.drawString(t, x - m.stringWidth(t) / 2, y);
    }

    @Override public void removeNotify() { animacion.stop(); super.removeNotify(); }
}

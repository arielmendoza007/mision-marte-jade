package marte.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import javax.swing.Timer;
import marte.modelo.Lectura;
import marte.modelo.Resultado;

/** Cámara frontal. Cada movimiento comienza con un aviso recibido del rover. */
public class MapaMarte extends JPanel {
    private String lugar = "Base";
    private String accion = "";
    private String ensayo = "NORMAL";
    private boolean roca;
    private int polvo;
    private long inicio;
    private final Timer cuadros = new Timer(50, e -> repaint());

    public void seleccionarSector(String escenario) {
        ensayo = escenario;
        lugar = "Base";
        polvo = 0;
        roca = false;
        detener();
    }

    public void mostrarLectura(Lectura lectura) {
        lugar = lectura.getSector().getNombre();
        polvo = lectura.getPolvo();
        roca = lectura.getObstaculo();
        repaint();
    }

    public void mostrarResultado(Resultado resultado) {
        detener();
        if (resultado.getFinalizado()) {
            lugar = "Base";
            roca = false;
            polvo = 0;
        } else if (resultado.getAccion().startsWith("EN CURSO ")) {
            accion = resultado.getAccion().substring(9);
            inicio = System.currentTimeMillis();
            cuadros.start();
        }
        repaint();
    }

    private void detener() {
        accion = "";
        cuadros.stop();
        repaint();
    }

    @Override public void removeNotify() {
        cuadros.stop();
        super.removeNotify();
    }

    @Override protected void paintComponent(Graphics grafico) {
        super.paintComponent(grafico);
        Graphics2D g = (Graphics2D) grafico.create();
        double progreso = Math.min(1, (System.currentTimeMillis() - inicio)
            / ("REGRESAR".equals(accion) ? 4200.0 : 3200.0));
        if (progreso >= 1) cuadros.stop();
        boolean marcha = "AVANZAR".equals(accion) || "RODEAR".equals(accion) || "REGRESAR".equals(accion);
        int lateral = "RODEAR".equals(accion) ? (int)(115 * Math.sin(Math.PI * progreso)) : 0;
        // Coordenadas de cámara en un lienzo de 480 por 260.
        g.scale(getWidth() / 480.0, getHeight() / 260.0);
        g.setColor(new Color(221, 157, 116));
        g.fillRect(0, 0, 480, 260);
        g.setColor(new Color(255, 218, 155));
        g.fillOval(370, 31, 21, 21);
        g.setColor(new Color(146, 86, 74));
        g.fillPolygon(new int[]{0,55,119,178,251,286,349,405,480,480,0},
            new int[]{128,111,92,85,57,62,82,89,105,164,164}, 11);
        g.setColor(new Color(179, 103, 74));
        g.fillPolygon(new int[]{0,76,140,213,310,392,480,480,0},
            new int[]{130,117,121,96,106,111,98,260,260}, 9);
        g.setColor(new Color(195, 125, 85));
        g.fillPolygon(new int[]{236,253,420,56}, new int[]{117,117,260,260}, 4);
        for (int i = 0; i < 30; i++) {
            double z = (i / 30.0 + (marcha ? progreso * .65 : 0)) % 1;
            int x = (int)(240 + ((i * 73 % 451) - 225) * z * 2) - lateral;
            int y = (int)(119 + 132 * z * z);
            g.setColor(new Color(125, 76, 61));
            g.fillRect(x, y, 1 + (int)(8*z), 1 + (int)(3*z));
        }
        if (roca) {
            g.translate(-lateral, 0);
            g.setColor(new Color(83, 58, 61));
            g.fillPolygon(new int[]{198,208,236,269,282,258,210},
                new int[]{166,145,133,143,163,171,170}, 7);
            g.translate(lateral, 0);
        }
        if (polvo >= 70) {
            g.setColor(new Color(233, 172, 117, 156));
            g.fillRect(0, 0, 480, 260);
        }
        if ("ESCANEAR".equals(accion)) {
            g.setColor(new Color(177, 238, 196));
            g.drawRect(198, 115, 84, 51);
            g.drawLine(198, 116 + (int)(48*progreso), 282, 116 + (int)(48*progreso));
        }
        g.setColor(new Color(50, 54, 56));
        g.fillPolygon(new int[]{0,48,109,345,425,480,480,0},
            new int[]{210,223,233,233,219,211,260,260}, 8);
        g.dispose();
        grafico.setFont(new Font("SansSerif", Font.BOLD, 16));
        grafico.setColor(Color.WHITE);
        grafico.drawString("Cámara frontal   /   " + lugar, 14, getHeight()-15);
        grafico.setColor(new Color(49, 43, 42));
        grafico.drawString("Base   ·   Falda   ·   Ladera   ·   Caldera", 14, 25);
        grafico.setFont(new Font("SansSerif", Font.PLAIN, 14));
        grafico.drawString("Ensayo " + ensayo, 14, 45);
    }
}

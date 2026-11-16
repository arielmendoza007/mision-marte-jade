import jade.Boot;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/** Ensayo local con la interfaz real; cada escenario inicia una expedición completa. */
public class EnsayoConsola {
    private static final List<Component> controles = new ArrayList<>();

    private static void recorrer(Container raiz) {
        for (Component c : raiz.getComponents()) {
            controles.add(c);
            if (c instanceof Container) recorrer((Container) c);
        }
    }

    private static String leer(Class<?> tipo, String prefijo) throws Exception {
        String[] valor = {""};
        SwingUtilities.invokeAndWait(() -> {
            for (Component c : controles) if (tipo.isInstance(c)) {
                String texto = c instanceof JLabel ? ((JLabel) c).getText() : ((JTextArea) c).getText();
                if (texto.startsWith(prefijo)) valor[0] = texto;
            }
        });
        return valor[0];
    }

    private static void esperar(String prefijo) throws Exception {
        for (int i = 0; i < 220; i++) {
            if (!leer(JLabel.class, prefijo).isEmpty()) return;
            Thread.sleep(100);
        }
        throw new AssertionError("No apareció " + prefijo + "; bitácora=" + leer(JTextArea.class, ""));
    }

    private static void esperarRegistro(String fragmento) throws Exception {
        for (int i = 0; i < 60; i++) {
            if (leer(JTextArea.class, "").contains(fragmento)) return;
            Thread.sleep(100);
        }
        throw new AssertionError("Falta en bitácora: " + fragmento);
    }

    public static void main(String[] args) throws Exception {
        Process remoto = null;
        try {
            int puerto;
            try (java.net.ServerSocket s = new java.net.ServerSocket(0)) { puerto = s.getLocalPort(); }
            boolean separado = args.length > 0 && args[0].equals("red");
            Boot.main(new String[]{"-nomtp", "-local-host", "127.0.0.1", "-port", "" + puerto,
                "entorno:marte.agentes.Entorno;coordinador:marte.agentes.Coordinador"
                    + (separado ? "" : ";rover:marte.agentes.Rover")});
            if (separado) remoto = new ProcessBuilder(System.getProperty("java.home") + "/bin/java",
                "-cp", System.getProperty("java.class.path"), "jade.Boot", "-container",
                "-host", "127.0.0.1", "-local-host", "127.0.0.1", "-port", "" + puerto,
                "rover:marte.agentes.Rover").inheritIO().start();
            for (int i = 0; i < 80 && controles.isEmpty(); i++) {
                SwingUtilities.invokeAndWait(() -> {
                    for (Frame f : Frame.getFrames())
                        if (f.isShowing() && f.getTitle().contains("Olympus Mons")) recorrer(f);
                });
                Thread.sleep(100);
            }
            esperar("Batería: 80/100");
            String[] escenarios = {"NORMAL", "TORMENTA", "BATERIA"};
            String[] finales = {"OBJETIVO CUMPLIDO", "REGRESO PREVENTIVO", "REGRESO PREVENTIVO"};
            int[] muestras = {1, 0, 0};
            List<String> filas = new ArrayList<>();
            filas.add("mision,escenario,final_esperado,final_observado,acierto,muestras,tiempo_total_ms,respuestas,propuestas,latencia_media_us");
            long tiempoTotal = 0;
            StringBuilder medidas = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                final int indice = i;
                SwingUtilities.invokeAndWait(() -> {
                    for (Component c : controles) {
                        if (c instanceof JComboBox) ((JComboBox<?>) c).setSelectedIndex(indice);
                        if (c instanceof JButton && ((JButton) c).getText().equals("Iniciar expedición"))
                            ((JButton) c).doClick();
                    }
                });
                String id = "M-" + (i + 1);
                esperar(id + " | " + finales[i]);
                esperar("Muestras: " + muestras[i]);
                esperarRegistro(id + " | respuestas ");
                String bitacora = leer(JTextArea.class, "");
                if (!bitacora.contains(id + ": REGRESAR")) throw new AssertionError("Falta regreso " + id);
                if (i == 0 && (!bitacora.contains(id + ": ESCANEAR")
                    || !bitacora.contains(id + ": RODEAR") || !bitacora.contains("la confirmación")))
                    throw new AssertionError("Faltan etapas de reconocimiento " + id);
                String ms = leer(JLabel.class, "Tiempo de respuesta: ").replaceAll("[^0-9]", "");
                tiempoTotal += Long.parseLong(ms);
                String marcador = id + " | respuestas ";
                String registroMedida = bitacora.substring(bitacora.lastIndexOf(marcador) + marcador.length())
                    .split("\n")[0];
                String[] partes = registroMedida.split(", promedio ");
                String[] conteos = partes[0].split("/");
                long promedioUs = Long.parseLong(partes[1].replaceAll("[^0-9]", ""));
                medidas.append(id).append(" ").append(escenarios[i]).append(": ")
                    .append(conteos[0]).append("/").append(conteos[1])
                    .append(" propuestas respondidas; latencia media ").append(promedioUs)
                    .append(" microsegundos; tiempo total ").append(ms).append(" ms\n");
                String fila = id + "," + escenarios[i] + "," + finales[i] + ","
                    + finales[i] + ",1," + muestras[i] + "," + ms + ","
                    + conteos[0] + "," + conteos[1] + "," + promedioUs;
                filas.add(fila);
                System.out.println("ENSAYO " + fila);
            }
            java.nio.file.Files.write(java.nio.file.Paths.get("ensayo-local.csv"), filas);
            String resumen = "Escenarios con final esperado: 3/3 (100%)\n"
                + "Expediciones que cumplieron la meta: 1/3\n"
                + "Tiempo medio hasta el regreso: " + (tiempoTotal / 3) + " ms\n"
                + medidas
                + "Ensayo local: pendiente validar la red entre dos computadoras.\n";
            java.nio.file.Files.writeString(java.nio.file.Paths.get("metricas-local.txt"), resumen);
            System.out.print(resumen);
            Thread.sleep(1800); // permite visualizar el regreso animado antes de la captura
            SwingUtilities.invokeAndWait(() -> {
                try {
                    Window ventana = SwingUtilities.getWindowAncestor(controles.get(0));
                    if (ventana == null) throw new IllegalStateException("Sin consola");
                    java.awt.image.BufferedImage foto = new java.awt.image.BufferedImage(
                        ventana.getWidth() * 2, ventana.getHeight() * 2,
                        java.awt.image.BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = foto.createGraphics();
                    g.scale(2, 2);
                    ventana.printAll(g);
                    g.dispose();
                    javax.imageio.ImageIO.write(foto, "png", new java.io.File("captura-consola-local.png"));
                } catch (Exception e) { throw new IllegalStateException(e); }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            if (remoto != null) remoto.destroyForcibly();
            System.exit(1);
        }
        if (remoto != null) remoto.destroyForcibly();
        System.exit(0);
    }
}

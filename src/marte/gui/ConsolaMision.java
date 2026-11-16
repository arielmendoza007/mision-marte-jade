package marte.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import marte.modelo.Lectura;
import marte.modelo.Resultado;

/** Tablero de misión: los cambios provienen de mensajes recibidos por Entorno. */
public class ConsolaMision extends JFrame {
    private static final Color FONDO = new Color(17, 24, 35);
    private static final Color TARJETA = new Color(31, 43, 57);
    private static final Color BLANCO = new Color(242, 243, 241);
    private static final Color SUAVE = new Color(171, 186, 198);
    private static final Color NARANJA = new Color(255, 176, 86);

    private final JComboBox<String> sector = new JComboBox<>(new String[] {
        "Ascenso normal", "Tormenta persistente", "Batería limitada" });
    private final JLabel sensores = texto("Sin lectura todavía", 13, BLANCO);
    private final JLabel estado = texto("Esperando misión", 13, BLANCO);
    private final JLabel bateria = texto("Batería: esperando telemetría", 14, BLANCO);
    private final JLabel muestras = texto("Muestras: 0", 14, BLANCO);
    private final JLabel regla = texto("Regla: -", 12, SUAVE);
    private final JLabel tiempo = texto("Tiempo de respuesta: -", 12, SUAVE);
    private final JLabel etapa = texto("Etapa: esperando lectura", 13, NARANJA);
    private final JProgressBar energia = new JProgressBar(0, 100);
    private final JTextArea registro = new JTextArea(7, 60);
    private final JButton iniciar = boton("Iniciar expedición", NARANJA, FONDO);
    private final MapaMarte mapa = new MapaMarte();

    public ConsolaMision(Consumer<String> alIniciar, Runnable alConsultar) {
        super("Olympus Mons | Centro de control");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel raiz = new JPanel(new BorderLayout(12, 12));
        raiz.setBackground(FONDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        setContentPane(raiz);

        JPanel cabecera = new JPanel(new BorderLayout(14, 10));
        cabecera.setOpaque(false);
        JPanel titulo = new JPanel(new GridLayout(0, 1));
        titulo.setOpaque(false);
        titulo.add(texto("OLYMPUS MONS", 23, BLANCO));
        titulo.add(texto("Expedición autónoma al borde de la caldera  /  JADE", 12, SUAVE));
        cabecera.add(titulo, BorderLayout.WEST);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 8));
        controles.setOpaque(false);
        sector.setPreferredSize(new Dimension(185, 34));
        sector.setFont(new Font("SansSerif", Font.PLAIN, 13));
        controles.add(sector);
        JButton consultar = boton("Consultar rover", TARJETA, BLANCO);
        controles.add(consultar);
        controles.add(iniciar);
        cabecera.add(controles, BorderLayout.EAST);
        raiz.add(cabecera, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(12, 0));
        centro.setOpaque(false);
        mapa.setBorder(BorderFactory.createLineBorder(new Color(87, 64, 64), 1));
        centro.add(mapa, BorderLayout.CENTER);

        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(TARJETA);
        lateral.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(58, 75, 92)),
            BorderFactory.createEmptyBorder(18, 18, 16, 18)));
        lateral.setPreferredSize(new Dimension(325, 0));
        lateral.add(texto("MISIÓN", 11, NARANJA));
        lateral.add(Box.createVerticalStrut(8));
        lateral.add(estado);
        lateral.add(Box.createVerticalStrut(12));
        lateral.add(etapa);
        lateral.add(Box.createVerticalStrut(24));
        lateral.add(texto("LECTURA", 11, NARANJA));
        lateral.add(Box.createVerticalStrut(8));
        lateral.add(sensores);
        lateral.add(Box.createVerticalStrut(24));
        lateral.add(texto("RECURSOS", 11, NARANJA));
        lateral.add(Box.createVerticalStrut(9));
        lateral.add(bateria);
        energia.setValue(0);
        energia.setStringPainted(false);
        energia.setForeground(new Color(86, 217, 176));
        energia.setBackground(new Color(57, 67, 78));
        energia.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        lateral.add(Box.createVerticalStrut(7));
        lateral.add(energia);
        lateral.add(Box.createVerticalStrut(9));
        lateral.add(muestras);
        lateral.add(Box.createVerticalStrut(5));
        lateral.add(texto("Coste previsto de regreso: 20 unidades", 11, SUAVE));
        lateral.add(Box.createVerticalGlue());
        lateral.add(regla);
        lateral.add(Box.createVerticalStrut(8));
        lateral.add(tiempo);
        centro.add(lateral, BorderLayout.EAST);
        raiz.add(centro, BorderLayout.CENTER);

        JPanel bitacora = new JPanel(new BorderLayout(0, 7));
        bitacora.setBackground(TARJETA);
        bitacora.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(58, 75, 92)),
            BorderFactory.createEmptyBorder(9, 12, 10, 12)));
        bitacora.add(texto("BITÁCORA ACL  ·  mensajes entre agentes", 11, NARANJA), BorderLayout.NORTH);
        registro.setEditable(false);
        registro.setLineWrap(true);
        registro.setWrapStyleWord(true);
        registro.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        registro.setBackground(new Color(23, 31, 42));
        registro.setForeground(new Color(220, 231, 232));
        registro.setCaretColor(BLANCO);
        JScrollPane desplazamiento = new JScrollPane(registro);
        desplazamiento.setBorder(BorderFactory.createEmptyBorder());
        bitacora.add(desplazamiento, BorderLayout.CENTER);
        bitacora.setPreferredSize(new Dimension(0, 170));
        raiz.add(bitacora, BorderLayout.SOUTH);

        iniciar.addActionListener(e -> {
            iniciar.setEnabled(false);
            sector.setEnabled(false);
            sensores.setText("Esperando lectura de la falda");
            regla.setText("Regla: pendiente");
            etapa.setText("Etapa: solicitando lectura inicial");
            tiempo.setText("Tiempo de respuesta: -");
            mapa.seleccionarSector((String) sector.getSelectedItem());
            String[] escenarios = {"NORMAL", "TORMENTA", "BATERIA"};
            alIniciar.accept(escenarios[sector.getSelectedIndex()]);
        });
        consultar.addActionListener(e -> alConsultar.run());

        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1120, pantalla.width - 60), Math.min(760, pantalla.height - 70));
        setMinimumSize(new Dimension(900, 610));
        setLocationRelativeTo(null);
    }

    private static JLabel texto(String contenido, int tamano, Color color) {
        JLabel l = new JLabel(contenido);
        l.setFont(new Font("SansSerif", tamano >= 20 ? Font.BOLD : Font.PLAIN, tamano));
        l.setForeground(color);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private static JButton boton(String titulo, Color fondo, Color letra) {
        JButton b = new JButton(titulo);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBackground(fondo);
        b.setForeground(letra);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        b.setFocusPainted(false);
        return b;
    }

    public void sensores(Lectura lectura) {
        enUi(() -> {
            mapa.mostrarLectura(lectura);
            sensores.setText("<html>" + lectura.getSector().getNombre() + " · punto "
                + lectura.getSector().getPunto() + "/3<br>Polvo: " + lectura.getPolvo()
                + "/100 &nbsp; Mineral: " + lectura.getMineral() + "/100<br>Paso: "
                + (lectura.getObstaculo() ? "bloqueado" : "libre") + "</html>");
            etapa.setText(lectura.getConfirmada()
                ? "Etapa: lectura confirmada" : "Etapa: lectura inicial");
        });
    }
    public void estado(String valor) { enUi(() -> estado.setText(valor)); }
    public void regla(String valor) {
        enUi(() -> regla.setText("<html><b>Regla:</b> " + valor + "</html>"));
    }
    public void tiempo(long milis) {
        enUi(() -> tiempo.setText("Tiempo de respuesta: " + milis + " ms"));
    }
    public void telemetria(int bateriaActual, int total) {
        enUi(() -> {
            bateria.setText("Batería: " + bateriaActual + "/100");
            energia.setValue(bateriaActual);
            muestras.setText("Muestras: " + total);
        });
    }
    public void resultado(Resultado resultado) {
        enUi(() -> {
            mapa.mostrarResultado(resultado);
            etapa.setText(resultado.getFinalizado()
                ? (resultado.getObjetivo() ? "Etapa: muestra entregada en base"
                    : "Etapa: regreso preventivo")
                : "Etapa: " + resultado.getAccion().toLowerCase());
        });
    }
    public void terminar() { enUi(() -> { iniciar.setEnabled(true); sector.setEnabled(true); }); }
    public void agregar(String valor) {
        enUi(() -> {
            registro.append(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                + "  " + valor + "\n");
            registro.setCaretPosition(registro.getDocument().getLength());
        });
    }
    private void enUi(Runnable cambio) {
        if (SwingUtilities.isEventDispatchThread()) cambio.run();
        else SwingUtilities.invokeLater(cambio);
    }
}

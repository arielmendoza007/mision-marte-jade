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
import javax.swing.JDialog;
import javax.swing.JSplitPane;
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
    private static final Color FONDO = new Color(240, 240, 240);
    private static final Color PANEL = new Color(250, 250, 250);
    private static final Color TEXTO = new Color(30, 30, 30);
    private static final Color SUAVE = new Color(65, 65, 65);
    private static final Color TITULO = new Color(45, 45, 45);

    private final javax.swing.JMenu ensayos = new javax.swing.JMenu("Ensayos");
    private String escenario = "NORMAL";
    private final JLabel sensores = texto("Sin lectura todavía", 13, TEXTO);
    private final JLabel estado = texto("Esperando misión", 13, TEXTO);
    private final JLabel bateria = texto("Batería: esperando telemetría", 14, TEXTO);
    private final JLabel muestras = texto("Muestras: 0", 14, TEXTO);
    private final JLabel regla = texto("Regla: -", 12, SUAVE);
    private final JLabel tiempo = texto("Duración de misión: -", 12, SUAVE);
    private final JLabel decision = texto("Rover: esperando propuesta", 14, TEXTO);
    private final JLabel etapa = texto("Etapa: esperando lectura", 13, TITULO);
    private final JProgressBar energia = new JProgressBar(0, 100);
    private final JTextArea registro = new JTextArea(7, 60);
    private final JButton iniciar = boton("Iniciar expedición");
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
        titulo.add(texto("Expedición Olympus Mons", 20, TEXTO));
        titulo.add(texto("Traer a base una muestra del borde de la caldera", 12, SUAVE));
        cabecera.add(titulo, BorderLayout.WEST);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 8));
        controles.setOpaque(false);
        javax.swing.JMenuBar barra = new javax.swing.JMenuBar();
        javax.swing.ButtonGroup grupo = new javax.swing.ButtonGroup();
        String[] nombres = {"Ascenso normal", "Tormenta persistente", "Batería limitada"};
        String[] claves = {"NORMAL", "TORMENTA", "BATERIA"};
        for (int i = 0; i < nombres.length; i++) {
            String clave = claves[i];
            javax.swing.JRadioButtonMenuItem opcion = new javax.swing.JRadioButtonMenuItem(nombres[i], i == 0);
            opcion.addActionListener(e -> escenario = clave);
            grupo.add(opcion);
            ensayos.add(opcion);
        }
        barra.add(ensayos);
        setJMenuBar(barra);
        JButton consultar = boton("Consultar rover");
        controles.add(consultar);
        controles.add(iniciar);
        cabecera.add(controles, BorderLayout.EAST);
        raiz.add(cabecera, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(12, 0));
        centro.setOpaque(false);
        mapa.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));
        centro.add(mapa, BorderLayout.CENTER);

        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(PANEL);
        lateral.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        lateral.setPreferredSize(new Dimension(325, 0));
        lateral.add(texto("Estado de la expedición", 14, TITULO));
        lateral.add(Box.createVerticalStrut(8));
        lateral.add(estado);
        lateral.add(Box.createVerticalStrut(6));
        lateral.add(etapa);
        lateral.add(Box.createVerticalStrut(6));
        lateral.add(texto("Lectura del entorno", 14, TITULO));
        lateral.add(Box.createVerticalStrut(8));
        lateral.add(sensores);
        lateral.add(Box.createVerticalStrut(6));
        lateral.add(texto("Recursos del rover", 14, TITULO));
        lateral.add(Box.createVerticalStrut(9));
        lateral.add(bateria);
        energia.setValue(0);
        energia.setStringPainted(false);
        energia.setForeground(new Color(85, 130, 85));
        energia.setBackground(new Color(225, 225, 225));
        energia.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        lateral.add(Box.createVerticalStrut(7));
        lateral.add(energia);
        lateral.add(Box.createVerticalStrut(9));
        lateral.add(muestras);
        lateral.add(Box.createVerticalStrut(5));
        lateral.add(texto("El regreso consume 20 unidades", 11, SUAVE));
        lateral.add(Box.createVerticalStrut(8));
        lateral.add(regla);
        lateral.add(Box.createVerticalStrut(8));
        lateral.add(decision);
        lateral.add(Box.createVerticalStrut(8));
        lateral.add(tiempo);
        JScrollPane datos = new JScrollPane(lateral);
        datos.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        datos.setBorder(null);
        datos.setPreferredSize(new Dimension(350, 0));
        lateral.setPreferredSize(null);
        centro.add(datos, BorderLayout.EAST);


        JPanel bitacora = new JPanel(new BorderLayout(0, 7));
        bitacora.setBackground(PANEL);
        bitacora.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180)),
            BorderFactory.createEmptyBorder(9, 12, 10, 12)));
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        encabezado.add(texto("Bitácora ACL · mensajes del Entorno", 16, TITULO), BorderLayout.WEST);
        JButton ampliar = new JButton("Ampliar bitácora");
        encabezado.add(ampliar, BorderLayout.EAST);
        bitacora.add(encabezado, BorderLayout.NORTH);
        ampliar.addActionListener(e -> {
            JDialog detalle = new JDialog(this, "Bitácora ACL", false);
            JTextArea historial = new JTextArea(registro.getDocument());
            historial.setEditable(false);
            historial.setLineWrap(true);
            historial.setWrapStyleWord(true);
            historial.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
            historial.setBackground(registro.getBackground());
            historial.setForeground(registro.getForeground());
            historial.setCaretColor(registro.getCaretColor());
            JScrollPane vista = new JScrollPane(historial);
            detalle.add(vista);
            detalle.setSize(getWidth() * 9 / 10, getHeight() * 3 / 4);
            detalle.setLocationRelativeTo(this);
            detalle.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            detalle.setVisible(true);
            SwingUtilities.invokeLater(() -> historial.setCaretPosition(historial.getDocument().getLength()));
        });
        registro.setEditable(false);
        registro.setLineWrap(true);
        registro.setWrapStyleWord(true);
        registro.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        registro.setBackground(Color.WHITE);
        registro.setForeground(new Color(30, 30, 30));
        registro.setCaretColor(TEXTO);
        JScrollPane desplazamiento = new JScrollPane(registro);
        desplazamiento.setBorder(BorderFactory.createEmptyBorder());
        bitacora.add(desplazamiento, BorderLayout.CENTER);
        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, centro, bitacora);
        division.setResizeWeight(0.62);
        division.setContinuousLayout(true);
        division.setDividerSize(9);
        bitacora.setMinimumSize(new Dimension(0, 95));
        centro.setMinimumSize(new Dimension(0, 180));
        raiz.add(division, BorderLayout.CENTER);
        SwingUtilities.invokeLater(() -> division.setDividerLocation(0.62));

        iniciar.addActionListener(e -> {
            iniciar.setEnabled(false);
            ensayos.setEnabled(false);
            sensores.setText("Esperando lectura de la falda");
            regla.setText("Regla: pendiente");
            decision.setText("Rover: esperando propuesta");
            etapa.setText("Etapa: solicitando lectura inicial");
            tiempo.setText("Duración de misión: -");
            mapa.seleccionarSector(escenario);
            alIniciar.accept(escenario);
        });
        consultar.addActionListener(e -> alConsultar.run());

        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1100, pantalla.width - 60), Math.min(740, pantalla.height - 70));
        setMinimumSize(new Dimension(Math.min(820, pantalla.width - 30), Math.min(555, pantalla.height - 65)));
        setLocationRelativeTo(null);
    }

    private static JLabel texto(String contenido, int tamano, Color color) {
        JLabel l = new JLabel(contenido);
        l.setFont(new Font("SansSerif", tamano >= 20 ? Font.BOLD : Font.PLAIN, Math.max(14, tamano)));
        l.setForeground(color);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private static JButton boton(String titulo) {
        JButton b = new JButton(titulo);
        b.setFont(new Font("SansSerif", Font.PLAIN, 14));
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
        enUi(() -> regla.setText("<html><body style='width: 225px'><b>Propuesta del coordinador</b><br>" + valor + "</html>"));
    }
    public void tiempo(long milis) {
        enUi(() -> tiempo.setText("Duración de misión: " + milis + " ms"));
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
            decision.setText("<html><body style='width: 225px'><b>Respuesta del rover</b><br>"
                + resultado.getDetalle() + "</html>");
            etapa.setText(resultado.getFinalizado()
                ? (resultado.getObjetivo() ? "Etapa: muestra entregada en base"
                    : "Etapa: en base, sin muestra")
                : "Etapa: " + resultado.getAccion().toLowerCase());
        });
    }
    public void terminar() { enUi(() -> { iniciar.setEnabled(true); ensayos.setEnabled(true); }); }
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

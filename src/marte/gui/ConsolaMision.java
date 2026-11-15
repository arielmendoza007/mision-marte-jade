package marte.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Font;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/** Consola operable. Las actualizaciones las provocan mensajes recibidos. */
public class ConsolaMision extends JFrame {
    private final JComboBox<String> sector = new JComboBox<>(new String[] {
        "A - Planicie", "B - Dep�sito", "C - Cuenca" });
    private final JLabel sensores = new JLabel("Sensores en espera de una exploraci�n");
    private final JLabel estado = new JLabel("Esperando misi�n");
    private final JLabel bateria = new JLabel("Bater�a: esperando telemetr�a");
    private final JLabel muestras = new JLabel("Muestras: 0");
    private final JLabel regla = new JLabel("Regla: -");
    private final JLabel tiempo = new JLabel("Tiempo de respuesta: -");
    private final JTextArea registro = new JTextArea(12, 52);
    private final JButton iniciar = new JButton("Iniciar misi�n");

    public ConsolaMision(Consumer<String> alIniciar, Runnable alConsultar) {
        super("Misi�n Marte | Consola de exploraci�n");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));

        JPanel controles = new JPanel(new GridLayout(0, 2, 8, 8));
        controles.setBorder(BorderFactory.createTitledBorder("Destino de exploraci�n"));
        controles.add(new JLabel("Sector:")); controles.add(sector);
        JButton consultar = new JButton("Consultar rover");
        controles.add(consultar);
        controles.add(iniciar);
        controles.add(new JLabel("Reserva de regreso: 20 unidades"));
        add(controles, BorderLayout.NORTH);

        JPanel indicadores = new JPanel(new GridLayout(0, 1, 5, 5));
        indicadores.setBorder(BorderFactory.createTitledBorder("Resultado de la misi�n"));
        estado.setFont(estado.getFont().deriveFont(Font.BOLD, 16f));
        indicadores.add(estado);
        indicadores.add(sensores);
        indicadores.add(bateria);
        indicadores.add(muestras);
        indicadores.add(regla);
        indicadores.add(tiempo);
        add(indicadores, BorderLayout.CENTER);

        registro.setEditable(false);
        registro.setLineWrap(true);
        registro.setWrapStyleWord(true);
        add(new JScrollPane(registro), BorderLayout.SOUTH);

        iniciar.addActionListener(e -> {
            iniciar.setEnabled(false);
            sector.setEnabled(false);
            sensores.setText("Esperando lectura del sector.");
            regla.setText("Regla: pendiente");
            tiempo.setText("Tiempo de respuesta: -");
            alIniciar.accept((String) sector.getSelectedItem());
        });
        consultar.addActionListener(e -> alConsultar.run());
        pack();
        setLocationRelativeTo(null);
    }

    public void sensores(marte.modelo.Lectura lectura) {
        enUi(() -> sensores.setText(lectura.getSector().getNombre() + " � punto "
            + lectura.getSector().getPunto() + "/3   |   Polvo: " + lectura.getPolvo()
            + "/100   |   Mineral: " + lectura.getMineral() + "/100   |   Paso: "
            + (lectura.getObstaculo() ? "bloqueado" : "libre")));
    }
    public void estado(String texto) { enUi(() -> estado.setText(texto)); }
    public void regla(String texto) { enUi(() -> regla.setText("Regla: " + texto)); }
    public void tiempo(long milis) {
        enUi(() -> tiempo.setText("Tiempo de respuesta: " + milis + " ms"));
    }
    public void telemetria(int energia, int total) {
        enUi(() -> {
            bateria.setText("Bater�a: " + energia + "/100");
            muestras.setText("Muestras: " + total);
        });
    }
    public void terminar() { enUi(() -> { iniciar.setEnabled(true); sector.setEnabled(true); }); }
    public void agregar(String texto) {
        enUi(() -> registro.append(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            + "  " + texto + "\n"));
    }
    private void enUi(Runnable cambio) { SwingUtilities.invokeLater(cambio); }
}

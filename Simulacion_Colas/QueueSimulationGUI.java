import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class QueueSimulationGUI extends JFrame {

    private JTextField txtCustomers;
    private JTable table;
    private DefaultTableModel model;
    private JTextArea metricsArea;
    private JComboBox<Integer> cmbWorkers;

    // ── Nuevos campos: hora de inicio configurable ────────────────────
    private JSpinner spinnerHour;
    private JSpinner spinnerMinute;

    public QueueSimulationGUI() {

        setTitle("Simulación Sistema de Colas — Modelo Coss Bú (Ejemplo 5.6)");
        setSize(1200, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ── Panel superior principal ──────────────────────────────────
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));

        // ── Fila 1: controles ─────────────────────────────────────────
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));

        // Tamaño del equipo
        controlsPanel.add(new JLabel("Tamaño del equipo (trabajadores):"));
        cmbWorkers = new JComboBox<>(new Integer[]{1, 3, 4, 5, 6});
        cmbWorkers.setPreferredSize(new Dimension(80, 28));
        controlsPanel.add(cmbWorkers);

        controlsPanel.add(Box.createHorizontalStrut(8));

        // Número de camiones
        controlsPanel.add(new JLabel("Número de camiones a simular:"));
        txtCustomers = new JTextField("12");
        txtCustomers.setPreferredSize(new Dimension(80, 28));
        controlsPanel.add(txtCustomers);

        controlsPanel.add(Box.createHorizontalStrut(8));

        // ── Hora de inicio: spinner de horas y minutos ────────────────
        // Por defecto: 23:00 (11 PM) — igual que el modelo del PDF.
        controlsPanel.add(new JLabel("Hora de inicio del turno:"));

        spinnerHour = new JSpinner(new SpinnerNumberModel(23, 0, 23, 1));
        spinnerHour.setPreferredSize(new Dimension(60, 28));
        // Mostrar siempre 2 dígitos en el spinner
        JSpinner.NumberEditor hourEditor = new JSpinner.NumberEditor(spinnerHour, "00");
        spinnerHour.setEditor(hourEditor);
        controlsPanel.add(spinnerHour);

        controlsPanel.add(new JLabel(":"));

        spinnerMinute = new JSpinner(new SpinnerNumberModel(0, 0, 59, 5));
        spinnerMinute.setPreferredSize(new Dimension(60, 28));
        JSpinner.NumberEditor minuteEditor = new JSpinner.NumberEditor(spinnerMinute, "00");
        spinnerMinute.setEditor(minuteEditor);
        controlsPanel.add(spinnerMinute);

        topPanel.add(controlsPanel, BorderLayout.CENTER);

        // ── Fila 2: botones ───────────────────────────────────────────
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        JButton btnSimulate = new JButton("▶ Simular");
        btnSimulate.setPreferredSize(new Dimension(140, 30));
        buttonsPanel.add(btnSimulate);

        JButton btnHelp = new JButton("¿Qué significa cada columna?");
        btnHelp.setPreferredSize(new Dimension(240, 30));
        buttonsPanel.add(btnHelp);

        topPanel.add(buttonsPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // ── Tabla de resultados ───────────────────────────────────────
        model = new DefaultTableModel();
        model.addColumn("Camión");
        model.addColumn("R Llegada");
        model.addColumn("T Entre Llegadas (min)");
        model.addColumn("Hora Llegada");
        model.addColumn("R Servicio");
        model.addColumn("T Servicio (min)");
        model.addColumn("Inicio Servicio");
        model.addColumn("Fin Servicio");
        model.addColumn("Espera (min)");
        model.addColumn("Tiempo Sistema (min)");

        table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ── Área de métricas ──────────────────────────────────────────
        metricsArea = new JTextArea(10, 20);
        metricsArea.setEditable(false);
        metricsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        metricsArea.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JScrollPane metricsScroll = new JScrollPane(metricsArea);
        metricsScroll.setPreferredSize(new Dimension(0, 180));
        add(metricsScroll, BorderLayout.SOUTH);

        // ── Acciones ──────────────────────────────────────────────────
        btnSimulate.addActionListener(e -> simulate());
        btnHelp.addActionListener(e -> showColumnInfo());
    }

    private void simulate() {

        model.setRowCount(0);
        metricsArea.setText("");

        try {
            int customers = Integer.parseInt(txtCustomers.getText().trim());
            int workers   = (int) cmbWorkers.getSelectedItem();

            // ── Leer hora de inicio desde los spinners ────────────────
            int startHour   = (int) spinnerHour.getValue();
            int startMinute = (int) spinnerMinute.getValue();

            if (customers <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Ingresa un número de camiones mayor a 0.",
                        "Valor inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ── Pasar hora de inicio al simulador ─────────────────────
            QueueSimulator simulator =
                    new QueueSimulator(customers, workers, startHour, startMinute);

            SimulationMetrics metrics = simulator.runSimulation(model);

            metricsArea.setText(metrics.toString());

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Por favor ingresa un número entero válido.",
                    "Error de entrada", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showColumnInfo() {

        String message =
                "SIGNIFICADO DE CADA COLUMNA\n" +
                "────────────────────────────────────────────\n\n" +

                "Camión:\n" +
                "  Número del camión en orden de llegada.\n\n" +

                "R Llegada:\n" +
                "  Número aleatorio (0–1) usado para determinar\n" +
                "  el tiempo entre llegadas (Tabla 5.14 del libro).\n\n" +

                "T Entre Llegadas:\n" +
                "  Minutos entre este camión y el anterior.\n\n" +

                "Hora Llegada:\n" +
                "  Hora exacta en que el camión llega al almacén.\n" +
                "  Calculada desde la hora de inicio configurada.\n\n" +

                "R Servicio:\n" +
                "  Número aleatorio usado para determinar\n" +
                "  el tiempo de descarga (Tablas 5.15–5.18).\n\n" +

                "T Servicio:\n" +
                "  Minutos que tarda el equipo en descargar el camión.\n\n" +

                "Inicio Servicio:\n" +
                "  Hora en que comienza la descarga.\n\n" +

                "Fin Servicio:\n" +
                "  Hora en que termina la descarga.\n\n" +

                "Espera:\n" +
                "  Minutos que el camión espera en cola antes\n" +
                "  de ser atendido.\n\n" +

                "Tiempo Sistema:\n" +
                "  Tiempo total del camión en el sistema\n" +
                "  (espera + descarga).";

        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(textArea),
                "Información de Columnas",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
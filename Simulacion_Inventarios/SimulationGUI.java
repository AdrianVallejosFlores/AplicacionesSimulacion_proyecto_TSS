import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class SimulationGUI extends JFrame {

    private JTextField txtMonths, txtInitialInventory, txtQ, txtR;

    private JTable table;
    private DefaultTableModel model;

    private JTable costTable;
    private DefaultTableModel costModel;

    public SimulationGUI() {

        setTitle("Simulación Sistema de Inventarios (Modelo Q,R - Coss Bu)");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));

        panel.add(new JLabel("Número de Meses:"));
        txtMonths = new JTextField();
        panel.add(txtMonths);

        panel.add(new JLabel("Inventario Inicial:"));
        txtInitialInventory = new JTextField();
        panel.add(txtInitialInventory);

        panel.add(new JLabel("Cantidad fija Q:"));
        txtQ = new JTextField();
        panel.add(txtQ);

        panel.add(new JLabel("Punto de Reorden R:"));
        txtR = new JTextField();
        panel.add(txtR);

        JButton btnSimulate = new JButton("Simular");
        panel.add(btnSimulate);

        add(panel, BorderLayout.NORTH);

        // TABLA PRINCIPAL
        model = new DefaultTableModel();
        model.addColumn("Mes");
        model.addColumn("Inventario Inicial");
        model.addColumn("R Demanda");
        model.addColumn("Demanda");
        model.addColumn("Inventario Final");
        model.addColumn("Faltante");
        model.addColumn("Órdenes");
        model.addColumn("Inventario Promedio");

        table = new JTable(model);

        JScrollPane simulationScroll = new JScrollPane(table);
        simulationScroll.setBorder(
                BorderFactory.createTitledBorder("Tabla de Simulación"));

        // TABLA DE COSTOS
        costModel = new DefaultTableModel();
        costModel.addColumn("Costo de ordenar");
        costModel.addColumn("Costo de conservar inventario");
        costModel.addColumn("Costo de faltante");
        costModel.addColumn("Costo total");

        costTable = new JTable(costModel);

        JScrollPane costScroll = new JScrollPane(costTable);
        costScroll.setBorder(
                BorderFactory.createTitledBorder("Tabla de Costos (Coss Bu)"));

        costTable.setRowHeight(25);

        // CONTENEDOR VERTICAL
        costScroll.setPreferredSize(new Dimension(0, 120)); // altura fija para tabla de costos

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(simulationScroll, BorderLayout.CENTER);
        centerPanel.add(costScroll, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        btnSimulate.addActionListener(e -> simulate());
    }

    private void simulate() {

        model.setRowCount(0);
        costModel.setRowCount(0);

        int months = Integer.parseInt(txtMonths.getText());
        int initialInventory = Integer.parseInt(txtInitialInventory.getText());
        int Q = Integer.parseInt(txtQ.getText());
        int R = Integer.parseInt(txtR.getText());

        InventorySimulator simulator =
                new InventorySimulator(months, initialInventory, Q, R);

        double[] results = simulator.runSimulation(model);

        int orders = (int) results[0];
        double avgInventory = results[1];
        int shortage = (int) results[2];

        double orderingCost = results[3];
        double holdingCost = results[4];
        double shortageCost = results[5];
        double totalCost = results[6];

        // FORMATO ESTILO LIBRO
        String ordenar =
        orders + "(100) = $" + String.format("%.0f", orderingCost);

        String inventario =
                String.format("%.0f", avgInventory) +
                "(1.67) = $" + String.format("%.0f", holdingCost);

        String faltante =
                shortage + "(50) = $" +
                String.format("%.0f", shortageCost);

        String total =
                "$" + String.format("%.0f", totalCost);

        costModel.addRow(new Object[]{
                ordenar,
                inventario,
                faltante,
                total
        });
    }
}
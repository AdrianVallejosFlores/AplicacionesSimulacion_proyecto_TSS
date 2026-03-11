import javax.swing.table.DefaultTableModel;
import java.util.*;

public class InventorySimulator {

    private int months;
    private int Q;
    private int R;
    private int inventory;
    private int orderCount = 0;

    private List<Order> pendingOrders = new ArrayList<>();
    private Random random = new Random();

    private double totalAverageInventory = 0;
    private int totalShortage = 0;

    public InventorySimulator(int months, int initialInventory, int Q, int R) {
        this.months = months;
        this.inventory = initialInventory;
        this.Q = Q;
        this.R = R;
    }

    public double[] runSimulation(DefaultTableModel model) {

        for (int month = 1; month <= months; month++) {

            Iterator<Order> iterator = pendingOrders.iterator();
            while (iterator.hasNext()) {
                Order order = iterator.next();
                order.monthsRemaining--;

                if (order.monthsRemaining == 0) {
                    inventory += order.quantity;
                    iterator.remove();
                }
            }

            int initialInventory = inventory;

            double rDemand = random.nextDouble();
            int demand = DemandDistribution.getDemand(rDemand);

            int finalInventory = initialInventory - demand;
            int shortage = 0;

            if (finalInventory < 0) {
                shortage = Math.abs(finalInventory);
                finalInventory = 0;
            }

            if (finalInventory <= R) {

                double rLead = random.nextDouble();
                int leadTime = LeadTimeDistribution.getLeadTime(rLead);

                pendingOrders.add(new Order(Q, leadTime));
                orderCount++;
            }

            double averageInventory;

            if (shortage == 0) {
                averageInventory = (initialInventory + finalInventory) / 2.0;
            } else {
                averageInventory = initialInventory / 2.0;
            }

            totalAverageInventory += averageInventory;
            totalShortage += shortage;

            model.addRow(new Object[]{
                    month,
                    initialInventory,
                    String.format("%.5f", rDemand),
                    demand,
                    finalInventory,
                    shortage,
                    orderCount,
                    String.format("%.2f", averageInventory)
            });

            inventory = finalInventory;
        }

        double costOrder = 100;
        double costHolding = 1.67;
        double costShortage = 50;

        double orderingCost = orderCount * costOrder;
        double holdingCost = totalAverageInventory * costHolding;
        double shortageCost = totalShortage * costShortage;

        double totalCost = orderingCost + holdingCost + shortageCost;

        return new double[]{
                orderCount,
                totalAverageInventory,
                totalShortage,
                orderingCost,
                holdingCost,
                shortageCost,
                totalCost
        };
    }
}
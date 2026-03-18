import javax.swing.table.DefaultTableModel;
import java.util.*;

public class QueueSimulator {

    private static final int SHIFT_DURATION_MIN = 510;

    private final int breakStartMin;
    private final int shiftEndMin;

    private int totalCustomers;
    private int workers;
    private int startHour;
    private int startMinute;
    private Random random = new Random();
    private int scenario;

    public QueueSimulator(int totalCustomers, int workers, int startHour, int startMinute) {

        this.totalCustomers = totalCustomers;
        this.workers        = workers;
        this.startHour      = startHour;
        this.startMinute    = startMinute;
        this.scenario       = (workers == 1) ? 1 : 2;

        int breakAbsoluteMin = 3 * 60;
        int startAbsoluteMin = startHour * 60 + startMinute;

        int rawBreak = breakAbsoluteMin - startAbsoluteMin;
        this.breakStartMin = (rawBreak <= 0) ? rawBreak + 24 * 60 : rawBreak;

        this.shiftEndMin = SHIFT_DURATION_MIN;
    }

    public SimulationMetrics runSimulation(DefaultTableModel model) {

        TimeUtils.initializeStartTime(startHour, startMinute);

        List<Customer> customers = new ArrayList<>();

        int initialTrucks  = getInitialTrucks();
        int lastArrival    = 0;
        int lastServiceEnd = 0;
        boolean breakTaken = false;

        double totalWaiting     = 0;
        double totalSystem      = 0;
        double totalServiceTime = 0;

        // ── Camiones que ya estaban esperando al abrir ────────────────
        for (int i = 1; i <= initialTrucks; i++) {

            Customer c = new Customer(i);

            c.rArrival     = 0;
            c.interArrival = 0;
            c.arrivalTime  = 0;

            c.rService    = random.nextDouble();
            c.serviceTime = ServiceDistribution.getServiceTime(c.rService, workers);

            c.serviceStart = lastServiceEnd;

            if (!breakTaken && c.serviceStart >= breakStartMin) {
                c.serviceStart += 30;
                breakTaken = true;
            }

            c.serviceEnd  = c.serviceStart + c.serviceTime;
            c.waitingTime = c.serviceStart - c.arrivalTime;
            c.systemTime  = c.serviceEnd   - c.arrivalTime;

            // ── Ocio del personal ─────────────────────────────────────
            // Para camiones iniciales siempre es 0: ya estaban esperando
            c.idleTime = 0;

            // ── Longitud de la cola ───────────────────────────────────
            // Los camiones iniciales llegan todos al mismo tiempo (t=0),
            // así que la cola va bajando: el primero ve (initialTrucks-1),
            // el segundo ve (initialTrucks-2), etc.
            c.queueLength = initialTrucks - i;

            lastServiceEnd = c.serviceEnd;

            totalWaiting     += c.waitingTime;
            totalSystem      += c.systemTime;
            totalServiceTime += c.serviceTime;

            customers.add(c);
            model.addRow(buildRow(c));
        }

        // ── Camiones que llegan durante el turno ──────────────────────
        int customerIndex = initialTrucks + 1;

        for (int i = 1; i <= totalCustomers; i++) {

            Customer c = new Customer(customerIndex++);

            c.rArrival     = random.nextDouble();
            c.interArrival = ArrivalDistribution.getInterArrivalTime(c.rArrival, scenario);

            if (i == 1) {
                c.arrivalTime = c.interArrival;
            } else {
                c.arrivalTime = lastArrival + c.interArrival;
            }

            if (c.arrivalTime > shiftEndMin) break;

            lastArrival = c.arrivalTime;

            c.rService    = random.nextDouble();
            c.serviceTime = ServiceDistribution.getServiceTime(c.rService, workers);

            c.serviceStart = Math.max(c.arrivalTime, lastServiceEnd);

            if (!breakTaken && c.serviceStart >= breakStartMin) {
                c.serviceStart += 30;
                breakTaken = true;
            }

            c.serviceEnd  = c.serviceStart + c.serviceTime;
            c.waitingTime = c.serviceStart - c.arrivalTime;
            c.systemTime  = c.serviceEnd   - c.arrivalTime;

            // ── Ocio del personal ─────────────────────────────────────
            // Tiempo que el servidor estuvo libre antes de que llegara
            // este camión. Solo > 0 si el camión llegó después de que
            // el servidor terminó el anterior trabajo.
            c.idleTime = Math.max(0, c.arrivalTime - lastServiceEnd);

            // ── Longitud de la cola ───────────────────────────────────
            // Contamos cuántos camiones anteriores aún no habían terminado
            // su servicio cuando este camión llegó.
            if (c.waitingTime > 0) {
                int qLen = 0;
                for (Customer prev : customers) {
                    if (prev.serviceEnd > c.arrivalTime) {
                        qLen++;
                    }
                }
                // Restamos 1: el que está en servicio no cuenta como cola
                c.queueLength = Math.max(0, qLen - 1);
            } else {
                c.queueLength = 0;
            }

            lastServiceEnd = c.serviceEnd;

            totalWaiting     += c.waitingTime;
            totalSystem      += c.systemTime;
            totalServiceTime += c.serviceTime;

            customers.add(c);
            model.addRow(buildRow(c));
        }

        if (customers.isEmpty()) {
            return new SimulationMetrics(0, 0, 0, 0);
        }

        int totalProcessed = customers.size();

        double avgWaiting     = totalWaiting / totalProcessed;
        double avgSystem      = totalSystem  / totalProcessed;
        int    lastEnd        = customers.get(totalProcessed - 1).serviceEnd;
        double totalTime      = Math.max(lastEnd, shiftEndMin);
        double utilization    = totalServiceTime / totalTime;
        double avgQueueLength = totalWaiting     / totalTime;

        int extraMinutes = Math.max(0, lastEnd - shiftEndMin);

        if (scenario == 2) {
            return new SimulationMetrics(
                    avgWaiting,
                    avgSystem,
                    utilization,
                    avgQueueLength,
                    workers,
                    extraMinutes,
                    totalWaiting,
                    totalTime
            );
        } else {
            return new SimulationMetrics(
                    avgWaiting,
                    avgSystem,
                    utilization,
                    avgQueueLength
            );
        }
    }

    // ── Camiones esperando al abrir el almacén (Tabla 5.13) ──────────
    private int getInitialTrucks() {

        if (scenario == 1) return 0;

        double r = random.nextDouble();

        if      (r < 0.50) return 0;
        else if (r < 0.75) return 1;
        else if (r < 0.90) return 2;
        else               return 3;
    }

    // ── Fila para la tabla de la GUI ──────────────────────────────────
    private Object[] buildRow(Customer c) {

        return new Object[]{
                c.id,
                String.format("%.5f", c.rArrival),
                c.interArrival,
                TimeUtils.formatTime(c.arrivalTime),
                String.format("%.5f", c.rService),
                c.serviceTime,
                TimeUtils.formatTime(c.serviceStart),
                TimeUtils.formatTime(c.serviceEnd),
                c.waitingTime,
                c.systemTime,
                c.idleTime,      // Ocio del personal (min)
                c.queueLength    // Longitud de la cola
        };
    }
}
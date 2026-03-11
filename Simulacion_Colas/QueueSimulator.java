import javax.swing.table.DefaultTableModel;
import java.util.*;

public class QueueSimulator {

    // ── Constantes del modelo (Ejemplo 5.6 del PDF) ─────────────────
    private static final int SHIFT_DURATION_MIN  = 510; // 11 PM → 7:30 AM = 510 min
    private static final int BREAK_START_MIN     = 240; // 3:00 AM = 11 PM + 240 min
    private static final int BREAK_DURATION_MIN  =  30; // descanso de 30 min

    private int totalCustomers;
    private int workers;
    private Random random = new Random();
    private int scenario;

    public QueueSimulator(int totalCustomers, int workers) {

        this.totalCustomers = totalCustomers;
        this.workers        = workers;
        this.scenario       = (workers == 1) ? 1 : 2;
    }

    public SimulationMetrics runSimulation(DefaultTableModel model) {

        TimeUtils.initializeStartTime();

        List<Customer> customers = new ArrayList<>();

        // ── Estado inicial: camiones esperando al abrir (Tabla 5.13) ─
        int initialTrucks = getInitialTrucks();

        int lastArrival    = 0;
        int lastServiceEnd = 0;

        double totalWaiting     = 0;
        double totalSystem      = 0;
        double totalServiceTime = 0;

        // ── Procesar camiones que ya estaban esperando al abrir ───────
        for (int i = 1; i <= initialTrucks; i++) {

            Customer c = new Customer(i);

            // Camiones iniciales: ya están ahí a las 11 PM (tiempo 0)
            c.rArrival    = 0;
            c.interArrival = 0;
            c.arrivalTime  = 0;

            c.rService   = random.nextDouble();
            c.serviceTime = ServiceDistribution.getServiceTime(c.rService, workers);

            c.serviceStart = lastServiceEnd;
            c.serviceEnd   = c.serviceStart + c.serviceTime;
            c.waitingTime  = c.serviceStart - c.arrivalTime;
            c.systemTime   = c.serviceEnd   - c.arrivalTime;

            lastServiceEnd = c.serviceEnd;

            totalWaiting     += c.waitingTime;
            totalSystem      += c.systemTime;
            totalServiceTime += c.serviceTime;

            customers.add(c);

            model.addRow(buildRow(c));
        }

        // ── Procesar camiones que llegan durante el turno ─────────────
        int customerIndex = initialTrucks + 1;

        for (int i = 1; i <= totalCustomers; i++) {

            Customer c = new Customer(customerIndex++);

            // ── Tiempo entre llegadas ──────────────────────────────────
            c.rArrival     = random.nextDouble();
            c.interArrival = ArrivalDistribution.getInterArrivalTime(c.rArrival, scenario);

            if (i == 1) {
                c.arrivalTime = c.interArrival;
            } else {
                c.arrivalTime = lastArrival + c.interArrival;
            }

            // Si el camión llega después del fin del turno, lo ignoramos
            if (c.arrivalTime > SHIFT_DURATION_MIN) break;

            lastArrival = c.arrivalTime;

            // ── Tiempo de servicio ────────────────────────────────────
            c.rService    = random.nextDouble();
            c.serviceTime = ServiceDistribution.getServiceTime(c.rService, workers);

            // ── Inicio de servicio: espera al servidor o al camión ────
            c.serviceStart = Math.max(c.arrivalTime, lastServiceEnd);

            // ── Ajuste por descanso del personal ─────────────────────
            // Si el servicio anterior terminó después de las 3 AM,
            // el descanso se toma al finalizar ese servicio.
            if (c.serviceStart >= BREAK_START_MIN &&
                lastServiceEnd  >= BREAK_START_MIN &&
                c.serviceStart  == lastServiceEnd) {

                // Solo se aplica descanso si no se ha tomado aún
                // (simplificación: se asume una vez por turno)
            }

            // ── Fin de servicio ───────────────────────────────────────
            c.serviceEnd  = c.serviceStart + c.serviceTime;

            // ── Tiempos de espera y sistema ───────────────────────────
            c.waitingTime = c.serviceStart - c.arrivalTime;
            c.systemTime  = c.serviceEnd   - c.arrivalTime;

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

        // ── Métricas de cola ──────────────────────────────────────────
        double avgWaiting      = totalWaiting / totalProcessed;
        double avgSystem       = totalSystem  / totalProcessed;
        int    lastEnd         = customers.get(totalProcessed - 1).serviceEnd;
        double totalTime       = Math.max(lastEnd, SHIFT_DURATION_MIN);
        double utilization     = totalServiceTime / totalTime;
        double avgQueueLength  = totalWaiting     / totalTime;

        // ── Cálculo de tiempo extra ───────────────────────────────────
        int extraMinutes = Math.max(0, lastEnd - SHIFT_DURATION_MIN);

        // ── Retornar métricas con costos ──────────────────────────────
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

    // ── Genera los camiones esperando al inicio (Tabla 5.13) ──────────
    private int getInitialTrucks() {

        if (scenario == 1) return 0; // escenario simple no usa esto

        double r = random.nextDouble();

        // Tabla 5.13 — Transformada inversa
        if      (r < 0.50) return 0;
        else if (r < 0.75) return 1;
        else if (r < 0.90) return 2;
        else               return 3;
    }

    // ── Construye la fila para la tabla de la GUI ────────────────────
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
                c.systemTime
        };
    }
}
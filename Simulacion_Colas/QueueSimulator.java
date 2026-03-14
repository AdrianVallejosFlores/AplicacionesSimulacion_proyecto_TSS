import javax.swing.table.DefaultTableModel;
import java.util.*;

public class QueueSimulator {

    // ── Duración fija del turno: siempre 8.5 horas = 510 minutos ────
    // Esto no cambia: el turno dura lo mismo sin importar cuándo empieza.
    private static final int SHIFT_DURATION_MIN = 510;

    // ── Criterios calculados dinámicamente según hora de inicio ──────
    // Se calculan en el constructor una vez que se conoce startHour/startMinute.
    private final int breakStartMin; // minutos desde inicio → hora del descanso (3 AM)
    private final int shiftEndMin;   // minutos desde inicio → fin del turno (7:30 AM)

    private int totalCustomers;
    private int workers;
    private int startHour;
    private int startMinute;
    private Random random = new Random();
    private int scenario;

    /**
     * Constructor principal.
     *
     * @param totalCustomers  cantidad de camiones a simular
     * @param workers         tamaño del equipo (3, 4, 5 o 6)
     * @param startHour       hora de inicio del turno (0–23)
     * @param startMinute     minuto de inicio del turno (0–59)
     */
    public QueueSimulator(int totalCustomers, int workers, int startHour, int startMinute) {

        this.totalCustomers = totalCustomers;
        this.workers        = workers;
        this.startHour      = startHour;
        this.startMinute    = startMinute;
        this.scenario       = (workers == 1) ? 1 : 2;

        // ── Calcular en qué minuto relativo ocurre el descanso ────────
        // El descanso es a las 3:00 AM = 180 minutos desde medianoche.
        // Si el turno empieza a las 23:00 (1380 min), el descanso ocurre
        // a los 1380+240=1620 min desde medianoche, es decir, a los 240
        // minutos relativos desde el inicio.
        // Fórmula: (3:00 AM en minutos absolutos) - (inicio en minutos absolutos)
        int breakAbsoluteMin = 3 * 60; // 3:00 AM = 180 min desde medianoche
        int startAbsoluteMin = startHour * 60 + startMinute;

        // Si el inicio es después de las 3 AM, el descanso es al día siguiente.
        int rawBreak = breakAbsoluteMin - startAbsoluteMin;
        this.breakStartMin = (rawBreak <= 0) ? rawBreak + 24 * 60 : rawBreak;

        // ── Calcular en qué minuto relativo termina el turno ──────────
        // Siempre son 510 minutos (8.5 horas) desde el inicio.
        this.shiftEndMin = SHIFT_DURATION_MIN;
    }

    public SimulationMetrics runSimulation(DefaultTableModel model) {

        // Configurar TimeUtils con la hora de inicio elegida por el usuario
        TimeUtils.initializeStartTime(startHour, startMinute);

        List<Customer> customers = new ArrayList<>();

        // ── Estado inicial: camiones esperando al abrir (Tabla 5.13) ─
        int initialTrucks = getInitialTrucks();

        int lastArrival    = 0;
        int lastServiceEnd = 0;
        boolean breakTaken = false; // control para que el descanso ocurra una sola vez

        double totalWaiting     = 0;
        double totalSystem      = 0;
        double totalServiceTime = 0;

        // ── Procesar camiones que ya estaban esperando al abrir ───────
        for (int i = 1; i <= initialTrucks; i++) {

            Customer c = new Customer(i);

            c.rArrival     = 0;
            c.interArrival = 0;
            c.arrivalTime  = 0;

            c.rService    = random.nextDouble();
            c.serviceTime = ServiceDistribution.getServiceTime(c.rService, workers);

            c.serviceStart = lastServiceEnd;

            // ── Verificar descanso antes de iniciar servicio ──────────
            if (!breakTaken && c.serviceStart >= breakStartMin) {
                c.serviceStart += 30; // 30 min de descanso
                breakTaken = true;
            }

            c.serviceEnd  = c.serviceStart + c.serviceTime;
            c.waitingTime = c.serviceStart - c.arrivalTime;
            c.systemTime  = c.serviceEnd   - c.arrivalTime;

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

            c.rArrival     = random.nextDouble();
            c.interArrival = ArrivalDistribution.getInterArrivalTime(c.rArrival, scenario);

            if (i == 1) {
                c.arrivalTime = c.interArrival;
            } else {
                c.arrivalTime = lastArrival + c.interArrival;
            }

            // Camión que llega después del fin del turno: se ignora
            if (c.arrivalTime > shiftEndMin) break;

            lastArrival = c.arrivalTime;

            c.rService    = random.nextDouble();
            c.serviceTime = ServiceDistribution.getServiceTime(c.rService, workers);

            c.serviceStart = Math.max(c.arrivalTime, lastServiceEnd);

            // ── Verificar descanso antes de iniciar servicio ──────────
            // El descanso ocurre una sola vez, tan pronto como el servidor
            // queda libre después de las 3 AM (breakStartMin).
            if (!breakTaken && c.serviceStart >= breakStartMin) {
                c.serviceStart += 30;
                breakTaken = true;
            }

            c.serviceEnd  = c.serviceStart + c.serviceTime;
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
        double avgWaiting     = totalWaiting / totalProcessed;
        double avgSystem      = totalSystem  / totalProcessed;
        int    lastEnd        = customers.get(totalProcessed - 1).serviceEnd;
        double totalTime      = Math.max(lastEnd, shiftEndMin);
        double utilization    = totalServiceTime / totalTime;
        double avgQueueLength = totalWaiting     / totalTime;

        // ── Tiempo extra: minutos más allá del fin del turno ─────────
        int extraMinutes = Math.max(0, lastEnd - shiftEndMin);

        // ── Retornar métricas con costos (solo escenario del PDF) ─────
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

    // ── Fila para la tabla de la GUI ─────────────────────────────────
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
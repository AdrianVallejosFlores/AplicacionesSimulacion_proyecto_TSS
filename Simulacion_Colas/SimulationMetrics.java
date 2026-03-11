public class SimulationMetrics {

    // ─── Métricas de cola ────────────────────────────────────────────
    double avgWaiting;
    double avgSystem;
    double serverUtilization;
    double avgQueueLength;

    // ─── Costos (según PDF — Ejemplo 5.6) ───────────────────────────
    double salarioNormal;
    double salarioExtra;
    double costoEsperaCamion;
    double costoOperacionAlmacen;
    double costoTotal;

    /**
     * Constructor básico — solo métricas de cola.
     * Se usa cuando no se calculan costos (escenario 1 genérico).
     */
    public SimulationMetrics(double avgWaiting,
                             double avgSystem,
                             double serverUtilization,
                             double avgQueueLength) {

        this.avgWaiting        = avgWaiting;
        this.avgSystem         = avgSystem;
        this.serverUtilization = serverUtilization;
        this.avgQueueLength    = avgQueueLength;

        // Sin costos calculados
        this.salarioNormal        = -1;
        this.salarioExtra         = -1;
        this.costoEsperaCamion    = -1;
        this.costoOperacionAlmacen = -1;
        this.costoTotal           = -1;
    }

    /**
     * Constructor completo — métricas de cola + costos del PDF.
     *
     * Fórmulas según Ejemplo 5.6:
     *   Salario normal        = workers * 8h * $25/h
     *   Salario extra         = workers * horasExtra * $37.50/h
     *   Costo espera camión   = totalWaitingHours * $100/h
     *   Costo operación       = totalSystemHours  * $500/h
     *   Costo total           = salarioNormal + salarioExtra
     *                         + costoEsperaCamion + costoOperacionAlmacen
     *
     * @param workers          número de trabajadores en el equipo
     * @param extraMinutes     minutos de tiempo extra trabajados
     * @param totalWaitingMin  suma total de minutos de espera de camiones
     * @param totalDurationMin duración total del turno en minutos
     */
    public SimulationMetrics(double avgWaiting,
                             double avgSystem,
                             double serverUtilization,
                             double avgQueueLength,
                             int workers,
                             int extraMinutes,
                             double totalWaitingMin,
                             double totalDurationMin) {

        this.avgWaiting        = avgWaiting;
        this.avgSystem         = avgSystem;
        this.serverUtilization = serverUtilization;
        this.avgQueueLength    = avgQueueLength;

        // ── Salarios ─────────────────────────────────────────────────
        // Turno normal: 8 horas × $25/h × workers
        this.salarioNormal = workers * 8 * 25.0;

        // Tiempo extra: fracción de hora extra × $37.50/h × workers
        double extraHours = extraMinutes / 60.0;
        this.salarioExtra = workers * extraHours * 37.50;

        // ── Costos de espera y operación ─────────────────────────────
        double waitingHours  = totalWaitingMin / 60.0;
        double durationHours = totalDurationMin / 60.0;

        this.costoEsperaCamion     = waitingHours  * 100.0;
        this.costoOperacionAlmacen = durationHours * 500.0;

        // ── Costo total ───────────────────────────────────────────────
        this.costoTotal = salarioNormal
                        + salarioExtra
                        + costoEsperaCamion
                        + costoOperacionAlmacen;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("RESULTADOS DE LA SIMULACIÓN\n");
        sb.append("--------------------------------------------------\n");
        sb.append(String.format("Tiempo promedio de espera:    %6.2f min%n", avgWaiting));
        sb.append(String.format("Tiempo promedio en sistema:   %6.2f min%n", avgSystem));
        sb.append(String.format("Utilización del servidor:     %6.2f %%%n", serverUtilization * 100));
        sb.append(String.format("Longitud promedio de cola:    %6.2f%n",    avgQueueLength));

        // Solo mostrar costos si fueron calculados
        if (costoTotal >= 0) {
            sb.append("\nDESGLOSE DE COSTOS (turno)\n");
            sb.append("--------------------------------------------------\n");
            sb.append(String.format("Salario normal:               $%8.2f%n", salarioNormal));
            sb.append(String.format("Salario extra:                $%8.2f%n", salarioExtra));
            sb.append(String.format("Costo espera camiones:        $%8.2f%n", costoEsperaCamion));
            sb.append(String.format("Costo operación almacén:      $%8.2f%n", costoOperacionAlmacen));
            sb.append("--------------------------------------------------\n");
            sb.append(String.format("COSTO TOTAL:                  $%8.2f%n", costoTotal));
        }

        return sb.toString();
    }
}
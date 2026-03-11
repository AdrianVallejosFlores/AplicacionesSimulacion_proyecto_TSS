public class ArrivalDistribution {

    /**
     * Retorna el tiempo entre llegadas según el escenario.
     *
     * Escenario 1: distribución genérica (1–5 min) para pruebas simples.
     * Escenario 2: distribución del PDF — Tabla 5.14 (20–60 min).
     */
    public static int getInterArrivalTime(double r, int scenario) {

        if (scenario == 1) {

            // Distribución uniforme discreta genérica para escenario simple
            if (r < 0.125) return 1;
            else if (r < 0.375) return 2;
            else if (r < 0.625) return 3;
            else if (r < 0.875) return 4;
            else return 5;

        } else {

            // Tabla 5.14 del PDF — Tiempo entre llegadas de camiones
            if (r < 0.02) return 20;
            else if (r < 0.10) return 25;
            else if (r < 0.22) return 30;
            else if (r < 0.47) return 35;
            else if (r < 0.67) return 40;
            else if (r < 0.82) return 45;
            else if (r < 0.92) return 50;
            else if (r < 0.97) return 55;
            else return 60;
        }
    }
}
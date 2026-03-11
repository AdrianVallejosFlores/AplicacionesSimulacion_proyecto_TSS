public class ServiceDistribution {

    /**
     * Retorna el tiempo de servicio según el número de trabajadores.
     *
     * workers == 1 : distribución genérica (1–5 min) para escenario simple.
     * workers == 3 : Tabla 5.15 del PDF (20–60 min).
     * workers == 4 : Tabla 5.16 del PDF (15–55 min).  ← CORREGIDA
     * workers == 5 : Tabla 5.17 del PDF (10–50 min).  ← CORREGIDA
     * workers == 6 : Tabla 5.18 del PDF  (5–45 min).  ← CORREGIDA
     */
    public static int getServiceTime(double r, int workers) {

        if (workers == 1) {

            // Distribución genérica para escenario simple
            if (r < 0.10) return 1;
            else if (r < 0.30) return 2;
            else if (r < 0.60) return 3;
            else if (r < 0.85) return 4;
            else return 5;
        }

        switch (workers) {

            case 3:
                // Tabla 5.15 — equipo de 3 personas
                if (r < 0.05) return 20;
                else if (r < 0.15) return 25;
                else if (r < 0.35) return 30;
                else if (r < 0.60) return 35;
                else if (r < 0.72) return 40;
                else if (r < 0.82) return 45;
                else if (r < 0.90) return 50;
                else if (r < 0.96) return 55;
                else return 60;

            case 4:
                // Tabla 5.16 — equipo de 4 personas (CORREGIDA)
                if (r < 0.05) return 15;
                else if (r < 0.20) return 20;
                else if (r < 0.40) return 25;
                else if (r < 0.60) return 30;
                else if (r < 0.75) return 35;
                else if (r < 0.87) return 40;
                else if (r < 0.95) return 45;
                else if (r < 0.99) return 50;
                else return 55;

            case 5:
                // Tabla 5.17 — equipo de 5 personas (CORREGIDA)
                if (r < 0.10) return 10;
                else if (r < 0.28) return 15;
                else if (r < 0.50) return 20;
                else if (r < 0.68) return 25;
                else if (r < 0.78) return 30;
                else if (r < 0.86) return 35;
                else if (r < 0.92) return 40;
                else if (r < 0.97) return 45;
                else return 50;

            case 6:
                // Tabla 5.18 — equipo de 6 personas (CORREGIDA)
                if (r < 0.12) return 5;
                else if (r < 0.27) return 10;
                else if (r < 0.53) return 15;
                else if (r < 0.68) return 20;
                else if (r < 0.80) return 25;
                else if (r < 0.88) return 30;
                else if (r < 0.94) return 35;
                else if (r < 0.98) return 40;
                else return 45;

            default:
                return 20;
        }
    }
}
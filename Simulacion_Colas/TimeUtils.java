public class TimeUtils {

    /**
     * Minutos desde medianoche que representan el inicio del turno.
     * Ya no es una constante fija — se configura antes de cada simulación
     * llamando a initializeStartTime(int hour, int minute).
     *
     * Valor por defecto: 23:00 (11 PM) por compatibilidad con el modelo del PDF.
     */
    private static int startMinutes = 23 * 60;

    /**
     * Mantiene compatibilidad con llamadas antiguas sin parámetros.
     * Usa el valor actual de startMinutes sin modificarlo.
     */
    public static void initializeStartTime() {
        // No hace nada — usa el valor ya configurado.
    }

    /**
     * Configura la hora de inicio del turno antes de correr la simulación.
     * Debe llamarse desde QueueSimulator antes de procesar camiones.
     *
     * @param hour   hora de inicio (0–23)
     * @param minute minuto de inicio (0–59)
     */
    public static void initializeStartTime(int hour, int minute) {
        startMinutes = hour * 60 + minute;
    }

    /**
     * Retorna los minutos desde medianoche que corresponden al inicio del turno.
     * QueueSimulator lo usa para calcular BREAK_START_MIN y SHIFT_END_MIN.
     */
    public static int getStartMinutes() {
        return startMinutes;
    }

    /**
     * Convierte minutos transcurridos desde el inicio del turno
     * a una hora legible en formato HH:mm, cruzando medianoche si es necesario.
     *
     * Ejemplo con inicio 23:00:
     *   minutesFromStart =   0  →  "23:00"
     *   minutesFromStart =  60  →  "00:00"
     *   minutesFromStart = 510  →  "07:30"
     */
    public static String formatTime(int minutesFromStart) {

        int totalMinutes = startMinutes + minutesFromStart;

        int hours   = (totalMinutes / 60) % 24;
        int minutes =  totalMinutes % 60;

        return String.format("%02d:%02d", hours, minutes);
    }
}
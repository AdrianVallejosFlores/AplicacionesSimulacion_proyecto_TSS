public class TimeUtils {

    /**
     * El turno nocturno del almacén SIEMPRE empieza a las 11:00 PM.
     * Esto equivale a 23 * 60 = 1380 minutos desde medianoche.
     *
     * CORRECCIÓN: antes se usaba LocalTime.now() lo cual hacía que
     * la hora de inicio cambiara según la hora real del sistema.
     * Para este modelo, la hora de inicio es fija: 11:00 PM.
     */
    private static final int START_MINUTES = 23 * 60; // 11:00 PM = 1380 min

    /**
     * Mantenemos este método para no romper las llamadas desde QueueSimulator,
     * pero ya no hace nada — la hora de inicio es siempre la misma.
     */
    public static void initializeStartTime() {
        // Hora de inicio fija: 23:00 (11 PM). No se necesita inicialización dinámica.
    }

    /**
     * Convierte minutos transcurridos desde el inicio del turno
     * a una hora legible en formato HH:mm, cruzando medianoche si es necesario.
     *
     * Ejemplo:
     *   minutesFromStart = 0   → "23:00"  (11:00 PM)
     *   minutesFromStart = 60  → "00:00"  (medianoche)
     *   minutesFromStart = 510 → "07:30"  (7:30 AM — fin del turno)
     */
    public static String formatTime(int minutesFromStart) {

        int totalMinutes = START_MINUTES + minutesFromStart;

        int hours   = (totalMinutes / 60) % 24;
        int minutes =  totalMinutes % 60;

        return String.format("%02d:%02d", hours, minutes);
    }
}
public class Customer {

    int id;
    double rArrival;
    int interArrival;
    int arrivalTime;
    double rService;
    int serviceTime;
    int serviceStart;
    int serviceEnd;
    int waitingTime;
    int systemTime;

    // ── NUEVOS CAMPOS ─────────────────────────────────────────────────
    int idleTime;       // Ocio del personal (min que el servidor espera sin camión)
    int queueLength;    // Longitud de la cola al momento de llegar este camión

    public Customer(int id) {
        this.id = id;
    }
}
public class LeadTimeDistribution {

    public static int getLeadTime(double r) {

        if (r < 0.30) return 1;
        else if (r < 0.70) return 2;
        else return 3;
    }
}
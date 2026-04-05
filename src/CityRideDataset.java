import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;


public final class CityRideDataset {

    private CityRideDataset() {}

    public static final int MIN_ZONE = 1;
    public static final int MAX_ZONE = 5;

    public enum TimeBand {
        PEAK,
        OFF_PEAK;

        @Override
        public String toString() {
            return this == PEAK ? "Peak" : "Off-Peak";
        }
    }

    public enum PassengerType {
        ADULT,
        STUDENT,
        CHILD,
        SENIOR_CITIZEN;

        @Override
        public String toString() {
            return name().charAt(0) + name().substring(1).toLowerCase().replace("_", " ");
        }
    }


    private static Map<PassengerType, BigDecimal> discountRate = new HashMap<>();
    private static Map<PassengerType, BigDecimal> dailyCap = new HashMap<>();
    private static Map<String, BigDecimal> baseFare = new HashMap<>();
    private static String peakStartTime = "07:00";
    private static String peakEndTime = "09:30";
    private static String peakEveningStartTime = "16:30";
    private static String peakEveningEndTime = "19:00";

    static {
        loadDefaults();
    }

    public static void loadDefaults() {
        discountRate.clear();
        discountRate.put(PassengerType.ADULT, money("0.00"));
        discountRate.put(PassengerType.STUDENT, money("0.25"));
        discountRate.put(PassengerType.CHILD, money("0.50"));
        discountRate.put(PassengerType.SENIOR_CITIZEN, money("0.30"));

        dailyCap.clear();
        dailyCap.put(PassengerType.ADULT, money("8.00"));
        dailyCap.put(PassengerType.STUDENT, money("6.00"));
        dailyCap.put(PassengerType.CHILD, money("4.00"));
        dailyCap.put(PassengerType.SENIOR_CITIZEN, money("7.00"));

        baseFare.clear();
        buildDefaultBaseFares();

        peakStartTime = "07:00";
        peakEndTime = "09:30";
        peakEveningStartTime = "16:30";
        peakEveningEndTime = "19:00";
    }

    // --- Accessors ---
    public static Map<PassengerType, BigDecimal> getDiscountRates() { return discountRate; }
    public static Map<PassengerType, BigDecimal> getDailyCaps() { return dailyCap; }
    public static Map<String, BigDecimal> getBaseFares() { return baseFare; }

    public static BigDecimal getDiscountRate(PassengerType type) { return discountRate.get(type); }
    public static BigDecimal getDailyCap(PassengerType type) { return dailyCap.get(type); }

    public static String getPeakStartTime() { return peakStartTime; }
    public static String getPeakEndTime() { return peakEndTime; }
    public static String getPeakEveningStartTime() { return peakEveningStartTime; }
    public static String getPeakEveningEndTime() { return peakEveningEndTime; }

    // --- Mutators (for admin / config loading) ---
    public static void setDiscountRate(PassengerType type, BigDecimal rate) { discountRate.put(type, rate); }
    public static void setDailyCap(PassengerType type, BigDecimal cap) { dailyCap.put(type, cap); }
    public static void setBaseFare(int from, int to, TimeBand band, BigDecimal fare) {
        baseFare.put(key(from, to, band), fare);
    }
    public static void setPeakWindows(String mStart, String mEnd, String eStart, String eEnd) {
        peakStartTime = mStart;
        peakEndTime = mEnd;
        peakEveningStartTime = eStart;
        peakEveningEndTime = eEnd;
    }
    public static void setDiscountRates(Map<PassengerType, BigDecimal> rates) { discountRate = new HashMap<>(rates); }
    public static void setDailyCaps(Map<PassengerType, BigDecimal> caps) { dailyCap = new HashMap<>(caps); }
    public static void setBaseFares(Map<String, BigDecimal> fares) { baseFare = new HashMap<>(fares); }

    public static BigDecimal getBaseFare(int fromZone, int toZone, TimeBand timeBand) {
        BigDecimal fare = baseFare.get(key(fromZone, toZone, timeBand));
        if (fare == null) {
            System.out.println("Warning: No fare found for " + fromZone + "->" + toZone + " " + timeBand + ". Using £0.00.");
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return fare;
    }

    public static String key(int fromZone, int toZone, TimeBand timeBand) {
        return fromZone + "-" + toZone + "-" + timeBand.name();
    }

    public static BigDecimal money(String amount) {
        return new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
    }


    public static TimeBand determineTimeBand(String time) {
        if (time == null || time.isEmpty()) return TimeBand.OFF_PEAK;
        if ((time.compareTo(peakStartTime) >= 0 && time.compareTo(peakEndTime) <= 0) ||
                (time.compareTo(peakEveningStartTime) >= 0 && time.compareTo(peakEveningEndTime) <= 0)) {
            return TimeBand.PEAK;
        }
        return TimeBand.OFF_PEAK;
    }

    private static void buildDefaultBaseFares() {
        // Peak fares
        put(1,1,TimeBand.PEAK,"2.50"); put(1,2,TimeBand.PEAK,"3.20");
        put(1,3,TimeBand.PEAK,"3.80"); put(1,4,TimeBand.PEAK,"4.40");
        put(1,5,TimeBand.PEAK,"5.00");
        put(2,1,TimeBand.PEAK,"3.20"); put(2,2,TimeBand.PEAK,"2.30");
        put(2,3,TimeBand.PEAK,"3.10"); put(2,4,TimeBand.PEAK,"3.80");
        put(2,5,TimeBand.PEAK,"4.50");
        put(3,1,TimeBand.PEAK,"3.80"); put(3,2,TimeBand.PEAK,"3.10");
        put(3,3,TimeBand.PEAK,"2.10"); put(3,4,TimeBand.PEAK,"3.00");
        put(3,5,TimeBand.PEAK,"3.70");
        put(4,1,TimeBand.PEAK,"4.40"); put(4,2,TimeBand.PEAK,"3.80");
        put(4,3,TimeBand.PEAK,"3.00"); put(4,4,TimeBand.PEAK,"2.00");
        put(4,5,TimeBand.PEAK,"2.90");
        put(5,1,TimeBand.PEAK,"5.00"); put(5,2,TimeBand.PEAK,"4.50");
        put(5,3,TimeBand.PEAK,"3.70"); put(5,4,TimeBand.PEAK,"2.90");
        put(5,5,TimeBand.PEAK,"1.90");

        // Off-peak fares
        put(1,1,TimeBand.OFF_PEAK,"2.00"); put(1,2,TimeBand.OFF_PEAK,"2.70");
        put(1,3,TimeBand.OFF_PEAK,"3.20"); put(1,4,TimeBand.OFF_PEAK,"3.70");
        put(1,5,TimeBand.OFF_PEAK,"4.20");
        put(2,1,TimeBand.OFF_PEAK,"2.70"); put(2,2,TimeBand.OFF_PEAK,"1.90");
        put(2,3,TimeBand.OFF_PEAK,"2.60"); put(2,4,TimeBand.OFF_PEAK,"3.20");
        put(2,5,TimeBand.OFF_PEAK,"3.80");
        put(3,1,TimeBand.OFF_PEAK,"3.20"); put(3,2,TimeBand.OFF_PEAK,"2.60");
        put(3,3,TimeBand.OFF_PEAK,"1.70"); put(3,4,TimeBand.OFF_PEAK,"2.50");
        put(3,5,TimeBand.OFF_PEAK,"3.10");
        put(4,1,TimeBand.OFF_PEAK,"3.70"); put(4,2,TimeBand.OFF_PEAK,"3.20");
        put(4,3,TimeBand.OFF_PEAK,"2.50"); put(4,4,TimeBand.OFF_PEAK,"1.60");
        put(4,5,TimeBand.OFF_PEAK,"2.40");
        put(5,1,TimeBand.OFF_PEAK,"4.20"); put(5,2,TimeBand.OFF_PEAK,"3.80");
        put(5,3,TimeBand.OFF_PEAK,"3.10"); put(5,4,TimeBand.OFF_PEAK,"2.40");
        put(5,5,TimeBand.OFF_PEAK,"1.50");
    }

    private static void put(int from, int to, TimeBand band, String amount) {
        baseFare.put(key(from, to, band), money(amount));
    }
}
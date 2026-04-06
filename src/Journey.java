import java.math.BigDecimal;
import java.math.RoundingMode;

public class Journey {
    private int id;
    private String date;
    private String time;
    private int fromZone;
    private int toZone;
    private CityRideDataset.TimeBand timeBand;
    private CityRideDataset.PassengerType passengerType;
    private int zonesCrossed;
    private BigDecimal baseFare;
    private BigDecimal discountApplied;
    private BigDecimal chargedFare;

    public Journey(int id, String date, String time, int fromZone, int toZone,
                   CityRideDataset.TimeBand timeBand, CityRideDataset.PassengerType passengerType,
                   int zonesCrossed, BigDecimal baseFare, BigDecimal discountApplied, BigDecimal chargedFare) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.fromZone = fromZone;
        this.toZone = toZone;
        this.timeBand = timeBand;
        this.passengerType = passengerType;
        this.zonesCrossed = zonesCrossed;
        this.baseFare = baseFare;
        this.discountApplied = discountApplied;
        this.chargedFare = chargedFare;
    }

    //Getters
    public int getId() { return id; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public int getFromZone() { return fromZone; }
    public int getToZone() { return toZone; }
    public CityRideDataset.TimeBand getTimeBand() { return timeBand; }
    public CityRideDataset.PassengerType getPassengerType() { return passengerType; }
    public int getZonesCrossed() { return zonesCrossed; }
    public BigDecimal getBaseFare() { return baseFare; }
    public BigDecimal getDiscountApplied() { return discountApplied; }
    public BigDecimal getChargedFare() { return chargedFare; }

    //Setters
    public void setId(int id) { this.id = id; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setFromZone(int fromZone) { this.fromZone = fromZone; }
    public void setToZone(int toZone) { this.toZone = toZone; }
    public void setTimeBand(CityRideDataset.TimeBand timeBand) { this.timeBand = timeBand; }
    public void setPassengerType(CityRideDataset.PassengerType passengerType) { this.passengerType = passengerType; }
    public void setZonesCrossed(int zonesCrossed) { this.zonesCrossed = zonesCrossed; }
    public void setBaseFare(BigDecimal baseFare) { this.baseFare = baseFare; }
    public void setDiscountApplied(BigDecimal discountApplied) { this.discountApplied = discountApplied; }
    public void setChargedFare(BigDecimal chargedFare) { this.chargedFare = chargedFare; }

    /* CSV header for export. */
    public static String csvHeader() {
        return "ID,Date,Time,FromZone,ToZone,TimeBand,PassengerType,ZonesCrossed,BaseFare,DiscountApplied,ChargedFare";
    }

    /* Convert to CSV line. */
    public String toCsvLine() {
        return String.format("%d,%s,%s,%d,%d,%s,%s,%d,%.2f,%.2f,%.2f",
                id, date, time, fromZone, toZone, timeBand.name(), passengerType.name(),
                zonesCrossed, baseFare, discountApplied, chargedFare);
    }


    public static Journey fromCsvLine(String line) {
        try {
            String[] parts = line.split(",");
            if (parts.length < 11) return null;
            int id = Integer.parseInt(parts[0].trim());
            String date = parts[1].trim();
            String time = parts[2].trim();
            int fromZone = Integer.parseInt(parts[3].trim());
            int toZone = Integer.parseInt(parts[4].trim());
            CityRideDataset.TimeBand timeBand = CityRideDataset.TimeBand.valueOf(parts[5].trim());
            CityRideDataset.PassengerType pType = CityRideDataset.PassengerType.valueOf(parts[6].trim());
            int zonesCrossed = Integer.parseInt(parts[7].trim());
            BigDecimal baseFare = new BigDecimal(parts[8].trim()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal discountApplied = new BigDecimal(parts[9].trim()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal chargedFare = new BigDecimal(parts[10].trim()).setScale(2, RoundingMode.HALF_UP);
            return new Journey(id, date, time, fromZone, toZone, timeBand, pType, zonesCrossed, baseFare, discountApplied, chargedFare);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Date: %s %s | Zones: %d->%d (%d crossed) | Band: %s | Type: %s | Base: £%.2f | Disc: £%.2f | Charged: £%.2f",
                id, date, time, fromZone, toZone, zonesCrossed, timeBand, passengerType, baseFare, discountApplied, chargedFare);
    }
}
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final List<Journey> journeys = new ArrayList<>();
    private static String passengerName = "Unknown";
    private static CityRideDataset.PassengerType passengerType = CityRideDataset.PassengerType.ADULT;
    private static int nextId = 1;
    private static final Map<CityRideDataset.PassengerType, BigDecimal> runningTotals = new HashMap<>();

    public static void main(String[] args) {
        initRunningTotals();
        System.out.println("Welcome to CityRide Lite!");
        setupSession();
        
        boolean exit = false;
        while (!exit) {
            displayMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    addJourney();
                    break;
                case "2":
                    listJourneys();
                    break;
                case "3":
                    filterJourneys();
                    break;
                case "4":
                    viewDailySummary();
                    break;
                case "5":
                    viewTotalsByPassengerType();
                    break;
                case "6":
                    removeJourney();
                    break;
                case "7":
                    resetDay();
                    break;
                case "8":
                    exit = true;
                    System.out.println("Exiting CityRide Lite. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void initRunningTotals() {
        for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
            runningTotals.put(type, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
    }
    
    private static void setupSession() {
        System.out.print("Enter Passenger Name: ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) {
            passengerName = name;
        }

        passengerType = readPassengerType();
        System.out.println("Session started for " + passengerName + " as " + passengerType);
    }

    private static void displayMenu() {
        System.out.println("\nSession: " + passengerName + " (" + passengerType + ")");
        System.out.println("--- Main Menu ---");
        System.out.println("1. Add Journey");
        System.out.println("2. List Journeys");
        System.out.println("3. Filter Journeys");
        System.out.println("4. View Daily Summary");
        System.out.println("5. View Totals by Passenger Type");
        System.out.println("6. Undo/Remove Journey");
        System.out.println("7. Reset Day");
        System.out.println("8. Exit");
        System.out.print("Choose an option: ");
    }
    
    private static void addJourney() {
        System.out.println("\nAdd Journey");
        
        System.out.print("Enter Date (DD/MM/YYYY): ");
        String date = scanner.nextLine().trim();
        if (date.isEmpty()) {
            System.out.println("Date cannot be blank.");
            return;
        }

        int fromZone = readZone("Starting Zone (1-5): ");
        int toZone = readZone("Destination Zone (1-5): ");

        CityRideDataset.TimeBand timeBand = readTimeBand();

        int zonesCrossed = Math.abs(toZone - fromZone) + 1;
        BigDecimal baseFare = CityRideDataset.getBaseFare(fromZone, toZone, timeBand);
        BigDecimal discountRate = CityRideDataset.DISCOUNT_RATE.get(passengerType);
        BigDecimal discountApplied = baseFare.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountedFare = baseFare.subtract(discountApplied);

        BigDecimal currentTotal = runningTotals.get(passengerType);
        BigDecimal cap = CityRideDataset.DAILY_CAP.get(passengerType);
        BigDecimal chargedFare;

        if (currentTotal.compareTo(cap) >= 0) {
            chargedFare = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else if (currentTotal.add(discountedFare).compareTo(cap) > 0) {
            chargedFare = cap.subtract(currentTotal);
        } else {
            chargedFare = discountedFare;
        }

        Journey journey = new Journey(nextId++, date, fromZone, toZone, timeBand, passengerType, 
                zonesCrossed, baseFare, discountApplied, chargedFare);
        journeys.add(journey);
        runningTotals.put(passengerType, currentTotal.add(chargedFare));

        System.out.println("Journey added successfully! Charged Fare: £" + chargedFare);
    }

    private static int readZone(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int zone = Integer.parseInt(input);
                if (zone >= CityRideDataset.MIN_ZONE && zone <= CityRideDataset.MAX_ZONE) {
                    return zone;
                }
                System.out.println("Zones must be between " + CityRideDataset.MIN_ZONE + " and " + CityRideDataset.MAX_ZONE + ".");
            } catch (NumberFormatException e) {
                System.out.println("Non-numeric zone input. Please enter a number.");
            }
        }
    }

    private static CityRideDataset.PassengerType readPassengerType() {
        while (true) {
            System.out.print("Enter Passenger Type (Adult, Student, Child, Senior Citizen): ");
            String typeStr = scanner.nextLine().trim().toUpperCase().replace(" ", "_");
            try {
                return CityRideDataset.PassengerType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                System.out.println("Unknown passenger type! Please enter either adult, student, child or senior citizen.");
            }
        }
    }

    private static CityRideDataset.TimeBand readTimeBand() {
        while (true) {
            System.out.print("Enter Time Band (Peak/Off-peak): ");
            String tb = scanner.nextLine().trim().toLowerCase().replace("-", "_");
            if (tb.equals("peak")) {
                return CityRideDataset.TimeBand.PEAK;
            } else if (tb.equals("off_peak")) {
                return CityRideDataset.TimeBand.OFF_PEAK;
            } else {
                System.out.println("Invalid time band. Please enter Peak or Off-peak!");
            }
        }
    }

    private static void listJourneys() {
        if (journeys.isEmpty()) {
            System.out.println("No journeys recorded.");
            return;
        }
        System.out.println("\n----------------------");
        System.out.println("\n-----All Journeys-----");
        System.out.println("\n----------------------");
        for (Journey j : journeys) {
            System.out.println(j);
        }
    }

    private static void filterJourneys() {
        if (journeys.isEmpty()) {
            System.out.println("No journeys to filter.");
            return;
        }
        System.out.println("\nFilter by: 1. Passenger Type, 2. Time Band, 3. Zone, 4. Date");
        String choice = scanner.nextLine().trim();
        List<Journey> filtered = new ArrayList<>();

        switch (choice) {
            case "1":
                CityRideDataset.PassengerType type = readPassengerType();
                for (Journey j : journeys) {
                    if (j.getPassengerType() == type) filtered.add(j);
                }
                break;
            case "2":
                CityRideDataset.TimeBand tb = readTimeBand();
                for (Journey j : journeys) {
                    if (j.getTimeBand() == tb) filtered.add(j);
                }
                break;
            case "3":
                int zone = readZone("Zone to search for (1-5): ");
                for (Journey j : journeys) {
                    if (j.getFromZone() == zone || j.getToZone() == zone) filtered.add(j);
                }
                break;
            case "4":
                System.out.print("Enter Date (DD/MM/YYYY): ");
                String dateInput = scanner.nextLine().trim();
                for (Journey j : journeys) {
                    if (j.getDate().equals(dateInput)) filtered.add(j);
                }
                break;
            default:
                System.out.println("Invalid option.");
                return;
        }

        if (filtered.isEmpty()) {
            System.out.println("No journeys found matching the criteria.");
        } else {
            for (Journey j : filtered) {
                System.out.println(j);
            }
        }
    }

    private static void viewDailySummary() {
        if (journeys.isEmpty()) {
            System.out.println("No journeys recorded.");
            return;
        }
        BigDecimal totalCharged = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        Journey mostExpensive = journeys.get(0);

        for (Journey j : journeys) {
            totalCharged = totalCharged.add(j.getChargedFare());
            if (j.getChargedFare().compareTo(mostExpensive.getChargedFare()) > 0) {
                mostExpensive = j;
            }
        }

        BigDecimal average = totalCharged.divide(new BigDecimal(journeys.size()), 2, RoundingMode.HALF_UP);

        System.out.println("\n--- Daily Summary ---");
        System.out.println("Total journeys: " + journeys.size());
        System.out.println("Total cost charged: £" + totalCharged);
        System.out.println("Average cost per journey: £" + average);
        System.out.println("Most expensive journey: ID " + mostExpensive.getId() + " (£" + mostExpensive.getChargedFare() + ")");
    }

    private static void viewTotalsByPassengerType() {
        System.out.println("\n--- Totals by Passenger Type ---");
        for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
            int count = 0;
            BigDecimal preDiscountSum = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            BigDecimal discountedSum = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            BigDecimal chargedSum = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            for (Journey j : journeys) {
                if (j.getPassengerType() == type) {
                    count++;
                    preDiscountSum = preDiscountSum.add(j.getBaseFare());
                    discountedSum = discountedSum.add(j.getBaseFare().subtract(j.getDiscountApplied()));
                    chargedSum = chargedSum.add(j.getChargedFare());
                }
            }
            
            boolean capReached = chargedSum.compareTo(CityRideDataset.DAILY_CAP.get(type)) >= 0;
            System.out.printf("%s: Count: %d | Pre-discount: £%.2f | Discounted: £%.2f | Charged: £%.2f | Cap reached: %s\n",
                    type, count, preDiscountSum, discountedSum, chargedSum, capReached ? "Yes" : "No");
        }

        int peakCount = 0;
        int offPeakCount = 0;
        for (Journey j : journeys) {
            if (j.getTimeBand() == CityRideDataset.TimeBand.PEAK) peakCount++;
            else offPeakCount++;
        }
        System.out.println("\nCategory Counts:");
        System.out.println("Peak journeys: " + peakCount);
        System.out.println("Off-peak journeys: " + offPeakCount);
    }

    private static void removeJourney() {
        if (journeys.isEmpty()) {
            System.out.println("No journeys to remove.");
            return;
        }
        int id = readInt("Enter Journey ID to remove: ");

        Journey toRemove = null;
        for (Journey j : journeys) {
            if (j.getId() == id) {
                toRemove = j;
                break;
            }
        }

        if (toRemove == null) {
            System.out.println("Error: Journey with ID " + id + " not found.");
            return;
        }

        System.out.print("Are you sure you want to remove journey " + id + "? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            journeys.remove(toRemove);
            recalculateTotals();
            System.out.println("Journey removed and totals recalculated.");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric input. Please enter a number.");
            }
        }
    }

    private static void recalculateTotals() {
        initRunningTotals();
        List<Journey> currentJourneys = new ArrayList<>(journeys);
        journeys.clear();
        nextId = 1;

        for (Journey old : currentJourneys) {
            BigDecimal baseFare = old.getBaseFare();
            BigDecimal discountApplied = old.getDiscountApplied();
            BigDecimal discountedFare = baseFare.subtract(discountApplied);

            BigDecimal currentTotal = runningTotals.get(old.getPassengerType());
            BigDecimal cap = CityRideDataset.DAILY_CAP.get(old.getPassengerType());
            BigDecimal chargedFare;

            if (currentTotal.compareTo(cap) >= 0) {
                chargedFare = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            } else if (currentTotal.add(discountedFare).compareTo(cap) > 0) {
                chargedFare = cap.subtract(currentTotal);
            } else {
                chargedFare = discountedFare;
            }

            Journey newJ = new Journey(nextId++, old.getDate(), old.getFromZone(), old.getToZone(),
                    old.getTimeBand(), old.getPassengerType(), old.getZonesCrossed(), 
                    baseFare, discountApplied, chargedFare);
            journeys.add(newJ);
            runningTotals.put(old.getPassengerType(), currentTotal.add(chargedFare));
        }
    }

    private static void resetDay() {
        journeys.clear();
        initRunningTotals();
        nextId = 1;
        System.out.println("The day has been reset. All journeys and totals are cleared.");
    }

    static class Journey {
        private final int id;
        private final String date;
        private final int fromZone;
        private final int toZone;
        private final CityRideDataset.TimeBand timeBand;
        private final CityRideDataset.PassengerType passengerType;
        private final int zonesCrossed;
        private final BigDecimal baseFare;
        private final BigDecimal discountApplied;
        private final BigDecimal chargedFare;

        public Journey(int id, String date, int fromZone, int toZone,
                       CityRideDataset.TimeBand timeBand, CityRideDataset.PassengerType passengerType,
                       int zonesCrossed, BigDecimal baseFare, BigDecimal discountApplied, BigDecimal chargedFare) {
            this.id = id;
            this.date = date;
            this.fromZone = fromZone;
            this.toZone = toZone;
            this.timeBand = timeBand;
            this.passengerType = passengerType;
            this.zonesCrossed = zonesCrossed;
            this.baseFare = baseFare;
            this.discountApplied = discountApplied;
            this.chargedFare = chargedFare;
        }

        public int getId() { return id; }
        public String getDate() { return date; }
        public int getFromZone() { return fromZone; }
        public int getToZone() { return toZone; }
        public CityRideDataset.TimeBand getTimeBand() { return timeBand; }
        public CityRideDataset.PassengerType getPassengerType() { return passengerType; }
        public int getZonesCrossed() { return zonesCrossed; }
        public BigDecimal getBaseFare() { return baseFare; }
        public BigDecimal getDiscountApplied() { return discountApplied; }
        public BigDecimal getChargedFare() { return chargedFare; }

        public String toString() {
            return String.format("ID: %d | Date: %s | Zones: %d->%d (%d crossed) | Band: %s | Type: %s | Base: £%.2f | Disc: £%.2f | Charged: £%.2f",
                    id, date, fromZone, toZone, zonesCrossed, timeBand, passengerType, baseFare, discountApplied, chargedFare);
        }
    }
}

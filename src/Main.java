import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static RiderProfile activeProfile = new RiderProfile();



    private static final List<Journey> journeys = new ArrayList<>();
    private static final Map<CityRideDataset.PassengerType, BigDecimal> runningTotals = new HashMap<>();
    private static int nextId = 1;

    private static void initRunningTotals() {
        for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
            runningTotals.put(type, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
    }

    private static BigDecimal getRunningTotal(CityRideDataset.PassengerType type) {
        return runningTotals.get(type);
    }

    private static boolean isCapReached(CityRideDataset.PassengerType type) {
        return runningTotals.get(type).compareTo(CityRideDataset.getDailyCap(type)) >= 0;
    }

    private static BigDecimal applyDailyCap(CityRideDataset.PassengerType type, BigDecimal discountedFare) {
        BigDecimal currentTotal = runningTotals.get(type);
        BigDecimal cap = CityRideDataset.getDailyCap(type);
        if (currentTotal.compareTo(cap) >= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else if (currentTotal.add(discountedFare).compareTo(cap) > 0) {
            return cap.subtract(currentTotal);
        } else {
            return discountedFare;
        }
    }


    private static Journey doAddJourney(String date, String time, int fromZone, int toZone,
                                        CityRideDataset.PassengerType passengerType) {
        CityRideDataset.TimeBand timeBand = CityRideDataset.determineTimeBand(time);
        int zonesCrossed = Math.abs(toZone - fromZone) + 1;
        BigDecimal baseFare = CityRideDataset.getBaseFare(fromZone, toZone, timeBand);
        BigDecimal discountRate = CityRideDataset.getDiscountRate(passengerType);
        BigDecimal discountApplied = baseFare.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountedFare = baseFare.subtract(discountApplied);
        BigDecimal chargedFare = applyDailyCap(passengerType, discountedFare);

        Journey journey = new Journey(nextId++, date, time, fromZone, toZone, timeBand, passengerType,
                zonesCrossed, baseFare, discountApplied, chargedFare);
        journeys.add(journey);
        runningTotals.put(passengerType, runningTotals.get(passengerType).add(chargedFare));
        return journey;
    }


    private static void recalculateTotals() {
        initRunningTotals();
        List<Journey> temp = new ArrayList<>(journeys);
        journeys.clear();
        nextId = 1;

        for (Journey old : temp) {
            BigDecimal baseFare = CityRideDataset.getBaseFare(old.getFromZone(), old.getToZone(), old.getTimeBand());
            BigDecimal discountRate = CityRideDataset.getDiscountRate(old.getPassengerType());
            BigDecimal discountApplied = baseFare.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal discountedFare = baseFare.subtract(discountApplied);
            BigDecimal chargedFare = applyDailyCap(old.getPassengerType(), discountedFare);

            Journey rebuilt = new Journey(nextId++, old.getDate(), old.getTime(),
                    old.getFromZone(), old.getToZone(), old.getTimeBand(), old.getPassengerType(),
                    old.getZonesCrossed(), baseFare, discountApplied, chargedFare);
            journeys.add(rebuilt);
            runningTotals.put(old.getPassengerType(), runningTotals.get(old.getPassengerType()).add(chargedFare));
        }
        nextId = journeys.isEmpty() ? 1 : journeys.get(journeys.size() - 1).getId() + 1;
    }

    private static Journey findById(int id) {
        for (Journey j : journeys) {
            if (j.getId() == id) return j;
        }
        return null;
    }

    private static int importFromCsv(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File not found: " + filename);
            return 0;
        }
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                Journey parsed = Journey.fromCsvLine(line);
                if (parsed != null) {
                    doAddJourney(parsed.getDate(), parsed.getTime(), parsed.getFromZone(),
                            parsed.getToZone(), parsed.getPassengerType());
                    count++;
                } else {
                    System.out.println("Skipping invalid CSV line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV: " + e.getMessage());
        }
        return count;
    }


    private static void exportJourneysToCsv(String filename) {
        try {
            File dir = new File(filename).getParentFile();
            if (dir != null && !dir.exists()) dir.mkdirs();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                writer.write(Journey.csvHeader());
                writer.newLine();
                for (Journey j : journeys) {
                    writer.write(j.toCsvLine());
                    writer.newLine();
                }
            }
            System.out.println("Journeys exported to " + filename);
        } catch (IOException e) {
            System.out.println("Error exporting CSV: " + e.getMessage());
        }
    }



    private static int readZone(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int zone = Integer.parseInt(input);
                if (zone >= CityRideDataset.MIN_ZONE && zone <= CityRideDataset.MAX_ZONE) return zone;
                System.out.printf("Error: Zone must be between %d and %d. Please try again.%n",
                        CityRideDataset.MIN_ZONE, CityRideDataset.MAX_ZONE);
            } catch (NumberFormatException e) {
                System.out.println("Error: Non-numeric input. Please enter a number (e.g. 1).");
            }
        }
    }

    private static String readDate() {
        while (true) {
            System.out.print("Enter date (DD/MM/YYYY, e.g. 05/04/2026): ");
            String input = scanner.nextLine().trim();
            if (input.matches("\\d{2}/\\d{2}/\\d{4}")) return input;
            System.out.println("Error: Invalid date format. Please use DD/MM/YYYY (e.g. 05/04/2026).");
        }
    }

    private static String readTime() {
        while (true) {
            System.out.print("Enter time (HH:mm, e.g. 08:30): ");
            String input = scanner.nextLine().trim();
            if (input.matches("\\d{2}:\\d{2}")) {
                String[] parts = input.split(":");
                int h = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                if (h >= 0 && h <= 23 && m >= 0 && m <= 59) return input;
            }
            System.out.println("Error: Invalid time. Use HH:mm with valid hours (00-23) and minutes (00-59).");
        }
    }

    private static CityRideDataset.PassengerType readPassengerType() {
        while (true) {
            System.out.print("Enter passenger type (Adult, Student, Child, Senior Citizen): ");
            String typeStr = scanner.nextLine().trim().toUpperCase().replace(" ", "_");
            try {
                return CityRideDataset.PassengerType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: Unknown passenger type. Please enter Adult, Student, Child, or Senior Citizen.");
            }
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number. Please enter a whole number (e.g. 1).");
            }
        }
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("Error: This field cannot be blank. Please enter a value.");
        }
    }

    private static boolean confirm(String prompt) {
        System.out.print(prompt + " (y/n): ");
        return scanner.nextLine().trim().equalsIgnoreCase("y");
    }

    private static boolean isValidTime(String time) {
        if (time == null || !time.matches("\\d{2}:\\d{2}")) return false;
        String[] parts = time.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        return h >= 0 && h <= 23 && m >= 0 && m <= 59;
    }

    private static BigDecimal readMoney(String input) {
        try {
            return new BigDecimal(input).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }



    public static void main(String[] args) {
        initRunningTotals();
        System.out.println("=============================================");
        System.out.println("   Welcome to CityRide Lite (Part 2)");
        System.out.println("=============================================\n");


        RiderProfile.loadConfig();


        if (RiderProfile.hasSavedState()) {
            System.out.print("A saved session was found. Resume it? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                RiderProfile loaded = RiderProfile.loadSavedProfile();
                if (loaded != null) {
                    activeProfile = loaded;
                    int count = importFromCsv(RiderProfile.getSavedJourneysPath());
                    System.out.println("Resumed session for " + activeProfile.getName() +
                            " with " + count + " journeys.");
                }
            }
        }

        // Main loop
        boolean exit = false;
        while (!exit) {
            displayMainMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": profileMenu(); break;
                case "2": journeyMenu(); break;
                case "3": calculateAndView(); break;
                case "4": reportsMenu(); break;
                case "5": adminMenu(); break;          // Req 1, 15
                case "6": exit = handleExit(); break;   // Req 20
                default: System.out.println("Invalid choice. Please enter a number from 1 to 6.");
            }
        }
    }



    private static void displayMainMenu() {
        System.out.println("\n--- Session: " + activeProfile.getName() +
                " (" + activeProfile.getPassengerType() + ") ---");
        BigDecimal running = getRunningTotal(activeProfile.getPassengerType());
        BigDecimal cap = CityRideDataset.getDailyCap(activeProfile.getPassengerType());
        System.out.printf("Running total: £%.2f / £%.2f cap%n", running, cap);
        if (isCapReached(activeProfile.getPassengerType())) {
            System.out.println("[DAILY CAP REACHED - further journeys are free]");
        }
        System.out.println("\n========= MAIN MENU =========");
        System.out.println("1. Profile (create/load/save)");
        System.out.println("2. Journeys (add/edit/delete/import/export)");
        System.out.println("3. Calculate & View Costs");
        System.out.println("4. Reports & Summary");
        System.out.println("5. Admin Menu");
        System.out.println("6. Save & Exit");
        System.out.println("=============================");
        System.out.print("Choose an option (1-6): ");
    }



    private static void profileMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Profile Menu ---");
            System.out.println("1. View Current Profile");
            System.out.println("2. Create New Profile");
            System.out.println("3. Load Saved Profile");
            System.out.println("4. Save Current Profile");
            System.out.println("5. List Saved Profiles");
            System.out.println("6. Back to Main Menu");
            System.out.print("Choose an option (1-6): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": System.out.println("\nCurrent Profile: " + activeProfile); break;
                case "2": createProfile(); break;
                case "3": loadProfile(); break;
                case "4": activeProfile.saveToFile(); break;
                case "5": RiderProfile.listProfiles(); break;
                case "6": back = true; break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void createProfile() {
        String name = readNonEmpty("Enter rider name (e.g. John Smith): ");
        CityRideDataset.PassengerType type = readPassengerType();
        System.out.print("Enter default payment method (Contactless/Oyster/Cash, e.g. Contactless): ");
        String payment = scanner.nextLine().trim();
        if (payment.isEmpty()) payment = "Contactless";
        activeProfile = new RiderProfile(name, type, payment);
        System.out.println("Profile created: " + activeProfile);
    }

    private static void loadProfile() {
        RiderProfile.listProfiles();
        String name = readNonEmpty("Enter profile name to load: ");
        RiderProfile loaded = RiderProfile.loadFromFile(name);
        if (loaded != null) activeProfile = loaded;
    }



    private static void journeyMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Journey Menu ---");
            System.out.printf("(%d journeys recorded)%n", journeys.size());
            System.out.println("1. Add Journey");
            System.out.println("2. Edit Journey");
            System.out.println("3. Delete Journey");
            System.out.println("4. List All Journeys");
            System.out.println("5. Filter Journeys");
            System.out.println("6. Import Journeys from CSV");
            System.out.println("7. Export Journeys to CSV");
            System.out.println("8. Reset Day (clear all)");
            System.out.println("9. Back to Main Menu");
            System.out.print("Choose an option (1-9): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": addJourney(); break;
                case "2": editJourney(); break;
                case "3": deleteJourney(); break;
                case "4": listJourneys(); break;
                case "5": filterJourneys(); break;
                case "6": importJourneys(); break;
                case "7": exportJourneys(); break;
                case "8": resetDay(); break;
                case "9": back = true; break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void addJourney() {
        System.out.println("\n--- Add Journey ---");
        String date = readDate();
        String time = readTime();
        int fromZone = readZone("Starting zone (1-5, e.g. 1): ");
        int toZone = readZone("Destination zone (1-5, e.g. 3): ");

        System.out.printf("Passenger type [%s] - press Enter to keep or type new: ", activeProfile.getPassengerType());
        String typeInput = scanner.nextLine().trim();
        CityRideDataset.PassengerType pType = activeProfile.getPassengerType();
        if (!typeInput.isEmpty()) {
            try {
                pType = CityRideDataset.PassengerType.valueOf(typeInput.toUpperCase().replace(" ", "_"));
            } catch (IllegalArgumentException e) {
                System.out.println("Unknown type, using profile default: " + activeProfile.getPassengerType());
            }
        }

        Journey j = doAddJourney(date, time, fromZone, toZone, pType);


        System.out.println("\nJourney added successfully!");
        System.out.println(j);
        BigDecimal running = getRunningTotal(pType);
        BigDecimal cap = CityRideDataset.getDailyCap(pType);
        System.out.printf("Running total (%s): £%.2f / £%.2f cap%n", pType, running, cap);
        if (isCapReached(pType)) {
            System.out.println("[DAILY CAP REACHED - further journeys for " + pType + " are free]");
        }
    }

    private static void editJourney() {
        if (journeys.isEmpty()) { System.out.println("No journeys to edit."); return; }
        System.out.println("\n--- Edit Journey ---");
        listJourneys();
        int id = readInt("Enter journey ID to edit (e.g. 1): ");
        Journey target = findById(id);
        if (target == null) {
            System.out.println("Error: Journey with ID " + id + " not found."); // Req 6b
            return;
        }

        System.out.println("Editing: " + target);
        System.out.println("(Press Enter to keep current value for each field)\n");

        System.out.printf("Date [%s]: ", target.getDate());
        String dateInput = scanner.nextLine().trim();
        String date = dateInput.isEmpty() ? target.getDate() : dateInput;

        System.out.printf("Time [%s]: ", target.getTime());
        String timeInput = scanner.nextLine().trim();
        String time = timeInput.isEmpty() ? target.getTime() : timeInput;

        System.out.printf("From zone [%d]: ", target.getFromZone());
        String fromInput = scanner.nextLine().trim();
        int fromZone = fromInput.isEmpty() ? target.getFromZone() : Integer.parseInt(fromInput);

        System.out.printf("To zone [%d]: ", target.getToZone());
        String toInput = scanner.nextLine().trim();
        int toZone = toInput.isEmpty() ? target.getToZone() : Integer.parseInt(toInput);

        System.out.printf("Passenger type [%s]: ", target.getPassengerType());
        String typeInput = scanner.nextLine().trim();
        CityRideDataset.PassengerType pType = target.getPassengerType();
        if (!typeInput.isEmpty()) {
            try {
                pType = CityRideDataset.PassengerType.valueOf(typeInput.toUpperCase().replace(" ", "_"));
            } catch (IllegalArgumentException e) {
                System.out.println("Unknown type, keeping: " + target.getPassengerType());
            }
        }

        if (fromZone < CityRideDataset.MIN_ZONE || fromZone > CityRideDataset.MAX_ZONE ||
                toZone < CityRideDataset.MIN_ZONE || toZone > CityRideDataset.MAX_ZONE) {
            System.out.println("Error: Zones must be between " + CityRideDataset.MIN_ZONE +
                    " and " + CityRideDataset.MAX_ZONE + ". Edit cancelled.");
            return;
        }

        // Apply edits
        CityRideDataset.TimeBand timeBand = CityRideDataset.determineTimeBand(time);
        int zonesCrossed = Math.abs(toZone - fromZone) + 1;
        BigDecimal baseFare = CityRideDataset.getBaseFare(fromZone, toZone, timeBand);
        BigDecimal discountRate = CityRideDataset.getDiscountRate(pType);
        BigDecimal discountApplied = baseFare.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);

        target.setDate(date);
        target.setTime(time);
        target.setFromZone(fromZone);
        target.setToZone(toZone);
        target.setTimeBand(timeBand);
        target.setPassengerType(pType);
        target.setZonesCrossed(zonesCrossed);
        target.setBaseFare(baseFare);
        target.setDiscountApplied(discountApplied);

        recalculateTotals();
        System.out.println("Journey updated and totals recalculated.");
        Journey updated = findById(id);
        if (updated != null) System.out.println("Updated: " + updated);
    }

    private static void deleteJourney() {
        if (journeys.isEmpty()) { System.out.println("No journeys to delete."); return; }
        System.out.println("\n--- Delete Journey ---");
        listJourneys();
        int id = readInt("Enter journey ID to remove (e.g. 1): ");
        Journey target = findById(id);
        if (target == null) {
            System.out.println("Error: Journey with ID " + id + " not found."); // Req 6b
            return;
        }

        System.out.println("Journey to remove: " + target);
        if (confirm("Are you sure you want to remove this journey?")) {
            journeys.remove(target);
            recalculateTotals(); // Req 6a
            System.out.println("Journey removed and totals recalculated.");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private static void listJourneys() {
        if (journeys.isEmpty()) { System.out.println("No journeys recorded."); return; }
        System.out.println("\n--- All Journeys ---");
        for (Journey j : journeys) System.out.println(j);
        System.out.println("--- " + journeys.size() + " journey(s) ---");
    }

    private static void filterJourneys() {
        if (journeys.isEmpty()) { System.out.println("No journeys to filter."); return; }
        System.out.println("\nFilter by:");
        System.out.println("1. Passenger Type");
        System.out.println("2. Time Band");
        System.out.println("3. Zone");
        System.out.println("4. Date");
        System.out.print("Choose filter (1-4): ");
        String choice = scanner.nextLine().trim();
        List<Journey> filtered = new ArrayList<>();

        switch (choice) {
            case "1":
                CityRideDataset.PassengerType type = readPassengerType();
                for (Journey j : journeys) { if (j.getPassengerType() == type) filtered.add(j); }
                break;
            case "2":
                System.out.print("Enter time band (Peak/Off-Peak): ");
                String tb = scanner.nextLine().trim().toLowerCase().replace("-", "_");
                CityRideDataset.TimeBand band = tb.equals("peak") ? CityRideDataset.TimeBand.PEAK :
                        tb.equals("off_peak") ? CityRideDataset.TimeBand.OFF_PEAK : null;
                if (band == null) { System.out.println("Invalid time band."); return; }
                for (Journey j : journeys) { if (j.getTimeBand() == band) filtered.add(j); }
                break;
            case "3":
                int zone = readZone("Zone to filter by (1-5, e.g. 2): ");
                for (Journey j : journeys) { if (j.getFromZone() == zone || j.getToZone() == zone) filtered.add(j); }
                break;
            case "4":
                String date = readDate();
                for (Journey j : journeys) { if (j.getDate().equals(date)) filtered.add(j); }
                break;
            default: System.out.println("Invalid filter option."); return;
        }

        if (filtered.isEmpty()) {
            System.out.println("No journeys found matching the criteria.");
        } else {
            System.out.println("\n--- Filtered Results (" + filtered.size() + " found) ---");
            for (Journey j : filtered) System.out.println(j);
        }
    }

    private static void importJourneys() {
        String filename = readNonEmpty("Enter CSV file path to import (e.g. data/journeys.csv): ");
        int count = importFromCsv(filename);
        System.out.println(count + " journey(s) imported.");
    }

    private static void exportJourneys() {
        if (journeys.isEmpty()) { System.out.println("No journeys to export."); return; }
        System.out.print("Enter output file path (e.g. data/export.csv, press Enter for default): ");
        String filename = scanner.nextLine().trim();
        if (filename.isEmpty()) filename = "data/journeys_export.csv";
        exportJourneysToCsv(filename);
    }

    private static void resetDay() {
        if (confirm("This will clear ALL journeys and totals. Are you sure?")) {
            journeys.clear();
            initRunningTotals();
            nextId = 1;
            System.out.println("Day reset. All journeys and totals cleared.");
        }
    }



    private static void calculateAndView() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Calculate & View ---");
            System.out.println("1. View Per-Journey Costs (with running totals)");
            System.out.println("2. View Totals by Passenger Type");
            System.out.println("3. Back to Main Menu");
            System.out.print("Choose an option (1-3): ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": viewPerJourneyCosts(); break;
                case "2": viewTotalsByType(); break;
                case "3": back = true; break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void viewPerJourneyCosts() {
        if (journeys.isEmpty()) { System.out.println("No journeys recorded."); return; }
        System.out.println("\n--- Per-Journey Cost Breakdown ---");
        Map<CityRideDataset.PassengerType, BigDecimal> runTotals = new HashMap<>();
        for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
            runTotals.put(type, BigDecimal.ZERO);
        }
        for (Journey j : journeys) {
            runTotals.put(j.getPassengerType(), runTotals.get(j.getPassengerType()).add(j.getChargedFare()));
            BigDecimal cap = CityRideDataset.getDailyCap(j.getPassengerType());
            boolean capApplied = j.getChargedFare().compareTo(j.getBaseFare().subtract(j.getDiscountApplied())) < 0;
            String capStatus = capApplied ? " [CAP APPLIED]" : "";
            System.out.printf("  %s | Running: £%.2f / £%.2f%s%n",
                    j, runTotals.get(j.getPassengerType()), cap, capStatus);
        }
    }

    private static void viewTotalsByType() {
        System.out.println("\n--- Totals by Passenger Type ---");
        for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
            int count = 0;
            BigDecimal preDiscount = BigDecimal.ZERO;
            BigDecimal discounted = BigDecimal.ZERO;
            BigDecimal charged = BigDecimal.ZERO;
            for (Journey j : journeys) {
                if (j.getPassengerType() == type) {
                    count++;
                    preDiscount = preDiscount.add(j.getBaseFare());
                    discounted = discounted.add(j.getBaseFare().subtract(j.getDiscountApplied()));
                    charged = charged.add(j.getChargedFare());
                }
            }
            if (count > 0) {
                boolean capReached = charged.compareTo(CityRideDataset.getDailyCap(type)) >= 0;
                System.out.printf("  %s: %d journeys | Pre-discount: £%.2f | Discounted: £%.2f | Charged: £%.2f | Cap: %s%n",
                        type, count, preDiscount, discounted, charged, capReached ? "REACHED" : "not reached");
            }
        }
        int peakCount = 0, offPeakCount = 0;
        for (Journey j : journeys) {
            if (j.getTimeBand() == CityRideDataset.TimeBand.PEAK) peakCount++;
            else offPeakCount++;
        }
        System.out.println("\n  Peak journeys:     " + peakCount);
        System.out.println("  Off-peak journeys: " + offPeakCount);
    }



    private static void reportsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Reports Menu ---");
            System.out.println("1. View Daily Summary");
            System.out.println("2. Export CSV Report");
            System.out.println("3. Export Text Summary");
            System.out.println("4. Back to Main Menu");
            System.out.print("Choose an option (1-4): ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": displayDailySummary(); break;
                case "2": exportCsvReport(); break;
                case "3": exportTextSummary(); break;
                case "4": back = true; break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void displayDailySummary() {
        if (journeys.isEmpty()) { System.out.println("No journeys recorded."); return; }

        BigDecimal totalCharged = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalUncapped = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        Journey mostExpensive = journeys.get(0);
        int peakCount = 0, offPeakCount = 0;
        Map<String, Integer> zonePairCounts = new LinkedHashMap<>();
        Map<Integer, Integer> zoneCounts = new LinkedHashMap<>();

        for (Journey j : journeys) {
            totalCharged = totalCharged.add(j.getChargedFare());
            BigDecimal discountedFare = j.getBaseFare().subtract(j.getDiscountApplied());
            totalUncapped = totalUncapped.add(discountedFare);
            if (j.getChargedFare().compareTo(mostExpensive.getChargedFare()) > 0) mostExpensive = j;
            if (j.getTimeBand() == CityRideDataset.TimeBand.PEAK) peakCount++; else offPeakCount++;
            String zonePair = j.getFromZone() + "->" + j.getToZone();
            zonePairCounts.merge(zonePair, 1, Integer::sum);
            zoneCounts.merge(j.getFromZone(), 1, Integer::sum);
            zoneCounts.merge(j.getToZone(), 1, Integer::sum);
        }

        BigDecimal average = totalCharged.divide(new BigDecimal(journeys.size()), 2, RoundingMode.HALF_UP);
        BigDecimal capSavings = totalUncapped.subtract(totalCharged);

        System.out.println("\n========================================");
        System.out.println("         END-OF-DAY SUMMARY");
        System.out.println("========================================");
        System.out.printf("Total journeys:           %d%n", journeys.size());                      // Req 12a
        System.out.printf("Total cost charged:       £%.2f%n", totalCharged);                      // Req 12b
        System.out.printf("Average cost per journey: £%.2f%n", average);                           // Req 12c
        System.out.printf("Most expensive journey:   ID %d (£%.2f)%n",
                mostExpensive.getId(), mostExpensive.getChargedFare());                             // Req 12d


        System.out.println("\n--- Cap Status ---");
        for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
            BigDecimal running = getRunningTotal(type);
            if (running.compareTo(BigDecimal.ZERO) > 0) {
                System.out.printf("  %s: £%.2f / £%.2f cap %s%n",
                        type, running, CityRideDataset.getDailyCap(type),
                        isCapReached(type) ? "[CAP REACHED]" : "");
            }
        }
        if (capSavings.compareTo(BigDecimal.ZERO) > 0) {
            System.out.printf("Savings from daily cap:   £%.2f%n", capSavings);
        }

        System.out.println("\n--- Category Counts ---");
        System.out.println("Peak journeys:     " + peakCount);
        System.out.println("Off-peak journeys: " + offPeakCount);
        System.out.println("\nJourneys per zone pair:");
        for (Map.Entry<String, Integer> entry : zonePairCounts.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("\nJourneys involving each zone:");
        for (Map.Entry<Integer, Integer> entry : zoneCounts.entrySet()) {
            System.out.println("  Zone " + entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("========================================\n");
    }


    private static void exportCsvReport() {
        if (journeys.isEmpty()) { System.out.println("No journeys to export."); return; }
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String safeName = activeProfile.getName().replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
        String filename = "reports/" + dateStr + "_" + safeName + "_report.csv";

        try {
            File dir = new File("reports");
            if (!dir.exists()) dir.mkdirs();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                writer.write(Journey.csvHeader());
                writer.newLine();
                BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                for (Journey j : journeys) {
                    writer.write(j.toCsvLine());
                    writer.newLine();
                    total = total.add(j.getChargedFare());
                }
                writer.newLine();
                writer.write("TOTAL,,,,,,,,," + total);
                writer.newLine();
            }
            System.out.println("CSV report exported to " + filename);
        } catch (IOException e) {
            System.out.println("Error exporting CSV report: " + e.getMessage());
        }
    }

    private static void exportTextSummary() {
        if (journeys.isEmpty()) { System.out.println("No journeys to export."); return; }
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String safeName = activeProfile.getName().replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
        String filename = "reports/" + dateStr + "_" + safeName + "_summary.txt";

        try {
            File dir = new File("reports");
            if (!dir.exists()) dir.mkdirs();

            StringBuilder sb = new StringBuilder();
            sb.append("CityRide Lite - Daily Summary Report\n");
            sb.append("====================================\n");
            sb.append("Rider: ").append(activeProfile.getName()).append("\n");
            sb.append("Date:  ").append(dateStr).append("\n\n");

            BigDecimal totalCharged = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalUncapped = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            Journey mostExpensive = journeys.get(0);

            sb.append("--- Journey Details ---\n");
            for (Journey j : journeys) {
                sb.append(j.toString()).append("\n");
                totalCharged = totalCharged.add(j.getChargedFare());
                BigDecimal discountedFare = j.getBaseFare().subtract(j.getDiscountApplied());
                totalUncapped = totalUncapped.add(discountedFare);
                if (j.getChargedFare().compareTo(mostExpensive.getChargedFare()) > 0) mostExpensive = j;
            }

            BigDecimal average = totalCharged.divide(new BigDecimal(journeys.size()), 2, RoundingMode.HALF_UP);
            BigDecimal savings = totalUncapped.subtract(totalCharged);

            sb.append("\n--- Summary ---\n");
            sb.append(String.format("Total journeys:           %d%n", journeys.size()));
            sb.append(String.format("Total cost charged:       £%.2f%n", totalCharged));
            sb.append(String.format("Average cost per journey: £%.2f%n", average));
            sb.append(String.format("Most expensive journey:   ID %d (£%.2f)%n",
                    mostExpensive.getId(), mostExpensive.getChargedFare()));
            if (savings.compareTo(BigDecimal.ZERO) > 0) {
                sb.append(String.format("Savings from daily cap:   £%.2f%n", savings));
            }

            sb.append("\n--- Cap Status ---\n");
            for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
                BigDecimal running = getRunningTotal(type);
                if (running.compareTo(BigDecimal.ZERO) > 0) {
                    sb.append(String.format("  %s: £%.2f / £%.2f cap %s%n",
                            type, running, CityRideDataset.getDailyCap(type),
                            isCapReached(type) ? "[CAP REACHED]" : ""));
                }
            }

            RiderProfile.writeFile(new File(filename), sb.toString());
            System.out.println("Text summary exported to " + filename);
        } catch (IOException e) {
            System.out.println("Error exporting text summary: " + e.getMessage());
        }
    }


    private static final String ADMIN_PASSWORD = "admin123";

    private static void adminMenu() {
        System.out.print("Enter admin password: ");
        String password = scanner.nextLine().trim();
        if (!password.equals(ADMIN_PASSWORD)) {
            System.out.println("Incorrect password. Access denied.");    // Req 15
            return;
        }
        System.out.println("Admin access granted.\n");

        boolean exit = false;
        while (!exit) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. View Active Configuration");
            System.out.println("2. Manage Base Fares (add/update/delete)");
            System.out.println("3. Manage Passenger Discounts");
            System.out.println("4. Manage Daily Caps");
            System.out.println("5. Manage Peak Windows");
            System.out.println("6. Save Configuration to File");
            System.out.println("7. Return to Main Menu");
            System.out.print("Choose an option (1-7): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": viewConfig(); break;
                case "2": manageBaseFares(); break;
                case "3": manageDiscounts(); break;
                case "4": manageDailyCaps(); break;
                case "5": managePeakWindows(); break;
                case "6": RiderProfile.saveConfig(); break;
                case "7": exit = true; break;
                default: System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void viewConfig() {
        System.out.println("\n========== ACTIVE CONFIGURATION ==========");

        System.out.println("\n--- Discount Rates ---");
        for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
            System.out.printf("  %s: %.0f%%%n", type, CityRideDataset.getDiscountRate(type).multiply(new BigDecimal("100")));
        }

        System.out.println("\n--- Daily Caps ---");
        for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
            System.out.printf("  %s: £%.2f%n", type, CityRideDataset.getDailyCap(type));
        }

        System.out.println("\n--- Peak Windows ---");
        System.out.printf("  Morning: %s - %s%n", CityRideDataset.getPeakStartTime(), CityRideDataset.getPeakEndTime());
        System.out.printf("  Evening: %s - %s%n", CityRideDataset.getPeakEveningStartTime(), CityRideDataset.getPeakEveningEndTime());

        System.out.println("\n--- Base Fares (Peak) ---");
        printFareTable(CityRideDataset.TimeBand.PEAK);
        System.out.println("\n--- Base Fares (Off-Peak) ---");
        printFareTable(CityRideDataset.TimeBand.OFF_PEAK);
        System.out.println("==========================================\n");
    }

    private static void printFareTable(CityRideDataset.TimeBand band) {
        System.out.print("      ");
        for (int to = CityRideDataset.MIN_ZONE; to <= CityRideDataset.MAX_ZONE; to++) {
            System.out.printf("Zone %-4d", to);
        }
        System.out.println();
        for (int from = CityRideDataset.MIN_ZONE; from <= CityRideDataset.MAX_ZONE; from++) {
            System.out.printf("Zone %d", from);
            for (int to = CityRideDataset.MIN_ZONE; to <= CityRideDataset.MAX_ZONE; to++) {
                System.out.printf("  £%-6.2f", CityRideDataset.getBaseFare(from, to, band));
            }
            System.out.println();
        }
    }

    private static void manageBaseFares() {
        System.out.println("\n--- Manage Base Fares ---");
        System.out.println("1. Update a fare");
        System.out.println("2. View current fares");
        System.out.print("Choose (1-2): ");
        String choice = scanner.nextLine().trim();

        if (choice.equals("2")) {
            printFareTable(CityRideDataset.TimeBand.PEAK);
            printFareTable(CityRideDataset.TimeBand.OFF_PEAK);
            return;
        }
        if (!choice.equals("1")) { System.out.println("Invalid choice."); return; }

        System.out.print("From zone (1-5, e.g. 1): ");
        int fromZone = readAdminZone();
        if (fromZone < 0) return;
        System.out.print("To zone (1-5, e.g. 3): ");
        int toZone = readAdminZone();
        if (toZone < 0) return;

        System.out.print("Time band (Peak/Off-Peak): ");
        CityRideDataset.TimeBand band = readAdminTimeBand();
        if (band == null) return;

        System.out.printf("Current fare for %d->%d (%s): £%.2f%n",
                fromZone, toZone, band, CityRideDataset.getBaseFare(fromZone, toZone, band));

        System.out.print("Enter new fare (e.g. 3.50), or 'delete' to remove: ");
        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase("delete")) {
            CityRideDataset.getBaseFares().remove(CityRideDataset.key(fromZone, toZone, band));
            System.out.println("Fare deleted.");
            return;
        }

        BigDecimal newFare = readMoney(input);
        if (newFare == null || newFare.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("Validation error: Fare must be a non-negative number (e.g. 3.50). Not saved."); // Req 18, 19
            return;
        }
        CityRideDataset.setBaseFare(fromZone, toZone, band, newFare);
        System.out.printf("Fare updated: %d->%d (%s) = £%.2f%n", fromZone, toZone, band, newFare);
    }

    private static void manageDiscounts() {
        System.out.println("\n--- Manage Passenger Discounts ---");
        for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
            System.out.printf("  %s: %.0f%%%n", type, CityRideDataset.getDiscountRate(type).multiply(new BigDecimal("100")));
        }

        System.out.print("Enter passenger type (Adult, Student, Child, Senior Citizen): ");
        String typeStr = scanner.nextLine().trim().toUpperCase().replace(" ", "_");
        CityRideDataset.PassengerType type;
        try { type = CityRideDataset.PassengerType.valueOf(typeStr); }
        catch (IllegalArgumentException e) {
            System.out.println("Validation error: Unknown passenger type. Not saved.");
            return;
        }

        System.out.printf("Current discount for %s: %.0f%%%n", type,
                CityRideDataset.getDiscountRate(type).multiply(new BigDecimal("100")));
        System.out.print("Enter new discount as decimal (e.g. 0.25 for 25%): ");
        BigDecimal rate = readMoney(scanner.nextLine().trim());

        if (rate == null || rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            System.out.println("Validation error: Discount must be between 0.00 and 1.00 (e.g. 0.25). Not saved.");
            return;
        }
        CityRideDataset.setDiscountRate(type, rate);
        System.out.printf("Discount updated: %s = %.0f%%%n", type, rate.multiply(new BigDecimal("100")));
    }

    private static void manageDailyCaps() {
        System.out.println("\n--- Manage Daily Caps ---");
        for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
            System.out.printf("  %s: £%.2f%n", type, CityRideDataset.getDailyCap(type));
        }

        System.out.print("Enter passenger type (Adult, Student, Child, Senior Citizen): ");
        String typeStr = scanner.nextLine().trim().toUpperCase().replace(" ", "_");
        CityRideDataset.PassengerType type;
        try { type = CityRideDataset.PassengerType.valueOf(typeStr); }
        catch (IllegalArgumentException e) {
            System.out.println("Validation error: Unknown passenger type. Not saved.");
            return;
        }

        System.out.printf("Current daily cap for %s: £%.2f%n", type, CityRideDataset.getDailyCap(type));
        System.out.print("Enter new daily cap (e.g. 8.00): ");
        BigDecimal cap = readMoney(scanner.nextLine().trim());

        if (cap == null || cap.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Validation error: Daily cap must be a positive number (e.g. 8.00). Not saved.");
            return;
        }
        CityRideDataset.setDailyCap(type, cap);
        System.out.printf("Daily cap updated: %s = £%.2f%n", type, cap);
    }

    private static void managePeakWindows() {
        System.out.println("\n--- Manage Peak Windows ---");
        System.out.printf("Current morning peak: %s - %s%n",
                CityRideDataset.getPeakStartTime(), CityRideDataset.getPeakEndTime());
        System.out.printf("Current evening peak: %s - %s%n",
                CityRideDataset.getPeakEveningStartTime(), CityRideDataset.getPeakEveningEndTime());

        System.out.print("Morning peak start (HH:mm, e.g. 07:00): ");
        String ms = scanner.nextLine().trim();
        System.out.print("Morning peak end (HH:mm, e.g. 09:30): ");
        String me = scanner.nextLine().trim();
        System.out.print("Evening peak start (HH:mm, e.g. 16:30): ");
        String es = scanner.nextLine().trim();
        System.out.print("Evening peak end (HH:mm, e.g. 19:00): ");
        String ee = scanner.nextLine().trim();

        if (!isValidTime(ms) || !isValidTime(me) || !isValidTime(es) || !isValidTime(ee)) {
            System.out.println("Validation error: All times must be in HH:mm format (e.g. 07:00). Not saved.");
            return;
        }
        if (ms.compareTo(me) >= 0 || es.compareTo(ee) >= 0) {
            System.out.println("Validation error: Start time must be before end time. Not saved.");
            return;
        }
        CityRideDataset.setPeakWindows(ms, me, es, ee);
        System.out.println("Peak windows updated successfully.");
    }

    // Admin input helpers
    private static int readAdminZone() {
        String input = scanner.nextLine().trim();
        try {
            int zone = Integer.parseInt(input);
            if (zone >= CityRideDataset.MIN_ZONE && zone <= CityRideDataset.MAX_ZONE) return zone;
            System.out.println("Validation error: Zone must be between " + CityRideDataset.MIN_ZONE +
                    " and " + CityRideDataset.MAX_ZONE + ".");
        } catch (NumberFormatException e) {
            System.out.println("Validation error: Non-numeric input. Please enter a number (1-5).");
        }
        return -1;
    }

    private static CityRideDataset.TimeBand readAdminTimeBand() {
        String input = scanner.nextLine().trim().toLowerCase().replace("-", "_");
        if (input.equals("peak")) return CityRideDataset.TimeBand.PEAK;
        if (input.equals("off_peak")) return CityRideDataset.TimeBand.OFF_PEAK;
        System.out.println("Validation error: Enter 'Peak' or 'Off-Peak'.");
        return null;
    }

    private static boolean handleExit() {
        if (!journeys.isEmpty()) {
            if (confirm("Save current day state (profile + journeys) before exiting?")) {
                RiderProfile.saveDayState(activeProfile, journeys);
            }
        }
        if (confirm("Are you sure you want to exit?")) {
            System.out.println("\nThank you for using CityRide Lite. Goodbye!");
            return true;
        }
        return false;
    }
}
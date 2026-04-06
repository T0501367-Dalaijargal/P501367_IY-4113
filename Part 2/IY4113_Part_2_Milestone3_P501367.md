# IY4113 Part 2 Milestone 3

| Assessment Details | Please Complete All Details                                             |
| ------------------ | ----------------------------------------------------------------------- |
| Group              | B                                                                       |
| Module Title       | IY4113 Applied Software Engineering using Object-Orientated Programming |
| Assessment Type    | Part 2 - Milestone 2                                                    |
| Module Tutor Name  | Jonathan Shore                                                          |
| Student ID Number  | P501367                                                                 |
| Date of Submission | 06/04/2026                                                              |
| Word Count         | 5019                                                                    |
| GitHub Link        | https://github.com/T0501367-Dalaijargal/P501367_IY-4113                 |

- [x] *I confirm that this assignment is my own work. Where I have referred to academic sources, I have provided in-text citations and included the sources in
  the final reference list.*

- [ ] *Where I have used AI, I have cited and referenced appropriately.*

------------------------------------------------------------------------------------------------------------------------------

### Research (minimum of 2, at least 3)

---

Conduct research to support your coding process, including use of code examples, tutorials, documentation and AI tools (if used).

Use the structure below to capture your evidence:

------------------------------------------------------------------------------------------------------------------------------

**Title of research:** Java BigDecimal for Financial Calculations

**Reference (link):** https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/math/BigDecimal.html

**How does the research help with coding practice?:**
The official Oracle documentation for BigDecimal was essential for implementing accurate fare calculations for CityRide Lite. Due to the errors that can occur in representing floating point numbers with the double and float data types, such as 0.1 + 0.2 != 0.3, it is unacceptable for ride sharing applications to accept such errors in calculations of fares and caps. The documentation made clear the importance of using the String constructor for the BigDecimal class rather than the double constructor. The ROUND_HALF_UP mode was selected for the rounding mode for rideshare applications as this represents the mathematical convention for monetary values. The methods call setScale(2, RoundingMode.HALF_UP) ensures that the monetary values are stored with only two decimal places.

**Key coding ideas you could reuse in your program:**

- Always construct `BigDecimal` from `String` literals for exact representation: `new BigDecimal("3.50")`
- Use `setScale(2, RoundingMode.HALF_UP)` consistently for all monetary results
- Use `compareTo()` instead of `equals()` for numeric comparison (since `2.0` and `2.00` are not `equals()` but are `compareTo() == 0`)
- Chain arithmetic: `baseFare.multiply(discountRate).setScale(2, RoundingMode.HALF_UP)` to calculate discounts in a single expression
- The `subtract()` and `add()` methods return new `BigDecimal` objects (immutability), so results must be assigned

**Screenshot of research:**

![](C:\Users\User\AppData\Roaming\marktext\images\2026-04-06-15-07-06-image.png)

------------------------------------------------------------------------------------------------------------------------------

**Title of research:** Java File I/O — Reading and Writing Text Files

**Reference (link):** https://docs.oracle.com/javase/tutorial/essential/io/file.html

**How does the research help with coding practice?:**
The Oracle I/O tutorial guided me through the implementation of all of the file persistence features of CityRide Lite. I used the tutorial to learn how to use the try-with-resources construct to automatically ensure that file handles are closed even if an exception occurs while using the file. Furthermore, the tutorial explained the difference between classes like FileReader and FileWriter versus classes like FileInputStream and FileOutputStream, which are used for reading from and writing to files of bytes. I used the advice from the tutorial to implement BufferedReader and BufferedWriter classes in my code to improve the performance of file I/O operations. Finally, the tutorial also discussed how to catch and handle exceptions that can occur during file I/O operations, which will allow CityRide Lite to gracefully fall back to default values in the event that the configuration file is missing.

**Key coding ideas you could reuse in your program:**

- Try-with-resources for automatic resource cleanup: `try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) { ... }`
- Using `File.exists()` to check for file presence before attempting to read, enabling fallback behaviour
- Creating parent directories with `dir.mkdirs()` before writing files to avoid `FileNotFoundException`
- Reading line-by-line with `reader.readLine()` in a while loop, with null check for end-of-file
- Catching `IOException` specifically and providing user-friendly error messages rather than stack traces

**Screenshot of research:**

![](C:\Users\User\AppData\Roaming\marktext\images\2026-04-06-15-09-10-image.png)

------------------------------------------------------------------------------------------------------------------------------

**Title of research:** Parsing and Writing CSV Files in Java Without External Libraries

**Reference (link):** https://www.baeldung.com/java-csv-file-array

**How does the research help with coding practice?:**
This Baeldung tutorial showed techniques for reading and writing CSV files without external libraries - functionality applicable to Requirements 8 and 13. The tutorial used the built-in `split()` and `format()` methods of the `String` class in order to split the lines of a CSV file and write lines to a CSV file. While there are external libraries for reading and writing CSV files, such as OpenCSV, the Baeldung tutorial’s approach of using only Java’s standard libraries is appropriate for this project. The simple structure of the CSV files for the journey and report data indicates that the standard library classes will suffice for processing the data. Additionally, factory methods could be used to create the journey objects from a line of a CSV file, such as the `fromCsvLine()` factory method defined in the tutorial. Furthermore, the header line of a CSV file should be skipped when reading the file with the `readLine()` method. The data read from the CSV file should also be validated before creating the objects from that data.

**Key coding ideas you could reuse in your program:**

- `String.split(",")` for simple CSV parsing with a known structure
- `String.format("%d,%s,%s,%.2f", ...)` for consistent CSV output formatting
- Skip-header pattern: call `readLine()` once before the parsing loop
- Factory method pattern: `Journey.fromCsvLine(line)` returns `null` on parse failure, allowing the caller to skip invalid rows gracefully
- Separating CSV header definition (`csvHeader()`) from row generation (`toCsvLine()`) for maintainability

**Screenshot of research:**

![](C:\Users\User\AppData\Roaming\marktext\images\2026-04-06-15-11-02-image.png)

------------------------------------------------------------------------------------------------------------------------------

### Program Code

---

The current program code is split across 4 Java files. All code compiles and runs successfully on Java 21. The files are:

1. **CityRideDataset.java** — Configuration data (fares, discounts, caps, peak windows) with mutable state for admin updates
2. **Journey.java** — Journey data model with CSV serialisation
3. **RiderProfile.java** — Rider profile model plus all file I/O (config, profiles, state persistence)
4. **Main.java** — All menus (main, journey, profile, calculate, reports, admin) and business logic

------------------------------------------------------------------------------------------------------------------------------

#### File 1: CityRideDataset.java

```java
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

    //Accessors
    public static Map<PassengerType, BigDecimal> getDiscountRates() { return discountRate; }
    public static Map<PassengerType, BigDecimal> getDailyCaps() { return dailyCap; }
    public static Map<String, BigDecimal> getBaseFares() { return baseFare; }

    public static BigDecimal getDiscountRate(PassengerType type) { return discountRate.get(type); }
    public static BigDecimal getDailyCap(PassengerType type) { return dailyCap.get(type); }

    public static String getPeakStartTime() { return peakStartTime; }
    public static String getPeakEndTime() { return peakEndTime; }
    public static String getPeakEveningStartTime() { return peakEveningStartTime; }
    public static String getPeakEveningEndTime() { return peakEveningEndTime; }


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

    public static BigDecimal getBaseFare(int fromZone, int toZone, TimeBand timeBand) {
        BigDecimal fare = baseFare.get(key(fromZone, toZone, timeBand));
        if (fare == null) {
            System.out.println("Warning: No fare found for " + fromZone + "->" + toZone
                + " " + timeBand + ". Using £0.00.");
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

    /* Determine time band from an HH:mm string based on current peak windows. */
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
```

------------------------------------------------------------------------------------------------------------------------------

#### File 2: Journey.java

```java
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
                   int zonesCrossed, BigDecimal baseFare, BigDecimal discountApplied,
                   BigDecimal chargedFare) {
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
    public void setPassengerType(CityRideDataset.PassengerType pt) { this.passengerType = pt; }
    public void setZonesCrossed(int zonesCrossed) { this.zonesCrossed = zonesCrossed; }
    public void setBaseFare(BigDecimal baseFare) { this.baseFare = baseFare; }
    public void setDiscountApplied(BigDecimal da) { this.discountApplied = da; }
    public void setChargedFare(BigDecimal chargedFare) { this.chargedFare = chargedFare; }

    /* CSV header for export. */
    public static String csvHeader() {
        return "ID,Date,Time,FromZone,ToZone,TimeBand,PassengerType,ZonesCrossed,"
             + "BaseFare,DiscountApplied,ChargedFare";
    }

    /* Convert to CSV line. */
    public String toCsvLine() {
        return String.format("%d,%s,%s,%d,%d,%s,%s,%d,%.2f,%.2f,%.2f",
                id, date, time, fromZone, toZone, timeBand.name(), passengerType.name(),
                zonesCrossed, baseFare, discountApplied, chargedFare);
    }

    /* Parse from a CSV line. Returns null if parsing fails. */
    public static Journey fromCsvLine(String line) {
        try {
            String[] parts = line.split(",");
            if (parts.length < 11) return null;
            int id = Integer.parseInt(parts[0].trim());
            String date = parts[1].trim();
            String time = parts[2].trim();
            int fromZone = Integer.parseInt(parts[3].trim());
            int toZone = Integer.parseInt(parts[4].trim());
            CityRideDataset.TimeBand timeBand =
                CityRideDataset.TimeBand.valueOf(parts[5].trim());
            CityRideDataset.PassengerType pType =
                CityRideDataset.PassengerType.valueOf(parts[6].trim());
            int zonesCrossed = Integer.parseInt(parts[7].trim());
            BigDecimal baseFare = new BigDecimal(parts[8].trim())
                .setScale(2, RoundingMode.HALF_UP);
            BigDecimal discountApplied = new BigDecimal(parts[9].trim())
                .setScale(2, RoundingMode.HALF_UP);
            BigDecimal chargedFare = new BigDecimal(parts[10].trim())
                .setScale(2, RoundingMode.HALF_UP);
            return new Journey(id, date, time, fromZone, toZone, timeBand, pType,
                zonesCrossed, baseFare, discountApplied, chargedFare);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Date: %s %s | Zones: %d->%d (%d crossed) "
                + "| Band: %s | Type: %s | Base: £%.2f | Disc: £%.2f | Charged: £%.2f",
                id, date, time, fromZone, toZone, zonesCrossed, timeBand, passengerType,
                baseFare, discountApplied, chargedFare);
    }
}
```

------------------------------------------------------------------------------------------------------------------------------

#### File 3: RiderProfile.java

```java
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


public class RiderProfile {


    private String name;
    private CityRideDataset.PassengerType passengerType;
    private String defaultPayment;

    public RiderProfile() {
        this.name = "Unknown";
        this.passengerType = CityRideDataset.PassengerType.ADULT;
        this.defaultPayment = "Contactless";
    }

    public RiderProfile(String name, CityRideDataset.PassengerType passengerType,
                        String defaultPayment) {
        this.name = name;
        this.passengerType = passengerType;
        this.defaultPayment = defaultPayment;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public CityRideDataset.PassengerType getPassengerType() { return passengerType; }
    public void setPassengerType(CityRideDataset.PassengerType pt) { this.passengerType = pt; }
    public String getDefaultPayment() { return defaultPayment; }
    public void setDefaultPayment(String dp) { this.defaultPayment = dp; }

    /* Serialize to a simple JSON string. */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"name\": \"").append(escapeJson(name)).append("\",\n");
        sb.append("  \"passengerType\": \"").append(passengerType.name()).append("\",\n");
        sb.append("  \"defaultPayment\": \"").append(escapeJson(defaultPayment)).append("\"\n");
        sb.append("}");
        return sb.toString();
    }

    /* Deserialize from a simple JSON string. */
    public static RiderProfile fromJson(String json) {
        RiderProfile profile = new RiderProfile();
        String nameVal = extractJsonString(json, "name");
        if (nameVal != null) profile.setName(nameVal);
        String typeVal = extractJsonString(json, "passengerType");
        if (typeVal != null) {
            try { profile.setPassengerType(
                CityRideDataset.PassengerType.valueOf(typeVal)); }
            catch (IllegalArgumentException ignored) {}
        }
        String payVal = extractJsonString(json, "defaultPayment");
        if (payVal != null) profile.setDefaultPayment(payVal);
        return profile;
    }

    @Override
    public String toString() {
        return String.format("Name: %s | Type: %s | Payment: %s",
            name, passengerType, defaultPayment);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static String extractJsonString(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int colonIdx = json.indexOf(':', idx + search.length());
        if (colonIdx < 0) return null;
        int startQuote = json.indexOf('"', colonIdx + 1);
        if (startQuote < 0) return null;
        int endQuote = json.indexOf('"', startQuote + 1);
        if (endQuote < 0) return null;
        return json.substring(startQuote + 1, endQuote);
    }

    private static String extractBlock(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int braceStart = json.indexOf('{', idx);
        if (braceStart < 0) return null;
        int depth = 1;
        int pos = braceStart + 1;
        while (pos < json.length() && depth > 0) {
            char c = json.charAt(pos);
            if (c == '{') depth++;
            else if (c == '}') depth--;
            pos++;
        }
        return json.substring(braceStart, pos);
    }


    public static String readFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    public static void writeFile(File file, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        }
    }

    private static String sanitiseFilename(String name) {
        return name.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
    }


    private static final String PROFILES_DIR = "data/profiles/";

    public void saveToFile() {
        try {
            File dir = new File(PROFILES_DIR);
            if (!dir.exists()) dir.mkdirs();
            String filename = PROFILES_DIR + sanitiseFilename(name) + ".json";
            writeFile(new File(filename), toJson());
            System.out.println("Profile saved to " + filename);
        } catch (IOException e) {
            System.out.println("Error saving profile: " + e.getMessage());
        }
    }

    public static RiderProfile loadFromFile(String name) {
        String filename = PROFILES_DIR + sanitiseFilename(name) + ".json";
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Profile not found: " + filename);
            return null;
        }
        try {
            String json = readFile(file);
            RiderProfile profile = fromJson(json);
            System.out.println("Profile loaded: " + profile);
            return profile;
        } catch (IOException e) {
            System.out.println("Error loading profile: " + e.getMessage());
            return null;
        }
    }

    public static void listProfiles() {
        File dir = new File(PROFILES_DIR);
        if (!dir.exists() || dir.listFiles() == null) {
            System.out.println("No saved profiles found.");
            return;
        }
        File[] files = dir.listFiles((d, n) -> n.endsWith(".json"));
        if (files == null || files.length == 0) {
            System.out.println("No saved profiles found.");
            return;
        }
        System.out.println("\n--- Saved Profiles ---");
        for (File f : files) {
            System.out.println("  - " + f.getName().replace(".json", ""));
        }
    }


    private static final String CONFIG_FILE = "data/config.json";

    public static void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            System.out.println("Config file not found. Loading safe default values.");
            CityRideDataset.loadDefaults();
            return;
        }
        try {
            String json = readFile(file);
            parseConfig(json);
            System.out.println("Configuration loaded from " + CONFIG_FILE);
        } catch (Exception e) {
            System.out.println("Error reading config file: " + e.getMessage());
            System.out.println("Loading safe default values.");
            CityRideDataset.loadDefaults();
        }
    }

    public static void saveConfig() {
        try {
            File dir = new File("data");
            if (!dir.exists()) dir.mkdirs();
            writeFile(new File(CONFIG_FILE), buildConfigJson());
            System.out.println("Configuration saved to " + CONFIG_FILE);
        } catch (IOException e) {
            System.out.println("Error saving config: " + e.getMessage());
        }
    }

    private static String buildConfigJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        // Discount rates
        sb.append("  \"discountRates\": {\n");
        Map<CityRideDataset.PassengerType, BigDecimal> dr =
            CityRideDataset.getDiscountRates();
        int i = 0;
        for (CityRideDataset.PassengerType type :
                CityRideDataset.PassengerType.values()) {
            sb.append("    \"").append(type.name()).append("\": \"")
              .append(dr.get(type)).append("\"");
            if (++i < dr.size()) sb.append(",");
            sb.append("\n");
        }
        sb.append("  },\n");
        // Daily caps
        sb.append("  \"dailyCaps\": {\n");
        Map<CityRideDataset.PassengerType, BigDecimal> dc =
            CityRideDataset.getDailyCaps();
        i = 0;
        for (CityRideDataset.PassengerType type :
                CityRideDataset.PassengerType.values()) {
            sb.append("    \"").append(type.name()).append("\": \"")
              .append(dc.get(type)).append("\"");
            if (++i < dc.size()) sb.append(",");
            sb.append("\n");
        }
        sb.append("  },\n");
        // Peak windows
        sb.append("  \"peakMorningStart\": \"")
          .append(CityRideDataset.getPeakStartTime()).append("\",\n");
        sb.append("  \"peakMorningEnd\": \"")
          .append(CityRideDataset.getPeakEndTime()).append("\",\n");
        sb.append("  \"peakEveningStart\": \"")
          .append(CityRideDataset.getPeakEveningStartTime()).append("\",\n");
        sb.append("  \"peakEveningEnd\": \"")
          .append(CityRideDataset.getPeakEveningEndTime()).append("\",\n");
        // Base fares
        sb.append("  \"baseFares\": {\n");
        Map<String, BigDecimal> bf = CityRideDataset.getBaseFares();
        i = 0;
        for (Map.Entry<String, BigDecimal> entry : bf.entrySet()) {
            sb.append("    \"").append(entry.getKey()).append("\": \"")
              .append(entry.getValue()).append("\"");
            if (++i < bf.size()) sb.append(",");
            sb.append("\n");
        }
        sb.append("  }\n");
        sb.append("}");
        return sb.toString();
    }

    private static void parseConfig(String json) {
        String discountBlock = extractBlock(json, "discountRates");
        if (discountBlock != null) {
            for (CityRideDataset.PassengerType type :
                    CityRideDataset.PassengerType.values()) {
                String val = extractJsonString(discountBlock, type.name());
                if (val != null) {
                    CityRideDataset.setDiscountRate(type,
                        new BigDecimal(val).setScale(2, RoundingMode.HALF_UP));
                }
            }
        }
        String capBlock = extractBlock(json, "dailyCaps");
        if (capBlock != null) {
            for (CityRideDataset.PassengerType type :
                    CityRideDataset.PassengerType.values()) {
                String val = extractJsonString(capBlock, type.name());
                if (val != null) {
                    CityRideDataset.setDailyCap(type,
                        new BigDecimal(val).setScale(2, RoundingMode.HALF_UP));
                }
            }
        }
        String pms = extractJsonString(json, "peakMorningStart");
        String pme = extractJsonString(json, "peakMorningEnd");
        String pes = extractJsonString(json, "peakEveningStart");
        String pee = extractJsonString(json, "peakEveningEnd");
        if (pms != null && pme != null && pes != null && pee != null) {
            CityRideDataset.setPeakWindows(pms, pme, pes, pee);
        }
        String fareBlock = extractBlock(json, "baseFares");
        if (fareBlock != null) {
            for (int from = CityRideDataset.MIN_ZONE;
                    from <= CityRideDataset.MAX_ZONE; from++) {
                for (int to = CityRideDataset.MIN_ZONE;
                        to <= CityRideDataset.MAX_ZONE; to++) {
                    for (CityRideDataset.TimeBand band :
                            CityRideDataset.TimeBand.values()) {
                        String k = CityRideDataset.key(from, to, band);
                        String val = extractJsonString(fareBlock, k);
                        if (val != null) {
                            CityRideDataset.setBaseFare(from, to, band,
                                new BigDecimal(val)
                                    .setScale(2, RoundingMode.HALF_UP));
                        }
                    }
                }
            }
        }
    }


    private static final String STATE_DIR = "data/state/";

    public static void saveDayState(RiderProfile profile,
                                     List<Journey> journeys) {
        try {
            File dir = new File(STATE_DIR);
            if (!dir.exists()) dir.mkdirs();
            writeFile(new File(STATE_DIR + "last_profile.json"),
                profile.toJson());
            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(STATE_DIR + "last_journeys.csv"))) {
                writer.write(Journey.csvHeader());
                writer.newLine();
                for (Journey j : journeys) {
                    writer.write(j.toCsvLine());
                    writer.newLine();
                }
            }
            System.out.println("Day state saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving day state: " + e.getMessage());
        }
    }

    public static RiderProfile loadSavedProfile() {
        File file = new File(STATE_DIR + "last_profile.json");
        if (!file.exists()) return null;
        try {
            return fromJson(readFile(file));
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean hasSavedState() {
        return new File(STATE_DIR + "last_profile.json").exists();
    }

    public static String getSavedJourneysPath() {
        return STATE_DIR + "last_journeys.csv";
    }
}
```

------------------------------------------------------------------------------------------------------------------------------

#### File 4: Main.java

Due to the length of Main.java (1,083 lines), only the key sections are shown below. The full file is available in the GitHub repository.

```java
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static RiderProfile activeProfile = new RiderProfile();

    // Journey management
    private static final List<Journey> journeys = new ArrayList<>();
    private static final Map<CityRideDataset.PassengerType, BigDecimal>
        runningTotals = new HashMap<>();
    private static int nextId = 1;

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
                    System.out.println("Resumed session for "
                        + activeProfile.getName()
                        + " with " + count + " journeys.");
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
                default:
                    System.out.println(
                        "Invalid choice. Please enter a number from 1 to 6.");
            }
        }
    }
    //remaining methods follow the same pattern
}
```

**Fare calculation with daily cap:**

```java
    / Add a journey with fare calculation. */
    private static Journey doAddJourney(String date, String time,
            int fromZone, int toZone,
            CityRideDataset.PassengerType passengerType) {
        CityRideDataset.TimeBand timeBand =
            CityRideDataset.determineTimeBand(time);
        int zonesCrossed = Math.abs(toZone - fromZone) + 1;
        BigDecimal baseFare =
            CityRideDataset.getBaseFare(fromZone, toZone, timeBand);
        BigDecimal discountRate =
            CityRideDataset.getDiscountRate(passengerType);
        BigDecimal discountApplied = baseFare.multiply(discountRate)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountedFare = baseFare.subtract(discountApplied);
        BigDecimal chargedFare = applyDailyCap(passengerType, discountedFare);

        Journey journey = new Journey(nextId++, date, time, fromZone, toZone,
            timeBand, passengerType, zonesCrossed, baseFare,
            discountApplied, chargedFare);
        journeys.add(journey);
        runningTotals.put(passengerType,
            runningTotals.get(passengerType).add(chargedFare));
        return journey;
    }

    /* Apply daily cap logic. */
    private static BigDecimal applyDailyCap(
            CityRideDataset.PassengerType type, BigDecimal discountedFare) {
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
```

**Admin menu with validation:**

```java
    private static final String ADMIN_PASSWORD = "admin123";

    private static void adminMenu() {
        System.out.print("Enter admin password: ");
        String password = scanner.nextLine().trim();
        if (!password.equals(ADMIN_PASSWORD)) {
            System.out.println("Incorrect password. Access denied.");
            return;
        }
        System.out.println("Admin access granted.\n");
        //submenu for fares, discounts, caps, peak windows
    }

    private static void manageDiscounts() {
        // Display current values, prompt for passenger type and new rate
        // Validation: rate must be between 0.00 and 1.00
        if (rate == null || rate.compareTo(BigDecimal.ZERO) < 0
                || rate.compareTo(BigDecimal.ONE) > 0) {
            System.out.println("Validation error: Discount must be between "
                + "0.00 and 1.00 (e.g. 0.25). Not saved.");
            return;
        }
        CityRideDataset.setDiscountRate(type, rate);
    }
```

**The full Main.java** contains all remaining methods including: `editJourney()`, `deleteJourney()`, `filterJourneys()`, `displayDailySummary()`, `exportCsvReport()`, `exportTextSummary()`, `manageDailyCaps()`, `managePeakWindows()`, `manageBaseFares()`, and `handleExit()`. The complete source code is available in the GitHub.

------------------------------------------------------------------------------------------------------------------------------

### Updated Gantt Chart

------------------------------------------------------------------------------------------------------------------------------

![](C:\Users\User\Downloads\Part%202%20(6).jpg)

------------------------------------------------------------------------------------------------------------------------------

### Diary Entries (at least 4)

------------------------------------------------------------------------------------------------------------------------------

### Diary Entry 1 (30/03/2026) — Refactoring from Part 1 to Part 2 Architecture

Today I began restructuring the code for Part 1 in order to meet the requirements for Part 2. The original code for Part 1 existed in a single Main.java file that contained the Journey class. Part 2 introduces 22 new requirements that will make this structure untenable; thus, I will need to restructure the code.

There are four files in the project: CityRideDataset.java, Journey.java, RiderProfile.java, and Main.java. I made the CityRideDataset mutable, as its data was immutable in the original code (due to the use of immutable maps in Java). However, one of the requirements for Part 2 is that an admin is able to modify the fares for the city’s rides. Thus, I changed the static maps to regular HashMap objects within the class.

I created 11 separate files for the original structure of the program; however, after considering the requirements of the program, I determined that four files is a more appropriate number. I grouped the file I/O functionality into the RiderProfile class, as well as grouped the menus and business logic into the Main class; thus, I believe the code is most easily navigated with this structure.

### Diary Entry 2  (02/04/2026)— Implementing Journey Time and Automatic Peak Detection

For Part 1, the user has to manually select whether each journey is to be made during Peak or Off-Peak hours. For Part 2, a time field has been added to each journey, and a determineTimeBand() method has been implemented within the CityRideDataset class that will automatically determine if a journey is to be made during Peak or Off-Peak hours. The time field will contain the time of the journey that the user enters, and the method will compare this time with the peak hours that have been configured for the system (defaulting to morning peak hours of 07:00 to 09:30, and evening peak hours of 16:30 to 19:00).

The method uses the compareTo() method of the String class to compare the time of the journey to the peak hours. The time values are of the HH:mm format, and such strings will sort in the same order as the times that they represent. Thus, the implementation of this method is relatively simple; it would be more complex to utilize Java’s LocalTime class for this example project.

One of the challenges in implementing this functionality was ensuring that the peak hours could be edited by the admin. Therefore, the four values that represent the start and end times of each peak hour are stored as static strings within the CityRideDataset class, with getter and setter methods. Additionally, when editing these times within the admin menu, a validation routine ensures that each time is of the proper HH:mm format, and that start times are prior to end times for each peak hour.

### Diary Entry 3 (04/04/2026) — JSON Persistence Without External Libraries

One of the more challenging aspects of the project was the implementation of JSON reading and writing for the config and profile files (Requirements 2, 5, and 14) without using external libraries such as Gson or Jackson. Since the specifications for the project indicate that only the Java standard library should be used, a minimal implementation of a JSON serialiser and parser was written.

The serialisation of Java objects to JSON format makes use of the StringBuilder object to construct the JSON string, including appropriately escaping any characters in the JSON string. The parsing of JSON strings is accomplished with two helper methods: one that extracts a JSON string value given a specific key (extractJsonString), and another that finds a specific key and value within a JSON object (extractBlock). These two methods are not general purpose JSON parsers - they are specifically designed to work with the JSON data that will be generated by this program. Additionally, they are relatively simple parsers; they assume that all data within the JSON strings is of the string type.

The potential problem with the extractJsonString method is that it can potentially fail if the JSON string contains nested objects; it will find the wrong colon or quote character. This problem was solved by having the extractBlock method find the specific sub-object first that is to be parsed (such as the discountRates sub-object), and then applying the extractJsonString method to that newly found object. Thus, no general purpose JSON parser was necessary for this project.

Another potential solution to this problem was to utilize the built-in javax.json library for Java. However, this library is not included in the standard Java distribution, though it is available through the Jakarta JSON Processing API. Thus, while this library would include more robust JSON parsing functionality, it would also increase the dependencies of the project.

### Diary Entry 4 (05/04/2026) — Daily Cap Recalculation After Edits and Deletes

The logic for the cap was the trickiest rule to implement (Requirement 6a). The cap works by keeping a running total of all the journeys for each passenger type. Once that total reaches the cap amount, any additional journeys for that passenger type are free. Thus, the order of the journeys for each passenger type matters - if an edited journey is deleted, the journeys that follow it may need to be charged.

To deal with this, the journey totals are recalculated. The `recalculateTotals()` method empties the journeys list, copies the journeys into a local list, empties the journeys list again, and then re-inserts each journey into the list. Each journey is reprocessed to calculate the charged fare for that journey. This is an O(n) operation, where n is the number of journeys in a day - a relatively small number.

One bug in the software was revealed through testing: journey IDs were changing after editing journeys. Rather than resetting the journey ID to 1 after editing journeys, the `nextId` counter should continue from the highest journey ID in the list: `nextId = journeys.isEmpty() ? 1 : journeys.get(journeys.size() - 1).getId() + 1`. The edit function locates the journey by its journey ID, and then recalculating the journey totals will preserve the journey ordering.

### Diary Entry 5 (06/04/2026) — Input Validation and User Experience (Requirements 18, 19, 22)

The session was focused entirely on implementing input validation for the program’s prompts. Requirement 22 states that all prompts must include the format and example values for the expected input, and Requirements 18-19 require that all input be validated such that invalid entries lead to an error message being displayed and no data being saved from that invalid entry.

There are several helper methods that are defined within the Main.java file that perform these validations: readZone, readDate, readTime, readPassengerType, readInt, readNonEmpty, and confirm. Each of these methods utilizes a while-true loop that continues to prompt the user for input until valid data is entered. For instance, the readDate method only accepts inputs that follow the DD/MM/YYYY pattern, with the regex pattern \d{2}/\d{2}/\d{4}, while the readTime method accepts times in the HH:mm format, with additional validation that the hour is between 0 and 23, and the minutes between 0 and 59.

For the admin menu, I have chosen instead to utilize return statements within each of the admin menu methods that recognize invalid entries and return to the admin menu with an error message. This is due to the fact that any changes made by the admin affect the rest of the program, so failing fast and recognizing invalid entries is better than silently failing later on. For instance, if the admin menu is provided a discount rate of 1.5, which would result in a 150% discount on flights, the system will display “Validation error: Discount must be between 0.00 and 1.00 (e.g. 0.25). Not saved.” and return to the admin menu.

Another design decision was to include the current value within square brackets next to each field within the admin menu. For instance, the date field will display Date [05/04/2026]: which allows the admin to simply press enter to leave that value intact. This is a convention within Unix operating systems and command line tools, and allows for more rapid editing of a field if only one or two fields are to be edited.

------------------------------------------------------------------------------------------------------------------------------

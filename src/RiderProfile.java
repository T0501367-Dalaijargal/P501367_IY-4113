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

    public RiderProfile(String name, CityRideDataset.PassengerType passengerType, String defaultPayment) {
        this.name = name;
        this.passengerType = passengerType;
        this.defaultPayment = defaultPayment;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public CityRideDataset.PassengerType getPassengerType() { return passengerType; }
    public void setPassengerType(CityRideDataset.PassengerType passengerType) { this.passengerType = passengerType; }
    public String getDefaultPayment() { return defaultPayment; }
    public void setDefaultPayment(String defaultPayment) { this.defaultPayment = defaultPayment; }

    /* Write to a simple JSON string. */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"name\": \"").append(escapeJson(name)).append("\",\n");
        sb.append("  \"passengerType\": \"").append(passengerType.name()).append("\",\n");
        sb.append("  \"defaultPayment\": \"").append(escapeJson(defaultPayment)).append("\"\n");
        sb.append("}");
        return sb.toString();
    }

    /* Read from a simple JSON string. */
    public static RiderProfile fromJson(String json) {
        RiderProfile profile = new RiderProfile();
        String nameVal = extractJsonString(json, "name");
        if (nameVal != null) profile.setName(nameVal);

        String typeVal = extractJsonString(json, "passengerType");
        if (typeVal != null) {
            try { profile.setPassengerType(CityRideDataset.PassengerType.valueOf(typeVal)); }
            catch (IllegalArgumentException ignored) {}
        }

        String payVal = extractJsonString(json, "defaultPayment");
        if (payVal != null) profile.setDefaultPayment(payVal);

        return profile;
    }

    @Override
    public String toString() {
        return String.format("Name: %s | Type: %s | Payment: %s", name, passengerType, defaultPayment);
    }


    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /* Extract a string value from a simple JSON object by key. */
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

    /* Extract a JSON object block by key name. */
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

    /* Save this profile to a JSON file. */
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

    /* Load a profile by name from file. */
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

    /* List all saved profile names. */
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

    /* Load configuration from file. */
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

    /* Save current config to file. */
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

        // Discounts
        sb.append("  \"discountRates\": {\n");
        Map<CityRideDataset.PassengerType, BigDecimal> dr = CityRideDataset.getDiscountRates();
        int i = 0;
        for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
            sb.append("    \"").append(type.name()).append("\": \"").append(dr.get(type)).append("\"");
            if (++i < dr.size()) sb.append(",");
            sb.append("\n");
        }
        sb.append("  },\n");

        // Daily caps
        sb.append("  \"dailyCaps\": {\n");
        Map<CityRideDataset.PassengerType, BigDecimal> dc = CityRideDataset.getDailyCaps();
        i = 0;
        for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
            sb.append("    \"").append(type.name()).append("\": \"").append(dc.get(type)).append("\"");
            if (++i < dc.size()) sb.append(",");
            sb.append("\n");
        }
        sb.append("  },\n");

        // Peak windows
        sb.append("  \"peakMorningStart\": \"").append(CityRideDataset.getPeakStartTime()).append("\",\n");
        sb.append("  \"peakMorningEnd\": \"").append(CityRideDataset.getPeakEndTime()).append("\",\n");
        sb.append("  \"peakEveningStart\": \"").append(CityRideDataset.getPeakEveningStartTime()).append("\",\n");
        sb.append("  \"peakEveningEnd\": \"").append(CityRideDataset.getPeakEveningEndTime()).append("\",\n");

        // Base fares
        sb.append("  \"baseFares\": {\n");
        Map<String, BigDecimal> bf = CityRideDataset.getBaseFares();
        i = 0;
        for (Map.Entry<String, BigDecimal> entry : bf.entrySet()) {
            sb.append("    \"").append(entry.getKey()).append("\": \"").append(entry.getValue()).append("\"");
            if (++i < bf.size()) sb.append(",");
            sb.append("\n");
        }
        sb.append("  }\n");
        sb.append("}");
        return sb.toString();
    }

    private static void parseConfig(String json) {
        // Parse discount rates
        String discountBlock = extractBlock(json, "discountRates");
        if (discountBlock != null) {
            for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
                String val = extractJsonString(discountBlock, type.name());
                if (val != null) {
                    CityRideDataset.setDiscountRate(type, new BigDecimal(val).setScale(2, RoundingMode.HALF_UP));
                }
            }
        }

        // Parse daily caps
        String capBlock = extractBlock(json, "dailyCaps");
        if (capBlock != null) {
            for (CityRideDataset.PassengerType type : CityRideDataset.PassengerType.values()) {
                String val = extractJsonString(capBlock, type.name());
                if (val != null) {
                    CityRideDataset.setDailyCap(type, new BigDecimal(val).setScale(2, RoundingMode.HALF_UP));
                }
            }
        }

        // Parse peak windows
        String pms = extractJsonString(json, "peakMorningStart");
        String pme = extractJsonString(json, "peakMorningEnd");
        String pes = extractJsonString(json, "peakEveningStart");
        String pee = extractJsonString(json, "peakEveningEnd");
        if (pms != null && pme != null && pes != null && pee != null) {
            CityRideDataset.setPeakWindows(pms, pme, pes, pee);
        }

        // Parse base fares
        String fareBlock = extractBlock(json, "baseFares");
        if (fareBlock != null) {
            for (int from = CityRideDataset.MIN_ZONE; from <= CityRideDataset.MAX_ZONE; from++) {
                for (int to = CityRideDataset.MIN_ZONE; to <= CityRideDataset.MAX_ZONE; to++) {
                    for (CityRideDataset.TimeBand band : CityRideDataset.TimeBand.values()) {
                        String k = CityRideDataset.key(from, to, band);
                        String val = extractJsonString(fareBlock, k);
                        if (val != null) {
                            CityRideDataset.setBaseFare(from, to, band, new BigDecimal(val).setScale(2, RoundingMode.HALF_UP));
                        }
                    }
                }
            }
        }
    }

    private static final String STATE_DIR = "data/state/";

    /* Save rider profile and current journeys to files. */
    public static void saveDayState(RiderProfile profile, List<Journey> journeys) {
        try {
            File dir = new File(STATE_DIR);
            if (!dir.exists()) dir.mkdirs();

            writeFile(new File(STATE_DIR + "last_profile.json"), profile.toJson());

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(STATE_DIR + "last_journeys.csv"))) {
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

    /* Load rider profile from last saved state. */
    public static RiderProfile loadSavedProfile() {
        File file = new File(STATE_DIR + "last_profile.json");
        if (!file.exists()) return null;
        try {
            return fromJson(readFile(file));
        } catch (IOException e) {
            return null;
        }
    }

    /* Check if a saved state exists. */
    public static boolean hasSavedState() {
        return new File(STATE_DIR + "last_profile.json").exists();
    }

    public static String getSavedJourneysPath() {
        return STATE_DIR + "last_journeys.csv";
    }
}
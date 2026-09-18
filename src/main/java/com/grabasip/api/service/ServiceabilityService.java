package com.grabasip.api.service;

import com.grabasip.api.web.dto.ServiceCheckResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * "The bracket" — is a location inside our delivery net?
 * Mirrors the frontend logic in lib/serviceability.ts (keep them in sync).
 * Coarse AREA gate only; the full address is validated by a human / admin.
 */
@Service
public class ServiceabilityService {

    public static final String CITY = "Hyderabad";

    /** A served locality: canonical display name + spelling/variant aliases. */
    private record Served(String name, List<String> aliases) {}

    private static final List<Served> SERVED = List.of(
            new Served("Gachibowli", List.of("gachibowly", "gachhibowli")),
            new Served("Gowlidoddy", List.of("gowli doddy", "gowlidody")),
            new Served("TNGOs Colony", List.of("tngos", "tngo colony", "t n g o s")),
            new Served("Chandanagar", List.of("chanda nagar", "chandhanagar")),
            new Served("Madinaguda", List.of("madina guda", "madeenaguda")),
            new Served("Miyapur", List.of("mayapur", "miapur")),
            new Served("Kukatpally", List.of("kukatpalli", "kukat pally")),
            new Served("KPHB", List.of("k p h b", "kphb colony", "kukatpally housing board")),
            new Served("JNTU", List.of("jntuh", "jntu kukatpally")),
            new Served("Moosapet", List.of("musapet", "moosapeta")),
            new Served("Ameerpet", List.of("amirpet", "ameer pet")),
            new Served("S.R. Nagar", List.of("sr nagar", "s r nagar", "srnagar", "sanjeeva reddy nagar")),
            new Served("Yousufguda", List.of("yusufguda", "yousufgooda", "yousuf guda")),
            new Served("Jubilee Hills", List.of("jubilee hill", "jublee hills")),
            new Served("Madhapur", List.of("madapur", "madhapoor")),
            new Served("Kondapur", List.of("kondapoor", "konda pur")),
            new Served("Kothaguda", List.of("kotha guda", "kothaguda x roads")),
            new Served("Hafeezpet", List.of("hafizpet", "hafeez pet", "hafeezpeta")),
            new Served("Uppal", List.of("uppal depot", "uppal x roads")),
            new Served("Boduppal", List.of("bodduppal", "bodu ppal")),
            new Served("Medipally", List.of("medipalli", "medpally")),
            new Served("Narapally", List.of("narapalli", "nara pally")),
            new Served("Chengicherla", List.of("chengicharla", "chengi cherla")),
            new Served("Bolligudam", List.of("bolligudem", "bolli gudam"))
    );

    private static final Map<String, String> SERVICE_PINCODES = new LinkedHashMap<>();
    static {
        SERVICE_PINCODES.put("500032", "Gachibowli");
        SERVICE_PINCODES.put("500084", "Kondapur");
        SERVICE_PINCODES.put("500081", "Madhapur");
        SERVICE_PINCODES.put("500072", "Kukatpally");
        SERVICE_PINCODES.put("500085", "KPHB");
        SERVICE_PINCODES.put("500049", "Miyapur");
        SERVICE_PINCODES.put("500050", "Chandanagar");
        SERVICE_PINCODES.put("500033", "Jubilee Hills");
        SERVICE_PINCODES.put("500045", "Yousufguda");
        SERVICE_PINCODES.put("500038", "S.R. Nagar");
        SERVICE_PINCODES.put("500018", "Moosapet");
        SERVICE_PINCODES.put("500016", "Ameerpet");
        SERVICE_PINCODES.put("500039", "Uppal");
        SERVICE_PINCODES.put("500092", "Boduppal");
    }

    private record Entry(String key, String display, boolean multi) {}

    private final List<Entry> entries = new ArrayList<>();
    private final List<String> localityOptions = new ArrayList<>();

    public ServiceabilityService() {
        for (Served s : SERVED) {
            localityOptions.add(s.name());
            addEntry(s.name(), s.name());
            for (String a : s.aliases()) addEntry(a, s.name());
        }
    }

    private void addEntry(String raw, String display) {
        String key = normalize(raw);
        if (!key.isEmpty()) entries.add(new Entry(key, display, key.contains(" ")));
    }

    public List<String> localityOptions() {
        return List.copyOf(localityOptions);
    }

    private static String normalize(String s) {
        if (s == null) return "";
        String x = s.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
        return x;
    }

    private static int fuzzyThreshold(int len) {
        return len <= 5 ? 1 : 2;
    }

    private static int lev(String a, String b) {
        int m = a.length(), n = b.length();
        if (m == 0) return n;
        if (n == 0) return m;
        int[] prev = new int[n + 1];
        int[] cur = new int[n + 1];
        for (int j = 0; j <= n; j++) prev[j] = j;
        for (int i = 1; i <= m; i++) {
            cur[0] = i;
            for (int j = 1; j <= n; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(prev[j] + 1, cur[j - 1] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev; prev = cur; cur = tmp;
        }
        return prev[n];
    }

    /** Best-scoring served locality for the given free text, or null. */
    private String matchLocality(String n) {
        if (n.isEmpty()) return null;
        String[] tokens = n.split(" ");
        String best = null;
        int bestScore = Integer.MAX_VALUE;

        for (Entry e : entries) {
            if (n.equals(e.key())) return e.display(); // whole-input exact
            if (e.multi()) {
                if (n.contains(e.key()) && 0 < bestScore) { best = e.display(); bestScore = 0; }
                continue;
            }
            for (String t : tokens) {
                if (t.length() < 3) continue;
                int score;
                if (t.equals(e.key())) {
                    score = 0;
                } else {
                    int d = lev(t, e.key());
                    if (d <= fuzzyThreshold(Math.max(t.length(), e.key().length()))) score = d;
                    else continue;
                }
                if (score < bestScore) { bestScore = score; best = e.display(); }
            }
        }
        return best;
    }

    private static final Pattern PIN = Pattern.compile("\\b(\\d{6})\\b");

    public ServiceCheckResponse check(String input) {
        String raw = input == null ? "" : input.trim();
        String n = normalize(raw);

        if (n.isEmpty() || n.length() < 3) {
            return ServiceCheckResponse.askAgain(
                    "Please enter your area (e.g. Gachibowli) or 6-digit pincode.");
        }

        Matcher pm = PIN.matcher(n);
        if (pm.find()) {
            String pin = pm.group(1);
            String area = SERVICE_PINCODES.get(pin);
            if (area != null) return ServiceCheckResponse.serviceable(area, pin);
            if (matchLocality(n) == null) {
                return ServiceCheckResponse.askAgain(
                        "We couldn't match that pincode. Please type your area name (e.g. Kondapur) so we can check precisely.");
            }
        }

        String area = matchLocality(n);
        if (area != null) return ServiceCheckResponse.serviceable(area, null);

        if (n.matches("^\\d+$")) {
            return ServiceCheckResponse.askAgain(
                    "That doesn't look complete. Enter a 6-digit pincode or your area name.");
        }
        if (n.replace(" ", "").length() >= 4) {
            return ServiceCheckResponse.notServiceable();
        }
        return ServiceCheckResponse.askAgain(
                "Please enter your area (e.g. Gachibowli) or 6-digit pincode.");
    }
}

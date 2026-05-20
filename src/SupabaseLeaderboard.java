import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SupabaseLeaderboard {
    private static final String SUPABASE_URL = normalizeSupabaseUrl(System.getenv("SUPABASE_URL"));
    private static final String SUPABASE_ANON_KEY = System.getenv("SUPABASE_ANON_KEY");
    private static final String TABLE_NAME = "leaderboard";

    private final HttpClient client = HttpClient.newHttpClient();

    public boolean isConfigured() {
        return SUPABASE_URL != null && !SUPABASE_URL.isBlank()
                && SUPABASE_ANON_KEY != null && !SUPABASE_ANON_KEY.isBlank();
    }

    public void submitScore(Character character) {
        if (!isConfigured()) {
            System.out.println("Online leaderboard is not configured on this computer.");
            return;
        }

        String json = "{\"name\":\"" + escapeJson(character.getName()) + "\",\"average\":" + character.getScore() + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUPABASE_URL + "/rest/v1/" + TABLE_NAME))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=minimal")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Score submitted to the online leaderboard.");
            } else {
                System.out.println("Could not submit score online. Status: " + response.statusCode());
                System.out.println(response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Could not connect to the online leaderboard.");
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void printTopScores() {
        if (!isConfigured()) {
            return;
        }

        String query = "/rest/v1/" + TABLE_NAME + "?select=name,average&order=average.asc&limit=10";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUPABASE_URL + query))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Online leaderboard:");
                printSimpleLeaderboardJson(response.body());
            } else {
                System.out.println("Could not load online leaderboard. Status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Could not connect to the online leaderboard.");
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void printSimpleLeaderboardJson(String json) {
        if (json.equals("[]")) {
            System.out.println("No online scores yet.");
            return;
        }

        String cleaned = json.replace("[", "").replace("]", "");
        String[] rows = cleaned.split("\\}\\s*,\\s*\\{");

        for (int i = 0; i < rows.length; i++) {
            String row = rows[i].replace("{", "").replace("}", "");
            String name = findJsonValue(row, "name");
            String average = findJsonValue(row, "average");
            System.out.println((i + 1) + ". " + name + " - " + average);
        }
    }

    private String findJsonValue(String row, String key) {
        String search = "\"" + key + "\":";
        int start = row.indexOf(search);
        if (start == -1) {
            return "";
        }

        start += search.length();
        if (start < row.length() && row.charAt(start) == '"') {
            int end = row.indexOf('"', start + 1);
            return row.substring(start + 1, end);
        }

        int end = row.indexOf(',', start);
        if (end == -1) {
            end = row.length();
        }
        return row.substring(start, end);
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private static String normalizeSupabaseUrl(String value) {
        if (value == null) {
            return null;
        }

        String url = value.trim();
        if (url.endsWith("/rest/v1/")) {
            url = url.substring(0, url.length() - "/rest/v1/".length());
        } else if (url.endsWith("/rest/v1")) {
            url = url.substring(0, url.length() - "/rest/v1".length());
        }

        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        return url;
    }
}

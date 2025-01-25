package com.example.onlineexamsystem.Student;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

public class QuoteFetcher {
    private static final String ZEN_QUOTES_API_URL = "https://zenquotes.io/api/random";

    public static String fetchRandomQuote() {
        try {
            URL url = new URL(ZEN_QUOTES_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String jsonResponse = reader.lines().collect(Collectors.joining());
                return extractQuoteFromJson(jsonResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error fetching quote.";
        }
    }

    private static String extractQuoteFromJson(String jsonResponse) {
        // Parse JSON and extract the quote
        // For simplicity, assuming the JSON structure is {"q": "Your quote here"}
        return jsonResponse.contains("\"q\":") ? jsonResponse.substring(jsonResponse.indexOf("\"q\":") + 6, jsonResponse.indexOf("\"a\":")) : "No quote found.";
    }
}


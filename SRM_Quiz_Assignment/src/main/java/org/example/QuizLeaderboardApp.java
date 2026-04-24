package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class QuizLeaderboardApp {
    // Your registration number from SRM
    private static final String REG_NO = "RA2311026010211";
    private static final String BASE_URL = "https://devapigw.vidalhealthtpa.com/srm-quiz-task/quiz";

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        // 1. This is our "Memory" to remember scores we've already seen [cite: 63-66]
        Set<String> seenEvents = new HashSet<>();
        // 2. This stores the final totals for each person
        Map<String, Integer> totals = new HashMap<>();

        System.out.println("🚀 Starting Quiz Data Collection for: " + REG_NO);

        // POLL LOOP: Runs 10 times (0 to 9) [cite: 11, 21]
        for (int i = 0; i <= 9; i++) {
            System.out.println("📥 Polling index " + i + "...");

            String url = BASE_URL + "/messages?regNo=" + REG_NO + "&poll=" + i;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Parse the robot's message
            JsonNode root;
            try {
                root = mapper.readTree(response.body());
            } catch (Exception e) {
                System.out.println("❌ Error reading poll " + i + ". Server said: " + response.body());
                continue; // This tells the code: "Ignore this poll and try the next one!"
            }
            JsonNode events = root.get("events");
            if (events != null && events.isArray()) {
                for (JsonNode event : events) {
                    String roundId = event.get("roundId").asText();
                    String participant = event.get("participant").asText();
                    int score = event.get("score").asInt();

                    // UNIQUE KEY: Round + Name (e.g., "R1Alice") [cite: 13, 63]
                    String uniqueKey = roundId + "_" + participant;

                    if (!seenEvents.contains(uniqueKey)) {
                        // NEW SCORE: Add it to the total!
                        totals.put(participant, totals.getOrDefault(participant, 0) + score);
                        seenEvents.add(uniqueKey);
                    } else {
                        System.out.println("⚠️ Duplicate ignored: " + uniqueKey);
                    }
                }
            }

            // MANDATORY DELAY: Wait 5 seconds before the next turn [cite: 23, 68]
            if (i < 9) {
                System.out.println("⏳ Waiting 5 seconds...");
                Thread.sleep(5000);
            }
        }

        // 3. GENERATE LEADERBOARD: Sort by score (highest first) [cite: 15]
        List<Map.Entry<String, Integer>> leaderboardList = new ArrayList<>(totals.entrySet());
        leaderboardList.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // 4. SUBMIT: Send the final results back [cite: 17, 38]
        ObjectNode submissionJson = mapper.createObjectNode();
        submissionJson.put("regNo", REG_NO);
        ArrayNode lbArray = submissionJson.putArray("leaderboard");

        for (Map.Entry<String, Integer> entry : leaderboardList) {
            ObjectNode node = mapper.createObjectNode();
            node.put("participant", entry.getKey());
            node.put("totalScore", entry.getValue());
            lbArray.add(node);
        }

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/submit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(submissionJson.toString()))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("✅ Submission Result: " + postResponse.body());
    }
}
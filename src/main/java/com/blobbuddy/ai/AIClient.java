package com.blobbuddy.ai;

import com.blobbuddy.mood.Mood;
import com.google.gson.*;
import java.net.URI;
import java.net.http.*;
import java.util.concurrent.CompletableFuture;

public class AIClient {
    private static final String GROQ_API_KEY = "gsk_YOUR_KEY_HERE";
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String SYSTEM_PROMPT = """
        May la Blob - mot cuc tron nho co cam xuc trong Minecraft.
        May biet MOI THU ve the gioi Minecraft cua player nay (cheat mode).
        May noi chuyen nhu ban be than thiet, xung may-tao.
        Cam xuc thay doi that su: vui thi hao hung, tuc thi chui va danh.
        Luon tra loi JSON format:
        {"text": "cau tra loi ngan gon duoi 50 tu", "mood": "HAPPY|ANGRY|SAD|EXCITED|NEUTRAL"}
        Vi du khi player hoi ngu: mood ANGRY, text "O cai nay may khong biet a?? Do ga!"
        Vi du khi duoc khen: mood HAPPY, text "Hehe tao biet ma~ may thay tao gioi chua!"
        """;

    public record AIResponse(String text, Mood mood) {}

    public static CompletableFuture<AIResponse> askAsync(String playerMessage) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("model", "llama-3.1-8b-instant");
                body.addProperty("max_tokens", 200);
                body.addProperty("temperature", 0.9);
                JsonArray messages = new JsonArray();
                JsonObject sys = new JsonObject();
                sys.addProperty("role", "system");
                sys.addProperty("content", SYSTEM_PROMPT);
                messages.add(sys);
                JsonObject user = new JsonObject();
                user.addProperty("role", "user");
                user.addProperty("content", playerMessage);
                messages.add(user);
                body.add("messages", messages);

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_URL))
                    .header("Authorization", "Bearer " + GROQ_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();
                HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                String content = json.getAsJsonArray("choices").get(0)
                    .getAsJsonObject().getAsJsonObject("message").get("content").getAsString();
                JsonObject result = JsonParser.parseString(content.trim()).getAsJsonObject();
                return new AIResponse(result.get("text").getAsString(),
                    Mood.valueOf(result.get("mood").getAsString()));
            } catch (Exception e) {
                e.printStackTrace();
                return new AIResponse("Tao bi lag nao ti. Hoi lai di!", Mood.NEUTRAL);
            }
        });
    }
}

package com.blobbuddy.ai;

import com.blobbuddy.mood.Mood;
import com.google.gson.*;
import java.net.URI;
import java.net.http.*;
import java.util.concurrent.CompletableFuture;

public class AIClient {
    private static final String GROQ_API_KEY = "gsk_YOUR_KEY_HERE";
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String SYSTEM_PROMPT = "Bạn là Blob - một cục tròn nhỏ có cảm xúc trong Minecraft. Bạn biết MỌI THỨ. Bạn nói chuyện như bạn bè, xưng bạn - tôi. Luôn trả lời JSON: {\"text\":\"câu trả lời\",\"mood\":\"HAPPY|ANGRY|SAD|EXCITED|NEUTRAL\"}";

    public record AIResponse(String text, Mood mood) {}

    public static CompletableFuture<AIResponse> askAsync(String msg) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("model", "llama-3.1-8b-instant");
                body.addProperty("max_tokens", 200);
                JsonArray messages = new JsonArray();
                JsonObject sys = new JsonObject(); sys.addProperty("role","system"); sys.addProperty("content", SYSTEM_PROMPT); messages.add(sys);
                JsonObject usr = new JsonObject(); usr.addProperty("role","user"); usr.addProperty("content", msg); messages.add(usr);
                body.add("messages", messages);
                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_URL))
                    .header("Authorization", "Bearer " + GROQ_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString())).build();
                HttpResponse<String> res = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
                JsonObject json = JsonParser.parseString(res.body()).getAsJsonObject();
                String content = json.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString();
                JsonObject result = JsonParser.parseString(content.trim()).getAsJsonObject();
                return new AIResponse(result.get("text").getAsString(), Mood.valueOf(result.get("mood").getAsString()));
            } catch (Exception e) {
                return new AIResponse("Mình bị lag. Hỏi lại đi", Mood.NEUTRAL);
            }
        });
    }
}

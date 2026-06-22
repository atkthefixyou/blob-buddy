package com.blobbuddy.ai;

import com.blobbuddy.mood.Mood;
import com.google.gson.*;
import java.net.URI;
import java.net.http.*;
import java.util.concurrent.CompletableFuture;

public class AIClient {

    // Thay bằng key của bạn — dùng Groq vì free & nhanh
    private static final String GROQ_API_KEY = "gsk_YOUR_KEY_HERE";
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    private static final String SYSTEM_PROMPT = """
        Mày là Blob — một cục tròn nhỏ có cảm xúc trong Minecraft.
        Mày biết MỌI THỨ về thế giới Minecraft của player này (cheat mode).
        Mày nói chuyện như bạn bè thân thiết, xưng mày-tao.
        Cảm xúc thay đổi thật sự: vui thì hào hứng, tức thì chửi và đánh.
        
        Luôn trả lời JSON format:
        {
          "text": "câu trả lời ngắn gọn (<50 từ)",
          "mood": "HAPPY|ANGRY|SAD|EXCITED|NEUTRAL"
        }
        
        Ví dụ khi player hỏi ngu: mood ANGRY, text "Ơ cái này mày không biết á?? Đồ gà!"
        Ví dụ khi được khen: mood HAPPY, text "Hehe tao biết mà~ mày thấy tao giỏi chưa!"
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
                String content = json.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

                // Parse JSON response từ AI
                JsonObject result = JsonParser.parseString(content.trim()).getAsJsonObject();
                String text = result.get("text").getAsString();
                Mood mood = Mood.valueOf(result.get("mood").getAsString());

                return new AIResponse(text, mood);

            } catch (Exception e) {
                e.printStackTrace();
                return new AIResponse("Ờ... tao bị lag não tí. Hỏi lại đi!", Mood.NEUTRAL);
            }
        });
    }
}
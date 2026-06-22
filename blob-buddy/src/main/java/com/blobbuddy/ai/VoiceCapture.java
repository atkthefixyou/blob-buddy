package com.blobbuddy.ai;

import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import com.google.gson.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class VoiceCapture {

    private static final String OPENAI_KEY = "sk-YOUR_OPENAI_KEY"; // Whisper API
    private static TargetDataLine micLine;
    private static boolean isRecording = false;

    // Format âm thanh
    private static final AudioFormat FORMAT = new AudioFormat(
        16000, 16, 1, true, false
    );

    /**
     * Bắt đầu ghi âm khi player giữ phím (gọi từ KeyBinding)
     */
    public static void startRecording() {
        if (isRecording) return;
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, FORMAT);
            micLine = (TargetDataLine) AudioSystem.getLine(info);
            micLine.open(FORMAT);
            micLine.start();
            isRecording = true;
            System.out.println("[BlobBuddy] 🎤 Đang ghi âm...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Dừng ghi âm → trả về text qua callback
     */
    public static CompletableFuture<String> stopAndTranscribe() {
        return CompletableFuture.supplyAsync(() -> {
            if (!isRecording || micLine == null) return "";
            try {
                isRecording = false;
                micLine.stop();

                // Đọc audio bytes
                byte[] audioData = micLine.drain() != 0
                    ? new byte[(int) micLine.getFramePosition() * FORMAT.getFrameSize()]
                    : new byte[0];
                micLine.read(audioData, 0, audioData.length);
                micLine.close();

                // Ghi ra file WAV tạm
                File tempWav = File.createTempFile("blob_voice_", ".wav");
                AudioInputStream ais = new AudioInputStream(
                    new ByteArrayInputStream(audioData), FORMAT,
                    audioData.length / FORMAT.getFrameSize()
                );
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, tempWav);

                // Gửi lên Whisper API
                return transcribeWithWhisper(tempWav);

            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        });
    }

    private static String transcribeWithWhisper(File audioFile) throws Exception {
        // Multipart form data upload
        String boundary = "----BlobBoundary" + System.currentTimeMillis();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // File part
        String filePart = "--" + boundary + "\r\n"
            + "Content-Disposition: form-data; name=\"file\"; filename=\"audio.wav\"\r\n"
            + "Content-Type: audio/wav\r\n\r\n";
        baos.write(filePart.getBytes());
        baos.write(java.nio.file.Files.readAllBytes(audioFile.toPath()));
        baos.write("\r\n".getBytes());

        // Model part
        String modelPart = "--" + boundary + "\r\n"
            + "Content-Disposition: form-data; name=\"model\"\r\n\r\n"
            + "whisper-1\r\n";
        baos.write(modelPart.getBytes());

        // Language hint
        String langPart = "--" + boundary + "\r\n"
            + "Content-Disposition: form-data; name=\"language\"\r\n\r\nvi\r\n";
        baos.write(langPart.getBytes());

        baos.write(("--" + boundary + "--\r\n").getBytes());

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.openai.com/v1/audio/transcriptions"))
            .header("Authorization", "Bearer " + OPENAI_KEY)
            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
            .POST(HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray()))
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        audioFile.delete();
        return json.has("text") ? json.get("text").getAsString() : "";
    }
}
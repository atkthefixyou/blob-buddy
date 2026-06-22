package com.blobbuddy.ai;

import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import com.google.gson.*;
import java.util.concurrent.CompletableFuture;

public class VoiceCapture {
    private static final String OPENAI_KEY = "sk-YOUR_OPENAI_KEY";
    private static TargetDataLine micLine;
    private static boolean isRecording = false;
    private static final AudioFormat FORMAT = new AudioFormat(16000, 16, 1, true, false);
    private static final java.util.List<byte[]> chunks = new java.util.ArrayList<>();

    public static void startRecording() {
        if (isRecording) return;
        try {
            chunks.clear();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, FORMAT);
            micLine = (TargetDataLine) AudioSystem.getLine(info);
            micLine.open(FORMAT);
            micLine.start();
            isRecording = true;
            Thread.ofVirtual().start(() -> {
                byte[] buf = new byte[4096];
                while (isRecording) {
                    int n = micLine.read(buf, 0, buf.length);
                    if (n > 0) chunks.add(java.util.Arrays.copyOf(buf, n));
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static CompletableFuture<String> stopAndTranscribe() {
        return CompletableFuture.supplyAsync(() -> {
            if (!isRecording || micLine == null) return "";
            try {
                isRecording = false;
                Thread.sleep(100);
                micLine.stop(); micLine.close();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                for (byte[] c : chunks) baos.write(c);
                byte[] audioData = baos.toByteArray();
                File tempWav = File.createTempFile("blob_voice_", ".wav");
                AudioInputStream ais = new AudioInputStream(
                    new ByteArrayInputStream(audioData), FORMAT,
                    audioData.length / FORMAT.getFrameSize());
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, tempWav);
                return transcribeWithWhisper(tempWav);
            } catch (Exception e) { e.printStackTrace(); return ""; }
        });
    }

    private static String transcribeWithWhisper(File audioFile) throws Exception {
        String boundary = "BlobBoundary" + System.currentTimeMillis();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"audio.wav\"\r\nContent-Type: audio/wav\r\n\r\n").getBytes());
        baos.write(java.nio.file.Files.readAllBytes(audioFile.toPath()));
        baos.write(("\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"model\"\r\n\r\nwhisper-1\r\n").getBytes());
        baos.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"language\"\r\n\r\nvi\r\n").getBytes());
        baos.write(("--" + boundary + "--\r\n").getBytes());
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.openai.com/v1/audio/transcriptions"))
            .header("Authorization", "Bearer " + OPENAI_KEY)
            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
            .POST(HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray()))
            .build();
        HttpResponse<String> response = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString());
        audioFile.delete();
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        return json.has("text") ? json.get("text").getAsString() : "";
    }
}

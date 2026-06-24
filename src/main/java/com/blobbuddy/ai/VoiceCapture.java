package com.blobbuddy.ai;

import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import com.google.gson.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VoiceCapture {
    private static final String OPENAI_KEY = "sk-efgh5678abcd1234efgh5678abcd1234efgh5678";
    private static TargetDataLine micLine;
    private static boolean isRecording = false;
    private static final List<byte[]> chunks = new ArrayList<>();
    private static final AudioFormat FORMAT = new AudioFormat(16000, 16, 1, true, false);

    public static void startRecording() {
        if (isRecording) return;
        try {
            chunks.clear();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, FORMAT);
            micLine = (TargetDataLine) AudioSystem.getLine(info);
            micLine.open(FORMAT); micLine.start(); isRecording = true;
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
                isRecording = false; Thread.sleep(100);
                micLine.stop(); micLine.close();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                for (byte[] c : chunks) baos.write(c);
                byte[] audio = baos.toByteArray();
                File tmp = File.createTempFile("blob_", ".wav");
                AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(audio), FORMAT, audio.length / FORMAT.getFrameSize()), AudioFileFormat.Type.WAVE, tmp);
                String boundary = "Boundary" + System.currentTimeMillis();
                ByteArrayOutputStream req = new ByteArrayOutputStream();
                req.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\"file\"; filename=\"a.wav\"\r\nContent-Type: audio/wav\r\n\r\n").getBytes());
                req.write(java.nio.file.Files.readAllBytes(tmp.toPath()));
                req.write(("\r\n--"+boundary+"\r\nContent-Disposition: form-data; name=\"model\"\r\n\r\nwhisper-1\r\n--"+boundary+"--\r\n").getBytes());
                HttpResponse<String> res = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder().uri(URI.create("https://api.openai.com/v1/audio/transcriptions"))
                        .header("Authorization","Bearer "+OPENAI_KEY)
                        .header("Content-Type","multipart/form-data; boundary="+boundary)
                        .POST(HttpRequest.BodyPublishers.ofByteArray(req.toByteArray())).build(),
                    HttpResponse.BodyHandlers.ofString());
                tmp.delete();
                JsonObject j = JsonParser.parseString(res.body()).getAsJsonObject();
                return j.has("text") ? j.get("text").getAsString() : "";
            } catch (Exception e) { e.printStackTrace(); return ""; }
        });
    }
}

package com.blobbuddy.ai;

import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;

public class TTSPlayer {

    // Chọn 1 trong 2 engine bên dưới

    // === OPTION 1: Edge TTS (free, tiếng Việt tốt, cần Python) ===
    // Chạy: pip install edge-tts
    // Rồi gọi từ Java qua ProcessBuilder

    // === OPTION 2: ElevenLabs API (giọng đẹp hơn, có free tier) ===
    private static final String ELEVENLABS_KEY = "YOUR_ELEVENLABS_KEY";
    private static final String VOICE_ID = "YOUR_VOICE_ID"; // chọn giọng trên ElevenLabs

    /**
     * Phát TTS — tự động chọn engine
     * Gọi method này sau khi nhận response từ AI
     */
    public static void speak(String text) {
        // Chạy async để không block game
        Thread.ofVirtual().start(() -> {
            try {
                // Thử Edge TTS trước (free)
                speakWithEdgeTTS(text);
            } catch (Exception e) {
                try {
                    // Fallback sang ElevenLabs
                    speakWithElevenLabs(text);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    // --- Edge TTS (Python subprocess) ---
    private static void speakWithEdgeTTS(String text) throws Exception {
        File tempMp3 = File.createTempFile("blob_tts_", ".mp3");

        // Gọi edge-tts CLI
        ProcessBuilder pb = new ProcessBuilder(
            "edge-tts",
            "--voice", "vi-VN-NamMinhNeural",   // giọng nam tiếng Việt
            "--text", text,
            "--write-media", tempMp3.getAbsolutePath()
        );
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        proc.waitFor();

        // Phát file mp3
        playAudioFile(tempMp3);
        tempMp3.delete();
    }

    // --- ElevenLabs API ---
    private static void speakWithElevenLabs(String text) throws Exception {
        String jsonBody = """
            {
              "text": "%s",
              "model_id": "eleven_multilingual_v2",
              "voice_settings": {
                "stability": 0.5,
                "similarity_boost": 0.75
              }
            }
            """.formatted(text.replace("\"", "\\\""));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.elevenlabs.io/v1/text-to-speech/" + VOICE_ID))
            .header("xi-api-key", ELEVENLABS_KEY)
            .header("Content-Type", "application/json")
            .header("Accept", "audio/mpeg")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        HttpResponse<byte[]> response = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofByteArray());

        File tempMp3 = File.createTempFile("blob_tts_", ".mp3");
        Files.write(tempMp3.toPath(), response.body());
        playAudioFile(tempMp3);
        tempMp3.delete();
    }

    // --- Phát audio file (MP3/WAV) ---
    private static void playAudioFile(File audioFile) throws Exception {
        // Dùng JavaFX MediaPlayer hoặc JLayer cho MP3
        // Nếu là WAV thì dùng javax.sound thẳng:
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(audioFile)) {
            AudioFormat baseFormat = ais.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false
            );

            try (AudioInputStream decoded = AudioSystem.getAudioInputStream(decodedFormat, ais)) {
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(decodedFormat);
                line.start();

                byte[] buf = new byte[4096];
                int bytesRead;
                while ((bytesRead = decoded.read(buf, 0, buf.length)) != -1) {
                    line.write(buf, 0, bytesRead);
                }
                line.drain();
                line.close();
            }
        }
    }
}
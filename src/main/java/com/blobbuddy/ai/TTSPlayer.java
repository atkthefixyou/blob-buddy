package com.blobbuddy.ai;

import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;

public class TTSPlayer {
    private static final String ELEVENLABS_KEY = "YOUR_ELEVENLABS_KEY";
    private static final String VOICE_ID = "YOUR_VOICE_ID";

    public static void speak(String text) {
        Thread.ofVirtual().start(() -> {
            try { speakWithEdgeTTS(text); }
            catch (Exception e) {
                try { speakWithElevenLabs(text); }
                catch (Exception ex) { ex.printStackTrace(); }
            }
        });
    }

    private static void speakWithEdgeTTS(String text) throws Exception {
        File tempMp3 = File.createTempFile("blob_tts_", ".mp3");
        Process proc = new ProcessBuilder("edge-tts",
            "--voice", "vi-VN-NamMinhNeural",
            "--text", text,
            "--write-media", tempMp3.getAbsolutePath())
            .redirectErrorStream(true).start();
        proc.waitFor();
        playAudioFile(tempMp3);
        tempMp3.delete();
    }

    private static void speakWithElevenLabs(String text) throws Exception {
        String body = "{\"text\":\"%s\",\"model_id\":\"eleven_multilingual_v2\",\"voice_settings\":{\"stability\":0.5,\"similarity_boost\":0.75}}"
            .formatted(text.replace("\"", "\\\""));
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.elevenlabs.io/v1/text-to-speech/" + VOICE_ID))
            .header("xi-api-key", ELEVENLABS_KEY)
            .header("Content-Type", "application/json")
            .header("Accept", "audio/mpeg")
            .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<byte[]> response = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofByteArray());
        File tempMp3 = File.createTempFile("blob_tts_", ".mp3");
        Files.write(tempMp3.toPath(), response.body());
        playAudioFile(tempMp3);
        tempMp3.delete();
    }

    private static void playAudioFile(File audioFile) throws Exception {
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(audioFile)) {
            AudioFormat base = ais.getFormat();
            AudioFormat decoded = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                base.getSampleRate(), 16, base.getChannels(),
                base.getChannels() * 2, base.getSampleRate(), false);
            try (AudioInputStream dec = AudioSystem.getAudioInputStream(decoded, ais)) {
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(
                    new DataLine.Info(SourceDataLine.class, decoded));
                line.open(decoded); line.start();
                byte[] buf = new byte[4096]; int n;
                while ((n = dec.read(buf, 0, buf.length)) != -1) line.write(buf, 0, n);
                line.drain(); line.close();
            }
        }
    }
}

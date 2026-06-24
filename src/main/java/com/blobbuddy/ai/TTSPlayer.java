package com.blobbuddy.ai;

import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;

public class TTSPlayer {
    private static final String ELEVENLABS_KEY = "sk_6bfd6a10dce3c8f701034d4502be3c4db0cacc111f2b4848";
    private static final String VOICE_ID = "w2KTJ6MO4SIK6nWK4YH8";

    public static void speak(String text) {
        Thread.ofVirtual().start(() -> {
            try { speakEdgeTTS(text); }
            catch (Exception e) {
                try { speakElevenLabs(text); }
                catch (Exception ex) { ex.printStackTrace(); }
            }
        });
    }

    private static void speakEdgeTTS(String text) throws Exception {
        File tmp = File.createTempFile("blob_tts_", ".mp3");
        new ProcessBuilder("edge-tts","--voice","vi-VN-NamMinhNeural","--text",text,"--write-media",tmp.getAbsolutePath())
            .redirectErrorStream(true).start().waitFor();
        playFile(tmp); tmp.delete();
    }

    private static void speakElevenLabs(String text) throws Exception {
        String body = "{\"text\":\"%s\",\"model_id\":\"eleven_multilingual_v2\"}".formatted(text.replace("\"","\\\""));
        HttpResponse<byte[]> res = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder().uri(URI.create("https://api.elevenlabs.io/v1/text-to-speech/"+VOICE_ID))
                .header("xi-api-key",ELEVENLABS_KEY).header("Content-Type","application/json").header("Accept","audio/mpeg")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
            HttpResponse.BodyHandlers.ofByteArray());
        File tmp = File.createTempFile("blob_tts_",".mp3");
        Files.write(tmp.toPath(), res.body()); playFile(tmp); tmp.delete();
    }

    private static void playFile(File f) throws Exception {
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(f)) {
            AudioFormat base = ais.getFormat();
            AudioFormat dec = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,base.getSampleRate(),16,base.getChannels(),base.getChannels()*2,base.getSampleRate(),false);
            try (AudioInputStream d = AudioSystem.getAudioInputStream(dec, ais)) {
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, dec));
                line.open(dec); line.start();
                byte[] buf = new byte[4096]; int n;
                while ((n = d.read(buf,0,buf.length)) != -1) line.write(buf,0,n);
                line.drain(); line.close();
            }
        }
    }
}

package com.blobbuddy;

import com.blobbuddy.ai.VoiceCapture;
import com.blobbuddy.entity.BlobEntityRenderer;
import com.blobbuddy.network.BlobPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.Entity;
import org.lwjgl.glfw.GLFW;

public class BlobClientMod implements ClientModInitializer {

    private static KeyMapping talkKey;
    private static boolean wasPressed = false;
    private static int nearestBlobId = -1;

    @Override
    public void onInitializeClient() {  
        software.bernie.geckolib.GeckoLib.initialize();
        // Đăng ký renderer
        EntityRendererRegistry.register(BlobBuddyMod.BLOB_ENTITY, BlobEntityRenderer::new);

        // Keybinding: giữ V để nói
        talkKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.blob-buddy.talk",
            GLFW.GLFW_KEY_V,
            "category.blob-buddy"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            boolean pressed = talkKey.isDown();

            // Bắt đầu ghi âm
            if (pressed && !wasPressed) {
                // Tìm Blob gần nhất
                client.level.entitiesForRendering().forEach(e -> {
                    // tìm blob gần player
                });
                VoiceCapture.startRecording();
            }

            // Dừng ghi âm + gửi lên server
            if (!pressed && wasPressed) {
                VoiceCapture.stopAndTranscribe().thenAccept(text -> {
                    if (!text.isBlank() && nearestBlobId != -1) {
                        ClientPlayNetworking.send(
                            new BlobPackets.VoiceTextPacket(nearestBlobId, text)
                        );
                    }
                });
            }

            wasPressed = pressed;
        });
      // Thêm vào BlobClientMod.java trong onInitializeClient():

// Đăng ký nhận packet từ server
PayloadTypeRegistry.playS2C().register(
    BlobPackets.AIResponsePacket.TYPE,
    BlobPackets.AIResponsePacket.CODEC
);
ClientPlayNetworking.registerGlobalReceiver(
    BlobPackets.AIResponsePacket.TYPE,
    (payload, ctx) -> {
        // Phát TTS (chạy off-thread)
        TTSPlayer.speak(payload.text());
    }
);
    }
}
package com.blobbuddy;

import com.blobbuddy.ai.VoiceCapture;
import com.blobbuddy.ai.TTSPlayer;
import com.blobbuddy.entity.BlobEntity;
import com.blobbuddy.entity.BlobEntityRenderer;
import com.blobbuddy.network.BlobPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.Entity;
import org.lwjgl.glfw.GLFW;
import software.bernie.geckolib.GeckoLib;

public class BlobClientMod implements ClientModInitializer {
    private static KeyMapping talkKey;
    private static boolean wasPressed = false;
    private static int nearestBlobId = -1;

    @Override
    public void onInitializeClient() {
        GeckoLib.initialize();
        EntityRendererRegistry.register(BlobBuddyMod.BLOB_ENTITY, BlobEntityRenderer::new);

        talkKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.blob-buddy.talk", GLFW.GLFW_KEY_V, "category.blob-buddy"
        ));

        PayloadTypeRegistry.playS2C().register(
            BlobPackets.AIResponsePacket.TYPE,
            BlobPackets.AIResponsePacket.CODEC
        );
        ClientPlayNetworking.registerGlobalReceiver(
            BlobPackets.AIResponsePacket.TYPE,
            (payload, ctx) -> TTSPlayer.speak(payload.text())
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) return;
            boolean pressed = talkKey.isDown();

            if (pressed && !wasPressed) {
                nearestBlobId = -1;
                double closest = 16.0;
                for (Entity e : client.level.entitiesForRendering()) {
                    if (e instanceof BlobEntity) {
                        double dist = client.player.distanceTo(e);
                        if (dist < closest) { closest = dist; nearestBlobId = e.getId(); }
                    }
                }
                VoiceCapture.startRecording();
            }

            if (!pressed && wasPressed) {
                final int blobId = nearestBlobId;
                VoiceCapture.stopAndTranscribe().thenAccept(text -> {
                    if (!text.isBlank() && blobId != -1) {
                        ClientPlayNetworking.send(new BlobPackets.VoiceTextPacket(blobId, text));
                    }
                });
            }
            wasPressed = pressed;
        });
    }
}

package com.blobbuddy.network;

import com.blobbuddy.BlobBuddyMod;
import com.blobbuddy.ai.AIClient;
import com.blobbuddy.entity.BlobEntity;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class BlobPackets {
    public static final ResourceLocation VOICE_TEXT = new ResourceLocation(BlobBuddyMod.MOD_ID, "voice_text");
    public static final ResourceLocation AI_RESPONSE = new ResourceLocation(BlobBuddyMod.MOD_ID, "ai_response");

    // Dummy records để BlobClientMod compile
    public record AIResponsePacket(String text, String mood) {
        public static final ResourceLocation TYPE = AI_RESPONSE;
    }
    public record VoiceTextPacket(int blobEntityId, String text) {
        public static final ResourceLocation TYPE = VOICE_TEXT;
    }

    public static void registerServerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(VOICE_TEXT, (server, player, handler, buf, responseSender) -> {
            int blobId = buf.readInt();
            String text = buf.readUtf();
            server.execute(() -> {
                Entity entity = player.level().getEntity(blobId);
                if (!(entity instanceof BlobEntity blob)) return;
                String msg = player.getName().getString() + " noi: " + text;
                AIClient.askAsync(msg).thenAccept(response -> server.execute(() -> {
                    blob.receiveAIResponse(response.text(), response.mood());
                    FriendlyByteBuf out = PacketByteBufs.create();
                    out.writeUtf(response.text());
                    out.writeUtf(response.mood().name());
                    ServerPlayNetworking.send(player, AI_RESPONSE, out);
                }));
            });
        });
    }
}

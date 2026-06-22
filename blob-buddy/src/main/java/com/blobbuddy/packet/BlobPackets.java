package com.blobbuddy.network;

import com.blobbuddy.BlobBuddyMod;
import com.blobbuddy.ai.AIClient;
import com.blobbuddy.entity.BlobEntity;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class BlobPackets {

    // Packet: Client gửi text lên server
    public record VoiceTextPacket(int blobEntityId, String text)
        implements CustomPacketPayload {

        public static final Type<VoiceTextPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BlobBuddyMod.MOD_ID, "voice_text")
        );
        public static final StreamCodec<FriendlyByteBuf, VoiceTextPacket> CODEC =
            StreamCodec.of(
                (buf, pkt) -> { buf.writeInt(pkt.blobEntityId); buf.writeUtf(pkt.text); },
                buf -> new VoiceTextPacket(buf.readInt(), buf.readUtf())
            );

        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void registerServerPackets() {
        PayloadTypeRegistry.playC2S().register(VoiceTextPacket.TYPE, VoiceTextPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(VoiceTextPacket.TYPE, (payload, ctx) -> {
            ctx.server().execute(() -> {
                Entity entity = ctx.player().level().getEntity(payload.blobEntityId());
                if (!(entity instanceof BlobEntity blob)) return;

                String playerName = ctx.player().getName().getString();
                String message = playerName + " nói: " + payload.text();

                // Async gọi AI
                AIClient.askAsync(message).thenAccept(response -> {
                    ctx.server().execute(() -> {
                        blob.receiveAIResponse(response.text(), response.mood());
                    });
                });
            });
        });
    }
}

// Thêm vào BlobPackets.java

// Packet: Server → Client (gửi AI response xuống)
public record AIResponsePacket(String text, String mood)
    implements CustomPacketPayload {

    public static final Type<AIResponsePacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(BlobBuddyMod.MOD_ID, "ai_response")
    );
    public static final StreamCodec<FriendlyByteBuf, AIResponsePacket> CODEC =
        StreamCodec.of(
            (buf, pkt) -> { buf.writeUtf(pkt.text()); buf.writeUtf(pkt.mood()); },
            buf -> new AIResponsePacket(buf.readUtf(), buf.readUtf())
        );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

// Đăng ký ở server side (trong registerServerPackets):
PayloadTypeRegistry.playS2C().register(AIResponsePacket.TYPE, AIResponsePacket.CODEC);

// Gửi từ server sau khi AI trả về:
AIClient.askAsync(message).thenAccept(response -> {
    ctx.server().execute(() -> {
        blob.receiveAIResponse(response.text(), response.mood());
        // Gửi xuống client để phát TTS
        ServerPlayNetworking.send(ctx.player(),
            new AIResponsePacket(response.text(), response.mood().name())
        );
    });
});
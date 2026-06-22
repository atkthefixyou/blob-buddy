package com.blobbuddy.network;

import com.blobbuddy.BlobBuddyMod;
import com.blobbuddy.ai.AIClient;
import com.blobbuddy.entity.BlobEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public class BlobPackets {

    public record VoiceTextPacket(int blobEntityId, String text) implements CustomPacketPayload {
        public static final Type<VoiceTextPacket> TYPE = new Type<>(
            Identifier.of(BlobBuddyMod.MOD_ID, "voice_text"));
        public static final StreamCodec<FriendlyByteBuf, VoiceTextPacket> CODEC = StreamCodec.of(
            (buf, pkt) -> { buf.writeInt(pkt.blobEntityId()); buf.writeUtf(pkt.text()); },
            buf -> new VoiceTextPacket(buf.readInt(), buf.readUtf()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record AIResponsePacket(String text, String mood) implements CustomPacketPayload {
        public static final Type<AIResponsePacket> TYPE = new Type<>(
            Identifier.of(BlobBuddyMod.MOD_ID, "ai_response"));
        public static final StreamCodec<FriendlyByteBuf, AIResponsePacket> CODEC = StreamCodec.of(
            (buf, pkt) -> { buf.writeUtf(pkt.text()); buf.writeUtf(pkt.mood()); },
            buf -> new AIResponsePacket(buf.readUtf(), buf.readUtf()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void registerServerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(VoiceTextPacket.TYPE, (payload, ctx) -> {
            ctx.server().execute(() -> {
                Entity entity = ctx.player().level().getEntity(payload.blobEntityId());
                if (!(entity instanceof BlobEntity blob)) return;
                String msg = ctx.player().getName().getString() + " noi: " + payload.text();
                AIClient.askAsync(msg).thenAccept(response -> ctx.server().execute(() -> {
                    blob.receiveAIResponse(response.text(), response.mood());
                    ServerPlayNetworking.send(ctx.player(),
                        new AIResponsePacket(response.text(), response.mood().name()));
                }));
            });
        });
    }
}

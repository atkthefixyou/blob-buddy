package com.blobbuddy.entity;

import com.blobbuddy.mood.Mood;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class BlobEntity extends PathfinderMob {
    private Mood currentMood = Mood.NEUTRAL;
    private String pendingResponse = null;
    private int angerTimer = 0;

    public BlobEntity(EntityType<? extends BlobEntity> type, Level world) {
        super(type, world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 100.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.ATTACK_DAMAGE, 5.0)
            .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    public void tick() {
        super.tick();
        if (pendingResponse != null && level() instanceof ServerLevel) {
            sendMessageNearby(pendingResponse);
            pendingResponse = null;
        }
        if (currentMood == Mood.ANGRY && level() instanceof ServerLevel sl) {
            if (++angerTimer > 40) { attackNearestPlayer(sl); angerTimer = 0; }
        }
    }

    public void receiveAIResponse(String text, Mood mood) {
        this.pendingResponse = text;
        this.currentMood = mood;
    }

    private void sendMessageNearby(String text) {
        level().players().stream()
            .filter(p -> distanceTo(p) < 16)
            .forEach(p -> p.sendSystemMessage(Component.literal("\u00a7d[Blob] \u00a7f" + text)));
    }

    private void attackNearestPlayer(ServerLevel sl) {
        Player nearest = level().getNearestPlayer(this, 8.0);
        if (nearest != null) {
            doHurtTarget(sl, nearest);
            sendMessageNearby("DO NGOC! Nhan dam nay di!");
        }
    }

    public Mood getCurrentMood() { return currentMood; }

    // Không override saveAdditionalSaveData vì API đổi sang ValueOutput
    // Dùng serializeNBT thay thế
    public void saveMoodToTag(CompoundTag tag) {
        tag.putString("mood", currentMood.name());
    }

    public void loadMoodFromTag(CompoundTag tag) {
        if (tag.contains("mood")) {
            try {
                Object val = tag.get("mood");
                if (val != null) currentMood = Mood.valueOf(val.toString());
            } catch (Exception e) { currentMood = Mood.NEUTRAL; }
        }
    }
}

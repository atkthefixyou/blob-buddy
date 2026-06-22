package com.blobbuddy.entity;

import com.blobbuddy.mood.Mood;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animation.*;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class BlobEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation ANIM_IDLE    = RawAnimation.begin().thenLoop("animation.blob.idle");
    private static final RawAnimation ANIM_HAPPY   = RawAnimation.begin().thenLoop("animation.blob.happy");
    private static final RawAnimation ANIM_ANGRY   = RawAnimation.begin().thenLoop("animation.blob.angry");
    private static final RawAnimation ANIM_SAD     = RawAnimation.begin().thenLoop("animation.blob.sad");
    private static final RawAnimation ANIM_EXCITED = RawAnimation.begin().thenLoop("animation.blob.excited");

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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "mood_controller", 5, state ->
            switch (currentMood) {
                case HAPPY   -> state.setAndContinue(ANIM_HAPPY);
                case ANGRY   -> state.setAndContinue(ANIM_ANGRY);
                case SAD     -> state.setAndContinue(ANIM_SAD);
                case EXCITED -> state.setAndContinue(ANIM_EXCITED);
                default      -> state.setAndContinue(ANIM_IDLE);
            }
        ));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    @Override
    public void tick() {
        super.tick();
        if (pendingResponse != null && !level().isClientSide()) {
            sendMessageNearby(pendingResponse);
            pendingResponse = null;
        }
        if (currentMood == Mood.ANGRY && !level().isClientSide()) {
            if (++angerTimer > 40) { attackNearestPlayer(); angerTimer = 0; }
        }
    }

    public void receiveAIResponse(String text, Mood mood) {
        this.pendingResponse = text;
        this.currentMood = mood;
    }

    private void sendMessageNearby(String text) {
        level().players().stream()
            .filter(p -> distanceTo(p) < 16)
            .forEach(p -> p.sendSystemMessage(Component.literal("§d[Blob] §f" + text)));
    }

    private void attackNearestPlayer() {
        Player nearest = level().getNearestPlayer(this, 8.0);
        if (nearest != null) {
            doHurtTarget(getServerLevel(), nearest);
            sendMessageNearby("DO NGOC! Nhan dam nay di!");
        }
    }

    public Mood getCurrentMood() { return currentMood; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag, HolderLookup.Provider provider) {
        super.addAdditionalSaveData(tag, provider);
        tag.putString("mood", currentMood.name());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag, HolderLookup.Provider provider) {
        super.readAdditionalSaveData(tag, provider);
        if (tag.contains("mood")) currentMood = Mood.valueOf(tag.getString("mood").orElse("NEUTRAL"));
    }
}

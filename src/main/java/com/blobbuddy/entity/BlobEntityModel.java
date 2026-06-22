package com.blobbuddy.entity;

import com.blobbuddy.BlobBuddyMod;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class BlobEntityModel extends GeoModel<BlobEntity> {
    @Override
    public Identifier getModelResource(BlobEntity entity) {
        return Identifier.of(BlobBuddyMod.MOD_ID, "geo/blob.geo.json");
    }
    @Override
    public Identifier getTextureResource(BlobEntity entity) {
        return Identifier.of(BlobBuddyMod.MOD_ID, "textures/entity/blob_neutral.png");
    }
    @Override
    public Identifier getAnimationResource(BlobEntity entity) {
        return Identifier.of(BlobBuddyMod.MOD_ID, "animations/blob.animation.json");
    }
}

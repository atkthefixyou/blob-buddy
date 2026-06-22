package com.blobbuddy.entity;

import com.blobbuddy.BlobBuddyMod;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoRenderState;
import net.minecraft.resources.Identifier;

public class BlobEntityModel extends GeoModel<BlobEntity> {
    @Override
    public Identifier getModelResource(BlobEntity entity) {
        return Identifier.of(BlobBuddyMod.MOD_ID, "geo/blob.geo.json");
    }
    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.of(BlobBuddyMod.MOD_ID, "textures/entity/blob_neutral.png");
    }
    @Override
    public Identifier getAnimationResource(BlobEntity entity) {
        return Identifier.of(BlobBuddyMod.MOD_ID, "animations/blob.animation.json");
    }
}

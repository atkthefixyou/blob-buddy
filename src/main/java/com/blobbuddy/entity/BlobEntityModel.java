package com.blobbuddy.entity;

import com.blobbuddy.BlobBuddyMod;
import com.geckolib.model.GeoModel;
import net.minecraft.resources.ResourceLocation;

public class BlobEntityModel extends GeoModel<BlobEntity> {
    @Override
    public ResourceLocation getModelResource(BlobEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(BlobBuddyMod.MOD_ID, "geo/blob.geo.json");
    }
    @Override
    public ResourceLocation getTextureResource(BlobEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(BlobBuddyMod.MOD_ID, "textures/entity/blob_neutral.png");
    }
    @Override
    public ResourceLocation getAnimationResource(BlobEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(BlobBuddyMod.MOD_ID, "animations/blob.animation.json");
    }
}

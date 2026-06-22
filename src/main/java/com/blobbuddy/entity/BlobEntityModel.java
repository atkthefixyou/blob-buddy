package com.blobbuddy.entity;

import com.blobbuddy.BlobBuddyMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

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

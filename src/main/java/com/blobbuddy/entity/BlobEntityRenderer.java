package com.blobbuddy.entity;

import com.blobbuddy.BlobBuddyMod;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BlobEntityRenderer extends GeoEntityRenderer<BlobEntity> {
    public BlobEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new BlobEntityModel());
    }

    @Override
    public ResourceLocation getTextureLocation(BlobEntity entity) {
        String name = switch (entity.getCurrentMood()) {
            case HAPPY   -> "blob_happy";
            case ANGRY   -> "blob_angry";
            case SAD     -> "blob_sad";
            case EXCITED -> "blob_excited";
            default      -> "blob_neutral";
        };
        return ResourceLocation.fromNamespaceAndPath(BlobBuddyMod.MOD_ID, "textures/entity/" + name + ".png");
    }
}

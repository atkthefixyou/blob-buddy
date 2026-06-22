package com.blobbuddy.entity;

import com.blobbuddy.BlobBuddyMod;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class BlobEntityRenderer extends GeoEntityRenderer<BlobEntity, GeoRenderState> {
    public BlobEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new BlobEntityModel());
    }
    @Override
    public Identifier getTextureLocation(GeoRenderState state) {
        BlobEntity entity = (BlobEntity) state.entity();
        String name = switch (entity.getCurrentMood()) {
            case HAPPY   -> "blob_happy";
            case ANGRY   -> "blob_angry";
            case SAD     -> "blob_sad";
            case EXCITED -> "blob_excited";
            default      -> "blob_neutral";
        };
        return Identifier.of(BlobBuddyMod.MOD_ID, "textures/entity/" + name + ".png");
    }
}

package com.blobbuddy.entity;

import com.blobbuddy.BlobBuddyMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class BlobEntityRenderer extends MobRenderer<BlobEntity, LivingEntityRenderState, BlobEntityModel> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
        Identifier.fromNamespaceAndPath(BlobBuddyMod.MOD_ID, "blob"), "main");

    public BlobEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new BlobEntityModel(ctx.bakeLayer(LAYER)), 0.4f);
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return Identifier.fromNamespaceAndPath(BlobBuddyMod.MOD_ID, "textures/entity/blob_neutral.png");
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }
}

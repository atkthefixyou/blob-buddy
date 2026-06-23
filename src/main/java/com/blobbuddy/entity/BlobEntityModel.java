package com.blobbuddy.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class BlobEntityModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart body;

    public BlobEntityModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-5f, -5f, -5f, 10, 10, 10),
            PartPose.offset(0f, 19f, 0f)
        );
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        body.y = 19f + (float)(Math.sin(System.currentTimeMillis() / 500.0) * 0.5);
    }
}

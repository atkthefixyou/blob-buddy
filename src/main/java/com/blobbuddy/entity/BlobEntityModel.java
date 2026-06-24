package com.blobbuddy.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class BlobEntityModel extends EntityModel<BlobEntity> {
    private final ModelPart body;

    public BlobEntityModel(ModelPart root) {
        this.body = root.getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(0, 0).addBox(-5f, -5f, -5f, 10, 10, 10),
            PartPose.offset(0f, 19f, 0f));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(BlobEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        body.y = 19f + (float)(Math.sin(ageInTicks * 0.05f) * 0.5f);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer,
                               int packedLight, int packedOverlay, float red,
                               float green, float blue, float alpha) {
        body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}

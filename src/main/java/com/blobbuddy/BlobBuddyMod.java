package com.blobbuddy;

import com.blobbuddy.entity.BlobEntity;
import com.blobbuddy.network.BlobPackets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class BlobBuddyMod implements ModInitializer {
    public static final String MOD_ID = "blob-buddy";
    public static EntityType<BlobEntity> BLOB_ENTITY;

    @Override
    public void onInitialize() {
        BLOB_ENTITY = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(MOD_ID, "blob"),
            FabricEntityTypeBuilder.create(MobCategory.MISC, BlobEntity::new)
                .dimensions(EntityDimensions.scalable(0.8f, 0.8f))
                .build()
        );
        BlobPackets.registerServerPackets();
    }
}

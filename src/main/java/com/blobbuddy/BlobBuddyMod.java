package com.blobbuddy;

import com.blobbuddy.entity.BlobEntity;
import com.blobbuddy.network.BlobPackets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class BlobBuddyMod implements ModInitializer {
    public static final String MOD_ID = "blob-buddy";
    public static EntityType<BlobEntity> BLOB_ENTITY;

    @Override
    public void onInitialize() {
        var key = ResourceKey.create(
            BuiltInRegistries.ENTITY_TYPE.key(),
            Identifier.of(MOD_ID, "blob")
        );
        BLOB_ENTITY = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            key,
            FabricEntityTypeBuilder.create(MobCategory.MISC, BlobEntity::new)
                .dimensions(EntityDimensions.scalable(0.8f, 0.8f))
                .build(key)
        );
        BlobPackets.registerServerPackets();
    }
}

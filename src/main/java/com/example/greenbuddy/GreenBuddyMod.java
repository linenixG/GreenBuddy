package com.example.greenbuddy;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreenBuddyMod implements ModInitializer {
    public static final String MOD_ID = "greenbuddy";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final EntityType<GreenBuddyEntity> GREEN_BUDDY = registerEntity();

    private static EntityType<GreenBuddyEntity> registerEntity() {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "green_buddy"));
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                key,
                EntityType.Builder.of(GreenBuddyEntity::new, MobCategory.CREATURE)
                        .sized(0.6F, 1.8F)
                        .build(key)
        );
    }

    @Override
    public void onInitialize() {
        FabricDefaultAttributeRegistry.register(GREEN_BUDDY, GreenBuddyEntity.createAttributes());
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            SpawnCommand.register(dispatcher);
            BuddyCommand.register(dispatcher);
        });
        LOGGER.info("Green Buddy Mod loaded!");
    }
}

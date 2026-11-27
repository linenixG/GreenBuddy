package com.example.greenbuddy.client;

import com.example.greenbuddy.GreenBuddyMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRenderers;

@Environment(EnvType.CLIENT)
public class GreenBuddyClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRenderers.register(GreenBuddyMod.GREEN_BUDDY, GreenBuddyRenderer::new);
    }
}

package com.example.greenbuddy.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class GreenBuddyClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Рендерер отключен, игра будет использовать стандартную модель зомби для нашего моба
    }
}

package com.example.greenbuddy.client;

import com.example.greenbuddy.GreenBuddyEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class GreenBuddyRenderer extends MobRenderer<GreenBuddyEntity, HumanoidModel<GreenBuddyEntity>> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("greenbuddy", "textures/entity/green_buddy.png");

    public GreenBuddyRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    // В 1.21.11 это НЕ @Override, а просто переопределение через базовый класс
    public Identifier getTextureLocation(GreenBuddyEntity entity) {
        return TEXTURE;
    }
}

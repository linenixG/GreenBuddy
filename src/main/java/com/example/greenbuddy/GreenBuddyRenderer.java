package com.example.greenbuddy.client;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import com.example.greenbuddy.GreenBuddyEntity;

public class GreenBuddyRenderer extends MobRenderer<GreenBuddyEntity, PlayerModel<GreenBuddyEntity>> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("greenbuddy", "textures/entity/green_buddy.png");

    public GreenBuddyRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public Identifier getTextureLocation(GreenBuddyEntity entity) {
        return TEXTURE;
    }
}

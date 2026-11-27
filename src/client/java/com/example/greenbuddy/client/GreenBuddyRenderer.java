package com.example.greenbuddy.client;

import com.example.greenbuddy.GreenBuddyEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.Identifier;

public class GreenBuddyRenderer extends MobRenderer<GreenBuddyEntity, PlayerModel<GreenBuddyEntity>> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("greenbuddy", "textures/entity/green_buddy.png");

    public GreenBuddyRenderer(EntityRendererProvider.Context context) {
        // Передаем ТРЕТИЙ аргумент — тень (0.5f)
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
        
        // Добавляем слои (чтобы не было ошибок с руками и головой)
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
    }

    @Override
    public Identifier getTextureLocation(GreenBuddyEntity entity) {
        return TEXTURE;
    }
}

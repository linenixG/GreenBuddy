package com.example.greenbuddy;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;

public class BuddyCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("buddy")
            .then(Commands.literal("attack").executes(context -> {
                executeForBuddies(context.getSource(), buddy -> { buddy.isAggressive = true; buddy.isFollowing = false; });
                context.getSource().sendSuccess(() -> Component.literal("Buddy: АТАКА!"), false);
                return 1;
            }))
            .then(Commands.literal("stop").executes(context -> {
                executeForBuddies(context.getSource(), buddy -> { buddy.isAggressive = false; buddy.isFollowing = false; buddy.guardPos = null; buddy.setTarget(null); });
                context.getSource().sendSuccess(() -> Component.literal("Buddy: Стоять."), false);
                return 1;
            }))
            .then(Commands.literal("follow").executes(context -> {
                executeForBuddies(context.getSource(), buddy -> { buddy.isAggressive = false; buddy.isFollowing = true; buddy.guardPos = null; });
                context.getSource().sendSuccess(() -> Component.literal("Buddy: Иду за тобой!"), false);
                return 1;
            }))
            .then(Commands.literal("build").executes(context -> {
                executeForBuddies(context.getSource(), buddy -> { buddy.isBuilding = true; buddy.getNavigation().stop(); });
                context.getSource().sendSuccess(() -> Component.literal("Buddy: Строю дом!"), false);
                return 1;
            }))
            .then(Commands.literal("guard").executes(context -> {
                executeForBuddies(context.getSource(), buddy -> { buddy.guardPos = buddy.blockPosition(); buddy.isAggressive = true; buddy.isFollowing = false; });
                context.getSource().sendSuccess(() -> Component.literal("Buddy: Охраняю эту точку!"), false);
                return 1;
            }))
            .then(Commands.literal("heal").executes(context -> {
                executeForBuddies(context.getSource(), GreenBuddyEntity::healBuddy);
                context.getSource().sendSuccess(() -> Component.literal("Buddy: Вылечен!"), false);
                return 1;
            }))
            .then(Commands.literal("stats").executes(context -> {
                executeForBuddies(context.getSource(), buddy -> {
                    context.getSource().sendSuccess(() -> Component.literal("Дерева нарублено: " + buddy.woodCollected), false);
                });
                return 1;
            }))
        );
    }

    private static void executeForBuddies(CommandSourceStack source, java.util.function.Consumer<GreenBuddyEntity> action) {
        var player = source.getPlayer();
        if (player != null) {
            var buddies = player.level().getEntitiesOfClass(GreenBuddyEntity.class, new AABB(player.blockPosition()).inflate(20));
            for (GreenBuddyEntity buddy : buddies) {
                action.accept(buddy);
            }
        }
    }
}

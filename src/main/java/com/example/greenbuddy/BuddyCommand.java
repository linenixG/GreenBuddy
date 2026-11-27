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
                var player = context.getSource().getPlayer();
                if (player != null) {
                    var buddies = player.level().getEntitiesOfClass(GreenBuddyEntity.class, new AABB(player.blockPosition()).inflate(10));
                    for (GreenBuddyEntity buddy : buddies) buddy.isAggressive = true;
                    context.getSource().sendSuccess(() -> Component.literal("Buddy is now aggressive!"), false);
                }
                return 1;
            }))
            .then(Commands.literal("stop").executes(context -> {
                var player = context.getSource().getPlayer();
                if (player != null) {
                    var buddies = player.level().getEntitiesOfClass(GreenBuddyEntity.class, new AABB(player.blockPosition()).inflate(10));
                    for (GreenBuddyEntity buddy : buddies) {
                        buddy.isAggressive = false;
                        buddy.setTarget(null);
                    }
                    context.getSource().sendSuccess(() -> Component.literal("Buddy stopped!"), false);
                }
                return 1;
            }))
        );
    }
}

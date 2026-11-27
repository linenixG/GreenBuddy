package com.example.greenbuddy;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SpawnCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spawnbuddy").executes(context -> {
            GreenBuddyEntity buddy = new GreenBuddyEntity(GreenBuddyMod.GREEN_BUDDY, context.getSource().getLevel());
            buddy.setPos(context.getSource().getPosition().x, context.getSource().getPosition().y, context.getSource().getPosition().z);
            context.getSource().getLevel().addFreshEntity(buddy);
            context.getSource().sendSuccess(() -> Component.literal("Green Buddy spawned! (Combat Mode)"), false);
            return 1;
        }));
    }
}

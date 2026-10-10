package com.worldbattles.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;

/** Server-authoritative teleports; preserves player inventory and game mode. */
public final class TeleportCommands {
    private TeleportCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spawn")
            .executes(ctx -> teleport(ctx.getSource(), "minecraft:overworld", 23, -58, 18, "Spawn")));
        dispatcher.register(Commands.literal("freelands")
            .executes(ctx -> teleport(ctx.getSource(), "secondworld:overworld", -541, 120, 1209, "Freelands")));
        dispatcher.register(Commands.literal("minage")
            .executes(ctx -> teleport(ctx.getSource(), "minage:overworld", -541, 120, 1209, "Minage")));
    }

    private static int teleport(CommandSourceStack source, String dimension,
                                double x, double y, double z, String label) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ResourceKey<net.minecraft.world.level.Level> dimensionKey = ResourceKey.create(
            Registries.DIMENSION, new ResourceLocation(dimension));
        ServerLevel destination = source.getServer().getLevel(dimensionKey);
        if (destination == null) {
            source.sendFailure(Component.literal("[WorldBattles] Dimension introuvable : " + dimension
                + ". Verifie le datapack des dimensions."));
            return 0;
        }
        // No commands, game-mode changes or inventory operations are performed.
        player.teleportTo(destination, x, y, z, Collections.emptySet(), 0.0F, 0.0F);
        player.sendSystemMessage(Component.literal("[WorldBattles] Teleporte vers " + label + "."));
        return 1;
    }
}

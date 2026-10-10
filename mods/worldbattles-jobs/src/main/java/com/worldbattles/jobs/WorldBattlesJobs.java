package com.worldbattles.jobs;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;

/** Standalone, read-only display mod. Never executes or replaces datapack functions. */
@Mod(WorldBattlesJobs.MODID)
public final class WorldBattlesJobs {
    public static final String MODID = "worldbattlesjobs";

    public WorldBattlesJobs() {
        JobsNetwork.register();
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("metiers").executes(ctx -> open(ctx.getSource().getPlayerOrException())));
        dispatcher.register(Commands.literal("wbmetiers").executes(ctx -> open(ctx.getSource().getPlayerOrException())));
        dispatcher.register(Commands.literal("jobs").executes(ctx -> open(ctx.getSource().getPlayerOrException())));
    }

    private int open(ServerPlayer player) {
        JobsNetwork.sendSnapshot(player);
        return 1;
    }
}

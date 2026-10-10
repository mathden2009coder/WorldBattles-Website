package com.worldbattles.commands;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;

/** Dedicated server commands. No datapack, inventory or game mode changes. */
@Mod(WorldBattlesCommands.MODID)
public final class WorldBattlesCommands {
    public static final String MODID = "worldbattlescommands";

    public WorldBattlesCommands() {
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        TeleportCommands.register(event.getDispatcher());
        JobsPauseCommands.register(event.getDispatcher());
    }
}

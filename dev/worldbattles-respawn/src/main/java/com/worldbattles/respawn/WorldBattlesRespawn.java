package com.worldbattles.respawn;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

/**
 * Faithful migration of SandWars RespawnParGroupe v7.
 * Does not transport, save, clear or restore inventory.
 * Only updates spawnpoint when current desired target differs.
 * Does not teleport after death (important for Dimension Localized Inventories).
 */
@Mod(WorldBattlesRespawn.MOD_ID)
public final class WorldBattlesRespawn {
    public static final String MOD_ID = "worldbattlesrespawn";
    private static final ResourceLocation SECOND_ID = new ResourceLocation("secondworld", "overworld");
    private static final ResourceLocation SECOND_NETHER_ID = new ResourceLocation("secondworld", "the_nether");
    private static final ResourceLocation MINAGE_ID = new ResourceLocation("minage", "overworld");
    private static final ResourceLocation MINAGE_NETHER_ID = new ResourceLocation("minage", "the_nether");
    private static final ResourceKey<Level> SECOND = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, SECOND_ID);
    private static final BlockPos VANILLA_SPAWN = new BlockPos(23, -58, 18);
    private static final BlockPos SECOND_SPAWN = new BlockPos(-541, 120, 1209);

    public WorldBattlesRespawn() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (!player.isAlive()) return;
        ResourceKey<Level> current = player.level().dimension();
        ResourceKey<Level> destination;
        BlockPos target;
        if (current.equals(Level.OVERWORLD) || current.equals(Level.NETHER) || current.equals(Level.END)) {
            destination = Level.OVERWORLD;
            target = VANILLA_SPAWN;
        } else if (current.equals(SECOND) || current.location().equals(SECOND_NETHER_ID)
                || current.location().equals(MINAGE_ID) || current.location().equals(MINAGE_NETHER_ID)) {
            destination = SECOND;
            target = SECOND_SPAWN;
        } else {
            return;
        }

        // The v7 datapack overwrites beds. Match that behavior while avoiding a
        // spawnpoint command (and gamerule/command overhead) every server tick.
        if (!Objects.equals(destination, player.getRespawnDimension())
                || !Objects.equals(target, player.getRespawnPosition())) {
            player.setRespawnPosition(destination, target, 0.0F, true, false);
        }
    }
}

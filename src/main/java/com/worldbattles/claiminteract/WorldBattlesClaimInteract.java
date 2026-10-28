package com.worldbattles.claiminteract;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Server-side interaction exception for configured WorldBattles spawn regions. */
@Mod(WorldBattlesClaimInteract.MODID)
public final class WorldBattlesClaimInteract {
    public static final String MODID = "worldbattlesclaiminteract";
    private WorldBattlesClaimInteract() {}

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class Events {
        private Events() {}

        @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
        public static void allowSpawnInteractions(PlayerInteractEvent.RightClickBlock event) {
            if (event.getEntity() == null || event.getLevel().isClientSide()) return;
            Level level = event.getLevel();
            BlockPos pos = event.getPos();
            if (!isAllowedZone(level, pos)) return;
            BlockState state = level.getBlockState(pos);
            if (!isAllowedBlock(level, pos, state)) return;

            // Clear claim cancellation only for the specifically allowed blocks/regions.
            event.setUseBlock(Event.Result.ALLOW);
            event.setCanceled(false);
        }
    }

    private static boolean isAllowedZone(Level level, BlockPos pos) {
        String dimension = level.dimension().location().toString();
        long x = pos.getX(), z = pos.getZ();
        if ("minecraft:overworld".equals(dimension))
            return Math.abs(x - 23L) <= 1000L && Math.abs(z - 18L) <= 1000L;
        if ("secondworld:overworld".equals(dimension) || "minage:overworld".equals(dimension))
            return Math.abs(x + 541L) <= 500L && Math.abs(z - 1209L) <= 500L;
        return false;
    }

    private static boolean isAllowedBlock(Level level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof Container) return true;
        return state.is(BlockTags.BUTTONS) || state.is(Blocks.LEVER);
    }
}
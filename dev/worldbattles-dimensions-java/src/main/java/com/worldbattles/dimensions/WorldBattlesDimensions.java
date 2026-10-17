package com.worldbattles.dimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.Optional;

/**
 * Fallback for Server Sided Portals 2.1.0, whose right-click handler tries Axis.X only.
 * Only activates correctly formed empty rectangular portals in their intended dimensions.
 * The original portal mod continues to handle travel, shape validity and destination.
 */
@Mod(WorldBattlesDimensions.MOD_ID)
public final class WorldBattlesDimensions {
    public static final String MOD_ID = "worldbattlesdimensions";
    private static final ResourceLocation SECONDWORLD = new ResourceLocation("secondworld", "overworld");
    private static final ResourceLocation MINAGE = new ResourceLocation("minage", "overworld");

    public WorldBattlesDimensions() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onIgnite(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (event.getEntity().isSpectator()) return;
        if (!event.getItemStack().is(Items.FLINT_AND_STEEL)) return;

        ResourceLocation dimension = level.dimension().location();
        Block expectedFrame;
        if (dimension.equals(SECONDWORLD)) expectedFrame = Blocks.AMETHYST_BLOCK;
        else if (dimension.equals(MINAGE)) expectedFrame = Blocks.COBBLESTONE;
        else return;

        // Do not change portal mechanics for arbitrary blocks or other worlds.
        BlockPos framePos = event.getPos();
        if (!level.getBlockState(framePos).is(expectedFrame)) return;
        BlockPos adjacent = framePos.relative(event.getFace());
        if (!level.hasChunkAt(adjacent)) return;
        // Axis.X remains handled by Server Sided Portals. Its implementation
        // does not try the rotated Axis.Z on frame interaction.
        Optional<PortalShape> rotated = PortalShape.findEmptyPortalShape(level, adjacent, Direction.Axis.Z);
        if (rotated.isEmpty()) return;

        rotated.get().createPortalBlocks();
        if (!event.getEntity().getAbilities().instabuild) {
            event.getItemStack().hurtAndBreak(1, event.getEntity(),
                player -> player.broadcastBreakEvent(event.getHand()));
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
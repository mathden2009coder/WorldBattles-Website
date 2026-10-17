package com.worldbattles.hammers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Native Forge break handler for existing SandWars hammers.
 * Never edits blocks directly: each secondary break goes through
 * ServerPlayerGameMode.destroyBlock, preserving Forge protection events.
 */
@Mod(WorldBattlesHammers.MOD_ID)
public final class WorldBattlesHammers {
    public static final String MOD_ID = "worldbattleshammers";
    private final Set<UUID> harvesting = new HashSet<>();

    public WorldBattlesHammers() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void breakArea(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) return;
        if (player.isCreative() || player.isSpectator() || !player.isAlive()) return;
        ItemStack tool = player.getMainHandItem();
        CompoundTag tag = tool.getTag();
        if (tag == null || !"hammer".equals(tag.getString("type"))) return;
        if (!tag.contains("sandwars_tier") && !tag.contains("sandwars_weak")) return;
        BlockPos origin = event.getPos();
        BlockState original = event.getState();
        if (!original.is(BlockTags.MINEABLE_WITH_PICKAXE)) return;
        if (!player.hasCorrectToolForDrops(original) || original.getDestroySpeed(level,origin) < 0) return;
        UUID id = player.getUUID();
        if (!harvesting.add(id)) return;
        try {
            HitResult trace = player.pick(6.0, 0.0f, false);
            Direction face = trace instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(origin)
                ? blockHit.getDirection() : Direction.getNearest(player.getLookAngle().x, player.getLookAngle().y, player.getLookAngle().z);
            for (int a=-1;a<=1;a++) {
                for (int b=-1;b<=1;b++) {
                    if (a==0 && b==0) continue;
                    BlockPos pos;
                    switch(face.getAxis()) {
                        case Y: pos=origin.offset(a,0,b); break;
                        case X: pos=origin.offset(0,a,b); break;
                        default: pos=origin.offset(a,b,0); break;
                    }
                    if (!level.hasChunkAt(pos)) continue;
                    BlockState state=level.getBlockState(pos);
                    if (state.isAir() || state.hasBlockEntity()) continue;
                    if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE) || !player.hasCorrectToolForDrops(state)) continue;
                    if (state.getDestroySpeed(level,pos)<0) continue;
                    player.gameMode.destroyBlock(pos);
                }
            }
        } finally {
            harvesting.remove(id);
        }
    }
}

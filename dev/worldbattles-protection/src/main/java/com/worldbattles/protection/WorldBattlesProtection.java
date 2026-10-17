package com.worldbattles.protection;

import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** ALPHA: player building is forbidden in arenas; automatic arena map restores remain unaffected. */
@Mod(WorldBattlesProtection.MOD_ID)
public final class WorldBattlesProtection {
    public static final String MOD_ID = "worldbattlesprotection";
    private static final int CENTER_X = -541;
    private static final int CENTER_Z = 1209;
    private static final int RADIUS = 500;
    private static final ResourceLocation OVERWORLD = new ResourceLocation("minecraft", "overworld");
    private static final ResourceLocation SECOND = new ResourceLocation("secondworld", "overworld");
    private static final ResourceLocation MINAGE = new ResourceLocation("minage", "overworld");


    // Only SecondWorld and Minage control game mode; Arenas in the vanilla Overworld own theirs.
    private final Map<UUID, Boolean> lastInside = new HashMap<>();

    public WorldBattlesProtection() { MinecraftForge.EVENT_BUS.register(this); }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        UUID id = player.getUUID();
        ResourceLocation dim = player.level().dimension().location();
        if (!dim.equals(SECOND) && !dim.equals(MINAGE)) {
            lastInside.remove(id);
            return;
        }
        if (exempt(player)) {
            lastInside.remove(id);
            return;
        }
        boolean inside = protectedAt(player, player.blockPosition());
        Boolean previous = lastInside.put(id, inside);
        if (previous != null && previous == inside) return;
        // At first observation, correct stale Adventure mode outside the zone,
        // without modifying players in an unprotected dimension.
        if (inside) {
            if (player.gameMode.getGameModeForPlayer() == GameType.SURVIVAL)
                player.setGameMode(GameType.ADVENTURE);
        } else if (player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
            player.setGameMode(GameType.SURVIVAL);
        }
        if (previous != null && previous != inside) {
            player.displayClientMessage(inside
                ? Component.literal("🛡 Vous entrez dans une zone protégée !").withStyle(ChatFormatting.GREEN)
                : Component.literal("⚔ Vous quittez la zone protégée !").withStyle(ChatFormatting.RED), true);
        }
    }
    @SubscribeEvent public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { lastInside.remove(event.getEntity().getUUID()); }
    @SubscribeEvent public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) { lastInside.remove(event.getEntity().getUUID()); }

    private static boolean protectedAt(Player player, BlockPos pos) {
        ResourceLocation dimension = player.level().dimension().location();
        if (dimension.equals(OVERWORLD)) return true;
        if (!dimension.equals(SECOND) && !dimension.equals(MINAGE)) return false;
        return Math.abs((long) pos.getX() - CENTER_X) <= RADIUS && Math.abs((long) pos.getZ() - CENTER_Z) <= RADIUS;
    }
    private static boolean exempt(Player player) { return player.isCreative() || player.isSpectator(); }
    @SubscribeEvent public void onBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && !exempt(player) && protectedAt(player, event.getPos()))
            event.setCanceled(true);
    }
    @SubscribeEvent public void onPlace(BlockEvent.EntityPlaceEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ServerPlayer player && !exempt(player) && protectedAt(player, event.getPos()))
            event.setCanceled(true);
    }
    @SubscribeEvent public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || exempt(player) || !protectedAt(player, event.getPos())) return;
        if (event.getItemStack().getItem() instanceof BlockItem || event.getItemStack().getItem() instanceof BucketItem)
            event.setUseItem(net.minecraftforge.eventbus.api.Event.Result.DENY);
    }
    @SubscribeEvent public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || exempt(player) || !protectedAt(player, player.blockPosition())) return;
        if (event.getItemStack().getItem() instanceof BucketItem) event.setCanceled(true);
    }
    private static boolean noDamage(ServerPlayer player) {
        ResourceLocation dim = player.level().dimension().location();
        return (dim.equals(SECOND) || dim.equals(MINAGE))
                && protectedAt(player, player.blockPosition()) && !exempt(player);
    }
    @SubscribeEvent public void onHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && noDamage(player))
            event.setCanceled(true);
    }
    @SubscribeEvent public void onAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && noDamage(player))
            event.setCanceled(true);
    }
}
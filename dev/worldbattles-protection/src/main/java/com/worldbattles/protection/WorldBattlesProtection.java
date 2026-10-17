package com.worldbattles.protection;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** ALPHA: Arenas 0.4.1 does not yet grant permissions; NOT FOR LIVE SERVER. */
@Mod(WorldBattlesProtection.MOD_ID)
public final class WorldBattlesProtection {
    public static final String MOD_ID = "worldbattlesprotection";
    private static final int CENTER_X = -541;
    private static final int CENTER_Z = 1209;
    private static final int RADIUS = 500;
    private static final ResourceLocation OVERWORLD = new ResourceLocation("minecraft", "overworld");
    private static final ResourceLocation SECOND = new ResourceLocation("secondworld", "overworld");
    private static final ResourceLocation MINAGE = new ResourceLocation("minage", "overworld");
    private static final Map<UUID, Integer> ARENA_BUILDERS = new HashMap<>();
    private static final int MAX_GRANT_TICKS = 20 * 60 * 15;
    public static void grantArenaBuilding(ServerPlayer player, int ticks) {
        if (!player.level().dimension().location().equals(OVERWORLD)) return;
        if (ticks <= 0) { revokeArenaBuilding(player); return; }
        ARENA_BUILDERS.put(player.getUUID(), player.tickCount + Math.min(ticks, MAX_GRANT_TICKS));
    }
    public static void revokeArenaBuilding(ServerPlayer player) { ARENA_BUILDERS.remove(player.getUUID()); }
    public static void setArenaBuildingAllowed(ServerPlayer player, boolean allowed) {
        if (allowed) grantArenaBuilding(player, MAX_GRANT_TICKS);
        else revokeArenaBuilding(player);
    }
    private static boolean canBuild(Player player) {
        if (exempt(player)) return true;
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        if (!player.level().dimension().location().equals(OVERWORLD)) return false;
        Integer until = ARENA_BUILDERS.get(player.getUUID());
        if (until == null) return false;
        if (serverPlayer.tickCount > until) {
            ARENA_BUILDERS.remove(player.getUUID());
            return false;
        }
        return true;
    }
    @SubscribeEvent public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        Integer until = ARENA_BUILDERS.get(player.getUUID());
        if (until != null && (player.tickCount > until || !player.level().dimension().location().equals(OVERWORLD)))
            ARENA_BUILDERS.remove(player.getUUID());
    }
    @SubscribeEvent public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { ARENA_BUILDERS.remove(event.getEntity().getUUID()); }
    @SubscribeEvent public void onRespawn(PlayerEvent.PlayerRespawnEvent event) { ARENA_BUILDERS.remove(event.getEntity().getUUID()); }
    @SubscribeEvent public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) { ARENA_BUILDERS.remove(event.getEntity().getUUID()); }
    public WorldBattlesProtection() { MinecraftForge.EVENT_BUS.register(this); }
    private static boolean protectedAt(Player player, BlockPos pos) {
        ResourceLocation dimension = player.level().dimension().location();
        if (dimension.equals(OVERWORLD)) return true;
        if (!dimension.equals(SECOND) && !dimension.equals(MINAGE)) return false;
        return Math.abs((long) pos.getX() - CENTER_X) <= RADIUS && Math.abs((long) pos.getZ() - CENTER_Z) <= RADIUS;
    }
    private static boolean exempt(Player player) { return player.isCreative() || player.isSpectator(); }
    @SubscribeEvent public void onBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && !canBuild(player) && protectedAt(player, event.getPos()))
            event.setCanceled(true);
    }
    @SubscribeEvent public void onPlace(BlockEvent.EntityPlaceEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ServerPlayer player && !canBuild(player) && protectedAt(player, event.getPos()))
            event.setCanceled(true);
    }
    @SubscribeEvent public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || canBuild(player) || !protectedAt(player, event.getPos())) return;
        if (event.getItemStack().getItem() instanceof BlockItem || event.getItemStack().getItem() instanceof BucketItem)
            event.setUseItem(net.minecraftforge.eventbus.api.Event.Result.DENY);
    }
    @SubscribeEvent public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || canBuild(player) || !protectedAt(player, player.blockPosition())) return;
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
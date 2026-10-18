package com.worldbattles.lobbypvp;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Exact WorldBattles lobby bounds, overworld only. */
@Mod(WorldBattlesLobbyPvp.MOD_ID)
public final class WorldBattlesLobbyPvp {
    public static final String MOD_ID = "worldbattleslobbypvp";
    public WorldBattlesLobbyPvp() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    private static boolean cancelDamage(net.minecraft.world.entity.LivingEntity target, DamageSource source) {
        if (!(target instanceof Player victim)) return false;
        if (victim.level().isClientSide || victim.level().dimension() != Level.OVERWORLD) return false;
        BlockPos p = victim.blockPosition();
        if (p.getX() < 0 || p.getX() > 31
                || p.getY() < -60 || p.getY() > -46
                || p.getZ() < 10 || p.getZ() > 40) return false;
        Entity attacker = source.getEntity();
        return attacker instanceof Player && attacker != victim;
    }
    @SubscribeEvent
    public void onAttack(LivingAttackEvent event) {
        if (cancelDamage(event.getEntity(), event.getSource())) event.setCanceled(true);
    }
    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        if (cancelDamage(event.getEntity(), event.getSource())) event.setCanceled(true);
    }
}

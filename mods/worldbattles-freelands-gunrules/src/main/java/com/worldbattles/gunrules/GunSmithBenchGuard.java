package com.worldbattles.gunrules;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Server-side Forge event fallback: unlike a Mixin, this event does not
 * depend on TaCZ method bytecode or Mixin bootstrap being loaded.
 */
public final class GunSmithBenchGuard {
    private static final Set<ResourceLocation> TACZ_BENCHES = Set.of(
        new ResourceLocation("tacz", "gun_smith_table"),
        new ResourceLocation("tacz", "workbench_a"),
        new ResourceLocation("tacz", "workbench_b"),
        new ResourceLocation("tacz", "workbench_c")
    );

    private static volatile Boolean selectiveMixinInstalled;

    /**
     * Detects whether Mixin actually transformed TaCZ's menu. Do not just
     * assume the presence of mixin JSON means it was applied.
     */
    public static boolean isSelectiveMixinInstalled() {
        Boolean cached = selectiveMixinInstalled;
        if (cached != null) return cached;
        synchronized (GunSmithBenchGuard.class) {
            if (selectiveMixinInstalled != null) return selectiveMixinInstalled;
            boolean found = false;
            try {
                Class<?> menu = Class.forName("com.tacz.guns.inventory.GunSmithTableMenu");
                for (Method method : menu.getDeclaredMethods()) {
                    if (method.getName().contains("worldbattles$blockFreelandsGunCraft")) {
                        found = true;
                        break;
                    }
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // If TaCZ or the injection cannot be verified, fail closed.
            }
            selectiveMixinInstalled = found;
            return found;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBenchUse(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!FreelandsGunRules.isFreelands(player.serverLevel().dimension().location())) return;

        BlockState state = event.getLevel().getBlockState(event.getPos());
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (!TACZ_BENCHES.contains(blockId)) return;

        // Strict mode guarantees the restriction even if Mixin fails to load.
        // When strict is disabled, only trust selective mode if the injection
        // can be confirmed in the transformed TaCZ menu class.
        if (!GunRulesConfig.STRICT_BENCH_BLOCK.get() && isSelectiveMixinInstalled()) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
        player.displayClientMessage(Component.literal(
            "§cEtabli TaCZ desactive dans Freelands : fabrication de fusils interdite."), true);
    }
}

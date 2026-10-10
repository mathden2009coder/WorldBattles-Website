package com.worldbattles.gunrules.mixin;

import com.worldbattles.gunrules.FreelandsGunRules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TaCZ's GunSmithTableMenu#doCraft is the server-authoritative craft path.
 * Cancel BEFORE TaCZ consumes ingredients or spawns the output.
 *
 * Only gun outputs are blocked. Ammo, attachments and workbench use remain
 * available in Freelands. Checks the player's CURRENT dimension at craft time,
 * even if a player opened the bench elsewhere before teleporting.
 */
@Mixin(targets = "com.tacz.guns.inventory.GunSmithTableMenu", remap = false)
public abstract class GunSmithCraftMixin {
    private static final ResourceLocation TACZ_GUN = new ResourceLocation("tacz", "modern_kinetic_gun");

    @Inject(method = "doCraft", at = @At("HEAD"), cancellable = true, remap = false)
    private void worldbattles$blockFreelandsGunCraft(ResourceLocation recipeId, Player player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!FreelandsGunRules.isFreelands(serverPlayer.level().dimension().location())) return;

        Recipe<?> recipe = serverPlayer.serverLevel().getRecipeManager().byKey(recipeId).orElse(null);
        if (recipe == null) return;
        ItemStack output = recipe.getResultItem(serverPlayer.level().registryAccess());
        if (output.isEmpty() || !TACZ_GUN.equals(BuiltInRegistries.ITEM.getKey(output.getItem()))) return;

        ci.cancel();
        serverPlayer.displayClientMessage(
            Component.literal("§cFabrication des fusils TaCZ interdite dans Freelands."), true);
    }
}

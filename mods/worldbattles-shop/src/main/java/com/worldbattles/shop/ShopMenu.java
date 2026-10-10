package com.worldbattles.shop;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/** Custom menu protocol with server-authoritative, read-only action slots. */
public final class ShopMenu extends ChestMenu {
    private final ServerPlayer owner;
    private final Map<Integer, Consumer<ServerPlayer>> actions = new HashMap<>();
    private boolean clickHandled;

    public ShopMenu(int id, Inventory inventory) {
        super(ShopRegistry.SHOP_MENU.get(), id, inventory, new SimpleContainer(54), 6);
        // Client menus have a LocalPlayer. Never cast a client player to ServerPlayer.
        owner = inventory.player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
    }

    public void button(int slot, ItemStack icon, Consumer<ServerPlayer> action) {
        if (owner == null) throw new IllegalStateException("Only the server can populate shop actions");
        if (slot < 0 || slot >= 54) throw new IllegalArgumentException("Invalid menu slot");
        getSlot(slot).set(icon);
        if (action != null) actions.put(slot, action);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // Never call super.clicked. Client packets may request an action, never item movement.
        if (owner == null || clickHandled || player != owner || clickType != ClickType.PICKUP || button != 0) return;
        if (slotId < 0 || slotId >= 54) return;
        Consumer<ServerPlayer> action = actions.get(slotId);
        if (action == null) return;
        clickHandled = true;
        WorldBattlesShop.defer(() -> {
            if (owner.isAlive() && WorldBattlesShop.allowed(owner) && owner.containerMenu == this) action.accept(owner);
            else owner.closeContainer();
        });
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return owner == null || (player == owner && WorldBattlesShop.allowed(owner));
    }
}

package com.worldbattles.shop;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Dedicated menu type. Never override the appearance of ordinary Minecraft chests. */
public final class ShopRegistry {
    private ShopRegistry() {}

    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, WorldBattlesShop.MOD_ID);

    public static final RegistryObject<MenuType<ShopMenu>> SHOP_MENU =
        MENUS.register("shop", () -> IForgeMenuType.create((id, inventory, extraData) -> new ShopMenu(id, inventory)));
}

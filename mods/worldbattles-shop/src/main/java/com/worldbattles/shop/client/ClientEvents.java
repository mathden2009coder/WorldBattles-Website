package com.worldbattles.shop.client;

import com.worldbattles.shop.ShopRegistry;
import com.worldbattles.shop.WorldBattlesShop;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only screen registration. Never load this class on a dedicated server. */
@Mod.EventBusSubscriber(modid = WorldBattlesShop.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ShopRegistry.SHOP_MENU.get(), ShopScreen::new));
    }
}

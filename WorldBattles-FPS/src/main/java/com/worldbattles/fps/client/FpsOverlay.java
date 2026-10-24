package com.worldbattles.fps.client;

import com.worldbattles.fps.WorldBattlesFps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WorldBattlesFps.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class FpsOverlay {
    private static final int ORANGE = 0xFFFF8A1E;
    private static final int WHITE = 0xFFF5F5F5;
    private FpsOverlay() { }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;
        GuiGraphics graphics = event.getGuiGraphics();
        String label = "WORLDBATTLES";
        String fps = "FPS: " + mc.getFps();
        int x = 8;
        int y = graphics.guiHeight() - 18;
        graphics.drawString(mc.font, label, x, y - 10, ORANGE, false);
        graphics.drawString(mc.font, fps, x, y, WHITE, false);
    }
}

package com.worldbattles.shop.client;

import com.worldbattles.shop.ShopMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * A completely custom WorldBattles storefront, not a chest skin.
 * Dark navy dashboard + orange highlights follow the WorldBattles website palette.
 * All clicks still use vanilla container packets; the server validates every transaction.
 */
public final class ShopScreen extends AbstractContainerScreen<ShopMenu> {
    private static final int BACK = 0xFF060912;
    private static final int PANEL = 0xFF0C1320;
    private static final int PANEL_HOVER = 0xFF18283B;
    private static final int PANEL_ALT = 0xFF111B2B;
    private static final int BORDER = 0xFF26364C;
    private static final int ORANGE = 0xFFFF8419;
    private static final int ORANGE_LIGHT = 0xFFFFB149;
    private static final int TEXT = 0xFFF7F9FC;
    private static final int MUTED = 0xFFA8B2C3;
    private static final int GREEN = 0xFF7FE091;

    private static final int[] HOME_SLOTS = {11, 13, 15, 29, 31, 33, 40};
    private static final int[] NAV_SLOTS = {46, 47, 48, 49, 50, 51, 52};
    private static final String[] NAV_NAMES = {"MARCHE", "DEPOSER", "COLIS", "ACCUEIL", "FERMER", "VENDRE", "BANQUE"};
    private final List<Hitbox> hitboxes = new ArrayList<>();
    private ItemStack hoveredItem = ItemStack.EMPTY;
    private int panelX, panelY, panelW, panelH;

    private record Hitbox(int x, int y, int w, int h, int slot) {
        boolean contains(double px, double py) {
            return px >= x && px < x + w && py >= y && py < y + h;
        }
    }

    public ShopScreen(ShopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.inventoryLabelY = -1000; // Do not display the vanilla inventory label.
    }

    @Override
    protected void init() {
        this.imageWidth = Math.min(458, Math.max(160, this.width - 14));
        this.imageHeight = Math.min(294, Math.max(140, this.height - 12));
        super.init();
        panelW = imageWidth;
        panelH = imageHeight;
        panelX = leftPos;
        panelY = topPos;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Entire visual is drawn in render() to keep our hitboxes and artwork aligned.
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Suppress the standard chest title and player-inventory labels.
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        hitboxes.clear();
        hoveredItem = ItemStack.EMPTY;

        // Shadow, custom panel, subtle top glow and bright orange brand line.
        graphics.fill(panelX - 3, panelY - 3, panelX + panelW + 4, panelY + panelH + 4, 0xB0000000);
        graphics.fillGradient(panelX, panelY, panelX + panelW, panelY + panelH, BACK, PANEL);
        outline(graphics, panelX, panelY, panelW, panelH, BORDER);
        graphics.fill(panelX, panelY, panelX + panelW, panelY + 3, ORANGE);
        graphics.fillGradient(panelX + 1, panelY + 4, panelX + panelW - 1, panelY + 48,
            0xFF17243A, 0xFF0A101A);
        graphics.fill(panelX + 12, panelY + 13, panelX + 15, panelY + 36, ORANGE);
        graphics.drawString(font, "WORLDBATTLES", panelX + 22, panelY + 12, TEXT, false);
        graphics.drawString(font, "SHOP  /  ECONOMIE", panelX + 22, panelY + 27, ORANGE_LIGHT, false);

        String subtitle = title.getString();
        int subtitleWidth = Math.min(164, Math.max(60, panelW / 3));
        graphics.drawString(font, shorten(subtitle, subtitleWidth), panelX + panelW - subtitleWidth - 13,
            panelY + 17, MUTED, false);
        graphics.fill(panelX + 12, panelY + 46, panelX + panelW - 12, panelY + 47, BORDER);

        int contentTop = panelY + 53;
        int contentBottom = panelY + panelH - 42;
        if (isHome()) {
            drawHome(graphics, contentTop, contentBottom, mouseX, mouseY);
        } else if (isCatalog()) {
            drawCatalog(graphics, contentTop, contentBottom, mouseX, mouseY);
        } else {
            drawActions(graphics, contentTop, contentBottom, mouseX, mouseY);
        }
        drawNavigation(graphics, mouseX, mouseY);

        if (!hoveredItem.isEmpty()) {
            graphics.renderTooltip(font, hoveredItem, mouseX, mouseY);
        }
    }

    private boolean isHome() {
        return title.getString().contains("SHOP") && !title.getString().contains("BANQUE");
    }

    private boolean isCatalog() {
        String t = title.getString();
        return t.startsWith("Marche des joueurs") || t.startsWith("Mes annonces")
            || t.startsWith("Mes colis") || t.startsWith("Banque - joueurs")
            || t.equals("Vendre au serveur");
    }

    private void drawHome(GuiGraphics g, int y0, int y1, int mx, int my) {
        int gap = 7;
        int x0 = panelX + 13;
        int areaW = panelW - 26;
        int rows = 3;
        int cols = 3;
        int cellW = (areaW - (cols - 1) * gap) / cols;
        int cellH = Math.max(24, (y1 - y0 - (rows - 1) * gap) / rows);
        for (int i = 0; i < HOME_SLOTS.length; i++) {
            int slot = HOME_SLOTS[i];
            int x = x0 + (i % cols) * (cellW + gap);
            int y = y0 + (i / cols) * (cellH + gap);
            drawCard(g, slot, x, y, cellW, cellH, mx, my, true);
        }
        if (HOME_SLOTS.length < 9) {
            int x = x0 + (HOME_SLOTS.length % cols) * (cellW + gap);
            int y = y0 + (HOME_SLOTS.length / cols) * (cellH + gap);
            g.fill(x, y, x + cellW, y + cellH, 0x66111B2B);
            outline(g, x, y, cellW, cellH, 0xFF1B2C40);
            g.drawString(font, "WORLDBATTLES", x + 8, y + 8, MUTED, false);
            g.drawString(font, "ECONOMY", x + 8, y + 20, ORANGE, false);
        }
    }

    private void drawCatalog(GuiGraphics g, int y0, int y1, int mx, int my) {
        int cols = 6;
        int rows = 6;
        int gap = 3;
        int areaW = panelW - 26;
        int cellW = Math.max(17, (areaW - (cols - 1) * gap) / cols);
        int cellH = Math.max(17, (y1 - y0 - (rows - 1) * gap) / rows);
        for (int slot = 0; slot < 36; slot++) {
            int x = panelX + 13 + (slot % cols) * (cellW + gap);
            int y = y0 + (slot / cols) * (cellH + gap);
            ItemStack stack = menu.getSlot(slot).getItem();
            if (!stack.isEmpty()) drawCard(g, slot, x, y, cellW, cellH, mx, my, false);
            else {
                g.fill(x, y, x + cellW, y + cellH, 0x44111B2B);
                outline(g, x, y, cellW, cellH, 0xFF192638);
            }
        }
    }

    private void drawActions(GuiGraphics g, int y0, int y1, int mx, int my) {
        List<Integer> content = new ArrayList<>();
        for (int slot = 0; slot < 45; slot++) {
            if (!menu.getSlot(slot).getItem().isEmpty()) content.add(slot);
        }
        if (content.isEmpty()) {
            g.drawCenteredString(font, "Aucun element disponible", panelX + panelW / 2,
                y0 + (y1 - y0) / 2, MUTED);
            return;
        }
        int cols = content.size() > 9 ? 4 : 3;
        int rows = (content.size() + cols - 1) / cols;
        int gap = 5;
        int cellW = (panelW - 26 - gap * (cols - 1)) / cols;
        int cellH = Math.max(19, (y1 - y0 - gap * (rows - 1)) / rows);
        for (int i = 0; i < content.size(); i++) {
            int x = panelX + 13 + (i % cols) * (cellW + gap);
            int y = y0 + (i / cols) * (cellH + gap);
            drawCard(g, content.get(i), x, y, cellW, cellH, mx, my, true);
        }
    }

    private void drawCard(GuiGraphics g, int slot, int x, int y, int w, int h, int mx, int my, boolean labeled) {
        ItemStack stack = menu.getSlot(slot).getItem();
        if (stack.isEmpty()) return;
        boolean hover = mx >= x && mx < x + w && my >= y && my < y + h;
        g.fillGradient(x, y, x + w, y + h, hover ? PANEL_HOVER : PANEL_ALT, PANEL);
        outline(g, x, y, w, h, hover ? ORANGE : BORDER);
        if (hover) {
            g.fill(x + 1, y + 1, x + w - 1, y + 3, ORANGE);
            hoveredItem = stack;
        }
        int itemX = x + 6;
        int itemY = y + Math.max(3, (h - 16) / 2);
        g.renderItem(stack, itemX, itemY);
        g.renderItemDecorations(font, stack, itemX, itemY);
        if (labeled && w > 68) {
            String name = stack.getHoverName().getString();
            g.drawString(font, shorten(name, w - 29), x + 27,
                y + Math.max(4, (h - 9) / 2), hover ? ORANGE_LIGHT : TEXT, false);
        } else if (w > 48 && h > 27) {
            String name = stack.getHoverName().getString();
            g.drawString(font, shorten(name, w - 5), x + 4, y + h - 10, MUTED, false);
        }
        hitboxes.add(new Hitbox(x, y, w, h, slot));
    }

    private void drawNavigation(GuiGraphics g, int mx, int my) {
        int y = panelY + panelH - 34;
        g.fill(panelX + 1, y - 5, panelX + panelW - 1, panelY + panelH - 1, 0xFF0B1422);
        g.fill(panelX + 12, y - 6, panelX + panelW - 12, y - 5, BORDER);
        int x0 = panelX + 12;
        int gap = 3;
        int navW = (panelW - 24 - gap * 8) / 9;
        // Previous and next are separate controls; present only on paginated pages.
        drawNav(g, 45, "<", x0, y, navW, 28, mx, my);
        for (int i = 0; i < NAV_SLOTS.length; i++) {
            drawNav(g, NAV_SLOTS[i], NAV_NAMES[i], x0 + (i + 1) * (navW + gap),
                y, navW, 28, mx, my);
        }
        drawNav(g, 53, ">", x0 + 8 * (navW + gap), y, navW, 28, mx, my);
    }

    private void drawNav(GuiGraphics g, int slot, String label, int x, int y, int w, int h, int mx, int my) {
        ItemStack stack = menu.getSlot(slot).getItem();
        if (stack.isEmpty()) return;
        boolean hover = mx >= x && mx < x + w && my >= y && my < y + h;
        g.fill(x, y, x + w, y + h, hover ? 0xFF50321F : PANEL_ALT);
        outline(g, x, y, w, h, hover ? ORANGE : BORDER);
        g.renderItem(stack, x + (w - 16) / 2, y + 2);
        if (w > 32) {
            g.drawCenteredString(font, shorten(label, w - 2), x + w / 2, y + 19,
                hover ? ORANGE_LIGHT : MUTED);
        }
        if (hover) hoveredItem = stack;
        hitboxes.add(new Hitbox(x, y, w, h, slot));
    }

    private void outline(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private String shorten(String value, int maxPixels) {
        if (font.width(value) <= maxPixels) return value;
        if (maxPixels <= font.width("...")) return "";
        while (!value.isEmpty() && font.width(value + "...") > maxPixels)
            value = value.substring(0, value.length() - 1);
        return value + "...";
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || minecraft == null || minecraft.gameMode == null || minecraft.player == null)
            return true;
        for (Hitbox hit : hitboxes) {
            if (hit.contains(mouseX, mouseY)) {
                minecraft.gameMode.handleInventoryMouseClick(menu.containerId, hit.slot(), 0,
                    ClickType.PICKUP, minecraft.player);
                return true;
            }
        }
        // Do not forward clicks to AbstractContainerScreen: hidden vanilla slots are not interactive.
        return true;
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        return true;
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        return true;
    }
}

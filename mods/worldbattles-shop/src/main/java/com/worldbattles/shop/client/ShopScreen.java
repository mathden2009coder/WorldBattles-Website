package com.worldbattles.shop.client;

import com.worldbattles.shop.ShopMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * WorldBattles dashboard. This screen does not render the vanilla chest or inventory.
 * It sends only slot-action requests; the server owns the actual economy.
 */
public final class ShopScreen extends AbstractContainerScreen<ShopMenu> {
    private static final int BACK = 0xFF070C16;
    private static final int PANEL = 0xFF101B2B;
    private static final int CARD = 0xFF142237;
    private static final int HOVER = 0xFF21354B;
    private static final int BORDER = 0xFF31455D;
    private static final int ORANGE = 0xFFFF891F;
    private static final int LIGHT = 0xFFFFB65A;
    private static final int WHITE = 0xFFF5F7FC;
    private static final int MUTED = 0xFFA7B6C8;
    private static final int GREEN = 0xFF6ED6A0;
    private static final int RED = 0xFFFF8080;

    private static final int[] HOME = {11, 13, 15, 29, 31, 33, 40};
    private static final int[] NAV = {49, 46, 47, 51, 48, 52, 50};
    private static final String[] NAV_LABELS = {"ACCUEIL", "MARCHE", "DEPOSER", "VENDRE", "COLIS", "BANQUE", "QUITTER"};

    private final List<Hitbox> hits = new ArrayList<>();
    private ItemStack hovered = ItemStack.EMPTY;
    private int px, py, pw, ph;

    private record Hitbox(int x, int y, int width, int height, int slot) {
        boolean contains(double mx, double my) {
            return mx >= x && mx < x + width && my >= y && my < y + height;
        }
    }

    public ShopScreen(ShopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.inventoryLabelY = -1000;
    }

    @Override
    protected void init() {
        this.imageWidth = Math.min(520, Math.max(180, this.width - 12));
        this.imageHeight = Math.min(340, Math.max(175, this.height - 10));
        super.init();
        px = leftPos;
        py = topPos;
        pw = imageWidth;
        ph = imageHeight;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        // No chest texture. Every visible element is laid out below.
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // No vanilla chest/inventory labels.
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        hits.clear();
        hovered = ItemStack.EMPTY;

        g.fill(px - 3, py - 3, px + pw + 4, py + ph + 4, 0xA8000000);
        g.fillGradient(px, py, px + pw, py + ph, BACK, PANEL);
        border(g, px, py, pw, ph, BORDER);
        g.fill(px, py, px + pw, py + 3, ORANGE);
        g.fillGradient(px + 1, py + 4, px + pw - 1, py + 55, 0xFF1B2D45, BACK);
        g.fill(px + 12, py + 11, px + 15, py + 29, ORANGE);
        g.drawString(font, "WORLDBATTLES", px + 22, py + 11, WHITE, false);
        g.drawString(font, "SHOP", px + 22 + font.width("WORLDBATTLES") + 8, py + 11, LIGHT, false);

        if (isHome() && pw >= 345 && !menu.getSlot(44).getItem().isEmpty()) {
            String balance = menu.getSlot(44).getItem().getHoverName().getString();
            g.drawString(font, fit(balance, Math.min(145, pw / 3)), px + pw - 12 - Math.min(145, pw / 3),
                py + 11, LIGHT, false);
        }

        g.drawString(font, fit(section(), pw - 100), px + 13, py + 31, WHITE, false);
        if (pw > 320) {
            g.drawString(font, fit(hint(), pw - 101), px + 13, py + 44, MUTED, false);
        }
        g.fill(px + 12, py + 57, px + pw - 12, py + 58, BORDER);
        pageButton(g, 45, "<", px + pw - 65, py + 30, mouseX, mouseY);
        pageButton(g, 53, ">", px + pw - 37, py + 30, mouseX, mouseY);

        int top = py + 64;
        int bottom = py + ph - 44;
        if (isHome()) drawHome(g, top, bottom, mouseX, mouseY);
        else if (isCatalog()) drawCatalog(g, top, bottom, mouseX, mouseY);
        else if (isDraft()) drawDraft(g, top, bottom, mouseX, mouseY);
        else if (isDetails()) drawDetails(g, top, bottom, mouseX, mouseY);
        else if (isSellQuantity()) drawSellQuantity(g, top, bottom, mouseX, mouseY);
        else if (isBank()) drawBank(g, top, bottom, mouseX, mouseY);
        else drawOther(g, top, bottom, mouseX, mouseY);

        drawNavigation(g, mouseX, mouseY);
        if (!hovered.isEmpty()) g.renderTooltip(font, hovered, mouseX, mouseY);
    }

    private String page() { return title.getString(); }
    private boolean isHome() { return page().equals("WorldBattles | SHOP"); }
    private boolean isBank() { return page().equals("WorldBattles | BANQUE"); }
    private boolean isDraft() { return page().equals("Creer une annonce"); }
    private boolean isDetails() {
        return page().startsWith("Achat - annonce") || page().startsWith("Annuler l'annonce");
    }
    private boolean isSellQuantity() { return page().equals("Vente au serveur"); }
    private boolean isCatalog() {
        return page().startsWith("Marche des joueurs") || page().startsWith("Mes annonces")
            || page().startsWith("Mes colis") || page().startsWith("Banque - joueurs")
            || page().equals("Vendre au serveur");
    }

    private String section() {
        if (isHome()) return "BIENVENUE DANS LA BOUTIQUE";
        if (isBank()) return "MA BANQUE";
        if (isDraft()) return "CREER UNE ANNONCE";
        if (page().startsWith("Marche des joueurs")) return "MARCHE DES JOUEURS  " + pageNumber();
        if (page().startsWith("Mes annonces")) return "MES ANNONCES  " + pageNumber();
        if (page().startsWith("Mes colis")) return "MES LIVRAISONS  " + pageNumber();
        if (page().startsWith("Banque - joueurs")) return "CLASSEMENT DES SOLDES  " + pageNumber();
        if (page().equals("Vendre au serveur")) return "VENDRE DES RESSOURCES";
        if (isSellQuantity()) return "CHOISIR LA QUANTITE";
        if (page().startsWith("Achat -")) return "CONFIRMER MON ACHAT";
        if (page().startsWith("Annuler l'annonce")) return "RETIRER MON ANNONCE";
        return page().toUpperCase();
    }

    private String pageNumber() {
        String[] parts = page().split(" ");
        return "P." + parts[parts.length - 1];
    }

    private String hint() {
        if (isHome()) return "Acheter, vendre et gerer ton argent au meme endroit";
        if (isDraft()) return "Ajuste le prix et la quantite, puis confirme";
        if (page().startsWith("Marche")) return "Clique sur un objet pour voir le vendeur et le prix";
        if (page().startsWith("Mes annonces")) return "Clique sur une annonce pour la retirer";
        if (page().startsWith("Mes colis")) return "Clique sur un colis pour recuperer son contenu";
        if (page().startsWith("Banque - joueurs")) return "Soldes classes du plus grand au plus petit";
        if (page().equals("Vendre au serveur")) return "Clique sur une ressource pour choisir la quantite";
        if (isSellQuantity()) return "Le paiement est immediat";
        if (isDetails()) return "Verifie l'objet avant de confirmer";
        if (isBank()) return "Ton argent est conserve entre les connexions";
        return "Survole un element pour voir ses informations";
    }

    private void drawHome(GuiGraphics g, int top, int bottom, int mx, int my) {
        int gap = 6;
        int left = px + 13;
        int usable = pw - 26;
        int w = (usable - 2 * gap) / 3;
        int h = Math.max(22, (bottom - top - 2 * gap) / 3);
        for (int i = 0; i < HOME.length; i++) {
            int x = left + (i % 3) * (w + gap);
            int y = top + (i / 3) * (h + gap);
            tile(g, HOME[i], x, y, w, h, mx, my, true, ORANGE, true);
        }
        int noteX = left + w + gap;
        int noteY = top + 2 * (h + gap);
        int noteW = usable - w - gap;
        g.fill(noteX, noteY, noteX + noteW, noteY + h, 0x8820354C);
        border(g, noteX, noteY, noteW, h, BORDER);
        if (h >= 29) {
            g.drawString(font, fit("COMMENT CA MARCHE ?", noteW - 14), noteX + 7, noteY + 7, LIGHT, false);
            g.drawString(font, fit("Choisis une carte ou utilise le menu en bas.", noteW - 14),
                noteX + 7, noteY + 20, MUTED, false);
        }
    }

    private void drawCatalog(GuiGraphics g, int top, int bottom, int mx, int my) {
        int cols = 4;
        int rows = 6;
        int gap = 4;
        int w = (pw - 26 - (cols - 1) * gap) / cols;
        int h = Math.max(14, (bottom - top - (rows - 1) * gap) / rows);
        boolean clickable = !page().startsWith("Banque - joueurs");
        boolean any = false;
        for (int i = 0; i < 24; i++) {
            int x = px + 13 + (i % cols) * (w + gap);
            int y = top + (i / cols) * (h + gap);
            if (!menu.getSlot(i).getItem().isEmpty()) {
                tile(g, i, x, y, w, h, mx, my, true, ORANGE, clickable);
                any = true;
            }
        }
        if (!any) {
            String message = page().startsWith("Mes colis") ? "Aucun colis en attente"
                : page().startsWith("Mes annonces") ? "Tu n'as aucune annonce active"
                : page().startsWith("Marche") ? "Aucun objet en vente pour le moment"
                : "Aucun element disponible";
            g.drawCenteredString(font, fit(message, pw - 34), px + pw / 2,
                top + Math.max(8, (bottom - top) / 2), MUTED);
        }
    }

    private void drawDraft(GuiGraphics g, int top, int bottom, int mx, int my) {
        int gap = 5;
        int left = px + 13;
        int usable = pw - 26;
        int total = bottom - top;
        int hero = Math.max(27, total / 4);
        int adjust = Math.max(18, total / 6);
        int last = Math.max(21, total - hero - 2 * adjust - 3 * gap);
        int half = (usable - gap) / 2;
        tile(g, 13, left, top, half, hero, mx, my, true, ORANGE, false);
        tile(g, 22, left + half + gap, top, usable - half - gap, hero, mx, my, true, LIGHT, false);

        int[] plus = {10, 11, 12, 14};
        int[] minus = {19, 20, 21, 23};
        int stepW = (usable - 3 * gap) / 4;
        for (int i = 0; i < 4; i++) {
            int x = left + i * (stepW + gap);
            tile(g, plus[i], x, top + hero + gap, stepW, adjust, mx, my, false, GREEN, true);
            tile(g, minus[i], x, top + hero + adjust + 2 * gap, stepW, adjust, mx, my, false, RED, true);
        }
        int y = top + hero + 2 * adjust + 3 * gap;
        tile(g, 31, left, y, half, last, mx, my, true, LIGHT, true);
        tile(g, 40, left + half + gap, y, usable - half - gap, last, mx, my, true, GREEN, true);
    }

    private void drawDetails(GuiGraphics g, int top, int bottom, int mx, int my) {
        int left = px + 13;
        int usable = pw - 26;
        int gap = 7;
        int hero = Math.max(30, (bottom - top) * 2 / 3 - gap);
        tile(g, 22, left, top, usable, hero, mx, my, true, LIGHT, false);
        int y = top + hero + gap;
        int half = (usable - gap) / 2;
        tile(g, 29, left, y, half, bottom - y, mx, my, true, GREEN, true);
        tile(g, 33, left + half + gap, y, usable - half - gap, bottom - y, mx, my, true, RED, true);
    }

    private void drawSellQuantity(GuiGraphics g, int top, int bottom, int mx, int my) {
        int left = px + 13;
        int usable = pw - 26;
        int gap = 5;
        int hero = Math.max(28, (bottom - top) / 3);
        tile(g, 22, left, top, usable, hero, mx, my, true, LIGHT, false);
        int cellH = Math.max(17, (bottom - top - hero - 3 * gap) / 2);
        int half = (usable - gap) / 2;
        int[] slots = {28, 30, 32, 34};
        for (int i = 0; i < slots.length; i++) {
            int x = left + (i % 2) * (half + gap);
            int y = top + hero + gap + (i / 2) * (cellH + gap);
            tile(g, slots[i], x, y, half, cellH, mx, my, true, GREEN, true);
        }
    }

    private void drawBank(GuiGraphics g, int top, int bottom, int mx, int my) {
        int left = px + 13;
        int usable = pw - 26;
        int gap = 7;
        int hero = Math.max(32, (bottom - top) / 2);
        tile(g, 22, left, top, usable, hero, mx, my, true, LIGHT, false);
        int y = top + hero + gap;
        int half = (usable - gap) / 2;
        tile(g, 30, left, y, half, bottom - y, mx, my, true, ORANGE, true);
        tile(g, 32, left + half + gap, y, usable - half - gap, bottom - y, mx, my, true, ORANGE, true);
    }

    private void drawOther(GuiGraphics g, int top, int bottom, int mx, int my) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            if (!menu.getSlot(i).getItem().isEmpty()) slots.add(i);
        }
        if (slots.isEmpty()) return;
        int cols = slots.size() > 9 ? 4 : 3;
        int rows = (slots.size() + cols - 1) / cols;
        int gap = 5;
        int w = (pw - 26 - (cols - 1) * gap) / cols;
        int h = Math.max(18, (bottom - top - (rows - 1) * gap) / rows);
        for (int i = 0; i < slots.size(); i++) {
            int x = px + 13 + (i % cols) * (w + gap);
            int y = top + (i / cols) * (h + gap);
            tile(g, slots.get(i), x, y, w, h, mx, my, true, ORANGE, true);
        }
    }

    private void drawNavigation(GuiGraphics g, int mx, int my) {
        int y = py + ph - 36;
        g.fill(px + 1, y - 6, px + pw - 1, py + ph - 1, 0xFF0B1524);
        g.fill(px + 12, y - 7, px + pw - 12, y - 6, BORDER);
        int gap = 3;
        int w = (pw - 24 - 6 * gap) / 7;
        for (int i = 0; i < NAV.length; i++) {
            int x = px + 12 + i * (w + gap);
            ItemStack item = menu.getSlot(NAV[i]).getItem();
            if (item.isEmpty()) continue;
            boolean over = inside(mx, my, x, y, w, 31);
            boolean active = activeTab() == NAV[i];
            int accent = NAV[i] == 50 ? RED : ORANGE;
            g.fill(x, y, x + w, y + 31, active ? 0xFF293F57 : over ? HOVER : CARD);
            border(g, x, y, w, 31, active || over ? accent : BORDER);
            if (active) g.fill(x + 1, y + 1, x + w - 1, y + 3, accent);
            g.renderItem(item, x + (w - 16) / 2, y + 2);
            g.drawCenteredString(font, fit(NAV_LABELS[i], w - 4), x + w / 2, y + 20,
                active ? LIGHT : over ? WHITE : MUTED);
            if (over) hovered = item;
            hits.add(new Hitbox(x, y, w, 31, NAV[i]));
        }
    }

    private int activeTab() {
        if (isHome()) return 49;
        if (isBank() || page().startsWith("Banque - joueurs")) return 52;
        if (isDraft()) return 47;
        if (page().startsWith("Mes colis")) return 48;
        if (page().startsWith("Vendre au serveur") || isSellQuantity()) return 51;
        if (page().startsWith("Marche") || page().startsWith("Mes annonces") || isDetails()) return 46;
        return -1;
    }

    private void pageButton(GuiGraphics g, int slot, String label, int x, int y, int mx, int my) {
        ItemStack item = menu.getSlot(slot).getItem();
        if (item.isEmpty()) return;
        boolean over = inside(mx, my, x, y, 25, 23);
        g.fill(x, y, x + 25, y + 23, over ? HOVER : CARD);
        border(g, x, y, 25, 23, over ? ORANGE : BORDER);
        g.drawCenteredString(font, label, x + 12, y + 7, over ? LIGHT : WHITE);
        if (over) hovered = item;
        hits.add(new Hitbox(x, y, 25, 23, slot));
    }

    private void tile(GuiGraphics g, int slot, int x, int y, int w, int h,
                      int mx, int my, boolean showLore, int accent, boolean clickable) {
        ItemStack item = menu.getSlot(slot).getItem();
        if (item.isEmpty() || w <= 0 || h <= 0) return;
        boolean over = inside(mx, my, x, y, w, h);
        g.fillGradient(x, y, x + w, y + h, over && clickable ? HOVER : CARD, PANEL);
        border(g, x, y, w, h, over && clickable ? accent : BORDER);
        if (over && clickable) g.fill(x + 1, y + 1, x + w - 1, y + 3, accent);

        if (w >= 30 && h >= 16) {
            int ix = x + 6;
            int iy = y + Math.max(1, (h - 16) / 2);
            g.renderItem(item, ix, iy);
            g.renderItemDecorations(font, item, ix, iy);
        }
        int textX = x + (w >= 30 ? 27 : 4);
        int textW = w - (w >= 30 ? 32 : 8);
        String label = item.getHoverName().getString();
        int nameY = y + (h >= 29 ? 5 : Math.max(2, (h - 9) / 2));
        if (textW > 5) g.drawString(font, fit(label, textW), textX, nameY,
            over && clickable ? LIGHT : WHITE, false);
        if (showLore && h >= 29 && textW > 5) {
            String lore = firstLore(item);
            if (!lore.isEmpty()) g.drawString(font, fit(lore, textW), textX, y + 18,
                accent == GREEN ? GREEN : MUTED, false);
        }
        if (over) hovered = item;
        if (clickable) hits.add(new Hitbox(x, y, w, h, slot));
    }

    private static String firstLore(ItemStack stack) {
        CompoundTag display = stack.getTagElement("display");
        if (display == null) return "";
        ListTag lines = display.getList("Lore", Tag.TAG_STRING);
        if (lines.isEmpty()) return "";
        try {
            Component parsed = Component.Serializer.fromJson(lines.getString(0));
            return parsed == null ? "" : parsed.getString();
        } catch (RuntimeException ignored) {
            return "";
        }
    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void border(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private String fit(String value, int maxWidth) {
        if (maxWidth < 5) return "";
        if (font.width(value) <= maxWidth) return value;
        while (!value.isEmpty() && font.width(value + "...") > maxWidth) {
            value = value.substring(0, value.length() - 1);
        }
        return value + "...";
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || minecraft == null || minecraft.gameMode == null || minecraft.player == null)
            return true;
        for (Hitbox hit : hits) {
            if (hit.contains(mouseX, mouseY)) {
                minecraft.gameMode.handleInventoryMouseClick(menu.containerId, hit.slot(), 0,
                    ClickType.PICKUP, minecraft.player);
                return true;
            }
        }
        // Hidden container slots never receive ordinary mouse clicks.
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return true;
    }
}

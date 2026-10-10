package com.worldbattles.jobs.client;

import com.worldbattles.jobs.JobCatalog;
import com.worldbattles.jobs.JobSnapshot;
import com.worldbattles.jobs.JobsNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Compact read-only dashboard, styled after WorldBattles Shop.
 * No vanilla chest, no inventory access, no reward/XP modifications.
 * Changing tabs never creates a new Screen, so the cursor stays in place.
 */
public final class JobsScreen extends Screen {
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

    private enum View { HOME, DETAIL, REWARDS }
    private record Hit(int x, int y, int w, int h, Runnable action) {
        boolean contains(double mx, double my) {
            return mx >= x && my >= y && mx < x + w && my < y + h;
        }
    }

    private final List<Hit> hits = new ArrayList<>();
    private JobSnapshot snapshot;
    private View view = View.HOME;
    private int selected = 0;
    private int rewardScroll = 0;
    private int ticks = 0;
    private int px, py, pw, ph;
    private String hoveredTooltip = "";

    public JobsScreen(JobSnapshot snapshot) {
        super(Component.literal("WorldBattles | METIERS"));
        this.snapshot = snapshot;
    }

    public void update(JobSnapshot next) {
        this.snapshot = next;
    }

    @Override
    protected void init() {
        pw = Math.min(440, Math.max(180, width * 84 / 100));
        ph = Math.min(285, Math.max(160, height * 84 / 100));
        px = (width - pw) / 2;
        py = (height - ph) / 2;
    }

    @Override
    public void tick() {
        if (++ticks % 40 == 0) JobsNetwork.requestRefresh();
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        hits.clear();
        hoveredTooltip = "";
        g.fill(px - 3, py - 3, px + pw + 4, py + ph + 4, 0xA8000000);
        g.fillGradient(px, py, px + pw, py + ph, BACK, PANEL);
        border(g, px, py, pw, ph, BORDER);
        g.fill(px, py, px + pw, py + 3, ORANGE);
        g.fillGradient(px + 1, py + 4, px + pw - 1, py + 54, 0xFF1B2D45, BACK);
        g.fill(px + 12, py + 11, px + 15, py + 29, ORANGE);
        g.drawString(font, "WORLDBATTLES", px + 22, py + 11, WHITE, false);
        g.drawString(font, "METIERS", px + 22 + font.width("WORLDBATTLES") + 8, py + 11, LIGHT, false);
        if (pw >= 385 && snapshot.installed()) {
            String total = "NIVEAUX " + totalLevels() + "/350";
            g.drawString(font, total, px + pw - 13 - font.width(total), py + 12, LIGHT, false);
        }
        String title = view == View.HOME ? "MES 7 METIERS" :
            (view == View.DETAIL ? "PROGRESSION - " : "RECOMPENSES - ") + JobCatalog.JOBS[selected].label();
        g.drawString(font, fit(title, pw - 26), px + 13, py + 32, WHITE, false);
        if (pw > 300) {
            String hint = view == View.HOME ? "Clique sur un metier pour afficher ses statistiques" :
                view == View.DETAIL ? "Progression et prochaine recompense du datapack" :
                "50 recompenses par metier - molette pour defiler";
            g.drawString(font, fit(hint, pw - 27), px + 13, py + 45, MUTED, false);
        }
        g.fill(px + 12, py + 57, px + pw - 12, py + 58, BORDER);

        int top = py + 64;
        int bottom = py + ph - 43;
        if (!snapshot.installed()) {
            drawMissing(g, top, bottom);
        } else if (view == View.HOME) {
            drawHome(g, top, bottom, mouseX, mouseY);
        } else {
            drawSidebar(g, top, bottom, mouseX, mouseY);
            if (view == View.DETAIL) drawDetail(g, top, bottom, mouseX, mouseY);
            else drawRewards(g, top, bottom, mouseX, mouseY);
        }
        drawNavigation(g, mouseX, mouseY);
        if (!hoveredTooltip.isEmpty()) {
            g.renderTooltip(font, Component.literal(hoveredTooltip), mouseX, mouseY);
        }
    }

    private void drawMissing(GuiGraphics g, int top, int bottom) {
        int centerX = px + pw / 2;
        int centerY = (top + bottom) / 2;
        g.drawCenteredString(font, "DATAPACK FARLANDS METIERS NON DETECTE", centerX, centerY - 14, RED);
        g.drawCenteredString(font, fit("Le datapack doit rester installe sur le serveur.", pw - 35),
            centerX, centerY + 1, MUTED);
        g.drawCenteredString(font, fit("Aucune statistique n'a ete modifiee.", pw - 35),
            centerX, centerY + 15, MUTED);
    }

    private void drawHome(GuiGraphics g, int top, int bottom, int mx, int my) {
        int gap = 6;
        int w = (pw - 26 - gap) / 2;
        int h = Math.max(24, (bottom - top - 3 * gap) / 4);
        for (int i = 0; i < JobCatalog.COUNT; i++) {
            int index = i;
            int x = px + 13 + (i % 2) * (w + gap);
            int y = top + (i / 2) * (h + gap);
            boolean over = inside(mx, my, x, y, w, h);
            card(g, x, y, w, h, over, ORANGE);
            drawIcon(g, i, x + 7, y + Math.max(3, (h - 16) / 2));
            g.drawString(font, fit(JobCatalog.JOBS[i].label(), w - 70), x + 28, y + 5,
                over ? LIGHT : WHITE, false);
            String lvl = "NIV. " + snapshot.levels()[i] + "/50";
            g.drawString(font, lvl, x + w - 6 - font.width(lvl), y + 5, LIGHT, false);
            if (h >= 32) {
                String xp = xpLabel(i);
                g.drawString(font, fit(xp, w - 36), x + 28, y + 17, MUTED, false);
                progress(g, x + 28, y + h - 7, w - 37, 3, i);
            }
            hits.add(new Hit(x, y, w, h, () -> choose(index)));
        }
        int x = px + 13 + w + gap;
        int y = top + 3 * (h + gap);
        card(g, x, y, w, h, false, GREEN);
        g.drawString(font, fit("PROGRESSION GLOBALE", w - 12), x + 8, y + 5, LIGHT, false);
        if (h >= 32) {
            String stats = totalLevels() + "/350 niv.  |  " + maxedJobs() + "/7 max";
            g.drawString(font, fit(stats, w - 12), x + 8, y + 17, MUTED, false);
            int percent = totalLevels() * 100 / 350;
            progressBar(g, x + 8, y + h - 7, w - 16, 3, percent, GREEN);
        }
    }

    private void drawSidebar(GuiGraphics g, int top, int bottom, int mx, int my) {
        int gap = 4;
        int w = Math.max(72, Math.min(112, pw / 4));
        int h = Math.max(17, (bottom - top - gap * 6) / 7);
        for (int i = 0; i < JobCatalog.COUNT; i++) {
            int index = i;
            int x = px + 13;
            int y = top + i * (h + gap);
            boolean over = inside(mx, my, x, y, w, h);
            boolean active = selected == i;
            g.fill(x, y, x + w, y + h, active ? 0xFF293F57 : over ? HOVER : CARD);
            border(g, x, y, w, h, active ? ORANGE : over ? LIGHT : BORDER);
            if (active) g.fill(x + 1, y + 1, x + 4, y + h - 1, ORANGE);
            if (h >= 16) drawIcon(g, i, x + 6, y + (h - 16) / 2);
            g.drawString(font, fit(JobCatalog.JOBS[i].label(), w - 34), x + 26,
                y + (h - 9) / 2, active ? LIGHT : WHITE, false);
            hits.add(new Hit(x, y, w, h, () -> choose(index)));
        }
    }

    private int detailX() { return px + 19 + Math.max(72, Math.min(112, pw / 4)); }
    private int detailW() { return px + pw - 13 - detailX(); }

    private void drawDetail(GuiGraphics g, int top, int bottom, int mx, int my) {
        int x = detailX(), w = detailW();
        int level = snapshot.levels()[selected];
        int heroH = Math.min(53, Math.max(35, (bottom - top) / 3));
        card(g, x, top, w, heroH, false, ORANGE);
        String lvl = "NIVEAU " + level + " / 50";
        g.drawString(font, lvl, x + 9, top + 7, LIGHT, false);
        String xp = xpLabel(selected);
        g.drawString(font, fit(xp, w - 18), x + 9, top + 20, MUTED, false);
        progress(g, x + 9, top + heroH - 10, w - 18, 5, selected);

        int helpY = top + heroH + 8;
        g.drawString(font, "COMMENT PROGRESSER", x + 5, helpY, LIGHT, false);
        String[] lines = JobCatalog.JOBS[selected].help();
        int lineY = helpY + 14;
        for (int i = 0; i < lines.length && i < 3; i++) {
            if (lineY + 9 > bottom - 54) break;
            g.drawString(font, fit("- " + lines[i], w - 10), x + 5, lineY, MUTED, false);
            lineY += 12;
        }

        int rewardY = bottom - 51;
        card(g, x, rewardY, w, 51, false, GREEN);
        int next = level + 1;
        if (level >= 50) {
            g.drawString(font, "NIVEAU MAXIMUM ATTEINT", x + 9, rewardY + 9, GREEN, false);
            g.drawString(font, fit("Les 50 niveaux sont termines !", w - 18),
                x + 9, rewardY + 27, WHITE, false);
        } else {
            g.drawString(font, "PROCHAINE RECOMPENSE  -  NIV. " + next, x + 9, rewardY + 7, GREEN, false);
            JobCatalog.Job job = JobCatalog.JOBS[selected];
            g.drawString(font, fit(job.typeAt(next), w - 18), x + 9, rewardY + 21, LIGHT, false);
            String name = job.rewardAt(next);
            g.drawString(font, fit(name, w - 18), x + 9, rewardY + 35, WHITE, false);
            if (inside(mx, my, x, rewardY, w, 51)) hoveredTooltip = name;
        }
    }

    private void drawRewards(GuiGraphics g, int top, int bottom, int mx, int my) {
        int x = detailX(), w = detailW();
        int listTop = top + 16;
        int rowH = 23;
        int visible = Math.max(1, (bottom - listTop) / rowH);
        int maxScroll = Math.max(0, 50 - visible);
        rewardScroll = Math.min(maxScroll, rewardScroll);
        g.drawString(font, "PALIER / RECOMPENSE", x + 4, top + 2, LIGHT, false);
        JobCatalog.Job job = JobCatalog.JOBS[selected];
        for (int i = 0; i < visible; i++) {
            int lvl = rewardScroll + i + 1;
            if (lvl > 50) break;
            int y = listTop + i * rowH;
            boolean over = inside(mx, my, x, y, w - 7, rowH - 3);
            boolean reached = lvl <= snapshot.levels()[selected];
            boolean next = lvl == snapshot.levels()[selected] + 1;
            g.fill(x, y, x + w - 7, y + rowH - 3, over ? HOVER : next ? 0xFF293F57 : CARD);
            border(g, x, y, w - 7, rowH - 3, next ? ORANGE : BORDER);
            String lvlLabel = "" + lvl;
            g.drawString(font, lvlLabel, x + 7, y + 6, reached ? GREEN : next ? LIGHT : MUTED, false);
            String name = job.rewardAt(lvl);
            g.drawString(font, fit(name, w - 63), x + 29, y + 3, reached ? WHITE : MUTED, false);
            String category = job.typeAt(lvl);
            g.drawString(font, fit(category, w - 63), x + 29, y + 12,
                category.contains("VANILLA") ? GREEN : category.contains("MOD") ? LIGHT : MUTED, false);
            g.drawString(font, reached ? "+" : next ? ">" : "-", x + w - 21, y + 6,
                reached ? GREEN : next ? LIGHT : MUTED, false);
            if (over) hoveredTooltip = "Niv. " + lvl + " - " + name;
        }
        int barX = x + w - 3;
        int barH = Math.max(1, bottom - listTop);
        g.fill(barX, listTop, barX + 3, bottom, BORDER);
        int thumbH = Math.max(12, barH * visible / 50);
        int thumbY = listTop + (barH - thumbH) * rewardScroll / Math.max(1, maxScroll);
        g.fill(barX, thumbY, barX + 3, thumbY + thumbH, ORANGE);
    }

    private void drawNavigation(GuiGraphics g, int mx, int my) {
        int y = py + ph - 36;
        g.fill(px + 1, y - 6, px + pw - 1, py + ph - 1, 0xFF0B1524);
        g.fill(px + 12, y - 7, px + pw - 12, y - 6, BORDER);
        String[] labels = {"TOUS LES METIERS", "DETAILS", "RECOMPENSES", "QUITTER"};
        View[] tabs = {View.HOME, View.DETAIL, View.REWARDS, View.HOME};
        int gap = 4;
        int w = (pw - 24 - 3 * gap) / 4;
        for (int i = 0; i < 4; i++) {
            int index = i;
            int x = px + 12 + i * (w + gap);
            boolean over = inside(mx, my, x, y, w, 30);
            boolean active = i < 3 && view == tabs[i];
            int accent = i == 3 ? RED : ORANGE;
            g.fill(x, y, x + w, y + 30, active ? 0xFF293F57 : over ? HOVER : CARD);
            border(g, x, y, w, 30, active || over ? accent : BORDER);
            if (active) g.fill(x + 1, y + 1, x + w - 1, y + 3, accent);
            g.drawCenteredString(font, fit(labels[i], w - 7), x + w / 2, y + 11,
                active ? LIGHT : over ? WHITE : MUTED);
            hits.add(new Hit(x, y, w, 30, () -> {
                if (index == 3) onClose();
                else switchView(tabs[index]);
            }));
        }
    }

    private void choose(int job) {
        if (selected != job) {
            selected = job;
            alignRewardScroll();
        }
        if (view == View.HOME) view = View.DETAIL;
    }

    private void switchView(View next) {
        if (next == View.REWARDS && view != View.REWARDS) alignRewardScroll();
        view = next;
    }

    private void alignRewardScroll() {
        int visible = Math.max(1, (py + ph - 43 - (py + 64 + 16)) / 23);
        rewardScroll = Math.max(0, Math.min(50 - visible, snapshot.levels()[selected] - 3));
    }

    private int totalLevels() {
        int sum = 0;
        for (int lvl : snapshot.levels()) sum += lvl;
        return sum;
    }

    private int maxedJobs() {
        int count = 0;
        for (int lvl : snapshot.levels()) if (lvl >= 50) count++;
        return count;
    }

    private String xpLabel(int i) {
        if (snapshot.levels()[i] >= 50) return "NIVEAU MAXIMUM";
        if (snapshot.levels()[i] == 0) return "EN ATTENTE DES STATS";
        return snapshot.xp()[i] + " / " + snapshot.needed()[i] + " XP";
    }

    private void progress(GuiGraphics g, int x, int y, int w, int h, int job) {
        int level = snapshot.levels()[job];
        int needed = snapshot.needed()[job];
        int percent = level >= 50 ? 100 : needed > 0 ?
            (int) Math.max(0, Math.min(100, 100L * snapshot.xp()[job] / needed)) : 0;
        progressBar(g, x, y, w, h, percent, level >= 50 ? GREEN : ORANGE);
    }

    private void progressBar(GuiGraphics g, int x, int y, int w, int h, int percent, int accent) {
        if (w <= 0) return;
        g.fill(x, y, x + w, y + h, BORDER);
        int filled = Math.max(0, Math.min(w, w * percent / 100));
        if (filled > 0) g.fill(x, y, x + filled, y + h, accent);
    }

    private void drawIcon(GuiGraphics g, int job, int x, int y) {
        ResourceLocation id = new ResourceLocation(JobCatalog.JOBS[job].icon());
        ItemStack icon = new ItemStack(BuiltInRegistries.ITEM.get(id));
        if (!icon.isEmpty()) g.renderItem(icon, x, y);
    }

    private void card(GuiGraphics g, int x, int y, int w, int h, boolean over, int accent) {
        g.fillGradient(x, y, x + w, y + h, over ? HOVER : CARD, PANEL);
        border(g, x, y, w, h, over ? accent : BORDER);
        if (over) g.fill(x + 1, y + 1, x + w - 1, y + 3, accent);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void border(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private String fit(String value, int maxWidth) {
        if (maxWidth < 8) return "";
        if (font.width(value) <= maxWidth) return value;
        while (!value.isEmpty() && font.width(value + "...") > maxWidth) {
            value = value.substring(0, value.length() - 1);
        }
        return value + "...";
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return true;
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit hit = hits.get(i);
            if (hit.contains(mouseX, mouseY)) {
                hit.action().run();
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (view == View.REWARDS && inside(mouseX, mouseY, detailX(), py + 64,
            detailW(), ph - 107)) {
            int visible = Math.max(1, (py + ph - 43 - (py + 64 + 16)) / 23);
            int max = Math.max(0, 50 - visible);
            rewardScroll = Math.max(0, Math.min(max, rewardScroll + (delta > 0 ? -2 : 2)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}

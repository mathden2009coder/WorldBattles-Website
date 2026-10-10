package com.worldbattles.shop;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/** All GUI navigation and purchase actions are performed on the server. */
public final class ShopGui {
    private ShopGui() {}
    private static final int PAGE_SIZE = 24;
    private static final Map<UUID, Draft> DRAFTS = new HashMap<>();

    private static final class Draft {
        long price = 100;
        int quantity = 1;
    }

    private static ShopData data(ServerPlayer player) { return ShopData.get(player.getServer()); }

    private static void open(ServerPlayer player, String title, Consumer<ShopMenu> fill) {
        if (!WorldBattlesShop.allowed(player)) {
            WorldBattlesShop.message(player, "Le shop est interdit dans cette dimension.");
            return;
        }
        data(player).remember(player);
        player.openMenu(new SimpleMenuProvider((id, inv, ignored) -> {
            ShopMenu menu = new ShopMenu(id, inv);
            fill.accept(menu);
            return menu;
        }, Component.literal(title)));
    }

    private static ItemStack icon(Item item, String title, String... lore) {
        return icon(new ItemStack(item), title, lore);
    }

    private static ItemStack icon(ItemStack source, String title, String... lore) {
        ItemStack copy = source.copy();
        CompoundTag display = copy.getOrCreateTagElement("display");
        display.putString("Name", Component.Serializer.toJson(Component.literal(title)));
        ListTag lines = new ListTag();
        for (String line : lore) lines.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(line))));
        display.put("Lore", lines);
        return copy;
    }

    private static void foot(ShopMenu m, ServerPlayer p) {
        // Persistent navigation across every screen, including detailed transactions.
        m.button(46, icon(Items.EMERALD, "Marche"), x -> market(x, 0));
        m.button(47, icon(Items.CHEST, "Deposer"), ShopGui::draft);
        m.button(48, icon(Items.ENDER_CHEST, "Mes colis"), x -> mailbox(x, 0));
        m.button(49, icon(Items.NETHER_STAR, "Accueil"), ShopGui::home);
        m.button(50, icon(Items.BARRIER, "Fermer"), ServerPlayer::closeContainer);
        m.button(51, icon(Items.GOLD_INGOT, "Vendre"), ShopGui::serverSell);
        m.button(52, icon(Items.SUNFLOWER, "Banque"), ShopGui::bank);
    }

    public static void home(ServerPlayer p) {
        long balance = data(p).balance(p.getUUID());
        open(p, "WorldBattles | SHOP", m -> {
            m.button(11, icon(Items.EMERALD, "Marche des joueurs", "Acheter les objets des autres joueurs"), x -> market(x, 0));
            m.button(13, icon(Items.CHEST, "Mettre un item en vente", "Tiens l'objet en main", "Choisis le prix et la quantite"), ShopGui::draft);
            m.button(15, icon(Items.GOLD_INGOT, "Vendre au serveur", "Vente immediate a prix fixe"), ShopGui::serverSell);
            m.button(29, icon(Items.BOOK, "Mes annonces", "Annuler les annonces non vendues"), x -> myListings(x, 0));
            m.button(31, icon(Items.SUNFLOWER, "Ma banque", "Solde : " + balance + " $"), ShopGui::bank);
            m.button(33, icon(Items.PLAYER_HEAD, "Argent des joueurs", "Consulter tous les soldes"), x -> balances(x, 0));
            m.button(40, icon(Items.ENDER_CHEST, "Mes livraisons", "Recuperer les achats et retours"), x -> mailbox(x, 0));
            m.button(44, icon(Items.GOLD_NUGGET, "SOLDE : " + balance + " $"), null);
            foot(m, p);
        });
    }

    public static void market(ServerPlayer p, int requestedPage) {
        List<ShopData.Listing> offers = data(p).listings();
        int page = clampPage(requestedPage, offers.size());
        open(p, "Marche des joueurs " + (page + 1), m -> {
            for (int i = page * PAGE_SIZE; i < Math.min(offers.size(), (page + 1) * PAGE_SIZE); i++) {
                ShopData.Listing offer = offers.get(i);
                m.button(i % PAGE_SIZE, icon(offer.stack(), "#" + offer.id() + " - " + offer.price() + " $",
                    "Vendeur : " + offer.sellerName(), "Quantite : " + offer.stack().getCount(), "Cliquer pour examiner"),
                    x -> offerDetails(x, offer.id(), page));
            }
            if (page > 0) m.button(45, icon(Items.ARROW, "Page precedente"), x -> market(x, page - 1));
            if ((page + 1) * PAGE_SIZE < offers.size()) m.button(53, icon(Items.ARROW, "Page suivante"), x -> market(x, page + 1));
            foot(m, p);
        });
    }

    private static void offerDetails(ServerPlayer p, long id, int page) {
        ShopData.Listing offer = data(p).listing(id);
        if (offer == null) {
            WorldBattlesShop.message(p, "Cette annonce n'existe plus.");
            market(p, page);
            return;
        }
        open(p, "Achat - annonce #" + id, m -> {
            m.button(22, icon(offer.stack(), "Objet en vente", "Vendeur : " + offer.sellerName(),
                "Quantite : " + offer.stack().getCount(), "Prix total : " + offer.price() + " $"), null);
            m.button(29, icon(Items.LIME_CONCRETE, "CONFIRMER L'ACHAT", "Prix : " + offer.price() + " $", "Livraison dans la boite"), x -> {
                ShopData.Result result = data(x).buy(x, id);
                WorldBattlesShop.message(x, result.message());
                market(x, page);
            });
            m.button(33, icon(Items.RED_CONCRETE, "Annuler"), x -> market(x, page));
            foot(m, p);
        });
    }

    private static void myListings(ServerPlayer p, int requestedPage) {
        List<ShopData.Listing> offers = data(p).listings().stream().filter(x -> x.seller().equals(p.getUUID())).toList();
        int page = clampPage(requestedPage, offers.size());
        open(p, "Mes annonces " + (page + 1), m -> {
            for (int i = page * PAGE_SIZE; i < Math.min(offers.size(), (page + 1) * PAGE_SIZE); i++) {
                ShopData.Listing offer = offers.get(i);
                m.button(i % PAGE_SIZE, icon(offer.stack(), "#" + offer.id() + " - " + offer.price() + " $", "Cliquer pour annuler"),
                    x -> cancelDetails(x, offer.id(), page));
            }
            if (page > 0) m.button(45, icon(Items.ARROW, "Page precedente"), x -> myListings(x, page - 1));
            if ((page + 1) * PAGE_SIZE < offers.size()) m.button(53, icon(Items.ARROW, "Page suivante"), x -> myListings(x, page + 1));
            foot(m, p);
        });
    }

    private static void cancelDetails(ServerPlayer p, long id, int page) {
        ShopData.Listing offer = data(p).listing(id);
        if (offer == null || !offer.seller().equals(p.getUUID())) {
            myListings(p, page);
            return;
        }
        open(p, "Annuler l'annonce #" + id, m -> {
            m.button(22, icon(offer.stack(), "Annonce #" + id, "Prix : " + offer.price() + " $"), null);
            m.button(29, icon(Items.LIME_CONCRETE, "CONFIRMER L'ANNULATION", "L'objet ira dans tes colis"), x -> {
                WorldBattlesShop.message(x, data(x).cancel(x, id).message());
                myListings(x, page);
            });
            m.button(33, icon(Items.RED_CONCRETE, "Retour"), x -> myListings(x, page));
            foot(m, p);
        });
    }

    public static void draft(ServerPlayer p) {
        ItemStack hand = p.getMainHandItem();
        if (hand.isEmpty()) {
            WorldBattlesShop.message(p, "Tiens d'abord l'item que tu veux vendre en main.");
            home(p);
            return;
        }
        Draft d = DRAFTS.computeIfAbsent(p.getUUID(), ignored -> new Draft());
        d.quantity = Math.max(1, Math.min(d.quantity, hand.getMaxStackSize()));
        int available = ShopData.matchingCount(p, hand);
        open(p, "Creer une annonce", m -> {
            m.button(13, icon(hand, "Objet : " + hand.getHoverName().getString(),
                "Disponibles : " + available, "Quantite annonce : " + d.quantity), null);
            m.button(22, icon(Items.GOLD_INGOT, "PRIX TOTAL : " + d.price + " $", "Ce prix concerne le lot entier"), null);
            m.button(10, icon(Items.GOLD_NUGGET, "+1 $"), x -> changePrice(x, 1));
            m.button(11, icon(Items.GOLD_NUGGET, "+10 $"), x -> changePrice(x, 10));
            m.button(12, icon(Items.GOLD_INGOT, "+100 $"), x -> changePrice(x, 100));
            m.button(14, icon(Items.GOLD_BLOCK, "+1000 $"), x -> changePrice(x, 1000));
            m.button(19, icon(Items.REDSTONE, "-1 $"), x -> changePrice(x, -1));
            m.button(20, icon(Items.REDSTONE, "-10 $"), x -> changePrice(x, -10));
            m.button(21, icon(Items.REDSTONE_BLOCK, "-100 $"), x -> changePrice(x, -100));
            m.button(23, icon(Items.REDSTONE_BLOCK, "-1000 $"), x -> changePrice(x, -1000));
            m.button(31, icon(Items.CHEST, "QUANTITE : " + d.quantity, "Cliquer : 1 / 16 / 32 / 64", "Maximum selon le type d'item"), ShopGui::nextQuantity);
            m.button(40, icon(Items.LIME_CONCRETE, "METTRE EN VENTE", "Retire " + d.quantity + " objet(s)", "Prix : " + d.price + " $"), x -> {
                ShopData.Result result = data(x).createListing(x, d.price, d.quantity);
                WorldBattlesShop.message(x, result.message());
                if (result.ok()) myListings(x, 0);
                else draft(x);
            });
            foot(m, p);
        });
    }

    private static void changePrice(ServerPlayer p, long delta) {
        Draft d = DRAFTS.computeIfAbsent(p.getUUID(), ignored -> new Draft());
        d.price = Math.max(1, Math.min(ShopData.MAX_PRICE, d.price + delta));
        draft(p);
    }

    private static void nextQuantity(ServerPlayer p) {
        Draft d = DRAFTS.computeIfAbsent(p.getUUID(), ignored -> new Draft());
        int[] choices = {1, 16, 32, 64};
        int next = 1;
        for (int n : choices) {
            if (n > d.quantity) { next = n; break; }
        }
        d.quantity = Math.min(next, p.getMainHandItem().getMaxStackSize());
        draft(p);
    }

    public static void serverSell(ServerPlayer p) {
        open(p, "Vendre au serveur", m -> {
            for (int i = 0; i < ServerPrices.ITEMS.size() && i < PAGE_SIZE; i++) {
                ServerPrices.Entry entry = ServerPrices.ITEMS.get(i);
                int count = ShopData.countPlain(p, entry.item());
                m.button(i, icon(entry.item(), entry.item().getDescription().getString(),
                    "Prix : " + entry.price() + " $ / unite", "Tu en as : " + count, "Cliquer pour vendre"),
                    x -> sellQuantity(x, entry));
            }
            foot(m, p);
        });
    }

    private static void sellQuantity(ServerPlayer p, ServerPrices.Entry entry) {
        int available = ShopData.countPlain(p, entry.item());
        open(p, "Vente au serveur", m -> {
            m.button(22, icon(entry.item(), entry.item().getDescription().getString(),
                "Prix unitaire : " + entry.price() + " $", "Disponibles : " + available,
                "Seuls les items sans NBT sont rachetes"), null);
            int[] counts = {1, 16, 64, Integer.MAX_VALUE};
            int[] slots = {28, 30, 32, 34};
            String[] labels = {"Vendre 1", "Vendre jusqu'a 16", "Vendre jusqu'a 64", "TOUT VENDRE"};
            for (int i = 0; i < counts.length; i++) {
                int requested = counts[i];
                int actual = Math.min(requested, available);
                m.button(slots[i], icon(Items.EMERALD, labels[i], "Recu : " + (actual * entry.price()) + " $"), x -> {
                    WorldBattlesShop.message(x, data(x).sellToServer(x, entry, requested).message());
                    sellQuantity(x, entry);
                });
            }
            m.button(45, icon(Items.ARROW, "Retour aux ressources"), ShopGui::serverSell);
            foot(m, p);
        });
    }

    public static void bank(ServerPlayer p) {
        long amount = data(p).balance(p.getUUID());
        open(p, "WorldBattles | BANQUE", m -> {
            m.button(22, icon(Items.GOLD_BLOCK, "TON SOLDE : " + amount + " $", "Argent sauvegarde par joueur", "Commun aux dimensions autorisees"), null);
            m.button(30, icon(Items.PLAYER_HEAD, "Soldes des joueurs"), x -> balances(x, 0));
            m.button(32, icon(Items.ENDER_CHEST, "Mes colis"), x -> mailbox(x, 0));
            foot(m, p);
        });
    }

    public static void balances(ServerPlayer p, int requestedPage) {
        List<ShopData.Account> accounts = data(p).accounts();
        int page = clampPage(requestedPage, accounts.size());
        open(p, "Banque - joueurs " + (page + 1), m -> {
            for (int i = page * PAGE_SIZE; i < Math.min(accounts.size(), (page + 1) * PAGE_SIZE); i++) {
                ShopData.Account a = accounts.get(i);
                m.button(i % PAGE_SIZE, icon(Items.PAPER, (i + 1) + ". " + a.name(), "Solde : " + a.balance() + " $"), null);
            }
            if (page > 0) m.button(45, icon(Items.ARROW, "Page precedente"), x -> balances(x, page - 1));
            if ((page + 1) * PAGE_SIZE < accounts.size()) m.button(53, icon(Items.ARROW, "Page suivante"), x -> balances(x, page + 1));
            foot(m, p);
        });
    }

    public static void mailbox(ServerPlayer p, int requestedPage) {
        List<ItemStack> items = data(p).mail(p.getUUID());
        int page = clampPage(requestedPage, items.size());
        open(p, "Mes colis " + (page + 1), m -> {
            for (int i = page * PAGE_SIZE; i < Math.min(items.size(), (page + 1) * PAGE_SIZE); i++) {
                int index = i;
                m.button(i % PAGE_SIZE, icon(items.get(i), "Colis #" + (i + 1), "Cliquer pour recuperer", "Si plein, le reste est conserve"), x -> {
                    WorldBattlesShop.message(x, data(x).claim(x, index).message());
                    mailbox(x, page);
                });
            }
            if (page > 0) m.button(45, icon(Items.ARROW, "Page precedente"), x -> mailbox(x, page - 1));
            if ((page + 1) * PAGE_SIZE < items.size()) m.button(53, icon(Items.ARROW, "Page suivante"), x -> mailbox(x, page + 1));
            foot(m, p);
        });
    }

    private static int clampPage(int requested, int count) {
        return Math.max(0, Math.min(requested, Math.max(0, (count - 1) / PAGE_SIZE)));
    }
}

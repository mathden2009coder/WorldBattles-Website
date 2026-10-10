package com.worldbattles.shop;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** All economy changes happen on the Minecraft server thread. Never trust a client for balances/items. */
public final class ShopData extends SavedData {
    private static final Logger LOG = LoggerFactory.getLogger(ShopData.class);
    public static final long MAX_BALANCE = 1_000_000_000_000L;
    public static final long MAX_PRICE = 10_000_000_000L;
    public static final int MAX_LISTINGS_PER_PLAYER = 20;
    public static final int MAX_LISTINGS_GLOBAL = 2_000;
    public static final int MAX_MAIL_PER_PLAYER = 200;

    public record Listing(long id, UUID seller, String sellerName, ItemStack stack, long price) {}
    public record Account(UUID id, String name, long balance) {}
    public record Result(boolean ok, String message) {
        static Result success(String message) { return new Result(true, message); }
        static Result fail(String message) { return new Result(false, message); }
    }

    private final Map<UUID, Long> balances = new HashMap<>();
    private final Map<UUID, String> names = new HashMap<>();
    private final Map<Long, Listing> listings = new LinkedHashMap<>();
    private final Map<UUID, List<ItemStack>> mail = new HashMap<>();
    private long nextListingId = 1;

    public static ShopData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(ShopData::load, ShopData::new, "worldbattles_shop_v1");
    }

    public static ShopData load(CompoundTag tag) {
        ShopData data = new ShopData();
        data.nextListingId = Math.max(1, tag.getLong("NextListingId"));
        for (Tag entryTag : tag.getList("Accounts", Tag.TAG_COMPOUND)) {
            CompoundTag e = (CompoundTag) entryTag;
            if (!e.hasUUID("Id")) continue;
            UUID id = e.getUUID("Id");
            data.balances.put(id, Math.max(0, Math.min(MAX_BALANCE, e.getLong("Balance"))));
            data.names.put(id, e.getString("Name"));
        }
        for (Tag entryTag : tag.getList("Listings", Tag.TAG_COMPOUND)) {
            CompoundTag e = (CompoundTag) entryTag;
            if (!e.hasUUID("Seller") || !e.contains("Item", Tag.TAG_COMPOUND)) continue;
            ItemStack stack = ItemStack.of(e.getCompound("Item"));
            long price = e.getLong("Price");
            long id = e.getLong("Id");
            if (stack.isEmpty() || price < 1 || price > MAX_PRICE || id < 1 || id == Long.MAX_VALUE) continue;
            data.listings.put(id, new Listing(id, e.getUUID("Seller"), e.getString("SellerName"), stack, price));
            data.nextListingId = Math.max(data.nextListingId, id + 1);
        }
        for (Tag entryTag : tag.getList("Mail", Tag.TAG_COMPOUND)) {
            CompoundTag e = (CompoundTag) entryTag;
            if (!e.hasUUID("Owner")) continue;
            List<ItemStack> items = new ArrayList<>();
            for (Tag itemTag : e.getList("Items", Tag.TAG_COMPOUND)) {
                ItemStack item = ItemStack.of((CompoundTag) itemTag);
                if (!item.isEmpty()) items.add(item);
            }
            data.mail.put(e.getUUID("Owner"), items);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong("NextListingId", nextListingId);
        ListTag accounts = new ListTag();
        for (Map.Entry<UUID, Long> account : balances.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putUUID("Id", account.getKey());
            e.putLong("Balance", account.getValue());
            e.putString("Name", names.getOrDefault(account.getKey(), "Unknown"));
            accounts.add(e);
        }
        tag.put("Accounts", accounts);

        ListTag offers = new ListTag();
        for (Listing offer : listings.values()) {
            CompoundTag e = new CompoundTag();
            e.putLong("Id", offer.id());
            e.putUUID("Seller", offer.seller());
            e.putString("SellerName", offer.sellerName());
            e.putLong("Price", offer.price());
            e.put("Item", offer.stack().save(new CompoundTag()));
            offers.add(e);
        }
        tag.put("Listings", offers);

        ListTag mailTag = new ListTag();
        for (Map.Entry<UUID, List<ItemStack>> box : mail.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putUUID("Owner", box.getKey());
            ListTag items = new ListTag();
            for (ItemStack stack : box.getValue()) items.add(stack.save(new CompoundTag()));
            e.put("Items", items);
            mailTag.add(e);
        }
        tag.put("Mail", mailTag);
        return tag;
    }

    public void remember(ServerPlayer player) {
        UUID id = player.getUUID();
        String name = player.getGameProfile().getName();
        if (!names.containsKey(id) || !names.get(id).equals(name)) {
            names.put(id, name);
            setDirty();
        }
        if (!balances.containsKey(id)) {
            balances.put(id, 0L);
            setDirty();
        }
    }

    public long balance(UUID id) { return balances.getOrDefault(id, 0L); }
    public String name(UUID id) { return names.getOrDefault(id, id.toString().substring(0, 8)); }
    public List<Listing> listings() { return new ArrayList<>(listings.values()); }
    public Listing listing(long id) { return listings.get(id); }
    public List<ItemStack> mail(UUID id) { return new ArrayList<>(mail.getOrDefault(id, List.of())); }

    public List<Account> accounts() {
        List<Account> result = new ArrayList<>();
        for (Map.Entry<UUID, Long> e : balances.entrySet())
            result.add(new Account(e.getKey(), name(e.getKey()), e.getValue()));
        result.sort(Comparator.comparingLong(Account::balance).reversed().thenComparing(Account::name));
        return result;
    }

    public Result adminSet(UUID id, String name, long amount) {
        if (amount < 0 || amount > MAX_BALANCE) return Result.fail("Montant invalide.");
        names.put(id, name);
        balances.put(id, amount);
        setDirty();
        return Result.success("Solde defini : " + amount + " $.");
    }

    public Result adminAdd(UUID id, String name, long amount) {
        if (amount < 0 || amount > MAX_BALANCE - balance(id)) return Result.fail("Plafond de solde atteint.");
        return adminSet(id, name, balance(id) + amount);
    }

    public Result createListing(ServerPlayer player, long price, int quantity) {
        if (price < 1 || price > MAX_PRICE) return Result.fail("Prix invalide.");
        if (listings.size() >= MAX_LISTINGS_GLOBAL) return Result.fail("Marche temporairement plein.");
        long sellerCount = listings.values().stream().filter(x -> x.seller().equals(player.getUUID())).count();
        if (sellerCount >= MAX_LISTINGS_PER_PLAYER) return Result.fail("Limite de 20 annonces atteinte.");
        ItemStack hand = player.getMainHandItem();
        if (hand.isEmpty() || quantity < 1 || quantity > hand.getMaxStackSize())
            return Result.fail("Tiens un objet en main et choisis une quantite valide.");
        if (matchingCount(player, hand) < quantity) return Result.fail("Tu n'as plus assez d'objets identiques.");
        if (nextListingId == Long.MAX_VALUE) return Result.fail("Limite technique d'annonces atteinte.");

        ItemStack escrow = hand.copy();
        escrow.setCount(quantity);
        removeMatching(player, escrow, quantity); // Escrow is removed before listing becomes visible.
        long id = nextListingId++;
        listings.put(id, new Listing(id, player.getUUID(), player.getGameProfile().getName(), escrow, price));
        LOG.info("MARKET_LIST id={} seller={} count={} price={}", id, player.getUUID(), quantity, price);
        player.getInventory().setChanged();
        setDirty();
        return Result.success("Annonce #" + id + " creee : " + quantity + " item(s) pour " + price + " $.");
    }

    public Result buy(ServerPlayer buyer, long listingId) {
        Listing listing = listings.get(listingId); // Re-read authoritative state at click time.
        if (listing == null) return Result.fail("Annonce deja vendue ou retiree.");
        if (listing.seller().equals(buyer.getUUID())) return Result.fail("Tu ne peux pas acheter ton propre item.");
        if (balance(buyer.getUUID()) < listing.price()) return Result.fail("Argent insuffisant.");
        if (balance(listing.seller()) > MAX_BALANCE - listing.price()) return Result.fail("Le vendeur a atteint son plafond d'argent.");
        if (mail.getOrDefault(buyer.getUUID(), List.of()).size() >= MAX_MAIL_PER_PLAYER)
            return Result.fail("Ta boite de recuperation est pleine.");

        // Single server-thread operation: remove offer, transfer money, queue delivery, mark data dirty.
        balances.put(buyer.getUUID(), balance(buyer.getUUID()) - listing.price());
        balances.put(listing.seller(), balance(listing.seller()) + listing.price());
        mail.computeIfAbsent(buyer.getUUID(), x -> new ArrayList<>()).add(listing.stack().copy());
        listings.remove(listingId);
        LOG.info("MARKET_BUY id={} buyer={} seller={} price={}", listingId, buyer.getUUID(), listing.seller(), listing.price());
        setDirty();
        return Result.success("Achat confirme ! Recupere ton item dans la boite de livraison.");
    }

    public Result cancel(ServerPlayer seller, long listingId) {
        Listing listing = listings.get(listingId);
        if (listing == null || !listing.seller().equals(seller.getUUID())) return Result.fail("Annonce introuvable.");
        if (mail.getOrDefault(seller.getUUID(), List.of()).size() >= MAX_MAIL_PER_PLAYER)
            return Result.fail("Vide d'abord ta boite de recuperation.");
        mail.computeIfAbsent(seller.getUUID(), x -> new ArrayList<>()).add(listing.stack().copy());
        listings.remove(listingId);
        LOG.info("MARKET_CANCEL id={} seller={}", listingId, seller.getUUID());
        setDirty();
        return Result.success("Annonce annulee. Objet retourne dans ta boite.");
    }

    public Result sellToServer(ServerPlayer player, ServerPrices.Entry offer, int requested) {
        if (requested <= 0) return Result.fail("Quantite invalide.");
        int count = countPlain(player, offer.item());
        int quantity = Math.min(requested, count);
        if (quantity <= 0) return Result.fail("Tu n'as pas cet item sans NBT dans ton inventaire.");
        long total = offer.price() * (long) quantity;
        if (total > MAX_BALANCE - balance(player.getUUID())) return Result.fail("Plafond d'argent atteint.");
        removePlain(player, offer.item(), quantity);
        balances.put(player.getUUID(), balance(player.getUUID()) + total);
        LOG.info("SERVER_BUYBACK player={} item={} count={} paid={}", player.getUUID(), offer.item(), quantity, total);
        player.getInventory().setChanged();
        setDirty();
        return Result.success("Vendu " + quantity + " item(s) pour " + total + " $.");
    }

    public Result claim(ServerPlayer player, int index) {
        List<ItemStack> items = mail.get(player.getUUID());
        if (items == null || index < 0 || index >= items.size()) return Result.fail("Colis introuvable.");
        ItemStack remaining = items.get(index).copy();
        player.getInventory().add(remaining); // Mutates 'remaining'; any overflow stays in escrow.
        if (remaining.isEmpty()) items.remove(index);
        else items.set(index, remaining);
        player.getInventory().setChanged();
        setDirty();
        return remaining.isEmpty() ? Result.success("Colis recupere !") : Result.fail("Inventaire plein : le reste du colis est conserve.");
    }

    public static int matchingCount(ServerPlayer player, ItemStack template) {
        int count = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameTags(stack, template)) count += stack.getCount();
        }
        return count;
    }

    private static void removeMatching(ServerPlayer player, ItemStack template, int quantity) {
        int remaining = quantity;
        for (int i = 0; i < 36 && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!ItemStack.isSameItemSameTags(stack, template)) continue;
            int taken = Math.min(remaining, stack.getCount());
            stack.shrink(taken);
            remaining -= taken;
        }
        if (remaining != 0) throw new IllegalStateException("Escrow inventory changed unexpectedly");
    }

    public static int countPlain(ServerPlayer player, net.minecraft.world.item.Item item) {
        int count = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item) && !stack.hasTag()) count += stack.getCount();
        }
        return count;
    }

    private static void removePlain(ServerPlayer player, net.minecraft.world.item.Item item, int quantity) {
        int remaining = quantity;
        for (int i = 0; i < 36 && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.is(item) || stack.hasTag()) continue;
            int taken = Math.min(remaining, stack.getCount());
            stack.shrink(taken);
            remaining -= taken;
        }
        if (remaining != 0) throw new IllegalStateException("Server sale inventory changed unexpectedly");
    }
}

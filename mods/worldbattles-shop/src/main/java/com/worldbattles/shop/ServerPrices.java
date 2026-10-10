package com.worldbattles.shop;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

/** Fixed buyback prices in dollars per single item; the server never sells items in v0.1. */
public final class ServerPrices {
    private ServerPrices() {}
    public record Entry(Item item, long price) {}
    public static final List<Entry> ITEMS = List.of(
        new Entry(Items.COBBLESTONE, 1),
        new Entry(Items.STONE, 2),
        new Entry(Items.DIRT, 1),
        new Entry(Items.OAK_LOG, 3),
        new Entry(Items.SPRUCE_LOG, 3),
        new Entry(Items.COAL, 8),
        new Entry(Items.RAW_IRON, 12),
        new Entry(Items.IRON_INGOT, 15),
        new Entry(Items.RAW_GOLD, 20),
        new Entry(Items.GOLD_INGOT, 25),
        new Entry(Items.REDSTONE, 5),
        new Entry(Items.LAPIS_LAZULI, 7),
        new Entry(Items.DIAMOND, 100),
        new Entry(Items.EMERALD, 80),
        new Entry(Items.ANCIENT_DEBRIS, 250),
        new Entry(Items.NETHERITE_SCRAP, 300),
        new Entry(Items.WHEAT, 4),
        new Entry(Items.CARROT, 3),
        new Entry(Items.POTATO, 3),
        new Entry(Items.COD, 8),
        new Entry(Items.SALMON, 10),
        new Entry(Items.BEEF, 5),
        new Entry(Items.ROTTEN_FLESH, 1)
    );
}

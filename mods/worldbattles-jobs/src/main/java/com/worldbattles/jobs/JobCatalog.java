package com.worldbattles.jobs;

/**
 * Exact player-facing reward descriptions from Farlands Metiers V5.4
 * (data/farlands_jobs/functions/ui/next/*.mcfunction).
 * Read-only presentation: this class does not award anything.
 */
public final class JobCatalog {
    private JobCatalog() {}
    public static final int COUNT = 7;
    public static final int MAX_LEVEL = 50;
    public record Job(String id, String label, String icon, String levelObjective,
                      String xpObjective, String needObjective, String[] help,
                      String[] rewards, String[] rewardTypes) {
        public String rewardAt(int level) {
            return level < 1 || level > 50 ? "" : rewards[level - 1];
        }
        public String typeAt(int level) {
            return level < 1 || level > 50 ? "" : rewardTypes[level - 1];
        }
    }
    public static final Job[] JOBS = new Job[] {
        new Job("miner", "MINEUR", "minecraft:iron_pickaxe",
            "fj_m_lvl", "fj_m_xp", "fj_m_need",
            new String[] {"Pioches vanilla et More Ores More Gems", "Minerais precieux : bonus d'XP", "Hammers aux niveaux 20 et 40"},
            new String[] {
                "Tin Pickaxe • Efficiency I", "32 torches + 16 charbon", "4 Cobalt Ingots", "12 lingots de fer", "Magnesium Pickaxe • Efficiency II",
                "Or + bouteilles d’XP", "2 blocs de Magnesium", "Livre Efficiency II", "4 Titanium Quartz", "Titanium Pickaxe enchantée",
                "4 diamants", "5 Electrum Ingots", "Livre Fortune I", "2 blocs de Titanium", "Sapphire Pickaxe • Efficiency III",
                "24 obsidiennes", "5 Shadowsteel Ingots", "3 débris antiques", "5 Thalassium Ingots", "Iron Gold Hammer 3×3",
                "Livre Efficiency IV", "Uranium 238 Ingot • Unbreaking 2", "1 bloc de diamant", "Uranium + Neptunium", "Tungsten Pickaxe • Fortune I",
                "Livre Fortune II", "5 Skysteel", "4 fragments de netherite", "2 Radioactive TNT", "Shadowsteel Pickaxe",
                "32× Obsidian", "2 blocs d’Adamantite", "Enchanted Book • Fortune 3", "5 Aetherium Ingots", "Adamantite Pickaxe • Fortune II",
                "8 débris antiques", "Bloc d’Urantherium", "1 lingot de netherite", "Neptunium Shadowite Shield • Unbreaking 3", "Netherite Diamond Hammer 3×3",
                "3 blocs de diamant", "3 Memory Opals", "5× Ancient Debris", "Uranium 234 Armor Boots • Protection 3", "Skysteel Pickaxe Maître",
                "2 lingots de netherite", "Uranium 234 Armor Helmet • Protection 4", "Netherite Block", "3 blocs d’Urantherium", "Urantherium Pickaxe Ultime"
            },
            new String[] {
                "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ"
            }),
        new Job("lumber", "BUCHERON", "minecraft:iron_axe",
            "fj_l_lvl", "fj_l_xp", "fj_l_need",
            new String[] {"Haches vanilla et More Ores More Gems", "Chaque utilisation de hache donne de l'XP", "Plantes et mobs avec hache : pas d'XP"},
            new String[] {
                "Tin Axe", "16× Oak Sapling + 16× Bone Meal", "5 Jade", "32 charbons de bois", "Magnesium Axe",
                "16 pommes", "Bloc de Jade", "Enchanted Book • Unbreaking 2", "5 Peridot", "Titanium Axe",
                "16 bouteilles de miel", "6 Rose Gold Ingots", "Livre Unbreaking II", "Peridot Boots", "Sapphirel Axe",
                "48 échafaudages", "2 blocs de Rose Gold", "32× Honey Bottle", "6× Rose Gold Ingot", "Tungsten Metal Axe",
                "Enchanted Book • Efficiency 5", "Green Fluorite Armor Chestplate", "2 nids d’abeilles", "2 blocs de Tungsten", "Tungsten Metal Axe",
                "16× Honey Block", "Green Fluorite Armor Leggings", "Enchanted Book • Efficiency 5", "5× Rose Gold Ingot", "Shadowsteel Axe",
                "Brown Shulker Box", "2× Block Of Green Fluorite", "24× Honey Block", "6× Block Of Green Fluorite", "Adamantite Axe",
                "32× Honey Block", "Green Fluorite Armor Helmet", "2× Brown Shulker Box", "4 Luminous Gems", "Thalassium Axe",
                "24× Honey Block", "2 blocs d’Aetherium", "64 bouteilles d’XP", "Green Fluorite Armor Boots • Protection 3", "Skysteel P Axe",
                "2 livres Mending", "Green Fluorite Armor Helmet • Protection 4", "8× Emerald Block", "3× Block Of Green Fluorite", "Urantherium Axe"
            },
            new String[] {
                "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ"
            }),
        new Job("farmer", "FERMIER", "minecraft:golden_hoe",
            "fj_f_lvl", "fj_f_xp", "fj_f_need",
            new String[] {"Houes vanilla et More Ores More Gems", "Recoltes et elevage donnent de l'XP"},
            new String[] {
                "Tin Hoe", "Poudre d’os + graines", "5× Citrine", "16 carottes dorées", "Magnesium Hoe",
                "24 bottes de foin", "Block Of Citrine", "16 émeraudes", "5× Heliodor", "Titanium Hoe",
                "Laisses + Name Tags", "6× Carnelian", "4 pommes dorées", "Jade Boots", "Sapphirel Hoe",
                "Livre Unbreaking III", "2 blocs de Peridot", "2 blocs d’émeraude", "6× Topaz", "Peridot 5 Hoe",
                "64 carottes dorées", "Heliodor 5 Armor Chestplate", "Livre Mending", "6× Sunflare Gem", "Aquamarine Gemstone Hoe",
                "Green Shulker Box", "Peridot Leggings", "8 pommes dorées", "5× Heliodor", "Shadowsteel Hoe",
                "4 blocs d’émeraude", "2 blocs de Rare Sapphire", "48 bouteilles d’XP", "5× Topaz", "Adamantite Hoe",
                "2× Green Shulker Box", "Heliodor 5 Armor Leggings", "Pomme dorée enchantée", "5× Sunflare Gem", "Thalassium Hoe",
                "8 blocs d’émeraude", "2× Block Of Sunflare Gem", "2 livres Mending", "Heliodor 5 Armor Boots • Protection 3", "Skysteel P Hoe",
                "16 pommes dorées", "Heliodor 5 Armor Leggings • Protection 4", "64× Golden Carrot", "3× Block Of Sunflare Gem", "Urantherium Hoe"
            },
            new String[] {
                "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ"
            }),
        new Job("hunter", "CHASSEUR", "minecraft:iron_sword",
            "fj_h_lvl", "fj_h_xp", "fj_h_need",
            new String[] {"Eliminer des creatures donne de l'XP", "Epees MOMG : bonus selon le palier", "Boss et creatures dangereuses : bonus"},
            new String[] {
                "Tin Sword", "48 flèches", "Atf Helmet", "Arc + 64 flèches", "Magnesium Sword",
                "Livre Sharpness II", "Titanium Shield", "3 pommes dorées", "Atf Chestplate", "Titanium Sword",
                "Arbalète + 64 flèches", "Tungsten Shield", "Livre Looting I", "Dea Armed Helmet", "Sapphirel Sword",
                "64 flèches spectrales", "Skysteel Bow", "Totem of Undying", "Dea Armed Chestplate", "Chromium Sword",
                "Livre Sharpness IV", "Thalassium Shield", "Totem Of Undying", "Fbi Armed Boots", "Tungsten Metal Sword",
                "Livre Looting II", "Fbi Armed Chestplate", "2 Totems", "Skysteel Shield", "Shadowsteel Sword",
                "2× Totem Of Undying", "Shadowsteel Leggings", "Enchanted Book • Looting 2", "Atf Vest Boots", "Adamantite Sword",
                "3 Totems", "Atf Vest Chestplate", "3× Totem Of Undying", "Joker Boots • Protection 4", "Thalassium Sword",
                "Livre Looting III", "Dea Armed Boots • Protection 4", "2 pommes dorées enchantées", "Joker Armed Helmet • Protection 4", "Skysteel P Sword",
                "5 Totems", "Joker Armed Chestplate • Protection 4", "Nether Star", "Skysteel Bow Ultime", "Urantherium Sword"
            },
            new String[] {
                "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ"
            }),
        new Job("fisher", "PECHEUR", "minecraft:fishing_rod",
            "fj_p_lvl", "fj_p_xp", "fj_p_need",
            new String[] {"Pecher des poissons avec une canne", "Cannes et tridents ameliores en recompense"},
            new String[] {
                "Aquamarine Boots", "Canne Lure I", "5 Aquamarine", "4 coquilles de nautile", "Aquamarine Sword",
                "16 cristaux de prismarine", "Bloc d’Aquamarine", "Canne Luck I + Lure I", "Blue Opal Boots", "Blue Opal Chestplate",
                "Cœur de la mer", "5 Black Opal", "8 coquilles de nautile", "5 White Opal", "Blue Opal Sword",
                "32 éclats de prismarine", "Bloc de White Opal", "Trident Impaling II", "White Opal Boots", "White Opal Helmet",
                "2 Cœurs de la mer", "4× Fire Opal Gemstone", "Canne Luck II + Lure II", "4× Black Opal", "White Opal Sword",
                "Conduit", "White Opal Leggings", "Trident avancé", "Bloc de Luminous Gem", "Aetherium Leggings",
                "Enchanted Book • Luck Of The Sea 3", "Aetherium Boots", "2 Conduits", "5 Leucosapphire", "Leucosapphire Sword",
                "Trident maître", "Aetherium Helmet", "4× Heart Of The Sea", "5× Leucosapphire Gemstone", "Skysteel Chestplate",
                "Canne ultime", "Skysteel Boots", "8 Cœurs de la mer", "White Opal Gem Armor Boots", "Leucosapphire Armor Chestplate • Power 5, Unbreaking 3, Mending 1",
                "Trident Riptide III", "Leucosapphire Armor Helmet", "2× Conduit", "2× Block Of Leucosapphire", "Urantherium Chestplate"
            },
            new String[] {
                "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL/ÉQUIPEMENT MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ"
            }),
        new Job("explorer", "EXPLORATEUR", "minecraft:compass",
            "fj_e_lvl", "fj_e_xp", "fj_e_need",
            new String[] {"Marche, sprint, nage, Elytra et vehicules", "1 XP tous les 10 blocs parcourus"},
            new String[] {
                "Scout Boots", "8 Ender Pearls", "4× Memory Opal", "Boussole + longue-vue", "Scout Helmet",
                "24 carottes dorées", "Scout Leggings", "32 fusées", "5× Memory Opal", "Scout Chestplate",
                "16 Ender Pearls", "Sniper Leggings", "Recovery Compass", "6× Ametrine", "Sniper Boots",
                "Ender Chest", "Sniper Helmet", "64 fusées", "6 Ametrine", "Sniper Helmet",
                "Light Blue Shulker Box", "Attacker Boots", "6 pommes dorées", "7× Memory Opal", "Sniper Chestplate",
                "2 Ender Chests", "Attacker Chestplate", "2× Light Blue Shulker Box", "7 Rare Sapphire", "Attacker Boots",
                "128 fusées", "Defender Boots", "Élytres Unbreaking II", "8× Rare Sapphire", "Attacker Chestplate",
                "Enchanted Book • Feather Falling 4", "Defender Helmet", "96× Firework Rocket", "8× Memory Opal", "Defender Boots",
                "Élytres Maître", "Scout Leggings", "4× Light Blue Shulker Box", "10× Rare Sapphire", "Defender Chestplate",
                "192 fusées", "Rare Sapphirel Armor Boots", "Dragon Head", "Defender Leggings", "Defender Helmet Maître"
            },
            new String[] {
                "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ"
            }),
        new Job("builder", "CONSTRUCTEUR", "minecraft:bricks",
            "fj_b_lvl", "fj_b_xp", "fj_b_need",
            new String[] {"Chaque bloc pose en Farlands : 1 XP", "Pelles vanilla et MOMG : XP supplementaire"},
            new String[] {
                "Tin Shovel", "32 échafaudages", "16 Chondrit Bricks", "Coffres + tonneaux", "Magnesium Shovel",
                "16 lingots de fer", "4 blocs de Tin", "32 redstone", "4× Block Of Chromium", "Titanium Shovel",
                "32 blocs de quartz", "4× Block Of Cobalt", "64 échafaudages", "4 blocs d’Electrum", "Sapphirel Shovel",
                "32 obsidiennes", "4× Block Of Electrum", "Yellow Shulker Box", "4× Block Of Steel", "Tungsten Metal Shovel",
                "Yellow Shulker Box", "Block Of Cobalt", "Enchanted Book • Silk Touch 1", "4 blocs de Shadowsteel", "Tungsten Metal Shovel",
                "16 blocs de fer", "Block Of Steel", "2× Yellow Shulker Box", "4× Block Of Titanium Quartz", "Shadowsteel Shovel",
                "2 blocs de diamant", "Block Of Titanium Quartz", "Enchanted Book • Silk Touch 1", "4× Block Of Electrum", "Adamantite Shovel",
                "4 Ender Chests", "Block Of Electrum", "4× Yellow Shulker Box", "4 blocs de Skysteel", "Thalassium Shovel",
                "Netherite Upgrade Smithing Template", "2× Block Of Chromium", "4× Diamond Block", "Steel Armor Boots", "Skysteel P Shovel",
                "6 blocs de diamant", "Steel Armor Chestplate", "Beacon", "6× Block Of Chromium", "Urantherium Shovel"
            },
            new String[] {
                "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ",
                "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ", "VANILLA", "MOD PRATIQUE", "VANILLA", "MOD PRATIQUE", "★ OUTIL MODDÉ"
            }),
    };
}

(()=>{
  const rewardMap={"miner:1":[{"id":"more_ores_more_gems:tin_pickaxe","source":"MOMG"}],"miner:2":[{"id":"minecraft:torch","source":"Minecraft"},{"id":"minecraft:coal","source":"Minecraft"}],"miner:3":[{"id":"more_ores_more_gems:cobalt_ingot","source":"MOMG"}],"miner:4":[{"id":"minecraft:iron_ingot","source":"Minecraft"}],"miner:5":[{"id":"more_ores_more_gems:magnesium_pickaxe","source":"MOMG"}],"miner:6":[{"id":"minecraft:gold_ingot","source":"Minecraft"},{"id":"minecraft:experience_bottle","source":"Minecraft"}],"miner:7":[{"id":"more_ores_more_gems:block_of_magnesium","source":"MOMG"}],"miner:8":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"miner:9":[{"id":"more_ores_more_gems:titanium_quartz","source":"MOMG"}],"miner:10":[{"id":"more_ores_more_gems:titanium_pickaxe","source":"MOMG"}],"miner:11":[{"id":"minecraft:diamond","source":"Minecraft"}],"miner:12":[{"id":"more_ores_more_gems:electrum_ingot","source":"MOMG"}],"miner:13":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"miner:14":[{"id":"more_ores_more_gems:blockof_titanium","source":"MOMG"}],"miner:15":[{"id":"more_ores_more_gems:sapphirel_pickaxe","source":"MOMG"}],"miner:16":[{"id":"minecraft:obsidian","source":"Minecraft"}],"miner:17":[{"id":"more_ores_more_gems:shadowsteel_ingot","source":"MOMG"}],"miner:18":[{"id":"minecraft:ancient_debris","source":"Minecraft"}],"miner:19":[{"id":"more_ores_more_gems:thalassium_ingot","source":"MOMG"}],"miner:20":[{"id":"hephaestools:hammer/sandwars_weak","source":"WorldBattles Hammers"}],"miner:21":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"miner:22":[{"id":"more_ores_more_gems:uranium_238_ingot","source":"MOMG"}],"miner:23":[{"id":"minecraft:diamond_block","source":"Minecraft"}],"miner:24":[{"id":"more_ores_more_gems:uranium_238_ingot","source":"MOMG"},{"id":"more_ores_more_gems:neptunium_ingot","source":"MOMG"}],"miner:25":[{"id":"more_ores_more_gems:tungsten_metal_pickaxe","source":"MOMG"}],"miner:26":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"miner:27":[{"id":"more_ores_more_gems:skysteel","source":"MOMG"}],"miner:28":[{"id":"minecraft:netherite_scrap","source":"Minecraft"}],"miner:29":[{"id":"more_ores_more_gems:uranium_radioactive_tnt","source":"MOMG"}],"miner:30":[{"id":"more_ores_more_gems:shadowsteel_pickaxe","source":"MOMG"}],"miner:31":[{"id":"minecraft:obsidian","source":"Minecraft"}],"miner:32":[{"id":"more_ores_more_gems:block_of_adamantite","source":"MOMG"}],"miner:33":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"miner:34":[{"id":"more_ores_more_gems:aetherium_ingot","source":"MOMG"}],"miner:35":[{"id":"more_ores_more_gems:adamantite_pickaxe","source":"MOMG"}],"miner:36":[{"id":"minecraft:ancient_debris","source":"Minecraft"}],"miner:37":[{"id":"more_ores_more_gems:block_of_urantherium","source":"MOMG"}],"miner:38":[{"id":"minecraft:netherite_ingot","source":"Minecraft"}],"miner:39":[{"id":"more_ores_more_gems:neptunium_shadowite_shield","source":"MOMG"}],"miner:40":[{"id":"hephaestools:hammer/netherite","source":"WorldBattles Hammers"}],"miner:41":[{"id":"minecraft:diamond_block","source":"Minecraft"}],"miner:42":[{"id":"more_ores_more_gems:memory_opal","source":"MOMG"}],"miner:43":[{"id":"minecraft:ancient_debris","source":"Minecraft"}],"miner:44":[{"id":"more_ores_more_gems:uranium_234_armor_boots","source":"MOMG"}],"miner:45":[{"id":"more_ores_more_gems:skysteel_p_pickaxe","source":"MOMG"}],"miner:46":[{"id":"minecraft:netherite_ingot","source":"Minecraft"}],"miner:47":[{"id":"more_ores_more_gems:uranium_234_armor_helmet","source":"MOMG"}],"miner:48":[{"id":"minecraft:netherite_block","source":"Minecraft"}],"miner:49":[{"id":"more_ores_more_gems:block_of_urantherium","source":"MOMG"}],"miner:50":[{"id":"more_ores_more_gems:urantherium_pickaxe","source":"MOMG"}],"lumber:1":[{"id":"more_ores_more_gems:tin_axe","source":"MOMG"}],"lumber:2":[{"id":"minecraft:oak_sapling","source":"Minecraft"},{"id":"minecraft:bone_meal","source":"Minecraft"}],"lumber:3":[{"id":"more_ores_more_gems:jade","source":"MOMG"}],"lumber:4":[{"id":"minecraft:charcoal","source":"Minecraft"}],"lumber:5":[{"id":"more_ores_more_gems:magnesium_axe","source":"MOMG"}],"lumber:6":[{"id":"minecraft:apple","source":"Minecraft"}],"lumber:7":[{"id":"more_ores_more_gems:blockof_jade","source":"MOMG"}],"lumber:8":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"lumber:9":[{"id":"more_ores_more_gems:peridot","source":"MOMG"}],"lumber:10":[{"id":"more_ores_more_gems:titanium_axe","source":"MOMG"}],"lumber:11":[{"id":"minecraft:honey_bottle","source":"Minecraft"}],"lumber:12":[{"id":"more_ores_more_gems:rose_gold_ingot","source":"MOMG"}],"lumber:13":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"lumber:14":[{"id":"more_ores_more_gems:peridot_5_armor_boots","source":"MOMG"}],"lumber:15":[{"id":"more_ores_more_gems:sapphirel_axe","source":"MOMG"}],"lumber:16":[{"id":"minecraft:scaffolding","source":"Minecraft"}],"lumber:17":[{"id":"more_ores_more_gems:block_of_rose_gold","source":"MOMG"}],"lumber:18":[{"id":"minecraft:honey_bottle","source":"Minecraft"}],"lumber:19":[{"id":"more_ores_more_gems:rose_gold_ingot","source":"MOMG"}],"lumber:20":[{"id":"more_ores_more_gems:tungsten_metal_axe","source":"MOMG"}],"lumber:21":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"lumber:22":[{"id":"more_ores_more_gems:green_fluorite_armor_chestplate","source":"MOMG"}],"lumber:23":[{"id":"minecraft:bee_nest","source":"Minecraft"}],"lumber:24":[{"id":"more_ores_more_gems:block_of_tungsten","source":"MOMG"}],"lumber:25":[{"id":"more_ores_more_gems:tungsten_metal_axe","source":"MOMG"}],"lumber:26":[{"id":"minecraft:honey_block","source":"Minecraft"}],"lumber:27":[{"id":"more_ores_more_gems:green_fluorite_armor_leggings","source":"MOMG"}],"lumber:28":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"lumber:29":[{"id":"more_ores_more_gems:rose_gold_ingot","source":"MOMG"}],"lumber:30":[{"id":"more_ores_more_gems:shadowsteel_axe","source":"MOMG"}],"lumber:31":[{"id":"minecraft:brown_shulker_box","source":"Minecraft"}],"lumber:32":[{"id":"more_ores_more_gems:block_of_green_fluorite","source":"MOMG"}],"lumber:33":[{"id":"minecraft:honey_block","source":"Minecraft"}],"lumber:34":[{"id":"more_ores_more_gems:block_of_green_fluorite","source":"MOMG"}],"lumber:35":[{"id":"more_ores_more_gems:adamantite_axe","source":"MOMG"}],"lumber:36":[{"id":"minecraft:honey_block","source":"Minecraft"}],"lumber:37":[{"id":"more_ores_more_gems:green_fluorite_armor_helmet","source":"MOMG"}],"lumber:38":[{"id":"minecraft:brown_shulker_box","source":"Minecraft"}],"lumber:39":[{"id":"more_ores_more_gems:luminous_gem","source":"MOMG"}],"lumber:40":[{"id":"more_ores_more_gems:thalassium_axe","source":"MOMG"}],"lumber:41":[{"id":"minecraft:honey_block","source":"Minecraft"}],"lumber:42":[{"id":"more_ores_more_gems:blockof_aetherium","source":"MOMG"}],"lumber:43":[{"id":"minecraft:experience_bottle","source":"Minecraft"}],"lumber:44":[{"id":"more_ores_more_gems:green_fluorite_armor_boots","source":"MOMG"}],"lumber:45":[{"id":"more_ores_more_gems:skysteel_p_axe","source":"MOMG"}],"lumber:46":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"lumber:47":[{"id":"more_ores_more_gems:green_fluorite_armor_helmet","source":"MOMG"}],"lumber:48":[{"id":"minecraft:emerald_block","source":"Minecraft"}],"lumber:49":[{"id":"more_ores_more_gems:block_of_green_fluorite","source":"MOMG"}],"lumber:50":[{"id":"more_ores_more_gems:urantherium_axe","source":"MOMG"}],"farmer:1":[{"id":"more_ores_more_gems:tin_hoe","source":"MOMG"}],"farmer:2":[{"id":"minecraft:bone_meal","source":"Minecraft"},{"id":"minecraft:wheat_seeds","source":"Minecraft"}],"farmer:3":[{"id":"more_ores_more_gems:citrine","source":"MOMG"}],"farmer:4":[{"id":"minecraft:golden_carrot","source":"Minecraft"}],"farmer:5":[{"id":"more_ores_more_gems:magnesium_hoe","source":"MOMG"}],"farmer:6":[{"id":"minecraft:hay_block","source":"Minecraft"}],"farmer:7":[{"id":"more_ores_more_gems:block_of_citrine","source":"MOMG"}],"farmer:8":[{"id":"minecraft:emerald","source":"Minecraft"}],"farmer:9":[{"id":"more_ores_more_gems:heliodor","source":"MOMG"}],"farmer:10":[{"id":"more_ores_more_gems:titanium_hoe","source":"MOMG"}],"farmer:11":[{"id":"minecraft:lead","source":"Minecraft"},{"id":"minecraft:name_tag","source":"Minecraft"}],"farmer:12":[{"id":"more_ores_more_gems:carnelian","source":"MOMG"}],"farmer:13":[{"id":"minecraft:golden_apple","source":"Minecraft"}],"farmer:14":[{"id":"more_ores_more_gems:jade_5_armor_boots","source":"MOMG"}],"farmer:15":[{"id":"more_ores_more_gems:sapphirel_hoe","source":"MOMG"}],"farmer:16":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"farmer:17":[{"id":"more_ores_more_gems:block_of_peridot","source":"MOMG"}],"farmer:18":[{"id":"minecraft:emerald_block","source":"Minecraft"}],"farmer:19":[{"id":"more_ores_more_gems:topaz","source":"MOMG"}],"farmer:20":[{"id":"more_ores_more_gems:peridot_5_hoe","source":"MOMG"}],"farmer:21":[{"id":"minecraft:golden_carrot","source":"Minecraft"}],"farmer:22":[{"id":"more_ores_more_gems:heliodor_5_armor_chestplate","source":"MOMG"}],"farmer:23":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"farmer:24":[{"id":"more_ores_more_gems:sunflare_gem","source":"MOMG"}],"farmer:25":[{"id":"more_ores_more_gems:aquamarine_gemstone_hoe","source":"MOMG"}],"farmer:26":[{"id":"minecraft:green_shulker_box","source":"Minecraft"}],"farmer:27":[{"id":"more_ores_more_gems:peridot_5_armor_leggings","source":"MOMG"}],"farmer:28":[{"id":"minecraft:golden_apple","source":"Minecraft"}],"farmer:29":[{"id":"more_ores_more_gems:heliodor","source":"MOMG"}],"farmer:30":[{"id":"more_ores_more_gems:shadowsteel_hoe","source":"MOMG"}],"farmer:31":[{"id":"minecraft:emerald_block","source":"Minecraft"}],"farmer:32":[{"id":"more_ores_more_gems:block_of_rare_sapphire","source":"MOMG"}],"farmer:33":[{"id":"minecraft:experience_bottle","source":"Minecraft"}],"farmer:34":[{"id":"more_ores_more_gems:topaz","source":"MOMG"}],"farmer:35":[{"id":"more_ores_more_gems:adamantite_hoe","source":"MOMG"}],"farmer:36":[{"id":"minecraft:green_shulker_box","source":"Minecraft"}],"farmer:37":[{"id":"more_ores_more_gems:heliodor_5_armor_leggings","source":"MOMG"}],"farmer:38":[{"id":"minecraft:enchanted_golden_apple","source":"Minecraft"}],"farmer:39":[{"id":"more_ores_more_gems:sunflare_gem","source":"MOMG"}],"farmer:40":[{"id":"more_ores_more_gems:thalassium_hoe","source":"MOMG"}],"farmer:41":[{"id":"minecraft:emerald_block","source":"Minecraft"}],"farmer:42":[{"id":"more_ores_more_gems:block_of_sunflare_gem","source":"MOMG"}],"farmer:43":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"farmer:44":[{"id":"more_ores_more_gems:heliodor_5_armor_boots","source":"MOMG"}],"farmer:45":[{"id":"more_ores_more_gems:skysteel_p_hoe","source":"MOMG"}],"farmer:46":[{"id":"minecraft:golden_apple","source":"Minecraft"}],"farmer:47":[{"id":"more_ores_more_gems:heliodor_5_armor_leggings","source":"MOMG"}],"farmer:48":[{"id":"minecraft:golden_carrot","source":"Minecraft"}],"farmer:49":[{"id":"more_ores_more_gems:block_of_sunflare_gem","source":"MOMG"}],"farmer:50":[{"id":"more_ores_more_gems:urantherium_hoe","source":"MOMG"}],"hunter:1":[{"id":"more_ores_more_gems:tin_sword","source":"MOMG"}],"hunter:2":[{"id":"minecraft:arrow","source":"Minecraft"}],"hunter:3":[{"id":"lrarmor:atf_helmet","source":"LR Armor"}],"hunter:4":[{"id":"minecraft:bow","source":"Minecraft"},{"id":"minecraft:arrow","source":"Minecraft"}],"hunter:5":[{"id":"more_ores_more_gems:magnesium_sword","source":"MOMG"}],"hunter:6":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"hunter:7":[{"id":"more_ores_more_gems:titanium_shield","source":"MOMG"}],"hunter:8":[{"id":"minecraft:golden_apple","source":"Minecraft"}],"hunter:9":[{"id":"lrarmor:atf_chestplate","source":"LR Armor"}],"hunter:10":[{"id":"more_ores_more_gems:titanium_sword","source":"MOMG"}],"hunter:11":[{"id":"minecraft:crossbow","source":"Minecraft"},{"id":"minecraft:arrow","source":"Minecraft"}],"hunter:12":[{"id":"more_ores_more_gems:tungsten_shield","source":"MOMG"}],"hunter:13":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"hunter:14":[{"id":"lrarmor:dea_armed_helmet","source":"LR Armor"}],"hunter:15":[{"id":"more_ores_more_gems:sapphirel_sword","source":"MOMG"}],"hunter:16":[{"id":"minecraft:spectral_arrow","source":"Minecraft"}],"hunter:17":[{"id":"more_ores_more_gems:skysteel_bow","source":"MOMG"}],"hunter:18":[{"id":"minecraft:totem_of_undying","source":"Minecraft"}],"hunter:19":[{"id":"lrarmor:dea_armed_chestplate","source":"LR Armor"}],"hunter:20":[{"id":"more_ores_more_gems:chromium_sword","source":"MOMG"}],"hunter:21":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"hunter:22":[{"id":"more_ores_more_gems:thalassium_shield","source":"MOMG"}],"hunter:23":[{"id":"minecraft:totem_of_undying","source":"Minecraft"}],"hunter:24":[{"id":"lrarmor:fbi_armed_boots","source":"LR Armor"}],"hunter:25":[{"id":"more_ores_more_gems:tungsten_metal_sword","source":"MOMG"}],"hunter:26":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"hunter:27":[{"id":"lrarmor:fbi_armed_chestplate","source":"LR Armor"}],"hunter:28":[{"id":"minecraft:totem_of_undying","source":"Minecraft"}],"hunter:29":[{"id":"more_ores_more_gems:skysteel_shield","source":"MOMG"}],"hunter:30":[{"id":"more_ores_more_gems:shadowsteel_sword","source":"MOMG"}],"hunter:31":[{"id":"minecraft:totem_of_undying","source":"Minecraft"}],"hunter:32":[{"id":"more_ores_more_gems:shadowsteel_armor_leggings","source":"MOMG"}],"hunter:33":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"hunter:34":[{"id":"lrarmor:atf_vest_boots","source":"LR Armor"}],"hunter:35":[{"id":"more_ores_more_gems:adamantite_sword","source":"MOMG"}],"hunter:36":[{"id":"minecraft:totem_of_undying","source":"Minecraft"}],"hunter:37":[{"id":"lrarmor:atf_vest_chestplate","source":"LR Armor"}],"hunter:38":[{"id":"minecraft:totem_of_undying","source":"Minecraft"}],"hunter:39":[{"id":"lrarmor:joker_boots","source":"LR Armor"}],"hunter:40":[{"id":"more_ores_more_gems:thalassium_sword","source":"MOMG"}],"hunter:41":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"hunter:42":[{"id":"lrarmor:dea_armed_boots","source":"LR Armor"}],"hunter:43":[{"id":"minecraft:enchanted_golden_apple","source":"Minecraft"}],"hunter:44":[{"id":"lrarmor:joker_armed_helmet","source":"LR Armor"}],"hunter:45":[{"id":"more_ores_more_gems:skysteel_p_sword","source":"MOMG"}],"hunter:46":[{"id":"minecraft:totem_of_undying","source":"Minecraft"}],"hunter:47":[{"id":"lrarmor:joker_armed_chestplate","source":"LR Armor"}],"hunter:48":[{"id":"minecraft:nether_star","source":"Minecraft"}],"hunter:49":[{"id":"more_ores_more_gems:skysteel_bow","source":"MOMG"}],"hunter:50":[{"id":"more_ores_more_gems:urantherium_sword","source":"MOMG"}],"fisher:1":[{"id":"more_ores_more_gems:aquamarine_gemstone_armor_boots","source":"MOMG"}],"fisher:2":[{"id":"minecraft:fishing_rod","source":"Minecraft"}],"fisher:3":[{"id":"more_ores_more_gems:aquamarine","source":"MOMG"}],"fisher:4":[{"id":"minecraft:nautilus_shell","source":"Minecraft"}],"fisher:5":[{"id":"more_ores_more_gems:aquamarine_gemstone_sword","source":"MOMG"}],"fisher:6":[{"id":"minecraft:prismarine_crystals","source":"Minecraft"}],"fisher:7":[{"id":"more_ores_more_gems:block_of_aquamarine","source":"MOMG"}],"fisher:8":[{"id":"minecraft:fishing_rod","source":"Minecraft"}],"fisher:9":[{"id":"more_ores_more_gems:blue_opal_armor_boots","source":"MOMG"}],"fisher:10":[{"id":"more_ores_more_gems:blue_opal_armor_chestplate","source":"MOMG"}],"fisher:11":[{"id":"minecraft:heart_of_the_sea","source":"Minecraft"}],"fisher:12":[{"id":"more_ores_more_gems:black_opal","source":"MOMG"}],"fisher:13":[{"id":"minecraft:nautilus_shell","source":"Minecraft"}],"fisher:14":[{"id":"more_ores_more_gems:white_opal","source":"MOMG"}],"fisher:15":[{"id":"more_ores_more_gems:blue_opal_sword","source":"MOMG"}],"fisher:16":[{"id":"minecraft:prismarine_shard","source":"Minecraft"}],"fisher:17":[{"id":"more_ores_more_gems:blockof_white_opal","source":"MOMG"}],"fisher:18":[{"id":"minecraft:trident","source":"Minecraft"}],"fisher:19":[{"id":"more_ores_more_gems:white_opal_gem_armor_boots","source":"MOMG"}],"fisher:20":[{"id":"more_ores_more_gems:white_opal_gem_armor_helmet","source":"MOMG"}],"fisher:21":[{"id":"minecraft:heart_of_the_sea","source":"Minecraft"}],"fisher:22":[{"id":"more_ores_more_gems:fire_opal_gemstone","source":"MOMG"}],"fisher:23":[{"id":"minecraft:fishing_rod","source":"Minecraft"}],"fisher:24":[{"id":"more_ores_more_gems:black_opal","source":"MOMG"}],"fisher:25":[{"id":"more_ores_more_gems:white_opal_gem_sword","source":"MOMG"}],"fisher:26":[{"id":"minecraft:conduit","source":"Minecraft"}],"fisher:27":[{"id":"more_ores_more_gems:white_opal_gem_armor_leggings","source":"MOMG"}],"fisher:28":[{"id":"minecraft:trident","source":"Minecraft"}],"fisher:29":[{"id":"more_ores_more_gems:block_of_luminous_gem","source":"MOMG"}],"fisher:30":[{"id":"more_ores_more_gems:aetherium_armor_leggings","source":"MOMG"}],"fisher:31":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"fisher:32":[{"id":"more_ores_more_gems:aetherium_armor_boots","source":"MOMG"}],"fisher:33":[{"id":"minecraft:conduit","source":"Minecraft"}],"fisher:34":[{"id":"more_ores_more_gems:leucosapphire_gemstone","source":"MOMG"}],"fisher:35":[{"id":"more_ores_more_gems:leucosapphire_sword","source":"MOMG"}],"fisher:36":[{"id":"minecraft:trident","source":"Minecraft"}],"fisher:37":[{"id":"more_ores_more_gems:aetherium_armor_helmet","source":"MOMG"}],"fisher:38":[{"id":"minecraft:heart_of_the_sea","source":"Minecraft"}],"fisher:39":[{"id":"more_ores_more_gems:leucosapphire_gemstone","source":"MOMG"}],"fisher:40":[{"id":"more_ores_more_gems:skysteel_5_armor_chestplate","source":"MOMG"}],"fisher:41":[{"id":"minecraft:fishing_rod","source":"Minecraft"}],"fisher:42":[{"id":"more_ores_more_gems:skysteel_5_armor_boots","source":"MOMG"}],"fisher:43":[{"id":"minecraft:heart_of_the_sea","source":"Minecraft"}],"fisher:44":[{"id":"more_ores_more_gems:white_opal_gem_armor_boots","source":"MOMG"}],"fisher:45":[{"id":"more_ores_more_gems:leucosapphire_armor_chestplate","source":"MOMG"}],"fisher:46":[{"id":"minecraft:trident","source":"Minecraft"}],"fisher:47":[{"id":"more_ores_more_gems:leucosapphire_armor_helmet","source":"MOMG"}],"fisher:48":[{"id":"minecraft:conduit","source":"Minecraft"}],"fisher:49":[{"id":"more_ores_more_gems:block_of_leucosapphire","source":"MOMG"}],"fisher:50":[{"id":"more_ores_more_gems:urantherium_armor_chestplate","source":"MOMG"}],"explorer:1":[{"id":"lrarmor:scout_boots","source":"LR Armor"}],"explorer:2":[{"id":"minecraft:ender_pearl","source":"Minecraft"}],"explorer:3":[{"id":"more_ores_more_gems:memory_opal","source":"MOMG"}],"explorer:4":[{"id":"minecraft:compass","source":"Minecraft"},{"id":"minecraft:spyglass","source":"Minecraft"}],"explorer:5":[{"id":"lrarmor:scout_helmet","source":"LR Armor"}],"explorer:6":[{"id":"minecraft:golden_carrot","source":"Minecraft"}],"explorer:7":[{"id":"lrarmor:scout_leggings","source":"LR Armor"}],"explorer:8":[{"id":"minecraft:firework_rocket","source":"Minecraft"}],"explorer:9":[{"id":"more_ores_more_gems:memory_opal","source":"MOMG"}],"explorer:10":[{"id":"lrarmor:scout_chestplate","source":"LR Armor"}],"explorer:11":[{"id":"minecraft:ender_pearl","source":"Minecraft"}],"explorer:12":[{"id":"lrarmor:sniper_leggings","source":"LR Armor"}],"explorer:13":[{"id":"minecraft:recovery_compass","source":"Minecraft"}],"explorer:14":[{"id":"more_ores_more_gems:ametrine","source":"MOMG"}],"explorer:15":[{"id":"lrarmor:sniper_boots","source":"LR Armor"}],"explorer:16":[{"id":"minecraft:ender_chest","source":"Minecraft"}],"explorer:17":[{"id":"lrarmor:sniper_helmet","source":"LR Armor"}],"explorer:18":[{"id":"minecraft:firework_rocket","source":"Minecraft"}],"explorer:19":[{"id":"more_ores_more_gems:ametrine","source":"MOMG"}],"explorer:20":[{"id":"lrarmor:sniper_helmet","source":"LR Armor"}],"explorer:21":[{"id":"minecraft:light_blue_shulker_box","source":"Minecraft"}],"explorer:22":[{"id":"lrarmor:attacker_boots","source":"LR Armor"}],"explorer:23":[{"id":"minecraft:golden_apple","source":"Minecraft"}],"explorer:24":[{"id":"more_ores_more_gems:memory_opal","source":"MOMG"}],"explorer:25":[{"id":"lrarmor:sniper_chestplate","source":"LR Armor"}],"explorer:26":[{"id":"minecraft:ender_chest","source":"Minecraft"}],"explorer:27":[{"id":"lrarmor:attacker_chestplate","source":"LR Armor"}],"explorer:28":[{"id":"minecraft:light_blue_shulker_box","source":"Minecraft"}],"explorer:29":[{"id":"more_ores_more_gems:rare_sapphire","source":"MOMG"}],"explorer:30":[{"id":"lrarmor:attacker_boots","source":"LR Armor"}],"explorer:31":[{"id":"minecraft:firework_rocket","source":"Minecraft"}],"explorer:32":[{"id":"lrarmor:defender_boots","source":"LR Armor"}],"explorer:33":[{"id":"minecraft:elytra","source":"Minecraft"}],"explorer:34":[{"id":"more_ores_more_gems:rare_sapphire","source":"MOMG"}],"explorer:35":[{"id":"lrarmor:attacker_chestplate","source":"LR Armor"}],"explorer:36":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"explorer:37":[{"id":"lrarmor:defender_helmet","source":"LR Armor"}],"explorer:38":[{"id":"minecraft:firework_rocket","source":"Minecraft"}],"explorer:39":[{"id":"more_ores_more_gems:memory_opal","source":"MOMG"}],"explorer:40":[{"id":"lrarmor:defender_boots","source":"LR Armor"}],"explorer:41":[{"id":"minecraft:elytra","source":"Minecraft"}],"explorer:42":[{"id":"lrarmor:scout_leggings","source":"LR Armor"}],"explorer:43":[{"id":"minecraft:light_blue_shulker_box","source":"Minecraft"}],"explorer:44":[{"id":"more_ores_more_gems:rare_sapphire","source":"MOMG"}],"explorer:45":[{"id":"lrarmor:defender_chestplate","source":"LR Armor"}],"explorer:46":[{"id":"minecraft:firework_rocket","source":"Minecraft"}],"explorer:47":[{"id":"more_ores_more_gems:rare_sapphirel_armor_boots","source":"MOMG"}],"explorer:48":[{"id":"minecraft:dragon_head","source":"Minecraft"}],"explorer:49":[{"id":"lrarmor:defender_leggings","source":"LR Armor"}],"explorer:50":[{"id":"lrarmor:defender_helmet","source":"LR Armor"}],"builder:1":[{"id":"more_ores_more_gems:tin_shovel","source":"MOMG"}],"builder:2":[{"id":"minecraft:scaffolding","source":"Minecraft"}],"builder:3":[{"id":"more_ores_more_gems:chondrit_brick","source":"MOMG"}],"builder:4":[{"id":"minecraft:chest","source":"Minecraft"},{"id":"minecraft:barrel","source":"Minecraft"}],"builder:5":[{"id":"more_ores_more_gems:magnesium_shovel","source":"MOMG"}],"builder:6":[{"id":"minecraft:iron_ingot","source":"Minecraft"}],"builder:7":[{"id":"more_ores_more_gems:block_of_tin","source":"MOMG"}],"builder:8":[{"id":"minecraft:redstone","source":"Minecraft"}],"builder:9":[{"id":"more_ores_more_gems:block_of_chromium","source":"MOMG"}],"builder:10":[{"id":"more_ores_more_gems:titanium_shovel","source":"MOMG"}],"builder:11":[{"id":"minecraft:quartz_block","source":"Minecraft"}],"builder:12":[{"id":"more_ores_more_gems:block_of_cobalt","source":"MOMG"}],"builder:13":[{"id":"minecraft:scaffolding","source":"Minecraft"}],"builder:14":[{"id":"more_ores_more_gems:block_of_electrum","source":"MOMG"}],"builder:15":[{"id":"more_ores_more_gems:sapphirel_shovel","source":"MOMG"}],"builder:16":[{"id":"minecraft:obsidian","source":"Minecraft"}],"builder:17":[{"id":"more_ores_more_gems:block_of_electrum","source":"MOMG"}],"builder:18":[{"id":"minecraft:yellow_shulker_box","source":"Minecraft"}],"builder:19":[{"id":"more_ores_more_gems:block_of_steel","source":"MOMG"}],"builder:20":[{"id":"more_ores_more_gems:tungsten_metal_shovel","source":"MOMG"}],"builder:21":[{"id":"minecraft:yellow_shulker_box","source":"Minecraft"}],"builder:22":[{"id":"more_ores_more_gems:block_of_cobalt","source":"MOMG"}],"builder:23":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"builder:24":[{"id":"more_ores_more_gems:block_of_shadowsteel","source":"MOMG"}],"builder:25":[{"id":"more_ores_more_gems:tungsten_metal_shovel","source":"MOMG"}],"builder:26":[{"id":"minecraft:iron_block","source":"Minecraft"}],"builder:27":[{"id":"more_ores_more_gems:block_of_steel","source":"MOMG"}],"builder:28":[{"id":"minecraft:yellow_shulker_box","source":"Minecraft"}],"builder:29":[{"id":"more_ores_more_gems:block_of_titanium_quartz","source":"MOMG"}],"builder:30":[{"id":"more_ores_more_gems:shadowsteel_shovel","source":"MOMG"}],"builder:31":[{"id":"minecraft:diamond_block","source":"Minecraft"}],"builder:32":[{"id":"more_ores_more_gems:block_of_titanium_quartz","source":"MOMG"}],"builder:33":[{"id":"minecraft:enchanted_book","source":"Minecraft"}],"builder:34":[{"id":"more_ores_more_gems:block_of_electrum","source":"MOMG"}],"builder:35":[{"id":"more_ores_more_gems:adamantite_shovel","source":"MOMG"}],"builder:36":[{"id":"minecraft:ender_chest","source":"Minecraft"}],"builder:37":[{"id":"more_ores_more_gems:block_of_electrum","source":"MOMG"}],"builder:38":[{"id":"minecraft:yellow_shulker_box","source":"Minecraft"}],"builder:39":[{"id":"more_ores_more_gems:block_of_skysteel","source":"MOMG"}],"builder:40":[{"id":"more_ores_more_gems:thalassium_shovel","source":"MOMG"}],"builder:41":[{"id":"minecraft:netherite_upgrade_smithing_template","source":"Minecraft"}],"builder:42":[{"id":"more_ores_more_gems:block_of_chromium","source":"MOMG"}],"builder:43":[{"id":"minecraft:diamond_block","source":"Minecraft"}],"builder:44":[{"id":"more_ores_more_gems:steel_armor_boots","source":"MOMG"}],"builder:45":[{"id":"more_ores_more_gems:skysteel_p_shovel","source":"MOMG"}],"builder:46":[{"id":"minecraft:diamond_block","source":"Minecraft"}],"builder:47":[{"id":"more_ores_more_gems:steel_armor_chestplate","source":"MOMG"}],"builder:48":[{"id":"minecraft:beacon","source":"Minecraft"}],"builder:49":[{"id":"more_ores_more_gems:block_of_chromium","source":"MOMG"}],"builder:50":[{"id":"more_ores_more_gems:urantherium_shovel","source":"MOMG"}]};
  const jobNames={miner:['⛏️ Mineur','⛏️ Miner'],lumber:['🪓 Bûcheron','🪓 Lumberjack'],farmer:['🌾 Fermier','🌾 Farmer'],hunter:['⚔️ Chasseur','⚔️ Hunter'],fisher:['🎣 Pêcheur','🎣 Fisher'],explorer:['🧭 Explorateur','🧭 Explorer'],builder:['🧱 Constructeur','🧱 Builder']};
  const state={filter:'all',range:null};

  function cleanName(item){
    const node=item.querySelector('.iname');
    if(!node) return item.querySelector('img')?.alt||'Item';
    const clone=node.cloneNode(true);
    clone.querySelectorAll('.count').forEach(x=>x.remove());
    return clone.textContent.trim()||item.querySelector('img')?.alt||'Item';
  }
  function categoryFor(name,id){
    const v=(name+' '+id).toLowerCase().replace(/_/g,' ');
    if(/helmet|chestplate|leggings|boots|shield|armor|casque|plastron|jambi|bottes/.test(v)) return 'armor';
    if(/pickaxe|\baxe\b|\bhoe\b|shovel|sword|hammer|bow|crossbow|fishing rod|fishing_rod|shears|trident/.test(v)) return 'tools';
    return 'resources';
  }
  function sourceSearch(source){
    if(source==='MOMG') return 'momg more ores more gems';
    if(source==='LR Armor') return 'lr armor lrarmor';
    if(source==='WorldBattles Hammers') return 'worldbattles hammers hephaestools hammer';
    return source.toLowerCase();
  }
  function ensureToolbar(page){
    const toolbar=page.querySelector('.reward-toolbar');
    const search=document.getElementById('reward-search');
    if(!toolbar||!search) return;
    toolbar.classList.add('reward-toolbar-v140');
    const original=toolbar.querySelector('div');
    if(original && !toolbar.querySelector('.reward-toolbar-title-v140')){
      original.classList.add('reward-toolbar-title-v140');
      const count=document.createElement('span');
      count.id='reward-results-count-v140';
      count.setAttribute('aria-live','polite');
      original.appendChild(count);
    }
    search.autocomplete='off';
    if(!page.querySelector('.reward-filters-v140')){
      const filters=document.createElement('div');
      filters.className='reward-filters-v140';
      filters.setAttribute('aria-label','Filtres de récompenses');
      filters.innerHTML='<button class="reward-filter-v140 active" data-reward-filter-v140="all" type="button"><span class="jobs-fr-v140">Tous</span><span class="jobs-en-v140">All</span></button><button class="reward-filter-v140" data-reward-filter-v140="tools" type="button">⚒ <span class="jobs-fr-v140">Outils</span><span class="jobs-en-v140">Tools</span></button><button class="reward-filter-v140" data-reward-filter-v140="armor" type="button">🛡 <span class="jobs-fr-v140">Armures</span><span class="jobs-en-v140">Armor</span></button><button class="reward-filter-v140" data-reward-filter-v140="resources" type="button">◆ <span class="jobs-fr-v140">Ressources</span><span class="jobs-en-v140">Resources</span></button><button class="reward-filter-v140" data-reward-filter-v140="vanilla" type="button">🟩 Vanilla</button><button class="reward-filter-v140" data-reward-filter-v140="modded" type="button">✦ <span class="jobs-fr-v140">Moddé</span><span class="jobs-en-v140">Modded</span></button>';
      toolbar.insertAdjacentElement('afterend',filters);
    }
  }
  function prepareRows(page){
    page.querySelectorAll('.job-panel').forEach(panel=>{
      const job=panel.id.replace('job-','');
      const ranges=panel.querySelector('.ranges');
      if(ranges && !panel.querySelector('.jobs-search-label-v140')){
        const label=document.createElement('div');
        label.className='jobs-search-label-v140';
        label.dataset.jobLabelV140=job;
        ranges.insertAdjacentElement('afterend',label);
      }
      panel.querySelectorAll('.row').forEach(row=>{
        const lvl=Number(row.querySelector('.lvl .n')?.textContent||0);
        if(lvl>0 && lvl%5===0) row.classList.add('milestone-v140');
        if(lvl===50) row.classList.add('level-50-v140');
        const mapped=rewardMap[job+':'+lvl]||[];
        const cats=new Set(),sources=new Set(),ids=[];
        row.querySelectorAll('.item').forEach((item,index)=>{
          const info=mapped[index]||{id:'',source:'Moddé'};
          const name=cleanName(item);
          const cat=categoryFor(name,info.id);
          cats.add(cat); sources.add(info.source); ids.push(info.id);
          item.dataset.jobSourceV140=info.source;
          item.dataset.jobItemIdV140=info.id;
          item.dataset.jobCategoryV140=cat;
          item.tabIndex=0;
          item.setAttribute('aria-label',name+' — '+info.source);
        });
        row.dataset.rewardCategoriesV140=[...cats].join(' ');
        row.dataset.rewardSourcesV140=[...sources].join(' ');
        row.dataset.rewardIdsV140=ids.join(' ');
        row.dataset.rewardSearchV140=((row.dataset.search||'')+' '+ids.join(' ')+' '+[...sources].map(sourceSearch).join(' ')).toLowerCase();
      });
    });
  }
  function setSearchLabels(page){
    const en=document.documentElement.lang==='en';
    page.querySelectorAll('[data-job-label-v140]').forEach(el=>{
      const names=jobNames[el.dataset.jobLabelV140]||[el.dataset.jobLabelV140,el.dataset.jobLabelV140];
      el.textContent=(en?'SEARCH RESULTS — ':'RÉSULTATS — ')+(en?names[1]:names[0]);
    });
  }
  function rowMatchesFilter(row){
    if(state.filter==='all') return true;
    if(state.filter==='vanilla') return (row.dataset.rewardSourcesV140||'').split(' ').includes('Minecraft');
    if(state.filter==='modded') return (row.dataset.rewardSourcesV140||'').split(' ').some(x=>x && x!=='Minecraft');
    return (row.dataset.rewardCategoriesV140||'').split(' ').includes(state.filter);
  }
  function rowMatchesRange(row){
    if(!state.range) return true;
    const n=Number(row.querySelector('.lvl .n')?.textContent||0);
    return n>=state.range[0]&&n<=state.range[1];
  }
  function applyView(page){
    const search=document.getElementById('reward-search');
    const q=(search?.value||'').toLowerCase().trim();
    const searching=!!q;
    page.classList.toggle('jobs-search-mode-v140',searching);
    let visible=0;
    page.querySelectorAll('.job-panel').forEach(panel=>{
      let panelVisible=0;
      panel.querySelectorAll('.row').forEach(row=>{
        const text=row.dataset.rewardSearchV140||((row.dataset.search||'')+' '+row.textContent).toLowerCase();
        const okSearch=!q||text.includes(q);
        const okFilter=rowMatchesFilter(row);
        const okRange=searching?true:rowMatchesRange(row);
        const show=okSearch&&okFilter&&okRange;
        row.classList.toggle('hidden',!show);
        if(show){panelVisible++;visible++;}
      });
      panel.classList.toggle('jobs-search-visible-v140',searching&&panelVisible>0);
    });
    const noResults=page.querySelector('.jobs-no-results-v140');
    if(noResults) noResults.classList.toggle('show',visible===0);
    const count=document.getElementById('reward-results-count-v140');
    if(count){
      const en=document.documentElement.lang==='en';
      count.textContent=(q||state.filter!=='all'||state.range)?'• '+visible+' '+(en?'results':'résultats'):'';
    }
  }
  function setupTooltip(page){
    let tip=document.getElementById('job-item-tooltip-v140');
    if(!tip){tip=document.createElement('div');tip.id='job-item-tooltip-v140';tip.setAttribute('role','tooltip');document.body.appendChild(tip);}
    function render(item){
      const name=cleanName(item),count=item.querySelector('.count')?.textContent.trim()||'×1';
      const ench=item.querySelector('.ench')?.textContent.trim()||'';
      const source=item.dataset.jobSourceV140||'Moddé';
      const en=document.documentElement.lang==='en';
      tip.innerHTML='<span class="tt-name-v140"></span><div class="tt-meta-v140"><span class="tt-count-v140"></span><span>•</span><span class="tt-source-v140"></span></div><div class="tt-ench-v140"></div>';
      tip.querySelector('.tt-name-v140').textContent=name;
      tip.querySelector('.tt-count-v140').textContent=(en?'Quantity ':'Quantité ')+count.replace('×','×');
      tip.querySelector('.tt-source-v140').textContent=(en?'Source: ':'Origine : ')+source;
      const enchEl=tip.querySelector('.tt-ench-v140');
      enchEl.textContent=ench?(en?'Enchantments: ':'Enchantements : ')+ench:'';
      enchEl.style.display=ench?'block':'none';
      tip.classList.add('show');
    }
    function place(x,y){
      const pad=12,off=14;
      tip.style.left='0px';tip.style.top='0px';
      const r=tip.getBoundingClientRect();
      let left=x+off,top=y+off;
      if(left+r.width>innerWidth-pad) left=x-r.width-off;
      if(top+r.height>innerHeight-pad) top=y-r.height-off;
      tip.style.left=Math.max(pad,left)+'px';tip.style.top=Math.max(pad,top)+'px';
    }
    page.querySelectorAll('.item').forEach(item=>{
      item.addEventListener('pointerenter',e=>{render(item);place(e.clientX,e.clientY)});
      item.addEventListener('pointermove',e=>place(e.clientX,e.clientY));
      item.addEventListener('pointerleave',()=>tip.classList.remove('show'));
      item.addEventListener('focus',()=>{render(item);const r=item.getBoundingClientRect();place(r.left+r.width/2,r.bottom)});
      item.addEventListener('blur',()=>tip.classList.remove('show'));
    });
    window.addEventListener('scroll',()=>tip.classList.remove('show'),{passive:true});
  }
  function initJobsV140(){
    const page=document.querySelector('.spa-page[data-route="jobs"]');
    if(!page||page.dataset.jobsV140Ready==='1') return;
    page.dataset.jobsV140Ready='1';
    ensureToolbar(page);
    prepareRows(page);
    setSearchLabels(page);
    const panels=[...page.querySelectorAll('.job-panel')];
    const lastPanel=panels[panels.length-1];
    if(lastPanel){
      const no=document.createElement('div');
      no.className='jobs-no-results-v140';
      no.innerHTML='<span class="jobs-fr-v140">Aucune récompense ne correspond à ta recherche ou à ce filtre.</span><span class="jobs-en-v140">No reward matches your search or this filter.</span>';
      lastPanel.insertAdjacentElement('afterend',no);
    }
    page.querySelectorAll('[data-reward-filter-v140]').forEach(btn=>btn.addEventListener('click',()=>{
      state.filter=btn.dataset.rewardFilterV140;
      page.querySelectorAll('[data-reward-filter-v140]').forEach(x=>x.classList.toggle('active',x===btn));
      applyView(page);
    }));
    const search=document.getElementById('reward-search');
    if(search) search.addEventListener('input',()=>{state.range=null;page.querySelectorAll('.range').forEach(x=>x.classList.remove('active-v140'));applyView(page)});
    page.querySelectorAll('[data-job]').forEach(btn=>btn.addEventListener('click',()=>{state.range=null;page.querySelectorAll('.range').forEach(x=>x.classList.remove('active-v140'));setTimeout(()=>applyView(page),0)}));
    page.querySelectorAll('[data-range]').forEach(btn=>btn.addEventListener('click',()=>{
      state.range=btn.dataset.range.split('-').map(Number);
      page.querySelectorAll('.range').forEach(x=>x.classList.toggle('active-v140',x===btn));
      setTimeout(()=>applyView(page),0);
    }));
    setupTooltip(page);
    applyView(page);
    const langObserver=new MutationObserver(()=>{setSearchLabels(page);applyView(page)});
    langObserver.observe(document.documentElement,{attributes:true,attributeFilter:['lang']});
  }
  if(document.readyState==='loading') document.addEventListener('DOMContentLoaded',initJobsV140);
  else initJobsV140();
})();
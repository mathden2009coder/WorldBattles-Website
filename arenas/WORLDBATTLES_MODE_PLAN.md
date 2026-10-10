# WorldBattles Arenas — World Battles mode

Status: development plan. Zombies implementation is preserved and must remain functional.

## Match rules
- 3 independent arena instances, maximum 10 participants per instance.
- 2 teams (Overworld and Nether), randomized and kept as balanced as possible.
- 120 seconds in each arena's waiting room; 5 seconds of preparation after teleporting to team starts.
- First team to 25 player eliminations wins the round.
- First team to win 2 rounds wins the match (maximum three rounds).
- Protected team respawns after death, with no escape through player commands.
- Spectators/eliminated participants cannot roam unrestricted.
- Match and between-round announcements use center-screen titles; scores are visible on dedicated bossbars or equivalent.
- Overworld minigame inventory is cleared upon entry and exit, without touching inventories saved in Freelands and Minage.
- Arena cleanup and independent structure restoration before the next round and after the match; arena remains locked on failure.
- Preserve 3 independent Zombies arenas unchanged.

## Source structure
- Minecraft saved template: `minecraft:arena2`.
- World: main minecraft:overworld.
- Original structure block: (35,-59,-12). Relative offset: (-145,0,-118). Structure origin: (-110,-59,-130), size (144,25,96), extent X -110..33, Y -59..-35, Z -130..-35.\n- Include entities: OFF. User supplied SAVE mode screenshot; do not assume that SAVE was pressed after changes.
- Nether team spawn: (27,-51,-57). Overworld team spawn: (-92,-51,-57). Waiting room: (-31,-59,-14).\n- IMPORTANT: Waiting room at Z=-14 is **outside structure template** Z range -130..-35. It will NOT be cloned by minecraft:arena2; must separately replicate/build the waiting room or obtain an alternate waiting area inside the structure before enabling clones.\n- Spawns in translated arenas require proper y-level safety verification; no arbitrary auto-correction of Y.
- Proposed clones at +300 and +600 X, but must verify those locations are empty and that the waiting room is recreated for each before generating.

## Safety
- Never overwrite the original Arena 2 outside exact saved-template bounds.
- Verify saved template dimensions and load target chunks before placement.
- One-time copies require explicit operator command and a world backup.
- No JAR should be described as playable until code compiles and test limitations are disclosed.

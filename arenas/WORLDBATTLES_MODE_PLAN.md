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
- Original map structure origin and size: TO BE READ FROM USER'S STRUCTURE SAVE BLOCK.
- Original two team spawn coordinates + safe waiting room: TO BE READ FROM USER.
- Instances 2/3 will use independent X offsets only after checking collision-free locations.

## Safety
- Never overwrite the original Arena 2 outside exact saved-template bounds.
- Verify saved template dimensions and load target chunks before placement.
- One-time copies require explicit operator command and a world backup.
- No JAR should be described as playable until code compiles and test limitations are disclosed.

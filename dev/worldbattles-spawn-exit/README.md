# WorldBattles Spawn Exit 0.1.0 ALPHA
Server-only Forge 1.20.1 bridge for worldbattles-arenas-0.4.1-safe-helper.jar.
When /spawn is invoked, uses its current session's own leave method before FTB Essentials teleports the player. SandWars/WorldBattles leave handles less than two players; Zombies leave supports solo. Per-instance cleanup remains entirely in existing Arenas mod.
Test on a backup, preserve Arenas 0.4.1. Incompatibility after changing Arenas version will log errors and leave session unchanged.

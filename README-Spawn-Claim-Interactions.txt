WorldBattles Spawn Claim Interactions FIX — version 1.0.1
Target: Minecraft 1.20.1 / Forge 47.4.10 / Java 17.

Allowed regions:
- minecraft:overworld: center X=23 Z=18, +/-1000 blocks on each axis.
- secondworld:overworld: center X=-541 Z=1209, +/-500 blocks on each axis.
- minage:overworld: center X=-541 Z=1209, +/-500 blocks on each axis.

Allows interactions with inventory containers (chests and compatible modded containers), buttons and levers only in those zones. It does not modify block-break or block-place events. Handler is restricted by dimension, coordinates and block type. Test with a non-OP player on the live server before deployment. The previous watchdog report was a server tick timeout and did not establish this mod as the cause.
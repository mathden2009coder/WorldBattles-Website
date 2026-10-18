package com.worldbattles.mapreset;

import net.minecraftforge.fml.common.Mod;

/**
 * Forge entrypoint for the exact, unchanged SandWars MapReset v3 functions.
 * No tick handlers, no additional resets, and no automatic structure placement.
 * Existing function invocations continue to work under the sandwars namespace.
 */
@Mod(WorldBattlesMapReset.MOD_ID)
public final class WorldBattlesMapReset {
    public static final String MOD_ID = "worldbattlesmapreset";
    public WorldBattlesMapReset() {}
}

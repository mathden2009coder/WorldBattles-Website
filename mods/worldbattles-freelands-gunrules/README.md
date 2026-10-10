# WorldBattles Freelands Gun Rules v0.1.0 — Forge 1.20.1

**Server-only mod**, no player modpack update needed.

Blocks **TaCZ gun crafting** in these four dimensions only:
- `secondworld:overworld`
- `secondworld:the_nether`
- `minage:overworld`
- `minage:the_nether`

TaCZ guns remain usable, and can still be obtained from other sources. Ammo and attachments can still be crafted in Freelands. The vanilla Overworld, Nether, End, and WorldsBattle/Zombies modes are unaffected. The mod does not change TaCZ recipes, player inventories, existing guns, the economy, jobs, or any datapack.

## How it works

Injects at the start of TaCZ 1.20.1's **server-side** `GunSmithTableMenu#doCraft`, before materials are consumed or the gun is spawned. Only cancels if the player's current dimension is in the four-item whitelist AND the recipe's output item is `tacz:modern_kinetic_gun`. TaCZ ammo (`tacz:ammo`) and attachments (`tacz:attachment`) are not canceled. This covers gun recipes from TaCZ gun packs using the standard TaCZ gun item, on any TaCZ workbench.

## Install

1. Back up the server world.
2. Stop the Forge 1.20.1 server on AxentHost.
3. Place `worldbattles-freelands-gunrules-0.1.0.jar` in the **server's** `mods` directory (not required for players).
4. Restart. Test: gun recipe blocked in Freelands, ammo/attachments allowed, gun recipe allowed outside Freelands.

**Important:** compile success and static checks are not an in-game integration test. Test on a staging copy of the server first; this mod uses a Mixin targeting TaCZ's 1.20.1 `GunSmithTableMenu#doCraft` method. Changes to TaCZ's implementation may require updating the Mixin.

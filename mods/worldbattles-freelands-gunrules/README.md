# WorldBattles Freelands Gun Rules v0.2.0 — Forge 1.20.1

**v0.2.0 fixes the original v0.1.0's lack of a guaranteed server-side fallback.** It adds a Forge `RightClickBlock` event guard, independent of Mixin injection, for all four standard TaCZ gunsmith workbenches.

Only these dimensions are protected:
- `secondworld:overworld`
- `secondworld:the_nether`
- `minage:overworld`
- `minage:the_nether`

Vanilla Overworld, vanilla Nether, End, WorldBattles combat arenas and Zombies remain unaffected. The mod does not remove weapons, modify the economy, change datapacks or change inventories.

## Default mode (reliable / strict)

The default config `blockAllTaczWorkbenchesInFreelands=true` prevents opening TaCZ gunsmith tables in Freelands. Thus **guns cannot be crafted there** even if the TaCZ-specific Mixin fails to load. This mode also blocks crafting **TaCZ ammo and attachments** on those benches in Freelands. Existing guns and ammo remain usable. Other crafting tables are unaffected.

## Optional selective mode (requires working Mixin)

In `config/worldbattlesgunrules-common.toml`, set `blockAllTaczWorkbenchesInFreelands=false` and restart. The mod checks if the selective Mixin was really applied to TaCZ's menu; if it was, benches can open and only TaCZ gun output is blocked (ammo and attachments can still be crafted). If the Mixin is missing, the Forge event fallback continues to block the benches.

## Install on AxentHost

1. Make a server backup and stop the Forge 1.20.1 server.
2. **Delete** `worldbattles-freelands-gunrules-0.1.0.jar` from the server `mods/` folder.
3. Put **only** `worldbattles-freelands-gunrules-0.2.0.jar` in server `mods/`. No player modpack change.
4. Restart and run `/wbgunrules` as an OP while in Freelands to check the detected dimension and protection mode.
5. Test: right-click a TaCZ gunsmith table in Freelands: it must be blocked with a message. Outside Freelands it must open normally.

**Do not leave both mod versions installed.** Compile checks are not equivalent to a live integration test; validate with a staging copy before using on a production server.

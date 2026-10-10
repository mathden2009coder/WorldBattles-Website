from pathlib import Path

root = Path(__file__).resolve().parents[1]
tele = (root / 'src/main/java/com/worldbattles/commands/TeleportCommands.java').read_text()
pause = (root / 'src/main/java/com/worldbattles/commands/JobsPauseCommands.java').read_text()
meta = (root / 'src/main/resources/META-INF/mods.toml').read_text()
for name, dim, xyz in [
    ('spawn', 'minecraft:overworld', '23, -58, 18'),
    ('freelands', 'secondworld:overworld', '-541, 120, 1209'),
    ('minage', 'minage:overworld', '-541, 120, 1209')]:
    assert f'Commands.literal("{name}")' in tele
    assert f'"{dim}", {xyz}' in tele
assert 'Collections.emptySet(), 0.0F, 0.0F' in tele
assert 'player.teleportTo(destination' in tele
assert 'player.setGameMode' not in tele and 'player.getInventory' not in tele
assert '.requires(source -> source.hasPermission(2))' in pause
for sub in ('pause', 'resume', 'status'):
    assert f'Commands.literal("{sub}")' in pause
assert 'public static final String OBJECTIVE = "fj_pause"' in pause
assert 'setScore(paused ? 1 : 0)' in pause
assert 'boolean creative = player.isCreative()' in pause
assert 'displayTest="IGNORE_ALL_VERSION"' in meta
print('OK: teleport targets, permissions, scoreboard persistence, no inventory/game mode modifications')

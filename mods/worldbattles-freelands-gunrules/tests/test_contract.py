"""Static safety checks for TaCZ server-only fail-safe integration."""
from pathlib import Path
import json

root = Path(__file__).resolve().parents[1]
java = (root / 'src/main/java/com/worldbattles/gunrules/FreelandsGunRules.java').read_text()
guard = (root / 'src/main/java/com/worldbattles/gunrules/GunSmithBenchGuard.java').read_text()
cfg = (root / 'src/main/java/com/worldbattles/gunrules/GunRulesConfig.java').read_text()
mixin = (root / 'src/main/java/com/worldbattles/gunrules/mixin/GunSmithCraftMixin.java').read_text()
meta = (root / 'src/main/resources/META-INF/mods.toml').read_text()
build = (root / 'build.gradle').read_text()
config = json.loads((root / 'src/main/resources/worldbattlesgunrules.mixins.json').read_text())
assert java.count('new ResourceLocation(') == 4
for dim in ('"secondworld", "overworld"', '"secondworld", "the_nether"',
            '"minage", "overworld"', '"minage", "the_nether"'):
    assert dim in java, dim
for forbidden in ('"minecraft", "overworld"', '"minecraft", "the_nether"', '"minecraft", "the_end"'):
    assert forbidden not in java
assert 'MinecraftForge.EVENT_BUS.register(new GunSmithBenchGuard())' in java
assert 'Commands.literal("wbgunrules")' in java
for block in ('gun_smith_table', 'workbench_a', 'workbench_b', 'workbench_c'):
    assert f'new ResourceLocation("tacz", "{block}")' in guard
assert '@SubscribeEvent(priority = EventPriority.HIGHEST)' in guard
assert 'PlayerInteractEvent.RightClickBlock' in guard
assert 'event.setCanceled(true)' in guard
assert 'event.setCancellationResult(InteractionResult.FAIL)' in guard
assert 'isSelectiveMixinInstalled()' in guard
assert 'define("blockAllTaczWorkbenchesInFreelands", true)' in cfg
assert 'com.tacz.guns.inventory.GunSmithTableMenu' in mixin
assert '@Inject(method = "doCraft", at = @At("HEAD"), cancellable = true' in mixin
assert 'new ResourceLocation("tacz", "modern_kinetic_gun")' in mixin
assert 'ci.cancel()' in mixin
assert 'extractItem' not in mixin and 'setScore' not in mixin
assert 'displayTest="IGNORE_ALL_VERSION"' in meta
assert 'modId="tacz"' in meta
assert "version = '0.2.0'" in build
assert "version=\"0.2.0\"" in meta
assert "'MixinConfigs': 'worldbattlesgunrules.mixins.json'" in build
assert config['mixins'] == ['GunSmithCraftMixin']
print('OK: strict Forge fallback, selective Mixin, only four Freelands dimensions')

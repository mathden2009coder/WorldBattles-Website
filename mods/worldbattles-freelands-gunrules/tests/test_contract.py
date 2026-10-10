"""Static safety checks for the TaCZ 1.20.1 server-only integration."""
from pathlib import Path
import json

root = Path(__file__).resolve().parents[1]
java = (root / 'src/main/java/com/worldbattles/gunrules/FreelandsGunRules.java').read_text()
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
assert 'com.tacz.guns.inventory.GunSmithTableMenu' in mixin
assert '@Inject(method = "doCraft", at = @At("HEAD"), cancellable = true' in mixin
assert 'new ResourceLocation("tacz", "modern_kinetic_gun")' in mixin
assert 'ci.cancel()' in mixin
assert 'extractItem' not in mixin and 'setScore' not in mixin
assert 'displayTest="IGNORE_ALL_VERSION"' in meta
assert 'modId="tacz"' in meta
assert "'MixinConfigs': 'worldbattlesgunrules.mixins.json'" in build
assert config['mixins'] == ['GunSmithCraftMixin']
print('OK: only 4 Freelands dimensions; server-authoritative gun-only block; ammo untouched')

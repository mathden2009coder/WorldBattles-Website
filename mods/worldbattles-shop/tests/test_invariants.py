"""Source-level checks; not a substitute for compiling or testing with two Minecraft clients."""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
source = root / 'src/main/java/com/worldbattles/shop'
shop = (source / 'WorldBattlesShop.java').read_text()
menu = (source / 'ShopMenu.java').read_text()
screen = (source / 'client/ShopScreen.java').read_text()
data = (source / 'ShopData.java').read_text()

for dim in ('secondworld", "overworld', 'secondworld", "the_nether',
            'minage", "overworld', 'minage", "the_nether'):
    assert dim in shop, f'Missing allowed dimension {dim}'
assert 'minecraft", "overworld' not in shop
assert 'super.clicked(' not in menu, 'A shop GUI must never move fake inventory slots'
assert 'owner.containerMenu == this' in menu
assert 'ShopRegistry.SHOP_MENU.get()' in menu
assert 'MenuScreens.register' in (source / 'client/ClientEvents.java').read_text()
assert 'handleInventoryMouseClick' in screen
assert 'balances.put(listing.seller()' in data
assert 'listings.remove(listingId)' in data
assert 'setDirty();' in data
print('OK: source invariants')

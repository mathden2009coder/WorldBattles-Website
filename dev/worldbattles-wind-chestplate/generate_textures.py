"""Generate the custom dark-steel chestplate textures and cyan armor highlights."""
import base64, os, struct, zlib

root = "src/main/resources/assets/worldbattleswindplate/textures"

def png(path, w, h, pixels):
    def chunk(t, data):
        return struct.pack("!I", len(data)) + t + data + struct.pack("!I", zlib.crc32(t + data) & 0xffffffff)
    raw = b"".join(b"\x00" + b"".join(bytes(p) for p in row) for row in pixels)
    data = (b"\x89PNG\r\n\x1a\n"
            + chunk(b"IHDR", struct.pack("!2I5B", w, h, 8, 6, 0, 0, 0))
            + chunk(b"IDAT", zlib.compress(raw, 9))
            + chunk(b"IEND", b""))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(data)

# The item sprite is derived from the supplied dark chestplate reference, pixelized
# to Minecraft's 32x32 icon and recolored with restrained cyan/diamond-blue accents.
b64_path = root + "/item/wind_chestplate.png.base64"
with open(b64_path, "r", encoding="ascii") as f:
    icon_data = base64.b64decode(f.read().strip())
with open(root + "/item/wind_chestplate.png", "wb") as f:
    f.write(icon_data)

clear = (0, 0, 0, 0)
edge = (24, 22, 26, 255)
dark = (39, 39, 45, 255)
mid = (53, 55, 64, 255)
steel = (72, 79, 94, 255)
light = (94, 108, 128, 255)
blue = (33, 111, 148, 255)
cyan = (36, 177, 212, 255)
shine = (104, 222, 240, 255)
armor = [[clear for x in range(64)] for y in range(32)]

def paint_region(x0, x1):
    width = x1 - x0
    for y in range(16, 32):
        for x in range(x0, x1):
            xx = x - x0
            if y in (16, 31) or xx in (0, width - 1):
                c = edge
            elif xx in (1, width - 2):
                c = dark
            elif xx < width * 0.25:
                c = dark if y % 4 else mid
            elif xx > width * 0.75:
                c = mid if y % 3 else dark
            else:
                c = steel if (xx + y) % 5 else mid
            if y in (20, 27) and 2 <= xx <= width - 3:
                c = dark
            armor[y][x] = c

# Standard 64x32 armor UV layout: left arm, torso, right arm (layer 1).
paint_region(0, 16)
paint_region(16, 40)
paint_region(40, 56)

# Dark plated torso with cool steel facets and small diamond-blue highlights.
for y in range(18, 30):
    for x in range(19, 37):
        if x in (19, 36) or y in (18, 29):
            armor[y][x] = dark
        elif x in (20, 35):
            armor[y][x] = steel
        elif (x + y) % 7 == 0:
            armor[y][x] = light
for x in (20, 21, 34, 35):
    armor[18][x] = blue
    armor[19][x] = cyan

# Tiny diamond rune at the center, with a short blue seam below it.
for x, y, c in [
    (27, 21, blue), (26, 22, cyan), (27, 22, shine),
    (28, 22, cyan), (27, 23, blue)
]:
    armor[y][x] = c
armor[24][27] = cyan
armor[25][27] = blue
armor[26][27] = blue

# A few subtle cyan lines on the shoulders/sleeves; most of the armor stays charcoal.
for x0 in (0, 40):
    for x in range(x0 + 4, x0 + 12):
        armor[18][x] = blue
        if x in (x0 + 5, x0 + 10):
            armor[19][x] = cyan
for x in (2, 13, 42, 53):
    armor[24][x] = blue
    armor[25][x] = steel

png(root + "/models/armor/wind_layer_1.png", 64, 32, armor)
print("Generated reference-style dark steel/cyan item and armor textures")

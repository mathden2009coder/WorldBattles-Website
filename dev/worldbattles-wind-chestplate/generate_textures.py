"""Generate pixel-perfect PNGs without extra dependencies."""
import zlib,struct,os
root="src/main/resources/assets/worldbattleswindplate/textures"
def png(path,w,h,pixels):
 def chunk(t,d):return struct.pack("!I",len(d))+t+d+struct.pack("!I",zlib.crc32(t+d)&0xffffffff)
 raw=b"".join(b"\x00"+b"".join(bytes(p) for p in row) for row in pixels)
 data=b"\x89PNG\r\n\x1a\n"+chunk(b"IHDR",struct.pack("!2I5B",w,h,8,6,0,0,0))+chunk(b"IDAT",zlib.compress(raw,9))+chunk(b"IEND",b"")
 os.makedirs(os.path.dirname(path),exist_ok=True)
 open(path,"wb").write(data)
clear=(0,0,0,0); dark=(17,34,48,255); edge=(12,21,33,255);steel=(38,74,87,255); cyan=(38,216,239,255); light=(156,252,255,255); blue=(9,130,176,255)
p=[[clear for x in range(32)] for y in range(32)]
# Classic chestplate silhouette, intentionally bolder than 16px.
for y in range(6,28):
 for x in range(5,27):
  torso=(9<=x<=22 and 11<=y<=27)
  arm=((5<=x<=11 or 20<=x<=26) and 7<=y<=19)
  if torso or arm:
   p[y][x]=edge if x in (5,26) or y in (7,27) else (steel if (x+y)%5 else dark)
for y in range(12,24):
 for x in range(11,21):
  if (x+y)%6==0:p[y][x]=blue
for x in range(12,20):
 p[13][x]=cyan;p[14][x]=light
for y in range(15,21):
 p[y][12]=cyan;p[y][19]=cyan
p[16][15]=light;p[16][16]=light;p[17][15]=cyan;p[17][16]=cyan
for x in (7,24):
 for y in (10,11,12):p[y][x]=cyan
png(root+"/item/wind_chestplate.png",32,32,p)
# vanilla armor layer 1 UV: torso, arms and collar; all other texels transparent.
a=[[clear for x in range(64)] for y in range(32)]
for y in range(16,32):
 for x in range(16,64):
  if 16<=x<40 or 40<=x<64:
   a[y][x]=(edge if x%8==0 or y in (16,31) else steel)
   if (x+y)%7==0:a[y][x]=blue
for x in range(21,34):
 a[20][x]=cyan;a[21][x]=light
for y in range(23,29):
 for x in (22,33):a[y][x]=cyan
for x in (43,57):
 for y in range(19,25):a[y][x]=cyan
png(root+"/models/armor/wind_layer_1.png",64,32,a)
print("Generated icon and worn armor PNG")

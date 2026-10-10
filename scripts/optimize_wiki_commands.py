from pathlib import Path
import re
p=Path("index.html")
s=p.read_text(encoding="utf-8")
def card(cmd,desc):
 return '<div class="wiki-command-card-v84"><div class="wiki-command-top-v84"><code>'+cmd+'</code><button class="wiki-copy-v84" data-copy-command="'+cmd+'" type="button">COPIER</button></div><p>'+desc+'</p></div>'
for cmd,desc in [("/spawn","Retour au lobby"),("/freelands","Accès à Freelands"),("/minage","Accès à Minage")]:
 if "<code>"+cmd+"</code>" not in s:
  anchor=s.index("<code>/jobs</code>")
  start=s.rfind('<div class="wiki-command-card-v84">',0,anchor)
  if start<0:raise ValueError("Wiki anchor missing")
  s=s[:start]+card(cmd,desc)+s[start:]
match=re.search(r"<code>/trigger\\s+m[eé]tiers</code>",s,re.I)
if match:
 start=s.rfind('<div class="wiki-command-card-v84">',0,match.start())
 if start<0:raise ValueError("Legacy command card missing")
 depth=0
 for tag in re.finditer(r"</?div\\b[^>]*>",s[start:]):
  depth+= -1 if tag.group().startswith("</") else 1
  if depth==0:
   s=s[:start]+s[start+tag.end():]
   break
 else:raise ValueError("Unclosed command card")
# Preserve each image byte-for-byte, but store it outside the HTML.
import base64,hashlib
folder=Path("assets/embedded")
folder.mkdir(parents=True,exist_ok=True)
def move_image(m):
 data=base64.b64decode(m.group(2),validate=True)
 ext={"jpeg":"jpg","svg+xml":"svg"}.get(m.group(1).lower(),m.group(1).lower())
 path=folder/(hashlib.sha256(data).hexdigest()[:24]+"."+ext)
 if not path.exists():path.write_bytes(data)
 elif path.read_bytes()!=data:raise ValueError("Image hash collision")
 return path.as_posix()
s=re.sub(r"data:image/(png|jpeg|jpg|webp|gif|svg\\+xml);base64,([A-Za-z0-9+/=]+)",move_image,s,flags=re.I)
p.write_text(s,encoding="utf-8")
print("Wiki updated",len(s))

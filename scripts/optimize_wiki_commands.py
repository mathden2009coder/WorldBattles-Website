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
p.write_text(s,encoding="utf-8")
print("Wiki updated",len(s))

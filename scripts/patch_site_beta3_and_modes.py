#!/usr/bin/env python3
"""Targeted, idempotent website refresh. Never replace the large index with a stale copy."""
from pathlib import Path
import re
import sys

def patch(source: Path):
    html=source.read_text("utf-8")
    before=html
    rules={
      "sandwars": ("Chaque joueur a une seule vie par manche. Quand toute une équipe est éliminée, l’autre gagne un point. La première équipe à remporter 3 manches gagne la partie.",
                   "Each player has one life per round. When an entire team is eliminated, the other team earns a point. First team to win 3 rounds wins the match."),
      "worldsbattle": ("Deux équipes s’affrontent : Nether contre Overworld. Chaque élimination adverse rapporte un point à ton équipe. La première à 25 kills gagne la manche, et 2 manches remportées donnent la victoire.",
                     "Two teams compete: Nether versus Overworld. Each enemy elimination earns a point for your team. First to 25 kills wins the round; win 2 rounds to win the match."),
    }
    stylesheet='''
<style id="worldbattles-game-rules-v2026">
.spa-page[data-route="world"] .wb-mode-card-content-v150 .wb-mode-rules-short-v2026{
  margin:4px 0 17px;padding:11px 13px;border-left:2px solid rgba(255,169,66,.65);
  background:rgba(255,255,255,.035);border-radius:0 8px 8px 0;
  font-size:13px;line-height:1.6;color:#dce5ee;max-width:520px;
}
.spa-page[data-route="world"] .wb-mode-card-v150[data-gallery-key-v150="worldsbattle"] .wb-mode-rules-short-v2026{
  border-left-color:rgba(110,231,183,.65);
}
</style>
'''
    if 'id="worldbattles-game-rules-v2026"' not in html:
        assert '</head>' in html
        html=html.replace('</head>',stylesheet+'</head>',1)
    for gallery,(fr,en) in rules.items():
        card=re.compile(r'(<article class="wb-mode-card-v150" data-gallery-key-v150="'+gallery+r'"[^>]*>.*?<div class="wb-mode-card-content-v150">.*?</p>)',re.S)
        match=card.search(html)
        assert match, f"Missing {gallery} card"
        if 'wb-mode-rules-short-v2026' not in match.group(1):
            desc='<p class="wb-mode-rules-short-v2026"><span class="wb-fr-v150">'+fr+'</span><span class="wb-en-v150">'+en+'</span></p>'
            html=html[:match.end()]+desc+html[match.end():]

    # Correct all fixed zombie kill labels. The numerical requirement scales with players.
    html=re.sub(r'(?<!\+)100 KILLS', '100+ KILLS', html)
    html=re.sub(r'(?<!\+)100 kills', '100+ kills', html)
    html=html.replace('The 100-kill mini-mode.', 'The 100+ kill mini-mode.')
    fr_phrases=[
      'Tuez 100 zombies','éliminer 100 zombies','élimine 100 zombies',
      'Objectif : 100 zombies',
    ]
    for phrase in fr_phrases:
        html=html.replace(phrase,phrase+' et +') if phrase+' et +' not in html else html
    en_phrases={'Kill 100 zombies':'Kill 100+ zombies',
                'kill 100 zombies':'kill 100+ zombies',
                'Goal: 100 zombies':'Goal: 100+ zombies',
                'eliminate 100 zombies':'eliminate 100+ zombies'}
    for a,b in en_phrases.items(): html=html.replace(a,b)
    # Explain the scaling, with a bilingual rendering in the original Zombies page.
    note=('<p class="zombies-scaling-note-v2026">'
       '<span class="wb-fr-v150">En solo : 100 éliminations. Ajoute 50 zombies par joueur supplémentaire (150 à 2, 200 à 3, etc.).</span>'
       '<span class="wb-en-v150">Solo: 100 eliminations. Add 50 zombies per extra player (150 with 2, 200 with 3, etc.).</span>'
       '</p>')
    if 'zombies-scaling-note-v2026' not in html:
        marker='<div class="dashboard-main"><h2>Objectif : 100 zombies et +</h2>'
        assert marker in html, "Zombies objective marker missing"
        html=html.replace(marker,marker+note,1)

    old='https://github.com/mathden2009coder/WorldBattles-Website/releases/download/beta-2/WorldBattles_Modpack_Player_BETA2_Forge_1.20.1.zip'
    new='https://github.com/mathden2009coder/WorldBattles-Website/releases/download/beta-3/WorldBattles_Modpack_Player_BETA3_Forge_1.20.1.zip'
    if old in html:html=html.replace(old,new)
    html=html.replace('WorldBattles_Modpack_Player_BETA2_Forge_1.20.1.zip','WorldBattles_Modpack_Player_BETA3_Forge_1.20.1.zip')
    html=html.replace('Player BETA 2','Player BETA 3')
    html=html.replace('PLAYER PACK BETA 1','PLAYER PACK BETA 3')
    html=html.replace('PACK JOUEUR BETA 1','PACK JOUEUR BETA 3')
    html=html.replace('72 mods • 6 textures • 1 shader','73 mods • 6 textures • 1 shader')

    assert new in html and old not in html
    assert html.count('wb-mode-rules-short-v2026')>=3
    assert '100 KILLS' not in html
    assert 'Objectif : 100 zombies et +' in html
    assert 'Player BETA 3' in html
    assert '73 mods • 6 textures • 1 shader' in html
    assert len(html) >= int(len(before)*0.99), 'Unexpected loss of site data'
    if html!=before:source.write_text(html,'utf-8')
    print(f'UPDATED site: {len(before)} -> {len(html)} chars, beta3 links={html.count(new)}')
if __name__=='__main__':patch(Path(sys.argv[1]))

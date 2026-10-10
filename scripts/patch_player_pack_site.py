#!/usr/bin/env python3
"""Patch the site's existing pack download links and Wiki, preserving everything else."""
import json
import sys
from pathlib import Path

BASE = 'https://github.com/mathden2009coder/WorldBattles-Website/releases'
OLD_ASSET = 'WorldBattles_Modpack_Player_BETA1_Forge_1.20.1.zip'
NEW_ASSET = 'WorldBattles_Modpack_Player_BETA2_Forge_1.20.1.zip'
OLD_LINK = f'{BASE}/download/beta-1/{OLD_ASSET}'
NEW_LINK = f'{BASE}/download/beta-2/{NEW_ASSET}'

def required(s, old, new, label):
    if old not in s:
        if new in s: return s
        raise ValueError(f'Missing site anchor for {label}: {old[:100]!r}')
    print(f'{label}: {s.count(old)} replacements')
    return s.replace(old, new)

def card(command, description):
    return ('<div class="wiki-command-card-v84"><div class="wiki-command-top-v84">'
            f'<code>{command}</code><button class="wiki-copy-v84" '
            f'data-copy-command="{command}" type="button">COPIER</button></div>'
            f'<p>{description}</p></div>')

def patch(site, metadata):
    data = json.loads(metadata.read_text(encoding='utf-8'))
    count = int(data['jar_count'])
    size = f"{data['size_mib']:.1f} Mo"
    s = site.read_text(encoding='utf-8')
    s = required(s, OLD_LINK, NEW_LINK, 'pack links')
    s = required(s, f'{BASE}/latest/download/{OLD_ASSET}', NEW_LINK, 'download overlay')
    s = required(s, 'Player BETA 1', 'Player BETA 2', 'version')
    s = required(s, '199.4 Mo', size, 'pack size')
    s = required(s, '70 mods • 6 textures • 1 shader',
                 f'{count} mods • 6 textures • 1 shader', 'pack contents')
    jobs_desc = 'Ouvre le tableau des 7 métiers avec tes niveaux, ton XP et les récompenses.'
    shop_desc = 'Ouvre la boutique pour acheter, vendre et consulter ton argent.'
    if '<code>/jobs</code>' not in s and '<code>/wbshop</code>' not in s:
        anchor = '<code>/trigger metiers</code>'
        assert anchor in s, 'Wiki command panel missing'
        start = s.rfind('<div class="wiki-command-card-v84">', 0, s.index(anchor))
        assert start >= 0, 'Wiki insertion point missing'
        s = s[:start] + card('/jobs', jobs_desc) + card('/wbshop', shop_desc) + s[start:]
    else:
        assert '<code>/jobs</code>' in s and '<code>/wbshop</code>' in s
    hint = 'Après connexion, utilise /jobs pour les métiers et /wbshop pour la boutique.'
    if hint not in s:
        anchor = '<div class="rule"><i>4</i>'
        assert anchor in s, 'Player installation panel missing'
        end = s.index('</div>', s.index(anchor)) + len('</div>')
        s = s[:end] + '<p class="pack-download-meta-v91">' + hint + '</p>' + s[end:]
    translations = {
        jobs_desc: 'Opens the 7-job dashboard with your levels, XP and rewards.',
        shop_desc: 'Opens the shop to buy, sell and check your balance.',
        hint: 'After joining, use /jobs for your jobs and /wbshop for the shop.',
    }
    anchor = '"Ouvre directement ton Ender Chest.":'
    if anchor in s:
        pairs = ''.join(json.dumps(fr, ensure_ascii=False) + ':' +
                        json.dumps(en, ensure_ascii=False) + ','
                        for fr, en in translations.items()
                        if json.dumps(fr, ensure_ascii=False) + ':' not in s)
        if pairs: s = s.replace(anchor, pairs + anchor, 1)
    assert NEW_LINK in s and OLD_LINK not in s
    assert f'{BASE}/latest/download/{OLD_ASSET}' not in s
    assert '<code>/jobs</code>' in s and '<code>/wbshop</code>' in s
    site.write_text(s, encoding='utf-8')
    print(f'Patched {site}: {count} mods, {size}')

if __name__ == '__main__':
    assert len(sys.argv) == 3
    patch(Path(sys.argv[1]), Path(sys.argv[2]))

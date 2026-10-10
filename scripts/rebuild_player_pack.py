#!/usr/bin/env python3
"""Keep every BETA 1 ZIP member and add two verified Forge 1.20.1 mods."""
import json
import shutil
import sys
from collections import Counter
from pathlib import Path, PurePosixPath
from zipfile import ZIP_DEFLATED, ZipFile

def run(base_zip, shop_jar, jobs_jar, output_zip, metadata):
    expected = {shop_jar: ('worldbattlesshop', '0.2.2'), jobs_jar: ('worldbattlesjobs', '0.1.0')}
    for jar, (modid, version) in expected.items():
        assert jar.is_file(), f'Missing compiled mod: {jar}'
        with ZipFile(jar) as archive:
            mods_toml = archive.read('META-INF/mods.toml').decode('utf-8')
        assert f'modId="{modid}"' in mods_toml, f'Unexpected mod id: {jar}'
        assert f'version="{version}"' in mods_toml, f'Unexpected mod version: {jar}'
    with ZipFile(base_zip, 'r') as src:
        entries = src.infolist()
        parents = Counter(
            str(PurePosixPath(i.filename).parent) for i in entries
            if i.filename.lower().endswith('.jar') and 'mods' in PurePosixPath(i.filename).parts
        )
        assert parents, 'The base pack has no mods directory; refusing to publish'
        mods_dir, base_count = parents.most_common(1)[0]
        assert base_count >= 20, f'Unexpected base pack: only {base_count} jars'
        print(f'Existing pack: {len(entries)} entries, {base_count} mods in {mods_dir}/')
        replacements = {'worldbattles-shop-0.2.2.jar': shop_jar,
                        'worldbattles-jobs-0.1.0.jar': jobs_jar}
        skipped = []
        output_zip.parent.mkdir(parents=True, exist_ok=True)
        with ZipFile(output_zip, 'w', allowZip64=True) as dst:
            for info in entries:
                path = PurePosixPath(info.filename)
                if str(path.parent) == mods_dir and path.suffix == '.jar' and (
                    path.name.startswith('worldbattles-shop-') or path.name.startswith('worldbattles-jobs-')
                ):
                    skipped.append(info.filename)
                    continue
                if info.is_dir():
                    dst.writestr(info, b'')
                else:
                    with src.open(info) as source, dst.open(info, 'w') as target:
                        shutil.copyfileobj(source, target, length=1024 * 1024)
            for name, jar in replacements.items():
                dst.write(jar, arcname=f'{mods_dir}/{name}',
                          compress_type=ZIP_DEFLATED, compresslevel=6)
            dst.writestr('WORLDBATTLES_BETA2_MODS.txt',
                'WorldBattles Player BETA 2 - Forge 1.20.1\n'
                'Nouveaux mods : WorldBattles Shop v0.2.2 et WorldBattles Metiers v0.1.0.\n'
                'Commandes : /wbshop (boutique), /jobs (metiers).\n'
                'Ces deux mods doivent aussi etre presents sur le serveur.\n'
                'Les autres mods, textures, shaders et configurations sont conserves.\n')
    with ZipFile(output_zip) as check:
        assert check.testzip() is None, 'Corrupt ZIP member'
        for name in replacements:
            assert f'{mods_dir}/{name}' in check.namelist(), f'Missing {name}'
        jar_count = sum(1 for name in check.namelist()
                        if str(PurePosixPath(name).parent) == mods_dir and name.lower().endswith('.jar'))
        assert jar_count == base_count - len(skipped) + 2
        assert len(check.namelist()) == len(entries) - len(skipped) + 3
    info = {'jar_count': jar_count, 'size_bytes': output_zip.stat().st_size,
            'size_mib': round(output_zip.stat().st_size / (1024 * 1024), 1),
            'mods_dir': mods_dir, 'replaced': skipped}
    metadata.write_text(json.dumps(info, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(info, ensure_ascii=False, indent=2))

if __name__ == '__main__':
    assert len(sys.argv) == 6, 'Usage: rebuild_player_pack.py BASE SHOP JOBS OUTPUT META'
    run(*(Path(arg) for arg in sys.argv[1:]))

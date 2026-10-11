#!/usr/bin/env python3
"""Prepare WorldBattles Player BETA 3 from the published BETA 2 without dropping files."""
from pathlib import Path, PurePosixPath
from zipfile import ZipFile, ZIP_DEFLATED
from collections import Counter
import shutil, sys, json, hashlib

def main(base_path, arena_path, output_path, meta_path):
    assert base_path.is_file() and arena_path.is_file()
    assert arena_path.name == "worldbattles-arenas-0.3.2-sandwars-waiting-spawn.jar"
    with ZipFile(arena_path) as mod:
        toml = mod.read("META-INF/mods.toml").decode("utf-8")
        assert 'modId="worldbattlesarenas"' in toml
        assert mod.testzip() is None
    with ZipFile(base_path) as src:
        files = src.infolist()
        assert src.testzip() is None, "BETA2 archive corrupt"
        counts = Counter(str(PurePosixPath(info.filename).parent)
                         for info in files
                         if info.filename.lower().endswith(".jar") and
                         "mods" in PurePosixPath(info.filename).parts)
        assert counts, "Missing mods directory"
        mods_dir, count = counts.most_common(1)[0]
        assert count >= 20, f"Unexpectedly small modpack: {count} mods"
        replaced = [info.filename for info in files if
                    str(PurePosixPath(info.filename).parent) == mods_dir and
                    info.filename.lower().endswith(".jar") and
                    (PurePosixPath(info.filename).name.lower().startswith("worldbattles-arenas-") or
                     PurePosixPath(info.filename).name.lower().startswith("worldbattlesarenas-"))]
        new_member = f"{mods_dir}/{arena_path.name}"
        assert new_member not in [i.filename for i in files] or new_member in replaced
        output_path.parent.mkdir(parents=True, exist_ok=True)
        with ZipFile(output_path, "w", allowZip64=True) as target:
            for info in files:
                if info.filename in replaced: continue
                if info.is_dir():
                    target.writestr(info, b"")
                else:
                    with src.open(info) as f, target.open(info, "w") as dst:
                        shutil.copyfileobj(f, dst, 1024*1024)
            target.write(arena_path, new_member, compress_type=ZIP_DEFLATED, compresslevel=6)
            target.writestr("WORLDBATTLES_BETA3_MODS.txt",
                "WorldBattles Player BETA 3 - Minecraft Forge 1.20.1\n"
                "WorldBattles Arenas v0.3.2 : Zombies + World Battles + Sand Wars.\n"
                "BETA 2 conservee : Shop v0.2.2, Metiers v0.1.0, autres mods et contenus.\n"
                "Ce mod Arenas v0.3.2 doit egalement etre installe sur le serveur.\n"
                "Ne gardez pas une ancienne version d'Arenas dans le dossier mods.\n")
    with ZipFile(output_path) as check:
        assert check.testzip() is None, "Output ZIP corrupt"
        names = check.namelist()
        assert len(names) == len(files) - len(replaced) + 2
        assert names.count(new_member) == 1
        jars = [n for n in names if str(PurePosixPath(n).parent) == mods_dir and n.lower().endswith(".jar")]
        assert len(jars) == count - len(replaced) + 1
        assert len([n for n in jars if "worldbattles-arenas-" in n.lower()]) == 1
        for info in files:
            if info.filename not in replaced:
                assert info.filename in names, f"Lost BETA2 entry {info.filename}"
        summary = dict(base_entries=len(files),output_entries=len(names),
                       previous_arenas=replaced, new_arenas=new_member,
                       jar_count=len(jars), mods_dir=mods_dir,
                       bytes=output_path.stat().st_size,
                       sha256=hashlib.sha256(output_path.read_bytes()).hexdigest())
    meta_path.write_text(json.dumps(summary, indent=2, ensure_ascii=False))
    print(json.dumps(summary, indent=2, ensure_ascii=False))
if __name__ == "__main__":
    assert len(sys.argv)==5
    main(*(Path(x) for x in sys.argv[1:]))

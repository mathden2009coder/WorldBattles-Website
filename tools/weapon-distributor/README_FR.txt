WORLD BATTLES — DISTRIBUTEUR D'ARMES (BUILD DE TEST)
Minecraft 1.20.1 / Forge 47.2.0 / Java 17 / TACZ 1.1.8 Hotfix

Ce projet est isolé dans tools/weapon-distributor. Le workflow GitHub Actions compile le mod et publie le JAR comme artefact téléchargeable pendant 14 jours.

Armes configurées :
- Deagle 50 : tacz:deagle / tacz:50ae
- M16A1 : tacz:m16a1 / tacz:556x45
- QBZ-95 : tacz:qbz_95 / tacz:58x42
- M95 : tacz:m95 / tacz:50bmg
- M107 : tacz:m107 / tacz:50bmg
- SPAS-12 : tacz:spas_12 / tacz:12g
- M870 : tacz:m870 / tacz:12g
- AKM (définition TACZ ak47) : tacz:ak47 / tacz:762x39
- HK-MP5A5 : tacz:hk_mp5a5 / tacz:9mm
- .22 Model 943 : tacz:taurus943 / tacz:22wmr

Comportement :
- Zombies : arme aléatoire au début et à chaque vague, boîte TACZ universelle à munitions infinies.
- WorldBattle : arme aléatoire au début de chaque manche et après la mort, boîte de niveau fer avec 64 munitions adaptées.
- SandWars : arme aléatoire au début de chaque manche, boîte de niveau fer avec 64 munitions adaptées.
- Évite la même arme deux fois de suite pour un joueur lorsque plusieurs armes sont disponibles.
- Vide l'inventaire lors d'une mort en mode reconnu.

IMPORTANT :
- Il s'agit d'un build expérimental. La détection utilise la réflexion sur les champs privés du mod d'arènes 0.4.1.
- Ce JAR doit être testé sur une copie du serveur et du monde, jamais directement en production.
- Les règles d'élimination propres à chaque mode doivent être vérifiées en jeu avant de considérer le mod stable.

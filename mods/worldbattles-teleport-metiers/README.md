# WorldBattles Teleportation + Pause Métiers v1.0.0 (Forge 1.20.1)

Mod uniquement serveur, aucun mod supplémentaire côté joueurs.

## Commandes de téléportation
- `/spawn` → `minecraft:overworld`, 23 -58 18, orientation 0 0.
- `/freelands` → `secondworld:overworld`, -541 120 1209, orientation 0 0.
- `/minage` → `minage:overworld`, -541 120 1209, orientation 0 0.

Les commandes sont ouvertes à tous. Elles ne modifient ni les inventaires ni les modes de jeu. Les dimensions doivent exister.

## Pause métiers (OP niveau 2)
- `/metiersadmin pause` / `/metiersadmin resume` / `/metiersadmin status` pour soi.
- `/metiersadmin pause <joueur>` / `resume <joueur>` / `status <joueur>` pour un joueur en ligne.
- Depuis la console, toujours spécifier le pseudo : `metiersadmin pause Pseudo`.

La pause manuelle est persistante (scoreboard `fj_pause`). L'arrêt des gains d'XP, y compris en Créatif, nécessite le **datapack Farlands Métiers V5.4 modifié** distribué avec ce mod. Le mod seul ne bloque pas l'XP.

## Installation
1. Faire une sauvegarde du monde, arrêter AxentHost.
2. Mettre `worldbattles-teleport-metiers-1.0.0.jar` dans `mods/` sur le serveur seulement.
3. Dans `datapacks/` du monde actif, remplacer l'ancien ZIP Farlands Métiers V5.4 par `Farlands_Metiers_V5_4_Pause_Creatif.zip`. **Ne pas laisser les deux ZIP**.
4. Redémarrer complètement. Tester les trois destinations et la pause métiers.

Le datapack conserve tous les niveaux, XP, récompenses et valeurs de progression du V5.4 d'origine. Seuls les 7 points de gain XP sont protégés contre la pause manuelle et le mode Créatif. La compilation CI ne remplace pas un test en jeu.

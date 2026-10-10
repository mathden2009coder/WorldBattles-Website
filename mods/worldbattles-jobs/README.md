# WorldBattles Métiers v0.1.0 (Forge 1.20.1)

**Interface de consultation, 100 % séparée du datapack.** Ne modifie aucun niveau, XP, objectif, récompense, loot, commande du datapack ou dimension. Laisse **Farlands Métiers V5.4** installé et actif : il reste l'unique source de vérité.

## Installation

1. Arrêter AxentHost, faire une sauvegarde du monde et du datapack.
2. Ajouter `worldbattles-jobs-0.1.0.jar` dans le dossier `mods` du serveur.
3. Ajouter le même `.jar` dans le dossier `mods` des joueurs Forge 1.20.1 (Java 17).
4. Garder WorldBattles Shop installé; les deux mods sont indépendants.
5. Démarrer et taper `/metiers`, `/wbmetiers` ou `/jobs`.

L'ancienne commande `/trigger metiers` du datapack reste disponible pour l'interface chat. Le mod ne touche pas à `/trigger metier set X` ni aux fonctions Farlands.

## Écrans

- **Tous les métiers** : 7 cartes, niveaux /50, XP / XP requis, barres de progression, total des niveaux et métiers au maximum.
- **Détails** : fonctionnement du métier, niveau, XP, prochaine récompense.
- **Récompenses** : catalogue exact des **50 récompenses pour chacun des 7 métiers** extrait de `data/farlands_jobs/functions/ui/next/*.mcfunction` de Farlands V5.4, défilement à la molette, niveaux atteints / prochains niveaux.
- Mise à jour automatique des statistiques toutes les ~2 secondes, depuis le serveur, sans rouvrir la fenêtre ni recentrer la souris.
- Interface compacte bleu nuit / orange dans la DA du WorldBattles Shop.

## Compatibilité / sécurité

- Lit les objectifs scoreboard `fj_m_*`, `fj_l_*`, `fj_f_*`, `fj_h_*`, `fj_p_*`, `fj_e_*`, `fj_b_*` et vérifie `fj_sys`.
- Ne crée **aucun objectif scoreboard**; ne donne aucun XP, objet ou argent; n'appelle aucune fonction de datapack.
- L'affichage est autorisé partout, comme l'interface chat du datapack. **Les gains d'XP restent limités aux dimensions Farlands par le datapack**, sans modification.
- Ne remplace ni WorldBattles Shop, ni les systèmes de portails, inventaires, SpawnGuard, Minage ou SecondWorld.
- Les descriptions des récompenses reflètent exactement le datapack V5.4. Si tu changes les récompenses du datapack dans le futur, il faudra actualiser le catalogue du mod.

## Validation

Les tests de compatibilité statiques et la compilation sont exécutés par GitHub Actions. Tester en jeu sur une copie du serveur avec un joueur ayant déjà progressé, un joueur neuf, et après un redémarrage, avant un déploiement général.

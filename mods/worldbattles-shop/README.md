# WorldBattles Shop v0.2.2 — interface personnalisée

**Prototype Forge 1.20.1 / Java 17.** Interface originale inspirée du site WorldBattles : bleu nuit `#060912`, panneaux `#0c1320`, orange `#ff8419`, typographie claire, cartes interactives, barre de navigation permanente. Ce n'est pas un coffre Minecraft recoloré : l'écran est entièrement dessiné par le mod.

## Installation : client **ET** serveur

Cette version contient un `MenuType` Forge et un `ShopScreen` personnalisé. Le **même `.jar` doit être installé sur le serveur AxentHost et dans le modpack Forge 1.20.1 de chaque joueur**. Un joueur sans le mod ne pourra pas rejoindre normalement. Aucun datapack de dimensions ou de portails n'est modifié.

## Fonctions implémentées dans le code

- `/shop`, `/wbshop` : accueil de la boutique WorldBattles.
- `/ah` : marché entre joueurs, annonces avec NBT (items moddés inclus), confirmation avant achat.
- `/wbshop list` : déposer l'objet tenu en main, choisir quantité et prix du lot.
- `/wbshop sell` : vendre immédiatement des ressources au serveur, quantité 1/16/64/tout.
- `/bal`, `/baltop` : banque individuelle et classement des comptes.
- `/wbshop mail` : récupérer achats et annonces annulées; colis en attente conservés si inventaire plein.
- `/wbshopadmin add <joueur> <montant>` / `set` : commandes OP de test et gestion.
- Paiement d'un vendeur hors ligne, transactions côté serveur et sauvegarde persistante.
- Shop **interdit** dans `minecraft:overworld`, `minecraft:the_nether`, `minecraft:the_end`; **autorisé** dans `secondworld:overworld`, `secondworld:the_nether`, `minage:overworld`, `minage:the_nether`.

## Construction

Workflow GitHub Actions sur la branche `feature/worldbattles-shop` (sans modifier `main`). Build :

```sh
gradle build --no-daemon --stacktrace
```

Le fichier attendu est `build/libs/worldbattles-shop-0.2.2.jar`.

## Avant utilisation sur le serveur principal

**La compilation et les tests en jeu ne sont pas encore confirmés.** Ne pas installer le prototype en production. Vérifier sur une copie du serveur, avec deux joueurs, les cas : inventaire plein, vente d'items moddés avec NBT, annonce achetée deux fois, vendeurs hors ligne, annulation, redémarrage, mort et changement de dimension. Retirer MiguelEconomy s'il est présent (conflit de commandes `/shop`, `/ah`).

Le shop et les soldes sont sauvegardés dans `world/data/worldbattles_shop_v1.dat` (nom conservé pour compatibilité avec le prototype précédent). Une sauvegarde régulière du monde est nécessaire; les sauvegardes ne sont pas une base de données transactionnelle garantie contre une coupure brutale.

## Affichage v0.2.2

- Tableau plus compact (84 % de la fenêtre disponible, maximum 440 × 285 pixels GUI), même thème.
- Catalogue de 24 articles par page, 4 lignes visibles et défilement à la molette.
- Position du curseur restaurée lors des changements de page du shop.
- Aucun changement aux sauvegardes, à l'économie ou aux dimensions.

# Rapport technique — comment le régénérer

Le rapport Word `Rapport_technique_ATT.docx` est produit par `generer_rapport.js` (bibliothèque `docx` pour Node, installée localement) :

```bash
cd docs/rapport
npm install          # une seule fois (crée node_modules/, ignoré par Git)
node generer_rapport.js
```

- Le texte des sections est dans `generer_rapport.js`, une fonction par chapitre.
- Les captures d'écran vont dans `captures/` avec le nom attendu par chaque figure (par exemple `06_09_inscription.png`) ; tant qu'un fichier manque, un cadre « capture à insérer » prend sa place.
- Les passages surlignés en jaune (« À compléter — … ») sont à rédiger pendant le rejeu de A à Z ; les consignes grises en italique sont à supprimer une fois la section écrite.
- Ouvrir le document dans Word et accepter la mise à jour des champs pour remplir le sommaire ; exporter en PDF depuis Word pour la version finale.

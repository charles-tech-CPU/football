-- KF DRITA / KF MALISHEVA / KF DUKAGJINI ont ete fusionnees avec leurs doublons du
-- championnat national (DRITA / MALISHEVA / DUKAGJINI) en gardant le code pays court
-- "KOS" (utilise pour les qualifications europeennes) au lieu du nom complet "Kosovo"
-- (utilise par la competition championnat). Consequence : /api/teams?competitionId=<Kosovo>
-- filtre sur le pays exact de la competition et ne les retourne plus, ce qui les rend
-- impossibles a selectionner dans les menus de saisie/edition de match du championnat.
UPDATE team SET country = 'Kosovo' WHERE id IN (840, 937, 940) AND country = 'KOS';

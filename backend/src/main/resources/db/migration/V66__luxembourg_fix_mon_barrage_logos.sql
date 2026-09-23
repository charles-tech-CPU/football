-- "MON" (id 816) est en realite MONDORF LES BAINS (id 908), tronque sur 3 de ses 30 matchs
-- lors de la saisie (confirme : MON n'a que ces 3 matchs, tous sans score, et MONDORF LES
-- BAINS n'en a que 27 au lieu de 30 - les 2 se completent exactement a 30 une fois fusionnes).
UPDATE match SET team1_id = 908 WHERE team1_id = 816;
UPDATE match SET team2_id = 908 WHERE team2_id = 816;
DELETE FROM team WHERE id = 816;

-- FC DIFFERDANGE 03 - ETTELBRUCK apparaissait 2 fois avec FC DIFFERDANGE a domicile les 2
-- fois (J7 joue, J21 a venir) : une fois MON fusionne, chaque equipe a bien exactement 30
-- matchs et 2 confrontations par adversaire, donc ce n'est pas un doublon a supprimer - juste
-- une simple inversion domicile/exterieur a corriger sur le match a venir (J21, pas encore
-- joue) pour que l'algorithme de detection de "2e phase" ne le prenne plus a tort pour une
-- confrontation supplementaire.
UPDATE match SET team1_id = (SELECT id FROM team WHERE name = 'ETTELBRUCK'), team2_id = (SELECT id FROM team WHERE name = 'FC DIFFERDANGE 03') WHERE id = 9268;

-- Blasons Luxembourg recuperes via TheSportsDB.
UPDATE team SET logo_path = '433.png' WHERE id = 433; -- DUDELANGE -> F91 Dudelange
UPDATE team SET logo_path = '436.png' WHERE id = 436; -- RACING Luxembourg -> Racing Union Luxembourg

-- Icones : 13e et 14e barragistes (barrages deja en place), 15e et 16e relegues.
UPDATE competition SET barrage_slots = 2, relegation_slots = 2 WHERE code = 'LUXEMBOURG' AND season = 2027;

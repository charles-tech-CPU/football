-- "CJAR" (id 823) et "BEC" (id 827) sont des noms corrompus dans les donnees source, sans
-- aucun rapport avec un vrai club belge. Le seul match de chacun correspond exactement au
-- match retour manquant d'une equipe reelle (confirme par le club absent de la journee et
-- l'existence du match aller sous le bon nom) :
-- - J21 (id 8203) "CERCLE BRUGES - CJAR" = retour de "CHARLEROI - CERCLE BRUGES" (J7).
-- - J28 (id 9707) "RSC ANDERLECHT - BEC" = retour de "BEVEREN - RSC ANDERLECHT" (J2).
UPDATE match SET team2_id = 534 WHERE id = 8203; -- CERCLE BRUGES - CHARLEROI
UPDATE match SET team2_id = 546 WHERE id = 9707; -- RSC ANDERLECHT - BEVEREN

-- Fiches fantomes desormais sans aucun match : suppression.
DELETE FROM team WHERE id IN (823, 827);

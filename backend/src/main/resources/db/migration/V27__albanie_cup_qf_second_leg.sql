-- Les quarts de finale de la coupe d'Albanie se jouent en aller-retour (confirme par
-- l'utilisateur), comme les demi-finales (deja 2 dates distinctes en base). Seul le match
-- aller avait ete importe (V16) pour chacune des 4 confrontations ; on ajoute le match
-- retour manquant (meme round, pas de date connue pour l'instant).
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status)
SELECT competition_id, round_label, NULL, NULL, team1_id, team2_id, NULL, NULL, 'SCHEDULED'
FROM match WHERE id IN (11938, 11939, 11940, 11941);

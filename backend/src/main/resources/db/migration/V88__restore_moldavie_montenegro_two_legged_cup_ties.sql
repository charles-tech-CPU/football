-- V69/V70 avaient supprime ces lignes en les prenant pour un doublon d'import (meme motif
-- que Gibraltar). Confirme par Charles : les quarts ET demies de la coupe de Moldavie, ainsi
-- que les demies de la coupe du Montenegro, se jouent bien en aller-retour - il fallait donc
-- restaurer le 2e lot de places generiques plutot que le supprimer.
--
-- Tant que les 2 equipes restent des places generiques "A DETERMINER", les 2 manches d'une
-- meme confrontation sont sinon indiscernables l'une de l'autre (memes equipes generiques
-- des 2 cotes) : CupBracket.vue s'appuie donc sur un suffixe explicite "- Aller"/"- Retour"
-- dans le round_label pour les regrouper 2 par 2 au lieu de les afficher comme des
-- confrontations distinctes (cf. ALLER_RETOUR_RE dans CupBracket.vue).

-- Moldavie : quarts aller-retour (4 confrontations x 2 manches).
UPDATE match SET round_label = round_label || ' - Aller' WHERE id IN (11902, 11903, 11904, 11905);
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status)
SELECT competition_id, replace(round_label, ' - Aller', ' - Retour'), date, time, team1_id, team2_id, score1, score2, status
FROM match WHERE id IN (11902, 11903, 11904, 11905);

-- Moldavie : demies aller-retour (2 confrontations x 2 manches).
UPDATE match SET round_label = round_label || ' - Aller' WHERE id IN (12122, 12123);
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status)
SELECT competition_id, replace(round_label, ' - Aller', ' - Retour'), date, time, team1_id, team2_id, score1, score2, status
FROM match WHERE id IN (12122, 12123);

-- Montenegro : demies aller-retour (2 confrontations x 2 manches).
UPDATE match SET round_label = round_label || ' - Aller' WHERE id IN (12073, 12074);
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status)
SELECT competition_id, replace(round_label, ' - Aller', ' - Retour'), date, time, team1_id, team2_id, score1, score2, status
FROM match WHERE id IN (12073, 12074);

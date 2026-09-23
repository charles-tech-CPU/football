-- Les demies de la coupe du Portugal n'avaient qu'1 seule confrontation avec 2 dates (3/3 et
-- 22/4) au lieu de 2 confrontations paralleles (4 demi-finalistes issus des quarts) jouees
-- chacune en aller-retour a ces memes 2 dates. On complete avec la 2e confrontation
-- manquante et on marque les 2 manches existantes comme Aller/Retour (cf. convention
-- ALLER_RETOUR_SUFFIX_RE de CupBracket.vue, V88).
UPDATE match SET round_label = round_label || ' - Aller' WHERE id = 11876;
UPDATE match SET round_label = round_label || ' - Retour' WHERE id = 12063;

INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status)
VALUES
  ((SELECT competition_id FROM match WHERE id = 11876), 'Coupe nationale (a determiner : DF-DF) - Aller', '2027-03-03', NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT competition_id FROM match WHERE id = 11876), 'Coupe nationale (a determiner : DF-DF) - Retour', '2027-04-22', NULL, 986, 987, NULL, NULL, 'SCHEDULED');

-- Icones classement : champion (1er), LDC (2e), Europa League (3e, une seule place au lieu
-- de 2), Conference League (4e, deja correct).
UPDATE competition SET el_slots = 1 WHERE code = 'PORTUGAL' AND season = 2027;

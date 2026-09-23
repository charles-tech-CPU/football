-- La coupe d'Eire partageait le meme round_label generique "Coupe nationale" pour tous les
-- tours joues (seule la finale, deja en placeholder, etait correctement etiquetee) :
-- reconstitue a partir des dates/scores (8 equipes en 1/8e, 4 en quarts, 2 en demies, tout
-- en match unique).
UPDATE match SET round_label = 'Coupe nationale - Huitièmes de finale'
WHERE id IN (1999, 2000, 2001, 2046, 2057, 2087, 2111, 2186);
UPDATE match SET round_label = 'Coupe nationale - Quarts de finale'
WHERE id IN (2974, 2975, 3049, 3192);
UPDATE match SET round_label = 'Coupe nationale - Demi-finales'
WHERE id IN (4417, 4418);

-- Barrage promotion/relegation : le vainqueur du 2e-5e et le vainqueur du 3e-4e (deja en
-- base) doivent ensuite s'affronter (2/11), puis le vainqueur de ce match affronte le 9e de
-- Premier Division (11/11) - ces 2 dernieres etapes manquaient.
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES
  ((SELECT id FROM competition WHERE code = 'EIRE' AND season = 2027), 'Barrage (a determiner : W1-W2)', '2026-11-02', NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'EIRE' AND season = 2027), 'Barrage (a determiner : 9E-W3)', '2026-11-11', NULL, 986, 987, NULL, NULL, 'SCHEDULED');

-- 3e et 4e places qualificatives desormais Conference League (au lieu de EL seul pour la
-- 2e place) : eclSlots 1 -> 2 couvre automatiquement les rangs 3 et 4.
UPDATE competition SET ecl_slots = 2 WHERE code = 'EIRE' AND season = 2027;

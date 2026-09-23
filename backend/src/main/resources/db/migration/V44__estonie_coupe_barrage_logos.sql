-- Blasons Estonie recuperes via TheSportsDB.
UPDATE team SET logo_path = '34.png' WHERE id = 34; -- PARNU JK VAPRUS -> Pärnu Vaprus
UPDATE team SET logo_path = '45.png' WHERE id = 45; -- NARVA -> Narva Trans
UPDATE team SET logo_path = '46.png' WHERE id = 46; -- HARJU JK LAAGRI -> Harju Laagri

-- La coupe partageait le meme round_label generique "Coupe nationale" pour tous les tours
-- (quarts/demies/finale non distingues) : reconstitue a partir des dates/scores (8 equipes
-- en quarts, tout en match unique).
UPDATE match SET round_label = 'Coupe nationale - Quarts de finale' WHERE id IN (80, 82, 87, 88);
UPDATE match SET round_label = 'Coupe nationale - Demi-finales' WHERE id IN (643, 648);
UPDATE match SET round_label = 'Coupe nationale - Finale' WHERE id = 882;

-- Barrage promotion/relegation : 9e de Division 1 contre le 2e de Division 2, aller-retour
-- (22/11 et 29/11).
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES
  ((SELECT id FROM competition WHERE code = 'ESTONIE' AND season = 2027), 'Barrage (a determiner : 9E-2E)', '2026-11-22', NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'ESTONIE' AND season = 2027), 'Barrage (a determiner : 9E-2E)', '2026-11-29', NULL, 986, 987, NULL, NULL, 'SCHEDULED');

-- Icones : 9e barragiste, 10e relegue.
UPDATE competition SET relegation_slots = 1, barrage_slots = 1 WHERE code = 'ESTONIE' AND season = 2027;

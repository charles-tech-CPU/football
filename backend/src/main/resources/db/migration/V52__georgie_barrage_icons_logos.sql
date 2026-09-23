-- Blasons Georgie recuperes via TheSportsDB.
UPDATE team SET logo_path = '744.png' WHERE id = 744; -- DINAMO BATOUMI -> Dinamo Batumi
UPDATE team SET logo_path = '909.png' WHERE id = 909; -- DINAMO TBILISSI -> Dinamo Tbilisi
UPDATE team SET logo_path = '904.png' WHERE id = 904; -- TORPEDO KOUTAISSI -> Torpedo Kutaisi

-- Barrages promotion/relegation : 8e de Division 1 contre le 3e de Division 2, et 9e de
-- Division 1 contre le 2e de Division 2. Equipes et dates pas encore connues (places
-- generiques "A DETERMINER", comme pour les autres barrages/tours en attente de tirage).
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES
  ((SELECT id FROM competition WHERE code = 'GEORGIE' AND season = 2027), 'Barrage (a determiner : 8E-3E)', NULL, NULL, (SELECT id FROM team WHERE name = 'A DETERMINER (1)'), (SELECT id FROM team WHERE name = 'A DETERMINER (2)'), NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'GEORGIE' AND season = 2027), 'Barrage (a determiner : 9E-2E)', NULL, NULL, (SELECT id FROM team WHERE name = 'A DETERMINER (1)'), (SELECT id FROM team WHERE name = 'A DETERMINER (2)'), NULL, NULL, 'SCHEDULED');

-- Icones : 1er champion, 2e Europa League, 3e Conference League, 8e et 9e barragistes, 10e relegue.
UPDATE competition SET el_slots = 1, ecl_slots = 1, barrage_slots = 2, relegation_slots = 1
WHERE code = 'GEORGIE' AND season = 2027;

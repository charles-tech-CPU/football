-- Blasons Lettonie recuperes via TheSportsDB.
UPDATE team SET logo_path = '85.png' WHERE id = 85; -- AUDA -> Auda Kekava
UPDATE team SET logo_path = '41.png' WHERE id = 41; -- BFC DAUGAVPILS -> Daugavpils

-- La coupe partageait le meme round_label generique "Coupe nationale" pour tous les tours :
-- reconstitue a partir des dates/vainqueurs (8 equipes en huitiemes -> quarts -> demies ->
-- finale, tout en match unique). 2 tours se sont joues a egalite (RIGA MARINERS-BFC
-- DAUGAVPILS 3-3 en huitiemes, BFC DAUGAVPILS-AUDA 2-2 en demies) et ont ete qualifies aux
-- tirs au but d'apres la suite du tableau (BFC DAUGAVPILS puis AUDA rejouent ensuite) :
-- scores de tab non connus, a ajouter via le controle "+ Tirs au but" du bracket.
UPDATE match SET round_label = 'Coupe nationale - Huitièmes de finale'
WHERE id IN (1282, 1299, 1300, 1301, 1302, 1317, 1322, 1327);
UPDATE match SET round_label = 'Coupe nationale - Quarts de finale'
WHERE id IN (1977, 2108, 2139, 2202);
UPDATE match SET round_label = 'Coupe nationale - Demi-finales'
WHERE id IN (2891, 2920);
UPDATE match SET round_label = 'Coupe nationale - Finale'
WHERE id = 5150;

-- Barrage promotion/relegation : 9e de Division 1 contre le 2e de Division 2, aller-retour
-- (20/11 et 23/11).
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES
  ((SELECT id FROM competition WHERE code = 'LETTONIE' AND season = 2027), 'Barrage (a determiner : 9E-2E)', '2026-11-20', NULL, (SELECT id FROM team WHERE name = 'A DETERMINER (1)'), (SELECT id FROM team WHERE name = 'A DETERMINER (2)'), NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'LETTONIE' AND season = 2027), 'Barrage (a determiner : 9E-2E)', '2026-11-23', NULL, (SELECT id FROM team WHERE name = 'A DETERMINER (1)'), (SELECT id FROM team WHERE name = 'A DETERMINER (2)'), NULL, NULL, 'SCHEDULED');

-- Icones : 1er champion, 2e/3e/4e conference, 9e barragiste, 10e relegue.
UPDATE competition SET el_slots = 0, ecl_slots = 3, barrage_slots = 1, relegation_slots = 1
WHERE code = 'LETTONIE' AND season = 2027;

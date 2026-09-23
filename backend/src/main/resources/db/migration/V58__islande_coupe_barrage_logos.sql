-- Blasons Islande recuperes via TheSportsDB.
UPDATE team SET logo_path = '156.png' WHERE id = 156; -- FRAM -> Fram Reykjavik
UPDATE team SET logo_path = '955.png' WHERE id = 955; -- VALUR REYKJAVIK -> Valur
UPDATE team SET logo_path = '168.png' WHERE id = 168; -- NJARDVIK -> UMF Njardvik

-- La coupe partageait le meme round_label generique "Coupe nationale" pour tous les tours :
-- reconstitue a partir des dates/vainqueurs (8 equipes en huitiemes -> quarts -> demies ->
-- finale, tout en match unique). 2 tours se sont joues a egalite (AKRANES-GRINDAVIK 2-2 en
-- huitiemes, FYLKIR-AFTURELDING 1-1 en demies) et ont ete qualifies aux tirs au but d'apres
-- la suite du tableau (AKRANES et AFTURELDING rejouent ensuite) : scores de tab non connus,
-- a ajouter via le nouveau controle "+ Tirs au but" du bracket.
UPDATE match SET round_label = 'Coupe nationale - Huitièmes de finale'
WHERE id IN (736, 737, 738, 739, 740, 743, 744, 745);
UPDATE match SET round_label = 'Coupe nationale - Quarts de finale'
WHERE id IN (1006, 1011, 1016, 1033);
UPDATE match SET round_label = 'Coupe nationale - Demi-finales'
WHERE id IN (1200, 1416);
UPDATE match SET round_label = 'Coupe nationale - Finale'
WHERE id = 3371;

-- Barrage de promotion (playoff interne a la Division 2, pour une 2e place de montee) :
-- 2e de D2 contre 5e de D2 et 3e de D2 contre 4e de D2 (aller-retour), puis les vainqueurs de
-- ces 2 confrontations s'affrontent pour la promotion. Equipes et dates pas encore connues.
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES
  ((SELECT id FROM competition WHERE code = 'ISLANDE' AND season = 2027), 'Barrage promotion (a determiner : 2E-5E)', NULL, NULL, (SELECT id FROM team WHERE name = 'A DETERMINER (1)'), (SELECT id FROM team WHERE name = 'A DETERMINER (2)'), NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'ISLANDE' AND season = 2027), 'Barrage promotion (a determiner : 2E-5E)', NULL, NULL, (SELECT id FROM team WHERE name = 'A DETERMINER (1)'), (SELECT id FROM team WHERE name = 'A DETERMINER (2)'), NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'ISLANDE' AND season = 2027), 'Barrage promotion (a determiner : 3E-4E)', NULL, NULL, (SELECT id FROM team WHERE name = 'A DETERMINER (1)'), (SELECT id FROM team WHERE name = 'A DETERMINER (2)'), NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'ISLANDE' AND season = 2027), 'Barrage promotion (a determiner : 3E-4E)', NULL, NULL, (SELECT id FROM team WHERE name = 'A DETERMINER (1)'), (SELECT id FROM team WHERE name = 'A DETERMINER (2)'), NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'ISLANDE' AND season = 2027), 'Barrage promotion (a determiner : W1-W2)', NULL, NULL, (SELECT id FROM team WHERE name = 'A DETERMINER (1)'), (SELECT id FROM team WHERE name = 'A DETERMINER (2)'), NULL, NULL, 'SCHEDULED');

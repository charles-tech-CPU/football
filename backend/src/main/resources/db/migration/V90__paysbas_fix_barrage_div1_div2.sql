-- Le barrage promotion-relegation (Divisie 1/Divisie 2) avait ete saisi avec les mauvais
-- appariements (dupliquait a tort les paires du Barrage Europe, 5E-8E/6E-7E, au lieu du
-- vrai bracket a 6 equipes 3E-8E, 4E-7E, 5E-6E) et il manquait la 3e confrontation du 1er
-- tour ainsi que les 2 demi-finales (W1-W2 et 16E-W3) : seule une "finale" (mal nommee
-- W3-W4) etait presente. Le Barrage Europe (5E-8E/6E-7E puis W1-W2) etait deja correct,
-- non touche ici.
DELETE FROM match WHERE id IN (12091, 12092, 12104, 12105, 12161, 12180);

INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status)
VALUES
  -- 1er tour (3 confrontations aller-retour entre les 3e-8e de Division 2).
  ((SELECT id FROM competition WHERE code = 'PAYS-BAS' AND season = 2027), 'Barrage (a determiner : 7E-4E)', '2027-04-28', NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'PAYS-BAS' AND season = 2027), 'Barrage (a determiner : 4E-7E)', '2027-05-01', NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'PAYS-BAS' AND season = 2027), 'Barrage (a determiner : 8E-3E)', '2027-04-28', NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'PAYS-BAS' AND season = 2027), 'Barrage (a determiner : 3E-8E)', '2027-05-01', NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'PAYS-BAS' AND season = 2027), 'Barrage (a determiner : 6E-5E)', '2027-05-05', NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'PAYS-BAS' AND season = 2027), 'Barrage (a determiner : 5E-6E)', '2027-05-09', NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  -- Demi-finales : vainqueur(7E-4E)/vainqueur(8E-3E), et le 16e d'Eredivisie contre le
  -- vainqueur(6E-5E).
  ((SELECT id FROM competition WHERE code = 'PAYS-BAS' AND season = 2027), 'Barrage (a determiner : W1-W2)', '2027-05-05', NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'PAYS-BAS' AND season = 2027), 'Barrage (a determiner : W2-W1)', '2027-05-09', NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'PAYS-BAS' AND season = 2027), 'Barrage (a determiner : 16E-W3)', '2027-05-13', NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'PAYS-BAS' AND season = 2027), 'Barrage (a determiner : W3-16E)', '2027-05-16', NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  -- Finale : vainqueur(W1-W2) contre vainqueur(16E-W3).
  ((SELECT id FROM competition WHERE code = 'PAYS-BAS' AND season = 2027), 'Barrage (a determiner : W4-W5)', '2027-05-20', NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'PAYS-BAS' AND season = 2027), 'Barrage (a determiner : W5-W4)', '2027-05-23', NULL, 986, 987, NULL, NULL, 'SCHEDULED');

-- Icones : champion (1er), LDC (2e-3e), Europa League directe (4e) ; le Barrage Europe
-- (5e-8e) determine une place Conference supplementaire (pas d'attribution directe -
-- eclSlots remis a 0) ; barragiste (16e, deja couvert par barrageSlots) et relegation
-- directe (17e-18e, relevee de 1 a 2 places).
UPDATE competition SET el_slots = 1, ecl_slots = 0, relegation_slots = 2
WHERE code = 'PAYS-BAS' AND season = 2027;

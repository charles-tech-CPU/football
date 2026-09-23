-- Norvege : la cascade de barrage pour une place europeenne continue au-dela du match
-- 5e-6e deja en base (id 11566, 'Barrage (a determiner : 5E-6E)', 2026-11-28) - meme
-- principe que la cascade d'Irlande du Nord (6e-7e puis 5e puis 4e) : le vainqueur affronte
-- ensuite le 4e (2/12), puis le vainqueur de ce match affronte le 3e (6/12). Le vainqueur de
-- cette derniere etape affronte enfin le 14e en barrage de promotion-relegation, cette fois
-- en aller-retour (13/12 et 17/12).
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status)
VALUES
  ((SELECT id FROM competition WHERE code = 'NORVEGE' AND season = 2027), 'Barrage (a determiner : 4E-W1)', '2026-12-02', NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'NORVEGE' AND season = 2027), 'Barrage (a determiner : 3E-W2)', '2026-12-06', NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'NORVEGE' AND season = 2027), 'Barrage (a determiner : 14E-W3)', '2026-12-13', NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'NORVEGE' AND season = 2027), 'Barrage (a determiner : W3-14E)', '2026-12-17', NULL, 986, 987, NULL, NULL, 'SCHEDULED');

-- Icones : barragiste au 14e (deja couvert par barrage_slots = 1), relegation directe pour
-- les 15e et 16e (releve de 1 a 2 places).
UPDATE competition SET relegation_slots = 2 WHERE code = 'NORVEGE' AND season = 2027;

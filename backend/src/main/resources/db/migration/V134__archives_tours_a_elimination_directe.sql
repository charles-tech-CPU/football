-- Archives internationales (V126-V131) : tours a elimination directe mal nommes par le script
-- d'import, ce qui faussait le parcours des selections (onglet Bilan, cf. InternationalStatsService).
-- Matchs retrouves par competition, date et libelle (leurs identifiants different d'une base a l'autre).

-- 1. Finale et match pour la 3e place joues le meme jour : le script a pris le mauvais pour la finale
-- (corrige depuis dans import/international_archive.py, cf. final_of). La vraie finale oppose les
-- vainqueurs des demi-finales : RD Congo - Ghana (CAN 1968), Afrique du Sud - Tunisie (CAN 1996),
-- Iran - Coree du Sud (Coupe d'Asie 1972), Mexique - Bresil (Gold Cup 1996).
UPDATE match
SET round_label = CASE round_label WHEN 'Finale' THEN 'Match pour la 3e place' ELSE 'Finale' END
WHERE round_label IN ('Finale', 'Match pour la 3e place')
  AND (competition_id, date) IN (
      ((SELECT id FROM competition WHERE code = 'CAF_AFCON_1968'), DATE '1968-01-21'),
      ((SELECT id FROM competition WHERE code = 'CAF_AFCON_1996'), DATE '1996-02-03'),
      ((SELECT id FROM competition WHERE code = 'AFC_ASIANCUP_1972'), DATE '1972-05-19'),
      ((SELECT id FROM competition WHERE code = 'CONCACAF_GOLDCUP_1996'), DATE '1996-01-21'));

-- 2. Copa America 1975, 1979 et 1983 (sans pays hote) : demi-finales et finale en aller-retour, que
-- le script ne reconnaissait pas (libelle "Tour preliminaire"). Le match aller de la finale precede la
-- finale (et son eventuel match d'appui) deja nommee.
UPDATE match
SET round_label = 'Finale - Aller'
WHERE round_label LIKE 'Tour pr%'
  AND (competition_id, date) IN (
      ((SELECT id FROM competition WHERE code = 'CONMEBOL_COPA_1975'), DATE '1975-10-16'),
      ((SELECT id FROM competition WHERE code = 'CONMEBOL_COPA_1979'), DATE '1979-11-28'),
      ((SELECT id FROM competition WHERE code = 'CONMEBOL_COPA_1983'), DATE '1983-10-27'));

UPDATE match
SET round_label = CASE WHEN round_label LIKE '% - Retour' THEN 'Demi-finales - Retour' ELSE 'Demi-finales - Aller' END
WHERE round_label LIKE 'Tour pr%'
  AND competition_id IN (
      SELECT id FROM competition WHERE code IN ('CONMEBOL_COPA_1975', 'CONMEBOL_COPA_1979', 'CONMEBOL_COPA_1983'));

-- 3. Coupe du monde 1938 : le 1er tour est un tour de 8es de finale ; les matchs nommes "Retour" sont
-- des matchs rejoues apres un nul (Allemagne - Suisse, Cuba - Roumanie).
UPDATE match
SET round_label = CASE WHEN round_label LIKE '% - Retour' THEN '8es de finale (match rejoué)' ELSE '8es de finale' END
WHERE round_label LIKE 'Tour pr%'
  AND competition_id = (SELECT id FROM competition WHERE code = 'FIFA_WC_1938');

-- 4. CAN 1957 (3 equipes) : Soudan - Egypte est une demi-finale, l'Ethiopie etait exemptee.
UPDATE match
SET round_label = 'Demi-finales'
WHERE round_label LIKE 'Tour pr%'
  AND competition_id = (SELECT id FROM competition WHERE code = 'CAF_AFCON_1957');

-- 5. Gold Cup 2005 : finale Etats-Unis - Panama (0-0) gagnee 3-1 aux tirs au but par les Etats-Unis,
-- score des tirs au but absent de la source.
UPDATE match
SET penalty_score1 = CASE WHEN team1_id = (SELECT id FROM team WHERE name = 'Etats-Unis' AND country = 'Etats-Unis') THEN 3 ELSE 1 END,
    penalty_score2 = CASE WHEN team1_id = (SELECT id FROM team WHERE name = 'Etats-Unis' AND country = 'Etats-Unis') THEN 1 ELSE 3 END
WHERE round_label = 'Finale'
  AND penalty_score1 IS NULL
  AND competition_id = (SELECT id FROM competition WHERE code = 'CONCACAF_GOLDCUP_2005');

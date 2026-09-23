-- Corrige les noms mal orthographies et ajoute les blasons associes.
UPDATE team SET name = 'HEARTS OF MIDLOTHIAN', logo_path = '889.png' WHERE id = 889;
UPDATE team SET name = 'KILMARNOCK', logo_path = '427.png' WHERE id = 427;

-- Format a 2 mini-championnats (top 6 / bottom 6) comme l'Autriche, applique APRES les
-- 2 phases aller-retour deja existantes (12 equipes se rencontrent 3 fois au total dans le
-- calendrier ecossais reel) : cf. resultsGroupSplit.regularSeasonCycles = 2 cote frontend,
-- pour ne pas confondre la 2e phase reelle (deja dans les donnees) avec la phase de groupe.
UPDATE competition SET ldc_slots = 0, el_slots = 0, ecl_slots = 0, barrage_slots = 0, relegation_slots = 0
WHERE code = 'ECOSSE' AND season = 2027;

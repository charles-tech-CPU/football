-- Position d'une confrontation dans son tour de coupe (1 = en haut du tableau), pour pouvoir
-- reorganiser le bracket depuis l'interface quand l'ordre par date ne correspond pas au
-- vrai tableau. Null = ordre par defaut (date / position au tour precedent).
ALTER TABLE match ADD COLUMN bracket_position INTEGER;

-- Armenie : cette saison les 8es de finale comptent 8 confrontations (16 equipes), il en
-- manquait 2. Places generiques "A DETERMINER" et pas de date connue pour l'instant, a
-- completer depuis le bracket comme les autres tours en attente de tirage.
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status)
VALUES
  ((SELECT id FROM competition WHERE code = 'ARMENIE-CUP' AND season = 2027), 'Coupe nationale (a determiner : HF-HF)', NULL, NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'ARMENIE-CUP' AND season = 2027), 'Coupe nationale (a determiner : HF-HF)', NULL, NULL, 986, 987, NULL, NULL, 'SCHEDULED');

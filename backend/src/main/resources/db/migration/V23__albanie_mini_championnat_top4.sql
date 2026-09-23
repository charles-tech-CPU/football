-- Apres les 2 phases aller-retour du championnat d'Albanie, les 4 meilleures equipes
-- disputent un mini-championnat en un seul match par confrontation (pas d'aller-retour) :
-- 4 equipes = 6 confrontations (1-2, 1-3, 1-4, 2-3, 2-4, 3-4 au classement final des 2
-- phases). Equipes reelles connues seulement une fois les 2 phases terminees : places
-- generiques "A DETERMINER (1)"/"(2)", a completer manuellement comme pour les barrages
-- (V16). Pas de date communiquee pour l'instant.
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status)
VALUES
  ((SELECT id FROM competition WHERE code = 'ALBANIE' AND season = 2027), 'Mini-championnat (a determiner : 1ER-2E)', NULL, NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'ALBANIE' AND season = 2027), 'Mini-championnat (a determiner : 1ER-3E)', NULL, NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'ALBANIE' AND season = 2027), 'Mini-championnat (a determiner : 1ER-4E)', NULL, NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'ALBANIE' AND season = 2027), 'Mini-championnat (a determiner : 2E-3E)', NULL, NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'ALBANIE' AND season = 2027), 'Mini-championnat (a determiner : 2E-4E)', NULL, NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'ALBANIE' AND season = 2027), 'Mini-championnat (a determiner : 3E-4E)', NULL, NULL, 986, 987, NULL, NULL, 'SCHEDULED');

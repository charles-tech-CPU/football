-- Il manquait le tour precedant les quarts (8 equipes) : les huitiemes de finale (16
-- equipes), pas encore connues - places generiques "A DETERMINER", a completer comme les
-- autres tours en attente de tirage. Pas de date connue pour l'instant.
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status)
VALUES
  ((SELECT id FROM competition WHERE code = 'ESTONIE' AND season = 2027), 'Coupe nationale (a determiner : HF-HF)', NULL, NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'ESTONIE' AND season = 2027), 'Coupe nationale (a determiner : HF-HF)', NULL, NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'ESTONIE' AND season = 2027), 'Coupe nationale (a determiner : HF-HF)', NULL, NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'ESTONIE' AND season = 2027), 'Coupe nationale (a determiner : HF-HF)', NULL, NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'ESTONIE' AND season = 2027), 'Coupe nationale (a determiner : HF-HF)', NULL, NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'ESTONIE' AND season = 2027), 'Coupe nationale (a determiner : HF-HF)', NULL, NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'ESTONIE' AND season = 2027), 'Coupe nationale (a determiner : HF-HF)', NULL, NULL, 986, 987, NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'ESTONIE' AND season = 2027), 'Coupe nationale (a determiner : HF-HF)', NULL, NULL, 986, 987, NULL, NULL, 'SCHEDULED');

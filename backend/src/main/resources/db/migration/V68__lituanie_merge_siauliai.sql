-- FA SAUILIAI (734) et SIAULIAI FA (18) sont la meme equipe reelle (FA Siauliai), saisie
-- 2 fois sous 2 orthographes differentes en cours de saison (confirme par l'utilisateur : la
-- Lituanie n'a que 9 equipes, pas 10 - et les 2 fiches ne se rencontrent jamais elles-memes).
UPDATE match SET team1_id = 18 WHERE team1_id = 734;
UPDATE match SET team2_id = 18 WHERE team2_id = 734;

UPDATE team_competition_status tcs
SET team_id = 18
WHERE tcs.team_id = 734
  AND NOT EXISTS (
      SELECT 1 FROM team_competition_status tcs2
      WHERE tcs2.team_id = 18 AND tcs2.competition_id = tcs.competition_id
  );
DELETE FROM team_competition_status WHERE team_id = 734;

DELETE FROM team WHERE id = 734;

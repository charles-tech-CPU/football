-- Selections disparues comptees avec leur successeur reconnu par la FIFA (a la demande de
-- l'utilisateur) : la Yougoslavie avec la Serbie, la Tchecoslovaquie avec la Tchequie. Leurs matchs
-- passent au successeur (ils ne se sont jamais affrontes), puis l'ancienne selection est supprimee.
-- Meme principe que la fusion de clubs (TeamService.merge) ; import/international_archive.py les
-- range desormais directement sous le successeur.
UPDATE match SET team1_id = (SELECT id FROM team WHERE name = 'Serbie' AND country = 'Serbie')
WHERE team1_id = (SELECT id FROM team WHERE name = 'Yougoslavie' AND country = 'Yougoslavie');
UPDATE match SET team2_id = (SELECT id FROM team WHERE name = 'Serbie' AND country = 'Serbie')
WHERE team2_id = (SELECT id FROM team WHERE name = 'Yougoslavie' AND country = 'Yougoslavie');

UPDATE match SET team1_id = (SELECT id FROM team WHERE name = 'Tchequie' AND country = 'Tchequie')
WHERE team1_id = (SELECT id FROM team WHERE name = 'Tchecoslovaquie' AND country = 'Tchecoslovaquie');
UPDATE match SET team2_id = (SELECT id FROM team WHERE name = 'Tchequie' AND country = 'Tchequie')
WHERE team2_id = (SELECT id FROM team WHERE name = 'Tchecoslovaquie' AND country = 'Tchecoslovaquie');

-- Statuts de competition (groupes) : repris par le successeur, sauf s'il en a deja un.
UPDATE team_competition_status s SET team_id = (SELECT id FROM team WHERE name = 'Serbie' AND country = 'Serbie')
WHERE s.team_id = (SELECT id FROM team WHERE name = 'Yougoslavie' AND country = 'Yougoslavie')
  AND NOT EXISTS (SELECT 1 FROM team_competition_status o WHERE o.competition_id = s.competition_id
                  AND o.team_id = (SELECT id FROM team WHERE name = 'Serbie' AND country = 'Serbie'));
UPDATE team_competition_status s SET team_id = (SELECT id FROM team WHERE name = 'Tchequie' AND country = 'Tchequie')
WHERE s.team_id = (SELECT id FROM team WHERE name = 'Tchecoslovaquie' AND country = 'Tchecoslovaquie')
  AND NOT EXISTS (SELECT 1 FROM team_competition_status o WHERE o.competition_id = s.competition_id
                  AND o.team_id = (SELECT id FROM team WHERE name = 'Tchequie' AND country = 'Tchequie'));
DELETE FROM team_competition_status
WHERE team_id IN (SELECT id FROM team WHERE (name, country) IN (('Yougoslavie', 'Yougoslavie'), ('Tchecoslovaquie', 'Tchecoslovaquie')));

DELETE FROM team WHERE (name, country) IN (('Yougoslavie', 'Yougoslavie'), ('Tchecoslovaquie', 'Tchecoslovaquie'));

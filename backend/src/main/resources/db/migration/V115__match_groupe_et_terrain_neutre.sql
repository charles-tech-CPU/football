-- Historique des competitions de selections nationales (V116 et suivantes) :
--   group_name : groupe du match, quand une meme equipe joue plusieurs phases de groupes d'une
--     competition (qualifications en plusieurs tours) ou que la competition enchaine groupes et
--     elimination directe (phase finale). Nul = match hors groupe (tour a elimination directe) ou
--     competition dont le groupe est porte par l'equipe (team_competition_status.group_name).
--   neutral : match joue sur terrain neutre (pas d'avantage du terrain dans le classement Elo des
--     selections).
ALTER TABLE match ADD COLUMN group_name VARCHAR(60);
ALTER TABLE match ADD COLUMN neutral BOOLEAN NOT NULL DEFAULT FALSE;

-- Coupe d'Asie 2027 (Arabie saoudite) : seul le pays hote joue a domicile.
UPDATE match SET neutral = TRUE
WHERE competition_id = (SELECT id FROM competition WHERE code = 'AFC_ASIANCUP_2027')
  AND team1_id <> (SELECT id FROM team WHERE name = 'Arabie Saoudite')
  AND team2_id <> (SELECT id FROM team WHERE name = 'Arabie Saoudite');

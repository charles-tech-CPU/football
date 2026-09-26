-- Nombre de journees attendues d'un championnat (suivi de l'avancement du calendrier sur l'onglet
-- Competitions). Initialise avec la derniere journee deja saisie ; Montenegro : 10 clubs en
-- 4 tours = 36 journees, seules les 19 premieres sont saisies pour l'instant.

ALTER TABLE competition ADD COLUMN total_rounds INTEGER;

UPDATE competition c
SET total_rounds = (
    SELECT MAX(CAST(SUBSTRING(m.round_label FROM '^J(\d+)$') AS INTEGER))
    FROM match m
    WHERE m.competition_id = c.id)
WHERE c.type = 'LEAGUE';

UPDATE competition SET total_rounds = 36 WHERE type = 'LEAGUE' AND code = 'MONTENEGRO';

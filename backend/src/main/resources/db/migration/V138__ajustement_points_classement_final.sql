-- Ajustement de points a la cloture d'un championnat (sanction, correction d'une erreur de saisie) :
-- les points figes du classement final = points calcules + ajustement, avec le motif affiche.
ALTER TABLE final_standing ADD COLUMN points_adjustment INTEGER NOT NULL DEFAULT 0;
ALTER TABLE final_standing ADD COLUMN adjustment_reason VARCHAR(200);

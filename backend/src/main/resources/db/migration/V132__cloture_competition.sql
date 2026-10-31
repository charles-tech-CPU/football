-- Cloture d'une competition en fin de saison (cf. CompetitionClosureService) : date de cloture
-- (competition en lecture seule tant qu'elle n'est pas rouverte) et classement final fige, dans
-- l'ordre valide a la main (departages au tete-a-tete, mini-championnats...), pour qu'il ne bouge
-- plus meme si les regles de calcul du classement evoluent ensuite.
ALTER TABLE competition ADD COLUMN closed_at TIMESTAMP;

CREATE TABLE final_standing (
    id BIGSERIAL PRIMARY KEY,
    competition_id BIGINT NOT NULL REFERENCES competition (id) ON DELETE CASCADE,
    team_id BIGINT NOT NULL REFERENCES team (id),
    final_rank INTEGER NOT NULL,
    group_name VARCHAR(60),
    played INTEGER NOT NULL,
    won INTEGER NOT NULL,
    drawn INTEGER NOT NULL,
    lost INTEGER NOT NULL,
    goals_for INTEGER NOT NULL,
    goals_against INTEGER NOT NULL,
    points INTEGER NOT NULL,
    UNIQUE (competition_id, team_id)
);

CREATE INDEX idx_final_standing_team ON final_standing (team_id);

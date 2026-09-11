-- Schema initial : equipes, competitions, matchs.
-- Classement et tete-a-tete ne sont volontairement PAS des tables : ils
-- sont recalcules a la volee (voir StandingsService).

CREATE TABLE team (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(150) NOT NULL UNIQUE,
    country VARCHAR(60)
);

CREATE TABLE competition (
    id      BIGSERIAL PRIMARY KEY,
    code    VARCHAR(40)  NOT NULL,
    name    VARCHAR(150) NOT NULL,
    type    VARCHAR(30)  NOT NULL,
    country VARCHAR(60),
    season  INT          NOT NULL,
    CONSTRAINT uq_competition_code_season UNIQUE (code, season)
);

CREATE TABLE match (
    id              BIGSERIAL PRIMARY KEY,
    competition_id  BIGINT      NOT NULL REFERENCES competition(id) ON DELETE CASCADE,
    round_label     VARCHAR(60) NOT NULL,
    date            DATE,
    time            TIME,
    team1_id        BIGINT      NOT NULL REFERENCES team(id),
    team2_id        BIGINT      NOT NULL REFERENCES team(id),
    score1          INT,
    score2          INT,
    status          VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    CONSTRAINT chk_match_teams_distinct CHECK (team1_id <> team2_id),
    CONSTRAINT chk_match_scores_consistency CHECK (
        (score1 IS NULL AND score2 IS NULL) OR (score1 IS NOT NULL AND score2 IS NOT NULL)
    )
);

CREATE INDEX idx_match_competition ON match(competition_id);
CREATE INDEX idx_match_team1 ON match(team1_id);
CREATE INDEX idx_match_team2 ON match(team2_id);
CREATE INDEX idx_match_date ON match(date);

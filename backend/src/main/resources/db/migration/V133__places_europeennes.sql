-- Places en coupes d'Europe de la saison suivante (cf. EuropeanSlotService) : une ligne par place
-- d'un pays (competition, tour d'entree), generees a partir des points d'entree des clubs de la
-- saison en cours puis modifiables a la main (nouveau cycle UEFA...). Le club est rempli a la
-- cloture du championnat / de la coupe du pays, ou choisi a la main (manual, jamais ecrase).
CREATE TABLE european_slot (
    id BIGSERIAL PRIMARY KEY,
    season INTEGER NOT NULL,
    competition_code VARCHAR(10) NOT NULL,
    entry_stage INTEGER NOT NULL,
    country VARCHAR(60) NOT NULL,
    route VARCHAR(60),
    team_id BIGINT REFERENCES team (id) ON DELETE SET NULL,
    manual BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_european_slot_season ON european_slot (season, country);

-- Vainqueur d'une coupe nationale cloturee (champion pour un championnat) : qualification europeenne.
ALTER TABLE competition ADD COLUMN winner_team_id BIGINT REFERENCES team (id) ON DELETE SET NULL;

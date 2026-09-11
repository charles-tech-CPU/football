-- Places qualificatives europeennes par competition (LEAGUE uniquement) :
-- nombre de places attribuees par rang dans le classement, ex. Angleterre
-- 5 places LDC, 1 place EL, 1 place ECL. Statuts manuels par equipe (tenant
-- du titre, promu, vainqueur de la coupe precedente) : aucune de ces
-- informations n'est deductible des donnees importees (une seule saison),
-- donc saisie manuelle plutot que calculee.

ALTER TABLE competition ADD COLUMN ldc_slots INT NOT NULL DEFAULT 0;
ALTER TABLE competition ADD COLUMN el_slots INT NOT NULL DEFAULT 0;
ALTER TABLE competition ADD COLUMN ecl_slots INT NOT NULL DEFAULT 0;

CREATE TABLE team_competition_status (
    id                  BIGSERIAL PRIMARY KEY,
    competition_id      BIGINT  NOT NULL REFERENCES competition(id) ON DELETE CASCADE,
    team_id             BIGINT  NOT NULL REFERENCES team(id) ON DELETE CASCADE,
    defending_champion  BOOLEAN NOT NULL DEFAULT FALSE,
    promoted            BOOLEAN NOT NULL DEFAULT FALSE,
    previous_cup_winner BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_team_competition_status UNIQUE (competition_id, team_id)
);

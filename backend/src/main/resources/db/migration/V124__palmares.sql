-- Palmares historique (onglet Statistiques > Palmares) : une ligne par saison et par competition
-- (championnat, coupe nationale, coupes d'Europe), remplie par V125 (import/palmares.py, source
-- Wikipedia). Noms de clubs en texte libre (beaucoup n'existent plus), relies au club de l'appli
-- quand il existe (logo) ; pays du club pour les coupes d'Europe. Plusieurs vainqueurs possibles
-- pour une meme saison (Coupe Intertoto : un par finale).
CREATE TABLE honour (
    id BIGSERIAL PRIMARY KEY,
    scope VARCHAR(60) NOT NULL,
    kind VARCHAR(20) NOT NULL,
    competition_label VARCHAR(80) NOT NULL,
    season VARCHAR(20) NOT NULL,
    season_end INTEGER NOT NULL,
    winner VARCHAR(150) NOT NULL,
    runner_up VARCHAR(150),
    third VARCHAR(150),
    winner_country VARCHAR(60),
    runner_up_country VARCHAR(60),
    winner_team_id BIGINT REFERENCES team (id) ON DELETE SET NULL,
    runner_up_team_id BIGINT REFERENCES team (id) ON DELETE SET NULL,
    third_team_id BIGINT REFERENCES team (id) ON DELETE SET NULL
);

CREATE INDEX idx_honour_scope ON honour (scope);

-- Fusionne les fiches Team en doublon : meme club reel importe deux fois avec un nom
-- different (ligue nationale vs coupes d'Europe), cf. limitation connue README section 2.
-- Verifie au prealable (fixtures) que chaque paire ne se rencontre jamais elle-meme et
-- que la version "code pays" ne joue que LDC/EL/EC pendant que l'autre joue le championnat.

DO $$
DECLARE
    pairs CONSTANT text[][] := ARRAY[
        ['971', '456'],  -- RAPID VIENNE -> SK RAPID (Autriche)
        ['888', '413'],  -- RB SALZBOURG -> RED BULL SALZBOURG (Autriche)
        ['890', '513'],  -- SAINT TROND -> SAINT-TROND (Belgique)
        ['853', '799'],  -- OMONIA NICOSIE -> OMONIA (Chypre)
        ['947', '350'],  -- FC NORDSJAIELLAND -> NORDSJAELLAND (Danemark)
        ['831', '431'],  -- THE NEW SAINTS -> TNS (Galles)
        ['856', '780'],  -- HAPOEL BEER SHEVA -> H. BEER SHEVA (Israel)
        ['923', '31'],   -- IELIMAI SEMEI -> YELIMAY SEMEY (Kazakhstan)
        ['943', '85'],   -- AUDA FK -> AUDA (Lettonie)
        ['884', '329'],  -- FC SAINT-GALL -> ST. GALLEN (Suisse)
        ['983', '473']   -- CHAKTHAR DONETSK -> SHAKTHAR (Ukraine)
    ];
    dup_id BIGINT;
    canonical_id BIGINT;
    i INT;
BEGIN
    FOR i IN 1..array_length(pairs, 1) LOOP
        dup_id := pairs[i][1]::BIGINT;
        canonical_id := pairs[i][2]::BIGINT;

        UPDATE match SET team1_id = canonical_id WHERE team1_id = dup_id;
        UPDATE match SET team2_id = canonical_id WHERE team2_id = dup_id;

        UPDATE team_competition_status tcs
        SET team_id = canonical_id
        WHERE tcs.team_id = dup_id
          AND NOT EXISTS (
              SELECT 1 FROM team_competition_status tcs2
              WHERE tcs2.team_id = canonical_id AND tcs2.competition_id = tcs.competition_id
          );
        DELETE FROM team_competition_status WHERE team_id = dup_id;

        -- Si la fiche a garder n'a pas de logo mais le doublon en avait un, on le recupere.
        UPDATE team
        SET logo_path = (SELECT logo_path FROM team WHERE id = dup_id)
        WHERE id = canonical_id AND logo_path IS NULL
          AND (SELECT logo_path FROM team WHERE id = dup_id) IS NOT NULL;

        DELETE FROM team WHERE id = dup_id;
    END LOOP;
END $$;

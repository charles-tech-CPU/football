-- Fusionne les fiches Team en doublon (meme principe que V17) : la version "coupes d'Europe"
-- (souvent avec un pays en code court type "HOL", "ISR") est fusionnee dans la fiche du
-- championnat, dont le nom est conserve. Paires verifiees sur les donnees du serveur : les 2
-- fiches ne se sont jamais affrontees et ne jouent jamais la meme competition. Liste validee
-- par l'utilisateur (dont AC ESCALDES -> ATLETIC ESCALDES et FC STEAUA BUCAREST -> FCSB).
-- Une paire absente de la base (base locale differente du serveur) est simplement ignoree.

-- La ligne UEFA "BOHEMIANS" (Tchequie) etait rattachee aux Bohemians irlandais (fiche 7) au
-- lieu de BOHEMIANS 1905 : corrigee avant la fusion BOHEMIAN FC -> BOHEMIANS (Eire).
UPDATE club_uefa_ranking
SET team_id = (SELECT id FROM team WHERE name = 'BOHEMIANS 1905' AND country = 'Tchequie')
WHERE club_name = 'BOHEMIANS' AND country = 'Tchequie'
  AND EXISTS (SELECT 1 FROM team WHERE name = 'BOHEMIANS 1905' AND country = 'Tchequie');

DO $$
DECLARE
    pairs CONSTANT text[][] := ARRAY[
        ['967', '532'],  -- AJAX AMSTERDAM -> AJAX (Pays-Bas)
        ['914', '37'],   -- BATE BORISOV -> BATE (Belarus)
        ['976', '475'],  -- BEITAR JERUSALEM -> B. JERUSALEM (Israel)
        ['895', '7'],    -- BOHEMIAN FC -> BOHEMIANS (Eire)
        ['959', '342'],  -- DAC DUNAJSKA STREDA -> DUN. STREDA (Slovaquie)
        ['843', '731'],  -- EGANTIA RROGOZHINE -> EGNATIA (Albanie)
        ['958', '442'],  -- FC NOAH -> NOAH (Armenie)
        ['960', '320'],  -- FC SION -> SION (Suisse)
        ['854', '312'],  -- FC THOUNE -> THUN (Suisse)
        ['977', '474'],  -- HAPOEL TEL AVIV -> H. TEL AVIV (Israel)
        ['972', '474'],  -- HAPOEL TEL AVIV (nom mal encode) -> H. TEL AVIV (Israel)
        ['941', '128'],  -- HJK HELSINKI -> HJK (Finlande)
        ['836', '807'],  -- INTER CLUB D'ESCALDES -> INTER ESCALDES (Andorre)
        ['883', '718'],  -- MACCABI TEL AVIV -> M. TEL AVIV (Israel)
        ['860', '499'],  -- NEC NIMEGUE -> NIJMEGEN (Pays-Bas)
        ['964', '603'],  -- NEFTCI PFK -> NEFCI BAKU (Azerbaidjan)
        ['863', '687'],  -- OLYMPIQUE LYONNAIS -> LYON (France)
        ['965', '337'],  -- PAKSI FC -> PAKS (Hongrie)
        ['886', '719'],  -- PAOK SALONIQUE -> PAOK (Grece)
        ['930', '430'],  -- PEN-Y-BONT -> PENYBONT (Galles)
        ['830', '589'],  -- SABAH FC -> SABAH BAKU (Azerbaidjan)
        ['954', '6'],    -- SHELBOURNE FC -> SHELBOURNE (Eire)
        ['861', '516'],  -- UNION SAINT GILLOISE -> ROYALE UNION SG (Belgique)
        ['924', '94'],   -- VIKINGUR GOTA -> VIKINGUR (Iles Feroe)
        ['915', '11'],   -- ZALGIRIS VILNIUS -> ZALGIRIS (Lituanie)
        ['903', '610'],  -- ZIRA FK -> ZIRA (Azerbaidjan)
        ['911', '813'],  -- AC ESCALDES -> ATLETIC ESCALDES (Andorre)
        ['942', '221']   -- FC STEAUA BUCAREST -> FCSB (Roumanie)
    ];
    dup_id BIGINT;
    canonical_id BIGINT;
    i INT;
BEGIN
    FOR i IN 1..array_length(pairs, 1) LOOP
        dup_id := pairs[i][1]::BIGINT;
        canonical_id := pairs[i][2]::BIGINT;

        IF NOT EXISTS (SELECT 1 FROM team WHERE id = dup_id)
           OR NOT EXISTS (SELECT 1 FROM team WHERE id = canonical_id) THEN
            RAISE NOTICE 'Fusion % -> % ignoree : equipe absente', dup_id, canonical_id;
            CONTINUE;
        END IF;
        IF EXISTS (SELECT 1 FROM match
                   WHERE (team1_id = dup_id AND team2_id = canonical_id)
                      OR (team1_id = canonical_id AND team2_id = dup_id)) THEN
            RAISE NOTICE 'Fusion % -> % ignoree : les 2 equipes se sont affrontees', dup_id, canonical_id;
            CONTINUE;
        END IF;

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

        UPDATE club_uefa_ranking SET team_id = canonical_id WHERE team_id = dup_id;

        -- Si la fiche a garder n'a pas de logo mais le doublon en avait un, on le recupere.
        UPDATE team
        SET logo_path = (SELECT logo_path FROM team WHERE id = dup_id)
        WHERE id = canonical_id AND logo_path IS NULL
          AND (SELECT logo_path FROM team WHERE id = dup_id) IS NOT NULL;

        DELETE FROM team WHERE id = dup_id;
    END LOOP;
END $$;

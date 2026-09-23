-- Blasons Bulgarie recuperes via TheSportsDB.
UPDATE team SET logo_path = '243.png' WHERE id = 243; -- BOTEV VRASTA -> Botev Vratsa
UPDATE team SET logo_path = '234.png' WHERE id = 234; -- LOK. PLOVDIV -> Lokomotiv Plovdiv
UPDATE team SET logo_path = '258.png' WHERE id = 258; -- LOK. SOFIA -> Lokomotiv Sofia

-- Champion (1er, icone auto) + Conference auto sur 2e-3e (eclSlots=2), barrage 13e /
-- relegation 14e (14 equipes). Le rang 5 (2e mini-championnat) recevra en plus l'icone
-- Conference via un rankMarker dedie, cf. LEAGUE_RANK_CONFIG cote frontend.
UPDATE competition SET ldc_slots = 1, el_slots = 0, ecl_slots = 2, barrage_slots = 1, relegation_slots = 1
WHERE code = 'BULGARIE' AND season = 2027;

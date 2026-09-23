-- Blasons Danemark recuperes via TheSportsDB.
UPDATE team SET logo_path = '365.png' WHERE id = 365; -- FC COPENHAGUE
UPDATE team SET logo_path = '350.png' WHERE id = 350; -- NORDSJAELLAND
UPDATE team SET logo_path = '361.png' WHERE id = 361; -- RANDERS
UPDATE team SET logo_path = '349.png' WHERE id = 349; -- HORSENS
UPDATE team SET logo_path = '268.png' WHERE id = 268; -- ODENSE -> Odense BK

-- Format a 2 mini-championnats (comme l'Autriche) : pas de barrageSlots generique, le seul
-- barrage est une confrontation croisee 3e (groupe du haut) - 7e/1er du groupe du bas, geree
-- via des rankMarkers dedies cote frontend (LEAGUE_RANK_CONFIG).
UPDATE competition SET ldc_slots = 0, el_slots = 0, ecl_slots = 0, barrage_slots = 0, relegation_slots = 0
WHERE code = 'DANEMARK' AND season = 2027;

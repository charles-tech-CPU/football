-- Blasons Chypre recuperes via TheSportsDB. OMONIA 29TH MAY introuvable (club trop obscur).
UPDATE team SET logo_path = '799.png' WHERE id = 799; -- OMONIA -> Omonia Nicosia
UPDATE team SET logo_path = '776.png' WHERE id = 776; -- APOEL -> APOEL Nicosia
UPDATE team SET logo_path = '747.png' WHERE id = 747; -- OL. NICOSIA -> Olympiakos Nicosia

-- Format identique a l'Autriche (2 mini-championnats), mais asymetrique : 6 premiers
-- (Championship group) / 8 derniers (play-out group), cf. LEAGUE_RANK_CONFIG frontend.
UPDATE competition SET ldc_slots = 0, el_slots = 0, ecl_slots = 0, barrage_slots = 0, relegation_slots = 0
WHERE code = 'CHYPRE' AND season = 2027;

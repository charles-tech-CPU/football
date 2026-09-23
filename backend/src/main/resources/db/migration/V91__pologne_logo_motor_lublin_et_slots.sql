-- Blason Motor Lublin recupere via TheSportsDB (match exact, meme source que V5).
UPDATE team SET logo_path = '346.png' WHERE id = 346; -- MOTOR LUBIN -> Motor Lublin

-- Icones : champion (1er), LDC (2e), Conference League (3e-4e, pas d'Europa League cette
-- saison), relegation directe (16e-18e, pas de barrage).
UPDATE competition SET el_slots = 0, ecl_slots = 2, relegation_slots = 3, barrage_slots = 0
WHERE code = 'POLOGNE' AND season = 2027;

-- Blason Macedoine recupere via TheSportsDB.
UPDATE team SET logo_path = '902.png' WHERE id = 902; -- FK SILEKS KRATOVO -> Sileks

-- Icones : 10e relegue, 9e et 8e barragistes.
UPDATE competition SET barrage_slots = 2, relegation_slots = 1 WHERE code = 'MACEDOINE' AND season = 2027;

-- L'unique place europeenne restante (au-dela du champion) se joue dans un tournoi a
-- elimination directe entre le 2e et le 11e, pas par attribution directe de rang :
-- ecl_slots (qui marquait a tort le 2e/3e comme qualifies Conference) remis a 0, remplace
-- par les rankMarkers dedies (quarts/huitiemes) cote frontend.
UPDATE competition SET ecl_slots = 0 WHERE code = 'SAN MARIN' AND season = 2027;

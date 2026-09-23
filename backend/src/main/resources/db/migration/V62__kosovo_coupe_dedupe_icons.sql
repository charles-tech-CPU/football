-- Les quarts de finale etaient inseres en double (8 places generiques au lieu de 4, l'un des
-- 2 jeux sans date) : une fois les equipes tirees au sort, les 2 lots auraient fusionne par
-- paire d'equipes en une confrontation aller-retour au lieu d'un match sec comme les autres
-- tours (huitiemes/finale). Supprime le lot sans date (les 4 avec la date du 04/03 restent).
DELETE FROM match WHERE id IN (12319, 12320, 12321, 12322);

-- Icones : 1er champion, 8e barragiste, 9e et 10e relegues. La 2e phase (aller-retour, apres
-- le simple tour initial de 9 journees) s'affichera automatiquement dans l'onglet resultats
-- au fur et a mesure des matchs saisis (meme mecanisme generique que l'Albanie, pas besoin de
-- config dediee tant qu'il n'y a pas de scission en mini-championnats separes).
UPDATE competition SET barrage_slots = 1, relegation_slots = 2 WHERE code = 'KOSOVO' AND season = 2027;

-- Scission top 6 / bottom 6 geree cote frontend (resultsGroupSplit) : places qualificatives
-- reelles portees par les groupSlots (1er champion, 2e Europa League, 3e-4e Conference
-- League), pas par ces colonnes generiques - mises a jour quand meme pour rester coherentes
-- (eclSlots 1 -> 2 pour couvrir le 3e ET le 4e).
UPDATE competition SET ecl_slots = 2 WHERE code = 'SUISSE' AND season = 2027;

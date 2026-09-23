-- Scission top 6 / 8 derniers geree cote frontend (resultsGroupSplit) : les places
-- qualificatives reelles sont portees par les groupSlots (1er champion, 2e Europa League,
-- 3e-4e Conference League), pas par ces colonnes generiques - mises a jour quand meme pour
-- rester coherentes avec le nouveau format (eclSlots 1 -> 2 pour couvrir le 3e ET le 4e).
UPDATE competition SET ecl_slots = 2 WHERE code = 'SERBIE' AND season = 2027;

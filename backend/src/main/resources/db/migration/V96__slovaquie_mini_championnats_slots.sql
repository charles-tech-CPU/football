-- Scission top 6 / bottom 6 geree cote frontend (resultsGroupSplit) : places qualificatives
-- reelles portees par les groupSlots (1er champion, 2e-3e Conference League), pas par ces
-- colonnes generiques - mises a jour quand meme pour rester coherentes (elSlots 1 -> 0,
-- eclSlots 1 -> 2).
UPDATE competition SET el_slots = 0, ecl_slots = 2 WHERE code = 'SLOVAQUIE' AND season = 2027;

-- Scission a 3 groupes geree cote frontend (resultsGroupSplit) : places qualificatives
-- reelles portees par les groupSlots (groupe du haut 1er champion/LDC, 2e LDC, 3e Europa,
-- 4e Conference ; groupe du milieu 7e-10e barrage europeen sans place directe ; groupe du
-- bas 14e-15e barragistes maintien, 16e relegue), pas par ces colonnes generiques - mises a
-- jour quand meme pour rester coherentes (elSlots 2 -> 1, barrageSlots 0 -> 2).
UPDATE competition SET el_slots = 1, barrage_slots = 2 WHERE code = 'TCHEQUIE' AND season = 2027;

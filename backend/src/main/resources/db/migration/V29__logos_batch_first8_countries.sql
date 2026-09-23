-- Blasons recuperes via TheSportsDB pour les clubs sans logo des 8 premiers pays
-- (ordre alphabetique) : Andorre, Armenie, Autriche, Azerbaidjan, Belarus. Matching exact
-- verifie manuellement (ligue nationale confirmee pour chaque club).
UPDATE team SET logo_path = '813.png' WHERE id = 813; -- ATLETIC ESCALDES -> Atlètic d'Escaldes
UPDATE team SET logo_path = '806.png' WHERE id = 806; -- ESPERANCA -> Esperança d'Andorra
UPDATE team SET logo_path = '807.png' WHERE id = 807; -- INTER ESCALDES -> Inter Club d'Escaldes
UPDATE team SET logo_path = '810.png' WHERE id = 810; -- SPORTING ESCALDES -> Sporting d'Escaldes

UPDATE team SET logo_path = '405.png' WHERE id = 405; -- ARARAT-ARMENIA

UPDATE team SET logo_path = '444.png' WHERE id = 444; -- A. LUSTENAU -> Austria Lustenau
UPDATE team SET logo_path = '447.png' WHERE id = 447; -- AUSTRIA VIENNE -> Austria Vienna
UPDATE team SET logo_path = '413.png' WHERE id = 413; -- RED BULL SALZBOURG
UPDATE team SET logo_path = '456.png' WHERE id = 456; -- SK RAPID -> Rapid Vienna

UPDATE team SET logo_path = '580.png' WHERE id = 580; -- ARAZ -> Araz Saatlı
UPDATE team SET logo_path = '563.png' WHERE id = 563; -- GABALA -> Qəbələ
UPDATE team SET logo_path = '590.png' WHERE id = 590; -- KAPAZ -> Kəpəz
UPDATE team SET logo_path = '603.png' WHERE id = 603; -- NEFCI BAKU -> Neftçi PFK
UPDATE team SET logo_path = '869.png' WHERE id = 869; -- QARABAG FK -> Qarabağ
UPDATE team SET logo_path = '624.png' WHERE id = 624; -- SHAMAKI -> Şamaxı
UPDATE team SET logo_path = '610.png' WHERE id = 610; -- ZIRA -> Zirə

UPDATE team SET logo_path = '121.png' WHERE id = 121; -- NEMAN -> Neman Grodno
UPDATE team SET logo_path = '36.png' WHERE id = 36;   -- ZHODINO -> Torpedo-BelAZ Zhodino

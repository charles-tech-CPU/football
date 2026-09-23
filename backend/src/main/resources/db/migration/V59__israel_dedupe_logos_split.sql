-- H. TEL AVIV - B. JERUSALEM etait saisi 2 fois en J1 (03/08 et 03/09), donnant 27 matchs aux
-- 2 equipes au lieu de 26 et une fausse "2e phase" (paire rejouee en apparence). Confirme
-- avec l'utilisateur : le match du 03/09 (3-0) est le bon, celui du 03/08 est supprime.
DELETE FROM match WHERE id = 1733;

-- Blasons Israel recuperes via TheSportsDB.
UPDATE team SET logo_path = '474.png' WHERE id = 474; -- H. TEL AVIV -> Hapoel Tel-Aviv
UPDATE team SET logo_path = '718.png' WHERE id = 718; -- M. TEL AVIV -> Maccabi Tel Aviv
UPDATE team SET logo_path = '676.png' WHERE id = 676; -- M. PETACH TIKVA -> Maccabi Petah Tikva
UPDATE team SET logo_path = '680.png' WHERE id = 680; -- M. HAIFA -> Maccabi Haifa
UPDATE team SET logo_path = '780.png' WHERE id = 780; -- H. BEER SHEVA -> Hapoel Be'er Sheva
UPDATE team SET logo_path = '795.png' WHERE id = 795; -- H. HAIFA -> Hapoel Haifa
UPDATE team SET logo_path = '675.png' WHERE id = 675; -- H. PETAH TIKVA -> Hapoel Petah Tikva
UPDATE team SET logo_path = '681.png' WHERE id = 681; -- HAP. RAMAT GAN -> Hapoel Ramat Gan

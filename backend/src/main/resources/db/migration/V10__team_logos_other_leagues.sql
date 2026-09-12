-- Blasons des championnats hors "top 5" (jamais couverts jusqu'ici, cf.
-- V5__team_logo.sql), recuperes automatiquement via l'API gratuite
-- TheSportsDB avec matching strict sur le nom (voir import/fetch_logos.py) :
-- seules les equipes reconnues avec certitude ont un logo, le reste sera
-- complete par des migrations suivantes au fur et a mesure des lots traites.
UPDATE team SET logo_path = '897.png' WHERE id = 897; -- DINAMO TIRANA (ALB) -> Dinamo City
UPDATE team SET logo_path = '936.png' WHERE id = 936; -- FK VLLAZNIA SHKODER (ALB) -> Vllaznia Shkodër
UPDATE team SET logo_path = '731.png' WHERE id = 731; -- EGNATIA (Albanie) -> Egnatia
UPDATE team SET logo_path = '653.png' WHERE id = 653; -- LACI (Albanie) -> Laçi
UPDATE team SET logo_path = '656.png' WHERE id = 656; -- TEUTA (Albanie) -> Teuta Durrës
UPDATE team SET logo_path = '732.png' WHERE id = 732; -- TIRANA (Albanie) -> Tirana
UPDATE team SET logo_path = '730.png' WHERE id = 730; -- VORA (Albanie) -> Vora
UPDATE team SET logo_path = '836.png' WHERE id = 836; -- INTER CLUB D'ESCALDES (AND) -> Inter Club d'Escaldes
UPDATE team SET logo_path = '804.png' WHERE id = 804; -- CARROI (Andorre) -> Carroi
UPDATE team SET logo_path = '808.png' WHERE id = 808; -- CASA DE Portugal (Andorre) -> Casa de Portugal
UPDATE team SET logo_path = '451.png' WHERE id = 451; -- GORI (Georgie) -> Gori
UPDATE team SET logo_path = '752.png' WHERE id = 752; -- MONS CALPE (Gibraltar) -> Mons Calpe
UPDATE team SET logo_path = '673.png' WHERE id = 673; -- ARIS (Grece) -> Aris
UPDATE team SET logo_path = '697.png' WHERE id = 697; -- ATROMITOS (Grece) -> Atromitos
UPDATE team SET logo_path = '671.png' WHERE id = 671; -- IRAKLIS 1908 (Grece) -> Iraklis 1908
UPDATE team SET logo_path = '672.png' WHERE id = 672; -- KALAMATA (Grece) -> Kalamata
UPDATE team SET logo_path = '720.png' WHERE id = 720; -- LEVADIAKOS (Grece) -> Levadiakos
UPDATE team SET logo_path = '715.png' WHERE id = 715; -- OFI CRETE (Grece) -> OFI
UPDATE team SET logo_path = '798.png' WHERE id = 798; -- PANATHINAIKOS (Grece) -> Panathinaikos
UPDATE team SET logo_path = '721.png' WHERE id = 721; -- PANETOLIKOS (Grece) -> Panetolikos
UPDATE team SET logo_path = '719.png' WHERE id = 719; -- PAOK (Grece) -> PAOK
UPDATE team SET logo_path = '716.png' WHERE id = 716; -- VOLOS (Grece) -> Volos
UPDATE team SET logo_path = '967.png' WHERE id = 967; -- AJAX AMSTERDAM (HOL) -> Ajax
UPDATE team SET logo_path = '879.png' WHERE id = 879; -- FC TWENTE (HOL) -> Twente
UPDATE team SET logo_path = '946.png' WHERE id = 946; -- DEBRECENI VSC (HON) -> Debreceni VSC
UPDATE team SET logo_path = '875.png' WHERE id = 875; -- FERENCVAROS TC (HON) -> Ferencváros
UPDATE team SET logo_path = '289.png' WHERE id = 289; -- HONVED (Hongrie) -> Budapest Honvéd
UPDATE team SET logo_path = '288.png' WHERE id = 288; -- KISVARDA (Hongrie) -> Kisvárda
UPDATE team SET logo_path = '367.png' WHERE id = 367; -- MTK BUDAPEST (Hongrie) -> MTK Budapest
UPDATE team SET logo_path = '263.png' WHERE id = 263; -- NYIREGYHAZA (Hongrie) -> Nyíregyháza
UPDATE team SET logo_path = '337.png' WHERE id = 337; -- PAKS (Hongrie) -> Paks
UPDATE team SET logo_path = '180.png' WHERE id = 180; -- KLAIPEDOS FM (Lituanie) -> Klaipėdos FM
UPDATE team SET logo_path = '556.png' WHERE id = 556; -- MOREIRENSE (Portugal) -> Moreirense
UPDATE team SET logo_path = '559.png' WHERE id = 559; -- NACIONAL (Portugal) -> Nacional
UPDATE team SET logo_path = '555.png' WHERE id = 555; -- RIO AVE (Portugal) -> Rio Ave
UPDATE team SET logo_path = '558.png' WHERE id = 558; -- SANTA CLARA (Portugal) -> Santa Clara
UPDATE team SET logo_path = '868.png' WHERE id = 868; -- UNIVERSITATEA CLUJ (ROU) -> Universitatea Cluj
UPDATE team SET logo_path = '841.png' WHERE id = 841; -- UNIVERSITATEA CRAIOVA (ROU) -> Universitatea Craiova
UPDATE team SET logo_path = '208.png' WHERE id = 208; -- BOTOSANI (Roumanie) -> Botoșani
UPDATE team SET logo_path = '224.png' WHERE id = 224; -- CFR CLUJ (Roumanie) -> CFR Cluj
UPDATE team SET logo_path = '240.png' WHERE id = 240; -- FARUL CONSTANTA (Roumanie) -> Farul Constanța
UPDATE team SET logo_path = '207.png' WHERE id = 207; -- FC VOLUNTARI (Roumanie) -> Voluntari
UPDATE team SET logo_path = '221.png' WHERE id = 221; -- FCSB (Roumanie) -> FCSB
UPDATE team SET logo_path = '238.png' WHERE id = 238; -- UTA ARAD (Roumanie) -> UTA Arad
UPDATE team SET logo_path = '326.png' WHERE id = 326; -- AKHMAT GROZNY (Russie) -> Akhmat Grozny
UPDATE team SET logo_path = '316.png' WHERE id = 316; -- FK ROSTOV (Russie) -> Rostov
UPDATE team SET logo_path = '352.png' WHERE id = 352; -- KRASNODAR (Russie) -> Krasnodar
UPDATE team SET logo_path = '315.png' WHERE id = 315; -- ORENBURG (Russie) -> Orenburg

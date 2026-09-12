-- Le fichier Excel source marque 45 matchs "REPORTE" (score et parfois date),
-- un marqueur que import_excel.py ne reconnaissait pas : il traitait S1/S2 non
-- numeriques comme un score absent (-> SCHEDULED au lieu de POSTPONED), et
-- rejetait purement et simplement toute ligne dont la date valait "REPORTE"
-- (le parseur exige une date exploitable). Consequence : 17 matchs reportes
-- etaient importes comme des matchs a venir ordinaires, et 28 autres
-- n'etaient jamais importes du tout.
--
-- Pour les 28 jamais importes, quelques equipes n'existaient que sous leur
-- nom "long" issu des feuilles de coupes d'Europe (ex: "BRAGA" -> "SPORTING
-- BRAGA", "RIJEKA" -> "HNK RIJEKA") : reutilisees telles quelles plutot que
-- de creer un doublon (cf. limite documentee dans import_excel.py).

-- 17 matchs deja importes : correction du statut SCHEDULED -> POSTPONED
UPDATE match SET status = 'POSTPONED' WHERE id = 3680; -- LETTONIE J24 SUPER NOVA - RIGA FC
UPDATE match SET status = 'POSTPONED' WHERE id = 3681; -- POLOGNE J3 RAKOW CZESTOCHOWA - ZAGLEBIE
UPDATE match SET status = 'POSTPONED' WHERE id = 3689; -- POLOGNE J2 KORONA - GORNIK ZABRZE
UPDATE match SET status = 'POSTPONED' WHERE id = 3708; -- SUISSE J4 THUN - SERVETTE
UPDATE match SET status = 'POSTPONED' WHERE id = 3709; -- BOSNIE J2 SLOGA MERIDIAN - FK BORAC BANJA LUKA
UPDATE match SET status = 'POSTPONED' WHERE id = 3712; -- LITUANIE J25 ZALGIRIS - KAUNO ZALGIRIS
UPDATE match SET status = 'POSTPONED' WHERE id = 3714; -- LETTONIE J25 AUDA - OGRE UNITED
UPDATE match SET status = 'POSTPONED' WHERE id = 3716; -- BULGARIE J6 LOK. SOFIA - CSKA SOFIA
UPDATE match SET status = 'POSTPONED' WHERE id = 3738; -- GEORGIE J20 FC IBERIA 1999 - DILA GORI
UPDATE match SET status = 'POSTPONED' WHERE id = 4084; -- EIRE J11 GALWAY - SHELBOURNE
UPDATE match SET status = 'POSTPONED' WHERE id = 4468; -- POLOGNE J3 GKS KATOWICE - WIECZYSTA KRAKOW
UPDATE match SET status = 'POSTPONED' WHERE id = 4475; -- KAZAKHSTAN J18 ERTIS PAVLODAR - FC ASTANA
UPDATE match SET status = 'POSTPONED' WHERE id = 4480; -- TCHEQUIE J6 BOHEMIANS - MLADA BOLESLAV
UPDATE match SET status = 'POSTPONED' WHERE id = 4481; -- LETTONIE J25 RFS - SUPER NOVA
UPDATE match SET status = 'POSTPONED' WHERE id = 4521; -- KAZAKHSTAN J23 KAIRAT ALMATY - ERTIS PAVLODAR
UPDATE match SET status = 'POSTPONED' WHERE id = 4691; -- KAZAKHSTAN J23 OKZHETPES - KYZYLZHAR
UPDATE match SET status = 'POSTPONED' WHERE id = 6466; -- UKRAINE J1 DYN. KYIV - LIVYI BEREG

-- 28 matchs jamais importes (date Excel = 'REPORTE') : creation directe en
-- POSTPONED, sans date/heure/score connus.
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'UKRAINE' AND season = 2027), 'J2', NULL, NULL, 419, 372, NULL, NULL, 'POSTPONED'); -- CH. ODESA - KOLOS KOVALIVKA
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'BELARUS' AND season = 2027), 'J17', NULL, NULL, 122, 901, NULL, NULL, 'POSTPONED'); -- BELSHINA - DINAMO MINSK
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'BELARUS' AND season = 2027), 'J17', NULL, NULL, 56, 72, NULL, NULL, 'POSTPONED'); -- ISLOCH - ML VITEBSK
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'KAZAKHSTAN' AND season = 2027), 'J23', NULL, NULL, 39, 66, NULL, NULL, 'POSTPONED'); -- ALTAI - ATYRAU
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'KAZAKHSTAN' AND season = 2027), 'J23', NULL, NULL, 962, 74, NULL, NULL, 'POSTPONED'); -- TOBOL KOSTANAI - ZHENIS
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'KAZAKHSTAN' AND season = 2027), 'J17', NULL, NULL, 31, 66, NULL, NULL, 'POSTPONED'); -- YELIMAY SEMEY - ATYRAU
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'IRLANDE NORD' AND season = 2027), 'J4', NULL, NULL, 498, 838, NULL, NULL, 'POSTPONED'); -- BALLYMENA - LARNE FC
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'KAZAKHSTAN' AND season = 2027), 'J23', NULL, NULL, 83, 65, NULL, NULL, 'POSTPONED'); -- FC ASTANA - KASPIJ AKTAU
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'KAZAKHSTAN' AND season = 2027), 'J23', NULL, NULL, 31, 67, NULL, NULL, 'POSTPONED'); -- YELIMAY SEMEY - ULYTAU
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'IRLANDE NORD' AND season = 2027), 'J3', NULL, NULL, 838, 496, NULL, NULL, 'POSTPONED'); -- LARNE FC - LIMAVADY
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'KAZAKHSTAN' AND season = 2027), 'J23', NULL, NULL, 43, 52, NULL, NULL, 'POSTPONED'); -- AKTOBE - ORDABASY
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'AZERBAIDJAN' AND season = 2027), 'J2', NULL, NULL, 869, 590, NULL, NULL, 'POSTPONED'); -- QARABAG FK - KAPAZ
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'AZERBAIDJAN' AND season = 2027), 'J2', NULL, NULL, 589, 602, NULL, NULL, 'POSTPONED'); -- SABAH BAKU - SUMQAYIT
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'SERBIE' AND season = 2027), 'J4', NULL, NULL, 216, 232, NULL, NULL, 'POSTPONED'); -- RADNICKI NIS - PARTIZAN BELGRADE
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'CROATIE' AND season = 2027), 'J2', NULL, NULL, 938, 855, NULL, NULL, 'POSTPONED'); -- HNK RIJEKA - DINAMO ZAGREB
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'POLOGNE' AND season = 2027), 'J4', NULL, NULL, 887, 271, NULL, NULL, 'POSTPONED'); -- JAGIELLONIA BIALYSTOK - POGON SZCZECIN
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'SLOVAQUIE' AND season = 2027), 'J3', NULL, NULL, 344, 342, NULL, NULL, 'POSTPONED'); -- KOSICE - DUN. STREDA
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'SERBIE' AND season = 2027), 'J5', NULL, NULL, 215, 213, NULL, NULL, 'POSTPONED'); -- ZELEZNICAR PANCEVO - ETOILE ROUGE BELGRADE
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'SLOVENIE' AND season = 2027), 'J6', NULL, NULL, 857, 227, NULL, NULL, 'POSTPONED'); -- NK CELJE - RADOMLJE
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'POLOGNE' AND season = 2027), 'J4', NULL, NULL, 322, 852, NULL, NULL, 'POSTPONED'); -- PLOCK - LECH POZNAN
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'SLOVAQUIE' AND season = 2027), 'J3', NULL, NULL, 301, 354, NULL, NULL, 'POSTPONED'); -- TRENCIN - SLOVAN BRATISLAVA
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'EIRE' AND season = 2027), 'J28', NULL, NULL, 10, 6, NULL, NULL, 'POSTPONED'); -- SHAMROCK ROVERS - SHELBOURNE
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'PORTUGAL' AND season = 2027), 'J2', NULL, NULL, 966, 554, NULL, NULL, 'POSTPONED'); -- SPORTING BRAGA - GIL VICENTE
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'BELARUS' AND season = 2027), 'J16', NULL, NULL, 37, 121, NULL, NULL, 'POSTPONED'); -- BATE - NEMAN
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'BELARUS' AND season = 2027), 'J16', NULL, NULL, 901, 81, NULL, NULL, 'POSTPONED'); -- DINAMO MINSK - SLAVIA MOZYR
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'BELARUS' AND season = 2027), 'J16', NULL, NULL, 72, 36, NULL, NULL, 'POSTPONED'); -- ML VITEBSK - ZHODINO
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'SERBIE' AND season = 2027), 'J2', NULL, NULL, 230, 215, NULL, NULL, 'POSTPONED'); -- RADNICKI 1923 - ZELEZNICAR PANCEVO
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ((SELECT id FROM competition WHERE code = 'KOSOVO' AND season = 2027), 'J2', NULL, NULL, 840, 612, NULL, NULL, 'POSTPONED'); -- KF DRITA - PRISHTINA

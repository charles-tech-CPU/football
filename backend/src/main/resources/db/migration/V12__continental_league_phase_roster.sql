-- Roster (36 clubs) de la phase de ligue LDC/EL/EC, extrait de la section
-- 'GROUPE' de chaque feuille Excel (voir import/extract_group_roster.py).
-- Sert a limiter la liste d'equipes proposee lors de l'ajout d'un match de
-- phase de ligue aux 36 clubs reellement engages, plutot qu'a toutes les
-- equipes ayant simplement joue un tour de qualification de cette coupe.

INSERT INTO team (name, country) VALUES ('TORREENSE', 'Portugal');

INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'PSG')); -- PARIS SAINT GERMAIN
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'BAYERN MUNICH')); -- BAYERN MUNICH
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'REAL MADRID')); -- REAL MADRID
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'LIVERPOOL')); -- LIVERPOOL FC
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'INTER')); -- INTER MILAN
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'MANCHESTER CITY')); -- MANCHESTER CITY
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'ARSENAL')); -- ARSENAL FC
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'BARCELONE')); -- FC BARCELONE
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'ATL. MADRID')); -- ATLETICO DE MADRID
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'DORTMUND')); -- BORUSSIA DORTMUND
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'AS ROME')); -- AS ROME
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'SPORTING')); -- SPORTING CP
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'ASTON VILLA')); -- ASTON VILLA
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'FC PORTO')); -- FC PORTO
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'MANCHESTER UTD')); -- MANCHESTER UNITED
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'CLUB BRUGGE')); -- CLUB BRUGES
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'BETIS')); -- REAL BETIS
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'PSV')); -- PSV EINDHOVEN
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'FEYENOORD')); -- FEYENOORD ROTTERDAM
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'LILLE')); -- LILLE OSC
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'FK BODO GLIMT')); -- BODO GLIMT
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'NAPLES')); -- SSC NAPLES
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'RB LEIPZIG')); -- RB LEIPZIG
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'VILLAREAL')); -- VILLAREAL CF
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'FENERBAHCE SK')); -- FENERBAHCE SK
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'CHAKTHAR DONETSK')); -- CHAKTHAR DONETSK
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'GALATASARAY')); -- GALATASARAY
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'SLAVIA PRAGUE')); -- SLAVIA PRAGUE
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'STUTTGART')); -- VFB STUTTGART
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'AEK ATHENES')); -- AEK ATHENES
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'SLOVAN BRATISLAVA')); -- SLOVAN BRATISLAVA
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'LASK')); -- LASK
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'COMO')); -- COME 1907
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'LENS')); -- RC LENS
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'VIKING FK')); -- VIKING FK
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'LDC' AND season = 2027), (SELECT id FROM team WHERE name = 'SABAH FC')); -- SABAH FC

INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'BAYER LEVERKUSEN')); -- BAYER LEVERKUSEN
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'SL BENFICA')); -- SL BENFICA
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'JUVENTUS')); -- JUVENTUS TURIN
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'AC MILAN')); -- AC MILAN
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'OLYMPIQUE LYONNAIS')); -- OLYMPIQUE LYONNAIS
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'ALKMAAR')); -- AZ ALKMAAR
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'OLYMPIAKOS')); -- OLYMPIAKOS
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'REAL SOCIEDAD')); -- REAL SOCIEDAD
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'MARSEILLE')); -- OLYMPIQUE DE MARSEILLE
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'FERENCVAROS TC')); -- FERENCVATOS
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'VIKTORIA PLZEN')); -- VIKTORIA PLZEN
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'UNION SAINT GILLOISE')); -- UNION SAINT GILLOISE
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'DINAMO ZAGREB')); -- DINAMO ZAGREB
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'RB SALZBOURG')); -- RB SALZBOURG
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'CELTIC GLASGOW')); -- CELTIC
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'SPARTA PRAGUE')); -- SPARTA PRAGUE
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'RENNES')); -- STADE RENNAIS
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'RSC ANDERLECHT')); -- RSC ANDERLECHT
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'STURM GRAZ')); -- STURM GRAZ
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'LECH POZNAN')); -- LECH POZNAN
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'CRYSTAL PALACE')); -- CRYSTAL PALACE
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'BOURNEMOUTH')); -- BOURNEMOUTH
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'SUNDERLAND')); -- SUNDERLAND
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'NK CELJE')); -- CELJE
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'JAGIELLONIA BIALYSTOK')); -- JAGIELLONIA BIALYSTOK
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'OMONIA NICOSIE')); -- OMONIA NICOSIE
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'CELTA VIGO')); -- CELTA VIGO
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'HOFFENHEIM')); -- HOFFENHEIM
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'BESIKTAS JK')); -- BESIKTAS
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'TORREENSE')); -- TORREENSE
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'HAPOEL BEER SHEVA')); -- HAPOEL BEER SHEVA
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'NEC NIMEGUE')); -- NEC NIMEGUE
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'OFI CRETE')); -- OFI CRETE
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'LILLESTROM')); -- LILLESTROM
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'LEVSKI SOFIA')); -- LEVSKI SOFIA
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EL' AND season = 2027), (SELECT id FROM team WHERE name = 'ARARAT-ARMENIA')); -- ARARAT ARMENIA

INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'ATALANTA BERGAME')); -- ATALANTA BERGAME
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'SPORTING BRAGA')); -- SPORTING BRAGA
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'AJAX AMSTERDAM')); -- AJAX AMSTERDAM
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'SC FRIBOURG')); -- SC FRIBOURG
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'AS MONACO')); -- AS MONACO
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'FC COPENHAGUE')); -- FC COPENHAGUE
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'FC MIDTJYLLAND')); -- FC MIDTJYLLAND
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'ETOILE ROUGE BELGRADE')); -- ETOILE ROUGE BELGRADE
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'KAA LA GANTOISE')); -- KAA LA GANTOISE
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'PANATHINAIKOS')); -- PANATHINAIKOS
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'FC PAFOS')); -- PAFOS FC
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'BRIGHTON &HOVE ALBION')); -- BRIGHTON & HOVE ALBION
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'LUGANO')); -- LUGANO
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'GETAFE')); -- GETAFE
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'KUPS')); -- KUPS
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'FC TWENTE')); -- TWENTE
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'LINCOLN RED IMPS')); -- LINCOLN RED IMPS
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'FK BORAC BANJA LUKA')); -- BORAC BANJA LUKA
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'SAINT TROND')); -- SAINT TROND
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'SK BRANN')); -- BRANN
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'HEARTS OF MIDDLOTHIAN')); -- HEART OF MIDLOTHIAN
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'KAIRAT ALMATY')); -- KAIRAT ALMATY
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'TRABZONSPOR')); -- TRABZONSPOR
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'UNIVERSITATEA CRAIOVA')); -- UNIVERSITATEA CRAIOVA
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'RIGA FC')); -- RIGA FC
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'HAJDUK SPLIT')); -- HAJDUK SPLIT
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'JABLONEC')); -- JABLONEC
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'NORDSJAELLAND')); -- NORDSJAEELAND
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'AGF AARHUS')); -- AGF AARHUS
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'INTER CLUB D''ESCALDES')); -- INTER CLUB D'ESCALDES
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'FC THOUNE')); -- FC THOUNE
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'CSKA SOFIA')); -- CSKA SOFIA
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'KAUNO ZALGIRIS')); -- KAUNO ZALGIRIS
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'MJALLBY AIF')); -- MJALLBY
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'FC IBERIA 1999')); -- IBERIA 1999
INSERT INTO team_competition_status (competition_id, team_id) VALUES ((SELECT id FROM competition WHERE code = 'EC' AND season = 2027), (SELECT id FROM team WHERE name = 'EGANTIA RROGOZHINE')); -- EGNATIA RROGOZHINE

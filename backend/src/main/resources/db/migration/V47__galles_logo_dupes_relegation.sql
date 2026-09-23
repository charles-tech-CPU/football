-- Blason The New Saints (TNS) recupere via TheSportsDB.
UPDATE team SET logo_path = '431.png' WHERE id = 431; -- TNS -> The New Saints

-- CARDIFF METROPOLITAN avait 2 matchs de trop (32 au lieu de 30) et CAERNARFON TOWN 2 de
-- moins (28) : 2 matchs ou CAERNARFON TOWN aurait du jouer ont ete saisis avec CARDIFF
-- METROPOLITAN a la place (confirme par le doublon exact de paire ordonnee et par les
-- matchs retour CAERNARFON TOWN-BRITON FERRY en J20 et CAERNARFON TOWN-AMMANFORD en J26
-- deja presents sous leur vrai nom).
UPDATE match SET team1_id = 918 WHERE id = 4830; -- J14 CARDIFF METROPOLITAN - BRITON FERRY -> CAERNARFON TOWN - BRITON FERRY
UPDATE match SET team2_id = 918 WHERE id = 3364; -- J8 AMMANFORD - CARDIFF METROPOLITAN -> AMMANFORD - CAERNARFON TOWN (score conserve)

-- Icones : 15e et 16e relegues, pas de 2e phase (championnat simple en aller-retour a 16 equipes).
UPDATE competition SET relegation_slots = 2 WHERE code = 'GALLES' AND season = 2027;

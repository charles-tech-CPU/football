-- "SJ" (828) est en realite SHAKTHAR (473), tronque sur 1 de ses 30 matchs (confirme :
-- SHAKTHAR-LNZ CHERKAZY n'avait qu'1 confrontation au lieu de 2, et SHAKTHAR n'avait que 29
-- matchs au lieu de 30).
UPDATE match SET team2_id = 473 WHERE id = 9749;
DELETE FROM team WHERE id = 828;

-- KRYVBAS avait 1 match de trop contre DYN. KYIV (3 confrontations au lieu de 2) alors qu'il
-- manquait le match retour de KUDRIVKA-DYN. KYIV (1 seule confrontation au lieu de 2) :
-- reassigne le doublon (score conserve, tous 2 encore programmes sans score).
UPDATE match SET team1_id = (SELECT id FROM team WHERE name = 'KUDRIVKA') WHERE id = 10232;

-- Blasons Ukraine recuperes via TheSportsDB.
UPDATE team SET logo_path = '419.png' WHERE id = 419; -- CH. ODESA -> Chornomorets Odesa
UPDATE team SET logo_path = '540.png' WHERE id = 540; -- DYN. KYIV -> Dynamo Kyiv
UPDATE team SET logo_path = '469.png' WHERE id = 469; -- LNZ CHERKAZY
UPDATE team SET logo_path = '473.png' WHERE id = 473; -- SHAKTHAR -> Shakhtar Donetsk
UPDATE team SET logo_path = '390.png' WHERE id = 390; -- VERES-RIVNE

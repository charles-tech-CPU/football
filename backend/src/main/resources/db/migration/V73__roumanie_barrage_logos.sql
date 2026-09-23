-- Blasons Roumanie recuperes via TheSportsDB.
UPDATE team SET logo_path = '254.png' WHERE id = 254; -- CSIKSZEREDA M. CIUC -> Csikszereda Miercurea Ciuc
UPDATE team SET logo_path = '246.png' WHERE id = 246; -- D. BUCAREST -> Dinamo Bucuresti
UPDATE team SET logo_path = '259.png' WHERE id = 259; -- RAPID BUCAREST -> Rapid Bucuresti
UPDATE team SET logo_path = '260.png' WHERE id = 260; -- SEPSI SF. GHEORGHE -> Sepsi OSK

-- Icones : barrage de maintien deja en place pour le 13e et le 14e (aller-retour contre le
-- 4e/3e de Division 2). Le barrage europeen (7e-8e puis vainqueur contre le 4e) est une place
-- europeenne supplementaire, distincte du maintien - icone dediee (rankMarkers) plutot que le
-- barrage automatique.
UPDATE competition SET barrage_slots = 2 WHERE code = 'ROUMANIE' AND season = 2027;

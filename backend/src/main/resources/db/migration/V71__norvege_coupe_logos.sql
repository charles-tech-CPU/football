-- Blasons Norvege recuperes via TheSportsDB.
UPDATE team SET logo_path = '862.png' WHERE id = 862; -- FK BODO GLIMT -> Bodo/Glimt
UPDATE team SET logo_path = '62.png' WHERE id = 62; -- LILLESTROM -> Lillestrom
UPDATE team SET logo_path = '113.png' WHERE id = 113; -- SANDERFJORD -> Sandefjord
UPDATE team SET logo_path = '878.png' WHERE id = 878; -- TROMSO IL -> Tromso

-- La coupe partageait le meme round_label generique "Coupe nationale" pour tous les tours :
-- reconstitue a partir des dates/vainqueurs (8 equipes en huitiemes -> quarts -> demies ->
-- finale, tout en match unique). La finale s'est terminee a egalite (SK BRANN 2-2 FK BODO
-- GLIMT), qualification aux tirs au but inconnue - a ajouter via le controle "+ Tirs au but".
UPDATE match SET round_label = 'Coupe nationale - Huitièmes de finale'
WHERE id IN (32, 48, 52, 57, 68, 72, 77, 78);
UPDATE match SET round_label = 'Coupe nationale - Quarts de finale'
WHERE id IN (150, 151, 152, 200);
UPDATE match SET round_label = 'Coupe nationale - Demi-finales'
WHERE id IN (456, 465);
UPDATE match SET round_label = 'Coupe nationale - Finale'
WHERE id = 695;

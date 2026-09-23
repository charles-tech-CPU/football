-- Blasons Suede recuperes via TheSportsDB.
UPDATE team SET logo_path = '105.png' WHERE id = 105; -- AIK STOCKHOLM
UPDATE team SET logo_path = '130.png' WHERE id = 130; -- DEGEFORS
UPDATE team SET logo_path = '110.png' WHERE id = 110; -- DJUGARDEN
UPDATE team SET logo_path = '876.png' WHERE id = 876; -- HAMMARBY IF
UPDATE team SET logo_path = '107.png' WHERE id = 107; -- MALMO FF

-- La coupe partageait le meme round_label generique "Coupe nationale" pour tous les tours
-- (8 equipes en quarts -> demies -> finale, tout en match unique). La demi HAMMARBY IF-SIRIUS
-- s'est terminee a egalite (3-3), HAMMARBY qualifie aux tirs au but (confirme par sa presence
-- en finale) - score inconnu, a ajouter via le controle "+ Tirs au but".
UPDATE match SET round_label = 'Coupe nationale - Quarts de finale'
WHERE id IN (102, 107, 119, 129);
UPDATE match SET round_label = 'Coupe nationale - Demi-finales'
WHERE id IN (188, 196);
UPDATE match SET round_label = 'Coupe nationale - Finale'
WHERE id = 741;

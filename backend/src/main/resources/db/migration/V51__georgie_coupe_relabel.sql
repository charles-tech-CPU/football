-- La coupe partageait le meme round_label generique "Coupe nationale" pour tous les tours
-- connus : reconstitue a partir des dates/vainqueurs (8 equipes en huitiemes dont le match
-- SAMTREDIA-IBERIA 1999 joue plus tard le 02/09, 4 equipes en quarts dont SAMTREDIA-RUSTAVI
-- pas encore joue, demies/finale deja correctement placees en "a determiner").
UPDATE match SET round_label = 'Coupe nationale - Huitièmes de finale'
WHERE id IN (1602, 1603, 1604, 1640, 1641, 1689, 1690, 2890);
UPDATE match SET round_label = 'Coupe nationale - Quarts de finale'
WHERE id IN (2511, 2513, 2514, 4476);

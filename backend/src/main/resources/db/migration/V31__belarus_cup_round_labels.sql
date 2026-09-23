-- Les 13 matchs de la coupe de Bielorussie partageaient tous le meme round_label generique
-- "Coupe nationale", sans distinction de tour : le bracket ne pouvait donc pas les etager
-- (Quarts/Demi/Finale) et les affichait tous mélanges. Etapes reconstituees a partir des
-- dates et de la progression des equipes (8 equipes au depart, pas de huitiemes dans les
-- donnees) : quarts et demies en aller-retour, finale en match unique.

-- Quarts de finale (mars 2026)
UPDATE match SET round_label = 'Coupe nationale - Quarts de finale - Aller' WHERE id IN (40, 53, 64, 69);
UPDATE match SET round_label = 'Coupe nationale - Quarts de finale - Retour' WHERE id IN (105, 109, 111, 116);

-- Demi-finales (avril 2026)
UPDATE match SET round_label = 'Coupe nationale - Demi-finales - Aller' WHERE id IN (359, 362);
UPDATE match SET round_label = 'Coupe nationale - Demi-finales - Retour' WHERE id IN (553, 556);

-- Finale (16 mai 2026, match unique) : DINAMO MINSK 1-2 BATE
UPDATE match SET round_label = 'Coupe nationale - Finale' WHERE id = 777;

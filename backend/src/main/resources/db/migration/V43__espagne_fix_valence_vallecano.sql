-- VALLECANO avait 2 matchs de trop (40 au lieu de 38) et VALENCE 2 de moins (36) : 2 matchs
-- ou VALENCE aurait du jouer ont ete saisis avec VALLECANO a la place (confirme par
-- VALENCE absent de J3/J6 alors que le retour existe deja sous son vrai nom - J29 et J24).
UPDATE match SET team2_id = 678 WHERE id = 2804; -- J3 A COROGNE - VALENCE (score conserve)
UPDATE match SET team2_id = 678 WHERE id = 3688; -- J6 ALAVES - VALENCE

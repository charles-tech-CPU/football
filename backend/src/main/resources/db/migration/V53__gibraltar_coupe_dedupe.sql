-- Chaque tour de la coupe (huitiemes, quarts, demies, finale) avait ete insere en double
-- (8 places generiques au lieu de 4 pour les huitiemes/quarts, 4 au lieu de 2 pour les
-- demies, 2 au lieu de 1 pour la finale) : structure confirmee par l'utilisateur = 4
-- huitiemes + 4 quarts + 2 demies + 1 finale, tout en match aller simple.
DELETE FROM match WHERE id IN (11740, 11741, 11742, 11743); -- doublons huitiemes
DELETE FROM match WHERE id IN (11860, 11861, 11862, 11863); -- doublons quarts
DELETE FROM match WHERE id IN (11873, 11875); -- doublons demies
DELETE FROM match WHERE id = 11987; -- doublon finale

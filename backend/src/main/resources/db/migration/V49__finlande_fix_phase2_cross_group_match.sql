-- La 2e phase de Finlande se scinde en 2 mini-championnats de 6 (groupe du haut : VPS,
-- OULU, GNISTAN, KUPS, HJK, INTER TURKU ; groupe du bas : FC ILVES, JARO, LAHTI, MARIEHAMN,
-- SJK, TPS TURKU), confirme par le nombre de confrontations : chaque equipe doit affronter
-- exactement 2 fois ses 5 coequipiers de groupe. Le match J27 INTER TURKU-TPS TURKU
-- traversait les 2 groupes (TPS TURKU se retrouvait avec 6 adversaires distincts au lieu de
-- 5, KUPS avec seulement 9 confrontations au lieu de 10) : INTER TURKU devait en realite
-- recevoir KUPS, son seul adversaire de groupe encore manquant a cette journee (KUPS et
-- INTER TURKU etant tous deux absents des autres affiches du groupe du haut le meme jour).
UPDATE match SET team2_id = 132 WHERE id = 4354; -- KUPS au lieu de TPS TURKU

-- Coupe des Iles Feroe entierement reconstituee avec l'utilisateur : 2 des 4 scores de
-- quarts etaient inverses (HB TORSHAVN et KI KLAKSVIK etaient en realite qualifies, pas B36
-- TORSHAVN et TOFTIR qui n'apparaissent plus ensuite), et la finale opposait par erreur
-- VIKINGUR REYKJAVIK (club islandais existant, sans rapport) au lieu de VIKINGUR (Iles Feroe).
UPDATE match SET score1 = 3, score2 = 1 WHERE id = 557; -- HB TORSHAVN 3-1 B36 TORSHAVN (et non 0-3)
UPDATE match SET score1 = 0, score2 = 3 WHERE id = 558; -- TOFTIR 0-3 KI KLAKSVIK (et non 3-1)
UPDATE match SET team1_id = 94 WHERE id = 3098; -- VIKINGUR (Iles Feroe) et non VIKINGUR REYKJAVIK (Islande)

-- Round_label generique "Coupe nationale" partage par tous les tours : relabellise selon la
-- progression reconstituee (huitiemes -> quarts -> demies aller-retour -> finale simple).
-- Demi KI KLAKSVIK-NSI RUNAVIK terminee 6-6 a l'aggrege, KI qualifie aux tirs au but (3-2,
-- non representable en base, pas de champ dedie).
UPDATE match SET round_label = 'Coupe nationale - Huitièmes de finale'
WHERE id IN (358, 360, 364, 365, 366, 368, 369, 370);
UPDATE match SET round_label = 'Coupe nationale - Quarts de finale'
WHERE id IN (557, 558, 559, 565);
UPDATE match SET round_label = 'Coupe nationale - Demi-finales'
WHERE id IN (735, 742, 1155, 1159);
UPDATE match SET round_label = 'Coupe nationale - Finale'
WHERE id = 3098;

-- Blason 07 VESTUR SORVAGUR recupere via TheSportsDB.
UPDATE team SET logo_path = '89.png' WHERE id = 89;

-- Icones : 9e et 10e relegues.
UPDATE competition SET relegation_slots = 2 WHERE code = 'ILES FEROE' AND season = 2027;

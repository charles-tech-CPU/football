-- 2 matchs dupliques en J22 et J25 (ULYTAU-KAIRAT ALMATY et KAIRAT ALMATY-KASPIJ AKTAU
-- copies d'un match deja existant en J9/J6) alors qu'il manquait le match retour de KAISAR-
-- ZHETYSU et de TOBOL KOSTANAI-ZHENIS (les 4 equipes concernees avaient 29 matchs au lieu de
-- 30) : confirme par le comptage de confrontations, corrige en reassignant les equipes tout
-- en conservant les scores deja saisis.
UPDATE match SET team1_id = (SELECT id FROM team WHERE name = 'ZHETYSU'), team2_id = (SELECT id FROM team WHERE name = 'KAISAR') WHERE id = 2018;
UPDATE match SET team1_id = (SELECT id FROM team WHERE name = 'ZHENIS'), team2_id = (SELECT id FROM team WHERE name = 'TOBOL KOSTANAI') WHERE id = 2997;

-- La coupe partageait le meme round_label generique "Coupe nationale" pour tous les tours :
-- reconstitue a partir des dates/vainqueurs (8 equipes en huitiemes, quarts en match unique
-- avec 2 tours a egalite - AKTOBE-ALTAI et KAISAR-TOBOL KOSTANAI, qualifies aux tirs au but
-- d'apres la suite du tableau, scores non connus a ajouter via le bracket -, demies en aller-
-- retour, finale a venir).
UPDATE match SET round_label = 'Coupe nationale - Huitièmes de finale'
WHERE id IN (548, 549, 550, 551, 552, 560, 561, 562);
UPDATE match SET round_label = 'Coupe nationale - Quarts de finale'
WHERE id IN (730, 731, 733, 734);
UPDATE match SET round_label = 'Coupe nationale - Demi-finales'
WHERE id IN (1337, 1547, 2227, 2228);
UPDATE match SET round_label = 'Coupe nationale - Finale'
WHERE id = 5580;

-- Icones : 13e, 14e, 15e et 16e relegues, pas de barrage.
UPDATE competition SET relegation_slots = 4, barrage_slots = 0 WHERE code = 'KAZAKHSTAN' AND season = 2027;

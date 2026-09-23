-- Correction de la V60 precedente : en reassignant les 2 matchs dupliques (J22 ULYTAU-
-- KAIRAT ALMATY et J25 KAIRAT ALMATY-KASPIJ AKTAU) vers KAISAR/ZHETYSU et ZHENIS/TOBOL
-- KOSTANAI, on a bien comble le trou de ces 4 equipes mais recree un trou ailleurs : J9 et
-- J22 etaient en realite 2 fois EXACTEMENT le meme match (ULYTAU recevant KAIRAT ALMATY,
-- jamais l'inverse) et J6/J25 pareil pour KAIRAT ALMATY-KASPIJ AKTAU - le match retour de
-- ces 2 confrontations n'a en fait jamais existe. Ajoute les 2 matchs manquants (score/date
-- inconnus, comme le fait deja le match TOBOL KOSTANAI-ZHENIS existant id 11420).
INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES
  ((SELECT id FROM competition WHERE code = 'KAZAKHSTAN' AND season = 2027), 'J23', NULL, NULL, (SELECT id FROM team WHERE name = 'KAIRAT ALMATY'), (SELECT id FROM team WHERE name = 'ULYTAU'), NULL, NULL, 'SCHEDULED'),
  ((SELECT id FROM competition WHERE code = 'KAZAKHSTAN' AND season = 2027), 'J23', NULL, NULL, (SELECT id FROM team WHERE name = 'KASPIJ AKTAU'), (SELECT id FROM team WHERE name = 'KAIRAT ALMATY'), NULL, NULL, 'SCHEDULED');

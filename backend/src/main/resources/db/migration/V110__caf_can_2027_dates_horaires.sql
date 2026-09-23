-- Qualifications CAN 2027 : dates et heures des journees 1 et 2 recalees sur le calendrier
-- officiel (source flashscore.fr, heures de Paris, releve du 23/09/2026). V107 avait mis tous
-- les matchs d une journee le meme jour a 18h00. Journees 3 et 4 deja correctes ; journees 5
-- et 6 (mars 2027) : seule la date est connue, deja correcte, heures laissees telles quelles.
-- Senegal / Mozambique : domicile et exterieur etaient inverses a l aller (J1) et au retour (J5).

UPDATE match SET date = DATE '2026-09-26', time = TIME '15:00' WHERE id = 12818; -- J1 Afrique du Sud - Guinée
UPDATE match SET date = DATE '2026-09-25', time = TIME '21:00' WHERE id = 12819; -- J1 Algérie - Zambie
UPDATE match SET date = DATE '2026-09-25', time = TIME '21:00' WHERE id = 12820; -- J1 Burkina Faso - Bénin
UPDATE match SET date = DATE '2026-09-24', time = TIME '21:00' WHERE id = 12821; -- J1 Cameroun - Comores
UPDATE match SET date = DATE '2026-09-24', time = TIME '21:00' WHERE id = 12822; -- J1 Côte d'Ivoire - Ghana
UPDATE match SET date = DATE '2026-09-25', time = TIME '21:00' WHERE id = 12823; -- J1 Égypte - Angola
UPDATE match SET date = DATE '2026-09-25', time = TIME '18:00' WHERE id = 12824; -- J1 Gambie - Somalie
UPDATE match SET date = DATE '2026-09-24', time = TIME '18:00' WHERE id = 12826; -- J1 Libye - Botswana
UPDATE match SET date = DATE '2026-09-25', time = TIME '18:00' WHERE id = 12827; -- J1 Malawi - Soudan du Sud
UPDATE match SET date = DATE '2026-09-25', time = TIME '21:00' WHERE id = 12828; -- J1 Mali - Cap-Vert
UPDATE match SET date = DATE '2026-09-25', time = TIME '21:00' WHERE id = 12829; -- J1 Maroc - Gabon
UPDATE match SET date = DATE '2026-09-24', time = TIME '18:00' WHERE id = 12830; -- J1 Mauritanie - Centrafrique
UPDATE match SET date = DATE '2026-09-24', time = TIME '15:00' WHERE id = 12831; -- J1 Namibie - Congo
UPDATE match SET date = DATE '2026-09-25', time = TIME '17:00' WHERE id = 12832; -- J1 Niger - Lesotho
UPDATE match SET date = DATE '2026-09-25', time = TIME '18:00' WHERE id = 12833; -- J1 Nigéria - Madagascar
UPDATE match SET date = DATE '2026-09-24', time = TIME '18:00' WHERE id = 12834; -- J1 RD Congo - Guinée Équatoriale
UPDATE match SET date = DATE '2026-09-25', time = TIME '18:00' WHERE id = 12835; -- J1 Rwanda - Libéria
UPDATE match SET date = DATE '2026-09-25', time = TIME '15:00', team1_id = team2_id, team2_id = team1_id WHERE id = 12836; -- J1 Mozambique - Sénégal
UPDATE match SET date = DATE '2026-09-24', time = TIME '21:00' WHERE id = 12837; -- J1 Sierra Leone - Zimbabwe
UPDATE match SET date = DATE '2026-09-25', time = TIME '18:00' WHERE id = 12838; -- J1 Soudan - Ethiopie
UPDATE match SET date = DATE '2026-09-25', time = TIME '15:00' WHERE id = 12839; -- J1 Tanzanie - Guinée-Bissau
UPDATE match SET date = DATE '2026-09-25', time = TIME '18:00' WHERE id = 12840; -- J1 Togo - Burundi
UPDATE match SET date = DATE '2026-09-24', time = TIME '21:00' WHERE id = 12841; -- J1 Tunisie - Ouganda
UPDATE match SET date = DATE '2026-10-06', time = TIME '21:00' WHERE id = 12842; -- J2 Angola - Malawi
UPDATE match SET date = DATE '2026-09-29', time = TIME '19:00' WHERE id = 12843; -- J2 Bénin - Mauritanie
UPDATE match SET date = DATE '2026-09-28', time = TIME '21:00' WHERE id = 12844; -- J2 Botswana - Tunisie
UPDATE match SET date = DATE '2026-09-29', time = TIME '15:00' WHERE id = 12845; -- J2 Burundi - Algérie
UPDATE match SET date = DATE '2026-09-29', time = TIME '18:00' WHERE id = 12846; -- J2 Cap-Vert - Rwanda
UPDATE match SET date = DATE '2026-09-28', time = TIME '18:00' WHERE id = 12847; -- J2 Centrafrique - Burkina Faso
UPDATE match SET date = DATE '2026-09-29', time = TIME '13:00' WHERE id = 12848; -- J2 Comores - Namibie
UPDATE match SET date = DATE '2026-09-29', time = TIME '21:00' WHERE id = 12849; -- J2 Congo - Cameroun
UPDATE match SET date = DATE '2026-09-30', time = TIME '18:00' WHERE id = 12850; -- J2 Érythrée - Afrique du Sud
UPDATE match SET date = DATE '2026-09-29', time = TIME '15:00' WHERE id = 12851; -- J2 Ethiopie - Sénégal
UPDATE match SET date = DATE '2026-09-29', time = TIME '21:00' WHERE id = 12852; -- J2 Gabon - Niger
UPDATE match SET date = DATE '2026-09-29', time = TIME '18:00' WHERE id = 12853; -- J2 Ghana - Gambie
UPDATE match SET date = DATE '2026-10-01', time = TIME '18:00' WHERE id = 12854; -- J2 Guinée - Kenya
UPDATE match SET date = DATE '2026-09-28', time = TIME '18:00' WHERE id = 12855; -- J2 Guinée Équatoriale - Sierra Leone
UPDATE match SET date = DATE '2026-09-29', time = TIME '18:00' WHERE id = 12856; -- J2 Guinée-Bissau - Nigéria
UPDATE match SET date = DATE '2026-09-29', time = TIME '15:00' WHERE id = 12857; -- J2 Lesotho - Maroc
UPDATE match SET date = DATE '2026-09-29', time = TIME '21:00' WHERE id = 12858; -- J2 Libéria - Mali
UPDATE match SET date = DATE '2026-09-29', time = TIME '15:00' WHERE id = 12859; -- J2 Madagascar - Tanzanie
UPDATE match SET date = DATE '2026-09-29', time = TIME '15:00' WHERE id = 12860; -- J2 Mozambique - Soudan
UPDATE match SET date = DATE '2026-09-29', time = TIME '18:00' WHERE id = 12861; -- J2 Ouganda - Libye
UPDATE match SET date = DATE '2026-09-29', time = TIME '21:00' WHERE id = 12862; -- J2 Somalie - Côte d'Ivoire
UPDATE match SET date = DATE '2026-09-29', time = TIME '15:00' WHERE id = 12863; -- J2 Soudan du Sud - Égypte
UPDATE match SET date = DATE '2026-09-29', time = TIME '18:00' WHERE id = 12864; -- J2 Zambie - Togo
UPDATE match SET date = DATE '2026-09-28', time = TIME '18:00' WHERE id = 12865; -- J2 Zimbabwe - RD Congo
UPDATE match SET team1_id = team2_id, team2_id = team1_id WHERE id = 12932; -- J5 Sénégal - Mozambique (2027-03-24)

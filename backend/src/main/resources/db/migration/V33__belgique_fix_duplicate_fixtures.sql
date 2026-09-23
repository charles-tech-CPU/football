-- 2 vrais doublons dans le calendrier belge, qui faisaient croire a une 2e phase (matchs
-- apparaissant 2 fois avec exactement la meme orientation) :
-- - J9 (id 4882) "LOUVAIN - RAAL LA LOUVIERE" dupliquait le J7 (id 3837), alors que
--   LOMMEL SK etait le seul club absent du J9 et que "LOMMEL SK - LOUVAIN" (J19) n'avait
--   pas son retour.
-- - J29 (id 9893) "MALINES - ROYALE UNION SG" dupliquait le J14 (id 6958) - Royale Union SG
--   jouait deja Antwerp ce jour-la (id 9894) - alors que RAAL LA LOUVIERE etait absent du
--   J29 et que "RAAL LA LOUVIERE - MALINES" (J4) n'avait pas son retour.
UPDATE match SET team2_id = 514 WHERE id = 4882; -- LOUVAIN - LOMMEL SK
UPDATE match SET team2_id = 542 WHERE id = 9893; -- MALINES - RAAL LA LOUVIERE

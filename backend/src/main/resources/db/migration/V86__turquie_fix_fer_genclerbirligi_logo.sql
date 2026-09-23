-- "FER" (822) est en realite FENERBAHCE SK (858), tronque sur 1 de ses matchs. De plus,
-- GENCLERBIRLIGI avait 1 match de trop contre SAMSUNSPOR (3 confrontations au lieu de 2)
-- alors que FENERBAHCE n'avait pas son match aller contre SAMSUNSPOR : confirme par le
-- comptage (FENERBAHCE 32/34, GENCLERBIRLIGI 35/34 avant correction).
UPDATE match SET team1_id = 858 WHERE id = 7560; -- FER -> FENERBAHCE SK (vs AMEDSPOR)
DELETE FROM team WHERE id = 822;
UPDATE match SET team1_id = 858 WHERE id = 10162; -- GENCLERBIRLIGI -> FENERBAHCE SK (vs SAMSUNSPOR)

-- Blason Turquie recupere via TheSportsDB.
UPDATE team SET logo_path = '880.png' WHERE id = 880; -- BESIKTAS JK -> Besiktas

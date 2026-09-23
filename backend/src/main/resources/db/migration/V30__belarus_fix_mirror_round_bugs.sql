-- La 2e moitie de saison du championnat bielorusse (J16-J30) inverse exactement les
-- confrontations de son "miroir" en 1ere moitie (J1-J15, decalage de 15 journees), sauf
-- ces 3 matchs restes dans le meme sens (ou dupliques), detectes en comparant chaque
-- journee a son miroir (toutes les autres confrontations sont bien inversees).

-- J17 (id 1909) doit etre le miroir inverse de J2 "FC MINSK - GOMEL" (0-1) : GOMEL a
-- domicile, score conserve (Gomel gagnant 1-0), pas seulement l'intitule des equipes.
UPDATE match SET team1_id = 119, team2_id = 116, score1 = 1, score2 = 0 WHERE id = 1909;

-- J22 (id 3506) doit etre le miroir inverse de J7 "GOMEL - ISLOCH" : pas encore joue,
-- simple inversion des equipes.
UPDATE match SET team1_id = 56, team2_id = 119 WHERE id = 3506;

-- J28 (id 5618) "GOMEL - DYNAMO BREST" fait doublon : Dynamo Brest joue deja ce jour-la
-- contre FC MINSK (id 5617), et le vrai miroir de J10 "GOMEL - DYNAMO BREST" existe deja
-- correctement a J25 (id 4571, "DYNAMO BREST - GOMEL"). Le seul adversaire manquant pour
-- Gomel a domicile ce jour-la est ARSENAL DZERZHINSK (deja affronte a l'exterieur en J13).
UPDATE match SET team2_id = 117 WHERE id = 5618;

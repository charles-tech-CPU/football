-- Le match J25 (id 9406) dupliquait exactement le J22 (id 8742) : SABAH BAKU - KAPAZ,
-- alors que le match retour KAPAZ - SABAH BAKU n'existait nulle part dans le calendrier.
-- On inverse les equipes du J25 pour en faire le vrai match retour manquant.
UPDATE match SET team1_id = 590, team2_id = 589 WHERE id = 9406;

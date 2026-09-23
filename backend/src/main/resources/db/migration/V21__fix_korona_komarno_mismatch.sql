-- Le match J12 du championnat slovaque (id 4922, 2026-10-24, vs TRENCIN) pointait par
-- erreur de saisie vers KORONA (id 276, club polonais - Korona Kielce), au lieu de
-- KOMARNO (id 302, club slovaque). Confirme par l'utilisateur.
UPDATE match SET team1_id = 302 WHERE id = 4922 AND team1_id = 276;

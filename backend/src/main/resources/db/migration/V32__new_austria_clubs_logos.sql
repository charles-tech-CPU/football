-- Clubs autrichiens ajoutes manuellement par l'utilisateur (coupe d'Autriche) : nom mis en
-- majuscules (convention du reste de la base) et blasons recuperes via TheSportsDB.
UPDATE team SET name = 'FIRST VIENNE' WHERE id = 990;

UPDATE team SET logo_path = '990.png' WHERE id = 990; -- FIRST VIENNE -> First Vienna
UPDATE team SET logo_path = '991.png' WHERE id = 991; -- HERTHA WELS
UPDATE team SET logo_path = '993.png' WHERE id = 993; -- INNSBRUCK -> Wacker Innsbruck
-- BREGENZ (id 992) : aucun blason trouve sur TheSportsDB, laisse sans logo pour l'instant.

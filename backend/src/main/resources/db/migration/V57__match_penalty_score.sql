-- Score aux tirs au but, uniquement renseigne quand un match/aggregat de coupe se termine
-- a egalite et doit etre departage (ex: demi-finale Iles Feroe KI KLAKSVIK-NSI RUNAVIK,
-- 6-6 a l'aggrege, KI qualifie 3-2 aux tab). Null dans tous les autres cas.
ALTER TABLE match ADD COLUMN penalty_score1 INTEGER;
ALTER TABLE match ADD COLUMN penalty_score2 INTEGER;

-- Renseigne retroactivement pour la demi Iles Feroe : match 1155 = NSI RUNAVIK (team1) -
-- KI KLAKSVIK (team2), KI qualifie 3-2 aux tab.
UPDATE match SET penalty_score1 = 2, penalty_score2 = 3 WHERE id = 1155;

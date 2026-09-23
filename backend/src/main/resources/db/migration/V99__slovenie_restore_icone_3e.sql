-- Correction V97 : le 3e avait perdu son icone Conference League en decalant l'unique slot
-- ecl vers le 2e. Comme pour la Suede (V98), le 2e et le 3e doivent tous les deux avoir
-- l'icone Conference.
UPDATE competition SET ecl_slots = 2 WHERE code = 'SLOVENIE' AND season = 2027;

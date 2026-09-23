-- Seul le 2e change : Europa League -> Conference League. Le 3e garde son icone Conference
-- inchangee (elSlots retire, eclSlots etendu a 2 pour couvrir 2e ET 3e, au lieu de decaler
-- l'unique slot ecl existant comme pour la Slovenie).
UPDATE competition SET el_slots = 0, ecl_slots = 2 WHERE code = 'SUEDE' AND season = 2027;

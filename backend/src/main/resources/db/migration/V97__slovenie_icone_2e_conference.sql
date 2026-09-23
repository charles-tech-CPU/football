-- Le 2e place n'est plus qualifie Europa League mais Conference League (elSlots retire, le
-- slot ecl existant se decale donc du 3e vers le 2e).
UPDATE competition SET el_slots = 0, ecl_slots = 1 WHERE code = 'SLOVENIE' AND season = 2027;

-- Pas de barragiste en Hongrie : le 11e (auparavant barragiste) est en realite relegue,
-- comme le 12e.
UPDATE competition SET barrage_slots = 0, relegation_slots = 2
WHERE code = 'HONGRIE' AND season = 2027;

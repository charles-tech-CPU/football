-- Ukraine : 1er champion (LDC), 2e-3e Conference League (pas d'Europa League), 13e-14e
-- barragistes de maintien, 15e-16e relegues - bloc barrage/relegation contigu en bas de
-- tableau, donc pas besoin de rankMarkers dedies dans CountryView.vue (systeme d'icones
-- automatique standard).
UPDATE competition SET ldc_slots = 1, el_slots = 0, ecl_slots = 2, barrage_slots = 2, relegation_slots = 2
WHERE code = 'UKRAINE' AND season = 2027;

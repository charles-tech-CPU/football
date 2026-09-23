-- Turquie : 1er champion (LDC), 2e LDC, 3e Europa League, 4e Conference League, 16e-17e-18e
-- relegues - bloc de relegation contigu en bas de tableau (18 equipes), donc pas besoin de
-- rankMarkers dedies dans CountryView.vue (systeme d'icones automatique standard).
UPDATE competition SET ldc_slots = 2, el_slots = 1, ecl_slots = 1, relegation_slots = 3
WHERE code = 'TURQUIE' AND season = 2027;

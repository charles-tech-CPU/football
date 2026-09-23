-- barrageSlots=1 restait de la config initiale et aurait affiche une icone ⚔️ parasite au
-- 17e rang (bloc automatique colle a relegationSlots=1), sans rapport avec les 2 vrais
-- barrages (europeen 7e-10e, maintien 14e/15e) desormais geres par rankMarkers dedies dans
-- CountryView.vue. relegationSlots=1 (dernier rang) reste inchange.
UPDATE competition SET barrage_slots = 0 WHERE code = 'TCHEQUIE' AND season = 2027;

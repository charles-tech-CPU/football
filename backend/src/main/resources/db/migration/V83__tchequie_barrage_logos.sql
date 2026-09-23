-- Blasons Tchequie recuperes via TheSportsDB.
UPDATE team SET logo_path = '307.png' WHERE id = 307; -- BRNO -> Zbrojovka Brno
UPDATE team SET logo_path = '283.png' WHERE id = 283; -- OSTRAVA -> Banik Ostrava

-- Barrages non contigus (maintien 14e/15e, europeen 9e/10e) : icones ajoutees via
-- rankMarkers dedies dans CountryView.vue plutot que barrageSlots (qui suppose une zone de
-- barrage collee a la relegation). relegationSlots/barrageSlots de base laisses inchanges,
-- la vraie coupure de relegation (18e seul ? 16e-17e-18e ?) n'est pas confirmee.

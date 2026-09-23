-- Places de relegation directe / barrage de maintien, configurables par championnat au
-- meme titre que les places qualificatives europeennes (LDC/EL/ECL), pour piloter les
-- icones automatiques du classement (barrage ⚔️ / relegation ⬇️).
ALTER TABLE competition ADD COLUMN relegation_slots INTEGER NOT NULL DEFAULT 0;
ALTER TABLE competition ADD COLUMN barrage_slots INTEGER NOT NULL DEFAULT 0;

UPDATE competition SET relegation_slots = 2, barrage_slots = 1
WHERE code = 'ALBANIE' AND season = 2027;

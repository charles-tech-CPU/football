-- Demies inserees en double (4 places generiques au lieu de 2, 2 par date), meme bug que
-- Gibraltar/Moldavie/Montenegro/Portugal : tout encore en placeholders "a determiner".
DELETE FROM match WHERE id IN (11989, 12027); -- doublon demies

-- Blasons Slovaquie recuperes via TheSportsDB.
UPDATE team SET logo_path = '353.png' WHERE id = 353; -- BANSKA BYSTRICA -> Dukla Banska Bystrica
UPDATE team SET logo_path = '342.png' WHERE id = 342; -- DUN. STREDA -> DAC 1904 Dunajska Streda

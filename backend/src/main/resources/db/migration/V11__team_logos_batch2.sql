-- Deuxieme lot de blasons (hors top 5), recuperes automatiquement via TheSportsDB
-- avec matching strict sur le nom (voir import/fetch_logos.py et V10). 409 equipes
-- reconnues avec certitude sur ce lot ; les autres (noms ambigus/non trouves) restent
-- a traiter manuellement, voir le rapport correspondant hors-repo.

UPDATE team SET logo_path = '805.png' WHERE id = 805; -- FC SANTA COLOMA (Andorre) -> FC Santa Coloma
UPDATE team SET logo_path = '811.png' WHERE id = 811; -- ORDINO (Andorre) -> Ordino
UPDATE team SET logo_path = '922.png' WHERE id = 922; -- FC ALASHKERT (ARM) -> Alashkert
UPDATE team SET logo_path = '958.png' WHERE id = 958; -- FC NOAH (ARM) -> Noah
UPDATE team SET logo_path = '395.png' WHERE id = 395; -- BKMA (Armenie) -> BKMA
UPDATE team SET logo_path = '404.png' WHERE id = 404; -- FC GANDZASAR (Armenie) -> Gandzasar
UPDATE team SET logo_path = '442.png' WHERE id = 442; -- NOAH (Armenie) -> Noah
UPDATE team SET logo_path = '370.png' WHERE id = 370; -- SARDARAPAT (Armenie) -> Sardarapat
UPDATE team SET logo_path = '443.png' WHERE id = 443; -- SYUNIK (Armenie) -> Syunik
UPDATE team SET logo_path = '454.png' WHERE id = 454; -- URARTU (Armenie) -> Urartu
UPDATE team SET logo_path = '369.png' WHERE id = 369; -- VAN (Armenie) -> Van
UPDATE team SET logo_path = '457.png' WHERE id = 457; -- ALTACH (Autriche) -> SCR Altach
UPDATE team SET logo_path = '373.png' WHERE id = 373; -- LASK (Autriche) -> LASK
UPDATE team SET logo_path = '402.png' WHERE id = 402; -- STURM GRAZ (Autriche) -> Sturm Graz
UPDATE team SET logo_path = '401.png' WHERE id = 401; -- TIROL (Autriche) -> Tirol
UPDATE team SET logo_path = '446.png' WHERE id = 446; -- WOLFSBERGER (Autriche) -> Wolfsberger AC
UPDATE team SET logo_path = '964.png' WHERE id = 964; -- NEFTCI PFK (AZE) -> Neftçi PFK
UPDATE team SET logo_path = '903.png' WHERE id = 903; -- ZIRA FK (AZE) -> Zirə
UPDATE team SET logo_path = '581.png' WHERE id = 581; -- IMISLI (Azerbaidjan) -> İmişli
UPDATE team SET logo_path = '589.png' WHERE id = 589; -- SABAH BAKU (Azerbaidjan) -> Sabah Baku
UPDATE team SET logo_path = '602.png' WHERE id = 602; -- SUMQAYIT (Azerbaidjan) -> Sumqayıt
UPDATE team SET logo_path = '562.png' WHERE id = 562; -- TURAN (Azerbaidjan) -> Turan
UPDATE team SET logo_path = '861.png' WHERE id = 861; -- UNION SAINT GILLOISE (BEL) -> Union Saint-Gilloise
UPDATE team SET logo_path = '117.png' WHERE id = 117; -- ARSENAL DZERZHINSK (Belarus) -> Arsenal Dzerzhinsk
UPDATE team SET logo_path = '123.png' WHERE id = 123; -- BARANOVICI (Belarus) -> Baranovichi
UPDATE team SET logo_path = '125.png' WHERE id = 125; -- DNEPR MOGILEV (Belarus) -> Dnepr Mogilev
UPDATE team SET logo_path = '71.png' WHERE id = 71; -- DYNAMO BREST (Belarus) -> Dynamo Brest
UPDATE team SET logo_path = '116.png' WHERE id = 116; -- FC MINSK (Belarus) -> Minsk
UPDATE team SET logo_path = '119.png' WHERE id = 119; -- GOMEL (Belarus) -> Gomel
UPDATE team SET logo_path = '81.png' WHERE id = 81; -- SLAVIA MOZYR (Belarus) -> Slavia Mozyr
UPDATE team SET logo_path = '118.png' WHERE id = 118; -- VITEBSK (Belarus) -> Vitebsk
UPDATE team SET logo_path = '545.png' WHERE id = 545; -- ANTWERP (Belgique) -> Antwerp
UPDATE team SET logo_path = '546.png' WHERE id = 546; -- BEVEREN (Belgique) -> Beveren
UPDATE team SET logo_path = '534.png' WHERE id = 534; -- CHARLEROI (Belgique) -> Charleroi
UPDATE team SET logo_path = '484.png' WHERE id = 484; -- CLUB BRUGGE (Belgique) -> Club Brugge
UPDATE team SET logo_path = '537.png' WHERE id = 537; -- GENK (Belgique) -> Genk
UPDATE team SET logo_path = '514.png' WHERE id = 514; -- LOMMEL SK (Belgique) -> Lommel
UPDATE team SET logo_path = '542.png' WHERE id = 542; -- RAAL LA LOUVIERE (Belgique) -> RAAL La Louvière
UPDATE team SET logo_path = '503.png' WHERE id = 503; -- STANDARD LIEGE (Belgique) -> Standard Liège
UPDATE team SET logo_path = '515.png' WHERE id = 515; -- WESTERLO (Belgique) -> Westerlo
UPDATE team SET logo_path = '914.png' WHERE id = 914; -- BATE BORISOV (BLR) -> BATE Borisov
UPDATE team SET logo_path = '901.png' WHERE id = 901; -- DINAMO MINSK (BLR) -> Dinamo Minsk
UPDATE team SET logo_path = '844.png' WHERE id = 844; -- FK BORAC BANJA LUKA (BOS) -> Borac Banja Luka
UPDATE team SET logo_path = '926.png' WHERE id = 926; -- FK SARAJEVO (BOS) -> FK Sarajevo
UPDATE team SET logo_path = '899.png' WHERE id = 899; -- FK VELEZ MOSTAR (BOS) -> Velež Mostar
UPDATE team SET logo_path = '483.png' WHERE id = 483; -- BSK BANJA LUKA (Bosnie) -> BSK Banja Luka
UPDATE team SET logo_path = '549.png' WHERE id = 549; -- CELIK ZENICA (Bosnie) -> Čelik Zenica
UPDATE team SET logo_path = '520.png' WHERE id = 520; -- RADNIK BIJELJINA (Bosnie) -> Radnik Bijeljina
UPDATE team SET logo_path = '521.png' WHERE id = 521; -- SIROKI BRIJEG (Bosnie) -> Široki Brijeg
UPDATE team SET logo_path = '482.png' WHERE id = 482; -- ZELJEZNICAR (Bosnie) -> Željezničar Sarajevo
UPDATE team SET logo_path = '845.png' WHERE id = 845; -- LEVSKI SOFIA (BUL) -> Levski Sofia
UPDATE team SET logo_path = '973.png' WHERE id = 973; -- LUDOGORETS RAZGRAD (BUL) -> Ludogorets Razgrad
UPDATE team SET logo_path = '257.png' WHERE id = 257; -- BOTEV PLOVDIV (Bulgarie) -> Botev Plovdiv
UPDATE team SET logo_path = '244.png' WHERE id = 244; -- CHERNO MORE (Bulgarie) -> Cherno More
UPDATE team SET logo_path = '256.png' WHERE id = 256; -- CSKA SOFIA (Bulgarie) -> CSKA Sofia
UPDATE team SET logo_path = '218.png' WHERE id = 218; -- DUNAV RUSE (Bulgarie) -> Dunav Ruse
UPDATE team SET logo_path = '225.png' WHERE id = 225; -- SEPTEMVRI SOFIA (Bulgarie) -> Septemvri Sofia
UPDATE team SET logo_path = '255.png' WHERE id = 255; -- SLAVIA SOFIA (Bulgarie) -> Slavia Sofia
UPDATE team SET logo_path = '209.png' WHERE id = 209; -- SPARTAK VARNA (Bulgarie) -> Spartak Varna
UPDATE team SET logo_path = '775.png' WHERE id = 775; -- AEK LARNACA (Chypre) -> AEK Larnaca
UPDATE team SET logo_path = '790.png' WHERE id = 790; -- AEL LIMASSOL (Chypre) -> AEL Limassol
UPDATE team SET logo_path = '791.png' WHERE id = 791; -- ANORTHOSIS (Chypre) -> Anorthosis
UPDATE team SET logo_path = '792.png' WHERE id = 792; -- KARMIOTISSA (Chypre) -> Karmiotissa
UPDATE team SET logo_path = '748.png' WHERE id = 748; -- NEA SALAMIS (Chypre) -> Nea Salamis Famagusta
UPDATE team SET logo_path = '800.png' WHERE id = 800; -- OMONIA ARADIPPOU (Chypre) -> Omonia Aradippou
UPDATE team SET logo_path = '855.png' WHERE id = 855; -- DINAMO ZAGREB (CRO) -> Dinamo Zagreb
UPDATE team SET logo_path = '938.png' WHERE id = 938; -- HNK RIJEKA (CRO) -> Rijeka
UPDATE team SET logo_path = '950.png' WHERE id = 950; -- NK VARAZDIN (CRO) -> Varaždin
UPDATE team SET logo_path = '417.png' WHERE id = 417; -- GORICA (Croatie) -> Gorica
UPDATE team SET logo_path = '453.png' WHERE id = 453; -- HAJDUK SPLIT (Croatie) -> Hajduk Split
UPDATE team SET logo_path = '411.png' WHERE id = 411; -- ISTRA 1961 (Croatie) -> Istra 1961
UPDATE team SET logo_path = '418.png' WHERE id = 418; -- OSIJEK (Croatie) -> Osijek
UPDATE team SET logo_path = '465.png' WHERE id = 465; -- RUDES (Croatie) -> Rudeš
UPDATE team SET logo_path = '376.png' WHERE id = 376; -- SLAVEN BELUPO (Croatie) -> Slaven Belupo Koprivnica
UPDATE team SET logo_path = '951.png' WHERE id = 951; -- APOLLON LIMASSOL (CYP) -> Apollon Limassol
UPDATE team SET logo_path = '882.png' WHERE id = 882; -- FC PAFOS (CYP) -> Pafos
UPDATE team SET logo_path = '891.png' WHERE id = 891; -- VIKTORIA PLZEN (CZE) -> Viktoria Plzeň
UPDATE team SET logo_path = '851.png' WHERE id = 851; -- AGF AARHUS (DAN) -> AGF Aarhus
UPDATE team SET logo_path = '881.png' WHERE id = 881; -- FC MIDTJYLLAND (DAN) -> FC Midtjylland
UPDATE team SET logo_path = '295.png' WHERE id = 295; -- BRONDBY (Danemark) -> Brøndby
UPDATE team SET logo_path = '366.png' WHERE id = 366; -- LYNGBY (Danemark) -> Lyngby
UPDATE team SET logo_path = '361.png' WHERE id = 361; -- RANDERS (Danemark) -> Randers FC
UPDATE team SET logo_path = '317.png' WHERE id = 317; -- SONDERJYSKE (Danemark) -> Sønderjyske
UPDATE team SET logo_path = '267.png' WHERE id = 267; -- VIBORG (Danemark) -> Viborg
UPDATE team SET logo_path = '963.png' WHERE id = 963; -- HIBERNIAN FC (ECO) -> Hibernian
UPDATE team SET logo_path = '961.png' WHERE id = 961; -- MOTHERWELL FC (ECO) -> Motherwell
UPDATE team SET logo_path = '409.png' WHERE id = 409; -- ABERDEEN (Ecosse) -> Aberdeen
UPDATE team SET logo_path = '479.png' WHERE id = 479; -- DUNDEE FC (Ecosse) -> Dundee
UPDATE team SET logo_path = '385.png' WHERE id = 385; -- DUNDEE UTD (Ecosse) -> Dundee United
UPDATE team SET logo_path = '397.png' WHERE id = 397; -- FALKIRK (Ecosse) -> Falkirk
UPDATE team SET logo_path = '386.png' WHERE id = 386; -- RANGERS (Ecosse) -> Rangers
UPDATE team SET logo_path = '426.png' WHERE id = 426; -- ST. JOHNSTONE (Ecosse) -> St Johnstone
UPDATE team SET logo_path = '398.png' WHERE id = 398; -- ST. MIRREN (Ecosse) -> St Mirren
UPDATE team SET logo_path = '7.png' WHERE id = 7; -- BOHEMIANS (Eire) -> Bohemians
UPDATE team SET logo_path = '584.png' WHERE id = 584; -- CASTLEBAR CELTIC (Eire) -> Castlebar Celtic
UPDATE team SET logo_path = '583.png' WHERE id = 583; -- COLLEGE CORINTHIANS (Eire) -> College Corinthians
UPDATE team SET logo_path = '1.png' WHERE id = 1; -- DERRY CITY (Eire) -> Derry City
UPDATE team SET logo_path = '9.png' WHERE id = 9; -- DUNDALK (Eire) -> Dundalk
UPDATE team SET logo_path = '597.png' WHERE id = 597; -- KERRY (Eire) -> Kerry
UPDATE team SET logo_path = '10.png' WHERE id = 10; -- SHAMROCK ROVERS (Eire) -> Shamrock Rovers
UPDATE team SET logo_path = '6.png' WHERE id = 6; -- SHELBOURNE (Eire) -> Shelbourne
UPDATE team SET logo_path = '2.png' WHERE id = 2; -- SLIGO ROVERS (Eire) -> Sligo Rovers
UPDATE team SET logo_path = '571.png' WHERE id = 571; -- UC DUBLIN (Eire) -> UCD
UPDATE team SET logo_path = '5.png' WHERE id = 5; -- WATERFORD (Eire) -> Waterford
UPDATE team SET logo_path = '895.png' WHERE id = 895; -- BOHEMIAN FC (EIRE) -> Bohemians
UPDATE team SET logo_path = '954.png' WHERE id = 954; -- SHELBOURNE FC (EIRE) -> Shelbourne
UPDATE team SET logo_path = '919.png' WHERE id = 919; -- FC LEVADIA TALLINN (EST) -> Levadia Tallinn
UPDATE team SET logo_path = '833.png' WHERE id = 833; -- FLORA TALLINN (EST) -> Flora Tallinn
UPDATE team SET logo_path = '928.png' WHERE id = 928; -- NOMME KALJU (EST) -> Nõmme Kalju
UPDATE team SET logo_path = '921.png' WHERE id = 921; -- PAIDE LINNAMEESKOND (EST) -> Paide Linnameeskond
UPDATE team SET logo_path = '69.png' WHERE id = 69; -- KURESSAARE (Estonie) -> Kuressaare
UPDATE team SET logo_path = '33.png' WHERE id = 33; -- NOMME UTD (Estonie) -> Nõmme United
UPDATE team SET logo_path = '103.png' WHERE id = 103; -- TALLINNA KALEV (Estonie) -> Tallinna Kalev
UPDATE team SET logo_path = '101.png' WHERE id = 101; -- TARTU WELCO (Estonie) -> Tartu Welco
UPDATE team SET logo_path = '848.png' WHERE id = 848; -- KI KLAKSVIK (FER) -> KÍ Klaksvík
UPDATE team SET logo_path = '932.png' WHERE id = 932; -- NSI RUNAVIK (FER) -> NSÍ Runavík
UPDATE team SET logo_path = '906.png' WHERE id = 906; -- FC ILVES (FIN) -> Ilves
UPDATE team SET logo_path = '941.png' WHERE id = 941; -- HJK HELSINKI (FIN) -> HJK Helsinki
UPDATE team SET logo_path = '187.png' WHERE id = 187; -- EBK (Finlande) -> EBK
UPDATE team SET logo_path = '137.png' WHERE id = 137; -- GNISTAN (Finlande) -> Gnistan
UPDATE team SET logo_path = '185.png' WHERE id = 185; -- HAKA (Finlande) -> Haka
UPDATE team SET logo_path = '190.png' WHERE id = 190; -- HONKA (Finlande) -> Honka
UPDATE team SET logo_path = '126.png' WHERE id = 126; -- INTER TURKU (Finlande) -> Inter Turku
UPDATE team SET logo_path = '188.png' WHERE id = 188; -- JAPS (Finlande) -> JäPS
UPDATE team SET logo_path = '133.png' WHERE id = 133; -- JARO (Finlande) -> Jaro
UPDATE team SET logo_path = '132.png' WHERE id = 132; -- KUPS (Finlande) -> KuPS
UPDATE team SET logo_path = '134.png' WHERE id = 134; -- LAHTI (Finlande) -> Lahti
UPDATE team SET logo_path = '135.png' WHERE id = 135; -- MARIEHAMN (Finlande) -> IFK Mariehamn
UPDATE team SET logo_path = '189.png' WHERE id = 189; -- MYPA (Finlande) -> MyPa
UPDATE team SET logo_path = '186.png' WHERE id = 186; -- SJK AKATEMIA (Finlande) -> SJK Akatemia
UPDATE team SET logo_path = '136.png' WHERE id = 136; -- TPS TURKU (Finlande) -> TPS Turku
UPDATE team SET logo_path = '191.png' WHERE id = 191; -- VJS (Finlande) -> VJS
UPDATE team SET logo_path = '127.png' WHERE id = 127; -- VPS (Finlande) -> VPS
UPDATE team SET logo_path = '918.png' WHERE id = 918; -- CAERNARFON TOWN (GAL) -> Caernarfon Town
UPDATE team SET logo_path = '893.png' WHERE id = 893; -- CONNAH'S QUAY NOMADS (GAL) -> Connah's Quay Nomads
UPDATE team SET logo_path = '930.png' WHERE id = 930; -- PEN-Y-BONT (GAL) -> Pen-y-Bont
UPDATE team SET logo_path = '380.png' WHERE id = 380; -- AMMANFORD (Galles) -> Ammanford
UPDATE team SET logo_path = '378.png' WHERE id = 378; -- CAMBRIAN UNITED (Galles) -> Cambrian United
UPDATE team SET logo_path = '381.png' WHERE id = 381; -- COLWYN BAY (Galles) -> Colwyn Bay
UPDATE team SET logo_path = '384.png' WHERE id = 384; -- FLINT (Galles) -> Flint
UPDATE team SET logo_path = '383.png' WHERE id = 383; -- LLANDUDNO (Galles) -> Llandudno
UPDATE team SET logo_path = '430.png' WHERE id = 430; -- PENYBONT (Galles) -> Pen-y-Bont
UPDATE team SET logo_path = '834.png' WHERE id = 834; -- FC IBERIA 1999 (GEO) -> Iberia 1999
UPDATE team SET logo_path = '27.png' WHERE id = 27; -- DILA GORI (Georgie) -> Dila Gori
UPDATE team SET logo_path = '22.png' WHERE id = 22; -- GAGRA (Georgie) -> Gagra
UPDATE team SET logo_path = '422.png' WHERE id = 422; -- GURIA (Georgie) -> Guria Lanchkhuti
UPDATE team SET logo_path = '70.png' WHERE id = 70; -- MESHAKHTE TKIBULI (Georgie) -> Meshakhte Tkibuli
UPDATE team SET logo_path = '406.png' WHERE id = 406; -- ODISHI 1919 (Georgie) -> Odishi 1919
UPDATE team SET logo_path = '63.png' WHERE id = 63; -- RUSTAVI (Georgie) -> Rustavi
UPDATE team SET logo_path = '802.png' WHERE id = 802; -- SAMTREDIA (Georgie) -> Samtredia
UPDATE team SET logo_path = '21.png' WHERE id = 21; -- SPAERI (Georgie) -> Spaeri
UPDATE team SET logo_path = '450.png' WHERE id = 450; -- TELAVI (Georgie) -> Telavi
UPDATE team SET logo_path = '835.png' WHERE id = 835; -- LINCOLN RED IMPS (GIB) -> Lincoln Red Imps
UPDATE team SET logo_path = '640.png' WHERE id = 640; -- COLLEGE 1975 FC (Gibraltar) -> College 1975
UPDATE team SET logo_path = '694.png' WHERE id = 694; -- EUROPA FC (Gibraltar) -> Europa FC
UPDATE team SET logo_path = '710.png' WHERE id = 710; -- EUROPA POINT (Gibraltar) -> Europa Point
UPDATE team SET logo_path = '695.png' WHERE id = 695; -- GLACIS UNITED (Gibraltar) -> Glacis United
UPDATE team SET logo_path = '639.png' WHERE id = 639; -- HOUND DOGS (Gibraltar) -> Hound Dogs
UPDATE team SET logo_path = '662.png' WHERE id = 662; -- LIONS GIBRALTAR (Gibraltar) -> Lions Gibraltar
UPDATE team SET logo_path = '709.png' WHERE id = 709; -- LYNX (Gibraltar) -> Lynx
UPDATE team SET logo_path = '264.png' WHERE id = 264; -- UJPEST (Hongrie) -> Újpest
UPDATE team SET logo_path = '339.png' WHERE id = 339; -- VASAS (Hongrie) -> Vasas
UPDATE team SET logo_path = '368.png' WHERE id = 368; -- ZALAEGERSZEG (Hongrie) -> Zalaegerszeg
UPDATE team SET logo_path = '91.png' WHERE id = 91; -- B36 TORSHAVN (Iles Feroe) -> B36 Tórshavn
UPDATE team SET logo_path = '158.png' WHERE id = 158; -- FC SUDUROY (Iles Feroe) -> Suðuroy
UPDATE team SET logo_path = '96.png' WHERE id = 96; -- HB TORSHAVN (Iles Feroe) -> HB Tórshavn
UPDATE team SET logo_path = '159.png' WHERE id = 159; -- HOYVIK (Iles Feroe) -> Hoyvík
UPDATE team SET logo_path = '95.png' WHERE id = 95; -- SKALA ITROTTARFELAG (Iles Feroe) -> Skála
UPDATE team SET logo_path = '573.png' WHERE id = 573; -- BANGOR FC (Irlande Nord) -> Bangor
UPDATE team SET logo_path = '486.png' WHERE id = 486; -- CLIFTONVILLE (Irlande Nord) -> Cliftonville
UPDATE team SET logo_path = '494.png' WHERE id = 494; -- COLERAINE (Irlande Nord) -> Coleraine
UPDATE team SET logo_path = '487.png' WHERE id = 487; -- CRUSADERS (Irlande Nord) -> Crusaders
UPDATE team SET logo_path = '492.png' WHERE id = 492; -- PORTADOWN (Irlande Nord) -> Portadown
UPDATE team SET logo_path = '912.png' WHERE id = 912; -- GLENTORAN FC (IRN) -> Glentoran
UPDATE team SET logo_path = '838.png' WHERE id = 838; -- LARNE FC (IRN) -> Larne
UPDATE team SET logo_path = '929.png' WHERE id = 929; -- LINFIELD FC (IRN) -> Linfield
UPDATE team SET logo_path = '167.png' WHERE id = 167; -- AFTURELDING (Islande) -> Afturelding
UPDATE team SET logo_path = '147.png' WHERE id = 147; -- BREIDABLIK (Islande) -> Breiðablik
UPDATE team SET logo_path = '174.png' WHERE id = 174; -- FYLKIR (Islande) -> Fylkir
UPDATE team SET logo_path = '169.png' WHERE id = 169; -- GRINDAVIK (Islande) -> Grindavík
UPDATE team SET logo_path = '173.png' WHERE id = 173; -- GROTTA (Islande) -> Grótta
UPDATE team SET logo_path = '151.png' WHERE id = 151; -- KEFLAVIK (Islande) -> Keflavík
UPDATE team SET logo_path = '150.png' WHERE id = 150; -- KR REYKJAVIK (Islande) -> KR Reykjavík
UPDATE team SET logo_path = '149.png' WHERE id = 149; -- STJARNAN (Islande) -> Stjarnan
UPDATE team SET logo_path = '146.png' WHERE id = 146; -- VIKINGUR REYKJAVIK (Islande) -> Víkingur Reykjavík
UPDATE team SET logo_path = '976.png' WHERE id = 976; -- BEITAR JERUSALEM (ISR) -> Beitar Jerusalem
UPDATE team SET logo_path = '977.png' WHERE id = 977; -- HAPOEL TEL AVIV (ISR) -> Hapoel Tel Aviv BC
UPDATE team SET logo_path = '972.png' WHERE id = 972; -- HAPÖEL TEL AVIV (ISR) -> Hapoel Tel Aviv BC
UPDATE team SET logo_path = '883.png' WHERE id = 883; -- MACCABI TEL AVIV (ISR) -> Maccabi Tel Aviv
UPDATE team SET logo_path = '717.png' WHERE id = 717; -- HAPOEL JERUSALEM (Israel) -> Hapoel Jerusalem
UPDATE team SET logo_path = '674.png' WHERE id = 674; -- IRONI TIBERIAS (Israel) -> Ironi Tiberias
UPDATE team SET logo_path = '43.png' WHERE id = 43; -- AKTOBE (Kazakhstan) -> Aktobe
UPDATE team SET logo_path = '39.png' WHERE id = 39; -- ALTAI (Kazakhstan) -> Altai Öskemen
UPDATE team SET logo_path = '66.png' WHERE id = 66; -- ATYRAU (Kazakhstan) -> Atyrau
UPDATE team SET logo_path = '83.png' WHERE id = 83; -- FC ASTANA (Kazakhstan) -> Astana
UPDATE team SET logo_path = '40.png' WHERE id = 40; -- KAIRAT ALMATY (Kazakhstan) -> Kairat Almaty
UPDATE team SET logo_path = '64.png' WHERE id = 64; -- KYZYLZHAR (Kazakhstan) -> Kyzylzhar Petropavl
UPDATE team SET logo_path = '67.png' WHERE id = 67; -- ULYTAU (Kazakhstan) -> Ulytau
UPDATE team SET logo_path = '74.png' WHERE id = 74; -- ZHENIS (Kazakhstan) -> Jeńis
UPDATE team SET logo_path = '840.png' WHERE id = 840; -- KF DRITA (KOS) -> Drita
UPDATE team SET logo_path = '940.png' WHERE id = 940; -- KF DUKAGJINI (KOS) -> Dukagjini
UPDATE team SET logo_path = '937.png' WHERE id = 937; -- KF MALISHEVA (KOS) -> Malisheva
UPDATE team SET logo_path = '576.png' WHERE id = 576; -- DRENICA (Kosovo) -> Drenica
UPDATE team SET logo_path = '613.png' WHERE id = 613; -- FC BALLKANI (Kosovo) -> Ballkani
UPDATE team SET logo_path = '560.png' WHERE id = 560; -- FERONIKELI (Kosovo) -> Feronikeli
UPDATE team SET logo_path = '561.png' WHERE id = 561; -- GJILANI (Kosovo) -> Gjilani
UPDATE team SET logo_path = '601.png' WHERE id = 601; -- KF LLAPI (Kosovo) -> Llapi
UPDATE team SET logo_path = '612.png' WHERE id = 612; -- PRISHTINA (Kosovo) -> Prishtina
UPDATE team SET logo_path = '578.png' WHERE id = 578; -- VUSHTRRIA (Kosovo) -> Vushtrria
UPDATE team SET logo_path = '49.png' WHERE id = 49; -- FK LIEPAJA (Lettonie) -> Liepāja
UPDATE team SET logo_path = '75.png' WHERE id = 75; -- JELGAVA (Lettonie) -> Jelgava
UPDATE team SET logo_path = '203.png' WHERE id = 203; -- LEEVON PPK (Lettonie) -> Leevon
UPDATE team SET logo_path = '205.png' WHERE id = 205; -- MARUPE (Lettonie) -> Mārupe
UPDATE team SET logo_path = '201.png' WHERE id = 201; -- METTA (Lettonie) -> Metta
UPDATE team SET logo_path = '25.png' WHERE id = 25; -- OGRE UNITED (Lettonie) -> Ogre United
UPDATE team SET logo_path = '24.png' WHERE id = 24; -- RFS (Lettonie) -> RFS
UPDATE team SET logo_path = '50.png' WHERE id = 50; -- RIGA FC (Lettonie) -> Riga FC
UPDATE team SET logo_path = '204.png' WHERE id = 204; -- RIGA MARINERS (Lettonie) -> Riga Mariners
UPDATE team SET logo_path = '202.png' WHERE id = 202; -- SPEKS (Lettonie) -> Spēks
UPDATE team SET logo_path = '86.png' WHERE id = 86; -- SUPER NOVA (Lettonie) -> Super Nova
UPDATE team SET logo_path = '206.png' WHERE id = 206; -- VALMIERA (Lettonie) -> Valmiera
UPDATE team SET logo_path = '920.png' WHERE id = 920; -- FC HEGELMANN (LIT) -> Hegelmann
UPDATE team SET logo_path = '915.png' WHERE id = 915; -- ZALGIRIS VILNIUS (LIT) -> Žalgiris Vilnius
UPDATE team SET logo_path = '183.png' WHERE id = 183; -- ATAKA VILNIUS (Lituanie) -> Ataka Vilnius
UPDATE team SET logo_path = '182.png' WHERE id = 182; -- ATMOSFERA (Lituanie) -> Atmosfera
UPDATE team SET logo_path = '16.png' WHERE id = 16; -- BANGA (Lituanie) -> Banga Gargždai
UPDATE team SET logo_path = '181.png' WHERE id = 181; -- DAINAVA ALYTUS (Lituanie) -> Dainava
UPDATE team SET logo_path = '178.png' WHERE id = 178; -- FK MINIJA (Lituanie) -> Minija
UPDATE team SET logo_path = '13.png' WHERE id = 13; -- FK PANEVEZYS (Lituanie) -> Panevėžys
UPDATE team SET logo_path = '176.png' WHERE id = 176; -- GARLIAVA (Lituanie) -> Garliava
UPDATE team SET logo_path = '19.png' WHERE id = 19; -- KAUNO ZALGIRIS (Lituanie) -> Kauno Žalgiris
UPDATE team SET logo_path = '179.png' WHERE id = 179; -- RITERIAI (Lituanie) -> Riteriai
UPDATE team SET logo_path = '14.png' WHERE id = 14; -- SUDUVA (Lituanie) -> Sūduva
UPDATE team SET logo_path = '849.png' WHERE id = 849; -- FC ATERT BISSEN (LUX) -> Atert Bissen
UPDATE team SET logo_path = '905.png' WHERE id = 905; -- FC DIFFERDANGE 03 (LUX) -> Differdange 03
UPDATE team SET logo_path = '908.png' WHERE id = 908; -- MONDORF LES BAINS (LUX) -> Mondorf-les-Bains
UPDATE team SET logo_path = '441.png' WHERE id = 441; -- HOSTERT (Luxembourg) -> Hostert
UPDATE team SET logo_path = '400.png' WHERE id = 400; -- JEUNESSE ESCH (Luxembourg) -> Jeunesse Esch
UPDATE team SET logo_path = '399.png' WHERE id = 399; -- PROGRES NIEDERKORN (Luxembourg) -> Progrès Niederkorn
UPDATE team SET logo_path = '438.png' WHERE id = 438; -- RUMELANGE (Luxembourg) -> Rumelange
UPDATE team SET logo_path = '470.png' WHERE id = 470; -- UNA STRASSEN (Luxembourg) -> UNA Strassen
UPDATE team SET logo_path = '471.png' WHERE id = 471; -- VICTORIA ROSPORT (Luxembourg) -> Victoria Rosport
UPDATE team SET logo_path = '927.png' WHERE id = 927; -- FK SHKENDIJA (MAC) -> Shkëndija
UPDATE team SET logo_path = '839.png' WHERE id = 839; -- FK VARDAR SKOPJE (MAC) -> Vardar
UPDATE team SET logo_path = '262.png' WHERE id = 262; -- ARSIMI (Macedoine) -> Arsimi
UPDATE team SET logo_path = '334.png' WHERE id = 334; -- BASHKIMI (Macedoine) -> Bashkimi
UPDATE team SET logo_path = '285.png' WHERE id = 285; -- SHKENDIJA HARACINE (Macedoine) -> Shkëndija Haraçinë
UPDATE team SET logo_path = '284.png' WHERE id = 284; -- STRUGA (Macedoine) -> Struga
UPDATE team SET logo_path = '261.png' WHERE id = 261; -- TIKVES (Macedoine) -> Tikvesh
UPDATE team SET logo_path = '832.png' WHERE id = 832; -- FLORIANA FC (MAL) -> Floriana
UPDATE team SET logo_path = '933.png' WHERE id = 933; -- HAMRUN SPARTANS (MAL) -> Ħamrun Spartans
UPDATE team SET logo_path = '916.png' WHERE id = 916; -- MARSAXLOKK FC (MAL) -> Marsaxlokk
UPDATE team SET logo_path = '945.png' WHERE id = 945; -- VALLETTA FC (MAL) -> Valletta
UPDATE team SET logo_path = '566.png' WHERE id = 566; -- BALZAN (Malte) -> Balzan
UPDATE team SET logo_path = '625.png' WHERE id = 625; -- BIRKIRKARA (Malte) -> Birkirkara
UPDATE team SET logo_path = '618.png' WHERE id = 618; -- HIBERNIANS (Malte) -> Hibernians
UPDATE team SET logo_path = '630.png' WHERE id = 630; -- MOSTA FC (Malte) -> Mosta
UPDATE team SET logo_path = '842.png' WHERE id = 842; -- CS PETROCUB HINCESTI (MOL) -> Petrocub Hîncești
UPDATE team SET logo_path = '900.png' WHERE id = 900; -- FC MILSAMI ORHEI (MOL) -> Milsami Orhei
UPDATE team SET logo_path = '872.png' WHERE id = 872; -- SHERIFF TIRASPOL (MOL) -> Sheriff Tiraspol
UPDATE team SET logo_path = '957.png' WHERE id = 957; -- ZIMBRU CHISINAU (MOL) -> Zimbru Chișinău
UPDATE team SET logo_path = '193.png' WHERE id = 193; -- BALTI (Moldavie) -> Bălți
UPDATE team SET logo_path = '200.png' WHERE id = 200; -- DACIA BUIUCANI (Moldavie) -> Dacia Buiucani
UPDATE team SET logo_path = '198.png' WHERE id = 198; -- REAL SIRETI (Moldavie) -> Real Sireți
UPDATE team SET logo_path = '910.png' WHERE id = 910; -- MORNAR BAR (MON) -> Mornar
UPDATE team SET logo_path = '847.png' WHERE id = 847; -- SUTJESKA NIKSIC (MON) -> Sutjeska
UPDATE team SET logo_path = '459.png' WHERE id = 459; -- ARSENAL TIVAT (Montenegro) -> Arsenal Tivat
UPDATE team SET logo_path = '416.png' WHERE id = 416; -- BOKELJ (Montenegro) -> Bokelj
UPDATE team SET logo_path = '460.png' WHERE id = 460; -- JEZERO (Montenegro) -> Jezero
UPDATE team SET logo_path = '415.png' WHERE id = 415; -- PETROVAC (Montenegro) -> Petrovac
UPDATE team SET logo_path = '953.png' WHERE id = 953; -- SK BRANN (NOR) -> Brann
UPDATE team SET logo_path = '866.png' WHERE id = 866; -- VIKING FK (NOR) -> Viking
UPDATE team SET logo_path = '100.png' WHERE id = 100; -- AALESUND (Norvege) -> Aalesund
UPDATE team SET logo_path = '54.png' WHERE id = 54; -- BJARG (Norvege) -> Bjarg
UPDATE team SET logo_path = '87.png' WHERE id = 87; -- BRYNE (Norvege) -> Bryne
UPDATE team SET logo_path = '99.png' WHERE id = 99; -- EGERSUND (Norvege) -> Egersund
UPDATE team SET logo_path = '97.png' WHERE id = 97; -- FREDRIKSTAD (Norvege) -> Fredrikstad
UPDATE team SET logo_path = '61.png' WHERE id = 61; -- HAMKAM (Norvege) -> Hamarkameratene
UPDATE team SET logo_path = '47.png' WHERE id = 47; -- KFUM OSLO (Norvege) -> KFUM-Kameratene Oslo
UPDATE team SET logo_path = '108.png' WHERE id = 108; -- KRISTIANSUND (Norvege) -> Kristiansund
UPDATE team SET logo_path = '28.png' WHERE id = 28; -- MOLDE (Norvege) -> Molde
UPDATE team SET logo_path = '88.png' WHERE id = 88; -- ROSENBORG (Norvege) -> Rosenborg
UPDATE team SET logo_path = '98.png' WHERE id = 98; -- SARPSBORG 08 (Norvege) -> Sarpsborg 08
UPDATE team SET logo_path = '111.png' WHERE id = 111; -- START (Norvege) -> Start
UPDATE team SET logo_path = '79.png' WHERE id = 79; -- TROMSDALEN (Norvege) -> Tromsdalen
UPDATE team SET logo_path = '112.png' WHERE id = 112; -- VALERENGA (Norvege) -> Vålerenga
UPDATE team SET logo_path = '532.png' WHERE id = 532; -- AJAX (Pays-Bas) -> Ajax
UPDATE team SET logo_path = '480.png' WHERE id = 480; -- CAMBUUR (Pays-Bas) -> Cambuur
UPDATE team SET logo_path = '518.png' WHERE id = 518; -- DEN HAAG (Pays-Bas) -> ADO Den Haag
UPDATE team SET logo_path = '481.png' WHERE id = 481; -- EXCELSIOR (Pays-Bas) -> Excelsior
UPDATE team SET logo_path = '526.png' WHERE id = 526; -- FEYENOORD (Pays-Bas) -> Feyenoord
UPDATE team SET logo_path = '529.png' WHERE id = 529; -- GRONINGEN (Pays-Bas) -> Groningen
UPDATE team SET logo_path = '538.png' WHERE id = 538; -- HEERENVEEN (Pays-Bas) -> Heerenveen
UPDATE team SET logo_path = '510.png' WHERE id = 510; -- PSV (Pays-Bas) -> PSV Eindhoven
UPDATE team SET logo_path = '525.png' WHERE id = 525; -- SPARTA ROTTERDAM (Pays-Bas) -> Sparta Rotterdam
UPDATE team SET logo_path = '500.png' WHERE id = 500; -- TELSTAR (Pays-Bas) -> Telstar
UPDATE team SET logo_path = '530.png' WHERE id = 530; -- UTRECHT (Pays-Bas) -> Utrecht
UPDATE team SET logo_path = '531.png' WHERE id = 531; -- ZWOLLE (Pays-Bas) -> Zwolle
UPDATE team SET logo_path = '949.png' WHERE id = 949; -- GKS KATOWICE (POL) -> GKS Katowice
UPDATE team SET logo_path = '852.png' WHERE id = 852; -- LECH POZNAN (POL) -> Lech Poznań
UPDATE team SET logo_path = '944.png' WHERE id = 944; -- RAKOW CZESTOCHOWA (POL) -> Raków Częstochowa
UPDATE team SET logo_path = '310.png' WHERE id = 310; -- CRACOVIA (Pologne) -> Cracovia
UPDATE team SET logo_path = '290.png' WHERE id = 290; -- GORNIK ZABRZE (Pologne) -> Górnik Zabrze
UPDATE team SET logo_path = '271.png' WHERE id = 271; -- POGON SZCZECIN (Pologne) -> Pogoń Szczecin
UPDATE team SET logo_path = '265.png' WHERE id = 265; -- RADOMIAK RADOM (Pologne) -> Radomiak Radom
UPDATE team SET logo_path = '266.png' WHERE id = 266; -- WIECZYSTA KRAKOW (Pologne) -> Wieczysta Kraków
UPDATE team SET logo_path = '885.png' WHERE id = 885; -- SL BENFICA (POR) -> Benfica
UPDATE team SET logo_path = '544.png' WHERE id = 544; -- ALVERCA (Portugal) -> Alverca
UPDATE team SET logo_path = '509.png' WHERE id = 509; -- AROUCA (Portugal) -> Arouca
UPDATE team SET logo_path = '502.png' WHERE id = 502; -- CASA PIA (Portugal) -> Casa Pia
UPDATE team SET logo_path = '488.png' WHERE id = 488; -- ESTORIL (Portugal) -> Estoril Praia
UPDATE team SET logo_path = '489.png' WHERE id = 489; -- FAMALICAO (Portugal) -> Famalicao
UPDATE team SET logo_path = '554.png' WHERE id = 554; -- GIL VICENTE (Portugal) -> Gil Vicente
UPDATE team SET logo_path = '501.png' WHERE id = 501; -- MARITIMO (Portugal) -> Marítimo
UPDATE team SET logo_path = '306.png' WHERE id = 306; -- RODINA MOSCOW (Russie) -> Rodina Moscow
UPDATE team SET logo_path = '753.png' WHERE id = 753; -- CAILUNGO (San Marin) -> Cailungo
UPDATE team SET logo_path = '761.png' WHERE id = 761; -- COSMOS (San Marin) -> Cosmos
UPDATE team SET logo_path = '754.png' WHERE id = 754; -- DOMAGNANO (San Marin) -> Domagnano
UPDATE team SET logo_path = '758.png' WHERE id = 758; -- FAETANO (San Marin) -> Faetano
UPDATE team SET logo_path = '760.png' WHERE id = 760; -- FIORENTINO (San Marin) -> Fiorentino
UPDATE team SET logo_path = '755.png' WHERE id = 755; -- FOLGORE (San Marin) -> Folgore
UPDATE team SET logo_path = '762.png' WHERE id = 762; -- LIBERTAS (San Marin) -> Libertas
UPDATE team SET logo_path = '756.png' WHERE id = 756; -- MURATA (San Marin) -> Murata
UPDATE team SET logo_path = '783.png' WHERE id = 783; -- PENNAROSSA (San Marin) -> Pennarossa
UPDATE team SET logo_path = '763.png' WHERE id = 763; -- SAN GIOVANNI (San Marin) -> San Giovanni
UPDATE team SET logo_path = '764.png' WHERE id = 764; -- TRE PENNE (San Marin) -> Tre Penne
UPDATE team SET logo_path = '247.png' WHERE id = 247; -- CUKARICKI (Serbie) -> Čukarički
UPDATE team SET logo_path = '248.png' WHERE id = 248; -- IMT NOVI BEOGRAD (Serbie) -> IMT Novi Beograd
UPDATE team SET logo_path = '229.png' WHERE id = 229; -- NOVI PAZAR (Serbie) -> Novi Pazar
UPDATE team SET logo_path = '252.png' WHERE id = 252; -- OFK BEOGRAD (Serbie) -> OFK Beograd
UPDATE team SET logo_path = '232.png' WHERE id = 232; -- PARTIZAN BELGRADE (Serbie) -> Partizan Belgrade
UPDATE team SET logo_path = '230.png' WHERE id = 230; -- RADNICKI 1923 (Serbie) -> Radnički 1923
UPDATE team SET logo_path = '216.png' WHERE id = 216; -- RADNICKI NIS (Serbie) -> Radnički Niš
UPDATE team SET logo_path = '215.png' WHERE id = 215; -- ZELEZNICAR PANCEVO (Serbie) -> Železničar Pančevo
UPDATE team SET logo_path = '231.png' WHERE id = 231; -- ZEMUN (Serbie) -> Zemun
UPDATE team SET logo_path = '302.png' WHERE id = 302; -- KOMARNO (Slovaquie) -> Komárno
UPDATE team SET logo_path = '344.png' WHERE id = 344; -- KOSICE (Slovaquie) -> Kosice
UPDATE team SET logo_path = '314.png' WHERE id = 314; -- MICHALOVCE (Slovaquie) -> Michalovce
UPDATE team SET logo_path = '341.png' WHERE id = 341; -- RUZOMBEROK (Slovaquie) -> Ružomberok
UPDATE team SET logo_path = '304.png' WHERE id = 304; -- SKALICA (Slovaquie) -> Skalica
UPDATE team SET logo_path = '354.png' WHERE id = 354; -- SLOVAN BRATISLAVA (Slovaquie) -> Slovan Bratislava
UPDATE team SET logo_path = '301.png' WHERE id = 301; -- TRENCIN (Slovaquie) -> Trenčín
UPDATE team SET logo_path = '242.png' WHERE id = 242; -- MARIBOR (Slovenie) -> Maribor
UPDATE team SET logo_path = '220.png' WHERE id = 220; -- MURA (Slovenie) -> Mura
UPDATE team SET logo_path = '211.png' WHERE id = 211; -- NAFTA (Slovenie) -> Nafta
UPDATE team SET logo_path = '227.png' WHERE id = 227; -- RADOMLJE (Slovenie) -> Radomlje
UPDATE team SET logo_path = '925.png' WHERE id = 925; -- AC VIRTUS (SM) -> Virtus
UPDATE team SET logo_path = '837.png' WHERE id = 837; -- TRE FIORI FC (SM) -> Tre Fiori
UPDATE team SET logo_path = '145.png' WHERE id = 145; -- BROMMAPOJKARNA (Suede) -> Brommapojkarna
UPDATE team SET logo_path = '143.png' WHERE id = 143; -- ELFSBORG (Suede) -> Elfsborg
UPDATE team SET logo_path = '104.png' WHERE id = 104; -- GAIS (Suede) -> GAIS
UPDATE team SET logo_path = '144.png' WHERE id = 144; -- HACKEN (Suede) -> Häcken
UPDATE team SET logo_path = '139.png' WHERE id = 139; -- HALMSTAD (Suede) -> Halmstad
UPDATE team SET logo_path = '140.png' WHERE id = 140; -- KALMAR (Suede) -> Kalmar
UPDATE team SET logo_path = '142.png' WHERE id = 142; -- ORGRYTE (Suede) -> Örgryte
UPDATE team SET logo_path = '114.png' WHERE id = 114; -- SIRIUS (Suede) -> Sirius
UPDATE team SET logo_path = '141.png' WHERE id = 141; -- VASTERAS SK (Suede) -> Västerås
UPDATE team SET logo_path = '960.png' WHERE id = 960; -- FC SION (SUI) -> Sion
UPDATE team SET logo_path = '297.png' WHERE id = 297; -- GRASSHOPPERS (Suisse) -> Grasshoppers
UPDATE team SET logo_path = '327.png' WHERE id = 327; -- LUGANO (Suisse) -> Lugano
UPDATE team SET logo_path = '311.png' WHERE id = 311; -- LUZERN (Suisse) -> Luzern
UPDATE team SET logo_path = '298.png' WHERE id = 298; -- SERVETTE (Suisse) -> Servette
UPDATE team SET logo_path = '320.png' WHERE id = 320; -- SION (Suisse) -> Sion
UPDATE team SET logo_path = '312.png' WHERE id = 312; -- THUN (Suisse) -> Thun
UPDATE team SET logo_path = '328.png' WHERE id = 328; -- VADUZ (Suisse) -> Vaduz
UPDATE team SET logo_path = '319.png' WHERE id = 319; -- YOUNG BOYS (Suisse) -> Young Boys
UPDATE team SET logo_path = '330.png' WHERE id = 330; -- ZURICH (Suisse) -> Zürich
UPDATE team SET logo_path = '871.png' WHERE id = 871; -- MSK ZILINA (SVK) -> Žilina
UPDATE team SET logo_path = '974.png' WHERE id = 974; -- SPARTAK TRNAVA (SVK) -> Spartak Trnava
UPDATE team SET logo_path = '975.png' WHERE id = 975; -- FC KOPER (SVN) -> Koper
UPDATE team SET logo_path = '873.png' WHERE id = 873; -- NK ALUMINIJ (SVN) -> Aluminij
UPDATE team SET logo_path = '952.png' WHERE id = 952; -- NK BRAVO (SVN) -> Bravo
UPDATE team SET logo_path = '857.png' WHERE id = 857; -- NK CELJE (SVN) -> Celje
UPDATE team SET logo_path = '948.png' WHERE id = 948; -- IFK GOTEBORG (SWE) -> IFK Göteborg
UPDATE team SET logo_path = '850.png' WHERE id = 850; -- MJALLBY AIF (SWE) -> Mjällby
UPDATE team SET logo_path = '359.png' WHERE id = 359; -- ARTIS BRNO (Tchequie) -> Artis Brno
UPDATE team SET logo_path = '355.png' WHERE id = 355; -- HRADEC KRALOVE (Tchequie) -> Hradec Králové
UPDATE team SET logo_path = '347.png' WHERE id = 347; -- JABLONEC (Tchequie) -> Jablonec
UPDATE team SET logo_path = '360.png' WHERE id = 360; -- MLADA BOLESLAV (Tchequie) -> Mladá Boleslav
UPDATE team SET logo_path = '356.png' WHERE id = 356; -- PARDUBICE (Tchequie) -> Pardubice
UPDATE team SET logo_path = '348.png' WHERE id = 348; -- SIGMA OLOMOUC (Tchequie) -> Sigma Olomouc
UPDATE team SET logo_path = '323.png' WHERE id = 323; -- SLAVIA PRAGUE (Tchequie) -> Slavia Prague
UPDATE team SET logo_path = '324.png' WHERE id = 324; -- SLOVACKO (Tchequie) -> Slovácko
UPDATE team SET logo_path = '308.png' WHERE id = 308; -- SPARTA PRAGUE (Tchequie) -> Sparta Prague
UPDATE team SET logo_path = '281.png' WHERE id = 281; -- TEPLICE (Tchequie) -> Teplice
UPDATE team SET logo_path = '282.png' WHERE id = 282; -- ZLIN (Tchequie) -> Zlín
UPDATE team SET logo_path = '858.png' WHERE id = 858; -- FENERBAHCE SK (TUR) -> Fenerbahçe
UPDATE team SET logo_path = '939.png' WHERE id = 939; -- ISTANBUL BASAKSEHIR (TUR) -> İstanbul Başakşehir
UPDATE team SET logo_path = '594.png' WHERE id = 594; -- ALANYASPOR (Turquie) -> Alanyaspor
UPDATE team SET logo_path = '619.png' WHERE id = 619; -- AMEDSPOR (Turquie) -> Amed
UPDATE team SET logo_path = '569.png' WHERE id = 569; -- CORUM (Turquie) -> Çorum
UPDATE team SET logo_path = '620.png' WHERE id = 620; -- ERZURUMSPOR (Turquie) -> Erzurumspor
UPDATE team SET logo_path = '622.png' WHERE id = 622; -- EYUPSPOR (Turquie) -> Eyüpspor
UPDATE team SET logo_path = '568.png' WHERE id = 568; -- GALATASARAY (Turquie) -> Galatasaray
UPDATE team SET logo_path = '593.png' WHERE id = 593; -- GAZIANTEP (Turquie) -> Gaziantep
UPDATE team SET logo_path = '595.png' WHERE id = 595; -- GENCLERBIRLIGI (Turquie) -> Gençlerbirliği
UPDATE team SET logo_path = '628.png' WHERE id = 628; -- GOZTEPE (Turquie) -> Göztepe
UPDATE team SET logo_path = '585.png' WHERE id = 585; -- KASIMPASA (Turquie) -> Kasımpaşa
UPDATE team SET logo_path = '609.png' WHERE id = 609; -- KOCAELISPOR (Turquie) -> Kocaelispor
UPDATE team SET logo_path = '587.png' WHERE id = 587; -- KONYASPOR (Turquie) -> Konyaspor
UPDATE team SET logo_path = '588.png' WHERE id = 588; -- RIZESPOR (Turquie) -> Rizespor
UPDATE team SET logo_path = '627.png' WHERE id = 627; -- SAMSUNSPOR (Turquie) -> Samsunspor
UPDATE team SET logo_path = '586.png' WHERE id = 586; -- TRABZONSPOR (Turquie) -> Trabzonspor
UPDATE team SET logo_path = '389.png' WHERE id = 389; -- FC KHARKIV (Ukraine) -> Kharkiv
UPDATE team SET logo_path = '388.png' WHERE id = 388; -- KARPATY LVIV (Ukraine) -> Karpaty Lviv
UPDATE team SET logo_path = '372.png' WHERE id = 372; -- KOLOS KOVALIVKA (Ukraine) -> Kolos Kovalivka
UPDATE team SET logo_path = '387.png' WHERE id = 387; -- KRYVBAS (Ukraine) -> Kryvbas
UPDATE team SET logo_path = '472.png' WHERE id = 472; -- KUDRIVKA (Ukraine) -> Kudrivka
UPDATE team SET logo_path = '420.png' WHERE id = 420; -- POLISSYA ZHYTOMYR (Ukraine) -> Polissya Zhytomyr

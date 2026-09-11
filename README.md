# Foot Results

Application de saisie et consultation de résultats de football européen (54 championnats, coupes nationales, Ligue des Champions / Europa League / Conference League). Backend Java / Spring Boot, frontend Vue 3, base PostgreSQL.

Projet **indépendant** du projet LoL (`lol-results`) : repo séparé, base de données séparée, ports différents (backend 8081, frontend 5174) pour pouvoir faire tourner les deux en parallèle si besoin.

## 1. Prérequis

Identiques au projet LoL : Java 21 (JDK), Maven, Node.js 20+/npm, PostgreSQL 14+, Git.

## 2. Principe du projet

- Les **matchs** (`match`) sont la seule source de vérité. `date`/`time` peuvent être vides (certains tours de coupe d'Europe n'ont pas de date exploitable dans le fichier source).
- Le **classement** (points, victoires/nuls/défaites, buts) n'est **pas stocké** : recalculé à la volée à partir des matchs `COMPLETED` d'une compétition de type `LEAGUE` (3 pts victoire, 1 pt nul, 0 pt défaite). N'a de sens que pour les championnats — pas pour les coupes, à élimination directe.
- Trois types de compétition (`CompetitionType`) : `LEAGUE` (championnat), `DOMESTIC_CUP` (coupe nationale), `CONTINENTAL_CUP` (LDC/EL/EC).
- Pas de "best of" comme au LoL : un match = un seul score aller (et un seul match retour distinct pour les confrontations européennes, stocké comme un deuxième match).

### Simplifications volontaires (v1)

- **`Team.country` et `Competition.country` sont du texte libre**, pas une table Pays normalisée : le fichier Excel utilise des libellés différents selon les feuilles (noms complets vs codes courts type "AZE"), réconcilier proprement aurait demandé un référentiel dédié. Consequence : un même club peut apparaître comme deux `Team` différents s'il joue à la fois en championnat et en coupe d'Europe avec un nom légèrement différent dans le fichier (ex: "PSG" vs "PARIS SAINT GERMAIN"). À fusionner manuellement via l'écran Équipes si besoin, ou à améliorer dans une prochaine version de l'import.
- **Coupes nationales** : seules les rencontres où le fichier donne déjà de vrais noms d'équipes (lignes `journee = "CUP"` dans le calendrier à plat, pour certains pays comme la Norvège ou le Belarus) sont importées, avec un round générique `"Coupe nationale"` (pas de tour précis disponible). Les tableaux de bracket en bas de chaque onglet pays (huitièmes/quarts...) sont des templates vides dans ton fichier actuel : rien à en importer pour l'instant.
- **Ligue des Champions / Europa League / Conference League** : seuls les **tours de qualification** (résultats aller/retour déjà connus) sont importés, comme deux matchs distincts par confrontation ("Aller"/"Retour"). La phase de ligue (36 clubs) et les tours à partir des seizièmes sont soit vides, soit dans un format de matrice pas assez fiable à parser automatiquement sans risque d'erreur — non importés en v1.
- **Sélections nationales** (qualifs Coupe du monde, Ligue des Nations) : hors périmètre v1, comme décidé.
- **Playoffs promotion/relégation et barrages de qualification européenne** ("BARRAGE"/"B EUR" dans le calendrier) : les "équipes" y sont en réalité des positions au classement ("3e", "9e") ou des renvois ("vainqueur du match 1"), pas des noms de clubs — non importés, pas exploitables sans logique de résolution des positions finales.

Tout ce qui n'est pas importé reste **saisissable manuellement via l'appli** (l'écran de saisie ne fait aucune différence entre un match importé et un match ajouté à la main).

## 3. Base de données

```bash
psql -U postgres
```

```sql
CREATE USER foot_user WITH PASSWORD 'foot_password';
CREATE DATABASE foot_results OWNER foot_user;
\q
```

## 4. Git

```bash
cd foot-results
git init
git add .
git commit -m "Scaffold initial : backend Spring Boot, frontend Vue 3, import Excel"
```

## 5. Backend (Spring Boot)

```bash
cd backend
mvn spring-boot:run
```

Flyway applique automatiquement `V1__init.sql` (schéma) puis `V2__seed_data.sql` (données importées de ton Excel : 982 équipes, 70 compétitions, 11 289 matchs). L'API démarre sur `http://localhost:8081`.

| Méthode | URL | Description |
|---|---|---|
| GET | `/api/competitions` | Liste des compétitions |
| POST | `/api/competitions` | Créer une compétition |
| GET | `/api/teams?country=France` | Liste des équipes (filtre pays optionnel) |
| POST | `/api/teams` | Créer une équipe |
| GET | `/api/matches?competitionId=1` | Matchs d'une compétition |
| POST | `/api/matches` | Créer un match |
| PUT | `/api/matches/{id}` | Modifier un match (score) |
| GET | `/api/standings?competitionId=1` | Classement recalculé (LEAGUE uniquement) |
| GET | `/api/head-to-head?competitionId=1` | Tête-à-tête recalculé |

⚠️ Comme pour le projet LoL, **je n'ai pas pu compiler ce backend** dans mon bac à sable (Maven Central bloqué). J'ai en revanche validé le schéma et l'intégralité des ~13 000 lignes SQL générées directement via `psql` (aucune erreur). Le premier `mvn spring-boot:run` chez toi reste le vrai test pour la partie Java elle-même.

## 6. Frontend (Vue 3)

```bash
cd frontend
npm install
npm run dev
```

Démarre sur `http://localhost:5174`, appelle l'API sur `http://localhost:8081` (CORS déjà configuré). Écrans : compétitions (avec filtre par type et recherche — utile avec 70 compétitions), détail d'une compétition (matchs + classement, filtre par journée/tour), équipes (avec recherche — utile avec ~1000 équipes).

## 7. Importer les données de ton Excel

```bash
cd import
pip install -r requirements.txt
python3 import_excel.py /chemin/vers/FOOT_2027.xlsx > ../backend/src/main/resources/db/migration/V2__seed_data.sql
```

Le `V2__seed_data.sql` fourni a déjà été généré et validé (voir section 2 pour le détail de ce qui est importé ou non). Le script est commenté en tête de fichier avec le détail exact des sources utilisées et des limitations.

**Si tu relances l'import après avoir modifié l'Excel** : régénère `V2__seed_data.sql` avant le tout premier démarrage. Une fois la base initialisée, Flyway ne rejoue pas une migration déjà appliquée — ajoute une `V3__...sql` pour des changements ultérieurs plutôt que de modifier `V2` après coup.

## 8. Pistes d'évolution (hors v1)

- Référentiel Pays normalisé (au lieu du texte libre), pour fiabiliser le rapprochement des équipes entre championnat et coupes d'Europe.
- Import de la phase de ligue LDC/EL/EC et des tours à partir des seizièmes, une fois qu'ils seront renseignés dans ton fichier.
- Import des coupes nationales à partir des tableaux de bracket par pays, une fois ceux-ci remplis.
- Sélections nationales (Ligue des Nations, qualifs Coupe du monde).
- Playoffs promotion/relégation et barrages de qualification européenne (nécessite de résoudre les positions finales de classement avant de connaître les équipes).
- Tableau de coefficients UEFA (feuilles "PAYS"/"UEFA" du fichier) pour déterminer automatiquement le nombre de places en coupes d'Europe par pays la saison suivante.

## Structure du repo

```
foot-results/
├── backend/    Spring Boot (Java 21, Maven, PostgreSQL, Flyway)
├── frontend/   Vue 3 + Vite
├── import/     Script Python de conversion Excel → SQL
└── README.md
```

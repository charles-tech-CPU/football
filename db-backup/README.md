# Sauvegarde de la base de données

`foot_results.sql` est un export complet (`pg_dump`) de la base `foot_results`
à la date indiquée par le dernier commit qui le modifie : schéma + toutes les
données (équipes fusionnées, statuts, places qualificatives, groupes de
2e phase, scores saisis...).

## Restaurer sur un autre ordinateur

1. Suivre la section 3 du README principal (créer l'utilisateur `foot_user`
   et la base `foot_results` **vide**), mais **ne pas encore démarrer le
   backend**.
2. Restaurer le dump dans cette base vide :

   ```bash
   psql -U foot_user -h localhost -d foot_results -f db-backup/foot_results.sql
   ```

3. Démarrer le backend normalement (`mvn spring-boot:run`). Flyway verra que
   les migrations sont déjà marquées comme appliquées (table
   `flyway_schema_history` incluse dans le dump) et ne rejouera rien.

⚠️ Si le backend a déjà été démarré une fois sur la base vide (donc si
Flyway a déjà créé les tables), il faut d'abord vider la base avant de
restaurer, sinon la restauration échouera sur des tables déjà existantes :

```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
```

## Regénérer cette sauvegarde

```bash
pg_dump -U foot_user -h localhost -d foot_results --no-owner --no-privileges -f db-backup/foot_results.sql
```

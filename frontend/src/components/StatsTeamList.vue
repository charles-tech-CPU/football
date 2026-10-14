<template>
  <section class="stats-card">
    <h3>{{ title }}</h3>
    <ol v-if="rows.length">
      <li v-for="r in rows" :key="r.teamId">
        <span class="team-cell">
          <TeamLogo :name="r.teamName" :logo-path="r.teamLogoPath" />
          <router-link :to="`/equipes/${r.teamId}`" class="team-link">{{ r.teamName }}</router-link>
        </span>
        <span class="stats-value" :title="`${r.played} match${r.played > 1 ? 's' : ''} joué${r.played > 1 ? 's' : ''}`">
          {{ r.value }} <small>{{ unit }}</small>
        </span>
      </li>
    </ol>
    <p v-else class="stats-empty">Personne en ce moment.</p>
  </section>
</template>

<script setup>
// Top 5 d'une statistique par club (page Statistiques).
import TeamLogo from './TeamLogo.vue'

defineProps({
  title: { type: String, required: true },
  unit: { type: String, default: '' },
  rows: { type: Array, required: true }
})
</script>

<style scoped>
.stats-card {
  padding: 14px 16px;
  border: 1px solid var(--border);
  border-radius: 14px;
  background: var(--surface);
}

.stats-card h3 {
  margin: 0 0 10px;
  font-size: 1em;
}

.stats-card ol {
  margin: 0;
  padding-left: 20px;
}

.stats-card li {
  padding: 4px 0;
}

.stats-card li > * {
  vertical-align: middle;
}

.stats-card li .team-cell {
  display: inline-flex;
}

.stats-value {
  float: right;
  font-weight: 800;
}

.stats-value small,
.stats-empty {
  color: var(--text-muted);
  font-weight: 400;
}
</style>

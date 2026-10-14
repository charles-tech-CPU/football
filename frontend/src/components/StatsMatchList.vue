<template>
  <div class="table-scroll">
    <table>
      <thead>
        <tr>
          <th>Date</th>
          <th>Tour</th>
          <th>Domicile</th>
          <th>Score</th>
          <th>Extérieur</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="m in matches" :key="m.id">
          <td class="muted">{{ m.date ? formatShortDate(m.date) : '—' }}</td>
          <td class="muted">{{ m.roundLabel }}</td>
          <td class="home">
            <span class="team-cell">
              <router-link :to="`/equipes/${m.team1Id}`" class="team-link">{{ m.team1Name }}</router-link>
              <TeamLogo :name="m.team1Name" :country="m.team1Country" :logo-path="m.team1LogoPath" />
            </span>
          </td>
          <td class="score">{{ m.score1 }} – {{ m.score2 }}</td>
          <td>
            <span class="team-cell">
              <TeamLogo :name="m.team2Name" :country="m.team2Country" :logo-path="m.team2LogoPath" />
              <router-link :to="`/equipes/${m.team2Id}`" class="team-link">{{ m.team2Name }}</router-link>
            </span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
// Liste de matchs remarquables (page Statistiques).
import TeamLogo from './TeamLogo.vue'
import { formatShortDate } from '../utils/format'

defineProps({
  matches: { type: Array, required: true }
})
</script>

<style scoped>
.muted {
  color: var(--text-muted);
  font-size: 0.9em;
  white-space: nowrap;
}

.home .team-cell {
  justify-content: flex-end;
}

.score {
  text-align: center;
  font-weight: 800;
  white-space: nowrap;
}
</style>

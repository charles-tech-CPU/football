<template>
  <h1>Derniers résultats</h1>
  <p class="section-intro">Tous les matchs joués, toutes compétitions confondues, du plus récent au plus ancien.</p>

  <table v-if="matches.length">
    <thead>
      <tr>
        <th>Date</th>
        <th>Heure</th>
        <th>Compétition</th>
        <th>Équipe 1</th>
        <th></th>
        <th>Équipe 2</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="m in matches" :key="m.id">
        <td>{{ m.date ?? '—' }}</td>
        <td>{{ formatTime(m.time) }}</td>
        <td>
          <span class="team-cell">
            <FlagIcon v-if="m.competitionCountry" :country="m.competitionCountry" />
            {{ m.competitionName }}
          </span>
        </td>
        <td><span class="team-chip" :style="chipStyle(m)">{{ m.team1Name }}</span></td>
        <td class="score-cell">{{ m.score1 }} - {{ m.score2 }}</td>
        <td><span class="team-chip" :style="chipStyle(m)">{{ m.team2Name }}</span></td>
      </tr>
    </tbody>
  </table>
  <p v-else-if="loaded" class="empty-state">Aucun match joué pour l'instant.</p>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../services/api'
import FlagIcon from '../components/FlagIcon.vue'
import { formatTime } from '../utils/format'
import { teamCountryStyle } from '../utils/teamColors'

const matches = ref([])
const loaded = ref(false)

function chipStyle(m) {
  const { bg, fg } = teamCountryStyle(m.competitionCountry ?? m.team1Name)
  return { backgroundColor: bg, color: fg }
}

async function load() {
  matches.value = await api.getRecentResults(500)
  loaded.value = true
}

onMounted(load)
</script>

<style scoped>
.score-cell {
  font-weight: 700;
  text-align: center;
}
</style>

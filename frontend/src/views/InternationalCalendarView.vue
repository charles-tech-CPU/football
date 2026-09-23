<template>
  <section class="hero">
    <span class="hero-eyebrow">Sélections nationales</span>
    <h1>Calendrier international</h1>
    <p class="section-intro">
      Sélections nationales, saison 2026-2027, toutes confédérations. Équipes fixes (pas de
      correction possible ici) ; date, heure, score et statut restent modifiables au fil des rencontres.
    </p>
  </section>

  <div class="filters">
    <select v-model="confederationFilter" aria-label="Filtrer par confédération">
      <option value="all">Toutes les confédérations</option>
      <option v-for="c in CONFEDERATIONS" :key="c.code" :value="c.code">{{ c.label }}</option>
    </select>
    <span v-if="filteredMatches.length" class="page-info">{{ filteredMatches.length }} match{{ filteredMatches.length > 1 ? 's' : '' }}</span>
  </div>

  <div v-if="filteredMatches.length" class="table-scroll">
    <table>
      <thead>
        <tr>
          <th>Date</th>
          <th>Heure</th>
          <th>Compétition</th>
          <th>Groupe / Journée</th>
          <th>Équipe 1</th>
          <th></th>
          <th></th>
          <th>Équipe 2</th>
          <th>Statut</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="m in filteredMatches" :key="m.id" :class="rowClass(m)">
          <MatchDateTimeCells v-model:date="edits[m.id].date" v-model:time="edits[m.id].time" flag-missing-time />
          <td>
            <span class="team-cell" :class="confederationBadgeClass(m)">{{ m.competitionName }}</span>
          </td>
          <td>{{ m.roundLabel }}</td>
          <td>
            <span class="team-cell">
              <FlagIcon :country="m.team1Country" />
              {{ m.team1Name }}
            </span>
          </td>
          <MatchScoreCells v-model:score1="edits[m.id].score1" v-model:score2="edits[m.id].score2" />
          <td>
            <span class="team-cell">
              <FlagIcon :country="m.team2Country" />
              {{ m.team2Name }}
            </span>
          </td>
          <td>
            <MatchStatusSelect v-model="edits[m.id].status" />
          </td>
          <td>
            <button @click="saveMatch(m)">Enregistrer</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
  <p v-else-if="loaded" class="empty-state">
    Aucun match programmé pour cette confédération pour la saison 2026-2027{{ confederationFilter !== 'all' ? " ('" + confederationLabel + "')" : '' }}.
  </p>

  <ul v-if="matches.length" class="standings-legend">
    <li v-for="c in CONFEDERATIONS" :key="c.code"><span class="legend-swatch" :class="c.legendClass"></span>{{ c.label }}</li>
  </ul>

  <p v-if="error" class="error-text">{{ error }}</p>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import api from '../services/api'
import FlagIcon from '../components/FlagIcon.vue'
import MatchDateTimeCells from '../components/MatchDateTimeCells.vue'
import MatchScoreCells from '../components/MatchScoreCells.vue'
import MatchStatusSelect from '../components/MatchStatusSelect.vue'
import { useMatchEdits } from '../composables/useMatchEdits'
import { CONFEDERATIONS, confederationForCompetitionCode, confederationBadgeClass } from '../utils/confederations'

const matches = ref([])
const loaded = ref(false)
const confederationFilter = ref('all')

const confederationLabel = computed(() => CONFEDERATIONS.find(c => c.code === confederationFilter.value)?.label ?? '')

const filteredMatches = computed(() => {
  if (confederationFilter.value === 'all') return matches.value
  return matches.value.filter(m => confederationForCompetitionCode(m.competitionCode)?.code === confederationFilter.value)
})

// Equipes non modifiables ici (selections nationales) : pas de liste de clubs a proposer.
const { edits, error, resetEdits, rowClass, saveMatch } = useMatchEdits({ reload: load })

async function load() {
  loaded.value = false
  matches.value = await api.getInternationalMatches()
  resetEdits(matches.value)
  loaded.value = true
}

onMounted(load)
</script>

<style scoped>
.page-info {
  color: var(--text-muted);
  font-size: 0.9em;
}
</style>

<template>
  <h1>Calendrier international</h1>
  <p class="section-intro">
    Sélections nationales, saison 2026-2027, toutes confédérations. Équipes fixes (pas de
    correction possible ici) ; date, heure, score et statut restent modifiables au fil des rencontres.
  </p>

  <div class="filters">
    <select v-model="confederationFilter">
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
          <td><input v-model="edits[m.id].date" class="date-input" type="date" /></td>
          <td>
            <input v-model="edits[m.id].time" class="time-input" type="time" />
            <span v-if="!edits[m.id].time" class="no-time-tag">Sans horaire</span>
          </td>
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
          <td>
            <input v-model.number="edits[m.id].score1" class="score-input" type="number" min="0" />
          </td>
          <td>
            <input v-model.number="edits[m.id].score2" class="score-input" type="number" min="0" />
          </td>
          <td>
            <span class="team-cell">
              <FlagIcon :country="m.team2Country" />
              {{ m.team2Name }}
            </span>
          </td>
          <td>
            <select v-model="edits[m.id].status">
              <option value="">À venir</option>
              <option value="POSTPONED">Reporté</option>
              <option value="SUSPENDED">Suspendu</option>
              <option value="FORFEIT">Forfait</option>
            </select>
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
import { computed, onMounted, reactive, ref } from 'vue'
import api from '../services/api'
import FlagIcon from '../components/FlagIcon.vue'
import { formatTime } from '../utils/format'
import { CONFEDERATIONS, confederationForCompetitionCode, confederationBadgeClass } from '../utils/confederations'

const matches = ref([])
const loaded = ref(false)
const error = ref('')
const edits = reactive({})
const confederationFilter = ref('all')

const confederationLabel = computed(() => CONFEDERATIONS.find(c => c.code === confederationFilter.value)?.label ?? '')

const filteredMatches = computed(() => {
  if (confederationFilter.value === 'all') return matches.value
  return matches.value.filter(m => confederationForCompetitionCode(m.competitionCode)?.code === confederationFilter.value)
})

const today = new Date().toISOString().slice(0, 10)

function statusRowClass(status) {
  if (status === 'POSTPONED') return 'row-postponed'
  if (status === 'SUSPENDED') return 'row-suspended'
  if (status === 'FORFEIT') return 'row-forfeit'
  return ''
}

function rowClass(m) {
  const status = edits[m.id]?.status
  const cls = statusRowClass(status)
  if (cls) return cls
  if (!m.date) return ''
  if (m.date < today) return 'row-overdue'
  if (m.date === today) return 'row-today'
  return ''
}

async function load() {
  loaded.value = false
  matches.value = await api.getInternationalMatches()
  for (const key of Object.keys(edits)) delete edits[key]
  for (const m of matches.value) {
    edits[m.id] = {
      date: m.date ?? '',
      time: formatTime(m.time),
      score1: m.score1,
      score2: m.score2,
      status: ['POSTPONED', 'SUSPENDED', 'FORFEIT'].includes(m.status) ? m.status : ''
    }
  }
  loaded.value = true
}

async function saveMatch(match) {
  error.value = ''
  const edit = edits[match.id]
  try {
    await api.updateMatch(match.id, {
      competitionId: match.competitionId,
      roundLabel: match.roundLabel,
      date: edit.date || null,
      time: edit.time || null,
      team1Id: match.team1Id,
      team2Id: match.team2Id,
      score1: edit.score1,
      score2: edit.score2,
      status: edit.status || null
    })
    await load()
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de l'enregistrement du match."
  }
}

onMounted(load)
</script>

<style scoped>
.table-scroll {
  overflow-x: auto;
  width: 100vw;
  position: relative;
  left: 50%;
  right: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
  padding: 0 20px;
}

.date-input {
  width: 140px;
}

.time-input {
  width: 120px;
}

.no-time-tag {
  display: inline-block;
  margin-left: 6px;
  padding: 1px 7px;
  border-radius: 999px;
  background: var(--surface-muted);
  color: var(--text-muted);
  font-size: 0.72em;
  font-weight: 600;
  white-space: nowrap;
}

.page-info {
  color: var(--text-muted);
  font-size: 0.9em;
}
</style>

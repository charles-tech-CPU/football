<template>
  <h1>Calendrier</h1>
  <p class="section-intro">Tous les matchs pas encore joués, toutes compétitions confondues, du plus proche au plus lointain.</p>

  <table v-if="matches.length">
    <thead>
      <tr>
        <th>Date</th>
        <th>Heure</th>
        <th>Compétition</th>
        <th>Équipe 1</th>
        <th></th>
        <th></th>
        <th>Équipe 2</th>
        <th>Statut</th>
        <th></th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="m in matches" :key="m.id" :class="statusRowClass(edits[m.id].status)">
        <td><input class="date-input" type="date" v-model="edits[m.id].date" /></td>
        <td><input class="time-input" type="time" v-model="edits[m.id].time" /></td>
        <td>
          <span class="team-cell">
            <FlagIcon v-if="m.competitionCountry" :country="m.competitionCountry" />
            {{ m.competitionName }}
          </span>
        </td>
        <td><span class="team-chip" :style="chipStyle(m)">{{ m.team1Name }}</span></td>
        <td>
          <input class="score-input" type="number" min="0" v-model.number="edits[m.id].score1" />
        </td>
        <td>
          <input class="score-input" type="number" min="0" v-model.number="edits[m.id].score2" />
        </td>
        <td><span class="team-chip" :style="chipStyle(m)">{{ m.team2Name }}</span></td>
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
  <p v-else-if="loaded" class="empty-state">Aucun match à venir pour l'instant.</p>
  <p v-if="error" class="error-text">{{ error }}</p>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import api from '../services/api'
import FlagIcon from '../components/FlagIcon.vue'
import { formatTime } from '../utils/format'
import { teamCountryStyle } from '../utils/teamColors'

const matches = ref([])
const loaded = ref(false)
const error = ref('')
const edits = reactive({})

function chipStyle(m) {
  const { bg, fg } = teamCountryStyle(m.competitionCountry ?? m.team1Name)
  return { backgroundColor: bg, color: fg }
}

function statusRowClass(status) {
  if (status === 'POSTPONED') return 'row-postponed'
  if (status === 'SUSPENDED') return 'row-suspended'
  if (status === 'FORFEIT') return 'row-forfeit'
  return ''
}

async function load() {
  loaded.value = false
  matches.value = await api.getUpcomingMatches(500)

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
.date-input {
  width: 140px;
}

.time-input {
  width: 120px;
}
</style>

<template>
  <div class="filters" v-if="rounds.length > 1">
    <select v-model="roundFilter">
      <option value="">Toutes les journées / tous les tours</option>
      <option v-for="r in rounds" :key="r" :value="r">{{ r }}</option>
    </select>
  </div>

  <table v-if="filteredMatches.length">
    <thead>
      <tr>
        <th>Round</th>
        <th>Date</th>
        <th>Heure</th>
        <th>Équipe 1</th>
        <th></th>
        <th></th>
        <th>Équipe 2</th>
        <th>Statut</th>
        <th></th>
      </tr>
    </thead>
    <tbody>
      <tr
        v-for="m in filteredMatches"
        :key="m.id"
        :class="{ 'row-scheduled': m.status === 'SCHEDULED', 'row-draw': isDraw(m), ...statusRowClass(m.status) }"
      >
        <td>{{ m.roundLabel }}</td>
        <td>{{ m.date ?? '—' }}</td>
        <td>{{ formatTime(m.time) }}</td>
        <td><span class="team-cell"><TeamLogo :name="m.team1Name" /> {{ m.team1Name }}</span></td>
        <td>
          <input class="score-input" type="number" min="0" v-model.number="edits[m.id].score1" />
        </td>
        <td>
          <input class="score-input" type="number" min="0" v-model.number="edits[m.id].score2" />
        </td>
        <td><span class="team-cell"><TeamLogo :name="m.team2Name" /> {{ m.team2Name }}</span></td>
        <td>{{ statusLabel(m.status) }}</td>
        <td>
          <button @click="saveScore(m)">Enregistrer</button>
        </td>
      </tr>
    </tbody>
  </table>
  <p v-else-if="loaded" class="empty-state">Aucun match pour ce filtre.</p>

  <details class="add-form">
    <summary>Ajouter un match</summary>
    <form class="inline" @submit.prevent="submitMatch">
      <select v-model.number="newMatch.team1Id" required>
        <option disabled value="">Équipe 1</option>
        <option v-for="t in teams" :key="t.id" :value="t.id">{{ t.name }}</option>
      </select>
      <select v-model.number="newMatch.team2Id" required>
        <option disabled value="">Équipe 2</option>
        <option v-for="t in teams" :key="t.id" :value="t.id">{{ t.name }}</option>
      </select>
      <input v-model="newMatch.roundLabel" placeholder="Round (ex: J1, 8e de finale)" required />
      <input v-model="newMatch.date" type="date" />
      <input v-model="newMatch.time" type="time" />
      <input class="score-input" type="number" min="0" v-model.number="newMatch.score1" placeholder="B1" />
      <input class="score-input" type="number" min="0" v-model.number="newMatch.score2" placeholder="B2" />
      <button type="submit">Ajouter</button>
    </form>
  </details>
  <p v-if="error" class="error-text">{{ error }}</p>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import api from '../services/api'
import TeamLogo from './TeamLogo.vue'
import { formatTime } from '../utils/format'

const props = defineProps({
  competitionId: { type: [String, Number], required: true }
})

const matches = ref([])
const teams = ref([])
const loaded = ref(false)
const error = ref('')
const edits = reactive({})
const roundFilter = ref('')

const newMatch = reactive({
  team1Id: '',
  team2Id: '',
  roundLabel: '',
  date: '',
  time: '',
  score1: null,
  score2: null
})

const playedMatches = computed(() => {
  const played = matches.value.filter(m => m.status === 'COMPLETED')
  return played.slice().sort((a, b) => {
    const ad = a.date ?? '', bd = b.date ?? ''
    if (ad !== bd) return bd.localeCompare(ad)
    return (b.time ?? '').localeCompare(a.time ?? '')
  })
})
const rounds = computed(() => [...new Set(playedMatches.value.map(m => m.roundLabel))])
const filteredMatches = computed(() =>
  roundFilter.value ? playedMatches.value.filter(m => m.roundLabel === roundFilter.value) : playedMatches.value
)

function isDraw(m) {
  return m.status === 'COMPLETED' && m.score1 === m.score2
}

const STATUS_LABELS = {
  SCHEDULED: 'À venir',
  COMPLETED: 'Joué',
  POSTPONED: 'Reporté',
  SUSPENDED: 'Suspendu',
  FORFEIT: 'Forfait'
}

function statusLabel(status) {
  return STATUS_LABELS[status] ?? status
}

function statusRowClass(status) {
  if (status === 'POSTPONED') return { 'row-postponed': true }
  if (status === 'SUSPENDED') return { 'row-suspended': true }
  if (status === 'FORFEIT') return { 'row-forfeit': true }
  return {}
}

async function load() {
  loaded.value = false
  roundFilter.value = ''
  const competitionId = Number(props.competitionId)
  const [matchList, teamList] = await Promise.all([
    api.getMatchesByCompetition(competitionId),
    api.getTeams()
  ])
  matches.value = matchList
  teams.value = teamList

  for (const key of Object.keys(edits)) delete edits[key]
  for (const m of matchList) {
    edits[m.id] = { score1: m.score1, score2: m.score2 }
  }
  loaded.value = true
}

async function saveScore(match) {
  error.value = ''
  const edit = edits[match.id]
  try {
    await api.updateMatch(match.id, {
      competitionId: match.competitionId,
      roundLabel: match.roundLabel,
      date: match.date,
      time: match.time,
      team1Id: match.team1Id,
      team2Id: match.team2Id,
      score1: edit.score1,
      score2: edit.score2
    })
    await load()
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de l'enregistrement du score."
  }
}

async function submitMatch() {
  error.value = ''
  try {
    await api.createMatch({
      competitionId: Number(props.competitionId),
      roundLabel: newMatch.roundLabel,
      date: newMatch.date || null,
      time: newMatch.time || null,
      team1Id: newMatch.team1Id,
      team2Id: newMatch.team2Id,
      score1: newMatch.score1,
      score2: newMatch.score2
    })
    newMatch.roundLabel = ''
    newMatch.date = ''
    newMatch.time = ''
    newMatch.score1 = null
    newMatch.score2 = null
    await load()
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de la création du match."
  }
}

watch(() => props.competitionId, load)
onMounted(load)
</script>

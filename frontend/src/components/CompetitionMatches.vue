<template>
  <div class="action-bar">
    <button type="button" class="action-btn" @click="showMatchModal = true">+ Ajouter un match</button>
    <button type="button" class="action-btn action-btn--secondary" @click="showTeamModal = true">+ Ajouter un club</button>
  </div>

  <AppModal v-model="showMatchModal" title="Ajouter un match">
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
      <input v-model.number="newMatch.score1" class="score-input" type="number" min="0" placeholder="B1" />
      <input v-model.number="newMatch.score2" class="score-input" type="number" min="0" placeholder="B2" />
      <button type="submit">Ajouter</button>
    </form>
  </AppModal>

  <AppModal v-model="showTeamModal" title="Ajouter un club">
    <form class="inline" @submit.prevent="submitNewTeam">
      <input v-model="newTeam.name" placeholder="Nom du club" required />
      <button type="submit">Ajouter</button>
    </form>
    <p v-if="teamError" class="error-text">{{ teamError }}</p>
  </AppModal>

  <div v-if="rounds.length > 1" class="filters">
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
        <td>
          <span class="team-cell">
            <TeamLogo :name="m.team1Name" :logo-path="m.team1LogoPath" />
            <select v-model.number="edits[m.id].team1Id">
              <option v-for="t in sortedTeams" :key="t.id" :value="t.id">{{ t.name }}</option>
            </select>
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
            <TeamLogo :name="m.team2Name" :logo-path="m.team2LogoPath" />
            <select v-model.number="edits[m.id].team2Id">
              <option v-for="t in sortedTeams" :key="t.id" :value="t.id">{{ t.name }}</option>
            </select>
          </span>
        </td>
        <td>{{ statusLabel(m.status) }}</td>
        <td>
          <button @click="saveScore(m)">Enregistrer</button>
        </td>
      </tr>
    </tbody>
  </table>
  <p v-else-if="loaded" class="empty-state">Aucun match pour ce filtre.</p>

  <p v-if="error" class="error-text">{{ error }}</p>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import api from '../services/api'
import TeamLogo from './TeamLogo.vue'
import AppModal from './AppModal.vue'
import { formatTime } from '../utils/format'

const props = defineProps({
  competitionId: { type: [String, Number], required: true },
  // Fragments (insensibles a la casse) : un match n'est garde que si son round_label
  // contient au moins un de ces fragments. Pas de filtre => tous les matchs.
  roundIncludes: { type: Array, default: null }
})

const matches = ref([])
const teams = ref([])
const loaded = ref(false)
const error = ref('')
const edits = reactive({})
const roundFilter = ref('')
const competitionCountry = ref(null)
const newTeam = reactive({ name: '' })
const teamError = ref('')
const showMatchModal = ref(false)
const showTeamModal = ref(false)

const newMatch = reactive({
  team1Id: '',
  team2Id: '',
  roundLabel: '',
  date: '',
  time: '',
  score1: null,
  score2: null
})

const scopedMatches = computed(() => {
  if (!props.roundIncludes) return matches.value
  const fragments = props.roundIncludes.map(f => f.toUpperCase())
  return matches.value.filter(m => fragments.some(f => (m.roundLabel ?? '').toUpperCase().includes(f)))
})

const playedMatches = computed(() => {
  const played = scopedMatches.value.filter(m => m.status === 'COMPLETED')
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
  const [matchList, teamList, competitions] = await Promise.all([
    api.getMatchesByCompetition(competitionId),
    api.getTeams({ competitionId }),
    api.getCompetitions()
  ])
  matches.value = matchList
  teams.value = teamList
  competitionCountry.value = competitions.find(c => c.id === competitionId)?.country ?? null

  for (const key of Object.keys(edits)) delete edits[key]
  for (const m of matchList) {
    edits[m.id] = { team1Id: m.team1Id, team2Id: m.team2Id, score1: m.score1, score2: m.score2 }
  }
  loaded.value = true
}

async function submitNewTeam() {
  teamError.value = ''
  try {
    await api.createTeam({ name: newTeam.name.toUpperCase(), country: competitionCountry.value })
    newTeam.name = ''
    teams.value = await api.getTeams({ competitionId: Number(props.competitionId) })
    showTeamModal.value = false
  } catch (e) {
    teamError.value = e.response?.data?.error ?? "Erreur lors de la création du club."
  }
}

const sortedTeams = computed(() => teams.value.slice().sort((a, b) => a.name.localeCompare(b.name)))

function teamNameById(id) {
  return teams.value.find(t => t.id === id)?.name ?? '?'
}

function confirmTeamChangeIfNeeded(match, edit) {
  const changed1 = edit.team1Id !== match.team1Id
  const changed2 = edit.team2Id !== match.team2Id
  if (!changed1 && !changed2) return true
  const lines = []
  if (changed1) lines.push(`Équipe 1 : ${match.team1Name} → ${teamNameById(edit.team1Id)}`)
  if (changed2) lines.push(`Équipe 2 : ${match.team2Name} → ${teamNameById(edit.team2Id)}`)
  return window.confirm(`Confirmer la modification du match ?\n${lines.join('\n')}`)
}

async function saveScore(match) {
  error.value = ''
  const edit = edits[match.id]
  if (!confirmTeamChangeIfNeeded(match, edit)) {
    edit.team1Id = match.team1Id
    edit.team2Id = match.team2Id
    return
  }
  try {
    await api.updateMatch(match.id, {
      competitionId: match.competitionId,
      roundLabel: match.roundLabel,
      date: match.date,
      time: match.time,
      team1Id: edit.team1Id,
      team2Id: edit.team2Id,
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
    showMatchModal.value = false
    await load()
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de la création du match."
  }
}

watch(() => props.competitionId, load)
onMounted(load)
</script>

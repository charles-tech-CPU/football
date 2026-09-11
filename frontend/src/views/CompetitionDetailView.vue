<template>
  <p><router-link to="/">← Toutes les compétitions</router-link></p>
  <h1>{{ competition?.name ?? '...' }}</h1>

  <template v-if="competition?.type === 'LEAGUE'">
    <h2>Classement</h2>
    <StandingsTable :rows="standings" />
  </template>

  <h2>Matchs</h2>
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
      <tr v-for="m in filteredMatches" :key="m.id" :class="{ 'row-scheduled': m.status === 'SCHEDULED' }">
        <td>{{ m.roundLabel }}</td>
        <td>{{ m.date ?? '—' }}</td>
        <td>{{ m.time ?? '' }}</td>
        <td>{{ m.team1Name }}</td>
        <td>
          <input class="score-input" type="number" min="0" v-model.number="edits[m.id].score1" />
        </td>
        <td>
          <input class="score-input" type="number" min="0" v-model.number="edits[m.id].score2" />
        </td>
        <td>{{ m.team2Name }}</td>
        <td>{{ m.status === 'COMPLETED' ? 'Joué' : 'À venir' }}</td>
        <td>
          <button @click="saveScore(m)">Enregistrer</button>
        </td>
      </tr>
    </tbody>
  </table>
  <p v-else-if="loaded">Aucun match pour ce filtre.</p>

  <h2>Ajouter un match</h2>
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
  <p v-if="error" style="color:#ff6b6b">{{ error }}</p>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import api from '../services/api'
import StandingsTable from '../components/StandingsTable.vue'

const props = defineProps({
  id: { type: [String, Number], required: true }
})

const competition = ref(null)
const matches = ref([])
const standings = ref([])
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

const rounds = computed(() => [...new Set(matches.value.map(m => m.roundLabel))])
const filteredMatches = computed(() =>
  roundFilter.value ? matches.value.filter(m => m.roundLabel === roundFilter.value) : matches.value
)

async function load() {
  const competitionId = Number(props.id)
  const [competitions, matchList, teamList] = await Promise.all([
    api.getCompetitions(),
    api.getMatchesByCompetition(competitionId),
    api.getTeams()
  ])
  competition.value = competitions.find(c => c.id === competitionId) ?? null
  matches.value = matchList
  teams.value = teamList
  standings.value = competition.value?.type === 'LEAGUE' ? await api.getStandings(competitionId) : []

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
      competitionId: Number(props.id),
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

watch(() => props.id, load)
onMounted(load)
</script>

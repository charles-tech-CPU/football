<template>
  <MatchCreationBar
    :competition-id="competitionId"
    :teams="teams"
    :competition-country="competitionCountry"
    @match-created="load"
    @team-created="reloadTeams"
  />

  <div v-if="rounds.length > 1" class="filters">
    <select v-model="roundFilter" aria-label="Filtrer par tour">
      <option value="">Toutes les journées / tous les tours</option>
      <option v-for="r in rounds" :key="r" :value="r">{{ r }}</option>
    </select>
  </div>

  <div v-if="!roundFilter && pageCount > 1" class="round-pager">
    <button type="button" :disabled="page >= pageCount - 1" @click="page++">← Journées précédentes</button>
    <span class="round-pager-label">{{ pageRounds.toReversed().join(' · ') }}</span>
    <button type="button" :disabled="page === 0" @click="page--">Journées suivantes →</button>
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
            <select v-model.number="edits[m.id].team1Id" aria-label="Équipe domicile">
              <option v-for="t in sortedTeams" :key="t.id" :value="t.id">{{ t.name }}</option>
            </select>
          </span>
        </td>
        <td>
          <input v-model.number="edits[m.id].score1" aria-label="Buts équipe domicile" class="score-input" type="number" min="0" />
        </td>
        <td>
          <input v-model.number="edits[m.id].score2" aria-label="Buts équipe extérieur" class="score-input" type="number" min="0" />
        </td>
        <td>
          <span class="team-cell">
            <TeamLogo :name="m.team2Name" :logo-path="m.team2LogoPath" />
            <select v-model.number="edits[m.id].team2Id" aria-label="Équipe extérieur">
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
import MatchCreationBar from './MatchCreationBar.vue'
import { teamChangeLines } from '../utils/matchEdit'
import { formatTime } from '../utils/format'
import { roundsByRecency } from '../utils/rounds'

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
const rounds = computed(() => roundsByRecency(playedMatches.value))

// Sans filtre, les journees sont paginees par ROUNDS_PER_PAGE, les plus recentes d'abord
// (page 0 = derniere et avant-derniere journee jouees).
const ROUNDS_PER_PAGE = 2
const page = ref(0)
const pageCount = computed(() => Math.ceil(rounds.value.length / ROUNDS_PER_PAGE))
const pageRounds = computed(() => rounds.value.slice(page.value * ROUNDS_PER_PAGE, (page.value + 1) * ROUNDS_PER_PAGE))

const filteredMatches = computed(() => {
  if (roundFilter.value) return playedMatches.value.filter(m => m.roundLabel === roundFilter.value)
  return pageRounds.value.flatMap(r => playedMatches.value.filter(m => m.roundLabel === r))
})

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
  page.value = 0
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

// Un club vient d'etre cree : on recharge la liste proposee dans les formulaires.
async function reloadTeams() {
  teams.value = await api.getTeams({ competitionId: Number(props.competitionId) })
}

const sortedTeams = computed(() => teams.value.slice().sort((a, b) => a.name.localeCompare(b.name)))

function teamNameById(id) {
  return teams.value.find(t => t.id === id)?.name ?? '?'
}

function confirmTeamChangeIfNeeded(match, edit) {
  const lines = teamChangeLines(match, edit, teamNameById)
  return lines.length === 0 || window.confirm(`Confirmer la modification du match ?\n${lines.join('\n')}`)
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


watch(() => props.competitionId, load)
onMounted(load)
</script>

<style scoped>
.round-pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 12px 0;
}
.round-pager-label {
  font-weight: 700;
}
</style>

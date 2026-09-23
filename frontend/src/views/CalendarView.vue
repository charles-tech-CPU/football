<template>
  <h1>Calendrier</h1>
  <p class="section-intro">Matchs pas encore joués (hors reportés/suspendus), du plus proche au plus lointain. Filtrable par type de compétition, 20 par page.</p>

  <div class="filters">
    <select v-model="typeFilter" @change="onFilterChange">
      <option value="league">Championnats</option>
      <option value="cup">Coupes nationales</option>
      <option value="ldc">Ligue des Champions</option>
      <option value="el">Europa League</option>
      <option value="ec">Conference League</option>
      <option value="all">Toutes les compétitions</option>
    </select>
    <span v-if="totalCount" class="page-info">{{ totalCount }} match{{ totalCount > 1 ? 's' : '' }}</span>
    <router-link to="/reportes-suspendus" class="action-btn action-btn--secondary">Reportés / Suspendus</router-link>
  </div>

  <div v-if="matches.length" class="table-scroll">
  <table>
    <thead>
      <tr>
        <th>Date</th>
        <th>Heure</th>
        <th>Compétition</th>
        <th>Journée</th>
        <th>Équipe 1</th>
        <th></th>
        <th></th>
        <th>Équipe 2</th>
        <th>Statut</th>
        <th></th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="m in matches" :key="m.id" :class="rowClass(m)">
        <td><input v-model="edits[m.id].date" class="date-input" type="date" /></td>
        <td>
          <input v-model="edits[m.id].time" class="time-input" type="time" />
          <span v-if="!edits[m.id].time" class="no-time-tag">Sans horaire</span>
        </td>
        <td>
          <span class="team-cell" :class="competitionBadgeClass(m)">
            <FlagIcon v-if="m.competitionCountry" :country="m.competitionCountry" />
            {{ m.competitionName }}
          </span>
        </td>
        <td>{{ m.roundLabel }}</td>
        <td>
          <span class="team-cell">
            <template v-if="isContinental(m)">
              <TeamLogo :name="m.team1Name" :logo-path="m.team1LogoPath" />
              <FlagIcon :country="m.team1Country" />
            </template>
            <select v-model.number="edits[m.id].team1Id">
              <option v-for="t in teamOptionsFor(m, edits[m.id].team1Id)" :key="t.id" :value="t.id">{{ t.name }}</option>
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
            <template v-if="isContinental(m)">
              <TeamLogo :name="m.team2Name" :logo-path="m.team2LogoPath" />
              <FlagIcon :country="m.team2Country" />
            </template>
            <select v-model.number="edits[m.id].team2Id">
              <option v-for="t in teamOptionsFor(m, edits[m.id].team2Id)" :key="t.id" :value="t.id">{{ t.name }}</option>
            </select>
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
  <p v-else-if="loaded" class="empty-state">Aucun match pour ce filtre.</p>

  <div v-if="totalPages > 1" class="pagination">
    <button type="button" :disabled="currentPage === 0" @click="prevPage">← Précédent</button>
    <span>Page {{ currentPage + 1 }} / {{ totalPages }}</span>
    <button type="button" :disabled="currentPage >= totalPages - 1" @click="nextPage">Suivant →</button>
  </div>

  <p v-if="error" class="error-text">{{ error }}</p>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import api from '../services/api'
import FlagIcon from '../components/FlagIcon.vue'
import TeamLogo from '../components/TeamLogo.vue'
import { formatTime } from '../utils/format'
import { competitionBadgeClass } from '../utils/competitionBadge'

function isContinental(m) {
  return m.competitionCode === 'LDC' || m.competitionCode === 'EL' || m.competitionCode === 'EC'
}

const PAGE_SIZE = 20

const matches = ref([])
const teams = ref([])
const loaded = ref(false)
const error = ref('')
const edits = reactive({})
// Championnats par defaut (le plus gros volume) : evite de charger d'un coup
// les ~8000 matchs a venir toutes competitions confondues.
const typeFilter = ref('league')
const currentPage = ref(0)
const totalCount = ref(0)

const totalPages = computed(() => Math.max(1, Math.ceil(totalCount.value / PAGE_SIZE)))

// Restreint la liste proposee aux clubs du pays de la competition du match (championnats/coupes
// nationales) ; pour les coupes d'Europe (pas de pays), la liste complete reste proposee. On
// garde toujours l'equipe actuellement selectionnee meme si son pays ne correspond pas exactement
// (libelles de pays en texte libre, cf README).
function teamOptionsFor(m, currentId) {
  const country = m.competitionCountry
  let list = country
    ? teams.value.filter(t => t.country && t.country.toLowerCase() === country.toLowerCase())
    : teams.value
  if (currentId != null && !list.some(t => t.id === currentId)) {
    const current = teams.value.find(t => t.id === currentId)
    if (current) list = [...list, current]
  }
  return list.slice().sort((a, b) => a.name.localeCompare(b.name))
}

function statusRowClass(status) {
  if (status === 'POSTPONED') return 'row-postponed'
  if (status === 'SUSPENDED') return 'row-suspended'
  if (status === 'FORFEIT') return 'row-forfeit'
  return ''
}

const today = new Date().toISOString().slice(0, 10)

function rowClass(m) {
  const status = edits[m.id].status
  const cls = statusRowClass(status)
  if (cls) return cls
  const date = edits[m.id].date
  if (!date) return ''
  if (date < today) return 'row-overdue'
  if (date === today) return 'row-today'
  return ''
}

let teamsLoaded = false

async function load() {
  loaded.value = false
  const [page, teamList] = await Promise.all([
    api.getUpcomingMatches({ page: currentPage.value, size: PAGE_SIZE, filter: typeFilter.value }),
    teamsLoaded ? Promise.resolve(teams.value) : api.getTeams()
  ])
  matches.value = page.items
  totalCount.value = page.totalCount
  teams.value = teamList
  teamsLoaded = true

  for (const key of Object.keys(edits)) delete edits[key]
  for (const m of matches.value) {
    edits[m.id] = {
      team1Id: m.team1Id,
      team2Id: m.team2Id,
      date: m.date ?? '',
      time: formatTime(m.time),
      score1: m.score1,
      score2: m.score2,
      status: ['POSTPONED', 'SUSPENDED', 'FORFEIT'].includes(m.status) ? m.status : ''
    }
  }
  loaded.value = true
}

function onFilterChange() {
  currentPage.value = 0
  load()
}

function prevPage() {
  if (currentPage.value === 0) return
  currentPage.value--
  load()
}

function nextPage() {
  if (currentPage.value >= totalPages.value - 1) return
  currentPage.value++
  load()
}

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

async function saveMatch(match) {
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
      date: edit.date || null,
      time: edit.time || null,
      team1Id: edit.team1Id,
      team2Id: edit.team2Id,
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

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin: 16px 0;
}
</style>

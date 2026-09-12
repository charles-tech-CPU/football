<template>
  <div class="results-grid-header" v-if="allowAdd">
    <h2>Grille des résultats</h2>
    <button type="button" class="add-match-btn" @click="showAddForm = !showAddForm">
      {{ showAddForm ? '✕ Fermer' : '+ Ajouter un match' }}
    </button>
  </div>
  <h2 v-else>Grille des résultats</h2>

  <form class="inline add-match-form" v-if="allowAdd && showAddForm" @submit.prevent="submitMatch">
    <select v-model.number="newMatch.team1Id" required>
      <option disabled value="">Équipe 1</option>
      <option v-for="t in sortedTeams" :key="t.id" :value="t.id">{{ t.name }}</option>
    </select>
    <select v-model.number="newMatch.team2Id" required>
      <option disabled value="">Équipe 2</option>
      <option v-for="t in sortedTeams" :key="t.id" :value="t.id">{{ t.name }}</option>
    </select>
    <input v-model="newMatch.date" type="date" />
    <input v-model="newMatch.time" type="time" />
    <input class="score-input" type="number" min="0" v-model.number="newMatch.score1" placeholder="B1" />
    <input class="score-input" type="number" min="0" v-model.number="newMatch.score2" placeholder="B2" />
    <button type="submit">Ajouter</button>
  </form>

  <div class="grid-scroll" v-if="teamOrder.length">
    <table class="results-grid">
      <thead>
        <tr>
          <th class="corner"></th>
          <th v-for="t in teamOrder" :key="`h-${t.id}`" :title="t.name">{{ shortName(t.name) }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in teamOrder" :key="`r-${row.id}`">
          <th :title="row.name">
            <span class="grid-row-header">
              <TeamLogo :name="row.name" :logo-path="row.logoPath" />
              <FlagIcon v-if="showFlags" :country="row.country" />
              {{ row.name }}
            </span>
          </th>
          <td
            v-for="col in teamOrder"
            :key="`c-${row.id}-${col.id}`"
            :class="cellClass(row.id, col.id)"
            @click="startEdit(row.id, col.id)"
          >
            <template v-if="row.id === col.id">—</template>
            <template v-else-if="editingKey === cellKey(row.id, col.id)">
              <span class="grid-edit">
                <input class="score-input" type="number" min="0" v-model.number="editScore1" @click.stop />
                <input class="score-input" type="number" min="0" v-model.number="editScore2" @click.stop />
                <button type="button" @click.stop="confirmEdit">✓</button>
              </span>
            </template>
            <template v-else-if="matchAt(row.id, col.id)">
              {{ cellLabel(row.id, col.id) }}
            </template>
            <template v-else>·</template>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
  <p v-if="error" class="error-text">{{ error }}</p>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import api from '../services/api'
import TeamLogo from './TeamLogo.vue'
import FlagIcon from './FlagIcon.vue'

const props = defineProps({
  competitionId: { type: [String, Number], required: true },
  // Fragments (insensibles a la casse) : un match n'est garde que si son round_label
  // contient au moins un de ces fragments. Pas de filtre => tous les matchs.
  roundIncludes: { type: Array, default: null },
  // Transmis tel quel a /api/standings pour ne classer/ordonner que les equipes de cette phase.
  round: { type: String, default: null },
  showFlags: { type: Boolean, default: false },
  // Affiche le formulaire "Ajouter un match" (necessaire pour les phases sans
  // autre ecran d'edition, ex: phase de ligue LDC/EL/EC - contrairement aux
  // championnats nationaux, deja editables via CompetitionMatches).
  allowAdd: { type: Boolean, default: false },
  // round_label impose aux matchs crees depuis ce formulaire (ex: "PHASE DE LIGUE").
  defaultRoundLabel: { type: String, default: '' }
})

const matches = ref([])
const standings = ref([])
const teams = ref([])
const error = ref('')
const editingKey = ref(null)
const editScore1 = ref(null)
const editScore2 = ref(null)
const showAddForm = ref(false)

const sortedTeams = computed(() => teams.value.slice().sort((a, b) => a.name.localeCompare(b.name)))

const newMatch = reactive({
  team1Id: '',
  team2Id: '',
  date: '',
  time: '',
  score1: null,
  score2: null
})

async function submitMatch() {
  error.value = ''
  try {
    await api.createMatch({
      competitionId: Number(props.competitionId),
      roundLabel: props.defaultRoundLabel,
      date: newMatch.date || null,
      time: newMatch.time || null,
      team1Id: newMatch.team1Id,
      team2Id: newMatch.team2Id,
      score1: newMatch.score1,
      score2: newMatch.score2
    })
    newMatch.team1Id = ''
    newMatch.team2Id = ''
    newMatch.date = ''
    newMatch.time = ''
    newMatch.score1 = null
    newMatch.score2 = null
    showAddForm.value = false
    await load()
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de la création du match."
  }
}

const matchByPair = computed(() => {
  const map = new Map()
  for (const m of matches.value) {
    map.set(`${m.team1Id}-${m.team2Id}`, m)
  }
  return map
})

const teamOrder = computed(() => {
  const ordered = []
  const seen = new Set()
  for (const row of standings.value) {
    ordered.push({ id: row.teamId, name: row.teamName, logoPath: row.teamLogoPath, country: row.teamCountry })
    seen.add(row.teamId)
  }
  const extra = new Map()
  for (const m of matches.value) {
    if (!seen.has(m.team1Id)) extra.set(m.team1Id, { name: m.team1Name, logoPath: m.team1LogoPath, country: m.team1Country })
    if (!seen.has(m.team2Id)) extra.set(m.team2Id, { name: m.team2Name, logoPath: m.team2LogoPath, country: m.team2Country })
  }
  // Phase sans aucun match joue pour l'instant (ex: phase de ligue EL/EC pas encore
  // saisie) : on affiche quand meme la grille, a partir des equipes de la
  // competition (recuperees pour le formulaire d'ajout), pour pouvoir commencer a
  // remplir plutot que de n'afficher qu'une grille vide/absente.
  if (props.allowAdd) {
    for (const t of teams.value) {
      if (!seen.has(t.id) && !extra.has(t.id)) extra.set(t.id, { name: t.name, logoPath: t.logoPath, country: t.country })
    }
  }
  for (const [id, info] of [...extra.entries()].sort((a, b) => a[1].name.localeCompare(b[1].name))) {
    ordered.push({ id, ...info })
  }
  return ordered
})

function shortName(name) {
  return name.length > 3 ? name.slice(0, 3).toUpperCase() : name.toUpperCase()
}

function cellKey(rowId, colId) {
  return `${rowId}-${colId}`
}

function matchAt(rowId, colId) {
  return matchByPair.value.get(cellKey(rowId, colId)) ?? null
}

function cellLabel(rowId, colId) {
  const m = matchAt(rowId, colId)
  if (!m || m.score1 == null || m.score2 == null) return '-'
  return `${m.score1}-${m.score2}`
}

function cellClass(rowId, colId) {
  if (rowId === colId) return 'grid-diagonal'
  const m = matchAt(rowId, colId)
  if (!m) return 'grid-empty'
  if (m.score1 == null || m.score2 == null) return 'grid-scheduled'
  if (m.score1 > m.score2) return 'grid-win'
  if (m.score1 < m.score2) return 'grid-loss'
  return 'grid-draw'
}

function startEdit(rowId, colId) {
  if (rowId === colId) return
  const m = matchAt(rowId, colId)
  if (!m) return
  editingKey.value = cellKey(rowId, colId)
  editScore1.value = m.score1
  editScore2.value = m.score2
}

async function confirmEdit() {
  const [rowId, colId] = editingKey.value.split('-').map(Number)
  const m = matchAt(rowId, colId)
  error.value = ''
  try {
    await api.updateMatch(m.id, {
      competitionId: m.competitionId,
      roundLabel: m.roundLabel,
      date: m.date,
      time: m.time,
      team1Id: m.team1Id,
      team2Id: m.team2Id,
      score1: editScore1.value,
      score2: editScore2.value
    })
    editingKey.value = null
    await load()
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de l'enregistrement du score."
  }
}

async function load() {
  const competitionId = Number(props.competitionId)
  const [matchList, standingsList, teamList] = await Promise.all([
    api.getMatchesByCompetition(competitionId),
    api.getStandings(competitionId, props.round),
    props.allowAdd ? api.getTeams({ competitionId }) : Promise.resolve([])
  ])
  matches.value = props.roundIncludes
    ? matchList.filter(m => props.roundIncludes.some(f => (m.roundLabel ?? '').toUpperCase().includes(f.toUpperCase())))
    : matchList
  standings.value = standingsList
  teams.value = teamList
}

watch(() => props.competitionId, load)
onMounted(load)
</script>

<style scoped>
.results-grid-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.results-grid-header h2 {
  margin: 0;
}

.add-match-btn {
  white-space: nowrap;
}

.add-match-form {
  margin-bottom: 16px;
}

.grid-scroll {
  overflow-x: auto;
  margin-bottom: 24px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: var(--surface);
  /* Sort la grille du conteneur centre (#app, max-width: 1080px) pour utiliser
     toute la largeur de la fenetre et eviter le scroll horizontal avec 36 equipes. */
  width: 100vw;
  position: relative;
  left: 50%;
  right: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
}

table.results-grid {
  border: none;
  border-radius: 0;
  margin-bottom: 0;
}

table.results-grid th, table.results-grid td {
  text-align: center;
  padding: 3px 4px;
  font-size: 0.72em;
  white-space: nowrap;
  border: 1px solid var(--border);
}

table.results-grid thead th {
  position: sticky;
  top: 0;
  background: var(--surface-muted);
  z-index: 1;
}

table.results-grid tbody th {
  text-align: left;
  position: sticky;
  left: 0;
  background: var(--surface-muted);
  font-weight: 600;
  z-index: 1;
  max-width: 130px;
  overflow: hidden;
  text-overflow: ellipsis;
}

table.results-grid .corner {
  position: sticky;
  left: 0;
  top: 0;
  z-index: 2;
  background: var(--surface-muted);
}

table.results-grid td {
  cursor: pointer;
  min-width: 28px;
}

table.results-grid tbody tr:hover td {
  background: unset;
}

.grid-diagonal {
  background: #1c2521 !important;
  cursor: default;
}

.grid-empty {
  color: #c2cac5;
  cursor: default;
}

.grid-scheduled {
  color: var(--text-muted);
}

.grid-win {
  background: #dcf5e6;
  font-weight: 700;
}

.grid-loss {
  background: #fbe0de;
}

.grid-draw {
  background: #fbf3c7;
  font-weight: 700;
}

.grid-row-header {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.grid-edit {
  display: inline-flex;
  gap: 2px;
  align-items: center;
}

.grid-edit .score-input {
  width: 32px;
  padding: 2px;
}

.grid-edit button {
  padding: 2px 6px;
}
</style>

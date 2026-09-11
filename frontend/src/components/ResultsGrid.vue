<template>
  <h2>Grille des résultats</h2>
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
              <TeamLogo :name="row.name" />
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
import { computed, onMounted, ref, watch } from 'vue'
import api from '../services/api'
import TeamLogo from './TeamLogo.vue'

const props = defineProps({
  competitionId: { type: [String, Number], required: true }
})

const matches = ref([])
const standings = ref([])
const error = ref('')
const editingKey = ref(null)
const editScore1 = ref(null)
const editScore2 = ref(null)

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
    ordered.push({ id: row.teamId, name: row.teamName })
    seen.add(row.teamId)
  }
  const extra = new Map()
  for (const m of matches.value) {
    if (!seen.has(m.team1Id)) extra.set(m.team1Id, m.team1Name)
    if (!seen.has(m.team2Id)) extra.set(m.team2Id, m.team2Name)
  }
  for (const [id, name] of [...extra.entries()].sort((a, b) => a[1].localeCompare(b[1]))) {
    ordered.push({ id, name })
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
  const [matchList, standingsList] = await Promise.all([
    api.getMatchesByCompetition(competitionId),
    api.getStandings(competitionId)
  ])
  matches.value = matchList
  standings.value = standingsList
}

watch(() => props.competitionId, load)
onMounted(load)
</script>

<style scoped>
.grid-scroll {
  overflow-x: auto;
  margin-bottom: 24px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: var(--surface);
}

table.results-grid {
  border: none;
  border-radius: 0;
  margin-bottom: 0;
}

table.results-grid th, table.results-grid td {
  text-align: center;
  padding: 4px 6px;
  font-size: 0.78em;
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
  max-width: 160px;
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
  min-width: 40px;
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

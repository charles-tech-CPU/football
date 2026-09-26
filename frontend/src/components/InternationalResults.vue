<template>
  <div v-if="groupCards.length" class="group-cards">
    <article v-for="grp in groupCards" :key="grp.name ?? '_'" class="group-card">
      <header class="group-card-head">
        <h3>{{ grp.name ?? 'Résultats' }}</h3>
        <span class="group-progress">{{ grp.played.length }}/{{ grp.total }} joués</span>
      </header>

      <div class="xtable-scroll">
        <table class="xtable">
          <thead>
            <tr>
              <th class="xtable-corner"><span class="corner-hint">Dom. \ Ext.</span></th>
              <th v-for="t in grp.teams" :key="`h-${t.teamId}`" class="xtable-col" :title="t.teamName">
                <FlagIcon :country="t.teamCountry" />
                <span class="col-code">{{ shortName(t.teamName) }}</span>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in grp.teams" :key="`r-${row.teamId}`">
              <th class="xtable-row" :title="row.teamName">
                <span class="team-cell">
                  <FlagIcon :country="row.teamCountry" />
                  <span class="row-name">{{ row.teamName }}</span>
                </span>
              </th>
              <td
                v-for="col in grp.teams"
                :key="`c-${row.teamId}-${col.teamId}`"
                class="xtable-cell"
                :class="cellClass(row.teamId, col.teamId)"
                :title="cellTitle(matchAt(row.teamId, col.teamId))"
                :tabindex="matchAt(row.teamId, col.teamId) ? 0 : -1"
                @click="startEdit(matchAt(row.teamId, col.teamId))"
                @keydown.enter="startEdit(matchAt(row.teamId, col.teamId))"
              >
                <span v-if="editing && editing.id === matchAt(row.teamId, col.teamId)?.id" class="cell-edit">
                  <input v-model.number="editScore1" aria-label="Buts équipe domicile" type="number" min="0" @click.stop @keydown.enter.stop="confirmEdit" />
                  <input v-model.number="editScore2" aria-label="Buts équipe extérieur" type="number" min="0" @click.stop @keydown.enter.stop="confirmEdit" />
                  <button type="button" @click.stop="confirmEdit">✓</button>
                  <button type="button" class="cell-cancel" @click.stop="editing = null">✕</button>
                </span>
                <span v-else-if="isResult(matchAt(row.teamId, col.teamId))" class="score-pill">{{ cellLabel(matchAt(row.teamId, col.teamId)) }}</span>
                <span v-else-if="matchAt(row.teamId, col.teamId)" class="cell-date">{{ shortDate(matchAt(row.teamId, col.teamId).date) }}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <ul v-if="grp.played.length" class="result-list">
        <li v-for="m in grp.played" :key="m.id" class="result-row">
          <span class="result-meta">{{ shortDate(m.date) }}</span>
          <span class="result-team result-team--home" :class="{ winner: m.score1 > m.score2 }">
            {{ m.team1Name }}<FlagIcon :country="m.team1Country" />
          </span>
          <span class="result-score">{{ m.score1 }}<span class="score-sep">-</span>{{ m.score2 }}</span>
          <span class="result-team" :class="{ winner: m.score2 > m.score1 }">
            <FlagIcon :country="m.team2Country" />{{ m.team2Name }}
          </span>
        </li>
      </ul>
      <p v-else class="group-empty">Aucun match joué pour l'instant.</p>
    </article>
  </div>

  <article v-if="otherResults.length" class="group-card other-card">
    <header class="group-card-head">
      <h3>Autres résultats</h3>
      <span class="group-progress">{{ otherResults.length }} match{{ otherResults.length > 1 ? 's' : '' }}</span>
    </header>
    <ul class="result-list">
      <li v-for="m in otherResults" :key="m.id" class="result-row">
        <span class="result-meta">{{ shortDate(m.date) }}<small v-if="m.roundLabel">{{ m.roundLabel }}</small></span>
        <span class="result-team result-team--home" :class="{ winner: m.score1 > m.score2 }">
          {{ m.team1Name }}<FlagIcon :country="m.team1Country" />
        </span>
        <span class="result-score">{{ m.score1 }}<span class="score-sep">-</span>{{ m.score2 }}</span>
        <span class="result-team" :class="{ winner: m.score2 > m.score1 }">
          <FlagIcon :country="m.team2Country" />{{ m.team2Name }}
        </span>
      </li>
    </ul>
  </article>

  <p v-if="!groupCards.length && !otherResults.length" class="empty-state">Aucun résultat pour l'instant dans cette compétition.</p>
  <p v-if="error" class="error-text">{{ error }}</p>
</template>

<script setup>
import { computed, ref } from 'vue'
import api from '../services/api'
import FlagIcon from './FlagIcon.vue'
import { isPlayed } from '../utils/matchEdit.js'

// Resultats d'une competition de selections nationales : une carte par groupe avec la grille
// croisee (domicile en ligne, exterieur en colonne, dans l'ordre du classement) puis la liste
// des matchs joues. Les matchs joues hors groupe (barrages, phase finale...) sont listes a part.
const props = defineProps({
  matches: { type: Array, default: () => [] },
  // Lignes de classement (avec `group`) : composition et ordre des equipes de chaque groupe.
  rows: { type: Array, default: () => [] }
})

const emit = defineEmits(['saved'])

const error = ref('')
const editing = ref(null)
const editScore1 = ref(null)
const editScore2 = ref(null)

const groupByTeam = computed(() => new Map(props.rows.map(r => [r.teamId, r.group ?? null])))

function groupOf(m) {
  const g1 = groupByTeam.value.get(m.team1Id)
  return g1 !== undefined && g1 === groupByTeam.value.get(m.team2Id) ? g1 : undefined
}

const matchByPair = computed(() => {
  const map = new Map()
  for (const m of props.matches) {
    if (groupOf(m) !== undefined) map.set(`${m.team1Id}-${m.team2Id}`, m)
  }
  return map
})

function byDate(a, b) {
  return (a.date ?? '').localeCompare(b.date ?? '') || (a.time ?? '').localeCompare(b.time ?? '')
}

const groupCards = computed(() => {
  const groups = new Map()
  for (const r of props.rows) {
    const key = r.group ?? null
    if (!groups.has(key)) groups.set(key, { name: key, teams: [], matches: [] })
    groups.get(key).teams.push(r)
  }
  for (const m of props.matches) {
    const g = groupOf(m)
    if (g !== undefined) groups.get(g).matches.push(m)
  }
  return [...groups.values()].map(g => ({
    name: g.name,
    teams: g.teams,
    total: g.matches.length,
    played: g.matches.filter(isPlayed).toSorted(byDate)
  }))
})

const otherResults = computed(() =>
  props.matches.filter(m => isPlayed(m) && groupOf(m) === undefined).toSorted(byDate))

function matchAt(rowId, colId) {
  return rowId === colId ? null : matchByPair.value.get(`${rowId}-${colId}`) ?? null
}

function isResult(m) {
  return m != null && isPlayed(m)
}

function shortName(name) {
  return name.slice(0, 3).toUpperCase()
}

// "2026-10-12" -> "12/10"
function shortDate(date) {
  if (!date) return '—'
  const [, month, day] = date.split('-')
  return `${day}/${month}`
}

function cellLabel(m) {
  return `${m.score1}-${m.score2}`
}

function cellTitle(m) {
  if (!m) return ''
  const score = isPlayed(m) ? ` ${m.score1}-${m.score2}` : ''
  const date = m.date ? ` (${m.date})` : ''
  return `${m.team1Name}${score} ${m.team2Name}${date}`
}

function cellClass(rowId, colId) {
  if (rowId === colId) return 'is-diagonal'
  const m = matchAt(rowId, colId)
  if (!m) return 'is-empty'
  if (!isPlayed(m)) return 'is-scheduled'
  if (m.score1 > m.score2) return 'is-win'
  if (m.score1 < m.score2) return 'is-loss'
  return 'is-draw'
}

function startEdit(m) {
  if (!m) return
  editing.value = m
  editScore1.value = m.score1
  editScore2.value = m.score2
}

async function confirmEdit() {
  const m = editing.value
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
    editing.value = null
    emit('saved')
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de l'enregistrement du score."
  }
}
</script>

<style scoped>
.group-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(min(100%, 520px), 1fr));
  gap: 18px;
  margin-bottom: 18px;
}

.group-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.other-card {
  margin-bottom: 18px;
}

.group-card-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 18px 10px;
}

.group-card-head h3 {
  margin: 0;
  font-family: var(--font-display);
  font-size: 1.2em;
  letter-spacing: 0.01em;
}

.group-progress {
  font-size: 0.78em;
  font-weight: 600;
  color: var(--text-muted);
  background: var(--surface-muted);
  padding: 3px 10px;
  border-radius: 999px;
  white-space: nowrap;
}

/* ---- Grille croisee ---- */
.xtable-scroll {
  overflow-x: auto;
  padding: 0 18px 14px;
}

/* Remise a zero du style global des <table> (carte, ombre, largeur 100%). */
table.xtable {
  width: 100%;
  margin: 0;
  border: none;
  border-radius: 0;
  box-shadow: none;
  border-collapse: separate;
  border-spacing: 4px;
  background: transparent;
}

table.xtable th,
table.xtable td {
  border: none;
  padding: 0;
  font-size: 0.86em;
}

table.xtable thead th {
  background: none;
  text-transform: none;
  letter-spacing: normal;
}

.xtable-corner {
  text-align: left;
  vertical-align: bottom;
}

.corner-hint {
  font-size: 0.72em;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.xtable-col {
  min-width: 52px;
  text-align: center;
  vertical-align: bottom;
  padding-bottom: 2px;
}

.xtable-col .col-code {
  display: block;
  margin-top: 4px;
  font-size: 0.78em;
  font-weight: 700;
  color: var(--text-muted);
  letter-spacing: 0.04em;
}

.xtable-corner,
.xtable-row {
  width: 1%;
}

.xtable-row {
  text-align: left;
  padding-right: 10px !important;
  white-space: nowrap;
}

.row-name {
  font-weight: 600;
}

table.xtable tbody tr:hover td,
table.xtable tbody tr:hover th {
  background: none;
}

.xtable-cell {
  height: 38px;
  text-align: center;
  border-radius: 8px;
  background: var(--surface-muted);
  transition: box-shadow 0.15s;
}

.xtable-cell.is-diagonal {
  background: repeating-linear-gradient(135deg, var(--surface-muted) 0 5px, var(--border) 5px 6px);
}

.xtable-cell.is-empty {
  background: transparent;
}

.xtable-cell.is-scheduled,
.xtable-cell.is-win,
.xtable-cell.is-draw,
.xtable-cell.is-loss {
  cursor: pointer;
}

.xtable-cell.is-scheduled:hover,
.xtable-cell.is-win:hover,
.xtable-cell.is-draw:hover,
.xtable-cell.is-loss:hover,
.xtable-cell:focus-visible {
  outline: none;
  box-shadow: inset 0 0 0 2px var(--primary);
}

.xtable-cell.is-win { background: #dcf5e6; color: var(--primary-dark); }
.xtable-cell.is-draw { background: #fbf3c7; color: #7a5d00; }
.xtable-cell.is-loss { background: #fbe0de; color: #a8322a; }

.score-pill {
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}

.cell-date {
  font-size: 0.82em;
  color: var(--text-muted);
}

.cell-edit {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 0 4px;
}

.cell-edit input {
  width: 38px;
  padding: 3px;
  text-align: center;
}

.cell-edit button {
  padding: 3px 7px;
}

.cell-edit .cell-cancel {
  background: var(--surface);
  color: var(--text-muted);
  border: 1px solid var(--border);
  box-shadow: none;
}

/* ---- Liste des matchs joues ---- */
.result-list {
  list-style: none;
  margin: auto 0 0;
  padding: 6px 0;
  border-top: 1px solid var(--border);
  background: #fafcfb;
}

.result-row {
  display: grid;
  grid-template-columns: 64px 1fr auto 1fr;
  align-items: center;
  gap: 12px;
  padding: 7px 18px;
  font-size: 0.9em;
}

.result-row + .result-row {
  border-top: 1px dashed var(--border);
}

.result-meta {
  display: flex;
  flex-direction: column;
  font-size: 0.8em;
  font-weight: 600;
  color: var(--text-muted);
}

.result-meta small {
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.result-team {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  color: var(--text-muted);
}

.result-team--home {
  justify-content: flex-end;
  text-align: right;
}

.result-team.winner {
  color: var(--text);
  font-weight: 700;
}

.result-score {
  min-width: 58px;
  text-align: center;
  padding: 4px 10px;
  border-radius: 8px;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
  color: white;
  background: linear-gradient(135deg, var(--pitch-800), var(--primary));
}

.score-sep {
  margin: 0 5px;
  opacity: 0.6;
}

.group-empty {
  margin: auto 0 0;
  padding: 12px 18px;
  border-top: 1px solid var(--border);
  font-size: 0.86em;
  color: var(--text-muted);
  background: #fafcfb;
}

@media (max-width: 560px) {
  .result-row {
    grid-template-columns: 1fr auto 1fr;
    padding: 7px 12px;
  }

  .result-meta {
    display: none;
  }
}
</style>

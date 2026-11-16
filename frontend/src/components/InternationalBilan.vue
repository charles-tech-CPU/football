<template>
  <div v-if="stats" class="bilan">
    <div class="bilan-toolbar">
      <div class="tab-bar">
        <button type="button" class="tab-btn" :class="{ active: view === 'matchs' }" @click="view = 'matchs'">Bilan des matchs</button>
        <button type="button" class="tab-btn" :class="{ active: view === 'parcours' }" @click="view = 'parcours'">Parcours en phase finale</button>
      </div>
      <input v-model="search" class="bilan-search" aria-label="Filtrer par sélection" placeholder="Filtrer par sélection..." />
    </div>

    <div v-if="view === 'matchs'" class="table-scroll">
      <table class="bilan-table">
        <thead>
          <tr>
            <th rowspan="2">#</th>
            <th rowspan="2">Sélection</th>
            <th
              v-for="g in groups"
              :key="g.key"
              :colspan="RECORD_COLUMNS.length + 1"
              class="bilan-group sortable"
              :class="{ sorted: sortBy === g.key }"
              :title="`Trier sur le bilan ${g.title.toLowerCase()}`"
              @click="sortBy = g.key"
            >
              {{ g.title }}{{ sortBy === g.key ? ' ▼' : '' }}
            </th>
          </tr>
          <tr>
            <template v-for="g in groups" :key="g.key">
              <th class="bilan-first" :title="g.key === 'finals' ? 'Participations à la phase finale' : 'Campagnes de qualification jouées'">Éd.</th>
              <th v-for="c in RECORD_COLUMNS" :key="c.key" :title="c.title">{{ c.label }}</th>
            </template>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in records" :key="r.teamId">
            <td class="muted">{{ r.rank }}</td>
            <td class="bilan-team"><span class="team-cell"><FlagIcon :country="r.teamName" /> {{ r.teamName }}</span></td>
            <template v-for="g in groups" :key="g.key">
              <template v-if="r[g.key]">
                <td class="bilan-first">{{ r[g.key].editions }}</td>
                <td v-for="c in RECORD_COLUMNS" :key="c.key" :class="c.cls">{{ c.value(r[g.key]) }}</td>
              </template>
              <td v-else class="bilan-first bilan-zero" :colspan="RECORD_COLUMNS.length + 1">—</td>
            </template>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else class="table-scroll">
      <table class="bilan-table">
        <thead>
          <tr>
            <th>Sélection</th>
            <th title="Participations à la phase finale">Part.</th>
            <th v-for="c in journeyColumns" :key="c.key" :title="c.title">{{ c.label }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="j in journeys" :key="j.teamId">
            <td class="bilan-team">
              <span class="team-cell"><FlagIcon :country="j.teamName" /> {{ j.teamName }}</span>
              <span v-if="j.titleEditions.length" class="bilan-titles">{{ j.titleEditions.map(editionLabel).join(', ') }}</span>
            </td>
            <td>{{ j.editions }}</td>
            <td v-for="c in journeyColumns" :key="c.key" :class="{ 'bilan-zero': !j[c.key] }">{{ j[c.key] || '·' }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <p class="section-intro small">
      V / N / P : victoires, nuls, défaites (matchs gagnés aux tirs au but comptés comme nuls). Pts : 3 points la
      victoire, 1 le nul. Clique sur « Phase finale » ou « Qualifications » pour trier. Stade atteint : tour le plus
      avancé joué (le match pour la 3e place compte comme une demi-finale ; une 2e phase de groupes compte selon son
      nombre d'équipes).
    </p>
  </div>
  <p v-else-if="error" class="error-text">{{ error }}</p>
</template>

<script setup>
// Bilan historique d'une competition de selections (cf. InternationalStatsService) : un tableau
// compact par selection (phase finale et qualifications cote a cote) et le parcours en phase finale.
import { computed, ref, watch } from 'vue'
import api from '../services/api'
import FlagIcon from './FlagIcon.vue'
import { journeyColumnsFor, mergedRecords, rankedRows } from '../utils/internationalBilan.js'

const props = defineProps({
  family: { type: String, required: true }
})

const stats = ref(null)
const error = ref('')
const search = ref('')
const view = ref('matchs')
const sortBy = ref('finals')

const RECORD_COLUMNS = [
  { key: 'played', label: 'MJ', title: 'Matchs joués', value: r => r.played },
  { key: 'won', label: 'V', title: 'Victoires', value: r => r.won, cls: 'win' },
  { key: 'drawn', label: 'N', title: 'Nuls', value: r => r.drawn },
  { key: 'lost', label: 'P', title: 'Défaites', value: r => r.lost, cls: 'loss' },
  { key: 'goalsFor', label: 'BP', title: 'Buts pour', value: r => r.goalsFor },
  { key: 'goalsAgainst', label: 'BC', title: 'Buts contre', value: r => r.goalsAgainst },
  { key: 'diff', label: 'Diff', title: 'Différence de buts', value: r => formatDiff(r.goalsFor - r.goalsAgainst) },
  { key: 'points', label: 'Pts', title: '3 points la victoire, 1 le nul', value: r => r.points, cls: 'bilan-points' }
]

const journeyColumns = computed(() => journeyColumnsFor(stats.value?.journeys ?? []))
const journeys = computed(() => rankedRows(stats.value?.journeys ?? [], search.value))
const groups = computed(() => {
  if (!stats.value) return []
  return stats.value.qualifiers.length
    ? [{ key: 'finals', title: 'Phase finale' }, { key: 'qualifiers', title: 'Qualifications' }]
    : [{ key: 'finals', title: 'Bilan' }]
})
const records = computed(() =>
  rankedRows(mergedRecords(stats.value?.finals ?? [], stats.value?.qualifiers ?? [], sortBy.value), search.value))

// "1959B" -> "1959 (bis)", "1819" (Ligue des Nations) -> "2018-2019".
function editionLabel(edition) {
  if (edition.endsWith('B')) return `${edition.slice(0, -1)} (bis)`
  if (props.family === 'UNL' || props.family === 'CNL') return `20${edition.slice(0, 2)}-20${edition.slice(2)}`
  return edition
}

function formatDiff(n) {
  return n > 0 ? `+${n}` : String(n)
}

async function load() {
  stats.value = null
  sortBy.value = 'finals'
  error.value = ''
  try {
    stats.value = await api.getInternationalStats(props.family)
  } catch (e) {
    error.value = e.response?.data?.error ?? 'Erreur lors du chargement du bilan.'
  }
}

watch(() => props.family, load, { immediate: true })
</script>

<style scoped>
.bilan-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}
.bilan-search {
  max-width: 260px;
}
.table-scroll {
  overflow-x: auto;
}
.bilan-table {
  width: auto;
  font-size: 13px;
  border-collapse: collapse;
}
.bilan-table th,
.bilan-table td {
  padding: 3px 6px;
  text-align: center;
  white-space: nowrap;
}
.bilan-table .bilan-team {
  text-align: left;
}
.bilan-group {
  border-bottom: 1px solid var(--border);
}
.bilan-first {
  border-left: 2px solid var(--border);
}
.sortable {
  cursor: pointer;
}
.sorted {
  color: var(--primary);
}
.bilan-points {
  font-weight: 700;
}
.bilan-titles {
  display: block;
  color: var(--text-muted);
  font-size: 11px;
}
.win {
  color: #16a34a;
}
.loss {
  color: #dc2626;
}
.muted {
  color: var(--text-muted);
}
.bilan-zero {
  color: var(--text-muted);
}
</style>

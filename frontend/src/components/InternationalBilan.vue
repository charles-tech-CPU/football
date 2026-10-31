<template>
  <div v-if="stats" class="bilan">
    <input v-model="search" class="bilan-search" aria-label="Filtrer par sélection" placeholder="Filtrer par sélection..." />

    <h2>Parcours en phase finale</h2>
    <div class="table-scroll">
      <table>
        <thead>
          <tr>
            <th>Sélection</th>
            <th title="Participations à la phase finale">Part.</th>
            <th v-for="c in journeyColumns" :key="c.key" :title="c.title">{{ c.label }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="j in journeys" :key="j.teamId">
            <td>
              <span class="team-cell"><FlagIcon :country="j.teamName" /> {{ j.teamName }}</span>
              <span v-if="j.titleEditions.length" class="bilan-titles">{{ j.titleEditions.map(editionLabel).join(', ') }}</span>
            </td>
            <td>{{ j.editions }}</td>
            <td v-for="c in journeyColumns" :key="c.key" :class="{ 'bilan-zero': !j[c.key] }">{{ j[c.key] || '·' }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <template v-for="section in recordSections" :key="section.title">
      <h2>{{ section.title }}</h2>
      <div class="table-scroll">
        <table>
          <thead>
            <tr>
              <th>#</th>
              <th>Sélection</th>
              <th title="Éditions jouées">Éd.</th>
              <th>MJ</th>
              <th>V</th>
              <th>N</th>
              <th>D</th>
              <th>BP</th>
              <th>BC</th>
              <th>Diff</th>
              <th title="3 points la victoire, 1 le nul">Pts</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in section.rows" :key="r.teamId">
              <td>{{ r.rank }}</td>
              <td><span class="team-cell"><FlagIcon :country="r.teamName" /> {{ r.teamName }}</span></td>
              <td>{{ r.editions }}</td>
              <td>{{ r.played }}</td>
              <td>{{ r.won }}</td>
              <td>{{ r.drawn }}</td>
              <td>{{ r.lost }}</td>
              <td>{{ r.goalsFor }}</td>
              <td>{{ r.goalsAgainst }}</td>
              <td>{{ formatDiff(r.goalsFor - r.goalsAgainst) }}</td>
              <td><strong>{{ r.points }}</strong></td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
    <p class="section-intro">
      Stade atteint : tour le plus avancé joué (le match pour la 3e place compte comme une demi-finale ;
      une 2e phase de groupes compte selon son nombre d'équipes). Bilans à 3 points la victoire, matchs
      gagnés aux tirs au but comptés comme nuls.
    </p>
  </div>
  <p v-else-if="error" class="error-text">{{ error }}</p>
</template>

<script setup>
// Bilan historique d'une competition de selections (cf. InternationalStatsService) : parcours en
// phase finale, bilan en phase finale et en qualifications, toutes editions confondues.
import { computed, ref, watch } from 'vue'
import api from '../services/api'
import FlagIcon from './FlagIcon.vue'
import { journeyColumnsFor, rankedRows } from '../utils/internationalBilan.js'

const props = defineProps({
  family: { type: String, required: true }
})

const stats = ref(null)
const error = ref('')
const search = ref('')

const journeyColumns = computed(() => journeyColumnsFor(stats.value?.journeys ?? []))
const journeys = computed(() => rankedRows(stats.value?.journeys ?? [], search.value))
const recordSections = computed(() => {
  if (!stats.value) return []
  const qualifiers = stats.value.qualifiers.length
  return [
    { title: qualifiers ? 'Bilan en phase finale' : 'Bilan', rows: rankedRows(stats.value.finals, search.value) },
    ...(qualifiers ? [{ title: 'Bilan en qualifications', rows: rankedRows(stats.value.qualifiers, search.value) }] : [])
  ]
})

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
.bilan-search {
  max-width: 320px;
  margin-bottom: 8px;
}
.table-scroll {
  overflow-x: auto;
}
.bilan-titles {
  display: block;
  color: var(--text-muted);
  font-size: 12px;
}
.bilan-zero {
  color: var(--text-muted);
}
</style>

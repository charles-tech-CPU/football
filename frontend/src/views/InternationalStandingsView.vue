<template>
  <section class="hero">
    <span class="hero-eyebrow">Sélections nationales</span>
    <h1>Classements internationaux</h1>
    <p class="section-intro">
      Classements des compétitions de sélections nationales, recalculés en direct comme pour les championnats de
      clubs : saison 2026-2027 en cours, et historique des grandes compétitions (phases finales et qualifications).
    </p>
  </section>

  <InternationalNav />

  <div class="tab-bar">
    <button
      v-for="c in CONFEDERATIONS"
      :key="c.code"
      class="tab-btn"
      :class="{ active: activeConfederation === c.code }"
      @click="selectConfederation(c.code)"
    >{{ c.label }}</button>
  </div>

  <div v-if="editionOptions.length" class="filters">
    <select v-model="edition" aria-label="Édition">
      <option v-if="currentCompetitions.length" :value="CURRENT">Saison {{ currentSeasonLabel }} (en cours)</option>
      <optgroup v-for="g in historyGroups" :key="g.label" :label="g.label">
        <option v-for="c in g.competitions" :key="c.id" :value="c.id">{{ c.name }}</option>
      </optgroup>
    </select>
  </div>

  <div class="tab-bar sub-tab-bar">
    <button class="tab-btn" :class="{ active: activeView === 'classement' }" @click="activeView = 'classement'">Classements</button>
    <button class="tab-btn" :class="{ active: activeView === 'resultats' }" @click="activeView = 'resultats'">Résultats</button>
  </div>

  <template v-if="activeCompetitions.length">
    <div v-for="comp in activeCompetitions" :key="comp.id" class="competition-block">
      <h2>{{ comp.name }}</h2>
      <InternationalResults
        v-if="activeView === 'resultats'"
        :matches="matchesByCompetition[comp.id] ?? []"
        :rows="standingsByCompetition[comp.id] ?? []"
        @saved="loadFor([comp])"
      />
      <template v-else>
        <StandingsTable
          v-if="(standingsByCompetition[comp.id] ?? []).length"
          :rows="standingsByCompetition[comp.id]"
          :show-flags="true"
          :show-logos="false"
          :points-first="true"
          :show-base-legend="false"
          :group-row-classes="(groupName, rows) => internationalRowClasses(comp.code, groupName, rows)"
        />
        <p v-else-if="loadedIds.has(comp.id)" class="empty-state">
          Pas de phase de groupes dans cette compétition : voir l'onglet « Résultats ».
        </p>
        <ul v-if="internationalLegend(comp.code).length" class="standings-legend">
          <li v-for="(item, i) in internationalLegend(comp.code)" :key="i">
            <span class="legend-swatch" :class="item.legendClass"></span>{{ item.label }}
          </li>
        </ul>
      </template>
    </div>
  </template>
  <p v-else-if="loaded" class="empty-state">Aucune compétition pour cette confédération.</p>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../services/api'
import StandingsTable from '../components/StandingsTable.vue'
import InternationalResults from '../components/InternationalResults.vue'
import InternationalNav from '../components/InternationalNav.vue'
import { CONFEDERATIONS, confederationForCompetitionCode } from '../utils/confederations'
import { internationalLegend, internationalRowClasses } from '../utils/internationalQualification'

// Edition affichee : toutes les competitions de la saison en cours de la confederation (CURRENT),
// ou une seule competition de l'historique (son id). Dans l'URL (?competition=12) pour les liens
// depuis les listes de matchs (fiche club, tete-a-tete).
const CURRENT = 'current'

const route = useRoute()
const router = useRouter()

const competitions = ref([])
const standingsByCompetition = ref({})
const matchesByCompetition = ref({})
const loadedIds = ref(new Set())
const activeView = ref('classement')
const activeConfederation = ref(CONFEDERATIONS[0].code)
const edition = ref(CURRENT)
const loaded = ref(false)

const currentSeason = computed(() => Math.max(...competitions.value.map(c => c.season ?? 0)))
const currentSeasonLabel = computed(() => `${currentSeason.value - 1}-${currentSeason.value}`)

const confederationCompetitions = computed(() =>
  competitions.value.filter(c => confederationForCompetitionCode(c.code)?.code === activeConfederation.value))

const currentCompetitions = computed(() =>
  confederationCompetitions.value.filter(c => c.season === currentSeason.value))

// Historique de la confederation, par annee (la plus recente d'abord).
const historyGroups = computed(() => {
  const byYear = new Map()
  const history = confederationCompetitions.value
    .filter(c => c.season !== currentSeason.value)
    .toSorted((a, b) => b.season - a.season || a.name.localeCompare(b.name))
  for (const c of history) {
    const label = String(c.season)
    if (!byYear.has(label)) byYear.set(label, [])
    byYear.get(label).push(c)
  }
  return [...byYear.entries()].map(([label, comps]) => ({ label, competitions: comps }))
})

const editionOptions = computed(() => [
  ...(currentCompetitions.value.length ? [CURRENT] : []),
  ...historyGroups.value.flatMap(g => g.competitions.map(c => c.id))
])

const activeCompetitions = computed(() => {
  if (edition.value === CURRENT) return currentCompetitions.value
  return confederationCompetitions.value.filter(c => c.id === edition.value)
})

function selectConfederation(code) {
  activeConfederation.value = code
  edition.value = editionOptions.value[0] ?? CURRENT
}

// Classement + matchs (pour l'onglet Resultats) de chaque competition, recharges ensemble
// pour que la grille des resultats suive toujours l'ordre du classement.
async function loadFor(comps) {
  const entries = await Promise.all(comps.map(async c => [c.id, await Promise.all([
    api.getStandings(c.id),
    api.getMatchesByCompetition(c.id)
  ])]))
  standingsByCompetition.value = { ...standingsByCompetition.value, ...Object.fromEntries(entries.map(([id, [s]]) => [id, s])) }
  matchesByCompetition.value = { ...matchesByCompetition.value, ...Object.fromEntries(entries.map(([id, [, m]]) => [id, m])) }
  loadedIds.value = new Set([...loadedIds.value, ...comps.map(c => c.id)])
}

function applyRouteCompetition() {
  const id = Number(route.query.competition)
  const comp = competitions.value.find(c => c.id === id)
  if (!comp) return false
  activeConfederation.value = confederationForCompetitionCode(comp.code)?.code ?? activeConfederation.value
  edition.value = comp.season === currentSeason.value ? CURRENT : comp.id
  return true
}

async function load() {
  loaded.value = false
  competitions.value = await api.getCompetitions({ type: 'INTERNATIONAL' })
  if (!applyRouteCompetition()) edition.value = editionOptions.value[0] ?? CURRENT
  await loadFor(activeCompetitions.value)
  loaded.value = true
}

watch(activeCompetitions, comps => {
  if (!loaded.value) return
  router.replace({ query: edition.value === CURRENT ? {} : { competition: edition.value } })
  loadFor(comps.filter(c => !loadedIds.value.has(c.id)))
})

watch(() => route.query.competition, () => {
  if (loaded.value) applyRouteCompetition()
})

onMounted(load)
</script>

<style scoped>
.competition-block {
  margin-bottom: 32px;
}
</style>

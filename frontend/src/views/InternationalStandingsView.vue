<template>
  <section class="hero">
    <span class="hero-eyebrow">Sélections nationales</span>
    <h1>Classements internationaux</h1>
    <p class="section-intro">Classements des compétitions de sélections nationales, saison 2026-2027, recalculés en direct comme pour les championnats de clubs.</p>
  </section>

  <div class="tab-bar">
    <button
      v-for="c in availableConfederations"
      :key="c.code"
      class="tab-btn"
      :class="{ active: activeConfederation === c.code }"
      @click="activeConfederation = c.code"
    >{{ c.label }}</button>
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
          :rows="standingsByCompetition[comp.id] ?? []"
          :show-flags="true"
          :show-logos="false"
          :points-first="true"
          :show-base-legend="false"
          :group-row-classes="(groupName, rows) => internationalRowClasses(comp.code, groupName, rows)"
        />
        <ul v-if="internationalLegend(comp.code).length" class="standings-legend">
          <li v-for="(item, i) in internationalLegend(comp.code)" :key="i">
            <span class="legend-swatch" :class="item.legendClass"></span>{{ item.label }}
          </li>
        </ul>
      </template>
    </div>
  </template>
  <p v-else-if="loaded" class="empty-state">Aucune compétition programmée pour cette confédération pour la saison 2026-2027.</p>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import api from '../services/api'
import StandingsTable from '../components/StandingsTable.vue'
import InternationalResults from '../components/InternationalResults.vue'
import { CONFEDERATIONS, confederationForCompetitionCode } from '../utils/confederations'
import { internationalLegend, internationalRowClasses } from '../utils/internationalQualification'

const competitions = ref([])
const standingsByCompetition = ref({})
const matchesByCompetition = ref({})
const activeView = ref('classement')
const activeConfederation = ref(CONFEDERATIONS[0].code)
const loaded = ref(false)

const availableConfederations = computed(() => CONFEDERATIONS)

const activeCompetitions = computed(() =>
  competitions.value.filter(c => confederationForCompetitionCode(c.code)?.code === activeConfederation.value)
)

// Classement + matchs (pour l'onglet Resultats) de chaque competition, recharges ensemble
// pour que la grille des resultats suive toujours l'ordre du classement.
async function loadFor(comps) {
  const entries = await Promise.all(comps.map(async c => [c.id, await Promise.all([
    api.getStandings(c.id),
    api.getMatchesByCompetition(c.id)
  ])]))
  standingsByCompetition.value = { ...standingsByCompetition.value, ...Object.fromEntries(entries.map(([id, [s]]) => [id, s])) }
  matchesByCompetition.value = { ...matchesByCompetition.value, ...Object.fromEntries(entries.map(([id, [, m]]) => [id, m])) }
}

async function load() {
  loaded.value = false
  competitions.value = await api.getCompetitions({ type: 'INTERNATIONAL' })
  await loadFor(activeCompetitions.value)
  loaded.value = true
}

watch(activeConfederation, () => loadFor(activeCompetitions.value))

onMounted(load)
</script>

<style scoped>
.competition-block {
  margin-bottom: 32px;
}
</style>

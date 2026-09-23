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

  <template v-if="activeCompetitions.length">
    <div v-for="comp in activeCompetitions" :key="comp.id" class="competition-block">
      <h2>{{ comp.name }}</h2>
      <StandingsTable
        :rows="standingsByCompetition[comp.id] ?? []"
        :show-flags="true"
        :show-base-legend="false"
        :group-row-classes="(groupName, rows) => internationalRowClasses(comp.code, groupName, rows)"
      />
      <ul v-if="internationalLegend(comp.code).length" class="standings-legend">
        <li v-for="(item, i) in internationalLegend(comp.code)" :key="i">
          <span class="legend-swatch" :class="item.legendClass"></span>{{ item.label }}
        </li>
      </ul>
    </div>
  </template>
  <p v-else-if="loaded" class="empty-state">Aucune compétition programmée pour cette confédération pour la saison 2026-2027.</p>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import api from '../services/api'
import StandingsTable from '../components/StandingsTable.vue'
import { CONFEDERATIONS, confederationForCompetitionCode } from '../utils/confederations'
import { internationalLegend, internationalRowClasses } from '../utils/internationalQualification'

const competitions = ref([])
const standingsByCompetition = ref({})
const activeConfederation = ref(CONFEDERATIONS[0].code)
const loaded = ref(false)

const availableConfederations = computed(() => CONFEDERATIONS)

const activeCompetitions = computed(() =>
  competitions.value.filter(c => confederationForCompetitionCode(c.code)?.code === activeConfederation.value)
)

async function loadStandingsFor(comps) {
  const entries = await Promise.all(comps.map(async c => [c.id, await api.getStandings(c.id)]))
  standingsByCompetition.value = { ...standingsByCompetition.value, ...Object.fromEntries(entries) }
}

async function load() {
  loaded.value = false
  competitions.value = await api.getCompetitions({ type: 'INTERNATIONAL' })
  await loadStandingsFor(activeCompetitions.value)
  loaded.value = true
}

watch(activeConfederation, () => loadStandingsFor(activeCompetitions.value))

onMounted(load)
</script>

<style scoped>
.competition-block {
  margin-bottom: 32px;
}
</style>

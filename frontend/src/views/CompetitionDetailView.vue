<template>
  <router-link class="back-link" to="/">← Toutes les compétitions</router-link>
  <div class="page-header">
    <h1>{{ competition?.name ?? '...' }}</h1>
  </div>
  <p class="section-intro" v-if="competition">
    <span class="badge badge-continental">Coupe d'Europe</span>
  </p>

  <div class="tab-bar" v-if="competition">
    <button class="tab-btn" :class="{ active: activeTab === 'classement' }" @click="activeTab = 'classement'">Classement</button>
    <button class="tab-btn" :class="{ active: activeTab === 'resultats' }" @click="activeTab = 'resultats'">Résultats</button>
    <button class="tab-btn" :class="{ active: activeTab === 'qualifs' }" @click="activeTab = 'qualifs'">Qualifications</button>
    <button class="tab-btn" :class="{ active: activeTab === 'finale' }" @click="activeTab = 'finale'">Phase finale</button>
  </div>

  <template v-if="competition">
    <template v-if="activeTab === 'classement'">
      <StandingsTable
        :rows="leaguePhaseStandings"
        :show-base-legend="false"
        :show-flags="true"
        :rank-bands="[{ count: 8, class: 'standing-blue' }, { count: 16, class: 'standing-green' }]"
      />
      <ul class="standings-legend" v-if="leaguePhaseStandings.length">
        <li><span class="legend-swatch legend-blue"></span>1re-8e : qualifié direct (8es de finale)</li>
        <li><span class="legend-swatch legend-green"></span>9e-24e : barrage</li>
        <li><span class="legend-swatch legend-red"></span>25e-36e : éliminé</li>
      </ul>
      <p class="section-intro" v-if="!leaguePhaseStandings.length">
        Aucun match de phase de ligue joué pour l'instant.
      </p>
    </template>

    <template v-else-if="activeTab === 'resultats'">
      <ResultsGrid
        :competition-id="competition.id"
        :round-includes="['PHASE DE LIGUE']"
        round="PHASE DE LIGUE"
        :show-flags="true"
        allow-add
        default-round-label="PHASE DE LIGUE"
      />
    </template>

    <template v-else-if="activeTab === 'qualifs'">
      <CupBracket
        :competition-id="competition.id"
        :round-includes="['QUALIF', 'BARRAGE']"
        :explicit-rounds="[
          { frag: '1ER TOUR', label: '1er tour qualificatif' },
          { frag: '2E TOUR', label: '2e tour qualificatif' },
          { frag: '3E TOUR', label: '3e tour qualificatif' },
          { frag: 'BARRAGE', label: 'Barrage' }
        ]"
        :show-flags="true"
      />
    </template>

    <template v-else-if="activeTab === 'finale'">
      <CupBracket
        :competition-id="competition.id"
        :round-includes="['FINALE', 'SEIZIEME', 'HUITIEME', 'HUITEME', 'QUART', 'DEMI']"
        :show-flags="true"
      />
    </template>
  </template>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import api from '../services/api'
import CupBracket from '../components/CupBracket.vue'
import StandingsTable from '../components/StandingsTable.vue'
import ResultsGrid from '../components/ResultsGrid.vue'

const props = defineProps({
  id: { type: [String, Number], required: true }
})

const competition = ref(null)
const activeTab = ref('classement')
const leaguePhaseStandings = ref([])

async function load() {
  const competitionId = Number(props.id)
  const competitions = await api.getCompetitions()
  competition.value = competitions.find(c => c.id === competitionId) ?? null
  leaguePhaseStandings.value = competition.value
    ? await api.getStandings(competition.value.id, 'PHASE DE LIGUE')
    : []
}

watch(() => props.id, load)
onMounted(load)
</script>

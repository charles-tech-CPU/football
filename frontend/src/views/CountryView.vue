<template>
  <router-link class="back-link" to="/">← Toutes les compétitions</router-link>
  <div class="page-header">
    <FlagIcon :country="country" />
    <h1>{{ country }}</h1>
  </div>
  <p class="section-intro" v-if="loaded">Saison {{ season }}</p>

  <div class="tab-bar">
    <button class="tab-btn" :class="{ active: activeTab === 'classement' }" @click="activeTab = 'classement'">Classement</button>
    <button class="tab-btn" :class="{ active: activeTab === 'championnat' }" @click="activeTab = 'championnat'">Championnat</button>
    <button class="tab-btn" :class="{ active: activeTab === 'avenir' }" @click="activeTab = 'avenir'">À venir</button>
    <button class="tab-btn" :class="{ active: activeTab === 'resultats' }" @click="activeTab = 'resultats'">Résultats</button>
    <button class="tab-btn" :class="{ active: activeTab === 'coupe' }" @click="activeTab = 'coupe'">Coupe</button>
  </div>

  <template v-if="loaded">
    <template v-if="activeTab === 'championnat'">
      <template v-if="league">
        <CompetitionMatches :competition-id="league.id" />
      </template>
      <p v-else class="empty-state">Pas de championnat importé pour ce pays.</p>
    </template>

    <template v-else-if="activeTab === 'avenir'">
      <template v-if="league">
        <UpcomingMatches :competition-id="league.id" />
      </template>
      <p v-else class="empty-state">Pas de championnat importé pour ce pays.</p>
    </template>

    <template v-else-if="activeTab === 'resultats'">
      <template v-if="league">
        <ResultsGrid :competition-id="league.id" />
      </template>
      <p v-else class="empty-state">Pas de championnat importé pour ce pays.</p>
    </template>

    <template v-else-if="activeTab === 'coupe'">
      <template v-if="cup">
        <CupBracket :competition-id="cup.id" />
      </template>
      <p v-else class="empty-state">Pas de coupe nationale importée pour ce pays.</p>
    </template>

    <template v-else-if="activeTab === 'classement'">
      <template v-if="league">
        <StandingsTable
          :rows="standings"
          :team-statuses="teamStatusMap"
          :ldc-slots="league.ldcSlots"
          :el-slots="league.elSlots"
          :ecl-slots="league.eclSlots"
        />

        <details class="add-form">
          <summary>Configurer les places qualificatives et statuts</summary>

          <form class="inline" @submit.prevent="saveSlots">
            <label>Places LDC
              <input class="score-input" type="number" min="0" v-model.number="slots.ldcSlots" />
            </label>
            <label>Places Europa League
              <input class="score-input" type="number" min="0" v-model.number="slots.elSlots" />
            </label>
            <label>Places Conference League
              <input class="score-input" type="number" min="0" v-model.number="slots.eclSlots" />
            </label>
            <button type="submit">Enregistrer</button>
          </form>

          <p class="section-intro">
            Groupe de 2e phase (ex: "Championnat" / "Relégation") : laisse vide tant que la compétition
            n'est pas scindée en plusieurs mini-championnats. Une fois renseigné pour au moins une équipe,
            le classement se sépare automatiquement par groupe.
          </p>

          <table>
            <thead>
              <tr>
                <th>Équipe</th>
                <th>Statuts</th>
                <th>Groupe (2e phase)</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in standings" :key="row.teamId">
                <td>{{ row.teamName }}</td>
                <td>
                  <StatusDropdown
                    :model-value="statusEdits[row.teamId]"
                    @update:model-value="v => saveStatus(row.teamId, v)"
                  />
                </td>
                <td>
                  <input
                    class="group-input"
                    v-model="statusEdits[row.teamId].groupName"
                    placeholder="ex: Championnat"
                    @change="saveStatus(row.teamId, statusEdits[row.teamId])"
                  />
                </td>
              </tr>
            </tbody>
          </table>
        </details>
      </template>
      <p v-else class="empty-state">Le classement n'a de sens que pour un championnat.</p>
    </template>
  </template>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import api from '../services/api'
import FlagIcon from '../components/FlagIcon.vue'
import StandingsTable from '../components/StandingsTable.vue'
import CompetitionMatches from '../components/CompetitionMatches.vue'
import UpcomingMatches from '../components/UpcomingMatches.vue'
import ResultsGrid from '../components/ResultsGrid.vue'
import CupBracket from '../components/CupBracket.vue'
import StatusDropdown from '../components/StatusDropdown.vue'

const props = defineProps({
  country: { type: String, required: true }
})

const league = ref(null)
const cup = ref(null)
const standings = ref([])
const teamStatusMap = ref({})
const loaded = ref(false)
const activeTab = ref('classement')
const slots = reactive({ ldcSlots: 0, elSlots: 0, eclSlots: 0 })
const statusEdits = reactive({})

const season = computed(() => league.value?.season ?? cup.value?.season ?? '')

async function load() {
  loaded.value = false
  const competitions = await api.getCompetitions()
  const forCountry = competitions.filter(c => c.country === props.country)
  league.value = forCountry.find(c => c.type === 'LEAGUE') ?? null
  cup.value = forCountry.find(c => c.type === 'DOMESTIC_CUP') ?? null

  if (activeTab.value === 'classement' && !league.value && cup.value) activeTab.value = 'coupe'

  if (league.value) {
    slots.ldcSlots = league.value.ldcSlots
    slots.elSlots = league.value.elSlots
    slots.eclSlots = league.value.eclSlots

    const [standingsList, statuses] = await Promise.all([
      api.getStandings(league.value.id),
      api.getTeamStatuses(league.value.id)
    ])
    standings.value = standingsList

    const map = {}
    for (const s of statuses) map[s.teamId] = s
    teamStatusMap.value = map

    for (const key of Object.keys(statusEdits)) delete statusEdits[key]
    for (const row of standingsList) {
      const existing = map[row.teamId]
      statusEdits[row.teamId] = {
        defendingChampion: existing?.defendingChampion ?? false,
        promoted: existing?.promoted ?? false,
        previousCupWinner: existing?.previousCupWinner ?? false,
        groupName: existing?.groupName ?? ''
      }
    }
  } else {
    standings.value = []
    teamStatusMap.value = {}
  }
  loaded.value = true
}

async function saveSlots() {
  const updated = await api.updateQualificationSlots(league.value.id, { ...slots })
  league.value = { ...league.value, ...updated }
}

async function saveStatus(teamId, value) {
  statusEdits[teamId] = value
  const payload = { ...value, groupName: value.groupName || null }
  const updated = await api.setTeamStatus(league.value.id, teamId, payload)
  teamStatusMap.value = { ...teamStatusMap.value, [teamId]: updated }
}

watch(() => props.country, load)
onMounted(load)
</script>

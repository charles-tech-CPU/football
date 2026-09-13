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
        <div class="tab-bar sub-tab-bar">
          <button class="tab-btn" :class="{ active: avenirSubTab === 'championnat' }" @click="avenirSubTab = 'championnat'">Championnat</button>
          <button class="tab-btn" v-if="cup" :class="{ active: avenirSubTab === 'coupe' }" @click="avenirSubTab = 'coupe'">Coupe</button>
          <button class="tab-btn" :class="{ active: avenirSubTab === 'barrage' }" @click="avenirSubTab = 'barrage'">Barrage</button>
        </div>
        <UpcomingMatches v-if="avenirSubTab === 'championnat'" :competition-id="league.id" :round-excludes="['BARRAGE']" />
        <UpcomingMatches v-else-if="avenirSubTab === 'coupe' && cup" :competition-id="cup.id" />
        <UpcomingMatches v-else-if="avenirSubTab === 'barrage'" :competition-id="league.id" :round-includes="['BARRAGE']" />
      </template>
      <p v-else class="empty-state">Pas de championnat importé pour ce pays.</p>
    </template>

    <template v-else-if="activeTab === 'resultats'">
      <template v-if="league">
        <ResultsGrid
          :competition-id="league.id"
          :expect-second-phase="league.code === 'ALBANIE'"
          :group-split="rankConfig?.resultsGroupSplit"
        />
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
          :rows="rankConfig?.resultsGroupSplit ? frozenStandings : standings"
          :team-statuses="teamStatusMap"
          :ldc-slots="rankConfig?.resultsGroupSplit ? 0 : league.ldcSlots"
          :el-slots="rankConfig?.resultsGroupSplit ? 0 : league.elSlots"
          :ecl-slots="rankConfig?.resultsGroupSplit ? 0 : league.eclSlots"
          :relegation-slots="rankConfig?.resultsGroupSplit ? 0 : league.relegationSlots"
          :barrage-slots="rankConfig?.resultsGroupSplit ? 0 : league.barrageSlots"
          :rank-markers="rankConfig?.rankMarkers"
        />
        <ul class="standings-legend" v-if="rankConfig?.rankMarkers">
          <li v-for="(item, ii) in rankConfig.legend" :key="ii">{{ item }}</li>
        </ul>

        <template v-if="rankConfig?.resultsGroupSplit">
          <div v-for="(grp, gi) in realGroupStandings" :key="`grp-${gi}`" class="standings-group">
            <h2>{{ grp.label }}</h2>
            <StandingsTable
              :rows="grp.rows"
              :team-statuses="teamStatusMap"
              :show-base-legend="false"
              :ldc-slots="grp.slots.ldcSlots"
              :el-slots="grp.slots.elSlots"
              :ecl-slots="grp.slots.eclSlots"
              :barrage-slots="grp.slots.barrageSlots"
              :relegation-slots="grp.slots.relegationSlots"
              :rank-markers="grp.slots.rankMarkers"
            />
          </div>
        </template>

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
            <label>Places barrage de maintien
              <input class="score-input" type="number" min="0" v-model.number="slots.barrageSlots" />
            </label>
            <label>Places de relégation
              <input class="score-input" type="number" min="0" v-model.number="slots.relegationSlots" />
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
const avenirSubTab = ref('championnat')
const slots = reactive({ ldcSlots: 0, elSlots: 0, eclSlots: 0, relegationSlots: 0, barrageSlots: 0 })
const statusEdits = reactive({})

const season = computed(() => league.value?.season ?? cup.value?.season ?? '')

// Particularites de format propres a un championnat precis, qui ne se deduisent pas des
// places qualificatives/barrage/relegation generiques (ex: mini-championnat final entre
// les 4 meilleures equipes de l'Albanie, unique a ce championnat).
const LEAGUE_RANK_CONFIG = {
  ALBANIE: {
    rankMarkers: [
      { rank: 1, icon: '🏅', tooltip: 'Qualifié pour le mini-championnat (top 4)' },
      { rank: 2, icon: '🏅', tooltip: 'Qualifié pour le mini-championnat (top 4)' },
      { rank: 3, icon: '🏅', tooltip: 'Qualifié pour le mini-championnat (top 4)' },
      { rank: 4, icon: '🏅', tooltip: 'Qualifié pour le mini-championnat (top 4)' }
    ],
    legend: ['🏅 Qualifié pour le mini-championnat (top 4)']
  },
  AUTRICHE: {
    // Apres les 22 journees (aller-retour x2), le championnat se scinde en 2 groupes de 6 :
    // Meistergruppe (haut de tableau, 1-6) et Qualifikationsgruppe (bas de tableau, 7-12),
    // qui rejouent un tour l'un entre eux. Le grand classement ne sert qu'a repartir les
    // groupes (icones 🅰️/🅱️) : LDC/EL/ECL, barrage et relegation se jouent au classement
    // FINAL de chaque mini-championnat, donc ces icones sont portees par les tableaux de
    // groupe (groupSlots), pas par le grand classement.
    rankMarkers: [
      { rank: 1, icon: '🅰️', tooltip: 'Meistergruppe (groupe du haut, places 1 à 6)' },
      { rank: 2, icon: '🅰️', tooltip: 'Meistergruppe (groupe du haut, places 1 à 6)' },
      { rank: 3, icon: '🅰️', tooltip: 'Meistergruppe (groupe du haut, places 1 à 6)' },
      { rank: 4, icon: '🅰️', tooltip: 'Meistergruppe (groupe du haut, places 1 à 6)' },
      { rank: 5, icon: '🅰️', tooltip: 'Meistergruppe (groupe du haut, places 1 à 6)' },
      { rank: 6, icon: '🅰️', tooltip: 'Meistergruppe (groupe du haut, places 1 à 6)' },
      { rank: 7, icon: '🅱️', tooltip: 'Qualifikationsgruppe (groupe du bas, places 7 à 12)' },
      { rank: 8, icon: '🅱️', tooltip: 'Qualifikationsgruppe (groupe du bas, places 7 à 12)' },
      { rank: 9, icon: '🅱️', tooltip: 'Qualifikationsgruppe (groupe du bas, places 7 à 12)' },
      { rank: 10, icon: '🅱️', tooltip: 'Qualifikationsgruppe (groupe du bas, places 7 à 12)' },
      { rank: 11, icon: '🅱️', tooltip: 'Qualifikationsgruppe (groupe du bas, places 7 à 12)' },
      { rank: 12, icon: '🅱️', tooltip: 'Qualifikationsgruppe (groupe du bas, places 7 à 12)' }
    ],
    legend: ['🅰️ Meistergruppe (groupe du haut)', '🅱️ Qualifikationsgruppe (groupe du bas)'],
    resultsGroupSplit: {
      sizes: [6, 6],
      labels: ['Meistergruppe', 'Qualifikationsgruppe'],
      groupSlots: [
        // Meistergruppe : 1er champion (LDC), 2e LDC, 3e EL, 4e ECL, 5e barrage Conference
        // (rang isole, non contigu -> rankMarker dedie plutot que barrageSlots generique).
        {
          ldcSlots: 2, elSlots: 1, eclSlots: 1, barrageSlots: 0, relegationSlots: 0,
          rankMarkers: [{ rank: 5, icon: '🎟️', tooltip: 'Barrage pour une place en Conference League' }]
        },
        // Qualifikationsgruppe : 1er (=7e general) et 2e (=8e general) en barrage
        // Conference, dernier (=12e general) relegue.
        {
          ldcSlots: 0, elSlots: 0, eclSlots: 0, barrageSlots: 0, relegationSlots: 1,
          rankMarkers: [
            { rank: 1, icon: '🎟️', tooltip: 'Barrage pour une place en Conference League' },
            { rank: 2, icon: '🎟️', tooltip: 'Barrage pour une place en Conference League' }
          ]
        }
      ]
    }
  },
  BULGARIE: {
    // Apres la saison reguliere (14 equipes), 3 mini-championnats : top 4 pour le titre,
    // 5e-8e pour une place Conference League, 9e-14e pour le maintien. Le grand classement
    // ne sert qu'a repartir les groupes (icones 🥇/🥈/🔻) : champion/Conference/barrage/
    // relegation se jouent au classement FINAL de chaque mini-championnat (groupSlots).
    rankMarkers: [
      { rank: 1, icon: '🥇', tooltip: 'Mini-championnat du futur champion (places 1 à 4)' },
      { rank: 2, icon: '🥇', tooltip: 'Mini-championnat du futur champion (places 1 à 4)' },
      { rank: 3, icon: '🥇', tooltip: 'Mini-championnat du futur champion (places 1 à 4)' },
      { rank: 4, icon: '🥇', tooltip: 'Mini-championnat du futur champion (places 1 à 4)' },
      { rank: 5, icon: '🥈', tooltip: 'Mini-championnat Conference League (places 5 à 8)' },
      { rank: 6, icon: '🥈', tooltip: 'Mini-championnat Conference League (places 5 à 8)' },
      { rank: 7, icon: '🥈', tooltip: 'Mini-championnat Conference League (places 5 à 8)' },
      { rank: 8, icon: '🥈', tooltip: 'Mini-championnat Conference League (places 5 à 8)' },
      { rank: 9, icon: '🔻', tooltip: 'Championnat de relégation (places 9 à 14)' },
      { rank: 10, icon: '🔻', tooltip: 'Championnat de relégation (places 9 à 14)' },
      { rank: 11, icon: '🔻', tooltip: 'Championnat de relégation (places 9 à 14)' },
      { rank: 12, icon: '🔻', tooltip: 'Championnat de relégation (places 9 à 14)' },
      { rank: 13, icon: '🔻', tooltip: 'Championnat de relégation (places 9 à 14)' },
      { rank: 14, icon: '🔻', tooltip: 'Championnat de relégation (places 9 à 14)' }
    ],
    legend: [
      '🥇 Mini-championnat du futur champion',
      '🥈 Mini-championnat Conference League',
      '🔻 Championnat de relégation'
    ],
    resultsGroupSplit: {
      sizes: [4, 4, 6],
      labels: [
        'Mini-championnat du futur champion',
        'Mini-championnat Conference League',
        'Championnat de relégation'
      ],
      groupSlots: [
        // Top 4 : 1er champion (LDC), 2e-3e Conference.
        { ldcSlots: 1, elSlots: 0, eclSlots: 2, barrageSlots: 0, relegationSlots: 0, rankMarkers: null },
        // 5e-8e : le 1er de ce mini-championnat (=5e general) prend la place Conference.
        {
          ldcSlots: 0, elSlots: 0, eclSlots: 0, barrageSlots: 0, relegationSlots: 0,
          rankMarkers: [{ rank: 1, icon: '🌍', tooltip: 'Qualifié Conference League' }]
        },
        // 9e-14e : avant-dernier en barrage, dernier relegue.
        { ldcSlots: 0, elSlots: 0, eclSlots: 0, barrageSlots: 1, relegationSlots: 1, rankMarkers: null }
      ]
    }
  },
  CHYPRE: {
    // Meme principe qu'Autriche/Bulgarie, mais groupes asymetriques : Championship group
    // (6 premiers) pour le titre et l'Europe, play-out group (8 derniers) pour le maintien.
    rankMarkers: [
      { rank: 1, icon: '🅰️', tooltip: 'Championship group (6 premiers)' },
      { rank: 2, icon: '🅰️', tooltip: 'Championship group (6 premiers)' },
      { rank: 3, icon: '🅰️', tooltip: 'Championship group (6 premiers)' },
      { rank: 4, icon: '🅰️', tooltip: 'Championship group (6 premiers)' },
      { rank: 5, icon: '🅰️', tooltip: 'Championship group (6 premiers)' },
      { rank: 6, icon: '🅰️', tooltip: 'Championship group (6 premiers)' },
      { rank: 7, icon: '🅱️', tooltip: 'Play-out group (8 derniers)' },
      { rank: 8, icon: '🅱️', tooltip: 'Play-out group (8 derniers)' },
      { rank: 9, icon: '🅱️', tooltip: 'Play-out group (8 derniers)' },
      { rank: 10, icon: '🅱️', tooltip: 'Play-out group (8 derniers)' },
      { rank: 11, icon: '🅱️', tooltip: 'Play-out group (8 derniers)' },
      { rank: 12, icon: '🅱️', tooltip: 'Play-out group (8 derniers)' },
      { rank: 13, icon: '🅱️', tooltip: 'Play-out group (8 derniers)' },
      { rank: 14, icon: '🅱️', tooltip: 'Play-out group (8 derniers)' }
    ],
    legend: ['🅰️ Championship group (haut de tableau)', '🅱️ Play-out group (bas de tableau)'],
    resultsGroupSplit: {
      sizes: [6, 8],
      labels: ['Championship group', 'Play-out group'],
      groupSlots: [
        // Championship group : 1er champion (LDC), 2e-3e Conference.
        { ldcSlots: 1, elSlots: 0, eclSlots: 2, barrageSlots: 0, relegationSlots: 0, rankMarkers: null },
        // Play-out group (8 equipes) : 3 derniers relegues.
        { ldcSlots: 0, elSlots: 0, eclSlots: 0, barrageSlots: 0, relegationSlots: 3, rankMarkers: null }
      ]
    }
  },
  DANEMARK: {
    // Format a 2 groupes de 6 (comme l'Autriche). Particularite : pas de barrage generique
    // (bas de tableau) - le seul barrage europeen oppose le 3e du groupe du haut au 7e
    // general (= 1er du groupe du bas), d'ou un rankMarker dedie de chaque cote plutot que
    // barrageSlots. Le 3e du haut n'est PAS qualifie automatiquement : il doit gagner ce
    // barrage, d'ou eclSlots=0 sur ce groupe.
    rankMarkers: [
      { rank: 1, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 2, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 3, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 4, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 5, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 6, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 7, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 8, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 9, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 10, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 11, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 12, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' }
    ],
    legend: ['🅰️ Groupe du haut', '🅱️ Groupe du bas'],
    resultsGroupSplit: {
      sizes: [6, 6],
      labels: ['Groupe du haut', 'Groupe du bas'],
      groupSlots: [
        // Groupe du haut : 1er champion (LDC), 2e Europa League, 3e en barrage europeen
        // (pas de qualification directe).
        {
          ldcSlots: 1, elSlots: 1, eclSlots: 0, barrageSlots: 0, relegationSlots: 0,
          rankMarkers: [{ rank: 3, icon: '🎟️', tooltip: 'Barrage Conference League face au 7e (1er du groupe du bas)' }]
        },
        // Groupe du bas : 1er (=7e general) en barrage europeen face au 3e du haut,
        // 5e et 6e (=11e/12e general) relegues.
        {
          ldcSlots: 0, elSlots: 0, eclSlots: 0, barrageSlots: 0, relegationSlots: 2,
          rankMarkers: [{ rank: 1, icon: '🎟️', tooltip: 'Barrage Conference League face au 3e du groupe du haut' }]
        }
      ]
    }
  },
  ECOSSE: {
    // Les 12 equipes se rencontrent 3 fois avant la scission (regularSeasonCycles: 2, car
    // le decompte par paire ordonnee ne produit que 2 "cycles" reels malgre les 3 vraies
    // confrontations - la 3e rencontre alterne le sens aller/retour). Puis top 6 / bottom 6
    // (comme l'Autriche), reprenant les places qualificatives/barrage/relegation deja en
    // place, simplement reparties par groupe au lieu du grand classement.
    rankMarkers: [
      { rank: 1, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 2, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 3, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 4, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 5, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 6, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 7, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 8, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 9, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 10, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 11, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 12, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' }
    ],
    legend: ['🅰️ Groupe du haut', '🅱️ Groupe du bas'],
    resultsGroupSplit: {
      sizes: [6, 6],
      regularSeasonCycles: 2,
      labels: ['Groupe du haut', 'Groupe du bas'],
      groupSlots: [
        // Groupe du haut : 1er et 2e LDC, 3e EL, 4e ECL.
        { ldcSlots: 2, elSlots: 1, eclSlots: 1, barrageSlots: 0, relegationSlots: 0, rankMarkers: null },
        // Groupe du bas : avant-dernier (5e) en barrage, dernier (6e) relegue.
        { ldcSlots: 0, elSlots: 0, eclSlots: 0, barrageSlots: 1, relegationSlots: 1, rankMarkers: null }
      ]
    }
  },
  FINLANDE: {
    // Meme format que l'Autriche : top 6 / bottom 6 a l'issue de la saison reguliere (un
    // seul tour aller-retour, regularSeasonCycles par defaut = 1), classement fige a ce
    // moment-la (rankMarkers ci-dessous, calcules sur frozenStandings). Chaque mini-
    // championnat repart ensuite des points deja acquis en phase 1 et y ajoute les matchs
    // de phase 2 entre membres du meme groupe (realGroupStandings, cumulatif comme le
    // classement general standard) : champion/conference pour le groupe du haut, barrage/
    // relegation pour le groupe du bas.
    rankMarkers: [
      { rank: 1, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 2, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 3, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 4, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 5, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 6, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 7, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 8, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 9, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 10, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 11, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 12, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' }
    ],
    legend: ['🅰️ Groupe du haut', '🅱️ Groupe du bas'],
    resultsGroupSplit: {
      sizes: [6, 6],
      labels: ['Groupe du haut', 'Groupe du bas'],
      groupSlots: [
        { ldcSlots: 1, elSlots: 0, eclSlots: 2, barrageSlots: 0, relegationSlots: 0, rankMarkers: null },
        { ldcSlots: 0, elSlots: 0, eclSlots: 0, barrageSlots: 1, relegationSlots: 1, rankMarkers: null }
      ]
    }
  },
  GIBRALTAR: {
    // Particularite : pas de relegation ni de barrage. A l'issue de la saison reguliere
    // (double aller-retour entre les 11 equipes), seuls les 6 premiers continuent dans un
    // unique mini-championnat (pas de groupe du bas, contrairement a l'Autriche/la
    // Finlande) : LDC/EL/ECL s'y jouent, les 5 derniers du classement fige n'ont plus de
    // matchs a jouer.
    rankMarkers: [
      { rank: 1, icon: '🅰️', tooltip: 'Qualifié pour le mini-championnat (top 6)' },
      { rank: 2, icon: '🅰️', tooltip: 'Qualifié pour le mini-championnat (top 6)' },
      { rank: 3, icon: '🅰️', tooltip: 'Qualifié pour le mini-championnat (top 6)' },
      { rank: 4, icon: '🅰️', tooltip: 'Qualifié pour le mini-championnat (top 6)' },
      { rank: 5, icon: '🅰️', tooltip: 'Qualifié pour le mini-championnat (top 6)' },
      { rank: 6, icon: '🅰️', tooltip: 'Qualifié pour le mini-championnat (top 6)' }
    ],
    legend: ['🅰️ Qualifié pour le mini-championnat (top 6)'],
    resultsGroupSplit: {
      sizes: [6],
      labels: ['Mini-championnat (top 6)'],
      groupSlots: [
        { ldcSlots: 1, elSlots: 0, eclSlots: 2, barrageSlots: 0, relegationSlots: 0, rankMarkers: null }
      ]
    }
  },
  GRECE: {
    // Apres la saison reguliere (14 equipes), 3 mini-championnats comme en Bulgarie : top 4
    // pour le titre/l'Europe, 5e-8e pour une place Conference, 9e-14e pour le maintien (pas
    // de barrage).
    rankMarkers: [
      { rank: 1, icon: '🥇', tooltip: 'Mini-championnat du futur champion (places 1 à 4)' },
      { rank: 2, icon: '🥇', tooltip: 'Mini-championnat du futur champion (places 1 à 4)' },
      { rank: 3, icon: '🥇', tooltip: 'Mini-championnat du futur champion (places 1 à 4)' },
      { rank: 4, icon: '🥇', tooltip: 'Mini-championnat du futur champion (places 1 à 4)' },
      { rank: 5, icon: '🥈', tooltip: 'Mini-championnat Conference League (places 5 à 8)' },
      { rank: 6, icon: '🥈', tooltip: 'Mini-championnat Conference League (places 5 à 8)' },
      { rank: 7, icon: '🥈', tooltip: 'Mini-championnat Conference League (places 5 à 8)' },
      { rank: 8, icon: '🥈', tooltip: 'Mini-championnat Conference League (places 5 à 8)' },
      { rank: 9, icon: '🔻', tooltip: 'Championnat de relégation (places 9 à 14)' },
      { rank: 10, icon: '🔻', tooltip: 'Championnat de relégation (places 9 à 14)' },
      { rank: 11, icon: '🔻', tooltip: 'Championnat de relégation (places 9 à 14)' },
      { rank: 12, icon: '🔻', tooltip: 'Championnat de relégation (places 9 à 14)' },
      { rank: 13, icon: '🔻', tooltip: 'Championnat de relégation (places 9 à 14)' },
      { rank: 14, icon: '🔻', tooltip: 'Championnat de relégation (places 9 à 14)' }
    ],
    legend: [
      '🥇 Mini-championnat du futur champion',
      '🥈 Mini-championnat Conference League',
      '🔻 Championnat de relégation'
    ],
    resultsGroupSplit: {
      sizes: [4, 4, 6],
      labels: [
        'Mini-championnat du futur champion',
        'Mini-championnat Conference League',
        'Championnat de relégation'
      ],
      groupSlots: [
        // Top 4 : 1er champion (LDC), 2e LDC, 3e Europa, 4e Conference.
        { ldcSlots: 2, elSlots: 1, eclSlots: 1, barrageSlots: 0, relegationSlots: 0, rankMarkers: null },
        // 5e-8e : le 1er de ce mini-championnat (=5e general) prend aussi la place Conference.
        {
          ldcSlots: 0, elSlots: 0, eclSlots: 0, barrageSlots: 0, relegationSlots: 0,
          rankMarkers: [{ rank: 1, icon: '🌍', tooltip: 'Qualifié Conference League' }]
        },
        // 9e-14e : les 2 derniers (13e et 14e) relegues, pas de barrage.
        { ldcSlots: 0, elSlots: 0, eclSlots: 0, barrageSlots: 0, relegationSlots: 2, rankMarkers: null }
      ]
    }
  },
  'IRLANDE NORD': {
    // Apres un triple aller-retour entre les 12 equipes (regularSeasonCycles: 2, car la
    // repartition domicile/exterieur asymetrique du triple tour fait apparaitre 2 cycles
    // d'occurrence de paire ordonnee, pas 3), le championnat se scinde en top 6 / bottom 6.
    // Le "Barrage Europe" (4e-5e-6e-7e, deja present en base sous forme de playoff en
    // cascade 6e-7e puis 5e puis 4e) est une place europeenne supplementaire, distincte du
    // barrage de maintien (11e) - icone dediee pour ne pas les confondre.
    rankMarkers: [
      { rank: 1, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 2, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 3, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 4, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 5, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 6, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 7, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 8, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 9, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 10, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 11, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 12, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' }
    ],
    legend: ['🅰️ Groupe du haut', '🅱️ Groupe du bas'],
    resultsGroupSplit: {
      sizes: [6, 6],
      labels: ['Groupe du haut', 'Groupe du bas'],
      regularSeasonCycles: 2,
      groupSlots: [
        // Groupe du haut : 1er champion (LDC), 2e-3e Conference, 4e-5e-6e barrage europeen.
        {
          ldcSlots: 1, elSlots: 0, eclSlots: 2, barrageSlots: 0, relegationSlots: 0,
          rankMarkers: [
            { rank: 4, icon: '🌐', tooltip: 'Barrage pour une place européenne (Conference League)' },
            { rank: 5, icon: '🌐', tooltip: 'Barrage pour une place européenne (Conference League)' },
            { rank: 6, icon: '🌐', tooltip: 'Barrage pour une place européenne (Conference League)' }
          ]
        },
        // Groupe du bas : le 1er (=7e general) complete le barrage europeen, avant-dernier
        // (11e) en barrage de maintien, dernier (12e) relegue.
        {
          ldcSlots: 0, elSlots: 0, eclSlots: 0, barrageSlots: 1, relegationSlots: 1,
          rankMarkers: [{ rank: 1, icon: '🌐', tooltip: 'Barrage pour une place européenne (Conference League)' }]
        }
      ]
    }
  },
  ISLANDE: {
    // Apres la saison reguliere (12 equipes, un aller-retour), split top 6 / bottom 6 comme
    // en Autriche/Finlande.
    rankMarkers: [
      { rank: 1, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 2, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 3, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 4, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 5, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 6, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 7, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 8, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 9, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 10, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 11, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' },
      { rank: 12, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 12)' }
    ],
    legend: ['🅰️ Groupe du haut', '🅱️ Groupe du bas'],
    resultsGroupSplit: {
      sizes: [6, 6],
      labels: ['Groupe du haut', 'Groupe du bas'],
      groupSlots: [
        { ldcSlots: 1, elSlots: 0, eclSlots: 2, barrageSlots: 0, relegationSlots: 0, rankMarkers: null },
        { ldcSlots: 0, elSlots: 0, eclSlots: 0, barrageSlots: 0, relegationSlots: 2, rankMarkers: null }
      ]
    }
  },
  ISRAEL: {
    // Apres la saison reguliere (14 equipes, un aller-retour), split top 6 / bottom 8.
    rankMarkers: [
      { rank: 1, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 2, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 3, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 4, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 5, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 6, icon: '🅰️', tooltip: 'Groupe du haut (places 1 à 6)' },
      { rank: 7, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 14)' },
      { rank: 8, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 14)' },
      { rank: 9, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 14)' },
      { rank: 10, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 14)' },
      { rank: 11, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 14)' },
      { rank: 12, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 14)' },
      { rank: 13, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 14)' },
      { rank: 14, icon: '🅱️', tooltip: 'Groupe du bas (places 7 à 14)' }
    ],
    legend: ['🅰️ Groupe du haut', '🅱️ Groupe du bas'],
    resultsGroupSplit: {
      sizes: [6, 8],
      labels: ['Groupe du haut', 'Groupe du bas'],
      groupSlots: [
        { ldcSlots: 1, elSlots: 0, eclSlots: 2, barrageSlots: 0, relegationSlots: 0, rankMarkers: null },
        { ldcSlots: 0, elSlots: 0, eclSlots: 0, barrageSlots: 0, relegationSlots: 2, rankMarkers: null }
      ]
    }
  },
  ROUMANIE: {
    // Barrage europeen (7e-8e puis le vainqueur contre le 4e, comme en Irlande du Nord) :
    // place europeenne supplementaire, distincte du barrage de maintien (13e/14e, deja
    // couvert par barrageSlots). Classement plat sinon, pas de scission en groupes.
    rankMarkers: [
      { rank: 7, icon: '🌐', tooltip: 'Barrage pour une place européenne (Conference League)' },
      { rank: 8, icon: '🌐', tooltip: 'Barrage pour une place européenne (Conference League)' }
    ],
    legend: ['🌐 Barrage pour une place européenne (Conference League)']
  },
  TCHEQUIE: {
    // 2 barrages non contigus avec le bas du classement (le systeme d'icones automatique ne
    // gere qu'un bloc barrage/relegation colle au dernier rang) : barrage europeen (7e-10e,
    // cascade façon Irlande du Nord, pour une place de Conference League) et barrage de
    // maintien (14e/15e, aller-retour contre le 3e/2e de Division 2) - tout en rankMarkers
    // dedies. La relegation directe (dernier rang) reste geree par relegationSlots.
    rankMarkers: [
      { rank: 7, icon: '🌐', tooltip: 'Barrage pour une place européenne (Conference League)' },
      { rank: 8, icon: '🌐', tooltip: 'Barrage pour une place européenne (Conference League)' },
      { rank: 9, icon: '🌐', tooltip: 'Barrage pour une place européenne (Conference League)' },
      { rank: 10, icon: '🌐', tooltip: 'Barrage pour une place européenne (Conference League)' },
      { rank: 14, icon: '⚔️', tooltip: 'Barrage de maintien' },
      { rank: 15, icon: '⚔️', tooltip: 'Barrage de maintien' }
    ],
    legend: [
      '🌐 Barrage pour une place européenne (Conference League)',
      '⚔️ Barrage de maintien'
    ]
  }
}

const rankConfig = computed(() => league.value ? LEAGUE_RANK_CONFIG[league.value.code] ?? null : null)

const rawMatches = ref([])

function roundNumber(m) {
  const found = (m.roundLabel ?? '').match(/(\d+)/)
  return found ? parseInt(found[1], 10) : null
}

// Detecte les "cycles" de confrontations (1ere fois qu'une paire ordonnee d'equipes se
// rencontre = cycle 1, 2e fois = cycle 2...), exactement comme ResultsGrid.vue - sert ici a
// isoler les matchs de la saison reguliere (avant scission en mini-championnats) de ceux de
// la 2e phase (entre membres du meme groupe final).
const allCycles = computed(() => {
  const sorted = rawMatches.value.slice().sort((a, b) => {
    const ra = roundNumber(a), rb = roundNumber(b)
    if (ra != null && rb != null && ra !== rb) return ra - rb
    const ad = a.date ?? '', bd = b.date ?? ''
    if (ad !== bd) return ad.localeCompare(bd)
    return a.id - b.id
  })
  const seenCount = new Map()
  const byCycle = new Map()
  for (const m of sorted) {
    const key = `${m.team1Id}-${m.team2Id}`
    const n = (seenCount.get(key) ?? 0) + 1
    seenCount.set(key, n)
    if (!byCycle.has(n)) byCycle.set(n, [])
    byCycle.get(n).push(m)
  }
  return [...byCycle.entries()].sort((a, b) => a[0] - b[0]).map(([cycle, list]) => ({ cycle, list }))
})

const regularSeasonCycles = computed(() => rankConfig.value?.resultsGroupSplit?.regularSeasonCycles ?? 1)

function standingsFromMatches(matchList) {
  const byTeam = new Map()
  for (const m of matchList) {
    if (m.status !== 'COMPLETED' || m.score1 == null || m.score2 == null) continue
    if (!byTeam.has(m.team1Id)) byTeam.set(m.team1Id, { teamId: m.team1Id, teamName: m.team1Name, teamLogoPath: m.team1LogoPath, teamCountry: m.team1Country, played: 0, won: 0, drawn: 0, lost: 0, goalsFor: 0, goalsAgainst: 0 })
    if (!byTeam.has(m.team2Id)) byTeam.set(m.team2Id, { teamId: m.team2Id, teamName: m.team2Name, teamLogoPath: m.team2LogoPath, teamCountry: m.team2Country, played: 0, won: 0, drawn: 0, lost: 0, goalsFor: 0, goalsAgainst: 0 })
    const t1 = byTeam.get(m.team1Id), t2 = byTeam.get(m.team2Id)
    t1.played++; t2.played++
    t1.goalsFor += m.score1; t1.goalsAgainst += m.score2
    t2.goalsFor += m.score2; t2.goalsAgainst += m.score1
    if (m.score1 > m.score2) { t1.won++; t2.lost++ }
    else if (m.score1 === m.score2) { t1.drawn++; t2.drawn++ }
    else { t2.won++; t1.lost++ }
  }
  const rows = [...byTeam.values()].map(t => ({ ...t, goalDifference: t.goalsFor - t.goalsAgainst, points: t.won * 3 + t.drawn }))
  rows.sort((a, b) => b.points - a.points || b.goalDifference - a.goalDifference || b.goalsFor - a.goalsFor)
  return rows
}

// Classement fige a l'issue de la saison reguliere, avant la scission en mini-
// championnats : ne bouge plus une fois les mini-championnats lances (sert uniquement a
// repartir les groupes, cf. rankMarkers du pays). Contrairement au classement general
// standard (cumulatif sur toute la saison), il ne compte que les matchs de la/des cycle(s)
// reguliers.
const frozenStandings = computed(() => {
  if (!rankConfig.value?.resultsGroupSplit) return standings.value
  const regularMatches = allCycles.value.filter(c => c.cycle <= regularSeasonCycles.value).flatMap(c => c.list)
  return standingsFromMatches(regularMatches)
})

// Composition reelle des groupes, deduite des matchs de 2e phase deja joues (2 equipes qui
// se rencontrent en 2e phase appartiennent forcement au meme groupe) : le classement de
// phase 1 sert seulement de repli quand un groupe n'a encore aucun match saisi (composants
// connexes incomplets), pas de reference absolue - confirme sur la Finlande ou le decoupage
// reel (LAHTI en bas, VPS en haut) contredisait l'ordre du classement de phase 1.
function groupAssignmentFromMatches(sizes) {
  const phase2Matches = allCycles.value.filter(c => c.cycle > regularSeasonCycles.value).flatMap(c => c.list)
  const adjacency = new Map()
  for (const m of phase2Matches) {
    if (!adjacency.has(m.team1Id)) adjacency.set(m.team1Id, new Set())
    if (!adjacency.has(m.team2Id)) adjacency.set(m.team2Id, new Set())
    adjacency.get(m.team1Id).add(m.team2Id)
    adjacency.get(m.team2Id).add(m.team1Id)
  }
  if (adjacency.size !== frozenStandings.value.length) return null

  const visited = new Set()
  const components = []
  for (const teamId of adjacency.keys()) {
    if (visited.has(teamId)) continue
    const stack = [teamId]
    const comp = []
    visited.add(teamId)
    while (stack.length) {
      const cur = stack.pop()
      comp.push(cur)
      for (const next of adjacency.get(cur) ?? []) {
        if (!visited.has(next)) { visited.add(next); stack.push(next) }
      }
    }
    components.push(comp)
  }
  if (components.length !== sizes.length || !components.every((c, i) => c.length === sizes[i])) {
    // Composants de taille inattendue (ex: donnees incompletes) : impossible de mapper
    // fiablement aux groupes configures, on se rabat sur le classement de phase 1.
    const bySize = [...components].sort((a, b) => b.length - a.length)
    if (!bySize.every((c, i) => c.length === sizes[i])) return null
  }

  const frozenRank = new Map(frozenStandings.value.map((r, i) => [r.teamId, i]))
  components.sort((a, b) => {
    const avgA = a.reduce((s, id) => s + (frozenRank.get(id) ?? 0), 0) / a.length
    const avgB = b.reduce((s, id) => s + (frozenRank.get(id) ?? 0), 0) / b.length
    return avgA - avgB
  })
  const groupOfTeam = new Map()
  components.forEach((comp, i) => { for (const id of comp) groupOfTeam.set(id, i) })
  return groupOfTeam
}

// Classements des mini-championnats : la composition de chaque groupe est reelle (voir
// groupAssignmentFromMatches), les points/stats affiches sont cumulatifs (saison reguliere +
// matchs de la 2e phase entre membres du groupe), donc identiques au classement general
// standard tant qu'aucun match de 2e phase n'a ete joue, et evoluent ensuite au fil des
// resultats - comme demande pour l'Autriche/la Finlande.
const realGroupStandings = computed(() => {
  const split = rankConfig.value?.resultsGroupSplit
  if (!split) return []
  const { sizes, labels, groupSlots } = split
  const defaultSlots = { ldcSlots: 0, elSlots: 0, eclSlots: 0, barrageSlots: 0, relegationSlots: 0, rankMarkers: null }
  const cumulativeByTeam = new Map(standings.value.map(r => [r.teamId, r]))
  const blocks = sizes.map((size, i) => ({ label: labels?.[i] ?? `Groupe ${i + 1}`, rows: [], slots: groupSlots?.[i] ?? defaultSlots }))

  const groupOfTeam = groupAssignmentFromMatches(sizes)
  if (groupOfTeam) {
    for (const row of frozenStandings.value) {
      const gi = groupOfTeam.get(row.teamId)
      if (gi != null && blocks[gi]) blocks[gi].rows.push(cumulativeByTeam.get(row.teamId) ?? row)
    }
  } else {
    let start = 0
    for (let i = 0; i < sizes.length && start < frozenStandings.value.length; i++) {
      const teamIds = frozenStandings.value.slice(start, start + sizes[i]).map(r => r.teamId)
      blocks[i].rows = teamIds.map(id => cumulativeByTeam.get(id)).filter(Boolean)
      start += sizes[i]
    }
  }

  for (const block of blocks) {
    block.rows.sort((a, b) => b.points - a.points || b.goalDifference - a.goalDifference || b.goalsFor - a.goalsFor)
  }
  return blocks
})

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
    slots.relegationSlots = league.value.relegationSlots
    slots.barrageSlots = league.value.barrageSlots

    const needsRawMatches = !!rankConfig.value?.resultsGroupSplit
    const [standingsList, statuses, matchList] = await Promise.all([
      api.getStandings(league.value.id),
      api.getTeamStatuses(league.value.id),
      needsRawMatches ? api.getMatchesByCompetition(league.value.id) : Promise.resolve([])
    ])
    standings.value = standingsList
    rawMatches.value = matchList

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
        previousEuropeCompetition: existing?.previousEuropeCompetition ?? null,
        groupName: existing?.groupName ?? ''
      }
    }
  } else {
    standings.value = []
    teamStatusMap.value = {}
    rawMatches.value = []
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

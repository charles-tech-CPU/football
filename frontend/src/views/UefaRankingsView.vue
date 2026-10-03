<template>
  <section class="hero">
    <span class="hero-eyebrow">Coefficients</span>
    <h1>Classement UEFA</h1>
    <p class="section-intro">
      Coefficient UEFA des clubs et des pays. Colonnes reprises telles quelles du fichier source,
      sauf "Pts 2027" (saison en cours) : recalculee en direct a partir des matchs de phase de
      ligue LDC/EL/EC deja saisis (victoire = 2 pts, nul = 1 pt, defaite = 0 pt).
    </p>
  </section>

  <div class="tab-bar">
    <button class="tab-btn" :class="{ active: activeTab === 'clubs' }" @click="activeTab = 'clubs'">Clubs</button>
    <button class="tab-btn" :class="{ active: activeTab === 'pays' }" @click="activeTab = 'pays'">Pays</button>
    <button class="tab-btn" :class="{ active: activeTab === 'evolution' }" @click="activeTab = 'evolution'">Évolution</button>
  </div>

  <template v-if="activeTab === 'evolution'">
    <div class="tab-bar sub-tab-bar">
      <button class="tab-btn" :class="{ active: historyKind === 'club' }" @click="setHistoryKind('club')">Club</button>
      <button class="tab-btn" :class="{ active: historyKind === 'pays' }" @click="setHistoryKind('pays')">Pays</button>
    </div>

    <div class="history-search">
      <input
        v-model="historyQuery"
        type="search"
        :placeholder="historyKind === 'club' ? 'Rechercher un club…' : 'Rechercher un pays…'"
        :aria-label="historyKind === 'club' ? 'Rechercher un club' : 'Rechercher un pays'"
      />
      <ul v-if="historySuggestions.length" class="history-suggestions">
        <li v-for="s in historySuggestions" :key="s.id">
          <button type="button" class="history-suggestion" @click="selectHistory(s)">
            <FlagIcon :country="s.country" />
            {{ s.name }}
          </button>
        </li>
      </ul>
    </div>

    <template v-if="historySelection">
      <h2 class="history-title">
        <FlagIcon :country="historySelection.country" />
        {{ historySelection.name }}
        <span v-if="history.length" class="history-current">{{ formatNumber(history.at(-1).coefficient) }}</span>
      </h2>
      <p class="section-intro">
        Coefficient {{ historyKind === 'club' ? 'du club (somme des 5 saisons)' : 'du pays (moyenne sur 5 saisons des points par club engagé)' }},
        recalculé à chaque date de match européen joué cette saison.
      </p>
      <UefaHistoryChart v-if="history.length" :points="history" :title="historySelection.name" />
    </template>
    <p v-else class="empty-state">Choisis un {{ historyKind === 'club' ? 'club' : 'pays' }} pour afficher l'évolution de son coefficient.</p>
  </template>

  <template v-else-if="activeTab === 'clubs'">
    <div v-if="clubs.length" class="table-scroll">
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>Club</th>
            <th>Coupe</th>
            <th>Total</th>
            <th>Pts 2027</th>
            <th>2026</th>
            <th>2025</th>
            <th>2024</th>
            <th>2023</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in clubs" :key="c.id">
            <td>{{ c.rank }}</td>
            <td>
              <span class="team-cell">
                <TeamLogo :name="c.clubName" :logo-path="c.teamLogoPath" :country="c.country" />
                <FlagIcon :country="c.country" />
                {{ c.clubName }}
              </span>
            </td>
            <td><span v-if="c.currentCup" class="comp-badge" :class="cupClass(c.currentCup)">{{ c.currentCup }}</span></td>
            <td>{{ formatNumber(c.total) }}</td>
            <td><strong>{{ formatNumber(c.points2027) }}</strong></td>
            <td>{{ formatNumber(c.points2026) }}</td>
            <td>{{ formatNumber(c.points2025) }}</td>
            <td>{{ formatNumber(c.points2024) }}</td>
            <td>{{ formatNumber(c.points2023) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-else-if="loaded" class="empty-state">Aucun classement disponible.</p>
  </template>

  <template v-else>
    <div v-if="countries.length" class="table-scroll">
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>Pays</th>
            <th>Total</th>
            <th>Pts 2027</th>
            <th>2026</th>
            <th>2025</th>
            <th>2024</th>
            <th>2023</th>
            <th>Clubs encore en Europe (LDC/EL/EC)</th>
            <th>Nb clubs 2027</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in countries" :key="c.id" :class="rowClass(c)">
            <td>{{ c.rank }}</td>
            <td>
              <span class="team-cell">
                <FlagIcon :country="c.country" />
                {{ c.country }}
              </span>
            </td>
            <td>{{ formatNumber(c.total) }}</td>
            <td><strong>{{ formatNumber(c.points2027) }}</strong></td>
            <td>{{ formatNumber(c.points2026) }}</td>
            <td>{{ formatNumber(c.points2025) }}</td>
            <td>{{ formatNumber(c.points2024) }}</td>
            <td>{{ formatNumber(c.points2023) }}</td>
            <td>{{ c.ldcNow ?? 0 }} / {{ c.elNow ?? 0 }} / {{ c.ecNow ?? 0 }}</td>
            <td>{{ c.nb2027 ?? '—' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-else-if="loaded" class="empty-state">Aucun classement disponible.</p>

    <ul v-if="countries.length" class="standings-legend">
      <li><span class="legend-swatch legend-blue"></span>Encore un club en Ligue des Champions</li>
      <li><span class="legend-swatch legend-orange-dark"></span>Encore un club en Europa League</li>
      <li><span class="legend-swatch legend-yellow"></span>Encore un club en Conference League</li>
      <li><span class="legend-swatch legend-red"></span>Plus aucun club en coupe d'Europe</li>
    </ul>
  </template>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import api from '../services/api'
import TeamLogo from '../components/TeamLogo.vue'
import FlagIcon from '../components/FlagIcon.vue'
import UefaHistoryChart from '../components/UefaHistoryChart.vue'

const activeTab = ref('clubs')
const clubs = ref([])
const countries = ref([])
const loaded = ref(false)

const MAX_SUGGESTIONS = 8
const historyKind = ref('club')
const historyQuery = ref('')
const historySelection = ref(null)
const history = ref([])

const historyCandidates = computed(() =>
  historyKind.value === 'club'
    ? clubs.value.map(c => ({ id: c.id, name: c.clubName, country: c.country }))
    : countries.value.map(c => ({ id: c.id, name: c.country, country: c.country })))

const historySuggestions = computed(() => {
  const q = historyQuery.value.trim().toLowerCase()
  if (!q || q === historySelection.value?.name.toLowerCase()) return []
  return historyCandidates.value.filter(c => c.name.toLowerCase().includes(q)).slice(0, MAX_SUGGESTIONS)
})

function setHistoryKind(kind) {
  historyKind.value = kind
  historyQuery.value = ''
  historySelection.value = null
  history.value = []
}

async function selectHistory(candidate) {
  historySelection.value = candidate
  historyQuery.value = candidate.name
  history.value = []
  const points = historyKind.value === 'club'
    ? await api.getClubUefaHistory(candidate.id)
    : await api.getCountryUefaHistory(candidate.id)
  // Ignore une reponse arrivee apres un autre choix.
  if (historySelection.value === candidate) history.value = points
}

function formatNumber(v) {
  if (v == null) return '—'
  return Number.isInteger(v) ? String(v) : String(Number(v.toFixed(3)))
}

function cupClass(cup) {
  if (cup === 'LDC') return 'comp-ldc'
  if (cup === 'EL') return 'comp-el'
  if (cup === 'EC') return 'comp-ecl'
  return ''
}

const COLOR_ROW_CLASS = { BLUE: 'standing-blue', ORANGE: 'standing-orange-dark', YELLOW: 'standing-yellow', RED: 'standing-red' }

function rowClass(c) {
  return COLOR_ROW_CLASS[c.colorCode] ?? ''
}

async function load() {
  loaded.value = false
  const [clubList, countryList] = await Promise.all([
    api.getClubUefaRankings(),
    api.getCountryUefaRankings()
  ])
  clubs.value = clubList
  countries.value = countryList
  loaded.value = true
}

onMounted(load)
</script>

<style scoped>
.history-search {
  position: relative;
  max-width: 360px;
  margin-bottom: 16px;
}

.history-search input {
  width: 100%;
  box-sizing: border-box;
}

.history-suggestions {
  position: absolute;
  z-index: 5;
  left: 0;
  right: 0;
  margin: 4px 0 0;
  padding: 4px;
  list-style: none;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 10px;
  box-shadow: var(--shadow-lg);
}

.history-suggestion {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  text-align: left;
  background: none;
  border: none;
  box-shadow: none;
  color: var(--text);
  padding: 6px 8px;
}

.history-suggestion:hover {
  background: var(--surface-muted);
}

.history-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.history-current {
  margin-left: auto;
  font-size: 0.8em;
  color: var(--primary-dark);
}
</style>

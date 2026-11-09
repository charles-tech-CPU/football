<template>
  <section class="hero">
    <span class="hero-eyebrow">Clubs</span>
    <h1>Statistiques</h1>
    <p v-if="activeTab === 'palmares'" class="section-intro">
      Palmarès de chaque pays (champion, 2e, 3e, vainqueur de la coupe) et des coupes d'Europe,
      depuis la création de chaque compétition.
    </p>
    <p v-else class="section-intro">
      Recalculées à partir des matchs joués de la saison. Les séries sont celles en cours (depuis le
      dernier match joué) ; une séance de tirs au but compte comme un match nul.
    </p>

    <div v-if="activeTab !== 'palmares' && stats" class="hero-stats">
      <div class="hero-stat">
        <span class="hero-stat-value">{{ stats.matchesPlayed }}</span>
        <span class="hero-stat-label">matchs joués</span>
      </div>
      <div class="hero-stat">
        <span class="hero-stat-value">{{ stats.goals }}</span>
        <span class="hero-stat-label">buts · {{ goalsPerMatch(stats) }} par match</span>
      </div>
      <div class="hero-stat">
        <span class="hero-stat-value">{{ percent(stats.homeWins, stats) }}</span>
        <span class="hero-stat-label">victoires à domicile</span>
      </div>
      <div class="hero-stat">
        <span class="hero-stat-value">{{ percent(stats.draws, stats) }}</span>
        <span class="hero-stat-label">matchs nuls</span>
      </div>
      <div class="hero-stat">
        <span class="hero-stat-value">{{ percent(stats.awayWins, stats) }}</span>
        <span class="hero-stat-label">victoires à l'extérieur</span>
      </div>
    </div>
  </section>

  <div class="tab-bar">
    <button class="tab-btn" :class="{ active: activeTab === 'competition' }" @click="activeTab = 'competition'">Par compétition</button>
    <button class="tab-btn" :class="{ active: activeTab === 'championnats' }" @click="activeTab = 'championnats'">Tous les championnats</button>
    <button class="tab-btn" :class="{ active: activeTab === 'palmares' }" @click="activeTab = 'palmares'">Palmarès</button>
  </div>

  <PalmaresPanel v-if="activeTab === 'palmares'" />

  <template v-else>
    <div v-if="activeTab === 'championnats'" class="tab-bar sub-tab-bar" aria-label="Championnats">
      <button
        v-for="g in LEAGUE_GROUPS"
        :key="g.key"
        class="tab-btn"
        :class="{ active: groupKey === g.key }"
        :title="groupTitle(g)"
        @click="groupKey = g.key"
      >{{ g.label }}</button>
    </div>

    <div v-if="activeTab === 'competition'" class="filters">
      <select v-model.number="competitionId" aria-label="Compétition" @change="loadCompetition">
        <optgroup v-for="g in groups" :key="g.label" :label="g.label">
          <option v-for="c in g.competitions" :key="c.id" :value="c.id">{{ c.name }}</option>
        </optgroup>
      </select>
    </div>

    <template v-if="stats && stats.matchesPlayed">
      <div class="stats-grid">
        <StatsTeamList title="Meilleures attaques" unit="buts" :rows="stats.bestAttacks" />
        <StatsTeamList title="Meilleures défenses" unit="encaissés" :rows="stats.bestDefenses" />
        <StatsTeamList title="Invincibles en cours" unit="matchs" :rows="stats.unbeatenRuns" />
        <StatsTeamList title="Séries de victoires" unit="victoires" :rows="stats.winRuns" />
        <StatsTeamList title="Sans victoire" unit="matchs" :rows="stats.winlessRuns" />
      </div>

      <template v-if="activeTab === 'championnats' && leagues.length">
        <h2>Comparatif des championnats</h2>
        <div class="table-scroll">
          <table class="leagues-table">
            <thead>
              <tr>
                <th v-for="col in LEAGUE_COLUMNS" :key="col.key">
                  <button class="sort-btn" :class="{ active: sortKey === col.key }" @click="sortBy(col.key)">
                    {{ col.label }}<span v-if="sortKey === col.key">{{ sortDesc ? ' ▼' : ' ▲' }}</span>
                  </button>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="l in sortedLeagues" :key="l.competitionId">
                <td>
                  <router-link :to="`/pays/${encodeURIComponent(l.country)}`" class="team-cell team-link">
                    <FlagIcon :country="l.country" />{{ l.country }}
                  </router-link>
                </td>
                <td>{{ l.matchesPlayed }}</td>
                <td>{{ l.goals }}</td>
                <td><strong>{{ goalsPerMatch(l) }}</strong></td>
                <td>{{ percent(l.homeWins, l) }}</td>
                <td>{{ percent(l.draws, l) }}</td>
                <td>{{ percent(l.awayWins, l) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>

      <h2>Plus larges victoires</h2>
      <StatsMatchList :matches="stats.biggestWins" />
      <h2>Matchs les plus prolifiques</h2>
      <StatsMatchList :matches="stats.highestScoring" />
    </template>
    <p v-else-if="stats" class="empty-state">Aucun match joué.</p>
  </template>
  <p v-if="error" class="error-text">{{ error }}</p>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import api from '../services/api'
import FlagIcon from '../components/FlagIcon.vue'
import PalmaresPanel from '../components/PalmaresPanel.vue'
import StatsTeamList from '../components/StatsTeamList.vue'
import StatsMatchList from '../components/StatsMatchList.vue'
import { LEAGUE_GROUPS, groupCountries } from '../utils/leagueGroups'

// Coupes nationales volontairement absentes : trop peu de matchs par club pour des stats parlantes.
const GROUPS = [
  { type: 'LEAGUE', label: 'Championnats' },
  { type: 'CONTINENTAL_CUP', label: "Coupes d'Europe" }
]

// Colonnes du comparatif ; valeur de tri (ratio pour les colonnes en pourcentage ou par match).
const LEAGUE_COLUMNS = [
  { key: 'country', label: 'Championnat', value: l => l.country },
  { key: 'matchesPlayed', label: 'Matchs', value: l => l.matchesPlayed },
  { key: 'goals', label: 'Buts', value: l => l.goals },
  { key: 'goalsPerMatch', label: 'Buts / match', value: l => ratio(l.goals, l) },
  { key: 'homeWins', label: 'Dom.', value: l => ratio(l.homeWins, l) },
  { key: 'draws', label: 'Nuls', value: l => ratio(l.draws, l) },
  { key: 'awayWins', label: 'Ext.', value: l => ratio(l.awayWins, l) }
]

const activeTab = ref('competition')
const competitions = ref([])
const competitionId = ref(null)
const competitionStats = ref(null)
const allLeagues = ref(null)
const error = ref('')
const groupKey = ref('tous')
const countryRanking = ref([])
const sortKey = ref('goalsPerMatch')
const sortDesc = ref(true)

const stats = computed(() => (activeTab.value === 'championnats' ? allLeagues.value?.overall : competitionStats.value) ?? null)
const leagues = computed(() => allLeagues.value?.leagues.filter(l => l.matchesPlayed) ?? [])

// Saison la plus recente de chaque competition seulement, triees par nom.
const groups = computed(() => GROUPS.map(g => ({
  label: g.label,
  competitions: latestSeasons(competitions.value.filter(c => c.type === g.type))
})).filter(g => g.competitions.length))

function latestSeasons(list) {
  const latestSeason = Math.max(...list.map(c => c.season ?? 0))
  return list.filter(c => (c.season ?? 0) === latestSeason).toSorted((a, b) => a.name.localeCompare(b.name))
}

function ratio(count, s) {
  return s.matchesPlayed ? count / s.matchesPlayed : 0
}

function goalsPerMatch(s) {
  return s.matchesPlayed ? ratio(s.goals, s).toFixed(2).replace('.', ',') : '—'
}

function percent(count, s) {
  return s.matchesPlayed ? `${Math.round(100 * ratio(count, s))} %` : '—'
}

const sortedLeagues = computed(() => {
  const col = LEAGUE_COLUMNS.find(c => c.key === sortKey.value)
  const sign = sortDesc.value ? -1 : 1
  return leagues.value.toSorted((a, b) => {
    const va = col.value(a)
    const vb = col.value(b)
    return sign * (typeof va === 'string' ? va.localeCompare(vb) : va - vb)
  })
})

function sortBy(key) {
  if (sortKey.value === key) sortDesc.value = !sortDesc.value
  else {
    sortKey.value = key
    sortDesc.value = key !== 'country'
  }
}

async function loadCompetition() {
  error.value = ''
  try {
    competitionStats.value = await api.getCompetitionStats(competitionId.value)
  } catch (e) {
    competitionStats.value = null
    error.value = e.response?.data?.error ?? 'Impossible de charger les statistiques.'
  }
}

// Info-bulle d'un groupe : ses pays (le top UEFA depend du classement charge).
function groupTitle(group) {
  return groupCountries(group, countryRanking.value).join(', ')
}

async function loadAllLeagues() {
  error.value = ''
  try {
    if (!countryRanking.value.length) countryRanking.value = await api.getCountryUefaRankings()
    const group = LEAGUE_GROUPS.find(g => g.key === groupKey.value)
    allLeagues.value = await api.getAllLeaguesStats(groupCountries(group, countryRanking.value))
  } catch (e) {
    error.value = e.response?.data?.error ?? 'Impossible de charger les statistiques.'
  }
}

watch(activeTab, tab => {
  if (tab === 'championnats' && !allLeagues.value) void loadAllLeagues()
})

watch(groupKey, loadAllLeagues)

onMounted(async () => {
  competitions.value = await api.getCompetitions()
  const first = groups.value[0]?.competitions.find(c => c.country === 'France') ?? groups.value[0]?.competitions[0]
  if (first) {
    competitionId.value = first.id
    await loadCompetition()
  }
})
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
  margin: 16px 0 24px;
}

.sort-btn {
  padding: 0;
  border: none;
  background: none;
  box-shadow: none;
  color: inherit;
  font: inherit;
  text-transform: inherit;
  letter-spacing: inherit;
  cursor: pointer;
}

.sort-btn:hover,
.sort-btn.active {
  color: var(--primary-dark);
  background: none;
  box-shadow: none;
}

.leagues-table .team-cell {
  gap: 8px;
}
</style>

<template>
  <section class="hero">
    <span class="hero-eyebrow">Clubs</span>
    <h1>Statistiques</h1>
    <p class="section-intro">
      Recalculées à partir des matchs joués de la compétition choisie. Les séries sont celles en
      cours (depuis le dernier match joué) ; une séance de tirs au but compte comme un match nul.
    </p>

    <div v-if="stats" class="hero-stats">
      <div class="hero-stat">
        <span class="hero-stat-value">{{ stats.matchesPlayed }}</span>
        <span class="hero-stat-label">matchs joués</span>
      </div>
      <div class="hero-stat">
        <span class="hero-stat-value">{{ stats.goals }}</span>
        <span class="hero-stat-label">buts · {{ goalsPerMatch }} par match</span>
      </div>
      <div class="hero-stat">
        <span class="hero-stat-value">{{ percent(stats.homeWins) }}</span>
        <span class="hero-stat-label">victoires à domicile</span>
      </div>
      <div class="hero-stat">
        <span class="hero-stat-value">{{ percent(stats.draws) }}</span>
        <span class="hero-stat-label">matchs nuls</span>
      </div>
      <div class="hero-stat">
        <span class="hero-stat-value">{{ percent(stats.awayWins) }}</span>
        <span class="hero-stat-label">victoires à l'extérieur</span>
      </div>
    </div>
  </section>

  <div class="filters">
    <select v-model.number="competitionId" aria-label="Compétition" @change="load">
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

    <h2>Plus larges victoires</h2>
    <StatsMatchList :matches="stats.biggestWins" />
    <h2>Matchs les plus prolifiques</h2>
    <StatsMatchList :matches="stats.highestScoring" />
  </template>
  <p v-else-if="stats" class="empty-state">Aucun match joué dans cette compétition.</p>
  <p v-if="error" class="error-text">{{ error }}</p>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import api from '../services/api'
import StatsTeamList from '../components/StatsTeamList.vue'
import StatsMatchList from '../components/StatsMatchList.vue'

const GROUPS = [
  { type: 'LEAGUE', label: 'Championnats' },
  { type: 'CONTINENTAL_CUP', label: "Coupes d'Europe" },
  { type: 'DOMESTIC_CUP', label: 'Coupes nationales' }
]

const competitions = ref([])
const competitionId = ref(null)
const stats = ref(null)
const error = ref('')

// Saison la plus recente de chaque competition seulement, triees par nom.
const groups = computed(() => GROUPS.map(g => ({
  label: g.label,
  competitions: latestSeasons(competitions.value.filter(c => c.type === g.type))
})).filter(g => g.competitions.length))

function latestSeasons(list) {
  const latestSeason = Math.max(...list.map(c => c.season ?? 0))
  return list.filter(c => (c.season ?? 0) === latestSeason).toSorted((a, b) => a.name.localeCompare(b.name))
}

const goalsPerMatch = computed(() =>
  stats.value?.matchesPlayed ? (stats.value.goals / stats.value.matchesPlayed).toFixed(2).replace('.', ',') : '—')

function percent(count) {
  if (!stats.value?.matchesPlayed) return '—'
  return `${Math.round((100 * count) / stats.value.matchesPlayed)} %`
}

async function load() {
  error.value = ''
  try {
    stats.value = await api.getCompetitionStats(competitionId.value)
  } catch (e) {
    stats.value = null
    error.value = e.response?.data?.error ?? 'Impossible de charger les statistiques.'
  }
}

onMounted(async () => {
  competitions.value = await api.getCompetitions()
  const first = groups.value[0]?.competitions.find(c => c.country === 'France') ?? groups.value[0]?.competitions[0]
  if (first) {
    competitionId.value = first.id
    await load()
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
</style>

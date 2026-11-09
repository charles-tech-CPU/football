<template>
  <section class="hero">
    <span class="hero-eyebrow">Football européen</span>
    <h1>Compétitions</h1>
    <p class="section-intro">Championnats européens, leurs coupes nationales, et les coupes d'Europe.</p>

    <div v-if="loaded" class="hero-stats">
      <div class="hero-stat">
        <span class="hero-stat-value">{{ countries.length }}</span>
        <span class="hero-stat-label">Pays</span>
      </div>
      <div class="hero-stat">
        <span class="hero-stat-value">{{ leagueCount }}</span>
        <span class="hero-stat-label">Championnats</span>
      </div>
      <div class="hero-stat">
        <span class="hero-stat-value">{{ cupCount }}</span>
        <span class="hero-stat-label">Coupes nationales</span>
      </div>
      <div class="hero-stat">
        <span class="hero-stat-value">{{ continental.length }}</span>
        <span class="hero-stat-label">Coupes d'Europe</span>
      </div>
    </div>

    <router-link v-if="hasArchives" class="hero-chip archives-link" to="/archives">📚 Archives des saisons passées</router-link>

    <div class="hero-search">
      <input v-model="search" aria-label="Rechercher un pays ou une compétition" placeholder="Rechercher un pays ou une compétition..." />
    </div>
  </section>

  <section v-if="seasonEnds.length" class="season-end">
    <h2>🔔 Fins de saison en approche</h2>
    <p class="section-intro">
      Championnats à {{ SEASON_END_ROUNDS }} journées ou moins de la fin (pastille bleue) : ce qu'il reste à vérifier.
    </p>
    <div class="season-end-grid">
      <router-link
        v-for="e in seasonEnds"
        :key="e.country"
        class="season-end-card"
        :to="`/pays/${encodeURIComponent(e.country)}`"
      >
        <span class="season-end-head">
          <FlagIcon :country="e.country" />
          <span class="season-end-name">{{ e.country }}</span>
          <span class="season-end-count" :title="e.left === 1 ? 'Dernière journée' : `${e.left} journées restantes`">{{ e.left }}</span>
        </span>
        <ul class="season-end-todo">
          <li v-for="(item, i) in e.todo" :key="i">{{ item }}</li>
        </ul>
      </router-link>
    </div>
  </section>

  <template v-if="filteredContinental.length">
    <h2>Coupes d'Europe</h2>
    <div class="continental-grid">
      <router-link
        v-for="c in filteredContinental"
        :key="c.id"
        class="continental-card"
        :class="`continental-card--${c.code.toLowerCase()}`"
        :to="`/competitions/${c.id}`"
      >
        <span class="continental-mark">
          ★
          <LateBadge :count="stats[c.id]?.late" />
        </span>
        <div class="country-card-info">
          <span class="country-card-name">{{ c.name }}</span>
          <span class="country-card-badges">
            <span class="badge badge-continental">{{ c.code }}</span>
            <span v-if="stats[c.id]?.remaining.finished" class="badge badge-finished">Terminée ✓</span>
          </span>
        </div>
      </router-link>
    </div>
  </template>

  <template v-if="filteredCountries.length">
    <h2 v-if="filteredContinental.length">Championnats</h2>
    <div class="country-grid">
      <router-link
        v-for="c in filteredCountries"
        :key="c.country"
        class="country-card"
        :class="{ 'country-card--pending': c.league && stats[c.league.id]?.drawPending }"
        :to="`/pays/${encodeURIComponent(c.country)}`"
      >
        <span class="card-flag">
          <FlagIcon :country="c.country" />
          <LateBadge :count="c.league && stats[c.league.id]?.late" />
          <span v-if="c.league && stats[c.league.id]?.drawPending" class="draw-pending-badge" title="Calendrier pas encore tiré au sort" aria-label="Calendrier pas encore tiré au sort">🎲</span>
          <RoundsLeftBadge v-else :remaining="c.league && stats[c.league.id]?.remaining" />
        </span>
        <div class="country-card-info">
          <span class="country-card-name">{{ c.country }}</span>
          <span class="country-card-badges">
            <span v-if="c.league" class="badge badge-league">Championnat</span>
            <span v-if="c.league?.closedAt" class="badge badge-finished">Clôturé 🔒</span>
            <span v-else-if="c.league && stats[c.league.id]?.drawPending" class="badge badge-pending">Calendrier à tirer</span>
            <span v-else-if="c.league && stats[c.league.id]?.remaining.finished" class="badge badge-finished">Terminé ✓</span>
            <span v-if="c.cup" class="badge badge-cup">Coupe</span>
          </span>
          <CalendarProgress v-if="c.league && stats[c.league.id]?.calendar.total && !stats[c.league.id].remaining.finished && !stats[c.league.id].drawPending" :progress="stats[c.league.id].calendar" />
        </div>
      </router-link>
    </div>
  </template>
  <p v-else-if="loaded && !filteredContinental.length" class="empty-state">Aucune compétition ne correspond à cette recherche.</p>

  <details class="add-form">
    <summary>Ajouter une compétition</summary>
    <form class="inline" @submit.prevent="submit">
      <input v-model="form.code" aria-label="Code" placeholder="Code (ex: FRANCE, LDC)" required />
      <input v-model="form.name" aria-label="Nom" placeholder="Nom (ex: France - Championnat 2027)" required />
      <select v-model="form.type" aria-label="Type">
        <option value="LEAGUE">Championnat</option>
        <option value="DOMESTIC_CUP">Coupe nationale</option>
        <option value="CONTINENTAL_CUP">Coupe d'Europe</option>
      </select>
      <input v-model="form.country" aria-label="Pays" placeholder="Pays (vide si continentale)" />
      <input v-model.number="form.season" aria-label="Saison" type="number" placeholder="Saison" required />
      <button type="submit">Ajouter</button>
    </form>
    <p v-if="error" class="error-text">{{ error }}</p>
  </details>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import api from '../services/api'
import FlagIcon from '../components/FlagIcon.vue'
import LateBadge from '../components/LateBadge.vue'
import RoundsLeftBadge from '../components/RoundsLeftBadge.vue'
import CalendarProgress from '../components/CalendarProgress.vue'
import {
  SEASON_END_ROUNDS,
  calendarProgress,
  lateCount,
  remainingProgress,
  roundsLeft,
  seasonEndChecklist
} from '../utils/competitionProgress'
import { archivedSeasons, currentSeason, sortContinental } from '../utils/archives'

const competitions = ref([])
const loaded = ref(false)
const error = ref('')
const search = ref('')
const stats = reactive({})

const form = reactive({
  code: '',
  name: '',
  type: 'LEAGUE',
  country: '',
  season: new Date().getFullYear() + 1
})

const countries = computed(() => {
  const byCountry = new Map()
  for (const c of competitions.value) {
    if (!c.country) continue
    if (!byCountry.has(c.country)) byCountry.set(c.country, { country: c.country, league: null, cup: null })
    const entry = byCountry.get(c.country)
    // Saison suivante deja preparee (cf. NextSeasonPanel) : la plus recente est mise en avant.
    if (c.type === 'LEAGUE' && (!entry.league || c.season > entry.league.season)) entry.league = c
    if (c.type === 'DOMESTIC_CUP' && (!entry.cup || c.season > entry.cup.season)) entry.cup = c
  }
  return [...byCountry.values()].sort((a, b) => a.country.localeCompare(b.country))
})

// Coupes d'Europe de la saison en cours (les precedentes sont aux archives), LDC > EL > EC.
const continental = computed(() => {
  const season = currentSeason(competitions.value)
  return sortContinental(competitions.value.filter(c => c.type === 'CONTINENTAL_CUP' && c.season === season))
})
const hasArchives = computed(() => archivedSeasons(competitions.value).length > 0)
// Championnats proches de la fin (pastille bleue <= SEASON_END_ROUNDS, pas encore termines),
// les plus avances d'abord.
const seasonEnds = computed(() =>
  countries.value
    .filter(c => c.league && !c.league.closedAt && stats[c.league.id])
    .map(c => ({ country: c.country, left: roundsLeft(stats[c.league.id].remaining), todo: stats[c.league.id].checklist }))
    .filter(e => e.left > 0 && e.left <= SEASON_END_ROUNDS)
    .sort((a, b) => a.left - b.left || a.country.localeCompare(b.country))
)

const leagueCount = computed(() => countries.value.filter(c => c.league).length)
const cupCount = computed(() => countries.value.filter(c => c.cup).length)

const filteredCountries = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return countries.value
  return countries.value.filter(c => c.country.toLowerCase().includes(q))
})

const filteredContinental = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return continental.value
  return continental.value.filter(c => c.name.toLowerCase().includes(q) || c.code.toLowerCase().includes(q))
})

// Matchs en retard des championnats et coupes d'Europe, journees restantes, avancement du calendrier des championnats
// (charge en tache de fond, la grille s'affiche sans attendre).
async function loadStats() {
  const today = new Date().toISOString().slice(0, 10)
  const tracked = competitions.value.filter(c => c.type !== 'DOMESTIC_CUP')
  await Promise.all(tracked.map(async c => {
    const matches = await api.getMatchesByCompetition(c.id)
    const late = lateCount(matches, today)
    const calendar = calendarProgress(matches, c.totalRounds)
    stats[c.id] = {
      late,
      calendar,
      remaining: remainingProgress(matches, c.totalRounds, today),
      // Saison preparee (cf. NextSeasonPanel) mais calendrier pas encore tire au sort.
      drawPending: c.type === 'LEAGUE' && !c.closedAt && matches.length === 0,
      checklist: c.type === 'LEAGUE' ? seasonEndChecklist(matches, c, { late, calendar }) : null
    }
  }))
}

async function load() {
  competitions.value = await api.getCompetitions()
  loaded.value = true
  void loadStats()
}

async function submit() {
  error.value = ''
  try {
    await api.createCompetition({ ...form, country: form.country || null })
    form.code = ''
    form.name = ''
    form.country = ''
    await load()
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de la création."
  }
}

onMounted(load)
</script>

<style scoped>
.country-card--pending {
  border-style: dashed;
  border-color: #b45309;
  background: repeating-linear-gradient(135deg, var(--surface), var(--surface) 10px, #fffbeb 10px, #fffbeb 20px);
}
.draw-pending-badge {
  position: absolute;
  top: -8px;
  left: -10px;
  width: 22px;
  height: 22px;
  box-sizing: border-box;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: #f59e0b;
  border: 2px solid var(--surface);
  font-size: 11px;
  line-height: 1;
  box-shadow: 0 2px 6px rgba(180, 83, 9, 0.35);
  z-index: 1;
}
.badge-pending {
  background: #fef3c7;
  color: #92400e;
}
.archives-link {
  margin-top: 12px;
  text-decoration: none;
}
.season-end {
  margin-bottom: 24px;
}
.season-end-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(min(100%, 300px), 1fr));
  gap: 12px;
}
.season-end-card {
  display: block;
  padding: 12px 14px;
  border: 1px solid var(--border);
  border-left: 4px solid #1d4ed8;
  border-radius: var(--radius);
  background: var(--surface);
  box-shadow: var(--shadow);
  color: var(--text);
  text-decoration: none;
}
.season-end-card:hover {
  box-shadow: var(--shadow-lg);
}
.season-end-head {
  display: flex;
  align-items: center;
  gap: 8px;
}
.season-end-name {
  flex: 1;
  font-weight: 700;
}
.season-end-count {
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: #1d4ed8;
  color: white;
  font-size: 12px;
  font-weight: 800;
}
.season-end-todo {
  margin: 8px 0 0;
  padding-left: 18px;
  color: var(--text-muted);
  font-size: 14px;
}
</style>

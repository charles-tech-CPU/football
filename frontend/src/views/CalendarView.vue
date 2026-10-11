<template>
  <section class="hero">
    <span class="hero-eyebrow">Clubs</span>
    <h1>Calendrier</h1>
    <p class="section-intro">Matchs pas encore joués (hors reportés/suspendus), du plus proche au plus lointain. Filtrable par type de compétition, 20 par page. 🔥 = affiche à voir de la journée, selon le classement.</p>
  </section>

  <div class="filters">
    <select v-model="typeFilter" aria-label="Filtrer par type de compétition" @change="onFilterChange">
      <option value="league">Championnats</option>
      <option value="cup">Coupes nationales</option>
      <option value="ldc">Ligue des Champions</option>
      <option value="el">Europa League</option>
      <option value="ec">Conference League</option>
      <option value="all">Toutes les compétitions</option>
    </select>
    <span v-if="totalCount" class="page-info">{{ totalCount }} match{{ totalCount > 1 ? 's' : '' }}</span>
    <router-link to="/reportes-suspendus" class="action-btn action-btn--secondary">Reportés / Suspendus</router-link>
  </div>

  <div v-if="matches.length" class="table-scroll">
  <table>
    <thead>
      <tr>
        <th>Date</th>
        <th>Heure</th>
        <th>Compétition</th>
        <th>Journée</th>
        <th>Équipe 1</th>
        <th></th>
        <th></th>
        <th>Équipe 2</th>
        <th>Statut</th>
        <th></th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="m in matches" :key="m.id" :class="rowClass(m)">
        <MatchDateTimeCells v-model:date="edits[m.id].date" v-model:time="edits[m.id].time" flag-missing-time />
        <td>
          <span class="team-cell" :class="competitionBadgeClass(m)">
            <FlagIcon v-if="m.competitionCountry" :country="m.competitionCountry" />
            {{ m.competitionName }}
          </span>
        </td>
        <td>
          {{ m.roundLabel }}
          <span v-if="m.featured" class="featured-match" title="Affiche de la journée : à voir" aria-label="Affiche de la journée">🔥</span>
        </td>
        <td>
          <span class="team-cell">
            <TeamLogo :name="m.team1Name" :country="m.team1Country" :logo-path="m.team1LogoPath" />
            <FlagIcon v-if="isContinental(m)" :country="m.team1Country" />
            <span v-if="m.team1LeagueRank" class="league-rank" :title="leagueRankTitle(m.team1LeagueRank)">{{ formatRank(m.team1LeagueRank) }}</span>
            <select v-model.number="edits[m.id].team1Id" aria-label="Équipe domicile">
              <option v-for="t in teamOptionsFor(m, edits[m.id].team1Id)" :key="t.id" :value="t.id">{{ t.name }}</option>
            </select>
          </span>
        </td>
        <MatchScoreCells v-model:score1="edits[m.id].score1" v-model:score2="edits[m.id].score2" />
        <td>
          <span class="team-cell">
            <TeamLogo :name="m.team2Name" :country="m.team2Country" :logo-path="m.team2LogoPath" />
            <FlagIcon v-if="isContinental(m)" :country="m.team2Country" />
            <span v-if="m.team2LeagueRank" class="league-rank" :title="leagueRankTitle(m.team2LeagueRank)">{{ formatRank(m.team2LeagueRank) }}</span>
            <select v-model.number="edits[m.id].team2Id" aria-label="Équipe extérieur">
              <option v-for="t in teamOptionsFor(m, edits[m.id].team2Id)" :key="t.id" :value="t.id">{{ t.name }}</option>
            </select>
          </span>
        </td>
        <td>
          <MatchStatusSelect v-model="edits[m.id].status" />
        </td>
        <td>
          <button @click="saveMatch(m)">Enregistrer</button>
        </td>
      </tr>
    </tbody>
  </table>
  </div>
  <p v-else-if="loaded" class="empty-state">Aucun match pour ce filtre.</p>

  <div v-if="totalPages > 1" class="pagination">
    <button type="button" :disabled="currentPage === 0" @click="prevPage">← Précédent</button>
    <span>Page {{ currentPage + 1 }} / {{ totalPages }}</span>
    <button type="button" :disabled="currentPage >= totalPages - 1" @click="nextPage">Suivant →</button>
  </div>

  <p v-if="error" class="error-text">{{ error }}</p>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import api from '../services/api'
import FlagIcon from '../components/FlagIcon.vue'
import TeamLogo from '../components/TeamLogo.vue'
import MatchDateTimeCells from '../components/MatchDateTimeCells.vue'
import MatchScoreCells from '../components/MatchScoreCells.vue'
import MatchStatusSelect from '../components/MatchStatusSelect.vue'
import { competitionBadgeClass } from '../utils/competitionBadge'
import { teamOptionsFor as teamOptionsAmong } from '../utils/matchEdit'
import { useMatchEdits } from '../composables/useMatchEdits'

function isContinental(m) {
  return m.competitionCode === 'LDC' || m.competitionCode === 'EL' || m.competitionCode === 'EC'
}

// Rang au championnat national de l'equipe (fourni par l'API, meme pour une coupe).
function formatRank(rank) {
  return rank === 1 ? '1er' : `${rank}e`
}

function leagueRankTitle(rank) {
  return `${formatRank(rank)} au classement du championnat`
}

const PAGE_SIZE = 20

const matches = ref([])
const teams = ref([])
const loaded = ref(false)
// Championnats par defaut (le plus gros volume) : evite de charger d'un coup
// les ~8000 matchs a venir toutes competitions confondues.
const typeFilter = ref('league')
const currentPage = ref(0)
const totalCount = ref(0)

const totalPages = computed(() => Math.max(1, Math.ceil(totalCount.value / PAGE_SIZE)))

const { edits, error, resetEdits, rowClass, saveMatch } = useMatchEdits({ teams, reload: load })

// Clubs du pays de la competition du match (tous pour une coupe d'Europe), cf. utils/matchEdit.
function teamOptionsFor(m, currentId) {
  return teamOptionsAmong(teams.value, m.competitionCountry, currentId)
}

let teamsLoaded = false

async function load() {
  loaded.value = false
  const [page, teamList] = await Promise.all([
    api.getUpcomingMatches({ page: currentPage.value, size: PAGE_SIZE, filter: typeFilter.value }),
    teamsLoaded ? Promise.resolve(teams.value) : api.getTeams()
  ])
  matches.value = page.items
  totalCount.value = page.totalCount
  teams.value = teamList
  teamsLoaded = true

  resetEdits(matches.value)
  loaded.value = true
}

function onFilterChange() {
  currentPage.value = 0
  load()
}

function prevPage() {
  if (currentPage.value === 0) return
  currentPage.value--
  load()
}

function nextPage() {
  if (currentPage.value >= totalPages.value - 1) return
  currentPage.value++
  load()
}

onMounted(load)
</script>

<style scoped>
.featured-match {
  margin-left: 4px;
  cursor: help;
}

.league-rank {
  min-width: 2.4em;
  padding: 1px 6px;
  border-radius: 999px;
  background: var(--primary-soft);
  color: var(--primary-dark);
  font-size: 0.75em;
  font-weight: 700;
  text-align: center;
  white-space: nowrap;
}

.page-info {
  color: var(--text-muted);
  font-size: 0.9em;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin: 16px 0;
}
</style>

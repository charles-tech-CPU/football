<template>
  <section class="hero">
    <span class="hero-eyebrow">Confrontations</span>
    <h1>Tête-à-tête</h1>
    <p class="section-intro">
      Bilan de toutes les confrontations entre deux équipes, toutes compétitions et saisons confondues.
      Une séance de tirs au but compte comme un match nul (vainqueur indiqué à part).
    </p>
  </section>

  <div class="filters h2h-pickers">
    <TeamSelect v-model="teamId" :teams="teams" aria-label="Première équipe" placeholder="Première équipe" />
    <button type="button" class="swap-btn" title="Inverser les deux équipes" aria-label="Inverser les deux équipes" :disabled="!teamId || !opponentId" @click="swap">⇄</button>
    <TeamSelect v-model="opponentId" :teams="teams" aria-label="Deuxième équipe" placeholder="Deuxième équipe" />
  </div>

  <template v-if="h2h">
    <section class="h2h-summary">
      <div class="h2h-side">
        <TeamLogo :name="h2h.team.name" :country="h2h.team.country" :logo-path="h2h.team.logoPath" />
        <router-link :to="`/equipes/${h2h.team.id}`" class="team-link h2h-name">{{ h2h.team.name }}</router-link>
        <span class="h2h-count h2h-count--win">{{ h2h.wins }}</span>
        <span class="h2h-label">victoire{{ h2h.wins > 1 ? 's' : '' }}</span>
      </div>
      <div class="h2h-side h2h-middle">
        <span class="h2h-played">{{ h2h.played }} match{{ h2h.played > 1 ? 's' : '' }}</span>
        <span class="h2h-count">{{ h2h.draws }}</span>
        <span class="h2h-label">nul{{ h2h.draws > 1 ? 's' : '' }}</span>
        <span v-if="h2h.played" class="h2h-goals">Buts : {{ h2h.goalsFor }} – {{ h2h.goalsAgainst }}</span>
        <span v-if="h2h.penaltyWins || h2h.penaltyLosses" class="h2h-goals">
          Tirs au but : {{ h2h.penaltyWins }} – {{ h2h.penaltyLosses }}
        </span>
      </div>
      <div class="h2h-side">
        <TeamLogo :name="h2h.opponent.name" :country="h2h.opponent.country" :logo-path="h2h.opponent.logoPath" />
        <router-link :to="`/equipes/${h2h.opponent.id}`" class="team-link h2h-name">{{ h2h.opponent.name }}</router-link>
        <span class="h2h-count h2h-count--loss">{{ h2h.losses }}</span>
        <span class="h2h-label">victoire{{ h2h.losses > 1 ? 's' : '' }}</span>
      </div>
    </section>

    <div v-if="h2h.played" class="h2h-bar" role="img" :aria-label="`${h2h.wins} victoires, ${h2h.draws} nuls, ${h2h.losses} défaites`">
      <span class="h2h-bar-win" :style="{ flexGrow: h2h.wins }"></span>
      <span class="h2h-bar-draw" :style="{ flexGrow: h2h.draws }"></span>
      <span class="h2h-bar-loss" :style="{ flexGrow: h2h.losses }"></span>
    </div>

    <ul v-if="h2h.biggestWin || h2h.biggestLoss" class="h2h-records">
      <li v-if="h2h.biggestWin">Plus large victoire de <strong>{{ h2h.team.name }}</strong> : {{ describe(h2h.biggestWin) }}</li>
      <li v-if="h2h.biggestLoss">Plus large victoire de <strong>{{ h2h.opponent.name }}</strong> : {{ describe(h2h.biggestLoss) }}</li>
    </ul>

    <template v-if="h2h.byCompetition.length > 1">
      <h2>Par compétition</h2>
      <div class="table-scroll">
        <table>
          <thead>
            <tr>
              <th>Compétition</th>
              <th class="num">Matchs</th>
              <th class="num">{{ h2h.team.name }}</th>
              <th class="num">Nuls</th>
              <th class="num">{{ h2h.opponent.name }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="c in h2h.byCompetition" :key="c.competitionId">
              <td><router-link :to="competitionLink(c)">{{ c.competitionName }}</router-link></td>
              <td class="num">{{ c.played }}</td>
              <td class="num">{{ c.wins }}</td>
              <td class="num">{{ c.draws }}</td>
              <td class="num">{{ c.losses }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <template v-if="h2h.upcomingMatches.length">
      <h2>Prochaines confrontations</h2>
      <TeamMatchList :matches="h2h.upcomingMatches" :team-id="h2h.team.id" />
    </template>

    <h2>Historique</h2>
    <TeamMatchList v-if="h2h.playedMatches.length" :matches="h2h.playedMatches" :team-id="h2h.team.id" />
    <p v-else class="empty-state">Ces deux équipes ne se sont jamais affrontées dans les compétitions saisies.</p>
  </template>
  <p v-else-if="!error && (!teamId || !opponentId)" class="empty-state">Choisis deux équipes pour afficher leur tête-à-tête.</p>
  <p v-if="error" class="error-text">{{ error }}</p>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../services/api'
import TeamSelect from '../components/TeamSelect.vue'
import TeamLogo from '../components/TeamLogo.vue'
import TeamMatchList from '../components/TeamMatchList.vue'
import { formatShortDate } from '../utils/format'
import { competitionLink } from '../utils/competitionBadge'

// Les deux equipes sont dans l'URL (?equipe1=12&equipe2=34) : lien partageable, et la fiche
// club ouvre la page avec le club deja choisi.
const route = useRoute()
const router = useRouter()

const teams = ref([])
const teamId = ref(Number(route.query.equipe1) || null)
const opponentId = ref(Number(route.query.equipe2) || null)
const h2h = ref(null)
const error = ref('')

function swap() {
  const first = teamId.value
  teamId.value = opponentId.value
  opponentId.value = first
}

function describe(m) {
  const pen = m.penaltyScore1 != null ? ` (tab ${m.penaltyScore1}–${m.penaltyScore2})` : ''
  const date = m.date ? `, ${formatShortDate(m.date)}` : ''
  return `${m.team1Name} ${m.score1}–${m.score2} ${m.team2Name}${pen} (${m.competitionName}${date})`
}

async function load() {
  error.value = ''
  h2h.value = null
  router.replace({ query: { equipe1: teamId.value ?? undefined, equipe2: opponentId.value ?? undefined } })
  if (!teamId.value || !opponentId.value) return
  if (teamId.value === opponentId.value) {
    error.value = 'Choisis deux équipes différentes.'
    return
  }
  try {
    h2h.value = await api.getHeadToHeadBetween(teamId.value, opponentId.value)
  } catch (e) {
    error.value = e.response?.data?.error ?? 'Impossible de charger le tête-à-tête.'
  }
}

watch([teamId, opponentId], load)

onMounted(async () => {
  teams.value = await api.getTeams()
  await load()
})
</script>

<style scoped>
.h2h-pickers {
  align-items: center;
}

.swap-btn {
  padding: 6px 12px;
}

.h2h-summary {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 16px;
  align-items: start;
  margin: 24px 0 12px;
}

.h2h-side {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  text-align: center;
}

.h2h-name {
  font-weight: 800;
  font-size: 1.1em;
}

.h2h-count {
  font-size: 2.6em;
  font-weight: 800;
  line-height: 1;
  color: var(--text-muted);
}

.h2h-count--win {
  color: #16a34a;
}

.h2h-count--loss {
  color: #dc2626;
}

.h2h-label,
.h2h-played,
.h2h-goals {
  color: var(--text-muted);
  font-size: 0.9em;
}

.h2h-middle {
  padding-top: 8px;
}

.h2h-bar {
  display: flex;
  height: 10px;
  border-radius: 5px;
  overflow: hidden;
  margin-bottom: 16px;
  background: var(--border);
}

.h2h-bar-win {
  background: #16a34a;
}

.h2h-bar-draw {
  background: #9ca3af;
}

.h2h-bar-loss {
  background: #dc2626;
}

.h2h-records {
  padding-left: 18px;
  color: var(--text-muted);
}

.num {
  text-align: right;
}

@media (max-width: 600px) {
  .h2h-summary {
    gap: 8px;
  }

  .h2h-count {
    font-size: 2em;
  }
}
</style>

<template>
  <section v-if="state" class="closure">
    <template v-if="state.closedAt">
      <div class="closure-banner">
        <span>🔒 Saison clôturée le {{ closedDate }} : classement figé, matchs en lecture seule.</span>
        <button type="button" :disabled="busy" @click="reopen">Rouvrir</button>
      </div>
      <ol v-if="state.finalStandings.length" class="closure-ranking closure-ranking--final">
        <li v-for="(r, i) in state.finalStandings" :key="r.teamId">
          <span class="closure-rank">{{ i + 1 }}</span>
          <TeamLogo :name="r.teamName" :logo-path="r.teamLogoPath" :country="r.teamCountry" />
          <span class="closure-team">{{ r.teamName }}</span>
          <span class="closure-points">{{ r.points }} pts</span>
        </li>
      </ol>
      <NextSeasonPanel
        v-if="isLeague && !nextSeasonExists && state.finalStandings.length"
        :league="competition"
        :final-standings="state.finalStandings"
        :has-cup="hasCup"
        @created="c => emit('next-season', c)"
      />
    </template>

    <details v-else class="add-form">
      <summary>🏁 Clôturer la saison</summary>
      <p class="section-intro">
        La clôture fige le classement final dans l'ordre ci-dessous, ajoute la saison au palmarès
        (ou la met à jour si elle y est déjà) et passe les matchs en lecture seule. Elle peut être
        annulée avec « Rouvrir ».
      </p>
      <p v-if="pending" class="error-text">
        ⚠️ {{ pending }} match{{ pending > 1 ? 's' : '' }} encore à jouer (barrages compris) : ils ne
        pourront plus être saisis une fois la saison clôturée.
      </p>

      <label class="closure-season">Saison du palmarès
        <input v-model.trim="season" aria-label="Saison du palmarès" placeholder="2026 ou 2026-2027" />
      </label>

      <template v-if="isLeague">
        <p class="section-intro">
          Ordre proposé d'après le classement : corrige-le avec les flèches si un départage (tête-à-tête,
          barrage pour le titre...) en décide autrement.
        </p>
        <ol class="closure-ranking">
          <li v-for="(r, i) in ranking" :key="r.teamId">
            <span class="closure-rank">{{ i + 1 }}</span>
            <TeamLogo :name="r.teamName" :logo-path="r.teamLogoPath" :country="r.teamCountry" />
            <span class="closure-team">{{ r.teamName }}</span>
            <span class="closure-points">{{ r.points }} pts</span>
            <button type="button" :disabled="i === 0" :aria-label="`Monter ${r.teamName}`" @click="move(i, -1)">↑</button>
            <button type="button" :disabled="i === ranking.length - 1" :aria-label="`Descendre ${r.teamName}`" @click="move(i, 1)">↓</button>
          </li>
        </ol>
      </template>

      <div v-else class="inline">
        <label>Vainqueur
          <select v-model="winnerId" aria-label="Vainqueur">
            <option :value="null">—</option>
            <option v-for="t in cupTeams" :key="t.id" :value="t.id">{{ t.name }}</option>
          </select>
        </label>
        <label>Finaliste
          <select v-model="runnerUpId" aria-label="Finaliste">
            <option :value="null">—</option>
            <option v-for="t in cupTeams" :key="t.id" :value="t.id">{{ t.name }}</option>
          </select>
        </label>
      </div>

      <button type="button" :disabled="busy || !canClose" @click="close">Clôturer la saison</button>
    </details>
    <p v-if="error" class="error-text">{{ error }}</p>
  </section>
</template>

<script setup>
// Cloture de fin de saison d'un championnat ou d'une coupe nationale (cf. CompetitionClosureService) :
// classement final fige, palmares, matchs en lecture seule.
import { computed, ref, watch } from 'vue'
import api from '../services/api'
import TeamLogo from './TeamLogo.vue'
import NextSeasonPanel from './NextSeasonPanel.vue'
import { cupFinal, moveRow, pendingCount, teamsOf } from '../utils/closure.js'

const props = defineProps({
  competition: { type: Object, required: true },
  // Championnat : classement final propose (cf. utils/closure.proposedRanking).
  proposedRows: { type: Array, default: () => [] },
  // Championnat cloture : sa saison suivante existe deja (sinon, panneau pour la preparer).
  nextSeasonExists: { type: Boolean, default: false },
  hasCup: { type: Boolean, default: false }
})
const emit = defineEmits(['changed', 'next-season'])

const state = ref(null)
const season = ref('')
const ranking = ref([])
const matches = ref([])
const winnerId = ref(null)
const runnerUpId = ref(null)
const busy = ref(false)
const error = ref('')

const isLeague = computed(() => props.competition.type === 'LEAGUE')
const pending = computed(() => pendingCount(matches.value))
const cupTeams = computed(() => teamsOf(matches.value))
const closedDate = computed(() => new Date(state.value.closedAt).toLocaleDateString('fr-FR'))
const canClose = computed(() => season.value && (isLeague.value ? ranking.value.length > 0 : winnerId.value != null))

async function load() {
  error.value = ''
  const [closure, matchList] = await Promise.all([
    api.getClosure(props.competition.id),
    api.getMatchesByCompetition(props.competition.id)
  ])
  state.value = closure
  season.value = closure.suggestedSeason
  matches.value = matchList
  if (!isLeague.value) ({ winnerId: winnerId.value, runnerUpId: runnerUpId.value } = cupFinal(matchList))
}

function move(index, delta) {
  ranking.value = moveRow(ranking.value, index, delta)
}

async function run(action) {
  busy.value = true
  error.value = ''
  try {
    state.value = await action()
    emit('changed')
  } catch (e) {
    error.value = e.response?.data?.error ?? 'Erreur lors de la clôture.'
  } finally {
    busy.value = false
  }
}

function close() {
  const payload = isLeague.value
    ? { season: season.value, ranking: ranking.value.map(r => r.teamId) }
    : { season: season.value, winnerId: winnerId.value, runnerUpId: runnerUpId.value }
  return run(() => api.closeCompetition(props.competition.id, payload))
}

function reopen() {
  if (!window.confirm('Rouvrir la saison ? Le classement figé sera supprimé (le palmarès est gardé).')) return
  return run(() => api.reopenCompetition(props.competition.id))
}

watch(() => props.proposedRows, rows => { ranking.value = rows.slice() }, { immediate: true })
watch(() => props.competition.id, load, { immediate: true })
</script>

<style scoped>
.closure {
  margin: 16px 0;
}
.closure-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 14px;
  border: 1px solid var(--border);
  border-left: 4px solid #15803d;
  border-radius: var(--radius);
  background: var(--surface);
  font-weight: 600;
}
.closure-season {
  display: block;
  margin: 8px 0;
}
.closure-ranking {
  max-width: 520px;
  padding: 0;
  list-style: none;
}
.closure-rank {
  min-width: 24px;
  text-align: right;
  font-weight: 700;
}
.closure-ranking li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 2px 0;
}
.closure-ranking--final {
  columns: 2;
  max-width: none;
}
.closure-team {
  flex: 1;
}
.closure-points {
  color: var(--text-muted);
  font-size: 14px;
}
</style>

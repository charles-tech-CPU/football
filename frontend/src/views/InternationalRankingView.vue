<template>
  <section class="hero">
    <span class="hero-eyebrow">Sélections nationales</span>
    <h1>Classement mondial</h1>
    <p class="section-intro">
      Classement Elo des sélections, calculé sur tous les matchs officiels enregistrés (Coupes du monde, Euros,
      Copa América, CAN, Coupes d'Asie, Gold Cup, Ligues des Nations et leurs qualifications depuis 2014, puis la
      saison en cours). Il se recalcule dès qu'un résultat est saisi.
    </p>
  </section>

  <InternationalNav />

  <div v-if="rows.length" class="filters">
    <select v-model="confederation" aria-label="Filtrer par confédération">
      <option value="">Toutes les confédérations</option>
      <option v-for="c in confederations" :key="c.code" :value="c.code">{{ c.label }}</option>
    </select>
    <input v-model="search" type="search" placeholder="Rechercher une sélection" aria-label="Rechercher une sélection" />
  </div>

  <div v-if="filteredRows.length" class="table-scroll">
    <table class="ranking">
      <thead>
        <tr>
          <th class="num">#</th>
          <th class="num" title="Places gagnées ou perdues sur les 90 derniers jours de compétition">90 j</th>
          <th>Sélection</th>
          <th>Confédération</th>
          <th class="num">Note</th>
          <th class="num" title="Victoires - nuls - défaites (tirs au but = nul)">V-N-D</th>
          <th class="num">Buts</th>
          <th>Forme</th>
          <th>Meilleure victoire</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="r in limitedRows" :key="r.teamId">
          <td class="num rank" :class="{ top: r.rank <= 3 }">{{ r.rank }}</td>
          <td class="num"><RankMove :rank="r.rank" :start-rank="r.rankChange == null ? null : r.rank + r.rankChange" since="90 jours" /></td>
          <td>
            <span class="team-cell">
              <FlagIcon :country="r.country" />
              <router-link :to="`/equipes/${r.teamId}`" class="team-link">{{ r.teamName }}</router-link>
            </span>
          </td>
          <td>
            <span class="comp-badge" :class="confederationOf(r.confederation)?.badgeClass" :title="`Note de départ ${r.startRating}`">
              {{ confederationOf(r.confederation)?.label ?? '—' }}
            </span>
          </td>
          <td class="num">
            <strong>{{ r.rating }}</strong>
            <span v-if="r.ratingChange" class="small" :class="r.ratingChange > 0 ? 'win' : 'loss'">
              {{ r.ratingChange > 0 ? '+' : '' }}{{ r.ratingChange }}
            </span>
          </td>
          <td class="num"><span class="win">{{ r.wins }}</span>-{{ r.draws }}-<span class="loss">{{ r.losses }}</span></td>
          <td class="num muted">{{ r.goalsFor }}-{{ r.goalsAgainst }}</td>
          <td>
            <span class="form" :aria-label="`Forme : ${r.form.join(' ')}`">
              <span v-for="(f, i) in r.form" :key="i" class="form-dot" :class="`form-${f}`">{{ RESULT_LETTERS[f] }}</span>
            </span>
          </td>
          <td>
            <span v-if="r.bestWin" :title="r.bestWin.competitionName">
              {{ r.bestWin.score }} contre
              <router-link :to="`/equipes/${r.bestWin.opponentId}`" class="team-link">{{ r.bestWin.opponentName }}</router-link>
              <span class="muted small"> ({{ r.bestWin.opponentRating }})</span>
            </span>
            <span v-else class="muted">—</span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
  <button v-if="filteredRows.length > COLLAPSED_ROWS" type="button" class="show-all" @click="expanded = !expanded">
    {{ expanded ? 'Réduire' : `Tout afficher (${filteredRows.length})` }}
  </button>
  <p v-if="loaded && !rows.length" class="empty-state">Aucun match international joué.</p>
  <p v-else-if="loaded && !filteredRows.length" class="empty-state">Aucune sélection ne correspond à ces filtres.</p>

  <details class="method">
    <summary>Comment est calculé ce classement ?</summary>
    <ul>
      <li>
        <strong>Elo</strong> : après chaque match, le vainqueur prend des points au perdant, d'autant plus que le
        résultat était <strong>inattendu</strong>. Battre une sélection bien notée rapporte beaucoup, battre une
        petite nation presque rien (qualité des adversaires) ; un nul contre plus fort fait gagner des points.
      </li>
      <li>
        <strong>Note de départ selon la confédération</strong> : les confédérations ne se rencontrent qu'en phase
        finale de Coupe du monde, en barrages intercontinentaux et via les invités (Copa América, Gold Cup). Chaque
        sélection part donc de la note de sa confédération, que ces matchs corrigent ensuite.
        <table class="coefficients">
          <tbody>
            <tr v-for="t in START_TIERS" :key="t.rating">
              <td class="num"><strong>{{ t.rating }}</strong></td>
              <td>{{ t.label }}</td>
            </tr>
          </tbody>
        </table>
      </li>
      <li>
        <strong>Importance du match</strong> : un match de phase finale de Coupe du monde pèse 60, une phase
        finale continentale (Euro, Copa América, CAN, Coupe d'Asie, Gold Cup) 50, les qualifications et les Ligues
        des Nations 40.
      </li>
      <li>
        <strong>Écart de buts</strong> : une victoire de 2 buts compte ×1,5, de 3 buts ×1,75, puis +⅛ par but
        supplémentaire. Une séance de tirs au but compte comme un match nul.
      </li>
      <li>
        <strong>Avantage du terrain</strong> : l'équipe qui reçoit a 100 points d'avance dans le calcul du résultat
        attendu ; une victoire à domicile rapporte donc un peu moins qu'à l'extérieur. Rien sur terrain neutre.
      </li>
      <li>
        <strong>Forme récente</strong> : les matchs sont pris dans l'ordre chronologique, la note reflète donc d'abord
        les derniers résultats. La colonne « 90 j » montre l'évolution depuis le classement d'il y a 90 jours.
      </li>
      <li>
        Principe repris du <em>World Football Elo Ratings</em> (eloratings.net). Limite : les matchs amicaux ne sont
        pas enregistrés dans l'application, ils ne comptent donc pas.
      </li>
    </ul>
  </details>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import api from '../services/api'
import FlagIcon from '../components/FlagIcon.vue'
import InternationalNav from '../components/InternationalNav.vue'
import RankMove from '../components/RankMove.vue'
import { CONFEDERATIONS } from '../utils/confederations'
import { filterTeams } from '../utils/teamSearch'

const COLLAPSED_ROWS = 50
const RESULT_LETTERS = { W: 'V', D: 'N', L: 'D' }

// Notes de depart, a garder alignees sur InternationalRankingService.START_RATINGS
const START_TIERS = [
  { rating: 1700, label: 'Amérique du Sud (CONMEBOL)' },
  { rating: 1650, label: 'Europe (UEFA)' },
  { rating: 1450, label: 'Afrique (CAF), Amérique du Nord (Concacaf)' },
  { rating: 1400, label: 'Asie (AFC)' },
  { rating: 1250, label: 'Océanie (OFC)' }
]

const rows = ref([])
const loaded = ref(false)
const confederation = ref('')
const search = ref('')
const expanded = ref(false)

const confederations = CONFEDERATIONS.filter(c => c.code !== 'FIFA')

function confederationOf(code) {
  return CONFEDERATIONS.find(c => c.code === code) ?? null
}

const filteredRows = computed(() => {
  const byConfederation = confederation.value ? rows.value.filter(r => r.confederation === confederation.value) : rows.value
  if (!search.value.trim()) return byConfederation
  const found = new Set(filterTeams(byConfederation.map(r => ({ id: r.teamId, name: r.teamName })), search.value).map(t => t.id))
  return byConfederation.filter(r => found.has(r.teamId))
})

const limitedRows = computed(() => (expanded.value ? filteredRows.value : filteredRows.value.slice(0, COLLAPSED_ROWS)))

onMounted(async () => {
  rows.value = await api.getInternationalRanking()
  loaded.value = true
})
</script>

<style scoped>
.ranking td {
  white-space: nowrap;
}

.num {
  text-align: right;
}

.rank {
  font-weight: 800;
}

.rank.top {
  color: var(--primary);
}

.win {
  color: #16a34a;
}

.loss {
  color: #dc2626;
}

.small {
  font-size: 0.8em;
  margin-left: 4px;
}

.muted {
  color: var(--text-muted);
}

.form {
  display: inline-flex;
  gap: 3px;
}

.form-dot {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 5px;
  font-size: 0.75em;
  font-weight: 800;
  color: #fff;
}

.form-W {
  background: #16a34a;
}

.form-D {
  background: #9ca3af;
}

.form-L {
  background: #dc2626;
}

.show-all {
  margin: 12px 0;
}

.method {
  margin-top: 24px;
  color: var(--text-muted);
}

.method summary {
  cursor: pointer;
  font-weight: 700;
  color: var(--text);
}

.coefficients {
  margin: 8px 0;
  width: auto;
}
</style>

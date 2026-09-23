<template>
  <div v-if="allowAdd" class="results-grid-header">
    <h2>Grille des résultats</h2>
    <button type="button" class="add-match-btn" @click="showAddForm = !showAddForm">
      {{ showAddForm ? '✕ Fermer' : '+ Ajouter un match' }}
    </button>
  </div>
  <h2 v-else>Grille des résultats</h2>

  <form v-if="allowAdd && showAddForm" class="inline add-match-form" @submit.prevent="submitMatch">
    <select v-model.number="newMatch.team1Id" aria-label="Équipe domicile" required>
      <option disabled value="">Équipe 1</option>
      <option v-for="t in sortedTeams" :key="t.id" :value="t.id">{{ t.name }}</option>
    </select>
    <select v-model.number="newMatch.team2Id" aria-label="Équipe extérieur" required>
      <option disabled value="">Équipe 2</option>
      <option v-for="t in sortedTeams" :key="t.id" :value="t.id">{{ t.name }}</option>
    </select>
    <input v-model="newMatch.date" aria-label="Date" type="date" />
    <input v-model="newMatch.time" aria-label="Heure" type="time" />
    <input v-model.number="newMatch.score1" aria-label="Buts équipe domicile" class="score-input" type="number" min="0" placeholder="B1" />
    <input v-model.number="newMatch.score2" aria-label="Buts équipe extérieur" class="score-input" type="number" min="0" placeholder="B2" />
    <button type="submit">Ajouter</button>
  </form>

  <div v-for="c in cycles" :key="c.cycle" class="results-grid-block">
    <h3 v-if="cycles.length > 1">{{ cycleLabel(c.cycle) }}</h3>
    <div v-if="teamOrder.length" class="grid-scroll">
      <table class="results-grid">
        <thead>
          <tr>
            <th class="corner"></th>
            <th v-for="t in teamOrder" :key="`h-${t.id}`" :title="t.name">{{ shortName(t.name) }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in teamOrder" :key="`r-${row.id}`">
            <th :title="row.name">
              <span class="grid-row-header">
                <TeamLogo :name="row.name" :logo-path="row.logoPath" />
                <FlagIcon v-if="showFlags" :country="row.country" />
                {{ row.name }}
              </span>
            </th>
            <td
              v-for="col in teamOrder"
              :key="`c-${row.id}-${col.id}`"
              :class="cellClass(c.matchByPair, row.id, col.id)"
              tabindex="0"
              @click="startEdit(c.cycle, c.matchByPair, row.id, col.id)"
              @keydown.enter="startEdit(c.cycle, c.matchByPair, row.id, col.id)"
            >
              <template v-if="row.id === col.id">—</template>
              <template v-else-if="editingKey === cellKey(c.cycle, row.id, col.id)">
                <span class="grid-edit">
                  <input v-model.number="editScore1" aria-label="Buts équipe domicile" class="score-input" type="number" min="0" @click.stop />
                  <input v-model.number="editScore2" aria-label="Buts équipe extérieur" class="score-input" type="number" min="0" @click.stop />
                  <button type="button" @click.stop="confirmEdit">✓</button>
                </span>
              </template>
              <template v-else-if="matchAt(c.matchByPair, row.id, col.id)">
                {{ cellLabel(c.matchByPair, row.id, col.id) }}
              </template>
              <template v-else>·</template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <div v-if="expectSecondPhase && cycles.length === 1" class="results-grid-block">
    <h3>2e phase <span class="pending-note">(à venir)</span></h3>
    <div v-if="teamOrder.length" class="grid-scroll">
      <table class="results-grid">
        <thead>
          <tr>
            <th class="corner"></th>
            <th v-for="t in teamOrder" :key="`h2-${t.id}`" :title="t.name">{{ shortName(t.name) }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in teamOrder" :key="`r2-${row.id}`">
            <th :title="row.name">
              <span class="grid-row-header">
                <TeamLogo :name="row.name" :logo-path="row.logoPath" />
                <FlagIcon v-if="showFlags" :country="row.country" />
                {{ row.name }}
              </span>
            </th>
            <td v-for="col in teamOrder" :key="`c2-${row.id}-${col.id}`" :class="row.id === col.id ? 'grid-diagonal' : 'grid-empty'">
              {{ row.id === col.id ? '—' : '·' }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <template v-if="groupSplit || maltePhases">
    <div v-for="(grp, gi) in groupBlocks" :key="`grp-${gi}`" class="results-grid-block">
      <h3>{{ grp.label }}<span v-if="!grp.matchByPair.size" class="pending-note"> (à venir)</span></h3>
      <div v-if="grp.teams.length" class="grid-scroll">
        <table class="results-grid">
          <thead>
            <tr>
              <th class="corner"></th>
              <th v-for="t in grp.teams" :key="`gh-${gi}-${t.id}`" :title="t.name">{{ shortName(t.name) }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in grp.teams" :key="`gr-${gi}-${row.id}`">
              <th :title="row.name">
                <span class="grid-row-header">
                  <TeamLogo :name="row.name" :logo-path="row.logoPath" />
                  <FlagIcon v-if="showFlags" :country="row.country" />
                  {{ row.name }}
                </span>
              </th>
              <td
                v-for="col in grp.teams"
                :key="`gc-${gi}-${row.id}-${col.id}`"
                :class="cellClass(grp.matchByPair, row.id, col.id)"
                tabindex="0"
                @click="startEdit(`group-${gi}`, grp.matchByPair, row.id, col.id)"
                @keydown.enter="startEdit(`group-${gi}`, grp.matchByPair, row.id, col.id)"
              >
                <template v-if="row.id === col.id">—</template>
                <template v-else-if="editingKey === cellKey(`group-${gi}`, row.id, col.id)">
                  <span class="grid-edit">
                    <input v-model.number="editScore1" aria-label="Buts équipe domicile" class="score-input" type="number" min="0" @click.stop />
                    <input v-model.number="editScore2" aria-label="Buts équipe extérieur" class="score-input" type="number" min="0" @click.stop />
                    <button type="button" @click.stop="confirmEdit">✓</button>
                  </span>
                </template>
                <template v-else-if="matchAt(grp.matchByPair, row.id, col.id)">{{ cellLabel(grp.matchByPair, row.id, col.id) }}</template>
                <template v-else>·</template>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </template>

  <div v-if="miniLeaguePositions.length" class="results-grid-block">
    <h3>Mini-championnat (top {{ miniLeaguePositions.length }})</h3>
    <div class="grid-scroll">
      <table class="results-grid">
        <thead>
          <tr>
            <th class="corner"></th>
            <th v-for="pos in miniLeaguePositions" :key="`mh-${pos}`">{{ pos }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="rowPos in miniLeaguePositions" :key="`mr-${rowPos}`">
            <th>{{ rowPos }}</th>
            <td
              v-for="colPos in miniLeaguePositions"
              :key="`mc-${rowPos}-${colPos}`"
              :class="miniCellClass(rowPos, colPos)"
              tabindex="0"
              @click="startMiniEdit(rowPos, colPos)"
              @keydown.enter="startMiniEdit(rowPos, colPos)"
            >
              <template v-if="rowPos === colPos">—</template>
              <template v-else-if="miniEditingKey === `${rowPos}-${colPos}`">
                <span class="grid-edit">
                  <input v-model.number="editScore1" aria-label="Buts équipe domicile" class="score-input" type="number" min="0" @click.stop />
                  <input v-model.number="editScore2" aria-label="Buts équipe extérieur" class="score-input" type="number" min="0" @click.stop />
                  <button type="button" @click.stop="confirmMiniEdit">✓</button>
                </span>
              </template>
              <template v-else-if="miniFixtureBetween(rowPos, colPos)">{{ miniCellLabel(rowPos, colPos) }}</template>
              <template v-else>·</template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <p class="section-intro">Équipes connues seulement une fois la 2e phase terminée (top {{ miniLeaguePositions.length }} du classement).</p>
  </div>

  <p v-if="error" class="error-text">{{ error }}</p>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import api from '../services/api'
import TeamLogo from './TeamLogo.vue'
import FlagIcon from './FlagIcon.vue'
import { compareByRoundThenDate, roundNumber } from '../utils/rounds.js'

const props = defineProps({
  competitionId: { type: [String, Number], required: true },
  // Fragments (insensibles a la casse) : un match n'est garde que si son round_label
  // contient au moins un de ces fragments. Pas de filtre => tous les matchs.
  roundIncludes: { type: Array, default: null },
  // Transmis tel quel a /api/standings pour ne classer/ordonner que les equipes de cette phase.
  round: { type: String, default: null },
  showFlags: { type: Boolean, default: false },
  // Affiche par avance une 2e grille vide (meme equipes) si la saison doit rejouer un 2e
  // aller-retour complet mais qu'aucun match de cette 2e phase n'a encore ete saisi -
  // pour rendre la structure visible avant meme d'avoir des resultats a y mettre.
  expectSecondPhase: { type: Boolean, default: false },
  // Scinde par avance le classement provisoire en N groupes vides (ex: Meistergruppe /
  // Qualifikationsgruppe en Autriche, mini-championnats en Bulgarie) tant qu'aucun match
  // de la phase de groupe n'a ete saisi : { sizes: [4, 4, 6], labels: [...],
  // regularSeasonCycles: 1 } - le 1er groupe prend les sizes[0] premieres equipes du
  // classement actuel, le 2e les sizes[1] suivantes, etc. regularSeasonCycles (defaut 1)
  // = nombre de tours complets joues AVANT la scission en groupes (ex: 2 en Ecosse, qui
  // joue un triple aller-retour avant le split top6/bottom6).
  groupSplit: { type: Object, default: null },
  // Format Malte (cf. LEAGUE_RANK_CONFIG.MALTE / malteBlocks dans CountryView.vue) : 2
  // championnats successifs identifies par PLAGE DE JOURNEES (pas par cycle, incompatible
  // ici car le 2e championnat refait s'affronter toutes les paires depuis le debut) :
  // [{ label, regularRange: [lo, hi], poolRange: [lo, hi], topIds, bottomIds, topLabel,
  // bottomLabel }, ...]. La composition des poules est calculee une seule fois cote
  // CountryView (malteBlocks) et transmise ici pour ne pas la recalculer.
  maltePhases: { type: Array, default: null },
  // Affiche le formulaire "Ajouter un match" (necessaire pour les phases sans
  // autre ecran d'edition, ex: phase de ligue LDC/EL/EC - contrairement aux
  // championnats nationaux, deja editables via CompetitionMatches).
  allowAdd: { type: Boolean, default: false },
  // round_label impose aux matchs crees depuis ce formulaire (ex: "PHASE DE LIGUE").
  defaultRoundLabel: { type: String, default: '' }
})

const matches = ref([])
const rawMatches = ref([])
const standings = ref([])
const teams = ref([])
const error = ref('')
const editingKey = ref(null)
const editScore1 = ref(null)
const editScore2 = ref(null)
const showAddForm = ref(false)

const sortedTeams = computed(() => teams.value.slice().sort((a, b) => a.name.localeCompare(b.name)))

const newMatch = reactive({
  team1Id: '',
  team2Id: '',
  date: '',
  time: '',
  score1: null,
  score2: null
})

async function submitMatch() {
  error.value = ''
  try {
    await api.createMatch({
      competitionId: Number(props.competitionId),
      roundLabel: props.defaultRoundLabel,
      date: newMatch.date || null,
      time: newMatch.time || null,
      team1Id: newMatch.team1Id,
      team2Id: newMatch.team2Id,
      score1: newMatch.score1,
      score2: newMatch.score2
    })
    newMatch.team1Id = ''
    newMatch.team2Id = ''
    newMatch.date = ''
    newMatch.time = ''
    newMatch.score1 = null
    newMatch.score2 = null
    showAddForm.value = false
    await load()
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de la création du match."
  }
}

function isPendingTeam(name) {
  return (name ?? '').toUpperCase().startsWith('A DETERMINER')
}

// Certains championnats (ex: Albanie) rejouent un aller-retour complet une 2e fois
// (championnat/relegation ou simplement un 2e tour) : un couple d'equipes peut donc
// s'affronter plus de 2 fois dans la saison. On detecte ces "cycles" successifs (1ere
// confrontation aller/retour entre 2 equipes = cycle 1, 2e confrontation = cycle 2, ...)
// pour afficher une grille distincte par cycle plutot que d'ecraser silencieusement les
// scores les plus anciens dans une seule grille.
const allCycles = computed(() => {
  const sorted = matches.value.toSorted(compareByRoundThenDate)
  const seenCount = new Map()
  const byCycle = new Map()
  for (const m of sorted) {
    const key = `${m.team1Id}-${m.team2Id}`
    const n = (seenCount.get(key) ?? 0) + 1
    seenCount.set(key, n)
    if (!byCycle.has(n)) byCycle.set(n, new Map())
    byCycle.get(n).set(key, m)
  }
  return [...byCycle.entries()].sort((a, b) => a[0] - b[0]).map(([cycle, matchByPair]) => ({ cycle, matchByPair }))
})

// Quand la competition se scinde en mini-championnats (groupSplit, ex: Autriche/Bulgarie/
// Chypre/Danemark), les cycles au-dela de la "vraie" saison reguliere (regularSeasonCycles,
// 1 par defaut - certains championnats comme l'Ecosse jouent 2 tours complets AVANT la
// scission en groupes) ne doivent pas s'afficher en grille generique indifferenciee
// ("phase N") : ils correspondent a la phase de groupe et atterrissent dans la grille du
// bon mini-championnat (cf. groupBlocks). Sans groupSplit, comportement inchange (Albanie
// etc, vrai 2e tour complet entre toutes les equipes).
const regularSeasonCycles = computed(() => props.groupSplit?.regularSeasonCycles ?? 1)
// maltePhases se base sur des plages de journees (cf. prop), pas sur les cycles : la
// grille generique par cycle n'a pas de sens ici (elle melangerait les 2 championnats), on
// la laisse vide et malteBlocks (groupBlocks) prend le relais entierement.
const cycles = computed(() => {
  if (props.maltePhases) return []
  return props.groupSplit ? allCycles.value.filter(c => c.cycle <= regularSeasonCycles.value) : allCycles.value
})

function cycleLabel(n) {
  return n === 1 ? '1ère phase' : `${n}e phase`
}

// Mini-championnat de fin de saison (V23, ex: Albanie) : matchs "Mini-championnat (a
// determiner : 1ER-2E)" etc, un seul match par paire de POSITIONS finales (pas encore de
// vraies equipes). On extrait les positions directement du round_label pour construire une
// petite grille dediee, distincte du reste (equipes generiques partagees, sinon collision).
const MINI_LEAGUE_RE = /Mini-championnat \(a d[ée]terminer\s*:([^)-]+)-([^)]+)\)/i

const miniLeagueFixtures = computed(() => {
  const list = []
  for (const m of rawMatches.value) {
    const found = (m.roundLabel ?? '').match(MINI_LEAGUE_RE)
    if (found) list.push({ posA: found[1].trim(), posB: found[2].trim(), match: m })
  }
  return list
})

const miniLeaguePositions = computed(() => {
  const set = new Set()
  for (const f of miniLeagueFixtures.value) { set.add(f.posA); set.add(f.posB) }
  return [...set].sort()
})

function miniFixtureBetween(rowPos, colPos) {
  return miniLeagueFixtures.value.find(f =>
    (f.posA === rowPos && f.posB === colPos) || (f.posA === colPos && f.posB === rowPos))
}

function miniCellClass(rowPos, colPos) {
  if (rowPos === colPos) return 'grid-diagonal'
  const f = miniFixtureBetween(rowPos, colPos)
  if (!f) return 'grid-empty'
  const m = f.match
  if (m.score1 == null || m.score2 == null) return 'grid-scheduled'
  const score1 = rowPos === f.posA ? m.score1 : m.score2
  const score2 = rowPos === f.posA ? m.score2 : m.score1
  if (score1 > score2) return 'grid-win'
  if (score1 < score2) return 'grid-loss'
  return 'grid-draw'
}

function miniCellLabel(rowPos, colPos) {
  const f = miniFixtureBetween(rowPos, colPos)
  if (!f || f.match.score1 == null || f.match.score2 == null) return '-'
  return rowPos === f.posA ? `${f.match.score1}-${f.match.score2}` : `${f.match.score2}-${f.match.score1}`
}

const miniEditingKey = ref(null)

function startMiniEdit(rowPos, colPos) {
  if (rowPos === colPos) return
  const f = miniFixtureBetween(rowPos, colPos)
  if (!f) return
  miniEditingKey.value = `${rowPos}-${colPos}`
  editingMatch.value = f.match
  editScore1.value = rowPos === f.posA ? f.match.score1 : f.match.score2
  editScore2.value = rowPos === f.posA ? f.match.score2 : f.match.score1
  miniSwapped.value = rowPos !== f.posA
}

async function confirmMiniEdit() {
  const m = editingMatch.value
  const score1 = miniSwapped.value ? editScore2.value : editScore1.value
  const score2 = miniSwapped.value ? editScore1.value : editScore2.value
  error.value = ''
  try {
    await api.updateMatch(m.id, {
      competitionId: m.competitionId,
      roundLabel: m.roundLabel,
      date: m.date,
      time: m.time,
      team1Id: m.team1Id,
      team2Id: m.team2Id,
      score1,
      score2
    })
    miniEditingKey.value = null
    await load()
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de l'enregistrement du score."
  }
}

const teamOrder = computed(() => {
  const ordered = []
  const seen = new Set()
  for (const row of standings.value) {
    ordered.push({ id: row.teamId, name: row.teamName, logoPath: row.teamLogoPath, country: row.teamCountry })
    seen.add(row.teamId)
  }
  const extra = new Map()
  for (const m of matches.value) {
    if (!seen.has(m.team1Id)) extra.set(m.team1Id, { name: m.team1Name, logoPath: m.team1LogoPath, country: m.team1Country })
    if (!seen.has(m.team2Id)) extra.set(m.team2Id, { name: m.team2Name, logoPath: m.team2LogoPath, country: m.team2Country })
  }
  // Phase sans aucun match joue pour l'instant (ex: phase de ligue EL/EC pas encore
  // saisie) : on affiche quand meme la grille, a partir des equipes de la
  // competition (recuperees pour le formulaire d'ajout), pour pouvoir commencer a
  // remplir plutot que de n'afficher qu'une grille vide/absente.
  if (props.allowAdd) {
    for (const t of teams.value) {
      if (!seen.has(t.id) && !extra.has(t.id)) extra.set(t.id, { name: t.name, logoPath: t.logoPath, country: t.country })
    }
  }
  for (const [id, info] of [...extra.entries()].sort((a, b) => a[1].name.localeCompare(b[1].name))) {
    ordered.push({ id, ...info })
  }
  return ordered
})

// Groupes provisoires (Meistergruppe/Qualifikationsgruppe, mini-championnats...) bases sur
// l'ordre actuel du classement - juste indicatif tant que la phase de groupe n'a pas
// reellement commence. `sizes` accepte des groupes de tailles differentes (ex: [4, 4, 6]).
// Index de groupe (0-based) d'une equipe, d'apres sa position dans le classement actuel -
// juste indicatif tant que la phase de groupe n'a pas reellement commence.
function groupIndexForTeam(teamId) {
  if (!props.groupSplit) return -1
  const idx = teamOrder.value.findIndex(t => t.id === teamId)
  if (idx === -1) return -1
  let start = 0
  for (let i = 0; i < props.groupSplit.sizes.length; i++) {
    if (idx < start + props.groupSplit.sizes[i]) return i
    start += props.groupSplit.sizes[i]
  }
  return -1
}

// Confrontations au-dela de la saison reguliere (allCycles, cycle > regularSeasonCycles)
// routees vers le mini-championnat des 2 equipes concernees (les 2 doivent appartenir au
// meme groupe pour etre de vrais matchs de phase de groupe).
const repeatMatchesByGroup = computed(() => {
  const perGroup = (props.groupSplit?.sizes ?? []).map(() => new Map())
  if (!props.groupSplit) return perGroup
  for (const c of allCycles.value) {
    if (c.cycle <= regularSeasonCycles.value) continue
    for (const m of c.matchByPair.values()) {
      const g1 = groupIndexForTeam(m.team1Id)
      const g2 = groupIndexForTeam(m.team2Id)
      if (g1 !== -1 && g1 === g2) perGroup[g1].set(`${m.team1Id}-${m.team2Id}`, m)
    }
  }
  return perGroup
})

function malteInRange(m, range) {
  const n = roundNumber(m)
  return n != null && n >= range[0] && n <= range[1]
}

// Identite (nom/logo/pays) d'une equipe d'apres n'importe quel match ou elle apparait -
// necessaire car une poule peut n'avoir encore aucun match joue (poule "a venir").
function malteTeamInfo(id) {
  const m = rawMatches.value.find(mm => mm.team1Id === id || mm.team2Id === id)
  if (!m) return null
  return m.team1Id === id
    ? { id, name: m.team1Name, logoPath: m.team1LogoPath, country: m.team1Country }
    : { id, name: m.team2Name, logoPath: m.team2LogoPath, country: m.team2Country }
}

function malteTeamsFromIds(ids) {
  return ids.map(malteTeamInfo).filter(Boolean).sort((a, b) => a.name.localeCompare(b.name))
}

function malteBuildMatchByPair(list) {
  const map = new Map()
  for (const m of list) map.set(`${m.team1Id}-${m.team2Id}`, m)
  return map
}

const groupBlocks = computed(() => {
  if (props.maltePhases) {
    const blocks = []
    for (const phase of props.maltePhases) {
      const regularMatches = rawMatches.value.filter(m => malteInRange(m, phase.regularRange) && !isPendingTeam(m.team1Name) && !isPendingTeam(m.team2Name))
      const poolMatches = rawMatches.value.filter(m => malteInRange(m, phase.poolRange) && !isPendingTeam(m.team1Name) && !isPendingTeam(m.team2Name))
      blocks.push({
        label: `${phase.label} - Saison régulière`,
        teams: malteTeamsFromIds([...phase.topIds, ...phase.bottomIds]),
        matchByPair: malteBuildMatchByPair(regularMatches)
      }, {
        label: phase.topLabel,
        teams: malteTeamsFromIds(phase.topIds),
        matchByPair: malteBuildMatchByPair(poolMatches.filter(m => phase.topIds.includes(m.team1Id) && phase.topIds.includes(m.team2Id)))
      }, {
        label: phase.bottomLabel,
        teams: malteTeamsFromIds(phase.bottomIds),
        matchByPair: malteBuildMatchByPair(poolMatches.filter(m => phase.bottomIds.includes(m.team1Id) && phase.bottomIds.includes(m.team2Id)))
      })
    }
    return blocks
  }
  if (!props.groupSplit) return []
  const { sizes, labels } = props.groupSplit
  const blocks = []
  let start = 0
  for (let i = 0; i < sizes.length && start < teamOrder.value.length; i++) {
    blocks.push({
      label: labels?.[i] ?? `Groupe ${i + 1}`,
      teams: teamOrder.value.slice(start, start + sizes[i]),
      matchByPair: repeatMatchesByGroup.value[i] ?? new Map()
    })
    start += sizes[i]
  }
  return blocks
})

function shortName(name) {
  return name.length > 3 ? name.slice(0, 3).toUpperCase() : name.toUpperCase()
}

function cellKey(cycle, rowId, colId) {
  return `${cycle}-${rowId}-${colId}`
}

function matchAt(matchByPair, rowId, colId) {
  return matchByPair.get(`${rowId}-${colId}`) ?? null
}

function cellLabel(matchByPair, rowId, colId) {
  const m = matchAt(matchByPair, rowId, colId)
  if (!m || m.score1 == null || m.score2 == null) return '-'
  return `${m.score1}-${m.score2}`
}

function cellClass(matchByPair, rowId, colId) {
  if (rowId === colId) return 'grid-diagonal'
  const m = matchAt(matchByPair, rowId, colId)
  if (!m) return 'grid-empty'
  if (m.score1 == null || m.score2 == null) return 'grid-scheduled'
  if (m.score1 > m.score2) return 'grid-win'
  if (m.score1 < m.score2) return 'grid-loss'
  return 'grid-draw'
}

const editingMatch = ref(null)
const miniSwapped = ref(false)

function startEdit(cycle, matchByPair, rowId, colId) {
  if (rowId === colId) return
  const m = matchAt(matchByPair, rowId, colId)
  if (!m) return
  editingKey.value = cellKey(cycle, rowId, colId)
  editingMatch.value = m
  editScore1.value = m.score1
  editScore2.value = m.score2
}

async function confirmEdit() {
  const m = editingMatch.value
  error.value = ''
  try {
    await api.updateMatch(m.id, {
      competitionId: m.competitionId,
      roundLabel: m.roundLabel,
      date: m.date,
      time: m.time,
      team1Id: m.team1Id,
      team2Id: m.team2Id,
      score1: editScore1.value,
      score2: editScore2.value
    })
    editingKey.value = null
    await load()
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de l'enregistrement du score."
  }
}

async function load() {
  const competitionId = Number(props.competitionId)
  const [matchList, standingsList, teamList] = await Promise.all([
    api.getMatchesByCompetition(competitionId),
    api.getStandings(competitionId, props.round),
    props.allowAdd ? api.getTeams({ competitionId }) : Promise.resolve([])
  ])
  const scoped = props.roundIncludes
    ? matchList.filter(m => props.roundIncludes.some(f => (m.roundLabel ?? '').toUpperCase().includes(f.toUpperCase())))
    : matchList
  // La grille matricielle n'a de sens qu'entre equipes reelles connues : les confrontations
  // "en attente de tirage" (barrage, mini-championnat de fin de saison...) partagent toutes
  // les 2 memes equipes generiques "A DETERMINER" et n'ont donc pas leur place ici tant
  // qu'elles n'ont pas ete completees (elles restent visibles dans l'onglet Championnat).
  rawMatches.value = scoped
  matches.value = scoped.filter(m => !isPendingTeam(m.team1Name) && !isPendingTeam(m.team2Name))
  standings.value = standingsList
  teams.value = teamList
}

watch(() => props.competitionId, load)
onMounted(load)
</script>

<style scoped>
.results-grid-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.results-grid-header h2 {
  margin: 0;
}

.results-grid-block {
  margin-bottom: 32px;
}

.results-grid-block h3 {
  margin: 0 0 10px;
}

.pending-note {
  font-weight: 400;
  font-size: 0.75em;
  color: var(--text-muted);
}

.add-match-btn {
  white-space: nowrap;
}

.add-match-form {
  margin-bottom: 16px;
}

.grid-scroll {
  overflow-x: auto;
  margin-bottom: 24px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: var(--surface);
  /* Sort la grille du conteneur centre (#app, max-width: 1080px) pour utiliser
     toute la largeur de la fenetre et eviter le scroll horizontal avec 36 equipes. */
  width: 100vw;
  position: relative;
  left: 50%;
  right: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
}

table.results-grid {
  border: none;
  border-radius: 0;
  margin-bottom: 0;
}

table.results-grid th, table.results-grid td {
  text-align: center;
  padding: 3px 4px;
  font-size: 0.72em;
  white-space: nowrap;
  border: 1px solid var(--border);
}

table.results-grid thead th {
  position: sticky;
  top: 0;
  background: var(--surface-muted);
  z-index: 1;
}

table.results-grid tbody th {
  text-align: left;
  position: sticky;
  left: 0;
  background: var(--surface-muted);
  font-weight: 600;
  z-index: 1;
  max-width: 130px;
  overflow: hidden;
  text-overflow: ellipsis;
}

table.results-grid .corner {
  position: sticky;
  left: 0;
  top: 0;
  z-index: 2;
  background: var(--surface-muted);
}

table.results-grid td {
  cursor: pointer;
  min-width: 28px;
}

table.results-grid tbody tr:hover td {
  background: unset;
}

.grid-diagonal {
  background: #1c2521 !important;
  cursor: default;
}

.grid-empty {
  color: #c2cac5;
  cursor: default;
}

.grid-scheduled {
  color: var(--text-muted);
}

.grid-win {
  background: #dcf5e6;
  font-weight: 700;
}

.grid-loss {
  background: #fbe0de;
}

.grid-draw {
  background: #fbf3c7;
  font-weight: 700;
}

.grid-row-header {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.grid-edit {
  display: inline-flex;
  gap: 2px;
  align-items: center;
}

.grid-edit .score-input {
  width: 32px;
  padding: 2px;
}

.grid-edit button {
  padding: 2px 6px;
}
</style>

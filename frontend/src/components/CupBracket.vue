<template>
  <div class="action-bar">
    <button type="button" class="action-btn" @click="showMatchModal = true">+ Ajouter un match</button>
    <button type="button" class="action-btn action-btn--secondary" @click="showTeamModal = true">+ Ajouter un club</button>
  </div>

  <Modal v-model="showMatchModal" title="Ajouter un match">
    <form class="inline" @submit.prevent="submitMatch">
      <select v-model.number="newMatch.team1Id" required>
        <option disabled value="">Équipe 1</option>
        <option v-for="t in teams" :key="t.id" :value="t.id">{{ t.name }}</option>
      </select>
      <select v-model.number="newMatch.team2Id" required>
        <option disabled value="">Équipe 2</option>
        <option v-for="t in teams" :key="t.id" :value="t.id">{{ t.name }}</option>
      </select>
      <input v-model="newMatch.roundLabel" placeholder="Round (ex: Coupe nationale)" required />
      <input v-model="newMatch.date" type="date" />
      <input v-model="newMatch.time" type="time" />
      <input class="score-input" type="number" min="0" v-model.number="newMatch.score1" placeholder="B1" />
      <input class="score-input" type="number" min="0" v-model.number="newMatch.score2" placeholder="B2" />
      <button type="submit">Ajouter</button>
    </form>
  </Modal>

  <Modal v-model="showTeamModal" title="Ajouter un club">
    <form class="inline" @submit.prevent="submitNewTeam">
      <input v-model="newTeam.name" placeholder="Nom du club" required />
      <button type="submit">Ajouter</button>
    </form>
    <p v-if="teamError" class="error-text">{{ teamError }}</p>
  </Modal>

  <div class="bracket-scroll" v-if="rounds.length">
    <div class="bracket">
      <div
        class="bracket-round"
        v-for="(r, ri) in rounds"
        :key="r.round"
        :class="{ 'bracket-round--last': ri === rounds.length - 1 }"
      >
        <h3 class="round-badge">{{ r.label }}</h3>
        <div class="bracket-round-body">
          <div
            class="match-pair"
            v-for="(pair, pi) in pairChunks(r.ties)"
            :key="pi"
            :class="{ 'match-pair--connect': ri < rounds.length - 1, 'match-pair--single': pair.length === 1 }"
          >
            <div
              class="tie-box"
              :class="{ 'tie-box--decided': tie.winnerId, 'tie-box--incoming': ri > 0 }"
              v-for="tie in pair"
              :key="tieKey(tie)"
            >
              <div class="tie-leg-wrap" v-for="(leg, li) in tie.legs" :key="leg.id">
                <span v-if="tie.legs.length > 1" class="leg-tag">{{ legLabel(li, tie.legs.length) }}</span>
                <div class="tie-leg">
                  <template v-if="teamEditingKey === teamKey(leg, 1)">
                    <span class="grid-edit" @click.stop>
                      <select v-model.number="editTeamValue">
                        <option v-for="t in sortedTeams" :key="t.id" :value="t.id">{{ t.name }}</option>
                      </select>
                      <button type="button" @click.stop="confirmTeamEdit(leg, 1)">✓</button>
                    </span>
                  </template>
                  <span v-else class="tie-team" :class="{ 'tie-winner': tie.winnerId === leg.team1Id }" @click="startTeamEdit(leg, 1)">
                    <TeamLogo :name="leg.team1Name" :logo-path="leg.team1LogoPath" />
                    <FlagIcon v-if="showFlags" :country="leg.team1Country" />
                    <span class="tie-team-name">{{ leg.team1Name }}</span>
                  </span>
                  <template v-if="editingId === leg.id">
                    <span class="grid-edit">
                      <input class="score-input" type="number" min="0" v-model.number="editScore1" @click.stop />
                      <input class="score-input" type="number" min="0" v-model.number="editScore2" @click.stop />
                      <button type="button" @click.stop="confirmEdit(leg)">✓</button>
                    </span>
                  </template>
                  <span v-else class="tie-score" @click="startEdit(leg)">
                    {{ leg.score1 ?? '-' }}<span class="tie-score-sep">:</span>{{ leg.score2 ?? '-' }}
                  </span>
                  <template v-if="teamEditingKey === teamKey(leg, 2)">
                    <span class="grid-edit" @click.stop>
                      <select v-model.number="editTeamValue">
                        <option v-for="t in sortedTeams" :key="t.id" :value="t.id">{{ t.name }}</option>
                      </select>
                      <button type="button" @click.stop="confirmTeamEdit(leg, 2)">✓</button>
                    </span>
                  </template>
                  <span v-else class="tie-team tie-team--right" :class="{ 'tie-winner': tie.winnerId === leg.team2Id }" @click="startTeamEdit(leg, 2)">
                    <span class="tie-team-name">{{ leg.team2Name }}</span>
                    <FlagIcon v-if="showFlags" :country="leg.team2Country" />
                    <TeamLogo :name="leg.team2Name" :logo-path="leg.team2LogoPath" />
                  </span>
                </div>
              </div>
              <div class="tie-aggregate" v-if="tie.legs.length > 1">
                <span class="tie-aggregate-tag">Agrégat</span>
                <span class="tie-aggregate-team" :class="{ 'tie-winner': tie.winnerId === tie.teamAId }">{{ tie.teamAName }}</span>
                <span class="tie-aggregate-score">{{ tie.aggA }} – {{ tie.aggB }}</span>
                <span class="tie-aggregate-team" :class="{ 'tie-winner': tie.winnerId === tie.teamBId }">{{ tie.teamBName }}</span>
              </div>
              <div class="tie-penalties" v-if="tie.wentToPenalties">
                <span class="tie-penalties-tag">Tab</span>
                <span class="tie-aggregate-team" :class="{ 'tie-winner': tie.winnerId === tie.teamAId }">{{ tie.teamAName }}</span>
                <span class="tie-aggregate-score" @click="startPenaltyEdit(tie)">{{ tie.penA }} – {{ tie.penB }}</span>
                <span class="tie-aggregate-team" :class="{ 'tie-winner': tie.winnerId === tie.teamBId }">{{ tie.teamBName }}</span>
              </div>
              <div class="tie-penalties" v-else-if="tie.needsPenalty">
                <template v-if="editingPenaltyId === tie.decider.id">
                  <span class="tie-penalties-tag">Tab</span>
                  <span class="grid-edit">
                    <input class="score-input" type="number" min="0" v-model.number="editPen1" @click.stop />
                    <input class="score-input" type="number" min="0" v-model.number="editPen2" @click.stop />
                    <button type="button" @click.stop="confirmPenaltyEdit(tie)">✓</button>
                  </span>
                </template>
                <button v-else type="button" class="tie-penalties-add" @click="startPenaltyEdit(tie)">+ Tirs au but</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <p v-else-if="loaded" class="empty-state">Aucun match dans cette coupe pour l'instant.</p>

  <p v-if="error" class="error-text">{{ error }}</p>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import api from '../services/api'
import TeamLogo from './TeamLogo.vue'
import FlagIcon from './FlagIcon.vue'
import Modal from './Modal.vue'

const props = defineProps({
  competitionId: { type: [String, Number], required: true },
  // Fragments (insensibles a la casse) : un match n'est garde que si son round_label
  // contient au moins un de ces fragments. Pas de filtre => tous les matchs.
  roundIncludes: { type: Array, default: null },
  // Quand fourni, remplace l'inference automatique des tours (par date/vainqueur) par
  // un regroupement explicite base sur le round_label reel des matchs : [{ frag, label }, ...],
  // dans l'ordre d'affichage voulu. Chaque tie est classe dans le premier groupe dont le
  // fragment (insensible a la casse) apparait dans le round_label de son premier match.
  explicitRounds: { type: Array, default: null },
  showFlags: { type: Boolean, default: false }
})

const matches = ref([])
const teams = ref([])
const loaded = ref(false)
const error = ref('')
const editingId = ref(null)
const editScore1 = ref(null)
const editScore2 = ref(null)
const editingPenaltyId = ref(null)
const editPen1 = ref(null)
const editPen2 = ref(null)
const teamEditingKey = ref(null)
const editTeamValue = ref(null)
const competitionCountry = ref(null)
const newTeam = reactive({ name: '' })
const teamError = ref('')
const showMatchModal = ref(false)
const showTeamModal = ref(false)

const newMatch = reactive({
  team1Id: '', team2Id: '', roundLabel: 'Coupe nationale', date: '', time: '', score1: null, score2: null
})

// Tours "en attente de tirage" (cf. import/import_excel.py PLACEHOLDER_RE et
// V16__pending_draw_matches.sql) : round_label du type "... (a determiner : HF-HF)".
const PENDING_DRAW_LABELS = { HF: '8es de finale', QF: 'Quarts de finale', DF: 'Demi-finales', F: 'Finale' }
const PENDING_DRAW_ORDER = { HF: 0, QF: 1, DF: 2, F: 3 }
const PENDING_DRAW_RE = /\(a d[ée]terminer\s*:\s*(HF|QF|DF|F)-\1\)/i

function isPendingTeam(name) {
  return (name ?? '').toUpperCase().startsWith('A DETERMINER')
}

// Cf. V88__restore_moldavie_montenegro_two_legged_cup_ties.sql : tant que les 2 equipes
// restent des places generiques "A DETERMINER", 2 manches d'une meme confrontation
// partagent les 2 memes IDs generiques des 2 cotes et sont donc indiscernables l'une de
// l'autre - un suffixe explicite dans le round_label est necessaire pour les regrouper
// (sinon chaque manche s'affiche comme sa propre confrontation, cf. bug corrige plus bas).
const ALLER_RETOUR_SUFFIX_RE = /-\s*(Aller|Retour)\s*$/i

// Regroupe par tour reel (round_label), pas par nombre de confrontations restantes :
// un simple comptage se desynchronise des que certaines confrontations d'un tour sont
// resolues avant les autres. On fusionne aussi Aller/Retour d'un meme tour, et on
// reconnait les libelles "en attente de tirage" pour leur donner un nom lisible.
function roundBucketFor(label) {
  const raw = label ?? ''
  const pending = raw.match(PENDING_DRAW_RE)
  if (pending) {
    const code = pending[1].toUpperCase()
    return { key: `PENDING-${code}`, label: PENDING_DRAW_LABELS[code] ?? raw, order: PENDING_DRAW_ORDER[code] }
  }
  const stripped = raw.replace(/-?\s*(aller|retour)\s*$/i, '').trim()
  return { key: (stripped || raw).toUpperCase(), label: stripped || raw || 'Autre', order: null }
}

function tieKey(tie) {
  return `${tie.teamAId}-${tie.teamBId}-${tie.legs[0].id}`
}

function legLabel(index, total) {
  if (index === 0) return 'Aller'
  if (index === total - 1) return 'Retour'
  return `Manche ${index + 1}`
}

// Regroupe les confrontations d'un tour par paires de 2 (celles qui s'affrontaient au tour
// precedent) pour dessiner le "coude" de connexion vers la confrontation suivante. Un
// nombre impair (bye, tour incomplet) laisse la derniere paire a une seule confrontation.
function pairChunks(ties) {
  const chunks = []
  for (let i = 0; i < ties.length; i += 2) chunks.push(ties.slice(i, i + 2))
  return chunks
}

const filteredMatches = computed(() => {
  if (!props.roundIncludes) return matches.value
  const fragments = props.roundIncludes.map(f => f.toUpperCase())
  return matches.value.filter(m => fragments.some(f => (m.roundLabel ?? '').toUpperCase().includes(f)))
})

// A partir des matchs d'un meme tour, regroupe en confrontations (une confrontation =
// plusieurs legs Aller/Retour entre les 2 memes equipes). Les matchs encore "en attente
// de tirage" (equipes generiques "A DETERMINER") ne sont JAMAIS fusionnes entre eux -
// ils partagent tous la meme paire d'ID generique, donc les regrouper par equipes les
// ecraserait tous en une seule confrontation fantome (bug corrige : matchs "manquants").
function buildTies(roundMatches) {
  const tieMap = new Map()
  const pendingAllerQueue = []
  let pendingTieCounter = 0
  const sortedById = roundMatches.slice().sort((a, b) => a.id - b.id)
  for (const m of sortedById) {
    const pending = isPendingTeam(m.team1Name) || isPendingTeam(m.team2Name)
    if (!pending) {
      const key = [m.team1Id, m.team2Id].sort((a, b) => a - b).join('-')
      if (!tieMap.has(key)) tieMap.set(key, [])
      tieMap.get(key).push(m)
      continue
    }
    const legMatch = (m.roundLabel ?? '').match(ALLER_RETOUR_SUFFIX_RE)
    if (!legMatch) {
      tieMap.set(`match-${m.id}`, [m])
      continue
    }
    if (/aller/i.test(legMatch[1])) {
      pendingTieCounter++
      const key = `pending-tie-${pendingTieCounter}`
      tieMap.set(key, [m])
      pendingAllerQueue.push(key)
    } else {
      const key = pendingAllerQueue.shift()
      if (key) tieMap.get(key).push(m)
      else tieMap.set(`match-${m.id}`, [m])
    }
  }
  return [...tieMap.values()].map(legs => {
    const sorted = legs.slice().sort((a, b) => (a.date ?? '').localeCompare(b.date ?? ''))
    const first = sorted[0]
    const teamAId = first.team1Id
    const teamAName = first.team1Name
    const teamBId = first.team2Id
    const teamBName = first.team2Name
    let aggA = 0, aggB = 0, hasAllScores = true
    for (const leg of sorted) {
      if (leg.score1 == null || leg.score2 == null) { hasAllScores = false; continue }
      if (leg.team1Id === teamAId) { aggA += leg.score1; aggB += leg.score2 }
      else { aggA += leg.score2; aggB += leg.score1 }
    }
    // Tirs au but : uniquement pertinents si l'aggregat (ou le match unique) est a egalite -
    // portes par le dernier match joue (celui qui a effectivement ete suivi de la seance).
    const decider = sorted[sorted.length - 1]
    const aggTied = hasAllScores && aggA === aggB
    let penA = null, penB = null
    if (aggTied && decider.penaltyScore1 != null && decider.penaltyScore2 != null) {
      if (decider.team1Id === teamAId) { penA = decider.penaltyScore1; penB = decider.penaltyScore2 }
      else { penA = decider.penaltyScore2; penB = decider.penaltyScore1 }
    }
    const wentToPenalties = penA != null && penB != null
    const winnerId = wentToPenalties
      ? (penA > penB ? teamAId : teamBId)
      : (hasAllScores && aggA !== aggB ? (aggA > aggB ? teamAId : teamBId) : null)
    return {
      teamAId, teamAName, teamBId, teamBName, legs: sorted, aggA, aggB, hasAllScores, winnerId,
      decider, needsPenalty: aggTied && !wentToPenalties, wentToPenalties, penA, penB
    }
  })
}

// Reordonne les confrontations d'un tour pour que chacune se retrouve visuellement a la
// position de ses 2 equipes au tour precedent (moyenne des index) plutot qu'a la position
// que lui donnerait un simple tri par date : sinon, des qu'un tour n'est pas rejoue dans le
// meme ordre que le tour precedent (frequent en qualifs de coupe d'Europe, ou les dates ne
// suivent pas la structure de l'arbre), une equipe se retrouve visuellement tres loin de son
// match precedent alors que le trait de connexion CSS (dessine par simple position d'index,
// cf. pairChunks) suppose implicitement le meme ordre d'un tour a l'autre.
function reorderForBracket(prevTies, ties) {
  if (!prevTies) return ties
  const prevIndex = new Map()
  prevTies.forEach((tie, idx) => {
    prevIndex.set(tie.teamAId, idx)
    prevIndex.set(tie.teamBId, idx)
  })
  const positionOf = tie => {
    const a = prevIndex.get(tie.teamAId)
    const b = prevIndex.get(tie.teamBId)
    if (a != null && b != null) return (a + b) / 2
    if (a != null) return a
    if (b != null) return b
    return null
  }
  return ties
    .map((tie, originalIndex) => ({ tie, originalIndex, position: positionOf(tie) }))
    .sort((x, y) => {
      // Une equipe absente du tour precedent (bye, entree directe a ce tour) garde sa
      // place d'origine (deja triee par date) plutot que d'etre arbitrairement deplacee.
      if (x.position != null && y.position != null) return x.position - y.position
      if (x.position != null) return -1
      if (y.position != null) return 1
      return x.originalIndex - y.originalIndex
    })
    .map(x => x.tie)
}

const rounds = computed(() => {
  if (!filteredMatches.value.length) return []

  const buckets = new Map()
  for (const m of filteredMatches.value) {
    const { key, label, order } = roundBucketFor(m.roundLabel)
    if (!buckets.has(key)) buckets.set(key, { label, order, matches: [] })
    buckets.get(key).matches.push(m)
  }

  let result
  if (props.explicitRounds) {
    const byGroup = new Map()
    for (const [, bucket] of buckets) {
      const upper = bucket.label.toUpperCase()
      const group = props.explicitRounds.find(g => upper.includes(g.frag.toUpperCase()))
      const key = group ? group.label : 'Autre'
      if (!byGroup.has(key)) byGroup.set(key, [])
      byGroup.get(key).push(...bucket.matches)
    }
    const orderedLabels = [...props.explicitRounds.map(g => g.label), 'Autre'].filter(l => byGroup.has(l))
    result = orderedLabels.map(label => ({
      round: label,
      label,
      ties: buildTies(byGroup.get(label)).sort((a, b) => (a.legs[0].date ?? '').localeCompare(b.legs[0].date ?? ''))
    }))
  } else {
    const entries = [...buckets.entries()].map(([key, bucket]) => {
      const minDate = bucket.matches.reduce((min, m) => (m.date && (!min || m.date < min) ? m.date : min), null)
      return {
        round: key,
        label: bucket.label,
        order: bucket.order,
        minDate,
        ties: buildTies(bucket.matches).sort((a, b) => (a.legs[0].date ?? '').localeCompare(b.legs[0].date ?? ''))
      }
    })
    // Priorite a la date reelle du tour (les tours "en attente de tirage" ont presque
    // toujours une date programmee, meme sans equipes connues - ex: Georgie, Gibraltar) :
    // sinon un tour non date (ex: places generiques sans date, cf. Estonie) se retrouvait
    // trie AVANT les tours deja joues juste parce qu'il portait un code HF/QF/DF/F connu.
    // Le code de tour ne sert de repli que si aucun des deux tours n'a de date du tout.
    entries.sort((a, b) => {
      if (a.minDate && b.minDate) return a.minDate.localeCompare(b.minDate)
      if (a.minDate) return -1
      if (b.minDate) return 1
      if (a.order != null && b.order != null) return a.order - b.order
      if (a.order != null) return -1
      if (b.order != null) return 1
      return 0
    })
    result = entries
  }

  for (let i = 1; i < result.length; i++) {
    result[i].ties = reorderForBracket(result[i - 1].ties, result[i].ties)
  }
  return result
})

const sortedTeams = computed(() => teams.value.slice().sort((a, b) => a.name.localeCompare(b.name)))

function teamKey(leg, side) {
  return `${leg.id}-${side}`
}

function startTeamEdit(leg, side) {
  teamEditingKey.value = teamKey(leg, side)
  editTeamValue.value = side === 1 ? leg.team1Id : leg.team2Id
}

async function confirmTeamEdit(leg, side) {
  error.value = ''
  try {
    await api.updateMatch(leg.id, {
      competitionId: leg.competitionId,
      roundLabel: leg.roundLabel,
      date: leg.date,
      time: leg.time,
      team1Id: side === 1 ? editTeamValue.value : leg.team1Id,
      team2Id: side === 2 ? editTeamValue.value : leg.team2Id,
      score1: leg.score1,
      score2: leg.score2,
      penaltyScore1: leg.penaltyScore1,
      penaltyScore2: leg.penaltyScore2
    })
    teamEditingKey.value = null
    await load()
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de la modification de l'équipe."
  }
}

async function startEdit(leg) {
  editingId.value = leg.id
  editScore1.value = leg.score1
  editScore2.value = leg.score2
}

async function confirmEdit(leg) {
  error.value = ''
  try {
    await api.updateMatch(leg.id, {
      competitionId: leg.competitionId,
      roundLabel: leg.roundLabel,
      date: leg.date,
      time: leg.time,
      team1Id: leg.team1Id,
      team2Id: leg.team2Id,
      score1: editScore1.value,
      score2: editScore2.value,
      penaltyScore1: leg.penaltyScore1,
      penaltyScore2: leg.penaltyScore2
    })
    editingId.value = null
    await load()
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de l'enregistrement du score."
  }
}

function startPenaltyEdit(tie) {
  editingPenaltyId.value = tie.decider.id
  editPen1.value = tie.penA
  editPen2.value = tie.penB
}

async function confirmPenaltyEdit(tie) {
  error.value = ''
  const leg = tie.decider
  // editPen1/editPen2 sont exprimes cote "teamA/teamB" de la confrontation, a reconvertir
  // vers team1/team2 de CE match precis (peut etre inverse, ex: match retour).
  const [penaltyScore1, penaltyScore2] = leg.team1Id === tie.teamAId
    ? [editPen1.value, editPen2.value]
    : [editPen2.value, editPen1.value]
  try {
    await api.updateMatch(leg.id, {
      competitionId: leg.competitionId,
      roundLabel: leg.roundLabel,
      date: leg.date,
      time: leg.time,
      team1Id: leg.team1Id,
      team2Id: leg.team2Id,
      score1: leg.score1,
      score2: leg.score2,
      penaltyScore1,
      penaltyScore2
    })
    editingPenaltyId.value = null
    await load()
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de l'enregistrement des tirs au but."
  }
}

async function submitMatch() {
  error.value = ''
  try {
    await api.createMatch({
      competitionId: Number(props.competitionId),
      roundLabel: newMatch.roundLabel,
      date: newMatch.date || null,
      time: newMatch.time || null,
      team1Id: newMatch.team1Id,
      team2Id: newMatch.team2Id,
      score1: newMatch.score1,
      score2: newMatch.score2
    })
    newMatch.date = ''
    newMatch.time = ''
    newMatch.score1 = null
    newMatch.score2 = null
    showMatchModal.value = false
    await load()
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de la création du match."
  }
}

async function submitNewTeam() {
  teamError.value = ''
  try {
    await api.createTeam({ name: newTeam.name.toUpperCase(), country: competitionCountry.value })
    newTeam.name = ''
    teams.value = await api.getTeams({ competitionId: Number(props.competitionId) })
    showTeamModal.value = false
  } catch (e) {
    teamError.value = e.response?.data?.error ?? "Erreur lors de la création du club."
  }
}

async function load() {
  loaded.value = false
  const competitionId = Number(props.competitionId)
  const [matchList, teamList, competitions] = await Promise.all([
    api.getMatchesByCompetition(competitionId),
    api.getTeams({ competitionId }),
    api.getCompetitions()
  ])
  matches.value = matchList
  teams.value = teamList
  competitionCountry.value = competitions.find(c => c.id === competitionId)?.country ?? null
  loaded.value = true
}

watch(() => props.competitionId, load)
onMounted(load)
</script>

<style scoped>
/* Fond "stade de nuit" : degrade sombre + lueurs de projecteurs, dans l'esprit d'une
   affiche de tournoi. Le bracket lui-meme dessine les lignes de connexion classiques
   (coude reliant 2 confrontations vers la suivante) en CSS pur via les bordures. */
.bracket-scroll {
  overflow-x: auto;
  overflow-y: hidden;
  margin-bottom: 24px;
  padding: 30px 12px 22px;
  border-radius: 18px;
  position: relative;
  /* Sort du conteneur centre (#app, max-width limite) pour utiliser toute la largeur de
     la fenetre et eviter le scroll horizontal avec plusieurs tours de bracket cote a cote. */
  width: 100vw;
  left: 50%;
  right: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
  background:
    radial-gradient(circle at 12% 15%, rgba(255, 255, 255, 0.16), transparent 32%),
    radial-gradient(circle at 88% 20%, rgba(255, 255, 255, 0.14), transparent 30%),
    radial-gradient(circle at 50% 0%, rgba(255, 255, 255, 0.08), transparent 45%),
    linear-gradient(180deg, #0a1120 0%, #101c33 55%, #0b1424 100%);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.06), 0 10px 30px rgba(0, 0, 0, 0.25);
}

.bracket-scroll::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 14px;
  background: linear-gradient(180deg, transparent, rgba(46, 160, 67, 0.35));
  border-radius: 0 0 18px 18px;
  pointer-events: none;
}

.bracket {
  display: flex;
  align-items: stretch;
  min-width: max-content;
  position: relative;
  z-index: 1;
}

.bracket-round {
  display: flex;
  flex-direction: column;
  min-width: 200px;
  padding: 0 22px;
}

.bracket-round--last {
  padding-right: 8px;
}

.round-badge {
  margin: 0 0 20px;
  align-self: center;
  background: rgba(255, 255, 255, 0.12);
  color: #f4f7f5;
  border: 1px solid rgba(255, 255, 255, 0.28);
  border-radius: 999px;
  padding: 7px 16px;
  font-size: 0.78em;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  text-align: center;
  white-space: nowrap;
  backdrop-filter: blur(2px);
}

.bracket-round-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
}

.match-pair {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 18px;
}

.match-pair--single {
  justify-content: center;
}

/* Coude sortant : relie le milieu des 2 confrontations de la paire vers le point median,
   puis un trait horizontal continue jusqu'au tour suivant. */
.match-pair--connect::after {
  content: '';
  position: absolute;
  top: 25%;
  bottom: 25%;
  right: -30px;
  width: 30px;
  border-top: 2px solid rgba(255, 255, 255, 0.45);
  border-right: 2px solid rgba(255, 255, 255, 0.45);
  border-bottom: 2px solid rgba(255, 255, 255, 0.45);
  border-top-right-radius: 8px;
  border-bottom-right-radius: 8px;
}

.match-pair--single.match-pair--connect::after {
  top: 50%;
  bottom: auto;
  height: 0;
  border-right: none;
  border-bottom: none;
  border-top-right-radius: 0;
}

/* Trait entrant : relie le tour precedent au milieu de chaque confrontation. */
.tie-box--incoming::before {
  content: '';
  position: absolute;
  top: 50%;
  left: -30px;
  width: 30px;
  border-top: 2px solid rgba(255, 255, 255, 0.45);
}

.tie-box {
  position: relative;
  background: linear-gradient(180deg, #ffffff, #f1f4f2);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  padding: 8px 10px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.35);
  transition: box-shadow 0.15s ease, transform 0.15s ease;
  overflow: hidden;
}

.tie-box:hover {
  box-shadow: 0 8px 22px rgba(0, 0, 0, 0.45);
  transform: translateY(-1px);
}

.tie-box--decided {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.35), inset 3px 0 0 var(--primary);
}

.tie-leg-wrap + .tie-leg-wrap {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed var(--border);
}

.leg-tag {
  display: block;
  font-size: 0.66em;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-muted);
  margin-bottom: 3px;
}

.tie-leg {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 6px;
  font-size: 0.74em;
}

.tie-team {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  overflow: hidden;
  min-width: 0;
  cursor: pointer;
}

.tie-team:hover .tie-team-name {
  text-decoration: underline;
}

.tie-team-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tie-team--right {
  justify-content: flex-end;
  text-align: right;
}

.tie-winner {
  font-weight: 700;
  color: var(--primary-dark);
}

.tie-box--decided .tie-team:not(.tie-winner) {
  opacity: 0.5;
}

.tie-score {
  cursor: pointer;
  font-variant-numeric: tabular-nums;
  font-weight: 700;
  font-size: 1.05em;
  color: var(--text);
  background: var(--surface-muted);
  padding: 3px 9px;
  border-radius: 8px;
  white-space: nowrap;
}

.tie-score:hover {
  background: var(--primary-soft);
}

.tie-score-sep {
  color: var(--text-muted);
  margin: 0 2px;
}

.tie-aggregate {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 6px;
  margin: 10px -13px -11px;
  padding: 7px 13px;
  background: var(--surface-muted);
  border-top: 1px solid var(--border);
  font-size: 0.68em;
}

.tie-aggregate-tag {
  font-weight: 700;
  text-transform: uppercase;
  font-size: 0.85em;
  letter-spacing: 0.03em;
  color: var(--text-muted);
  margin-right: 2px;
}

.tie-aggregate-team {
  color: var(--text-muted);
}

.tie-aggregate-score {
  font-weight: 800;
  color: var(--text);
}

.tie-penalties {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 6px;
  margin: 0 -13px -11px;
  padding: 5px 13px 7px;
  background: var(--surface-muted);
  border-top: 1px solid var(--border);
  font-size: 0.68em;
}

.tie-penalties-tag {
  font-weight: 700;
  text-transform: uppercase;
  font-size: 0.85em;
  letter-spacing: 0.03em;
  color: var(--text-muted);
  margin-right: 2px;
}

.tie-penalties-add {
  background: none;
  border: none;
  color: var(--text-muted);
  font-size: 0.85em;
  cursor: pointer;
  text-decoration: underline;
  padding: 0;
}

.tie-penalties-add:hover {
  color: var(--text);
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

<template>
  <template v-if="groupedRows.length">
    <div v-for="grp in groupedRows" :key="grp.name ?? '_'" class="standings-group">
      <h2 v-if="grp.name">{{ grp.name }}</h2>
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>Équipe</th>
            <th>MJ</th>
            <th>V</th>
            <th>N</th>
            <th>D</th>
            <th>BP</th>
            <th>BC</th>
            <th>DIFF</th>
            <th>PTS</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, index) in grp.rows" :key="row.teamId" :class="rowClass(row, grp.baseIndex + index, grp)">
            <td class="col-rank">
              <span class="rank-num">{{ grp.baseIndex + index + 1 }}</span>
              <span
                v-for="(m, mi) in markersFor(grp.baseIndex + index + 1)"
                :key="mi"
                class="rank-marker"
                :title="m.tooltip"
              >{{ m.icon }}</span>
            </td>
            <td>
              <span class="team-cell">
                <TeamLogo :name="row.teamName" :logo-path="row.teamLogoPath" />
                <FlagIcon v-if="showFlags" :country="row.teamCountry" />
                <span class="team-name">{{ row.teamName }}</span>
              </span>
            </td>
            <td>{{ row.played }}</td>
            <td>{{ row.won }}</td>
            <td>{{ row.drawn }}</td>
            <td>{{ row.lost }}</td>
            <td>{{ row.goalsFor }}</td>
            <td>{{ row.goalsAgainst }}</td>
            <td :class="row.goalDifference > 0 ? 'diff-pos' : row.goalDifference < 0 ? 'diff-neg' : ''">{{ row.goalDifference > 0 ? '+' : '' }}{{ row.goalDifference }}</td>
            <td><span class="pts-pill">{{ row.points }}</span></td>
          </tr>
        </tbody>
      </table>
    </div>
  </template>
  <p v-else class="empty-state">Aucun match joué pour l'instant dans cette compétition.</p>

  <ul v-if="rows.length && showBaseLegend" class="standings-legend">
    <li><span class="legend-swatch legend-blue"></span>Champion sortant</li>
    <li><span class="legend-swatch legend-purple"></span>Vainqueur de la coupe (préc.)</li>
    <li><span class="legend-swatch legend-red"></span>Promu</li>
    <li><span class="legend-swatch legend-green"></span>A joué la Ligue des Champions (préc.)</li>
    <li><span class="legend-swatch legend-orange-dark"></span>A joué l'Europa League (préc.)</li>
    <li><span class="legend-swatch legend-yellow"></span>A joué la Conference League (préc.)</li>
  </ul>
  <ul v-if="rows.length && (ldcSlots > 0 || elSlots > 0 || eclSlots > 0 || barrageSlots > 0 || relegationSlots > 0)" class="standings-legend">
    <li v-if="ldcSlots > 0">🏆 Champion</li>
    <li v-if="ldcSlots > 1">🔷 Qualifié Ligue des Champions</li>
    <li v-if="elSlots > 0">🎖️ Qualifié Europa League</li>
    <li v-if="eclSlots > 0">🌍 Qualifié Conference League</li>
    <li v-if="barrageSlots > 0">⚔️ Barrage de maintien</li>
    <li v-if="relegationSlots > 0">⬇️ Descend en division inférieure</li>
  </ul>
</template>

<script setup>
import { computed } from 'vue'
import TeamLogo from './TeamLogo.vue'
import FlagIcon from './FlagIcon.vue'

const props = defineProps({
  rows: { type: Array, default: () => [] },
  teamStatuses: { type: Object, default: () => ({}) },
  ldcSlots: { type: Number, default: 0 },
  elSlots: { type: Number, default: 0 },
  eclSlots: { type: Number, default: 0 },
  relegationSlots: { type: Number, default: 0 },
  barrageSlots: { type: Number, default: 0 },
  showBaseLegend: { type: Boolean, default: true },
  showFlags: { type: Boolean, default: false },
  // Cas particulier (phase de ligue continentale LDC/EL/EC) : decoupage en tranches de
  // rang par couleur, ex: [{ count: 8, class: 'standing-blue' }, { count: 16, class:
  // 'standing-green' }] puis 'standing-red' pour le reste. Independant des places
  // qualificatives/barrage/relegation (qui n'ont pas de sens pour une phase de groupes).
  rankBands: { type: Array, default: null },
  rankBandsRest: { type: String, default: 'standing-red' },
  // Icone + infobulle supplementaire(s) a afficher a cote d'un rang precis, en plus des
  // icones automatiques LDC/EL/ECL/barrage/relegation (ex: mini-championnat top 4 en
  // Albanie) : [{ rank: 1, icon: '🏅', tooltip: '...' }]
  rankMarkers: { type: Array, default: null },
  // Couleur par ligne calculee groupe par groupe (selections nationales : places
  // qualificatives propres a chaque ligue/groupe) : (groupName, rows) => [classe par ligne].
  // Prioritaire sur rankBands et sur les statuts de club.
  groupRowClasses: { type: Function, default: null }
})

// Icones automatiques de qualification/barrage/relegation, calculees a partir des places
// configurees pour la competition (memes champs que "Configurer les places qualificatives").
// Generique a tous les championnats ; 0 place configuree => aucune icone de cette categorie.
const autoMarkers = computed(() => {
  const total = props.rows.length
  const markers = []
  let rank = 1
  // Le 1er est champion (cette saison) : a differencier des autres places qualificatives
  // LDC, qui ne sont "que" qualifiees sans etre sacrees.
  if (props.ldcSlots > 0) {
    markers.push({ rank, icon: '🏆', tooltip: 'Champion' })
    rank++
  }
  for (let i = 1; i < props.ldcSlots; i++, rank++) markers.push({ rank, icon: '🔷', tooltip: 'Qualifié Ligue des Champions' })
  for (let i = 0; i < props.elSlots; i++, rank++) markers.push({ rank, icon: '🎖️', tooltip: 'Qualifié Europa League' })
  for (let i = 0; i < props.eclSlots; i++, rank++) markers.push({ rank, icon: '🌍', tooltip: 'Qualifié Conference League' })

  const relegationStart = total - props.relegationSlots + 1
  const barrageStart = relegationStart - props.barrageSlots
  for (let r = Math.max(1, barrageStart); r < relegationStart; r++) {
    markers.push({ rank: r, icon: '⚔️', tooltip: 'Barrage de maintien' })
  }
  for (let r = Math.max(1, relegationStart); r <= total; r++) {
    markers.push({ rank: r, icon: '⬇️', tooltip: 'Descend en division inférieure' })
  }
  return markers
})

function markersFor(rank) {
  return [...autoMarkers.value.filter(m => m.rank === rank), ...(props.rankMarkers?.filter(m => m.rank === rank) ?? [])]
}

const groupedRows = computed(() => {
  if (!props.rows.length) return []
  const withClasses = grp => ({ ...grp, classes: props.groupRowClasses?.(grp.name, grp.rows) ?? null })
  if (!props.rows.some(r => r.group)) return [withClasses({ name: null, rows: props.rows, baseIndex: 0 })]
  const map = new Map()
  for (const r of props.rows) {
    const key = r.group ?? '—'
    if (!map.has(key)) map.set(key, [])
    map.get(key).push(r)
  }
  return [...map.entries()].map(([name, rows]) => withClasses({ name, rows, baseIndex: 0 }))
})

function rowClass(row, index, grp) {
  if (grp?.classes) return grp.classes[index] ?? ''
  const rank = index + 1
  // La couleur de ligne est reservee aux faits sur le club (saison precedente) : champion,
  // coupe, promu, campagne europeenne. Les places qualificatives/barrage/relegation de la
  // saison EN COURS sont des icones (voir markersFor), pas des couleurs - independant du rang.
  const status = props.teamStatuses[row.teamId]
  if (status?.defendingChampion) return 'standing-blue'
  if (status?.previousCupWinner) return 'standing-purple'
  if (status?.promoted) return 'standing-red'
  if (status?.previousEuropeCompetition === 'LDC') return 'standing-green'
  if (status?.previousEuropeCompetition === 'EL') return 'standing-orange-dark'
  if (status?.previousEuropeCompetition === 'ECL') return 'standing-yellow'
  if (props.rankBands) {
    let threshold = 0
    for (const band of props.rankBands) {
      threshold += band.count
      if (rank <= threshold) return band.class
    }
    return props.rankBandsRest ?? 'standing-red'
  }
  return ''
}
</script>

<style scoped>
.standings-group h2 {
  margin: 0 0 10px;
}

.standings-group {
  margin-bottom: 8px;
}

.rank-marker {
  margin-left: 4px;
  cursor: help;
}

.col-rank {
  white-space: nowrap;
}

.rank-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 1.9em;
  height: 1.9em;
  border-radius: 8px;
  font-weight: 700;
  font-size: 0.9em;
  background: var(--surface-muted);
  color: var(--text-muted);
}

.team-name {
  font-weight: 600;
}

.diff-pos {
  color: var(--primary-dark);
  font-weight: 600;
}

.diff-neg {
  color: var(--danger);
  font-weight: 600;
}

.pts-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 2.3em;
  padding: 3px 8px;
  border-radius: 8px;
  font-weight: 800;
  color: white;
  background: linear-gradient(135deg, var(--pitch-800), var(--primary));
}
</style>

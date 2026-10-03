<template>
  <div class="history-chart">
    <div class="chart-area" @mouseleave="hoverIndex = null">
      <svg :viewBox="`0 0 ${WIDTH} ${HEIGHT}`" role="img" :aria-label="`Évolution du coefficient UEFA : ${title}`">
        <g v-for="tick in yTicks" :key="tick.value">
          <line class="grid-line" :x1="PAD.left" :x2="WIDTH - PAD.right" :y1="tick.y" :y2="tick.y" />
          <text class="axis-label" :x="PAD.left - 8" :y="tick.y" text-anchor="end" dominant-baseline="middle">{{ formatValue(tick.value) }}</text>
        </g>
        <text
          v-for="tick in xTicks"
          :key="tick.label + tick.x"
          class="axis-label"
          :x="tick.x"
          :y="HEIGHT - PAD.bottom + 18"
          text-anchor="middle"
        >{{ tick.label }}</text>

        <path class="series-line" :d="linePath" />
        <line
          v-if="hovered"
          class="crosshair"
          :x1="hovered.x"
          :x2="hovered.x"
          :y1="PAD.top"
          :y2="HEIGHT - PAD.bottom"
        />
        <circle
          v-for="(p, i) in plotted"
          :key="i"
          class="series-dot"
          :class="{ 'series-dot--active': hoverIndex === i }"
          :cx="p.x"
          :cy="p.y"
          :r="hoverIndex === i ? 6 : 4"
        />
        <rect
          class="hit-area"
          :x="PAD.left"
          :y="PAD.top"
          :width="WIDTH - PAD.left - PAD.right"
          :height="HEIGHT - PAD.top - PAD.bottom"
          @mousemove="onMove"
        />
      </svg>
      <div
        v-if="hovered"
        class="chart-tooltip"
        :style="{ left: `${(hovered.x / WIDTH) * 100}%`, top: `${(hovered.y / HEIGHT) * 100}%` }"
      >
        <div class="tooltip-date">{{ formatDate(hovered.point.date) }}</div>
        <div><strong>{{ formatValue(hovered.point.coefficient) }}</strong> coefficient</div>
        <div class="tooltip-muted">{{ formatValue(hovered.point.seasonPoints) }} pts saison 2027</div>
      </div>
    </div>

    <details class="chart-data">
      <summary>Voir les données</summary>
      <table>
        <thead>
          <tr>
            <th>Date</th>
            <th>Pts saison 2027</th>
            <th>Coefficient</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(p, i) in points" :key="i">
            <td>{{ formatDate(p.date) }}</td>
            <td>{{ formatValue(p.seasonPoints) }}</td>
            <td>{{ formatValue(p.coefficient) }}</td>
          </tr>
        </tbody>
      </table>
    </details>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  // [{ date: 'YYYY-MM-DD' | null, seasonPoints, coefficient }] : le 1er point (date null) est
  // le point de depart de la saison (cf. UefaRankingService.history).
  points: { type: Array, required: true },
  title: { type: String, default: '' }
})

const WIDTH = 720
const HEIGHT = 280
const PAD = { top: 16, right: 20, bottom: 32, left: 56 }
const DAY = 86_400_000
// Point de depart (sans date) place un peu avant le premier match date, pour garder un axe temporel.
const START_OFFSET_DAYS = 10

const hoverIndex = ref(null)

const times = computed(() => {
  const dated = props.points.filter(p => p.date).map(p => Date.parse(p.date))
  const first = dated.length ? Math.min(...dated) : Date.now()
  return props.points.map(p => (p.date ? Date.parse(p.date) : first - START_OFFSET_DAYS * DAY))
})

const xDomain = computed(() => {
  const min = Math.min(...times.value)
  const max = Math.max(...times.value)
  return max > min ? [min, max] : [min - DAY, max + DAY]
})

const yDomain = computed(() => {
  const values = props.points.map(p => Number(p.coefficient))
  const min = Math.min(...values)
  const max = Math.max(...values)
  const pad = max > min ? (max - min) * 0.1 : Math.max(Math.abs(max) * 0.05, 1)
  return [Math.max(0, min - pad), max + pad]
})

function xOf(t) {
  const [a, b] = xDomain.value
  return PAD.left + ((t - a) / (b - a)) * (WIDTH - PAD.left - PAD.right)
}

function yOf(v) {
  const [a, b] = yDomain.value
  return HEIGHT - PAD.bottom - ((v - a) / (b - a)) * (HEIGHT - PAD.top - PAD.bottom)
}

const plotted = computed(() =>
  props.points.map((point, i) => ({ point, x: xOf(times.value[i]), y: yOf(Number(point.coefficient)) })))

const linePath = computed(() =>
  plotted.value.map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' '))

const yTicks = computed(() => {
  const [a, b] = yDomain.value
  return [0, 1, 2, 3, 4].map(i => {
    const value = a + ((b - a) * i) / 4
    return { value, y: yOf(value) }
  })
})

// Un repere par mois couvert par la courbe.
const xTicks = computed(() => {
  const [a, b] = xDomain.value
  const ticks = []
  const d = new Date(a)
  d.setDate(1)
  d.setMonth(d.getMonth() + 1)
  while (d.getTime() <= b) {
    ticks.push({ x: xOf(d.getTime()), label: d.toLocaleDateString('fr-FR', { month: 'short' }) })
    d.setMonth(d.getMonth() + 1)
  }
  return ticks
})

const hovered = computed(() => (hoverIndex.value == null ? null : plotted.value[hoverIndex.value]))

// Point le plus proche du curseur sur l'axe horizontal (zone de survol = toute la largeur du trace).
function onMove(event) {
  const svg = event.currentTarget.ownerSVGElement
  const rect = svg.getBoundingClientRect()
  const x = ((event.clientX - rect.left) / rect.width) * WIDTH
  let best = 0
  plotted.value.forEach((p, i) => {
    if (Math.abs(p.x - x) < Math.abs(plotted.value[best].x - x)) best = i
  })
  hoverIndex.value = best
}

function formatValue(v) {
  return Number(v).toLocaleString('fr-FR', { maximumFractionDigits: 3 })
}

function formatDate(date) {
  if (!date) return 'Début de saison'
  return new Date(date).toLocaleDateString('fr-FR', { day: 'numeric', month: 'short', year: 'numeric' })
}
</script>

<style scoped>
.chart-area {
  position: relative;
}

svg {
  display: block;
  width: 100%;
  height: auto;
}

.grid-line {
  stroke: var(--border);
  stroke-width: 1;
}

.axis-label {
  fill: var(--text-muted);
  font-size: 11px;
}

.series-line {
  fill: none;
  stroke: var(--primary);
  stroke-width: 2;
  stroke-linejoin: round;
  stroke-linecap: round;
}

.series-dot {
  fill: var(--primary);
  stroke: var(--surface);
  stroke-width: 2;
}

.crosshair {
  stroke: var(--text-muted);
  stroke-width: 1;
  stroke-dasharray: 3 3;
}

.hit-area {
  fill: transparent;
  cursor: crosshair;
}

.chart-tooltip {
  position: absolute;
  transform: translate(-50%, calc(-100% - 12px));
  padding: 6px 10px;
  border-radius: 8px;
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  color: var(--text);
  font-size: 0.85em;
  white-space: nowrap;
  pointer-events: none;
}

.tooltip-date {
  font-weight: 700;
  margin-bottom: 2px;
}

.tooltip-muted {
  color: var(--text-muted);
}

.chart-data {
  margin-top: 12px;
}

.chart-data summary {
  cursor: pointer;
  color: var(--text-muted);
}
</style>

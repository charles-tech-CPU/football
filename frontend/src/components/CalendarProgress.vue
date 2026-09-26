<template>
  <span class="calendar-progress" :class="{ complete }" :title="title">
    <span class="bar-row">
      <span class="bar"><span :style="{ width: `${percent}%` }"></span></span>
      <span class="count">{{ progress.complete }}/{{ progress.total }} j.</span>
    </span>
    <span class="label">
      <template v-if="complete">Calendrier complet ✓</template>
      <template v-else>Incomplet dès J{{ progress.firstMissing }}</template>
    </span>
    <span v-if="progress.firstUntimedDate" class="untimed">
      Sans horaire dès le {{ formatShortDate(progress.firstUntimedDate) }}
    </span>
  </span>
</template>

<script setup>
import { computed } from 'vue'
import { formatShortDate } from '../utils/format'

// Avancement du calendrier d'un championnat : journees dont tous les matchs ont une date et
// un horaire, sur le nombre de journees attendues (cf. utils/competitionProgress.calendarProgress).
const props = defineProps({
  progress: { type: Object, required: true }
})

const complete = computed(() => props.progress.firstMissing == null)
const percent = computed(() => Math.round((props.progress.complete / props.progress.total) * 100))
const title = computed(() => complete.value
  ? `Les ${props.progress.total} journées ont leurs dates et horaires`
  : `${props.progress.complete} journées complètes sur ${props.progress.total}, dates ou horaires manquants à partir de la journée ${props.progress.firstMissing}`)
</script>

<style scoped>
.calendar-progress {
  display: flex;
  flex-direction: column;
  gap: 3px;
  margin-top: 2px;
}
.bar-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.bar {
  flex: 1;
  height: 6px;
  border-radius: 999px;
  background: var(--surface-muted);
  box-shadow: inset 0 0 0 1px var(--border);
  overflow: hidden;
}
.bar span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: var(--gold);
  transition: width 0.5s ease;
}
.complete .bar span {
  background: var(--primary);
}
.count {
  font-size: 0.74em;
  color: var(--text-muted);
  white-space: nowrap;
}
.label {
  font-size: 0.72em;
  font-weight: 600;
  color: #92600c;
}
.untimed {
  font-size: 0.72em;
  color: var(--text-muted);
}
.complete .label {
  color: var(--primary-dark);
}
</style>

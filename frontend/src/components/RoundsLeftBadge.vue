<template>
  <span v-if="count" class="rounds-left-badge" :title="title" :aria-label="title">{{ count }}</span>
</template>

<script setup>
// Pastille bleue (coin superieur gauche du parent, qui doit etre positionne) avec le nombre de
// journees restant a jouer ; les barrages / tours hors journees comptent pour une de plus.
// Pendant de LateBadge (matchs en retard, en rouge a droite). Cf. remainingProgress.
import { computed } from 'vue'

const props = defineProps({
  remaining: { type: Object, default: null }
})

const count = computed(() => (props.remaining ? props.remaining.rounds + (props.remaining.otherRounds ? 1 : 0) : 0))

const title = computed(() => {
  const { rounds, otherRounds } = props.remaining ?? {}
  const parts = []
  if (rounds) parts.push(`${rounds} journée${rounds > 1 ? 's' : ''} à jouer`)
  if (otherRounds) parts.push('barrages / tours finaux à jouer')
  return parts.join(' + ')
})
</script>

<style scoped>
.rounds-left-badge {
  position: absolute;
  top: -8px;
  left: -10px;
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  box-sizing: border-box;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: #1d4ed8;
  border: 2px solid var(--surface);
  color: white;
  font-size: 12px;
  font-weight: 800;
  line-height: 1;
  box-shadow: 0 2px 6px rgba(29, 78, 216, 0.35);
  z-index: 1;
}
</style>

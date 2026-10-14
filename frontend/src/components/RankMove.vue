<template>
  <span v-if="move" class="rank-move" :class="move.cls" :title="move.title">{{ move.label }}</span>
</template>

<script setup>
// Places gagnees / perdues depuis le debut de saison (cf. utils/ranking.rankMove).
import { computed } from 'vue'
import { rankMove } from '../utils/ranking'

const props = defineProps({
  rank: { type: Number, default: null },
  startRank: { type: Number, default: null }
})

const move = computed(() => rankMove(props.rank, props.startRank))
</script>

<style scoped>
.rank-move {
  font-size: 0.8em;
  font-weight: 700;
  white-space: nowrap;
}

.move-up {
  color: var(--primary);
}

.move-down {
  color: #c0392b;
}

.move-same {
  color: var(--text-muted);
}
</style>

<template>
  <span
    v-if="count"
    class="late-badge"
    :title="`${count} match${count > 1 ? 's' : ''} en retard`"
    :aria-label="`${count} match${count > 1 ? 's' : ''} en retard`"
  >{{ count }}</span>
</template>

<script setup>
// Pastille rouge (coin superieur droit du parent, qui doit etre positionne) avec le
// nombre de matchs en retard ; rien n'est affiche s'il n'y en a pas.
defineProps({
  count: { type: [Number, Boolean, null], default: 0 }
})
</script>

<style scoped>
.late-badge {
  position: absolute;
  top: -8px;
  right: -10px;
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  box-sizing: border-box;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: var(--danger);
  border: 2px solid var(--surface);
  color: white;
  font-size: 12px;
  font-weight: 800;
  line-height: 1;
  font-family: inherit;
  box-shadow: 0 2px 6px rgba(213, 68, 58, 0.4);
  z-index: 1;
}
</style>

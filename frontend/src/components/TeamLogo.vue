<template>
  <span class="team-logo" :style="style" :title="name">{{ initials }}</span>
</template>

<script setup>
import { computed } from 'vue'
import { teamCountryStyle } from '../utils/teamColors'

const props = defineProps({
  name: { type: String, required: true },
  country: { type: String, default: null }
})

const initials = computed(() => {
  const words = props.name.trim().split(/\s+/).filter(Boolean)
  if (words.length === 1) return words[0].slice(0, 3).toUpperCase()
  return words.slice(0, 2).map(w => w[0]).join('').toUpperCase()
})

const style = computed(() => {
  const { bg, fg } = teamCountryStyle(props.country ?? props.name)
  return { backgroundColor: bg, color: fg }
})
</script>

<style scoped>
.team-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.8em;
  height: 1.8em;
  border-radius: 50%;
  font-size: 0.7em;
  font-weight: 800;
  letter-spacing: -0.02em;
  flex-shrink: 0;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.08);
}
</style>

<template>
  <span v-if="code" class="flag-icon" :class="`fi fi-${code}`" :title="label"></span>
  <span v-else class="flag-icon flag-icon--placeholder" :title="label">🏳️</span>
</template>

<script setup>
import { computed } from 'vue'
import { flagCode, canonicalCountry } from '../utils/countryFlags'

const props = defineProps({
  country: { type: String, default: null }
})

const code = computed(() => (props.country ? flagCode(props.country) : null))
const label = computed(() => canonicalCountry(props.country) ?? props.country)
</script>

<style scoped>
.flag-icon {
  display: inline-block;
  width: 1.5em;
  height: 1.125em;
  border-radius: 3px;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.08);
  background-size: cover;
  background-position: center;
  vertical-align: middle;
  flex-shrink: 0;
}

.flag-icon--placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 1em;
  box-shadow: none;
}
</style>

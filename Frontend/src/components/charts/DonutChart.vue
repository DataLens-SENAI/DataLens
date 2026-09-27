<template>
  <div class="relative">
    <Doughnut :data="dadosGrafico" :options="opcoes" />

    <!-- Conteúdo central (ex: total) -->
    <div class="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
      <slot />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Doughnut } from 'vue-chartjs'

const props = defineProps({
  rotulos: { type: Array, required: true },
  valores: { type: Array, required: true },
  cores: { type: Array, required: true }
})

const dadosGrafico = computed(() => ({
  labels: props.rotulos,
  datasets: [{
    data: props.valores,
    backgroundColor: props.cores,
    borderWidth: 0,
    hoverOffset: 4
  }]
}))

const opcoes = {
  cutout: '80%',
  plugins: {
    tooltip: { callbacks: { label: (item) => ` ${item.label}: ${item.formattedValue}` } }
  }
}
</script>

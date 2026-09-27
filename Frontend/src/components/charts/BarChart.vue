<template>
  <div class="relative">
    <Bar :data="dadosGrafico" :options="opcoes" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Bar } from 'vue-chartjs'

const props = defineProps({
  rotulos: { type: Array, required: true },
  valores: { type: Array, required: true },
  cor: { type: String, default: '#1d4ed8' },
  nomeSerie: { type: String, default: '' }
})

const dadosGrafico = computed(() => ({
  labels: props.rotulos,
  datasets: [{
    label: props.nomeSerie,
    data: props.valores,
    backgroundColor: props.cor,
    borderRadius: 6,
    maxBarThickness: 36
  }]
}))

const opcoes = {
  scales: {
    x: { grid: { display: false }, border: { color: '#e2e8f0' } },
    y: { beginAtZero: true, grid: { color: '#f1f5f9' }, border: { display: false }, ticks: { maxTicksLimit: 5 } }
  }
}
</script>

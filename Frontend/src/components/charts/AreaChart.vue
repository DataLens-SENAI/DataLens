<template>
  <div class="relative">
    <Line :data="dadosGrafico" :options="opcoes" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Line } from 'vue-chartjs'

const props = defineProps({
  rotulos: { type: Array, required: true },
  valores: { type: Array, required: true },
  cor: { type: String, default: '#1d4ed8' },
  minimo: { type: Number, default: undefined }
})

const dadosGrafico = computed(() => ({
  labels: props.rotulos,
  datasets: [{
    data: props.valores,
    borderColor: props.cor,
    borderWidth: 3,
    pointBackgroundColor: '#fff',
    pointBorderColor: props.cor,
    pointRadius: 3,
    tension: 0.3,
    fill: true,
    backgroundColor: (contexto) => {
      const { ctx, chartArea } = contexto.chart
      if (!chartArea) return 'transparent'
      const gradiente = ctx.createLinearGradient(0, chartArea.top, 0, chartArea.bottom)
      gradiente.addColorStop(0, `${props.cor}40`)
      gradiente.addColorStop(1, `${props.cor}00`)
      return gradiente
    }
  }]
}))

const opcoes = computed(() => ({
  scales: {
    x: { grid: { display: false }, border: { display: false } },
    y: { min: props.minimo, grid: { color: '#f1f5f9' }, border: { display: false }, ticks: { maxTicksLimit: 4 } }
  }
}))
</script>

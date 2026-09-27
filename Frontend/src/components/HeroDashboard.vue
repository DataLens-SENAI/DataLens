<template>
  <!-- Prévia do dashboard de qualidade exibida na Home pública -->
  <div class="w-full max-w-5xl rounded-2xl bg-slate-100 p-3 md:p-5 shadow-[0_0_60px_-10px_rgba(125,211,252,0.45)] ring-1 ring-sky-200/60 text-slate-900">
    <div class="grid grid-cols-1 md:grid-cols-3 gap-3 md:gap-4">
      <!-- Score de Qualidade -->
      <div class="bg-white rounded-xl border border-slate-200 p-5 flex flex-col">
        <div class="flex items-center justify-between">
          <h3 class="font-bold">Score de Qualidade</h3>
          <Info class="w-4 h-4 text-slate-400" />
        </div>

        <div class="relative flex-1 flex items-center justify-center py-8">
          <svg viewBox="0 0 120 120" class="w-40 h-40 -rotate-90">
            <circle cx="60" cy="60" r="50" fill="none" stroke="#e2e8f0" stroke-width="10" />
            <circle
              cx="60" cy="60" r="50" fill="none" stroke="#4ade80" stroke-width="10" stroke-linecap="round"
              :stroke-dasharray="circunferencia"
              :stroke-dashoffset="circunferencia * (1 - score / 100)"
            />
          </svg>
          <div class="absolute flex flex-col items-center">
            <span class="text-3xl font-bold">{{ score }}%</span>
            <span class="flex items-center gap-1 text-xs font-medium text-emerald-600">
              +2.4% <TrendingUp class="w-3 h-3" />
            </span>
          </div>
        </div>

        <div class="border-t border-slate-200 pt-4 flex justify-center">
          <span class="flex items-center gap-1.5 rounded-full bg-emerald-50 px-3 py-1 text-xs font-medium text-emerald-700">
            <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
            Status: Excelente
          </span>
        </div>
      </div>

      <div class="md:col-span-2 flex flex-col gap-3 md:gap-4">
        <!-- Mini indicadores -->
        <div class="grid grid-cols-1 sm:grid-cols-3 gap-3 md:gap-4">
          <div v-for="card in cards" :key="card.titulo" class="bg-white rounded-xl border border-slate-200 p-4 flex flex-col gap-2">
            <div class="flex items-start justify-between">
              <span :class="['w-8 h-8 rounded-md flex items-center justify-center', card.corIcone]">
                <component :is="card.icone" class="w-4 h-4" />
              </span>
              <span :class="['text-[10px] font-semibold px-1.5 py-0.5 rounded', card.corTag]">{{ card.tag }}</span>
            </div>
            <p class="text-[10px] font-medium uppercase tracking-wide text-slate-500">{{ card.titulo }}</p>
            <p class="text-xl font-bold">{{ card.valor }}</p>
            <p class="text-xs text-slate-500">{{ card.descricao }}</p>
          </div>
        </div>

        <!-- Evolução da Qualidade -->
        <div class="bg-white rounded-xl border border-slate-200 p-4 md:p-5 flex-1 flex flex-col gap-3">
          <div class="flex items-center justify-between">
            <h3 class="font-bold">Evolução da Qualidade</h3>
            <span class="rounded-md border border-slate-200 bg-slate-50 px-2 py-1 text-xs text-slate-600">Últimos 6 meses</span>
          </div>
          <AreaChart
            class="h-44"
            :rotulos="evolucaoQualidade.meses"
            :valores="evolucaoQualidade.valores"
            :minimo="70"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Info, TrendingUp, ListChecks, Copy, TriangleAlert } from 'lucide-vue-next'
import AreaChart from './charts/AreaChart.vue'
import { evolucaoQualidade } from '../data/mock'

const score = 92
const circunferencia = 2 * Math.PI * 50

const cards = [
  { titulo: 'Registros Completos', valor: '1.2M', descricao: '89% da base total', tag: '+14k', icone: ListChecks, corIcone: 'bg-blue-50 text-blue-700', corTag: 'bg-emerald-50 text-emerald-700' },
  { titulo: 'Duplicidades Removidas', valor: '14.5k', descricao: 'Últimos 30 dias', tag: '-230', icone: Copy, corIcone: 'bg-slate-100 text-slate-600', corTag: 'bg-slate-100 text-slate-600' },
  { titulo: 'Dados Inconsistentes', valor: '3.2k', descricao: 'Ação necessária', tag: '-12%', icone: TriangleAlert, corIcone: 'bg-rose-50 text-rose-600', corTag: 'bg-rose-50 text-rose-600' }
]
</script>

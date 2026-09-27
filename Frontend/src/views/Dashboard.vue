<template>
  <div class="flex flex-col gap-8">
    <!-- Cabeçalho -->
    <div class="flex flex-col md:flex-row md:items-end md:justify-between gap-4">
      <div>
        <h1 class="font-instrument text-3xl md:text-4xl font-bold">Visão Geral</h1>
        <p class="text-slate-500 mt-1">Panorama consolidado da carteira e performance comercial.</p>
      </div>

      <div class="flex items-center gap-3">
        <label class="relative">
          <Calendar class="w-4 h-4 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
          <select
            v-model="periodo"
            class="appearance-none rounded-md border border-slate-300 bg-white py-2 pl-9 pr-9 text-sm font-medium outline-none focus:border-blue-500"
          >
            <option v-for="opcao in periodos" :key="opcao">{{ opcao }}</option>
          </select>
          <ChevronDown class="w-4 h-4 text-slate-500 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
        </label>

        <button
          class="flex items-center gap-2 rounded-md bg-slate-950 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800 duration-200"
          @click="exportar"
        >
          <Download class="w-4 h-4" />
          Exportar Relatório
        </button>
      </div>
    </div>

    <!-- KPIs -->
    <section class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-4">
      <CardIndicador
        v-for="kpi in kpisDashboard"
        :key="kpi.titulo"
        :titulo="kpi.titulo"
        :valor="kpi.valor"
        :variacao="kpi.variacao"
        :tipo-variacao="kpi.tipoVariacao"
        :descricao="kpi.descricao"
        :icone="icones[kpi.icone]"
      />
    </section>

    <!-- Insights Estratégicos -->
    <section class="flex flex-col gap-4">
      <h2 class="flex items-center gap-2 text-xl font-bold">
        <Sparkles class="w-5 h-5 text-blue-700" />
        Insights Estratégicos
      </h2>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <article
          v-for="insight in insights"
          :key="insight.tag"
          :class="['bg-white rounded-lg border border-slate-200 border-l-4 p-5 flex gap-3 shadow-sm', estiloInsight[insight.tipo].borda]"
        >
          <component :is="estiloInsight[insight.tipo].icone" :class="['w-5 h-5 shrink-0 mt-0.5', estiloInsight[insight.tipo].corIcone]" />
          <div class="flex flex-col items-start gap-3">
            <p class="text-sm font-semibold text-slate-800">{{ insight.texto }}</p>
            <span :class="['text-xs font-medium px-2 py-1 rounded', estiloInsight[insight.tipo].tag]">{{ insight.tag }}</span>
          </div>
        </article>
      </div>
    </section>

    <!-- Gráficos -->
    <section class="grid grid-cols-1 lg:grid-cols-5 gap-6">
      <div class="lg:col-span-2 bg-white rounded-xl border border-slate-200 shadow-sm p-6 flex flex-col gap-6">
        <h2 class="text-lg font-bold">Distribuição por Nível</h2>

        <DonutChart
          class="h-56"
          :rotulos="distribuicaoNivel.map((item) => item.nivel)"
          :valores="distribuicaoNivel.map((item) => item.total)"
          :cores="distribuicaoNivel.map((item) => item.cor)"
        >
          <span class="text-3xl font-bold">{{ formatarNumero(totalClientes) }}</span>
          <span class="text-sm text-slate-500">Total</span>
        </DonutChart>

        <ul class="flex flex-wrap justify-center gap-x-6 gap-y-2 text-sm text-slate-600">
          <li v-for="item in distribuicaoNivel" :key="item.nivel" class="flex items-center gap-2">
            <span class="w-2.5 h-2.5 rounded-full" :style="{ backgroundColor: item.cor }"></span>
            {{ item.nivel }} ({{ Math.round((item.total / totalClientes) * 100) }}%)
          </li>
        </ul>
      </div>

      <div class="lg:col-span-3 bg-white rounded-xl border border-slate-200 shadow-sm p-6 flex flex-col gap-6">
        <div class="flex items-center justify-between">
          <h2 class="text-lg font-bold">Clientes por Segmento</h2>
          <router-link to="/segmentos" class="flex items-center gap-1 text-sm font-semibold text-blue-700 hover:underline">
            Ver todos <ArrowRight class="w-4 h-4" />
          </router-link>
        </div>

        <BarChart
          class="h-64"
          nome-serie="Clientes"
          :rotulos="clientesPorSegmento.map((item) => item.segmento)"
          :valores="clientesPorSegmento.map((item) => item.total)"
        />
      </div>
    </section>

    <!-- Consultores e Serviços -->
    <section class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="lg:col-span-2 bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <h2 class="text-lg font-bold p-6">Performance por Consultor</h2>

        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="bg-slate-50 text-slate-500 text-left">
              <tr>
                <th class="font-medium px-6 py-3">Consultor</th>
                <th class="font-medium px-6 py-3">Clientes Ativos</th>
                <th class="font-medium px-6 py-3">Ticket Médio</th>
                <th class="font-medium px-6 py-3 text-right">Status</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="consultor in consultores" :key="consultor.nome">
                <td class="px-6 py-4">
                  <div class="flex items-center gap-3 font-medium whitespace-nowrap">
                    <span class="w-8 h-8 rounded-full bg-slate-100 text-slate-600 text-xs font-semibold flex items-center justify-center">
                      {{ iniciais(consultor.nome) }}
                    </span>
                    {{ consultor.nome }}
                  </div>
                </td>
                <td class="px-6 py-4">{{ consultor.clientesAtivos }}</td>
                <td class="px-6 py-4 whitespace-nowrap">{{ formatarMoeda(consultor.ticketMedio) }}</td>
                <td class="px-6 py-4 text-right">
                  <span :class="['inline-flex items-center gap-1 text-xs font-medium px-2.5 py-1 rounded-full whitespace-nowrap', statusConsultor[consultor.status].classe]">
                    <component :is="statusConsultor[consultor.status].icone" v-if="statusConsultor[consultor.status].icone" class="w-3 h-3" />
                    {{ statusConsultor[consultor.status].texto }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6 flex flex-col gap-5">
        <h2 class="text-lg font-bold">Top Serviços</h2>

        <ol class="flex flex-col gap-5">
          <li v-for="(servico, indice) in topServicos" :key="servico.nome" class="flex flex-col gap-2">
            <div class="flex items-center gap-3">
              <span class="w-6 h-6 rounded bg-blue-50 text-blue-700 text-xs font-semibold flex items-center justify-center">{{ indice + 1 }}</span>
              <span class="text-sm font-medium flex-1">{{ servico.nome }}</span>
              <span class="text-right leading-tight">
                <span class="block text-sm font-bold">{{ formatarNumero(servico.contratos) }}</span>
                <span class="text-xs text-slate-500">contratos</span>
              </span>
            </div>
            <div class="h-1.5 rounded-full bg-slate-100">
              <div class="h-full rounded-full bg-blue-700" :style="{ width: `${(servico.contratos / maiorServico) * 100}%` }"></div>
            </div>
          </li>
        </ol>

        <router-link to="/servicos" class="mt-auto text-center text-sm font-semibold text-blue-700 hover:underline">
          Ver Catálogo Completo
        </router-link>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import {
  Calendar, ChevronDown, Download, Sparkles, ArrowRight, Lightbulb, TriangleAlert, TrendingUp,
  ArrowUp, ArrowDown, Users, Star, DollarSign, Layers, Globe
} from 'lucide-vue-next'
import CardIndicador from '../components/CardIndicador.vue'
import DonutChart from '../components/charts/DonutChart.vue'
import BarChart from '../components/charts/BarChart.vue'
import {
  kpisDashboard, insights, distribuicaoNivel, clientesPorSegmento, consultores, topServicos
} from '../data/mock'
import { formatarMoeda, formatarNumero, iniciais } from '../utils/formatar'
import { exportarCSV } from '../utils/exportarCSV'

const periodos = ['Este Mês', 'Último Trimestre', 'Este Ano']
const periodo = ref(periodos[0])

const icones = { Users, Star, DollarSign, Layers, Globe }

const totalClientes = distribuicaoNivel.reduce((soma, item) => soma + item.total, 0)
const maiorServico = Math.max(...topServicos.map((servico) => servico.contratos))

const estiloInsight = {
  oportunidade: { icone: Lightbulb, borda: 'border-l-blue-700', corIcone: 'text-blue-700', tag: 'bg-blue-50 text-blue-700' },
  risco: { icone: TriangleAlert, borda: 'border-l-rose-500', corIcone: 'text-rose-500', tag: 'bg-rose-50 text-rose-700' },
  performance: { icone: TrendingUp, borda: 'border-l-emerald-500', corIcone: 'text-emerald-600', tag: 'bg-emerald-50 text-emerald-700' }
}

const statusConsultor = {
  acima: { texto: 'Acima da meta', icone: ArrowUp, classe: 'bg-emerald-50 text-emerald-700' },
  'na-meta': { texto: 'Na meta', icone: null, classe: 'bg-slate-100 text-slate-600' },
  atencao: { texto: 'Atenção', icone: ArrowDown, classe: 'bg-rose-50 text-rose-700' }
}

function exportar() {
  exportarCSV('performance-consultores.csv', consultores.map((consultor) => ({
    Consultor: consultor.nome,
    'Clientes Ativos': consultor.clientesAtivos,
    'Ticket Médio': consultor.ticketMedio,
    Status: statusConsultor[consultor.status].texto
  })))
}
</script>

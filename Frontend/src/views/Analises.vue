<template>
  <div class="flex flex-col gap-8">
    <!-- Cabeçalho -->
    <div class="flex flex-col md:flex-row md:items-end md:justify-between gap-4">
      <div>
        <h1 class="font-instrument text-3xl md:text-4xl font-bold">Análises</h1>
        <p class="text-slate-500 mt-1">Relatório detalhado da carteira de clientes por segmento, nível e consultor.</p>
      </div>

      <button
        class="self-start md:self-auto flex items-center gap-2 rounded-md bg-slate-950 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800 disabled:opacity-50 duration-200"
        :disabled="!clientesFiltrados.length"
        @click="exportar"
      >
        <Download class="w-4 h-4" />
        Exportar Relatório
      </button>
    </div>

    <!-- Resumo do filtro atual -->
    <section class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
      <CardIndicador titulo="Clientes" :valor="formatarNumero(clientesFiltrados.length)" :icone="Users" />
      <CardIndicador titulo="Faturamento Total" :valor="formatarMoeda(faturamentoTotal)" :icone="DollarSign" />
      <CardIndicador titulo="Ticket Médio" :valor="formatarMoeda(ticketMedio)" :icone="Wallet" />
      <CardIndicador
        titulo="Em Risco"
        :valor="formatarNumero(totalEmRisco)"
        :descricao="clientesFiltrados.length ? `${Math.round((totalEmRisco / clientesFiltrados.length) * 100)}% da seleção` : ''"
        :icone="TriangleAlert"
      />
    </section>

    <!-- Filtros -->
    <section class="bg-white rounded-xl border border-slate-200 shadow-sm p-4 flex flex-col lg:flex-row gap-3">
      <label class="relative flex-1">
        <Search class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
        <input
          v-model.trim="filtros.busca"
          type="search"
          placeholder="Buscar cliente..."
          class="w-full rounded-md border border-slate-300 py-2 pl-9 pr-3 text-sm outline-none focus:border-blue-500"
        >
      </label>

      <div class="grid grid-cols-2 sm:grid-cols-4 lg:flex gap-3">
        <select v-model="filtros.segmento" :class="classeSelect">
          <option value="">Todos os segmentos</option>
          <option v-for="segmento in opcoesSegmento" :key="segmento">{{ segmento }}</option>
        </select>
        <select v-model="filtros.nivel" :class="classeSelect">
          <option value="">Todos os níveis</option>
          <option v-for="nivel in ['A', 'B', 'C']" :key="nivel" :value="nivel">Nível {{ nivel }}</option>
        </select>
        <select v-model="filtros.consultor" :class="classeSelect">
          <option value="">Todos os consultores</option>
          <option v-for="consultor in consultores" :key="consultor.nome">{{ consultor.nome }}</option>
        </select>
        <select v-model="filtros.status" :class="classeSelect">
          <option value="">Todos os status</option>
          <option v-for="(info, chave) in statusCliente" :key="chave" :value="chave">{{ info.texto }}</option>
        </select>
      </div>
    </section>

    <!-- Resultado -->
    <section class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="flex items-center justify-between gap-4 p-4 md:px-6 border-b border-slate-200">
        <p class="text-sm text-slate-500">
          <span class="font-semibold text-slate-900">{{ clientesFiltrados.length }}</span> clientes encontrados
          <button v-if="temFiltro" class="ml-2 font-semibold text-blue-700 hover:underline" @click="limparFiltros">Limpar filtros</button>
        </p>

        <div class="flex rounded-md border border-slate-300 p-0.5">
          <button
            v-for="modo in modos"
            :key="modo.valor"
            :class="['flex items-center gap-1.5 rounded px-3 py-1.5 text-xs font-semibold duration-200', visualizacao === modo.valor ? 'bg-slate-900 text-white' : 'text-slate-600 hover:text-slate-900']"
            @click="visualizacao = modo.valor"
          >
            <component :is="modo.icone" class="w-4 h-4" />
            <span class="hidden sm:inline">{{ modo.texto }}</span>
          </button>
        </div>
      </div>

      <p v-if="!clientesFiltrados.length" class="p-12 text-center text-slate-500">
        Nenhum cliente encontrado com os filtros selecionados.
      </p>

      <!-- Tabela -->
      <div v-else-if="visualizacao === 'tabela'" class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-slate-50 text-slate-500 text-left">
            <tr>
              <th v-for="coluna in colunas" :key="coluna.campo" :class="['font-medium px-6 py-3 whitespace-nowrap', coluna.direita ? 'text-right' : '']">
                <button class="inline-flex items-center gap-1 hover:text-slate-900" @click="ordenarPor(coluna.campo)">
                  {{ coluna.texto }}
                  <ArrowUp v-if="ordenacao.campo === coluna.campo && ordenacao.crescente" class="w-3.5 h-3.5" />
                  <ArrowDown v-else-if="ordenacao.campo === coluna.campo" class="w-3.5 h-3.5" />
                  <ArrowUpDown v-else class="w-3.5 h-3.5 opacity-40" />
                </button>
              </th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="cliente in clientesPagina" :key="cliente.id" class="hover:bg-slate-50">
              <td class="px-6 py-4 font-medium whitespace-nowrap">{{ cliente.nome }}</td>
              <td class="px-6 py-4">{{ cliente.segmento }}</td>
              <td class="px-6 py-4">
                <span :class="['text-xs font-semibold px-2 py-0.5 rounded', classeNivel[cliente.nivel]]">{{ cliente.nivel }}</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">{{ cliente.consultor }}</td>
              <td class="px-6 py-4 text-right">{{ cliente.servicos }}</td>
              <td class="px-6 py-4 text-right whitespace-nowrap">{{ formatarMoeda(cliente.faturamento) }}</td>
              <td class="px-6 py-4 text-right">
                <span :class="['text-xs font-medium px-2.5 py-1 rounded-full whitespace-nowrap', statusCliente[cliente.status].classe]">
                  {{ statusCliente[cliente.status].texto }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Cards -->
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 p-4 md:p-6">
        <article v-for="cliente in clientesPagina" :key="cliente.id" class="rounded-lg border border-slate-200 p-4 flex flex-col gap-3">
          <div class="flex items-start justify-between gap-2">
            <p class="font-semibold">{{ cliente.nome }}</p>
            <span :class="['text-xs font-semibold px-2 py-0.5 rounded', classeNivel[cliente.nivel]]">{{ cliente.nivel }}</span>
          </div>
          <p class="text-2xl font-bold">{{ formatarMoeda(cliente.faturamento) }}</p>
          <dl class="grid grid-cols-2 gap-2 text-xs">
            <div>
              <dt class="text-slate-500">Segmento</dt>
              <dd class="font-medium">{{ cliente.segmento }}</dd>
            </div>
            <div>
              <dt class="text-slate-500">Serviços</dt>
              <dd class="font-medium">{{ cliente.servicos }}</dd>
            </div>
            <div class="col-span-2">
              <dt class="text-slate-500">Consultor</dt>
              <dd class="font-medium">{{ cliente.consultor }}</dd>
            </div>
          </dl>
          <span :class="['self-start text-xs font-medium px-2.5 py-1 rounded-full', statusCliente[cliente.status].classe]">
            {{ statusCliente[cliente.status].texto }}
          </span>
        </article>
      </div>

      <!-- Paginação -->
      <div v-if="totalPaginas > 1" class="flex items-center justify-between gap-4 p-4 md:px-6 border-t border-slate-200 text-sm">
        <p class="text-slate-500">Página {{ pagina }} de {{ totalPaginas }}</p>
        <div class="flex gap-2">
          <button :class="classeBotaoPagina" :disabled="pagina === 1" @click="pagina--">Anterior</button>
          <button :class="classeBotaoPagina" :disabled="pagina === totalPaginas" @click="pagina++">Próxima</button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import {
  Download, Search, Users, DollarSign, Wallet, TriangleAlert, Table, LayoutGrid, ArrowUp, ArrowDown, ArrowUpDown
} from 'lucide-vue-next'
import CardIndicador from '../components/CardIndicador.vue'
import { clientes, consultores, opcoesSegmento } from '../data/mock'
import { formatarMoeda, formatarNumero } from '../utils/formatar'
import { exportarCSV } from '../utils/exportarCSV'

const ITENS_POR_PAGINA = 8

const classeSelect = 'rounded-md border border-slate-300 bg-white px-3 py-2 text-sm outline-none focus:border-blue-500'
const classeBotaoPagina = 'rounded-md border border-slate-300 px-3 py-1.5 font-medium hover:bg-slate-50 disabled:opacity-40 disabled:hover:bg-transparent'

const statusCliente = {
  ativo: { texto: 'Ativo', classe: 'bg-emerald-50 text-emerald-700' },
  oportunidade: { texto: 'Oportunidade', classe: 'bg-blue-50 text-blue-700' },
  risco: { texto: 'Em risco', classe: 'bg-rose-50 text-rose-700' }
}

const classeNivel = {
  A: 'bg-blue-700 text-white',
  B: 'bg-emerald-100 text-emerald-800',
  C: 'bg-slate-200 text-slate-700'
}

const colunas = [
  { campo: 'nome', texto: 'Cliente' },
  { campo: 'segmento', texto: 'Segmento' },
  { campo: 'nivel', texto: 'Nível' },
  { campo: 'consultor', texto: 'Consultor' },
  { campo: 'servicos', texto: 'Serviços', direita: true },
  { campo: 'faturamento', texto: 'Faturamento', direita: true },
  { campo: 'status', texto: 'Status', direita: true }
]

const modos = [
  { valor: 'tabela', texto: 'Tabela', icone: Table },
  { valor: 'cards', texto: 'Cards', icone: LayoutGrid }
]

const filtrosIniciais = { busca: '', segmento: '', nivel: '', consultor: '', status: '' }
const filtros = reactive({ ...filtrosIniciais })
const visualizacao = ref('tabela')
const ordenacao = reactive({ campo: 'faturamento', crescente: false })
const pagina = ref(1)

const temFiltro = computed(() => Object.values(filtros).some(Boolean))

const clientesFiltrados = computed(() => {
  const busca = filtros.busca.toLowerCase()

  const filtrados = clientes.filter((cliente) =>
    (!busca || cliente.nome.toLowerCase().includes(busca)) &&
    (!filtros.segmento || cliente.segmento === filtros.segmento) &&
    (!filtros.nivel || cliente.nivel === filtros.nivel) &&
    (!filtros.consultor || cliente.consultor === filtros.consultor) &&
    (!filtros.status || cliente.status === filtros.status)
  )

  const direcao = ordenacao.crescente ? 1 : -1
  return filtrados.sort((a, b) => {
    const valorA = a[ordenacao.campo]
    const valorB = b[ordenacao.campo]
    const comparacao = typeof valorA === 'number' ? valorA - valorB : String(valorA).localeCompare(String(valorB), 'pt-BR')
    return comparacao * direcao
  })
})

const faturamentoTotal = computed(() => clientesFiltrados.value.reduce((soma, cliente) => soma + cliente.faturamento, 0))
const ticketMedio = computed(() => clientesFiltrados.value.length ? faturamentoTotal.value / clientesFiltrados.value.length : 0)
const totalEmRisco = computed(() => clientesFiltrados.value.filter((cliente) => cliente.status === 'risco').length)

const totalPaginas = computed(() => Math.max(1, Math.ceil(clientesFiltrados.value.length / ITENS_POR_PAGINA)))
const clientesPagina = computed(() => {
  const inicio = (pagina.value - 1) * ITENS_POR_PAGINA
  return clientesFiltrados.value.slice(inicio, inicio + ITENS_POR_PAGINA)
})

// Volta para a primeira página sempre que o resultado mudar
watch(filtros, () => {
  pagina.value = 1
})

function ordenarPor(campo) {
  if (ordenacao.campo === campo) {
    ordenacao.crescente = !ordenacao.crescente
  } else {
    ordenacao.campo = campo
    ordenacao.crescente = true
  }
}

function limparFiltros() {
  Object.assign(filtros, filtrosIniciais)
}

function exportar() {
  exportarCSV('relatorio-clientes.csv', clientesFiltrados.value.map((cliente) => ({
    Cliente: cliente.nome,
    Segmento: cliente.segmento,
    Nível: cliente.nivel,
    Consultor: cliente.consultor,
    Serviços: cliente.servicos,
    Faturamento: cliente.faturamento,
    Status: statusCliente[cliente.status].texto
  })))
}
</script>

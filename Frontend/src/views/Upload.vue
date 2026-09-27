<template>
  <div class="max-w-4xl mx-auto flex flex-col gap-8">
    <h1 class="font-instrument text-3xl md:text-4xl font-bold">Importar Dados</h1>

    <!-- Etapas da importação -->
    <ol class="flex items-start">
      <li v-for="(etapa, indice) in etapas" :key="etapa" class="flex-1 flex flex-col items-center gap-2 relative">
        <!-- Linha entre as etapas -->
        <span
          v-if="indice > 0"
          :class="['absolute top-4 right-1/2 w-full h-0.5 z-0', indice <= etapaAtual ? 'bg-blue-700' : 'bg-slate-200']"
        ></span>

        <span
          :class="[
            'relative z-10 w-8 h-8 rounded-full flex items-center justify-center text-sm font-semibold',
            indice < etapaAtual ? 'bg-blue-700 text-white' : '',
            indice === etapaAtual ? 'bg-blue-700 text-white ring-4 ring-blue-100' : '',
            indice > etapaAtual ? 'bg-slate-200 text-slate-600' : ''
          ]"
        >
          <Check v-if="indice < etapaAtual" class="w-4 h-4" />
          <template v-else>{{ indice + 1 }}</template>
        </span>
        <span :class="['text-xs text-center', indice === etapaAtual ? 'font-semibold text-blue-700' : 'text-slate-600']">
          {{ etapa }}
        </span>
      </li>
    </ol>

    <!-- Seleção do arquivo -->
    <section class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-200">
        <h2 class="text-xl font-bold">Selecione o arquivo</h2>
        <p class="text-sm text-slate-500 mt-1">Faça o upload da sua planilha para iniciar o processo de importação para a base do DataLens.</p>
      </div>

      <div class="p-6">
        <!-- Área de arrastar e soltar -->
        <div
          v-if="!arquivo"
          :class="[
            'rounded-xl border-2 border-dashed px-6 py-14 flex flex-col items-center text-center gap-2 cursor-pointer duration-200',
            arrastando ? 'border-blue-500 bg-blue-50' : 'border-slate-300 bg-slate-50 hover:border-slate-400'
          ]"
          role="button"
          tabindex="0"
          @click="abrirSeletor"
          @keydown.enter.prevent="abrirSeletor"
          @keydown.space.prevent="abrirSeletor"
          @dragover.prevent="arrastando = true"
          @dragleave.prevent="arrastando = false"
          @drop.prevent="soltarArquivo"
        >
          <span class="w-16 h-16 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center mb-2">
            <FileText class="w-7 h-7" />
          </span>
          <p class="text-lg font-bold">Arraste sua planilha aqui</p>
          <p class="text-slate-500">ou clique para selecionar do seu computador</p>
          <span class="mt-4 rounded-md bg-slate-950 px-5 py-2 text-sm font-semibold text-white">Procurar Arquivo</span>
        </div>

        <!-- Arquivo selecionado + status -->
        <div v-else class="rounded-xl border border-slate-200 p-5 flex flex-col gap-4">
          <div class="flex items-center gap-4">
            <span class="w-11 h-11 shrink-0 rounded-lg bg-emerald-50 text-emerald-700 flex items-center justify-center">
              <FileSpreadsheet class="w-5 h-5" />
            </span>
            <div class="flex-1 min-w-0">
              <p class="font-semibold truncate">{{ arquivo.name }}</p>
              <p class="text-xs text-slate-500">{{ formatarTamanho(arquivo.size) }}</p>
            </div>
            <button
              class="p-2 text-slate-400 hover:text-rose-600 duration-200"
              aria-label="Remover arquivo"
              @click="removerArquivo"
            >
              <Trash2 class="w-5 h-5" />
            </button>
          </div>

          <div class="h-2 rounded-full bg-slate-100 overflow-hidden">
            <div
              :class="['h-full rounded-full transition-all duration-200', status === 'concluido' ? 'bg-emerald-500' : 'bg-blue-700']"
              :style="{ width: `${progresso}%` }"
            ></div>
          </div>

          <div class="flex items-center justify-between gap-4 text-sm">
            <p v-if="status === 'enviando'" class="text-slate-600">Enviando arquivo... {{ progresso }}%</p>
            <p v-else class="flex items-center gap-2 font-medium text-emerald-700">
              <CircleCheck class="w-4 h-4" />
              Upload concluído · Pronto para validação
            </p>

            <button
              v-if="status === 'concluido'"
              class="shrink-0 rounded-md bg-slate-950 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800 duration-200"
              @click="etapaAtual = 1"
            >
              Continuar
            </button>
          </div>
        </div>

        <p v-if="erro" class="mt-4 flex items-center gap-2 text-sm text-rose-600">
          <CircleAlert class="w-4 h-4 shrink-0" />
          {{ erro }}
        </p>

        <input
          ref="seletor"
          type="file"
          class="hidden"
          :accept="EXTENSOES.map((extensao) => `.${extensao}`).join(',')"
          @change="selecionarArquivo"
        >
      </div>

      <div class="bg-slate-50 border-t border-slate-200 px-6 py-4 flex gap-3 text-xs text-slate-600">
        <Info class="w-4 h-4 shrink-0" />
        <div>
          <p class="font-semibold text-slate-800">Formatos suportados: .xlsx, .xls, .csv. Tamanho máximo: 50MB.</p>
          <p class="mt-1">Os dados enviados são processados localmente antes da importação para garantir a integridade e conformidade com as regras de negócio.</p>
        </div>
      </div>
    </section>

    <!-- Rascunhos -->
    <section class="flex flex-col gap-3">
      <h2 class="text-xs font-semibold uppercase tracking-wide text-slate-500">Importações recentes (rascunhos)</h2>

      <article
        v-for="importacao in importacoesRecentes"
        :key="importacao.arquivo"
        class="bg-white rounded-xl border border-slate-200 shadow-sm p-4 flex items-center gap-4"
      >
        <span class="w-10 h-10 shrink-0 rounded-lg bg-slate-100 text-slate-600 flex items-center justify-center">
          <Table class="w-5 h-5" />
        </span>
        <div class="flex-1 min-w-0">
          <p class="font-semibold truncate">{{ importacao.arquivo }}</p>
          <p class="text-xs text-slate-500">Parado em: {{ importacao.etapa }} · {{ importacao.quando }}</p>
        </div>
        <button class="text-sm font-semibold text-blue-700 hover:underline">Continuar</button>
      </article>
    </section>
  </div>
</template>

<script setup>
import { ref, onBeforeUnmount } from 'vue'
import { FileText, FileSpreadsheet, Trash2, Check, CircleCheck, CircleAlert, Info, Table } from 'lucide-vue-next'
import { importacoesRecentes } from '../data/mock'
import { formatarTamanho } from '../utils/formatar'

const EXTENSOES = ['xlsx', 'xls', 'csv']
const TAMANHO_MAXIMO = 50 * 1024 * 1024

const etapas = ['Upload', 'Validação', 'Padronização', 'Revisão', 'Importação']
const etapaAtual = ref(0)

const seletor = ref(null)
const arquivo = ref(null)
const arrastando = ref(false)
const status = ref('') // 'enviando' | 'concluido'
const progresso = ref(0)
const erro = ref('')
let intervalo = null

function abrirSeletor() {
  seletor.value.click()
}

function selecionarArquivo(evento) {
  carregarArquivo(evento.target.files[0])
  evento.target.value = ''
}

function soltarArquivo(evento) {
  arrastando.value = false
  carregarArquivo(evento.dataTransfer.files[0])
}

function carregarArquivo(novoArquivo) {
  if (!novoArquivo) return
  erro.value = ''

  const extensao = novoArquivo.name.split('.').pop().toLowerCase()
  if (!EXTENSOES.includes(extensao)) {
    erro.value = 'Formato não suportado. Envie um arquivo .xlsx, .xls ou .csv.'
    return
  }
  if (novoArquivo.size > TAMANHO_MAXIMO) {
    erro.value = 'O arquivo ultrapassa o tamanho máximo de 50MB.'
    return
  }

  arquivo.value = novoArquivo
  simularEnvio()
}

// Upload simulado: ainda não há API para receber o arquivo
function simularEnvio() {
  clearInterval(intervalo)
  status.value = 'enviando'
  progresso.value = 0

  intervalo = setInterval(() => {
    progresso.value = Math.min(progresso.value + 10, 100)
    if (progresso.value === 100) {
      clearInterval(intervalo)
      status.value = 'concluido'
    }
  }, 150)
}

function removerArquivo() {
  clearInterval(intervalo)
  arquivo.value = null
  status.value = ''
  progresso.value = 0
  etapaAtual.value = 0
}

onBeforeUnmount(() => clearInterval(intervalo))
</script>

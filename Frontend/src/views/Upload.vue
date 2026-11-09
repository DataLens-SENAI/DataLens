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
              :class="['h-full rounded-full transition-all duration-200', corDaBarra]"
              :style="{ width: `${progresso}%` }"
            ></div>
          </div>

          <div class="flex items-center justify-between gap-4 text-sm">
            <p v-if="status === 'enviando'" class="text-slate-600">Enviando arquivo... {{ progresso }}%</p>
            <p v-else-if="status === 'processando'" class="text-slate-600">Validando e padronizando os dados...</p>
            <p v-else-if="resultado?.status === 'SUCESSO'" class="flex items-center gap-2 font-medium text-emerald-700">
              <CircleCheck class="w-4 h-4" />
              Importação concluída · {{ formatarNumero(resultado.registrosProcessados) }} registros processados
            </p>
            <p v-else-if="resultado" class="flex items-center gap-2 font-medium text-rose-700">
              <CircleAlert class="w-4 h-4" />
              {{ resultado.problemas.length }} problema(s) encontrado(s). Nada foi gravado: corrija a planilha e envie de novo.
            </p>

            <router-link
              v-if="resultado?.status === 'SUCESSO'"
              to="/dashboard"
              class="shrink-0 rounded-md bg-slate-950 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800 duration-200"
            >
              Ver Dashboard
            </router-link>
            <button
              v-else-if="status === 'concluido'"
              class="shrink-0 rounded-md bg-slate-950 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800 duration-200"
              @click="removerArquivo"
            >
              Enviar outro arquivo
            </button>
          </div>
        </div>

        <!-- Problemas da validação: linha, campo, valor original e sugestão -->
        <div v-if="resultado?.problemas.length" class="mt-4 overflow-x-auto rounded-lg border border-rose-200">
          <table class="w-full text-sm">
            <thead class="bg-rose-50 text-left text-rose-800">
              <tr>
                <th class="font-medium px-4 py-2">Linha</th>
                <th class="font-medium px-4 py-2">Campo</th>
                <th class="font-medium px-4 py-2">Problema</th>
                <th class="font-medium px-4 py-2">Valor original</th>
                <th class="font-medium px-4 py-2">Sugestão</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-rose-100">
              <tr v-for="(problema, indice) in resultado.problemas" :key="indice">
                <td class="px-4 py-2">{{ problema.linha ?? 'Arquivo' }}</td>
                <td class="px-4 py-2">{{ problema.campo }}</td>
                <td class="px-4 py-2">{{ problema.mensagem }}</td>
                <td class="px-4 py-2 text-slate-600">{{ problema.valorOriginal ?? '—' }}</td>
                <td class="px-4 py-2 text-slate-600">{{ problema.valorSugerido ?? '—' }}</td>
              </tr>
            </tbody>
          </table>
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
          <p class="mt-1">A planilha é validada e padronizada antes de qualquer gravação. Se houver erros, nada é gravado e você vê a lista do que corrigir.</p>
        </div>
      </div>
    </section>

    <!-- Histórico de importações (telemetria) -->
    <section class="flex flex-col gap-3">
      <h2 class="text-xs font-semibold uppercase tracking-wide text-slate-500">Importações recentes</h2>

      <p v-if="!historico.length" class="text-sm text-slate-500">Nenhuma importação ainda.</p>

      <article
        v-for="importacao in historico"
        :key="importacao.idTelemetria"
        class="bg-white rounded-xl border border-slate-200 shadow-sm p-4 flex items-center gap-4"
      >
        <span class="w-10 h-10 shrink-0 rounded-lg bg-slate-100 text-slate-600 flex items-center justify-center">
          <Table class="w-5 h-5" />
        </span>
        <div class="flex-1 min-w-0">
          <p class="font-semibold truncate">{{ importacao.nomeArquivo }}</p>
          <p class="text-xs text-slate-500">
            {{ importacao.consultor }} · {{ formatarDataHora(importacao.dataUpload) }} · {{ importacao.registrosProcessados }} registros
          </p>
        </div>
        <span :class="['text-xs font-medium px-2.5 py-1 rounded-full whitespace-nowrap', importacao.status === 'SUCESSO' ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-700']">
          {{ TEXTO_STATUS[importacao.status] ?? importacao.status }}
        </span>
      </article>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { FileText, FileSpreadsheet, Trash2, Check, CircleCheck, CircleAlert, Info, Table } from 'lucide-vue-next'
import { enviarArquivo, requisitar } from '../services/api'
import { formatarNumero, formatarTamanho } from '../utils/formatar'

const EXTENSOES = ['xlsx', 'xls', 'csv']
const TAMANHO_MAXIMO = 50 * 1024 * 1024

// Texto de cada status de telemetria
const TEXTO_STATUS = {
  SUCESSO: 'Concluída',
  FALHA_VALIDACAO: 'Com erros',
  FALHA_PROCESSAMENTO: 'Falha no processamento',
  FALHA_GRAVACAO: 'Falha ao gravar'
}

// Etapas exibidas; a API faz validação, padronização e importação de uma vez
const etapas = ['Upload', 'Validação', 'Padronização', 'Revisão', 'Importação']
const ETAPA_VALIDACAO = 1
const etapaAtual = ref(0)

const seletor = ref(null)
const arquivo = ref(null)
const arrastando = ref(false)
const status = ref('') // 'enviando' | 'processando' | 'concluido'
const progresso = ref(0)
const erro = ref('')
const resultado = ref(null) // resposta de POST /importacoes
const historico = ref([]) // GET /importacoes

onMounted(carregarHistorico)

// Últimas importações
async function carregarHistorico() {
  try {
    historico.value = await requisitar('/importacoes')
  } catch (falha) {
    erro.value = falha.message
  }
}

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

// Confere formato e tamanho antes de enviar (a API confere de novo)
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
  enviar()
}

// Envia para a API e mostra o resultado: sucesso conclui todas as etapas, erro para na validação
async function enviar() {
  status.value = 'enviando'
  progresso.value = 0
  resultado.value = null
  etapaAtual.value = 0

  try {
    resultado.value = await enviarArquivo('/importacoes', arquivo.value, (percentual) => {
      progresso.value = percentual
      if (percentual === 100) {
        status.value = 'processando'
        etapaAtual.value = ETAPA_VALIDACAO
      }
    })
    etapaAtual.value = resultado.value.status === 'SUCESSO' ? etapas.length : ETAPA_VALIDACAO
  } catch (falha) {
    erro.value = falha.message
  }
  status.value = 'concluido'
  carregarHistorico()
}

function removerArquivo() {
  arquivo.value = null
  status.value = ''
  progresso.value = 0
  etapaAtual.value = 0
  resultado.value = null
  erro.value = ''
}

// Barra azul durante o envio, verde no sucesso e vermelha se houver erro
const corDaBarra = computed(() => {
  if (status.value !== 'concluido') return 'bg-blue-700'
  return resultado.value?.status === 'SUCESSO' ? 'bg-emerald-500' : 'bg-rose-500'
})

// "2026-11-09T01:26:06" -> "09/11/2026 01:26"
function formatarDataHora(data) {
  return new Date(data).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
}
</script>

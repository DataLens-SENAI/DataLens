<template>
  <!-- Fundo escuro atrás do menu no mobile -->
  <div
    v-if="aberta"
    class="fixed inset-0 z-40 bg-slate-950/60 lg:hidden"
    @click="emit('fechar')"
  ></div>

  <aside
    :class="[
      'fixed inset-y-0 left-0 z-50 w-64 bg-slate-900 text-slate-300 flex flex-col transition-transform duration-300',
      aberta ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
    ]"
  >
    <!-- Marca -->
    <div class="flex items-start justify-between px-6 pt-6 pb-8">
      <router-link to="/dashboard">
        <p class="font-instrument text-xl font-bold text-white">DataLens</p>
        <p class="text-xs text-slate-400">Inteligência Comercial</p>
      </router-link>

      <button class="lg:hidden p-1 text-slate-400 hover:text-white" aria-label="Fechar menu" @click="emit('fechar')">
        <X class="w-5 h-5" />
      </button>
    </div>

    <!-- Navegação -->
    <nav class="flex-1 overflow-y-auto px-3 flex flex-col gap-1">
      <router-link
        v-for="item in itens"
        :key="item.rota"
        :to="item.rota"
        class="flex items-center gap-3 px-3 py-2 rounded-md text-sm font-medium hover:bg-slate-800 hover:text-white duration-200"
        active-class="!bg-blue-700 !text-white"
      >
        <component :is="item.icone" class="w-5 h-5 shrink-0" />
        {{ item.nome }}
      </router-link>
    </nav>

    <!-- Usuário -->
    <div class="mx-3 mt-4 border-t border-slate-700/70 px-3 py-5 flex flex-col gap-4">
      <div>
        <p class="text-sm font-semibold text-white">Usuário DataLens</p>
        <p class="text-xs text-slate-400">Admin</p>
      </div>

      <router-link to="/login" class="flex items-center gap-3 text-sm text-slate-400 hover:text-white duration-200">
        <LogOut class="w-5 h-5" />
        Logout
      </router-link>
    </div>
  </aside>
</template>

<script setup>
import {
  LayoutDashboard, Users, BriefcaseBusiness, Shapes, Layers, ChartColumn,
  Lightbulb, CloudUpload, ListChecks, Settings, LogOut, X
} from 'lucide-vue-next'

defineProps({
  aberta: { type: Boolean, default: false }
})

const emit = defineEmits(['fechar'])

const itens = [
  { nome: 'Visão Geral', rota: '/dashboard', icone: LayoutDashboard },
  { nome: 'Clientes', rota: '/clientes', icone: Users },
  { nome: 'Consultores', rota: '/consultores', icone: BriefcaseBusiness },
  { nome: 'Segmentos', rota: '/segmentos', icone: Shapes },
  { nome: 'Serviços', rota: '/servicos', icone: Layers },
  { nome: 'Análises', rota: '/analises', icone: ChartColumn },
  { nome: 'Insights', rota: '/insights', icone: Lightbulb },
  { nome: 'Importação de Dados', rota: '/importacao', icone: CloudUpload },
  { nome: 'Qualidade dos Dados', rota: '/qualidade', icone: ListChecks },
  { nome: 'Configurações', rota: '/configuracoes', icone: Settings }
]
</script>

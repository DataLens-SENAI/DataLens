<script setup>
// Definimos as propriedades (props) que o card vai receber para ser dinâmico
defineProps({
  titulo: {
    type: String,
    required: true
  },
  valor: {
    type: [String, Number],
    required: true
  },
  variacao: {
    type: String,
    default: ''
  },
  tipoVariacao: {
    type: String, // 'positivo' ou 'negativo'
    default: 'positivo'
  },
  // Texto auxiliar ao lado do valor (ex: "23% do total")
  descricao: {
    type: String,
    default: ''
  },
  // Componente de ícone (ex: um ícone do lucide-vue-next)
  icone: {
    type: [Object, Function],
    default: null
  }
})
</script>

<template>
  <div class="bg-white p-5 rounded-xl shadow-sm border border-slate-200 flex flex-col justify-between gap-3">
    <!-- Cabeçalho do Card -->
    <div class="flex items-start justify-between gap-2">
      <span class="text-xs font-medium uppercase tracking-wide text-slate-500">{{ titulo }}</span>
      <span v-if="icone" class="w-8 h-8 shrink-0 rounded-full bg-slate-100 text-slate-600 flex items-center justify-center">
        <component :is="icone" class="w-4 h-4" />
      </span>
    </div>

    <!-- Valor Principal -->
    <div class="flex flex-wrap items-baseline gap-2">
      <h3 class="text-2xl font-bold text-slate-900">{{ valor }}</h3>

      <!-- Tag opcional de variação (ex: +12% em relação ao mês passado) -->
      <span
        v-if="variacao"
        :class="[
          'text-xs font-semibold px-2 py-0.5 rounded-full',
          tipoVariacao === 'positivo' ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'
        ]"
      >
        {{ variacao }}
      </span>
      <span v-if="descricao" class="text-xs text-slate-500">{{ descricao }}</span>
    </div>
  </div>
</template>

<template>
  <section class="flex flex-col items-center justify-center text-center gap-3 py-24">
    <div class="w-14 h-14 rounded-full bg-blue-50 text-blue-700 flex items-center justify-center">
      <Hammer class="w-6 h-6" />
    </div>
    <h1 class="font-instrument text-2xl font-bold">{{ route.meta.titulo }}</h1>
    <p class="text-slate-500 max-w-sm">Esta tela ainda está em construção e estará disponível nas próximas entregas.</p>
    <router-link to="/dashboard" class="mt-2 text-sm font-semibold text-blue-700 hover:underline">
      Voltar para a Visão Geral
    </router-link>
  </section>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { Hammer } from 'lucide-vue-next'

const route = useRoute()
</script>

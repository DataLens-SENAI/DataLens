<template>
  <Header />

  <main class="min-h-screen bg-slate-950 flex flex-col gap-24 md:gap-32 pt-16 md:pt-24 lg:pt-28">

    <!-- Seção Hero (Apresentação Principal) -->
    <section class="relative flex flex-col mx-auto items-center gap-8 px-4 w-full">
      <!-- Brilho atrás do texto -->
      <div class="absolute z-0 w-96 h-36 bg-indigo-200/20 rounded-full blur-3xl"></div>

      <h1 class="relative z-10 font-instrument text-white font-bold text-4xl md:text-5xl lg:text-7xl text-center tracking-tight">
        <div>Transforme Dados em</div>
        <div class="bg-linear-to-r from-indigo-200 to-indigo-500 bg-clip-text text-transparent pb-2">
          Inteligência Comercial
        </div>
      </h1>

      <p class="relative z-10 text-zinc-400 max-w-xs md:max-w-lg lg:max-w-2xl text-center text-base md:text-lg lg:text-xl">
        A plataforma definitiva para limpeza, análise e insights estratégicos de
        dados B2B. Acelere suas vendas com dados precisos.
      </p>

      <HeroDashboard class="relative z-10 mt-8" />
    </section>

    <!-- Métricas que importam -->
    <section id="features" class="max-w-7xl mx-auto w-full px-4 md:px-8 flex flex-col items-center gap-4 scroll-mt-24">
      <span class="rounded-full border border-cyan-900 bg-cyan-950/60 px-3 py-1 text-xs font-medium text-cyan-400">Construído para Performance</span>
      <h2 class="font-instrument text-white font-bold text-3xl md:text-5xl text-center">Métricas que importam</h2>
      <p class="text-zinc-400 text-center">Dados limpos e organizados para decisões mais rápidas e precisas.</p>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 w-full mt-8">
        <div
          v-for="metrica in metricas"
          :key="metrica.titulo"
          class="rounded-xl border border-zinc-800 bg-slate-900/60 p-8 flex flex-col items-center text-center gap-2"
        >
          <span class="w-11 h-11 rounded-lg bg-slate-800 flex items-center justify-center mb-2">
            <component :is="metrica.icone" :class="['w-5 h-5', metrica.cor]" />
          </span>
          <p class="text-white font-bold text-4xl">{{ metrica.valor }}</p>
          <p class="text-zinc-300 text-sm">{{ metrica.titulo }}</p>
          <p class="text-zinc-500 text-xs">{{ metrica.descricao }}</p>
        </div>
      </div>
    </section>

    <!-- Gerenciamento de carteira -->
    <section id="solutions" class="bg-slate-900/50 border-y border-zinc-800 scroll-mt-16">
      <div class="max-w-7xl mx-auto px-4 md:px-8 py-16 md:py-24 grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
        <div class="flex flex-col items-start gap-6">
          <span class="rounded-full border border-blue-900 bg-blue-950/60 px-3 py-1 text-xs font-medium text-blue-400">Insights em Tempo Real</span>
          <h2 class="font-instrument text-white font-bold text-3xl md:text-4xl">Tudo o que você precisa para gerenciar sua carteira</h2>
          <p class="text-zinc-400 md:text-lg">
            Nosso dashboard consolidado oferece uma visão clara da performance comercial, identificando
            oportunidades de cross-sell e riscos de churn de forma automatizada.
          </p>

          <ul class="flex flex-col gap-5 mt-2">
            <li v-for="recurso in recursos" :key="recurso.titulo" class="flex gap-4">
              <span class="w-9 h-9 shrink-0 rounded-md bg-slate-800 flex items-center justify-center">
                <component :is="recurso.icone" :class="['w-4 h-4', recurso.cor]" />
              </span>
              <div>
                <p class="text-white font-semibold">{{ recurso.titulo }}</p>
                <p class="text-zinc-400 text-sm">{{ recurso.descricao }}</p>
              </div>
            </li>
          </ul>
        </div>

        <!-- Placeholder da imagem ilustrativa -->
        <div class="relative aspect-4/3 rounded-2xl overflow-hidden border border-zinc-800 bg-slate-950">
          <div class="absolute -top-10 -left-10 w-72 h-72 rounded-full bg-blue-600/30 blur-3xl"></div>
          <div class="absolute -bottom-10 -right-10 w-72 h-72 rounded-full bg-cyan-500/20 blur-3xl"></div>
          <div
            class="absolute inset-0 opacity-15"
            style="background-image: linear-gradient(#94a3b8 1px, transparent 1px), linear-gradient(90deg, #94a3b8 1px, transparent 1px); background-size: 40px 40px;"
          ></div>
          <div class="absolute inset-0 flex items-center justify-center">
            <ChartPie class="w-16 h-16 text-slate-700" />
          </div>
        </div>
      </div>
    </section>

  </main>

  <Footer />
</template>

<script setup>
import { Users, ChartLine, Zap, ScanSearch, TriangleAlert, ChartPie } from 'lucide-vue-next'
import Header from '../components/Header.vue'
import Footer from '../components/Footer.vue'
import HeroDashboard from '../components/HeroDashboard.vue'

const metricas = [
  { valor: '1.2M+', titulo: 'Empresas Mapeadas', descricao: 'Base de dados atualizada diariamente', icone: Users, cor: 'text-cyan-400' },
  { valor: '99.9%', titulo: 'Precisão dos Dados', descricao: 'Algoritmos avançados de validação', icone: ChartLine, cor: 'text-blue-400' },
  { valor: '3x', titulo: 'Crescimento em Vendas', descricao: 'Média de nossos clientes no primeiro ano', icone: Zap, cor: 'text-cyan-400' }
]

const recursos = [
  { titulo: 'Visão 360º do Cliente', descricao: 'Entenda o perfil completo de cada cliente, serviços contratados e histórico.', icone: ScanSearch, cor: 'text-cyan-400' },
  { titulo: 'Alertas Inteligentes', descricao: 'Receba notificações sobre quedas de uso ou oportunidades de upgrade.', icone: TriangleAlert, cor: 'text-blue-400' }
]
</script>

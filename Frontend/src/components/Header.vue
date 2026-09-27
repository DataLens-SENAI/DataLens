<template>
  <header class="flex justify-between items-center bg-slate-950 px-4 py-4 md:px-6 lg:px-8 text-zinc-400 w-full border-b border-zinc-800 sticky top-0 z-50">
    <!-- Logo -->
    <router-link to="/" class="flex items-center">
      <img src="../assets/logo.svg" alt="DataLens" class="w-24 md:w-28 lg:w-32 hover:scale-[1.05] duration-300">
    </router-link>

    <!-- Navegação Desktop (Visível apenas em telas grandes) -->
    <nav class="hidden md:flex items-center gap-6 lg:gap-8 font-semibold text-sm">
      <router-link to="/" class="hover:text-zinc-200 duration-300">Home</router-link>
      <a href="#features" class="hover:text-zinc-200 duration-300">Features</a>

      <!-- Dropdown Solutions -->
      <div class="relative" @mouseenter="solucoesAberto = true" @mouseleave="solucoesAberto = false">
        <button
          class="flex items-center gap-1 hover:text-zinc-200 duration-300"
          :aria-expanded="solucoesAberto"
          @click="solucoesAberto = !solucoesAberto"
        >
          Solutions
          <ChevronDown :class="['w-4 h-4 duration-300', solucoesAberto ? 'rotate-180' : '']" />
        </button>

        <div v-if="solucoesAberto" class="absolute left-1/2 -translate-x-1/2 top-full pt-3">
          <div class="w-60 rounded-lg border border-zinc-800 bg-slate-900 p-2 shadow-2xl">
            <a
              v-for="solucao in solucoes"
              :key="solucao.titulo"
              href="#solutions"
              class="block rounded-md px-3 py-2 hover:bg-slate-800 duration-200"
              @click="solucoesAberto = false"
            >
              <span class="block text-zinc-200">{{ solucao.titulo }}</span>
              <span class="block text-xs font-normal text-zinc-500">{{ solucao.descricao }}</span>
            </a>
          </div>
        </div>
      </div>

      <a href="#team" class="hover:text-zinc-200 duration-300">Team</a>
      <a href="#pricing" class="hover:text-zinc-200 duration-300">Pricing</a>
    </nav>

    <!-- Botão de Ação (Desktop) -->
    <router-link to="/login" class="hidden md:inline-flex py-2 px-4 bg-white text-slate-900 text-sm font-semibold rounded-lg hover:shadow-sm shadow-slate-200 duration-300 hover:scale-[1.02]">
      Entrar no Sistema
    </router-link>

    <!-- Botão do Menu Hambúrguer (Visível apenas em telas mobile) -->
    <button @click="toggleMenu" class="md:hidden text-zinc-300 focus:outline-none p-2" aria-label="Abrir menu">
      <svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
        <path v-if="!menuAberto" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path>
        <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
      </svg>
    </button>
  </header>

  <!-- Menu Mobile Suspenso (Abre quando clica no hambúrguer) -->
  <div v-if="menuAberto" class="md:hidden bg-slate-950 border-b border-zinc-800 px-6 py-6 flex flex-col gap-6 text-zinc-300 fixed w-full z-40 shadow-2xl transition-all">
    <nav class="flex flex-col gap-4 font-bold text-base">
      <router-link to="/" @click="menuAberto = false" class="hover:text-white duration-200">Home</router-link>
      <a href="#features" @click="menuAberto = false" class="hover:text-white duration-200">Features</a>
      <a href="#solutions" @click="menuAberto = false" class="hover:text-white duration-200">Solutions</a>
      <a href="#team" @click="menuAberto = false" class="hover:text-white duration-200">Team</a>
      <a href="#pricing" @click="menuAberto = false" class="hover:text-white duration-200">Pricing</a>
    </nav>

    <hr class="border-zinc-800">

    <router-link to="/login" @click="menuAberto = false" class="py-3 text-center bg-white text-slate-900 text-sm font-semibold rounded-lg shadow-md duration-300">
      Entrar no Sistema
    </router-link>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ChevronDown } from 'lucide-vue-next'

const menuAberto = ref(false)
const solucoesAberto = ref(false)

const solucoes = [
  { titulo: 'Limpeza de Dados', descricao: 'Padronize e remova duplicidades' },
  { titulo: 'Gestão de Carteira', descricao: 'Visão 360º dos seus clientes' },
  { titulo: 'Insights Comerciais', descricao: 'Cross-sell e alertas de churn' }
]

function toggleMenu() {
  menuAberto.value = !menuAberto.value
}
</script>

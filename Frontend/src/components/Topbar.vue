<template>
  <header class="sticky top-0 z-30 h-16 bg-white border-b border-slate-200 flex items-center gap-4 px-4 md:px-6 lg:px-8">
    <button class="lg:hidden p-1 -ml-1 text-slate-600" aria-label="Abrir menu" @click="emit('abrir-menu')">
      <Menu class="w-6 h-6" />
    </button>

    <!-- Breadcrumb -->
    <nav class="flex items-center gap-1.5 text-sm min-w-0">
      <router-link to="/dashboard" class="text-slate-500 hover:text-slate-900 duration-200">Home</router-link>
      <ChevronRight class="w-4 h-4 text-slate-400 shrink-0" />
      <span class="font-semibold text-slate-900 truncate">{{ route.meta.titulo }}</span>
    </nav>

    <div class="ml-auto flex items-center gap-2 md:gap-4">
      <label class="relative hidden md:block">
        <Search class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
        <input
          type="search"
          placeholder="Buscar..."
          class="w-56 lg:w-64 rounded-full border border-slate-200 bg-slate-50 py-2 pl-9 pr-4 text-sm outline-none focus:border-blue-500 focus:bg-white duration-200"
        >
      </label>

      <button class="relative p-2 text-slate-600 hover:text-slate-900" aria-label="Notificações">
        <Bell class="w-5 h-5" />
        <span class="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-rose-500"></span>
      </button>

      <router-link to="/configuracoes" class="p-2 text-slate-600 hover:text-slate-900" aria-label="Configurações">
        <Settings class="w-5 h-5" />
      </router-link>

      <div class="w-9 h-9 rounded-full bg-slate-900 text-white text-xs font-semibold flex items-center justify-center" title="Usuário DataLens">
        UD
      </div>
    </div>
  </header>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { Menu, ChevronRight, Search, Bell, Settings } from 'lucide-vue-next'

const route = useRoute()
const emit = defineEmits(['abrir-menu'])
</script>

<template>
  <div class="min-h-screen bg-slate-50 text-slate-900">
    <Sidebar :aberta="sidebarAberta" @fechar="sidebarAberta = false" />

    <div class="lg:pl-64 flex flex-col min-h-screen">
      <Topbar @abrir-menu="sidebarAberta = true" />

      <main class="flex-1 p-4 md:p-6 lg:p-8">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from '../components/Sidebar.vue'
import Topbar from '../components/Topbar.vue'

const route = useRoute()
const sidebarAberta = ref(false)

// Fecha o menu mobile ao trocar de página
watch(() => route.path, () => {
  sidebarAberta.value = false
})
</script>

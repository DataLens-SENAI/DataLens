<template>
  <footer class="bg-slate-900 border-t border-zinc-800 text-zinc-400">
    <div class="max-w-7xl mx-auto px-4 md:px-8 py-12 md:py-16">
      <div class="grid grid-cols-2 md:grid-cols-4 gap-10">
        <div class="col-span-2 md:col-span-1 flex flex-col gap-4">
          <img src="../assets/logo.svg" alt="DataLens" class="w-28">
          <p class="text-sm max-w-xs">Transformando dados complexos em inteligência acionável para equipes B2B de alta performance.</p>
        </div>

        <div v-for="coluna in colunas" :key="coluna.titulo" class="flex flex-col gap-4">
          <h3 class="text-sm font-semibold text-white">{{ coluna.titulo }}</h3>
          <ul class="flex flex-col gap-2 text-sm">
            <li v-for="link in coluna.links" :key="link">
              <a href="#" class="hover:text-zinc-200 duration-200">{{ link }}</a>
            </li>
          </ul>
        </div>
      </div>

      <div class="mt-12 pt-6 border-t border-zinc-800 flex flex-col sm:flex-row items-center justify-between gap-4 text-sm">
        <p>© {{ anoAtual }} DataLens. Todos os direitos reservados.</p>
        <div class="flex items-center gap-4">
          <a v-for="rede in redes" :key="rede.nome" href="#" :aria-label="rede.nome" class="hover:text-zinc-200 duration-200">
            <component :is="rede.icone" class="w-5 h-5" />
          </a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { Linkedin, Twitter, Github } from 'lucide-vue-next'

const anoAtual = new Date().getFullYear()

const colunas = [
  { titulo: 'Produto', links: ['Funcionalidades', 'Preços', 'Casos de Uso'] },
  { titulo: 'Empresa', links: ['Sobre Nós', 'Carreiras', 'Contato'] },
  { titulo: 'Legal', links: ['Privacidade', 'Termos de Uso'] }
]

const redes = [
  { nome: 'LinkedIn', icone: Linkedin },
  { nome: 'Twitter', icone: Twitter },
  { nome: 'GitHub', icone: Github }
]
</script>

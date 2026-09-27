<template>
  <main class="min-h-screen grid lg:grid-cols-2 bg-slate-50 text-slate-900">
    <!-- Formulário -->
    <section class="flex flex-col justify-center px-6 py-12 sm:px-12 lg:px-16 xl:px-24">
      <div class="w-full max-w-md mx-auto">
        <router-link
          to="/"
          class="inline-flex items-center gap-2 mb-10 text-sm font-medium text-slate-500 hover:text-slate-900 duration-200"
        >
          <ArrowLeft class="w-4 h-4" />
          Voltar para o início
        </router-link>

        <router-link to="/" class="flex items-center gap-3">
          <span class="w-10 h-10 rounded-md bg-slate-900 text-white flex items-center justify-center">
            <ChartColumn class="w-5 h-5" />
          </span>
          <span class="font-instrument text-3xl font-bold">DataLens</span>
        </router-link>

        <h1 class="font-instrument text-3xl font-bold mt-8">Bem-vindo</h1>
        <p class="text-slate-500 mt-2">Transforme dados comerciais em decisões estratégicas.</p>

        <form class="flex flex-col gap-5 mt-10" novalidate @submit.prevent="entrar">
          <!-- E-mail -->
          <div class="flex flex-col gap-1.5">
            <label for="email" class="text-xs font-semibold text-slate-700">E-mail Corporativo</label>
            <div class="relative">
              <Mail class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                id="email"
                v-model.trim="email"
                type="email"
                autocomplete="email"
                placeholder="seu.nome@empresa.com"
                :class="[classeInput, erros.email ? 'border-rose-400' : 'border-slate-300']"
              >
            </div>
            <p v-if="erros.email" class="text-xs text-rose-600">{{ erros.email }}</p>
          </div>

          <!-- Senha -->
          <div class="flex flex-col gap-1.5">
            <label for="senha" class="text-xs font-semibold text-slate-700">Senha</label>
            <div class="relative">
              <Lock class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                id="senha"
                v-model="senha"
                :type="mostrarSenha ? 'text' : 'password'"
                autocomplete="current-password"
                placeholder="••••••••"
                :class="[classeInput, 'pr-10', erros.senha ? 'border-rose-400' : 'border-slate-300']"
              >
              <button
                type="button"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-700"
                :aria-label="mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'"
                @click="mostrarSenha = !mostrarSenha"
              >
                <EyeOff v-if="mostrarSenha" class="w-4 h-4" />
                <Eye v-else class="w-4 h-4" />
              </button>
            </div>
            <p v-if="erros.senha" class="text-xs text-rose-600">{{ erros.senha }}</p>
          </div>

          <div class="flex items-center justify-between text-sm">
            <label class="flex items-center gap-2 text-slate-600 cursor-pointer">
              <input v-model="lembrar" type="checkbox" class="w-4 h-4 rounded border-slate-300 accent-slate-900">
              Lembrar-me
            </label>
            <a href="#" class="text-xs font-semibold text-blue-700 hover:underline">Esqueci minha senha</a>
          </div>

          <button
            type="submit"
            :disabled="carregando"
            class="flex items-center justify-center gap-2 rounded-md bg-slate-900 py-2.5 text-sm font-semibold text-white hover:bg-slate-800 disabled:opacity-70 duration-200"
          >
            {{ carregando ? 'Entrando...' : 'Entrar' }}
            <LogIn class="w-4 h-4" />
          </button>
        </form>

        <hr class="border-slate-200 mt-10">
        <p class="text-xs text-slate-500 mt-6">© {{ anoAtual }} DataLens. Todos os direitos reservados.</p>
      </div>
    </section>

    <!-- Imagem lateral (placeholder até termos o asset definitivo) -->
    <section class="hidden lg:block relative overflow-hidden bg-slate-950">
      <div class="absolute -top-24 -left-24 w-md h-112 rounded-full bg-cyan-500/30 blur-3xl"></div>
      <div class="absolute bottom-0 right-0 w-lg h-128 rounded-full bg-indigo-600/30 blur-3xl"></div>
      <div
        class="absolute inset-0 opacity-20"
        style="background-image: linear-gradient(#94a3b8 1px, transparent 1px), linear-gradient(90deg, #94a3b8 1px, transparent 1px); background-size: 48px 48px;"
      ></div>

      <div class="relative h-full flex flex-col justify-end p-16 text-white">
        <p class="font-instrument text-3xl font-bold max-w-md">Dados limpos, decisões mais rápidas.</p>
        <p class="text-slate-300 mt-3 max-w-md">Centralize, padronize e analise a sua carteira comercial em um só lugar.</p>
      </div>
    </section>
  </main>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, ChartColumn, Mail, Lock, Eye, EyeOff, LogIn } from 'lucide-vue-next'

const CHAVE_EMAIL = 'datalens:email'

const router = useRouter()
const anoAtual = new Date().getFullYear()

const classeInput = 'w-full rounded-md border bg-white py-2.5 pl-9 pr-3 text-sm outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 duration-200'

const email = ref(lerEmailSalvo())
const senha = ref('')
const lembrar = ref(!!email.value)
const mostrarSenha = ref(false)
const carregando = ref(false)
const erros = reactive({ email: '', senha: '' })

function lerEmailSalvo() {
  try {
    return localStorage.getItem(CHAVE_EMAIL) ?? ''
  } catch {
    return ''
  }
}

function validar() {
  erros.email = ''
  erros.senha = ''

  if (!email.value) erros.email = 'Informe o seu e-mail corporativo.'
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) erros.email = 'Informe um e-mail válido.'

  if (!senha.value) erros.senha = 'Informe a sua senha.'
  else if (senha.value.length < 6) erros.senha = 'A senha deve ter pelo menos 6 caracteres.'

  return !erros.email && !erros.senha
}

// Login simulado: ainda não há API de autenticação
function entrar() {
  if (!validar()) return

  try {
    if (lembrar.value) localStorage.setItem(CHAVE_EMAIL, email.value)
    else localStorage.removeItem(CHAVE_EMAIL)
  } catch {
    // Sem acesso ao localStorage: apenas não lembra o e-mail
  }

  carregando.value = true
  setTimeout(() => router.push('/dashboard'), 800)
}
</script>

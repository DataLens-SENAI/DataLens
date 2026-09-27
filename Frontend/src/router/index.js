import { createRouter, createWebHistory } from 'vue-router'

import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import AppLayout from '../layouts/AppLayout.vue'

// Telas da área interna ainda sem protótipo usam o placeholder "Em breve"
const EmBreve = () => import('../views/EmBreve.vue')

const routes = [
    {
        path: '/',
        name: 'home',
        component: Home
    },
    {
        path: '/login',
        name: 'login',
        component: Login
    },
    {
        // Área interna: Sidebar + Topbar compartilhadas
        path: '/',
        component: AppLayout,
        children: [
            { path: 'dashboard', name: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { titulo: 'Dashboard' } },
            { path: 'clientes', name: 'clientes', component: EmBreve, meta: { titulo: 'Clientes' } },
            { path: 'consultores', name: 'consultores', component: EmBreve, meta: { titulo: 'Consultores' } },
            { path: 'segmentos', name: 'segmentos', component: EmBreve, meta: { titulo: 'Segmentos' } },
            { path: 'servicos', name: 'servicos', component: EmBreve, meta: { titulo: 'Serviços' } },
            { path: 'analises', name: 'analises', component: () => import('../views/Analises.vue'), meta: { titulo: 'Análises' } },
            { path: 'insights', name: 'insights', component: EmBreve, meta: { titulo: 'Insights' } },
            { path: 'importacao', name: 'importacao', component: () => import('../views/Upload.vue'), meta: { titulo: 'Importação de Dados' } },
            { path: 'qualidade', name: 'qualidade', component: EmBreve, meta: { titulo: 'Qualidade dos Dados' } },
            { path: 'configuracoes', name: 'configuracoes', component: EmBreve, meta: { titulo: 'Configurações' } }
        ]
    },
    {
        path: '/:pathMatch(.*)*',
        redirect: '/'
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior(to) {
        if (to.hash) return { el: to.hash, behavior: 'smooth' }
        return { top: 0 }
    }
})

export default router

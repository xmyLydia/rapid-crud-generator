import { createRouter, createWebHistory } from 'vue-router'
import Logs from '../views/Logs.vue'

const routes = [
  { path: '/logs', component: Logs },
  { path: '/', redirect: '/logs' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

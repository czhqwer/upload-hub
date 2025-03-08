import Vue from 'vue'
import VueRouter from 'vue-router'
import FileUpload from '@/views/FileUpload.vue';

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'home',
    component: FileUpload
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router

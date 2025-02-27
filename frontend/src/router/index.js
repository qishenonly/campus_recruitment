import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'register', 
    component: () => import('../views/auth/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: () => import('../views/layout/Layout.vue'),
    children: [
      {
        path: '',
        redirect: '/home'
      },
      {
        path: 'home',
        name: 'home',
        component: () => import('../views/home/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'company',
        name: 'company',
        component: () => import('../views/company/Company.vue'),
        meta: { title: '公司' }
      },
      {
        path: 'message',
        name: 'message',
        component: () => import('../views/message/Message.vue'),
        meta: { title: '消息' }
      },
      {
        path: 'mine',
        name: 'mine',
        component: () => import('../views/mine/Mine.vue'),
        meta: { title: '我的' }
      },
      {
        path: 'job/:id',
        name: 'jobDetail',
        component: () => import('../views/job/JobDetail.vue'),
        meta: { title: '职位详情' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
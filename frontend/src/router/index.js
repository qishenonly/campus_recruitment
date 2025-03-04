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
        component: () => {
          const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
          const lastVisitedRole = localStorage.getItem('lastVisitedRole')
          
          // 如果角色发生变化（说明是切换用户登录了），则刷新页面
          if (userInfo.role && lastVisitedRole !== userInfo.role) {
            localStorage.setItem('lastVisitedRole', userInfo.role)
            window.location.reload()
          }
          
          return userInfo.role === 'COMPANY' 
            ? import('../views/mine/CompanyMine.vue')
            : import('../views/mine/Mine.vue')
        },
        meta: { 
          title: '我的',
          keepAlive: false
        }
      },
      {
        path: 'job/:id',
        name: 'jobDetail',
        component: () => import('../views/job/JobDetail.vue'),
        meta: { title: '职位详情' }
      },
      {
        path: 'company/:id',
        name: 'CompanyDetail',
        component: () => import('../views/company/CompanyDetail.vue')
      },
      {
        path: 'resume',
        name: 'resume',
        component: () => import('../views/mine/Resume.vue'),
        meta: { title: '我的简历' }
      },
      {
        path: 'favorites',
        name: 'favorites',
        component: () => import('../views/mine/Favorites.vue'),
        meta: { title: '我的收藏' }
      },
      {
        path: 'chat/:chatId',
        name: 'chat',
        component: () => import('../views/message/Chat.vue'),
        meta: { title: '聊天' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
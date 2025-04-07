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

          console.log(userInfo);
          
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
        path: 'applications',
        name: 'applications',
        component: () => import('../views/mine/Applications.vue'),
        meta: { 
          title: '投递记录',
          requireAuth: true,
          roles: ['STUDENT']
        }
      },
      {
        path: 'chat/:chatId',
        name: 'chat',
        component: () => import('../views/message/Chat.vue'),
        meta: { title: '聊天' }
      },
      {
        path: '/company/job-management',
        name: 'jobManagement',
        component: () => import('../views/company/JobManagement.vue'),
        meta: { 
          title: '职位管理',
          requireAuth: true,
          roles: ['COMPANY'] 
        }
      },
      {
        path: '/company/resume-management',
        name: 'resumeManagement',
        component: () => import('../views/company/ResumeManagement.vue'),
        meta: { 
          title: '简历管理',
          requireAuth: true,
          roles: ['COMPANY'] 
        }
      },
      {
        path: '/company/interview-management',
        name: 'interviewManagement',
        component: () => import('../views/company/InterviewManagement.vue'),
        meta: { 
          title: '面试管理',
          requireAuth: true,
          roles: ['COMPANY'] 
        }
      },
      {
        path: '/company/talent-pool',
        name: 'talentPool',
        component: () => import('../views/company/TalentPool.vue'),
        meta: { 
          title: '人才库',
          requireAuth: true,
          roles: ['COMPANY'] 
        }
      },
      {
        path: '/company/profile',
        name: 'companyInfo',
        component: () => import('../views/company/CompanyInfo.vue'),
        meta: { 
          title: '企业信息',
          requireAuth: true,
          roles: ['COMPANY'] 
        }
      },
      {
        path: '/company/team',
        name: 'teamManagement',
        component: () => import('../views/company/TeamManagement.vue'),
        meta: { 
          title: '团队管理',
          requireAuth: true,
          roles: ['COMPANY'] 
        }
      },
      {
        path: '/company/settings',
        name: 'accountSettings',
        component: () => import('../views/mine/AccountSettings.vue'),
        meta: { 
          title: '账号设置',
          requireAuth: true
        }
      },
      // 学生账号设置
      {
        path: '/settings',
        name: 'studentSettings',
        component: () => import('../views/mine/StudentSettings.vue'),
        meta: { 
          title: '账号设置',
          requireAuth: true,
          roles: ['STUDENT']
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
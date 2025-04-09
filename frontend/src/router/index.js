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
    path: '/forgot-password',
    name: 'forgotPassword',
    component: () => import('../views/auth/ForgotPassword.vue'),
    meta: { title: '忘记密码' }
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
  },
  // 管理后台路由
  {
    path: '/admin/login',
    name: 'adminLogin',
    component: () => import('../views/admin/AdminLogin.vue'),
    meta: { title: '管理员登录' }
  },
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'adminDashboard',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: { 
          title: '数据统计',
          requireAuth: true,
          adminOnly: true
        }
      },
      {
        path: 'students',
        name: 'studentManagement',
        component: () => import('../views/admin/StudentManagement.vue'),
        meta: { 
          title: '学生管理',
          requireAuth: true,
          adminOnly: true
        }
      },
      {
        path: 'companies',
        name: 'companyManagement',
        component: () => import('../views/admin/CompanyManagement.vue'),
        meta: { 
          title: '企业管理',
          requireAuth: true,
          adminOnly: true
        }
      },
      {
        path: 'settings',
        name: 'systemSettings',
        component: () => import('../views/admin/Settings.vue'),
        meta: { 
          title: '系统设置',
          requireAuth: true,
          adminOnly: true
        }
      },
      {
        path: 'logs',
        name: 'systemLogs',
        component: () => import('../views/admin/SystemLogs.vue'),
        meta: { 
          title: '系统日志',
          requireAuth: true,
          adminOnly: true
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title + ' - 青云直聘'
  }
  
  // 检查是否需要管理员权限
  if (to.matched.some(record => record.meta.adminOnly)) {
    const adminInfo = JSON.parse(localStorage.getItem('adminInfo') || '{}')
    if (!adminInfo.id) {
      next({ path: '/admin/login', query: { redirect: to.fullPath } })
    } else {
      next()
    }
  } 
  // 检查是否需要普通认证
  else if (to.matched.some(record => record.meta.requireAuth)) {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    
    if (!userInfo.id) {
      next({ path: '/login', query: { redirect: to.fullPath } })
    } else {
      // 检查角色权限
      const roles = to.meta.roles
      if (roles && !roles.includes(userInfo.role)) {
        next({ path: '/403' })
      } else {
        next()
      }
    }
  } else {
    next()
  }
})

export default router
<template>
  <div class="company-mine-page">
    <!-- 顶部个人信息卡片 -->
    <div class="profile-card">
      <div class="avatar-section">
        <img :src="companyLogo" class="company-logo" />
        <div class="company-info">
          <h3>{{ companyName }}</h3>
          <p class="role">{{ userRole === 'HR' ? '人力资源' : '招聘管理员' }}</p>
        </div>
      </div>
      <div class="profile-stats">
        <div class="stat-item">
          <span class="stat-num">{{ jobPostings }}</span>
          <span class="stat-label">发布职位</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ totalApplications }}</span>
          <span class="stat-label">收到简历</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ interviewCount }}</span>
          <span class="stat-label">面试中</span>
        </div>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="function-menu">
      <div class="menu-section">
        <div class="section-title">招聘管理</div>
        <div class="menu-grid">
          <div class="menu-item" @click="router.push('/company/job-management')">
            <van-icon name="notes-o" class="menu-icon" />
            <span>职位管理</span>
          </div>
          <div class="menu-item" @click="router.push('/company/resume-management')">
            <van-icon name="records" class="menu-icon" />
            <span>简历管理</span>
          </div>
          <div class="menu-item" @click="router.push('/company/interview-management')">
            <van-icon name="clock-o" class="menu-icon" />
            <span>面试管理</span>
          </div>
        </div>
      </div>

      <div class="menu-section">
        <div class="section-title">企业管理</div>
        <div class="menu-grid">
          <div class="menu-item" @click="router.push('/company/profile')">
            <van-icon name="shop-o" class="menu-icon" />
            <span>企业信息</span>
          </div>
          <div class="menu-item" @click="router.push('/company/team')">
            <van-icon name="cluster-o" class="menu-icon" />
            <span>团队管理</span>
          </div>
          <div class="menu-item" @click="router.push('/company/settings')">
            <van-icon name="setting-o" class="menu-icon" />
            <span>账号设置</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 退出登录按钮 -->
    <div class="logout-section">
      <van-button type="danger" block @click="handleLogout">退出登录</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showDialog } from 'vant'
import { getCompanyInfo, getCompanyDashboard } from '@/api/company'

const router = useRouter()

// 公司信息
const companyName = ref('正在加载...')
const companyLogo = ref('')
const userRole = ref('')

// 统计数据
const jobPostings = ref(0)
const totalApplications = ref(0)
const interviewCount = ref(0)

// 处理头像URL
const getAvatarUrl = (url) => {
  if (!url) return '/default-company.png';
  
  // 确保URL中包含/api前缀
  if (!url.startsWith('/api') && !url.startsWith('http')) {
    if (url.startsWith('/')) {
      url = '/api' + url;
    } else {
      url = '/api/' + url;
    }
  }
  
  // 如果URL以/api开头但不是完整URL，添加基础路径
  if (url.startsWith('/api') && !url.startsWith('http')) {
    const baseURL = import.meta.env.VITE_API_URL || '';
    // 如果基础URL已经包含/api，避免重复
    if (baseURL && baseURL.endsWith('/api')) {
      url = baseURL + url.substring(4); // 移除/api
    } else if (baseURL) {
      // 确保baseURL和url之间没有重复的斜杠
      if (baseURL.endsWith('/') && url.startsWith('/')) {
        url = baseURL + url.substring(1);
      } else if (!baseURL.endsWith('/') && !url.startsWith('/')) {
        url = baseURL + '/' + url;
      } else {
        url = baseURL + url;
      }
    }
  }
  
  return url;
}

// 加载企业信息
const loadCompanyInfo = async () => {
  try {
    const response = await getCompanyInfo()
    if (response.code === 200 && response.data) {
      companyName.value = response.data.name || '未知企业'
      companyLogo.value = response.data.logo ? getAvatarUrl(response.data.logo) : '/default-company.png'
      
      // 从localStorage获取用户信息
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      userRole.value = userInfo.position || 'HR'
    }
  } catch (error) {
    console.error('获取企业信息失败:', error)
  }
}

// 加载统计数据
const loadDashboardData = async () => {
  try {
    const response = await getCompanyDashboard()
    if (response.code === 200 && response.data) {
      jobPostings.value = response.data.jobPostings || 0
      totalApplications.value = response.data.totalApplications || 0
      interviewCount.value = response.data.interviewCount || 0
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const handleLogout = () => {
  showDialog({
    title: '确认退出',
    message: '是否确认退出登录？',
    showCancelButton: true,
  }).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  })
}

onMounted(() => {
  loadCompanyInfo()
  loadDashboardData()
  
  // 设置定时刷新，每分钟更新一次数据
  const timer = setInterval(() => {
    loadDashboardData()
  }, 60000)
  
  // 组件卸载时清除定时器
  return () => {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.company-mine-page {
  min-height: 100vh;
  background-color: #f6f6f8;
  padding: 16px;
}

.profile-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.avatar-section {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.company-logo {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
  margin-right: 16px;
}

.company-info h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.role {
  margin: 4px 0 0;
  font-size: 14px;
  color: #666;
}

.profile-stats {
  display: flex;
  justify-content: space-around;
  text-align: center;
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-num {
  font-size: 20px;
  font-weight: 500;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.function-menu {
  margin-bottom: 16px;
}

.menu-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 16px;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0;
  cursor: pointer;
}

.menu-icon {
  font-size: 24px;
  color: var(--primary-color);
  margin-bottom: 8px;
}

.menu-item span {
  font-size: 14px;
  color: #333;
}

.logout-section {
  margin-top: 32px;
  padding: 0 16px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .menu-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .profile-card {
    padding: 16px;
  }
  
  .company-logo {
    width: 50px;
    height: 50px;
  }
  
  .company-info h3 {
    font-size: 16px;
  }
  
  .stat-num {
    font-size: 18px;
  }
}
</style> 
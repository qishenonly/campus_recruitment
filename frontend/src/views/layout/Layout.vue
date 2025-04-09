<template>
  <div class="layout-container">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-content">
        <div class="logo">青云直聘</div>
        <nav class="nav-menu">
          <router-link to="/" class="nav-item" active-class="active">首页</router-link>
          <router-link to="/company" class="nav-item" active-class="active">公司</router-link>
          <!-- 只在登录后显示消息和我的 -->
          <template v-if="userInfo">
            <router-link to="/message" class="nav-item" active-class="active">消息</router-link>
            <router-link to="/mine" class="nav-item" active-class="active">我的</router-link>
          </template>
        </nav>
        
        <!-- 登录状态判断 -->
        <div class="auth-section">
          <template v-if="!userInfo">
            <router-link to="/login" class="login-btn">登录 / 注册</router-link>
          </template>
          <template v-else>
            <div class="user-actions">
              <router-link to="/mine" class="user-info">
                <img 
                  v-if="userInfo && userInfo.avatar" 
                  :src="getAvatarUrl(userInfo.avatar)" 
                  class="user-avatar" 
                  alt="用户头像"
                />
                <span class="user-name">{{ userInfo.username }}</span>
              </router-link>
              <span class="divider">|</span>
              <span class="logout-btn" @click="handleLogout">退出</span>
            </div>
          </template>
        </div>
      </div>
    </header>

    <!-- 主要内容区 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-content">
        <p>© 2024 青云直聘系统 All Rights Reserved</p>
      </div>
    </footer>

    <!-- 使用 store 中的状态 -->
    <complete-info-dialog
      v-model="dialogStore.showCompleteInfo"
      @complete="dialogStore.hideCompleteInfoDialog"
    />
    
    <!-- 企业信息完善对话框，改用本地变量控制 -->
    <complete-company-info-dialog
      v-if="userInfo && userInfo.role === 'COMPANY'"
      :model-value="localShowCompanyInfoDialog"
      @update:model-value="(val) => localShowCompanyInfoDialog = val"
      @complete="handleCompanyInfoComplete"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { showDialog } from 'vant'
import { useDialogStore } from '@/stores/dialog'
import CompleteInfoDialog from '../auth/components/CompleteInfoDialog.vue'
import CompleteCompanyInfoDialog from '../auth/components/CompleteCompanyInfoDialog.vue'
import { getUserProfile } from '@/api/user'
import { getCompanyInfo } from '@/api/company'

const router = useRouter()
const dialogStore = useDialogStore()
// 从 localStorage 获取用户信息
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo')))

// 监听 localStorage 变化
const handleStorageChange = (event) => {
  if (event.key === 'userInfo' && event.newValue) {
    userInfo.value = JSON.parse(event.newValue)
  } else if (event.key === 'userInfo' && !event.newValue) {
    userInfo.value = null
  }
}

// 本地控制对话框显示
const localShowCompanyInfoDialog = ref(false)

// 处理完善企业信息完成事件
const handleCompanyInfoComplete = () => {
  console.log('企业信息完善完成')
  localShowCompanyInfoDialog.value = false
}

// 显示企业信息完善对话框
const showCompanyInfoDialog = () => {
  console.log('调用showCompanyInfoDialog方法')
  // 使用setTimeout确保DOM已完全渲染
  setTimeout(() => {
    localShowCompanyInfoDialog.value = true
    console.log('企业信息完善对话框已触发显示, 当前状态:', localShowCompanyInfoDialog.value)
  }, 300)
}

// 检查用户信息是否需要完善
const checkUserInfo = async () => {
  console.log('开始检查用户信息', userInfo.value)
  
  if (!userInfo.value) {
    console.log('未登录，跳过检查')
    return // 未登录不处理
  }
  
  // 根据用户角色检查是否需要完善信息
  if (userInfo.value.role === 'STUDENT') {
    console.log('检查学生用户信息')
    try {
      const profileRes = await getUserProfile()
      console.log('获取学生用户资料结果:', profileRes)
      
      // 只有在获取不到用户资料时才显示完善信息弹窗
      if (!profileRes.data) {
        console.log('学生资料不存在，显示完善信息对话框')
        dialogStore.showCompleteInfoDialog()
      }
    } catch (error) {
      console.error('获取用户资料失败:', error)
      // 如果获取资料失败,也显示完善信息弹窗
      console.log('获取学生资料异常，显示完善信息对话框')
      dialogStore.showCompleteInfoDialog()
    }
  } else if (userInfo.value.role === 'COMPANY') {
    console.log('检查企业用户信息')
    
    // 如果已经有企业ID，说明已经完善了企业信息
    if (userInfo.value.companyId) {
      console.log('用户已关联企业ID:', userInfo.value.companyId)
      return
    }
    
    try {
      // 检查企业信息是否存在
      console.log('调用getCompanyInfo API')
      const companyRes = await getCompanyInfo()
      console.log('获取企业信息结果:', companyRes)
      
      // 修改逻辑：如果获取到了有效的企业信息，则不显示弹框
      if (companyRes.data && companyRes.code === 200 && companyRes.data.id) {
        console.log('企业信息已存在，无需显示完善信息弹框', companyRes.data)
        // 更新用户信息中的企业ID和名称
        if (userInfo.value && !userInfo.value.companyId) {
          userInfo.value.companyId = companyRes.data.id
          userInfo.value.companyName = companyRes.data.name
          localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        }
      } else {
        console.log('企业信息不存在或不完整，显示完善企业信息对话框')
        showCompanyInfoDialog()
      }
    } catch (error) {
      console.error('获取企业信息失败:', error)
      
      // 判断错误是否为企业不存在的情况
      const shouldShowDialog = (
        (error.response && error.response.status === 404) || 
        (error.response && error.response.data && 
         (error.response.data.message.includes('找不到该用户关联的企业信息') || 
          error.response.data.message.includes('企业信息不存在')))
      )
      
      if (shouldShowDialog) {
        console.log('企业不存在错误，显示完善企业信息对话框')
        showCompanyInfoDialog()
      }
    }
  } else {
    console.log('未知用户角色:', userInfo.value.role)
  }
}

// 页面加载时检测用户是否需要完善信息
onMounted(async () => {
  console.log('Layout组件已挂载')
  
  // 添加 storage 事件监听
  window.addEventListener('storage', handleStorageChange)
  
  try {
    console.log('开始检查用户信息完善状态')
    await checkUserInfo()
    console.log('用户信息检查完成')
    
    // 注释掉开发环境下的测试代码，避免强制显示对话框
    /* 
    if (process.env.NODE_ENV === 'development' && 
        userInfo.value && 
        userInfo.value.role === 'COMPANY' && 
        !userInfo.value.companyId) {
      console.log('开发环境：强制显示企业信息完善对话框')
      showCompanyInfoDialog()
    }
    */
  } catch (error) {
    console.error('检查用户信息时出现异常:', error)
  }
})

onBeforeUnmount(() => {
  // 移除事件监听
  window.removeEventListener('storage', handleStorageChange)
})

// 处理头像URL的方法
const getAvatarUrl = (url) => {
  if (!url) return '/default-avatar.png';
  
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
    const baseURL = import.meta.env.VITE_API_BASE_URL || '';
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

// 退出登录
const handleLogout = () => {
  showDialog({
    title: '提示',
    message: '确认退出登录吗？',
    showCancelButton: true
  }).then(() => {
    localStorage.removeItem('userInfo')
    localStorage.removeItem('token')
    userInfo.value = null
    router.push('/')
  }).catch(() => {
    // 取消退出
  })
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  height: 100%;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: var(--primary-color);
}

.nav-menu {
  flex: 1;
  display: flex;
  gap: 32px;
  margin-left: 48px;
}

.nav-item {
  font-size: 16px;
  color: #333;
  text-decoration: none;
  position: relative;
}

.nav-item.active {
  color: var(--primary-color);
}

.auth-section {
  margin-left: auto;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.login-btn, .user-info, .logout-btn {
  color: var(--primary-color);
  text-decoration: none;
  font-size: 14px;
  cursor: pointer;
}

.user-info {
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
}

.user-name {
  color: #333;
}

.divider {
  color: #ddd;
  font-size: 14px;
}

.logout-btn:hover {
  opacity: 0.8;
}

.main-content {
  margin-top: 60px;
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin-left: auto;
  margin-right: auto;
  padding: 20px;
}

.footer {
  background-color: #f7f7f7;
  padding: 40px 0;
  margin-top: auto;
}

.footer-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.footer-links {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
}

.footer-link {
  color: #666;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-link:hover {
  color: var(--primary-color);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 12px;
  }

  .nav-menu {
    gap: 16px;
    margin-left: 24px;
  }

  .nav-item {
    font-size: 14px;
  }

  .user-actions {
    gap: 4px;
  }
}

/* 添加路由切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>
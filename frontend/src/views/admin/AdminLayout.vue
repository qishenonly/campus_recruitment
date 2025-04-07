<template>
  <div class="admin-layout">
    <el-container class="admin-container">
      <!-- 侧边栏 -->
      <el-aside width="200px" class="aside">
        <div class="logo-container">
          <h1 class="logo">青云直聘</h1>
          <p class="subtitle">管理后台</p>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          :router="true"
          :collapse="isCollapse"
          background-color="#18191C"
          text-color="#ffffff"
          active-text-color="#1677FF">
          
          <el-menu-item index="/admin/dashboard">
            <el-icon><i-ep-data-line /></el-icon>
            <template #title>数据统计</template>
          </el-menu-item>
          
          <el-menu-item index="/admin/students">
            <el-icon><i-ep-user /></el-icon>
            <template #title>学生管理</template>
          </el-menu-item>
          
          <el-menu-item index="/admin/companies">
            <el-icon><i-ep-office-building /></el-icon>
            <template #title>企业管理</template>
          </el-menu-item>
          
          <el-menu-item index="/admin/settings">
            <el-icon><i-ep-setting /></el-icon>
            <template #title>系统设置</template>
          </el-menu-item>
          
          <el-menu-item index="/admin/logs">
            <el-icon><i-ep-document /></el-icon>
            <template #title>系统日志</template>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主区域 -->
      <el-container class="main-container">
        <!-- 头部 -->
        <el-header class="header">
          <div class="header-left">
            <el-icon
              class="collapse-btn"
              @click="toggleSidebar"
              :class="{ 'is-active': isCollapse }">
              <i-ep-fold v-if="isCollapse" />
              <i-ep-expand v-else />
            </el-icon>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentPageTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          
          <div class="header-right">
            <el-tooltip content="刷新页面" placement="bottom">
              <el-icon class="action-icon" @click="refreshPage"><i-ep-refresh /></el-icon>
            </el-tooltip>
            
            <el-dropdown trigger="click">
              <div class="avatar-container">
                <span class="admin-name">管理员</span>
                <el-avatar size="small" :src="adminAvatar" />
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="goToSettings">
                    <el-icon><i-ep-setting /></el-icon>账户设置
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><i-ep-switch-button /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <!-- 内容区 -->
        <el-main class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
        
        <!-- 页脚 -->
        <el-footer class="footer">
          <p>© 2024 青云直聘校园招聘系统 - 技术支持：字节跳动</p>
        </el-footer>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

// 默认头像
const adminAvatar = ref('https://sf-oss.bytescm.com/obj/static/xitu_extension/static/gold-user.png')
// 侧边栏状态
const isCollapse = ref(false)
// 当前路由
const route = useRoute()
const router = useRouter()

// 获取当前激活的菜单项
const activeMenu = computed(() => {
  return route.path
})

// 获取当前页面标题
const currentPageTitle = computed(() => {
  const routeMap = {
    '/admin/dashboard': '数据统计',
    '/admin/students': '学生管理',
    '/admin/companies': '企业管理',
    '/admin/settings': '系统设置',
    '/admin/logs': '系统日志'
  }
  return routeMap[route.path] || '首页'
})

// 切换侧边栏
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 刷新页面
const refreshPage = () => {
  window.location.reload()
}

// 跳转到设置页
const goToSettings = () => {
  router.push('/admin/settings')
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 清除登录信息
    localStorage.removeItem('adminInfo')
    localStorage.removeItem('adminToken')
    // 跳转到登录页
    router.push('/admin/login')
    ElMessage.success('已退出登录')
  }).catch(() => {})
}

// 页面加载时检查登录状态
onMounted(() => {
  const adminInfo = localStorage.getItem('adminInfo')
  if (!adminInfo && !route.path.includes('/admin/login')) {
    router.push('/admin/login')
    ElMessage.warning('请先登录')
  }
})
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.admin-container {
  height: 100%;
}

/* 侧边栏样式 */
.aside {
  background-color: #18191C;
  color: #ffffff;
  height: 100%;
  transition: all 0.3s;
  overflow-x: hidden;
}

.logo-container {
  height: 64px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  color: #1677FF;
  font-size: 18px;
  margin: 0;
  font-weight: 600;
}

.subtitle {
  color: #aaaaaa;
  font-size: 12px;
  margin: 4px 0 0 0;
}

.sidebar-menu {
  border-right: none;
}

/* 头部样式 */
.header {
  background-color: #ffffff;
  border-bottom: 1px solid #eaeaea;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  margin-right: 16px;
  transition: color 0.3s;
}

.collapse-btn:hover {
  color: #1677FF;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-icon {
  font-size: 18px;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: all 0.3s;
}

.action-icon:hover {
  background-color: rgba(0, 0, 0, 0.04);
  color: #1677FF;
}

.avatar-container {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 8px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s;
}

.avatar-container:hover {
  background-color: rgba(0, 0, 0, 0.04);
}

.admin-name {
  font-size: 14px;
  color: #333333;
}

/* 主内容区域 */
.main-container {
  background-color: #f5f7fa;
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.main-content {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

/* 页脚 */
.footer {
  padding: 16px 20px;
  text-align: center;
  color: #888;
  font-size: 12px;
  background-color: #ffffff;
  border-top: 1px solid #eaeaea;
}

/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 
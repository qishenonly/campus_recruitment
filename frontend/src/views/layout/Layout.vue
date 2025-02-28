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
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showDialog } from 'vant'

const router = useRouter()
// 从 localStorage 获取用户信息
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo')))

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
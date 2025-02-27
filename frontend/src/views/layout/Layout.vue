<template>
  <div class="layout-container">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-content">
        <div class="logo">青云直聘</div>
        <nav class="nav-menu">
          <router-link to="/home" class="nav-item" active-class="active">
            <span>首页</span>
          </router-link>
          <router-link to="/company" class="nav-item" active-class="active">
            <span>公司</span>
          </router-link>
          <router-link to="/message" class="nav-item" active-class="active">
            <span>消息</span>
          </router-link>
          <router-link to="/mine" class="nav-item" active-class="active">
            <span>我的</span>
          </router-link>
        </nav>
        <div class="auth-buttons">
          <router-link to="/login" class="login-btn">登录 / 注册</router-link>
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

// 临时使用的登录状态，后续需要替换为真实的用户状态管理
const isLoggedIn = ref(false)
const username = ref('')
const userAvatar = ref('')

const showUserMenu = () => {
  // TODO: 显示用户菜单
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
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  height: 100%;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: var(--primary-color);
  margin-right: 80px;  /* 增加与导航菜单的距离 */
}

.nav-menu {
  display: flex;
  gap: 40px;  /* 增加导航项之间的间距 */
}

.nav-item {
  position: relative;
  color: #666;
  text-decoration: none;
  font-size: 16px;
  padding: 8px 0;
}

.nav-item span {
  position: relative;
  z-index: 1;
}

.nav-item::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: var(--primary-color);
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.nav-item:hover {
  color: var(--primary-color);
}

.nav-item:hover::after {
  width: 100%;
}

.nav-item.active {
  color: var(--primary-color);
  font-weight: 500;
}

.nav-item.active::after {
  width: 100%;
}

.auth-buttons {
  margin-left: auto;
}

.login-btn {
  color: var(--primary-color);
  text-decoration: none;
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

@media (max-width: 768px) {
  .auth-buttons {
    display: none; /* 在移动端可以考虑其他展示方式 */
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
<template>
  <div class="auth-page">
    <div class="auth-container">
      <h2 class="title">登录青云直聘</h2>
      <p class="subtitle">找工作从这里开始</p>

      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="formData.phone"
            name="phone"
            label="手机号"
            placeholder="请输入手机号"
            :rules="[{ required: true, message: '请输入手机号' }]"
          />
          <van-field
            v-model="formData.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请输入密码' }]"
          />
        </van-cell-group>

        <div class="form-actions">
          <van-button round block type="primary" native-type="submit">
            登录
          </van-button>
        </div>

        <div class="auth-links">
          <router-link to="/register">注册新账号</router-link>
          <router-link to="/forgot-password">忘记密码？</router-link>
        </div>

        <div class="other-login">
          <div class="divider">
            <span>其他登录方式</span>
          </div>
          <div class="login-methods">
            <div class="login-method" @click="handleOtherLogin('wechat')">
              <van-icon name="wechat" color="#07c160" size="24" />
              <span>微信登录</span>
            </div>
          </div>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'

const router = useRouter()
const formData = ref({
  phone: '',
  password: ''
})

const onSubmit = async (values) => {
  try {
    // TODO: 调用登录 API
    console.log('登录表单:', values)
    showToast('登录成功')
    router.push('/')
  } catch (error) {
    showToast('登录失败')
  }
}

const handleOtherLogin = (type) => {
  showToast('暂未开放')
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  padding: 20px;
}

.auth-container {
  width: 100%;
  max-width: 400px;
  background: white;
  padding: 40px 30px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
}

.title {
  text-align: center;
  font-size: 24px;
  color: #333;
  margin: 0 0 8px;
}

.subtitle {
  text-align: center;
  color: #666;
  margin: 0 0 32px;
}

.form-actions {
  margin: 24px 0;
}

.auth-links {
  display: flex;
  justify-content: space-between;
  margin-bottom: 32px;
}

.auth-links a {
  color: var(--primary-color);
  text-decoration: none;
  font-size: 14px;
}

.divider {
  display: flex;
  align-items: center;
  margin: 24px 0;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #eee;
}

.divider span {
  padding: 0 16px;
  color: #999;
  font-size: 14px;
}

.login-methods {
  display: flex;
  justify-content: center;
  gap: 32px;
}

.login-method {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.login-method span {
  font-size: 12px;
  color: #666;
}
</style> 
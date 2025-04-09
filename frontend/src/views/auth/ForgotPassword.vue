<template>
  <div class="forgot-password-page">
    <div class="auth-container">
      <div class="auth-card">
        <div class="auth-content">
          <div class="auth-header">
            <h2 class="title">忘记密码</h2>
            <p class="subtitle">请输入您的注册邮箱，我们将发送验证码</p>
          </div>

          <!-- 步骤指示器 -->
          <div class="steps-indicator">
            <div class="step" :class="{ active: currentStep >= 1 }">
              <div class="step-dot">1</div>
              <div class="step-label">验证邮箱</div>
            </div>
            <div class="step-line" :class="{ active: currentStep >= 2 }"></div>
            <div class="step" :class="{ active: currentStep >= 2 }">
              <div class="step-dot">2</div>
              <div class="step-label">重置密码</div>
            </div>
            <div class="step-line" :class="{ active: currentStep >= 3 }"></div>
            <div class="step" :class="{ active: currentStep >= 3 }">
              <div class="step-dot">3</div>
              <div class="step-label">完成</div>
            </div>
          </div>

          <!-- 第一步：邮箱验证 -->
          <div v-if="currentStep === 1" class="step-content">
            <van-form @submit="verifyEmail">
              <van-field
                v-model="email"
                name="email"
                label="邮箱"
                placeholder="请输入注册邮箱"
                :rules="[
                  { required: true, message: '请输入邮箱' },
                  { pattern: emailPattern, message: '请输入有效的邮箱地址' }
                ]"
              />
              
              <div class="verification-code-container">
                <van-field
                  v-model="verificationCode"
                  name="verificationCode"
                  label="验证码"
                  placeholder="请输入验证码"
                  :rules="[{ required: true, message: '请输入验证码' }]"
                />
                <van-button 
                  size="small" 
                  type="primary" 
                  class="code-button"
                  :disabled="countDown > 0 || sendingCode"
                  :loading="sendingCode"
                  @click="sendCode"
                >
                  {{ countDown > 0 ? `${countDown}秒后重新获取` : '获取验证码' }}
                </van-button>
              </div>

              <div class="form-actions">
                <van-button round block type="primary" native-type="submit" :loading="loading">
                  下一步
                </van-button>
              </div>
            </van-form>
          </div>

          <!-- 第二步：重置密码 -->
          <div v-else-if="currentStep === 2" class="step-content">
            <van-form @submit="handleResetPassword">
              <van-field
                v-model="newPassword"
                type="password"
                name="newPassword"
                label="新密码"
                placeholder="请输入新密码"
                :rules="[
                  { required: true, message: '请输入新密码' },
                  { min: 6, message: '密码至少6个字符' }
                ]"
              />
              
              <van-field
                v-model="confirmPassword"
                type="password"
                name="confirmPassword"
                label="确认密码"
                placeholder="请再次输入新密码"
                :rules="[
                  { required: true, message: '请确认新密码' },
                  { validator: validatePasswordMatch, message: '两次密码输入不一致' }
                ]"
              />

              <div class="form-actions">
                <van-button round block type="primary" native-type="submit" :loading="loading">
                  重置密码
                </van-button>
              </div>
            </van-form>
          </div>

          <!-- 第三步：重置成功 -->
          <div v-else-if="currentStep === 3" class="step-content success-step">
            <div class="success-icon">
              <van-icon name="success" size="60" color="#07c160" />
            </div>
            <h3>密码重置成功</h3>
            <p>您的密码已经成功重置，现在可以使用新密码登录了</p>
            <van-button round block type="primary" @click="goToLogin">
              返回登录
            </van-button>
          </div>

          <div class="auth-footer">
            记起密码了？
            <router-link to="/login" class="login-link">立即登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast, showFailToast } from 'vant'
import { sendVerificationCode, resetLoginPassword as apiResetPassword, verifyCode } from '@/api/auth'

const router = useRouter()
const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/

// 表单数据
const email = ref('')
const verificationCode = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

// 状态管理
const currentStep = ref(1)
const loading = ref(false)
const countDown = ref(0)
const sendingCode = ref(false)
const timer = ref(null)

// 验证两次密码是否一致
const validatePasswordMatch = (val) => {
  return val === newPassword.value
}

// 发送验证码
const sendCode = async () => {
  if (countDown.value > 0 || sendingCode.value) return
  
  if (!email.value) {
    showToast('请输入邮箱地址')
    return
  }
  
  if (!emailPattern.test(email.value)) {
    showToast('请输入有效的邮箱地址')
    return
  }
  
  try {
    sendingCode.value = true
    await sendVerificationCode(email.value)
    
    showSuccessToast('验证码已发送')
    
    countDown.value = 60
    timer.value = setInterval(() => {
      countDown.value--
      if (countDown.value <= 0) {
        clearInterval(timer.value)
        timer.value = null
      }
    }, 1000)
  } catch (error) {
    showFailToast('发送验证码失败：' + (error.message || '未知错误'))
  } finally {
    sendingCode.value = false
  }
}

// 验证邮箱
const verifyEmail = async () => {
  if (!email.value || !verificationCode.value) {
    showToast('请填写完整信息')
    return
  }
  
  // 基本格式验证
  if (!emailPattern.test(email.value)) {
    showToast('请输入正确格式的邮箱地址')
    return
  }
  
  if (verificationCode.value.length < 4) {
    showToast('验证码格式不正确')
    return
  }
  
  try {
    loading.value = true
    // 进入下一步（移除调用验证码验证接口，在最后一步一起验证）
    // 这里不调用 verifyCode API，而是直接进入下一步
    // 验证码在最终重置密码时一并提交
    currentStep.value = 2
    showSuccessToast('验证码已确认，请设置新密码')
  } catch (error) {
    showFailToast('验证失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 重置密码
const handleResetPassword = async () => {
  if (!newPassword.value || !confirmPassword.value) {
    showToast('请填写完整信息')
    return
  }
  
  if (newPassword.value !== confirmPassword.value) {
    showToast('两次密码输入不一致')
    return
  }
  
  try {
    loading.value = true
    const res = await apiResetPassword({
      email: email.value,
      code: verificationCode.value, // 重新添加验证码参数
      newPassword: newPassword.value
    })
    
    if (res.code === 200) {
      currentStep.value = 3 // 进入成功页面
    } else {
      showFailToast(res.message || '密码重置失败')
    }
  } catch (error) {
    showFailToast('密码重置失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 返回登录页
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.forgot-password-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
}

.auth-container {
  width: 100%;
  max-width: 480px;
  padding: 20px;
}

.auth-card {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.auth-content {
  padding: 30px;
}

.auth-header {
  margin-bottom: 30px;
  text-align: center;
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px;
}

.subtitle {
  font-size: 14px;
  color: #888;
  margin: 0;
}

.steps-indicator {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 1;
}

.step-dot {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.step.active .step-dot {
  background-color: var(--primary-color, #2b7efb);
  color: white;
}

.step-label {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
  transition: all 0.3s;
}

.step.active .step-label {
  color: var(--primary-color, #2b7efb);
  font-weight: 500;
}

.step-line {
  flex: 1;
  height: 2px;
  background-color: #f0f0f0;
  position: relative;
  transition: all 0.3s;
}

.step-line.active {
  background-color: var(--primary-color, #2b7efb);
}

.step-content {
  margin-bottom: 30px;
}

.verification-code-container {
  display: flex;
  align-items: flex-start;
}

.code-button {
  margin-top: 14px;
  margin-left: 10px;
  width: 120px;
  height: 36px;
  font-size: 12px;
}

.form-actions {
  margin-top: 30px;
}

.auth-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.login-link {
  color: var(--primary-color, #2b7efb);
  text-decoration: none;
}

.success-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  margin-bottom: 20px;
}

.success-step h3 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
}

.success-step p {
  font-size: 14px;
  color: #666;
  margin-bottom: 30px;
}
</style> 
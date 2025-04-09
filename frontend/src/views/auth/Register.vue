<template>
  <div class="auth-page">
    <!-- 添加返回按钮 -->
    <div class="back-home">
      <van-icon name="arrow-left" @click="goHome" />
      <span @click="goHome">返回首页</span>
    </div>

    <div class="auth-background">
      <div class="auth-container">
        <div class="auth-form">
          <div class="auth-header">
            <img src="@/assets/logo.png" alt="logo" class="auth-logo" />
            <h2>创建账号</h2>
            <p>加入青云直聘，开启职业新篇章</p>
          </div>

          <van-form @submit="onSubmit" class="register-form">
            <van-field
              v-model="formData.email"
              name="email"
              placeholder="请输入邮箱"
              :rules="[
                { required: true, message: '请输入邮箱' },
                { pattern: emailPattern, message: '请输入正确的邮箱格式' }
              ]"
            >
              <template #prefix>
                <i class="iconfont icon-email"></i>
              </template>
            </van-field>

            <van-field
              v-model="formData.code"
              name="code"
              label="验证码"
              placeholder="请输入验证码"
              :rules="[{ required: true, message: '请输入验证码' }]"
            >
              <template #prefix>
                <i class="iconfont icon-verify"></i>
              </template>
              <template #button>
                <van-button
                  size="small"
                  type="primary"
                  class="send-code-btn"
                  :disabled="countdown > 0 || sendingCode"
                  :loading="sendingCode"
                  @click="sendCode"
                >
                  {{ countdown > 0 ? `${countdown}秒后重试` : '获取验证码' }}
                </van-button>
              </template>
            </van-field>

            <van-field
              v-model="formData.password"
              :type="showPassword ? 'text' : 'password'"
              name="password"
              placeholder="请输入密码"
              :rules="[
                { required: true, message: '请输入密码' },
                { pattern: passwordPattern, message: '密码必须包含字母和数字，长度8-20位' }
              ]"
            >
              <template #prefix>
                <i class="iconfont icon-password"></i>
              </template>
              <template #right-icon>
                <van-icon 
                  :name="showPassword ? 'eye' : 'closed-eye'"
                  @click="showPassword = !showPassword"
                  style="cursor: pointer;"
                />
              </template>
            </van-field>

            <van-field
              v-model="formData.confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'"
              name="confirmPassword"
              placeholder="请确认密码"
              :rules="[
                { required: true, message: '请确认密码' },
                { validator: validateConfirmPassword, message: '两次输入的密码不一致' }
              ]"
            >
              <template #prefix>
                <i class="iconfont icon-password"></i>
              </template>
              <template #right-icon>
                <van-icon 
                  :name="showConfirmPassword ? 'eye-o' : 'closed-eye'" 
                  @click="showConfirmPassword = !showConfirmPassword"
                />
              </template>
            </van-field>

            <van-field
              name="role"
              label="注册角色"
            >
              <template #input>
                <div class="role-selector">
                  <van-radio-group v-model="formData.role" direction="horizontal">
                    <van-radio name="STUDENT">学生</van-radio>
                    <van-radio name="COMPANY">企业员工</van-radio>
                  </van-radio-group>
                </div>
              </template>
            </van-field>

            <div class="agreement-checkbox">
              <van-checkbox v-model="formData.agreement" shape="square" icon-size="14px">
                <span class="agreement-text">
                  我已阅读并同意
                  <span class="agreement-link" @click.stop="showAgreement">《用户协议》</span>
                  和
                  <span class="agreement-link" @click.stop="showPrivacy">《隐私政策》</span>
                </span>
              </van-checkbox>
            </div>

            <div class="form-actions">
              <van-button 
                round 
                block 
                type="primary" 
                native-type="submit"
                :disabled="!formData.agreement"
              >
                注册
              </van-button>
            </div>
          </van-form>

          <div class="auth-footer">
            已有账号？
            <router-link to="/login" class="login-link">立即登录</router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- 用户协议弹窗 -->
    <van-dialog
      v-model:show="showAgreementDialog"
      title="用户协议"
      :show-confirm-button="true"
      confirm-button-text="我已阅读并同意"
      @confirm="handleAgreementConfirm"
      class="agreement-dialog"
    >
      <div class="agreement-content">
        <h3>青云直聘用户协议</h3>
        <p>欢迎您使用青云直聘！</p>
        <h4>1. 服务协议的范围</h4>
        <p>1.1 本协议是您与青云直聘平台之间关于使用青云直聘平台服务所订立的协议。</p>
        <p>1.2 本协议内容包括协议正文及所有青云直聘已经发布的或将来可能发布的各类规则。</p>
        
        <h4>2. 账号注册与安全</h4>
        <p>2.1 您承诺提供真实、准确、完整的注册信息。</p>
        <p>2.2 您应当妥善保管账号和密码，对账号下的所有行为负责。</p>
        
        <h4>3. 用户行为规范</h4>
        <p>3.1 遵守法律法规，不得发布违法违规信息。</p>
        <p>3.2 尊重知识产权，不得侵犯他人权益。</p>
        
        <h4>4. 服务内容</h4>
        <p>4.1 平台提供求职招聘信息发布、浏览等服务。</p>
        <p>4.2 平台有权对服务内容进行更新和调整。</p>
      </div>
    </van-dialog>

    <!-- 隐私政策弹窗 -->
    <van-dialog
      v-model:show="showPrivacyDialog"
      title="隐私政策"
      :show-confirm-button="true"
      confirm-button-text="我已阅读并同意"
      @confirm="handlePrivacyConfirm"
      class="privacy-dialog"
    >
      <div class="privacy-content">
        <h3>青云直聘隐私政策</h3>
        <p>我们重视您的隐私保护，承诺保护您的个人信息安全。</p>
        
        <h4>1. 信息收集</h4>
        <p>1.1 我们收集的信息包括：</p>
        <ul>
          <li>账号信息（邮箱、密码等）</li>
          <li>个人简历信息</li>
          <li>求职意向信息</li>
        </ul>
        
        <h4>2. 信息使用</h4>
        <p>2.1 我们使用收集的信息用于：</p>
        <ul>
          <li>提供求职招聘服务</li>
          <li>改善用户体验</li>
          <li>发送服务通知</li>
        </ul>
        
        <h4>3. 信息保护</h4>
        <p>3.1 我们采用业界标准的安全技术保护您的信息。</p>
        <p>3.2 未经您的同意，我们不会向第三方分享您的个人信息。</p>
        
        <h4>4. 信息管理</h4>
        <p>4.1 您有权查看、更正、删除您的个人信息。</p>
        <p>4.2 您可以通过账号设置管理隐私选项。</p>
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { register, sendVerificationCode } from '@/api/auth'

const router = useRouter()
const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
const passwordPattern = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,20}$/

const loading = ref(false)
const countdown = ref(0)
let timer = null

const formData = reactive({
  email: '',
  code: '',
  password: '',
  confirmPassword: '',
  role: 'STUDENT',
  agreement: false
})

const counting = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const showAgreementDialog = ref(false)
const showPrivacyDialog = ref(false)
const sendingCode = ref(false)

const validateConfirmPassword = (value) => {
  return value === formData.password
}

const sendCode = async () => {
  if (countdown.value > 0 || sendingCode.value) return
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!formData.email) {
    showToast('请输入邮箱')
    return
  }
  if (!emailRegex.test(formData.email)) {
    showToast('请输入正确的邮箱格式')
    return
  }

  try {
    sendingCode.value = true
    await sendVerificationCode(formData.email)
    showToast({
      message: '验证码已发送',
      type: 'success'
    })
    
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
    showToast({
      message: '发送验证码失败，请稍后再试',
      type: 'fail'
    })
  } finally {
    sendingCode.value = false
  }
}

const onSubmit = async () => {
  if (!formData.agreement) {
    showToast('请阅读并同意用户协议和隐私政策')
    return
  }
  
  if (formData.password !== formData.confirmPassword) {
    showToast('两次输入的密码不一致')
    return
  }
  
  try {
    loading.value = true
    await register({
      email: formData.email,
      code: formData.code,
      password: formData.password,
      role: formData.role
    })
    
    showToast({
      message: '注册成功',
      type: 'success'
    })
    
    // 跳转到登录页
    router.push('/login')
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}

const showAgreement = () => {
  showAgreementDialog.value = true
}

const showPrivacy = () => {
  showPrivacyDialog.value = true
}

const handleAgreementConfirm = () => {
  formData.agreement = true
}

const handlePrivacyConfirm = () => {
  formData.agreement = true
}

// 添加返回首页方法
const goHome = () => {
  router.push('/')
}

// 组件卸载时清除定时器
onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<style scoped>
/* 复用 Login.vue 的基础样式 */
.auth-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1890ff 0%, #1890ff 100%);
}

.auth-background {
  min-height: 100vh;
  background-image: url('@/assets/auth-bg.png');
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.auth-container {
  width: 100%;
  max-width: 440px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.auth-form {
  padding: 40px;
}

.auth-header {
  text-align: center;
  margin-bottom: 40px;
}

.auth-logo {
  width: 80px;
  height: 80px;
  margin-bottom: 16px;
}

.auth-header h2 {
  font-size: 24px;
  color: #333;
  margin: 0 0 8px;
}

.auth-header p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.register-form {
  margin-bottom: 24px;
}

.register-form :deep(.van-field) {
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
  padding: 12px 16px;
}

.register-form :deep(.van-field__control) {
  height: 24px;
}

.agreement-checkbox {
  margin: 16px 0;
}

.agreement-text {
  font-size: 12px;
  color: #666;
}

.agreement-link {
  color: var(--primary-color);
  cursor: pointer;
}

.form-actions {
  margin-bottom: 24px;
}

.auth-footer {
  text-align: center;
  color: #666;
  font-size: 14px;
}

.login-link {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
}

/* 图标样式 */
.iconfont {
  font-size: 20px;
}

@media (max-width: 768px) {
  .auth-container {
    margin: 20px;
  }

  .auth-form {
    padding: 30px 20px;
  }
}

.agreement-dialog :deep(.van-dialog__content),
.privacy-dialog :deep(.van-dialog__content) {
  max-height: 60vh;
  overflow-y: auto;
  padding: 20px;
}

.agreement-content,
.privacy-content {
  text-align: left;
  padding: 0 20px;
}

.agreement-content h3,
.privacy-content h3 {
  font-size: 18px;
  color: #333;
  margin: 0 0 16px 0;
}

.agreement-content h4,
.privacy-content h4 {
  font-size: 16px;
  color: #333;
  margin: 16px 0 8px;
}

.agreement-content p,
.privacy-content p {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
}

.privacy-content ul {
  padding-left: 20px;
  margin: 8px 0 16px;
}

.privacy-content li {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 8px;
  position: relative;
}

.agreement-content,
.privacy-content {
  scrollbar-width: thin;
  scrollbar-color: #ddd #f5f5f5;
}

.agreement-content::-webkit-scrollbar,
.privacy-content::-webkit-scrollbar {
  width: 6px;
}

.agreement-content::-webkit-scrollbar-track,
.privacy-content::-webkit-scrollbar-track {
  background: #f5f5f5;
}

.agreement-content::-webkit-scrollbar-thumb,
.privacy-content::-webkit-scrollbar-thumb {
  background-color: #ddd;
  border-radius: 3px;
}

:deep(.van-dialog) {
  width: 90%;
  max-width: 540px;
  border-radius: 8px;
}

:deep(.van-dialog__content) {
  padding: 20px 0;
  max-height: 60vh;
  overflow-y: auto;
}

:deep(.van-dialog__header) {
  padding: 20px;
  font-weight: 500;
  font-size: 18px;
  border-bottom: 1px solid #eee;
}

:deep(.van-dialog__footer) {
  padding: 16px 20px;
  border-top: 1px solid #eee;
}

:deep(.van-checkbox__label) {
  font-size: 12px;
  line-height: 14px;
}

/* 调整复选框大小 */
:deep(.van-checkbox__icon) {
  font-size: 14px;
  height: 14px;
  line-height: 14px;
}

.back-home {
  position: fixed;
  top: 20px;
  left: 20px;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 100;
  transition: all 0.3s;
}

.back-home:hover {
  background: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.back-home .van-icon {
  font-size: 16px;
  color: #666;
}

.back-home span {
  font-size: 14px;
  color: #666;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .back-home {
    top: 12px;
    left: 12px;
    padding: 6px 12px;
  }
}

.send-code-btn {
  height: 32px;
  padding: 0 12px;
  font-size: 12px;
}

.send-code-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.privacy-content a {
  color: var(--primary-color);
  text-decoration: none;
}

/* 角色选择器样式 */
.role-selector {
  display: flex;
  justify-content: flex-start;
  padding: 8px 0;
}

.role-selector .van-radio {
  margin-right: 20px;
}

.role-selector .van-radio__label {
  color: #333;
}
</style> 
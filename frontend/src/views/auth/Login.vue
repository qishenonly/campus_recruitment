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
            <h2>欢迎回来</h2>
            <p>登录青云直聘，发现更多机会</p>
          </div>

          <van-form @submit="onSubmit" class="login-form">
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
              v-model="formData.password"
              :type="showPassword ? 'text' : 'password'"
              name="password"
              placeholder="请输入密码"
              :rules="[{ required: true, message: '请输入密码' }]"
            >
              <template #prefix>
                <i class="iconfont icon-password"></i>
              </template>
              <template #right-icon>
                <van-icon 
                  :name="showPassword ? 'eye-o' : 'closed-eye'" 
                  @click="showPassword = !showPassword"
                />
              </template>
            </van-field>

            <div class="remember-checkbox">
              <van-checkbox v-model="formData.remember" shape="square" icon-size="14px">
                <span class="remember-text">记住我</span>
              </van-checkbox>
            </div>

            <div class="form-options">
              <router-link to="/forgot-password" class="forgot-link">忘记密码？</router-link>
            </div>

            <div class="form-actions">
              <van-button round block type="primary" native-type="submit">
                登录
              </van-button>
            </div>
          </van-form>

          <div class="auth-divider">
            <span>其他登录方式</span>
          </div>

          <div class="social-login">
            <button class="social-btn wechat" @click="handleSocialLogin('wechat')">
              <i class="iconfont icon-wechat"></i>
            </button>
            <button class="social-btn github" @click="handleSocialLogin('github')">
              <i class="iconfont icon-github"></i>
            </button>
          </div>

          <div class="auth-footer">
            还没有账号？
            <router-link to="/register" class="register-link">立即注册</router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- 完善信息弹窗 -->
    <complete-info-dialog
      v-model="showCompleteInfo"
      @complete="handleInfoComplete"
    />

    <!-- 完善企业信息弹窗 -->
    <complete-company-info-dialog
      v-model="showCompleteCompanyInfo"
      @complete="handleCompanyInfoComplete"
    />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { login } from '@/api/auth'
import { getUserProfile } from '@/api/user'
import { getCompanyInfo } from '@/api/company'
import { useDialogStore } from '@/stores/dialog'
import CompleteInfoDialog from './components/CompleteInfoDialog.vue'
import CompleteCompanyInfoDialog from './components/CompleteCompanyInfoDialog.vue'

const router = useRouter()
const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/

const loading = ref(false)
const showCompleteInfo = ref(false)
const showCompleteCompanyInfo = ref(false)
const dialogStore = useDialogStore()

const formData = reactive({
  email: '',
  password: '',
  remember: false
})

const showPassword = ref(false)

const onSubmit = async () => {
  try {
    loading.value = true
    const res = await login(formData)
    
    // 保存token和用户信息
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    
    showToast({
      message: '登录成功',
      type: 'success'
    })

    // 先跳转到首页
    await router.push('/home')

    // 根据用户角色检查是否需要完善信息
    if (res.data.role === 'STUDENT') {
      try {
        const profileRes = await getUserProfile()
        // 只有在获取不到用户资料时才显示完善信息弹窗
        if (!profileRes.data) {
          setTimeout(() => {
            dialogStore.showCompleteInfoDialog()
          }, 300)
        }
      } catch (error) {
        console.error('获取用户资料失败:', error)
        // 如果获取资料失败,也显示完善信息弹窗
        setTimeout(() => {
          dialogStore.showCompleteInfoDialog()
        }, 300)
      }
    } else if (res.data.role === 'COMPANY') {
      try {
        // 检查企业信息是否存在
        const companyRes = await getCompanyInfo()
        
        // 如果获取不到企业信息，显示完善企业信息弹窗
        if (!companyRes.data) {
          setTimeout(() => {
            dialogStore.showCompleteCompanyInfoDialog()
          }, 300)
        }
      } catch (error) {
        console.error('获取企业信息失败:', error)
        // 判断错误是否为企业不存在的情况
        if (
          (error.response && error.response.status === 404) || 
          (error.response && error.response.data && 
           (error.response.data.message.includes('找不到该用户关联的企业信息') || 
            error.response.data.message.includes('企业信息不存在')))
        ) {
          // 显示完善企业信息弹窗
          setTimeout(() => {
            dialogStore.showCompleteCompanyInfoDialog()
          }, 300)
        }
      }
    }
  } catch (error) {
    console.error('登录失败:', error)
    showToast({
      message: error.message || '登录失败',
      type: 'fail'
    })
  } finally {
    loading.value = false
  }
}

const handleInfoComplete = () => {
  showCompleteInfo.value = false
}

const handleCompanyInfoComplete = () => {
  showCompleteCompanyInfo.value = false
}

const handleSocialLogin = (type) => {
  showToast('暂未开放')
}

// 添加返回首页方法
const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
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

.login-form {
  margin-bottom: 24px;
}

.login-form :deep(.van-field) {
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
  padding: 12px 16px;
}

.login-form :deep(.van-field__control) {
  height: 24px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.forgot-link {
  color: var(--primary-color);
  font-size: 14px;
  text-decoration: none;
}

.form-actions {
  margin-bottom: 24px;
}

.auth-divider {
  position: relative;
  text-align: center;
  margin: 24px 0;
}

.auth-divider::before,
.auth-divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: calc(50% - 80px);
  height: 1px;
  background: #e8e8e8;
}

.auth-divider::before {
  left: 0;
}

.auth-divider::after {
  right: 0;
}

.auth-divider span {
  background: white;
  padding: 0 16px;
  color: #999;
  font-size: 14px;
}

.social-login {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 24px;
}

.social-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.social-btn.wechat {
  background: #07c160;
  color: white;
}

.social-btn.github {
  background: #24292e;
  color: white;
}

.social-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.auth-footer {
  text-align: center;
  color: #666;
  font-size: 14px;
}

.register-link {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
}

/* 图标样式 */
.iconfont {
  font-size: 20px;
}

.remember-checkbox {
  margin: 16px 0;
}

.remember-text {
  font-size: 12px;
  color: #666;
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

@media (max-width: 768px) {
  .auth-container {
    margin: 20px;
  }

  .auth-form {
    padding: 30px 20px;
  }
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
</style> 
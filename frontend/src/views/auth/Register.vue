<template>
  <div class="auth-page">
    <div class="auth-container">
      <h2 class="title">注册账号</h2>
      <p class="subtitle">加入青云直聘，开启职业新篇章</p>

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
            v-model="formData.code"
            name="code"
            label="验证码"
            placeholder="请输入验证码"
            :rules="[{ required: true, message: '请输入验证码' }]"
          >
            <template #button>
              <van-button 
                size="small" 
                type="primary" 
                :disabled="counting"
                @click="sendCode"
              >
                {{ counting ? `${counter}s` : '获取验证码' }}
              </van-button>
            </template>
          </van-field>
          <van-field
            v-model="formData.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请输入密码' }]"
          />
          <van-field
            v-model="formData.confirmPassword"
            type="password"
            name="confirmPassword"
            label="确认密码"
            placeholder="请再次输入密码"
            :rules="[
              { required: true, message: '请确认密码' },
              { validator: validateConfirmPassword, message: '两次输入的密码不一致' }
            ]"
          />
        </van-cell-group>

        <div class="agreement">
          <van-checkbox v-model="formData.agreement" shape="square">
            我已阅读并同意
            <a href="#" @click.prevent="showAgreement">《用户协议》</a>
            和
            <a href="#" @click.prevent="showPrivacy">《隐私政策》</a>
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

        <div class="auth-links">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
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
  code: '',
  password: '',
  confirmPassword: '',
  agreement: false
})

const counting = ref(false)
const counter = ref(60)

const validateConfirmPassword = (value) => {
  return value === formData.value.password
}

const sendCode = async () => {
  if (counting.value) return
  if (!formData.value.phone) {
    showToast('请先输入手机号')
    return
  }

  try {
    // TODO: 调用发送验证码 API
    counting.value = true
    counter.value = 60
    const timer = setInterval(() => {
      counter.value--
      if (counter.value <= 0) {
        clearInterval(timer)
        counting.value = false
      }
    }, 1000)
  } catch (error) {
    showToast('发送验证码失败')
  }
}

const onSubmit = async (values) => {
  try {
    // TODO: 调用注册 API
    console.log('注册表单:', values)
    showToast('注册成功')
    router.push('/login')
  } catch (error) {
    showToast('注册失败')
  }
}

const showAgreement = () => {
  // TODO: 显示用户协议
}

const showPrivacy = () => {
  // TODO: 显示隐私政策
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

.agreement {
  margin: 24px 16px;
}

.agreement a {
  color: var(--primary-color);
  text-decoration: none;
}

.form-actions {
  margin: 24px 0;
}

.auth-links {
  text-align: center;
  font-size: 14px;
}

.auth-links span {
  color: #666;
}

.auth-links a {
  color: var(--primary-color);
  text-decoration: none;
  margin-left: 4px;
}
</style> 
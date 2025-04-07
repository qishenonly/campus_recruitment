<template>
  <div class="login-container">
    <div class="login-form-container">
      <div class="login-header">
        <div class="logo-container">
          <h1 class="logo">青云直聘</h1>
          <p class="subtitle">管理后台</p>
        </div>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        class="login-form"
        @keyup.enter="handleLogin">
        
        <el-form-item label="账号" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入管理员账号"
            prefix-icon="i-ep-user"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="i-ep-lock"
            show-password
          />
        </el-form-item>
        
        <div class="remember-container">
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          <a href="javascript:;" class="forgot-password" @click="handleForgotPassword">忘记密码？</a>
        </div>
        
        <el-form-item class="login-btn-container">
          <el-button
            type="primary"
            :loading="loading"
            class="login-button"
            @click="handleLogin">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>© 2024 青云直聘校园招聘系统</p>
      </div>
    </div>
    
    <div class="login-background">
      <div class="login-banner">
        <h2 class="banner-title">校园招聘管理系统</h2>
        <p class="banner-desc">高效管理校园招聘流程，连接优质企业与人才</p>
        <div class="banner-image"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { adminLogin, getAdminInfo } from '../../api/admin';

const router = useRouter();
const loading = ref(false);
const rememberMe = ref(false);
const loginFormRef = ref(null);

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
});

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, max: 20, message: '长度在 4 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
};

// 处理登录
const handleLogin = () => {
  loginFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true;
      
      // 调用登录API
      adminLogin(loginForm).then(res => {
        // 存储登录信息和令牌
        localStorage.setItem('adminToken', res.data);
        
        if (rememberMe.value) {
          localStorage.setItem('adminRemember', JSON.stringify({
            username: loginForm.username,
            remember: true
          }));
        } else {
          localStorage.removeItem('adminRemember');
        }
        
        // 获取管理员信息
        return getAdminInfo();
      }).then(adminInfoRes => {
        // 存储管理员信息
        localStorage.setItem('adminInfo', JSON.stringify(adminInfoRes.data));
        
        // 登录成功提示和跳转
        ElMessage.success('登录成功');
        router.push('/admin/dashboard');
      }).catch(error => {
        console.error('登录失败:', error);
        ElMessage.error(error.message || '登录失败，请重试');
      }).finally(() => {
        loading.value = false;
      });
    }
  });
};

// 处理忘记密码
const handleForgotPassword = () => {
  ElMessage.info('请联系系统管理员重置密码');
};

// 页面加载时自动填充记住的用户名
onMounted(() => {
  const adminRemember = localStorage.getItem('adminRemember');
  if (adminRemember) {
    const { username, remember } = JSON.parse(adminRemember);
    loginForm.username = username;
    rememberMe.value = remember;
  }
  
  // 如果已登录，直接进入系统
  const adminToken = localStorage.getItem('adminToken');
  if (adminToken) {
    router.push('/admin/dashboard');
  }
});
</script>

<style scoped>
.login-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.login-form-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  max-width: 520px;
  padding: 0 40px;
  background-color: #ffffff;
}

.login-background {
  flex: 1.2;
  background-color: #1677FF;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.login-banner {
  max-width: 600px;
  padding: 40px;
  color: #ffffff;
  z-index: 2;
}

.banner-title {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 16px;
}

.banner-desc {
  font-size: 16px;
  opacity: 0.8;
  margin-bottom: 40px;
}

.banner-image {
  height: 320px;
  background-image: url('https://lf3-static.bytednsdoc.com/obj/eden-cn/ptlz_zlp/ljhwZthlaukjlkulzlp/root-web-sites/login-bg.png');
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}

.login-header {
  margin-bottom: 40px;
  text-align: center;
}

.logo-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo {
  color: #1677FF;
  font-size: 24px;
  margin: 0;
  font-weight: 600;
}

.subtitle {
  color: #666;
  font-size: 14px;
  margin: 8px 0 0 0;
}

.login-form {
  width: 100%;
  max-width: 360px;
}

.remember-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.forgot-password {
  font-size: 14px;
  color: #666;
  text-decoration: none;
}

.forgot-password:hover {
  color: #1677FF;
}

.login-button {
  width: 100%;
  height: 40px;
  font-size: 16px;
}

.login-btn-container {
  margin-top: 24px;
}

.login-footer {
  margin-top: 60px;
  text-align: center;
  color: #999;
  font-size: 12px;
}

/* 响应式设计 */
@media screen and (max-width: 992px) {
  .login-background {
    display: none;
  }
  
  .login-form-container {
    max-width: 100%;
  }
}
</style> 
<template>
  <div class="account-settings-container">
    <el-tabs v-model="activeTab" type="card">
      <el-tab-pane label="个人信息" name="profile">
        <!-- 个人资料卡片 -->
        <el-card class="settings-card">
          <el-form 
            :model="profileForm" 
            :rules="profileRules" 
            ref="profileFormRef"
            label-width="100px">

            <!-- 头像上传 -->
            <el-form-item label="头像">
              <el-upload
                class="avatar-uploader"
                :http-request="handleAvatarUpload"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload">
                <img 
                  v-if="profileForm.avatar" 
                  :src="getAvatarUrl(profileForm.avatar)" 
                  class="avatar" 
                  alt="用户头像" 
                  @error="handleAvatarError"
                />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">推荐尺寸: 200px × 200px，支持jpg、png格式</div>
            </el-form-item>

            <!-- 用户名 -->
            <el-form-item label="用户名" prop="username">
              <el-input v-model="profileForm.username" placeholder="请输入用户名"></el-input>
            </el-form-item>

            <!-- 昵称/姓名 -->
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="profileForm.realName" placeholder="请输入真实姓名"></el-input>
            </el-form-item>

            <!-- 邮箱 -->
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱"></el-input>
            </el-form-item>

            <!-- 手机号 -->
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号"></el-input>
            </el-form-item>

            <!-- 保存按钮 -->
            <el-form-item>
              <el-button type="primary" @click="saveProfile">保存</el-button>
              <el-button @click="resetProfile">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="修改密码" name="password">
        <!-- 修改密码卡片 -->
        <el-card class="settings-card">
          <el-form 
            :model="passwordForm" 
            :rules="passwordRules" 
            ref="passwordFormRef"
            label-width="120px">

            <!-- 原密码 -->
            <el-form-item label="原密码" prop="oldPassword">
              <el-input 
                v-model="passwordForm.oldPassword" 
                type="password" 
                placeholder="请输入原密码" 
                show-password></el-input>
            </el-form-item>

            <!-- 新密码 -->
            <el-form-item label="新密码" prop="newPassword">
              <el-input 
                v-model="passwordForm.newPassword" 
                type="password" 
                placeholder="请输入新密码" 
                show-password></el-input>
            </el-form-item>

            <!-- 确认新密码 -->
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input 
                v-model="passwordForm.confirmPassword" 
                type="password" 
                placeholder="请再次输入新密码" 
                show-password></el-input>
            </el-form-item>

            <!-- 保存按钮 -->
            <el-form-item>
              <el-button type="primary" @click="changeUserPassword">保存</el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="账号安全" name="security">
        <!-- 账号安全卡片 -->
        <el-card class="settings-card">
          <div class="security-list">
            <div class="security-item">
              <div class="security-info">
                <div class="security-title">账号密码</div>
                <div class="security-desc">当前密码强度: <span class="password-strength">中</span></div>
              </div>
              <el-button 
                size="small" 
                type="primary" 
                plain
                @click="activeTab = 'password'">
                修改
              </el-button>
            </div>

            <div class="security-item">
              <div class="security-info">
                <div class="security-title">手机绑定</div>
                <div class="security-desc">
                  {{ profileForm.phone ? `已绑定手机：${maskPhone(profileForm.phone)}` : '未绑定手机号' }}
                </div>
              </div>
              <el-button 
                size="small" 
                type="primary" 
                plain
                @click="showPhoneBindDialog">
                {{ profileForm.phone ? '修改' : '绑定' }}
              </el-button>
            </div>

            <div class="security-item">
              <div class="security-info">
                <div class="security-title">邮箱绑定</div>
                <div class="security-desc">
                  {{ profileForm.email ? `已绑定邮箱：${maskEmail(profileForm.email)}` : '未绑定邮箱' }}
                </div>
              </div>
              <el-button 
                size="small" 
                type="primary" 
                plain
                @click="showEmailBindDialog">
                {{ profileForm.email ? '修改' : '绑定' }}
              </el-button>
            </div>

            <div class="security-item">
              <div class="security-info">
                <div class="security-title">登录设备管理</div>
                <div class="security-desc">查看并管理您的登录设备</div>
              </div>
              <el-button 
                size="small" 
                type="primary" 
                plain
                @click="showDevices">
                查看
              </el-button>
            </div>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 绑定手机对话框 -->
    <el-dialog v-model="phoneDialogVisible" title="绑定手机" width="400px">
      <el-form :model="phoneForm" :rules="phoneRules" ref="phoneFormRef" label-width="80px">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="phoneForm.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="verify-code-input">
            <el-input v-model="phoneForm.code" placeholder="请输入验证码"></el-input>
            <el-button 
              type="primary" 
              :disabled="countdown > 0" 
              @click="sendPhoneVerifyCode">
              {{ countdown > 0 ? `${countdown}秒后重新获取` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="phoneDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPhoneForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 绑定邮箱对话框 -->
    <el-dialog v-model="emailDialogVisible" title="绑定邮箱" width="400px">
      <el-form :model="emailForm" :rules="emailRules" ref="emailFormRef" label-width="80px">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="emailForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="verify-code-input">
            <el-input v-model="emailForm.code" placeholder="请输入验证码"></el-input>
            <el-button 
              type="primary" 
              :disabled="emailCountdown > 0" 
              @click="sendEmailVerifyCode">
              {{ emailCountdown > 0 ? `${emailCountdown}秒后重新获取` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="emailDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEmailForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 设备管理对话框 -->
    <el-dialog v-model="devicesDialogVisible" title="登录设备管理" width="600px">
      <el-table :data="loginDevices" style="width: 100%">
        <el-table-column prop="deviceName" label="设备名称" width="180"></el-table-column>
        <el-table-column prop="ip" label="IP地址" width="120"></el-table-column>
        <el-table-column prop="location" label="登录地点"></el-table-column>
        <el-table-column prop="loginTime" label="登录时间" width="180"></el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button 
              type="danger" 
              size="small" 
              @click="removeDevice(scope.row.id)"
              :disabled="scope.row.current">
              {{ scope.row.current ? '当前设备' : '删除' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getUserProfile, 
  updateUserProfile, 
  uploadUserAvatar, 
  changePassword, 
  sendPhoneCode, 
  bindPhone, 
  sendEmailCode, 
  bindEmail 
} from '@/api/userSettings'

// 获取基础 API URL
const apiBaseUrl = import.meta.env.VITE_API_URL || '';

// 当前激活的标签页
const activeTab = ref('profile')

// 构建完整的头像URL
const getAvatarUrl = (avatarPath) => {
  if (!avatarPath) return '';
  
  // 如果头像路径已经是完整的URL，直接返回
  if (avatarPath.startsWith('http')) {
    return avatarPath;
  }
  
  // 确保路径以/开头
  const path = avatarPath.startsWith('/') ? avatarPath : '/' + avatarPath;
  return apiBaseUrl + path;
}

// 个人信息表单
const profileFormRef = ref(null)
const profileForm = ref({
  avatar: '',
  username: '',
  realName: '',
  email: '',
  phone: ''
})
const profileRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度应为3-20个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

// 修改密码表单
const passwordFormRef = ref(null)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
// 自定义密码验证规则
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入新密码'))
  } else {
    if (passwordForm.value.confirmPassword !== '') {
      if (!passwordFormRef.value) return
      passwordFormRef.value.validateField('confirmPassword', () => null)
    }
    callback()
  }
}
const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}
// 密码表单验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { validator: validatePass, trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validatePass2, trigger: 'blur' }
  ]
}

// 手机表单相关
const phoneDialogVisible = ref(false)
const phoneFormRef = ref(null)
const phoneForm = ref({
  phone: '',
  code: ''
})
const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}
const countdown = ref(0)
let timer = null

// 邮箱表单相关
const emailDialogVisible = ref(false)
const emailFormRef = ref(null)
const emailForm = ref({
  email: '',
  code: ''
})
const emailRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}
const emailCountdown = ref(0)
let emailTimer = null

// 设备管理相关
const devicesDialogVisible = ref(false)
const loginDevices = ref([
  {
    id: 1,
    deviceName: '苹果 iPhone 13',
    ip: '192.168.1.1',
    location: '北京',
    loginTime: '2023-04-01 10:30:45',
    current: true
  },
  {
    id: 2,
    deviceName: 'Chrome浏览器',
    ip: '172.16.10.2',
    location: '上海',
    loginTime: '2023-03-28 16:42:21',
    current: false
  }
])

// 获取用户信息
const fetchUserProfile = async () => {
  try {
    const res = await getUserProfile();
    console.log('获取用户信息响应:', res);
    
    if (res.code === 200) {
      const data = res.data;
      
      // 直接使用后端返回的头像URL，不做额外处理
      let avatarUrl = data.avatar || '';
      
      profileForm.value = {
        avatar: avatarUrl,
        username: data.username || '',
        realName: data.realName || '',
        email: data.email || '',
        phone: data.phone || ''
      };
      
      console.log('获取到的用户信息:', data);
      console.log('使用的头像URL:', avatarUrl);
      
      // 初始化绑定表单
      phoneForm.value.phone = data.phone || '';
      emailForm.value.email = data.email || '';
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    ElMessage.error('获取用户信息失败');
  }
}

// 保存个人信息
const saveProfile = async () => {
  try {
    await profileFormRef.value.validate()
    
    const res = await updateUserProfile(profileForm.value)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      
      // 更新本地存储的用户信息
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      userInfo.avatar = profileForm.value.avatar
      userInfo.username = profileForm.value.username
      userInfo.realName = profileForm.value.realName
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存个人信息失败:', error)
  }
}

// 重置个人信息
const resetProfile = () => {
  fetchUserProfile()
}

// 修改密码
const changeUserPassword = async () => {
  try {
    await passwordFormRef.value.validate()
    
    const res = await changePassword(passwordForm.value)
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      // 修改成功后清空表单
      passwordForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    } else {
      ElMessage.error(res.message || '密码修改失败')
    }
  } catch (error) {
    console.error('密码修改失败:', error)
  }
}

// 重置密码表单
const resetPasswordForm = () => {
  passwordFormRef.value.resetFields()
}

// 头像上传相关
const handleAvatarUpload = async (options) => {
  try {
    // 创建FormData
    const formData = new FormData();
    formData.append('file', options.file);
    
    // 调用API上传
    const res = await uploadUserAvatar(formData);
    console.log('上传头像API响应:', res);
    
    if (res.code === 200) {
      console.log('头像上传成功，返回数据:', res.data);
      
      // 获取正确的URL路径
      let avatarUrl = res.data.url;
      
      // 确保URL中包含/api前缀
      if (avatarUrl) {
        // 检查URL是否已经包含/api前缀
        if (!avatarUrl.startsWith('/api') && !avatarUrl.startsWith('http')) {
          if (avatarUrl.startsWith('/')) {
            avatarUrl = '/api' + avatarUrl;
          } else {
            avatarUrl = '/api/' + avatarUrl;
          }
        }
        
        // 如果URL以/api开头但不是完整URL，添加基础路径
        if (avatarUrl.startsWith('/api') && !avatarUrl.startsWith('http')) {
          const baseURL = import.meta.env.VITE_API_URL || '';
          // 如果基础URL已经包含/api，避免重复
          if (baseURL && baseURL.endsWith('/api')) {
            avatarUrl = baseURL + avatarUrl.substring(4); // 移除/api
          } else if (baseURL) {
            // 确保baseURL和avatarUrl之间没有重复的斜杠
            if (baseURL.endsWith('/') && avatarUrl.startsWith('/')) {
              avatarUrl = baseURL + avatarUrl.substring(1);
            } else if (!baseURL.endsWith('/') && !avatarUrl.startsWith('/')) {
              avatarUrl = baseURL + '/' + avatarUrl;
            } else {
              avatarUrl = baseURL + avatarUrl;
            }
          }
        }
      }
      
      console.log('最终使用的头像URL:', avatarUrl);
      profileForm.value.avatar = avatarUrl;
      ElMessage.success('头像上传成功');
      
      // 更新本地存储的用户信息
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
      userInfo.avatar = avatarUrl;
      localStorage.setItem('userInfo', JSON.stringify(userInfo));
      
      // 添加时间戳参数，防止浏览器缓存
      const img = document.querySelector('.avatar');
      if (img) {
        img.src = avatarUrl + '?t=' + new Date().getTime();
      }
    } else {
      console.error('头像上传失败，返回数据:', res);
      ElMessage.error(res.message || '头像上传失败');
    }
  } catch (error) {
    console.error('头像上传失败:', error);
    ElMessage.error('头像上传失败: ' + (error.message || '未知错误'));
  }
}

const beforeAvatarUpload = (file) => {
  console.log('准备上传的文件:', file);
  console.log('文件类型:', file.type);
  console.log('文件大小:', file.size / 1024 / 1024, 'MB');
  
  const isJPG = file.type === 'image/jpeg';
  const isPNG = file.type === 'image/png';
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPG && !isPNG) {
    ElMessage.error('上传头像图片只能是 JPG 或 PNG 格式!');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!');
    return false;
  }
  
  // 检查文件名是否有中文（有些服务器可能会有问题）
  if (/[\u4e00-\u9fa5]/.test(file.name)) {
    // 不直接阻止上传，但给出警告
    console.warn('文件名包含中文，可能在某些服务器上出现问题');
    ElMessage.warning('文件名包含中文，建议使用英文命名');
  }
  
  console.log('文件校验通过，准备上传');
  return true;
}

// 添加处理头像加载错误的方法
const handleAvatarError = (e) => {
  console.error('头像加载失败:', e);
  // 记录当前URL
  const currentUrl = e.target.src;
  console.log('加载失败的URL:', currentUrl);
  console.log('原始头像URL:', profileForm.value.avatar);
  
  // 直接使用默认头像，不再尝试修复
  e.target.src = '/default-avatar.png';
}

// 绑定手机号相关
const showPhoneBindDialog = () => {
  phoneForm.value.phone = profileForm.value.phone || ''
  phoneForm.value.code = ''
  phoneDialogVisible.value = true
}

// 发送手机验证码
const sendPhoneVerifyCode = () => {
  phoneFormRef.value.validateField('phone', (valid) => {
    if (valid) {
      sendPhoneCode(phoneForm.value.phone).then(res => {
        if (res.code === 200) {
          ElMessage.success(`验证码已发送至手机 ${maskPhone(phoneForm.value.phone)}`)
          countdown.value = 60
          timer = setInterval(() => {
            countdown.value--
            if (countdown.value <= 0) {
              clearInterval(timer)
            }
          }, 1000)
        } else {
          ElMessage.error(res.message || '验证码发送失败')
        }
      }).catch(error => {
        console.error('发送验证码失败:', error)
        ElMessage.error('验证码发送失败')
      })
    }
  })
}

// 提交手机绑定表单
const submitPhoneForm = async () => {
  try {
    await phoneFormRef.value.validate()
    
    const res = await bindPhone(phoneForm.value.phone, phoneForm.value.code)
    if (res.code === 200) {
      ElMessage.success('手机绑定成功')
      phoneDialogVisible.value = false
      profileForm.value.phone = phoneForm.value.phone
      
      // 更新本地存储的用户信息
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      userInfo.phone = phoneForm.value.phone
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    } else {
      ElMessage.error(res.message || '手机绑定失败')
    }
  } catch (error) {
    console.error('手机绑定失败:', error)
  }
}

// 绑定邮箱相关
const showEmailBindDialog = () => {
  emailForm.value.email = profileForm.value.email || ''
  emailForm.value.code = ''
  emailDialogVisible.value = true
}

// 发送邮箱验证码
const sendEmailVerifyCode = () => {
  emailFormRef.value.validateField('email', (valid) => {
    if (valid) {
      sendEmailCode(emailForm.value.email).then(res => {
        if (res.code === 200) {
          ElMessage.success(`验证码已发送至邮箱 ${maskEmail(emailForm.value.email)}`)
          emailCountdown.value = 60
          emailTimer = setInterval(() => {
            emailCountdown.value--
            if (emailCountdown.value <= 0) {
              clearInterval(emailTimer)
            }
          }, 1000)
        } else {
          ElMessage.error(res.message || '验证码发送失败')
        }
      }).catch(error => {
        console.error('发送验证码失败:', error)
        ElMessage.error('验证码发送失败')
      })
    }
  })
}

// 提交邮箱绑定表单
const submitEmailForm = async () => {
  try {
    await emailFormRef.value.validate()
    
    const res = await bindEmail(emailForm.value.email, emailForm.value.code)
    if (res.code === 200) {
      ElMessage.success('邮箱绑定成功')
      emailDialogVisible.value = false
      profileForm.value.email = emailForm.value.email
      
      // 更新本地存储的用户信息
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      userInfo.email = emailForm.value.email
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    } else {
      ElMessage.error(res.message || '邮箱绑定失败')
    }
  } catch (error) {
    console.error('邮箱绑定失败:', error)
  }
}

// 设备管理相关
const showDevices = () => {
  devicesDialogVisible.value = true
}

const removeDevice = (deviceId) => {
  ElMessageBox.confirm('确定要删除该登录设备吗？此操作将使该设备上的登录失效', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    loginDevices.value = loginDevices.value.filter(device => device.id !== deviceId)
    ElMessage.success('设备已删除')
  }).catch(() => {})
}

// 工具函数：隐藏部分手机号
const maskPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 工具函数：隐藏部分邮箱
const maskEmail = (email) => {
  if (!email) return ''
  const parts = email.split('@')
  if (parts.length !== 2) return email
  const name = parts[0]
  const domain = parts[1]
  const maskedName = name.length > 3 
    ? name.substring(0, 3) + '****' 
    : name + '****'
  return maskedName + '@' + domain
}

onMounted(() => {
  fetchUserProfile()
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
  if (emailTimer) {
    clearInterval(emailTimer)
  }
})
</script>

<style scoped>
.account-settings-container {
  padding: 20px;
}

.settings-card {
  margin-top: 20px;
  margin-bottom: 20px;
}

.avatar-uploader {
  width: 178px;
  height: 178px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.avatar[src=""] {
  display: none;
}

.avatar:hover {
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.1);
}

.upload-tip {
  color: #606266;
  font-size: 12px;
  margin-top: 8px;
}

.verify-code-input {
  display: flex;
}

.verify-code-input .el-button {
  margin-left: 10px;
  flex-shrink: 0;
}

.security-list {
  padding: 0 20px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.security-item:last-child {
  border-bottom: none;
}

.security-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
}

.security-desc {
  color: #606266;
  font-size: 14px;
}

.password-strength {
  color: #e6a23c;
  font-weight: bold;
}
</style> 
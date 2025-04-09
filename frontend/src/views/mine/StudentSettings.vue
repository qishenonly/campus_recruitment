<template>
  <div class="settings-container">
    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <span>账号设置</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <!-- 个人信息 -->
        <el-tab-pane label="基本信息" name="profile">
          <el-form 
            ref="profileForm$" 
            :model="profileForm" 
            :rules="profileRules" 
            label-width="80px"
            status-icon
          >
            <el-form-item label="头像" prop="avatar">
              <div class="avatar-wrapper">
                <el-avatar 
                  :size="80" 
                  :src="getAvatarUrl(profileForm.avatar) || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" 
                />
                <el-upload
                  class="avatar-uploader"
                  :show-file-list="false"
                  :http-request="uploadAvatar"
                  :before-upload="beforeAvatarUpload"
                >
                  <el-button type="primary">更换头像</el-button>
                </el-upload>
              </div>
            </el-form-item>
            
            <el-form-item label="用户名" prop="username">
              <el-input v-model="profileForm.username" />
            </el-form-item>
            
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="profileForm.realName" />
            </el-form-item>
            
            <el-form-item label="学校" prop="university">
              <el-input v-model="profileForm.university" />
            </el-form-item>
            
            <el-form-item label="专业" prop="major">
              <el-input v-model="profileForm.major" />
            </el-form-item>
            
            <el-form-item label="学历" prop="education">
              <el-select v-model="profileForm.education" placeholder="请选择学历" style="width: 100%">
                <el-option label="大专" value="大专" />
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="毕业年份" prop="graduationYear">
              <el-date-picker 
                v-model="profileForm.graduationYear" 
                type="year" 
                placeholder="选择毕业年份" 
                format="YYYY" 
                value-format="YYYY" 
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="所在地" prop="location">
              <el-input v-model="profileForm.location" />
            </el-form-item>
            
            <el-form-item label="个人介绍" prop="introducation">
              <el-input 
                v-model="profileForm.introducation" 
                type="textarea" 
                :rows="4" 
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="profileLoading">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 密码修改 -->
        <el-tab-pane label="密码修改" name="password">
          <el-form 
            ref="passwordForm$" 
            :model="passwordForm" 
            :rules="passwordRules" 
            label-width="120px"
            status-icon
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input 
                v-model="passwordForm.oldPassword" 
                type="password" 
                show-password
                placeholder="请输入原密码"
              />
            </el-form-item>
            
            <el-form-item label="新密码" prop="newPassword">
              <el-input 
                v-model="passwordForm.newPassword" 
                type="password" 
                show-password
                placeholder="请输入新密码"
              />
            </el-form-item>
            
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input 
                v-model="passwordForm.confirmPassword" 
                type="password" 
                show-password
                placeholder="请再次输入新密码"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="changeUserPassword" :loading="passwordLoading">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 账号安全 -->
        <el-tab-pane label="账号安全" name="security">
          <div class="security-container">
            <div class="security-item">
              <div class="security-info">
                <div class="security-title">手机绑定</div>
                <div class="security-desc" v-if="profileForm.phone">已绑定：{{ profileForm.phone }}</div>
                <div class="security-desc" v-else>未绑定手机号</div>
              </div>
              <el-button @click="showPhoneBindDialog" type="primary" plain>
                {{ profileForm.phone ? '修改' : '绑定' }}
              </el-button>
            </div>
            
            <div class="security-item">
              <div class="security-info">
                <div class="security-title">邮箱绑定</div>
                <div class="security-desc" v-if="profileForm.email">已绑定：{{ profileForm.email }}</div>
                <div class="security-desc" v-else>未绑定邮箱</div>
              </div>
              <el-button @click="showEmailBindDialog" type="primary" plain>
                {{ profileForm.email ? '修改' : '绑定' }}
              </el-button>
            </div>
          </div>
          
          <!-- 手机绑定对话框 -->
          <el-dialog
            title="手机绑定"
            v-model="phoneDialogVisible"
            width="400px"
          >
            <el-form 
              ref="phoneForm$" 
              :model="phoneForm" 
              :rules="phoneRules" 
              label-width="80px"
              status-icon
            >
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="phoneForm.phone" placeholder="请输入手机号">
                  <template #append>
                    <el-button 
                      @click="sendPhoneVerifyCode" 
                      :disabled="phoneCodeTimer > 0"
                      :loading="sendingPhoneCode"
                    >
                      {{ phoneCodeTimer > 0 ? `${phoneCodeTimer}秒后重发` : '获取验证码' }}
                    </el-button>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item label="验证码" prop="code">
                <el-input v-model="phoneForm.code" placeholder="请输入验证码" />
              </el-form-item>
            </el-form>
            <template #footer>
              <div class="dialog-footer">
                <el-button @click="phoneDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleBindPhone" :loading="bindingPhone">
                  确认
                </el-button>
              </div>
            </template>
          </el-dialog>
          
          <!-- 邮箱绑定对话框 -->
          <el-dialog
            title="邮箱绑定"
            v-model="emailDialogVisible"
            width="400px"
          >
            <el-form 
              ref="emailForm$" 
              :model="emailForm" 
              :rules="emailRules" 
              label-width="80px"
              status-icon
            >
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="emailForm.email" placeholder="请输入邮箱">
                  <template #append>
                    <el-button 
                      @click="sendEmailVerifyCode" 
                      :disabled="emailCodeTimer > 0"
                      :loading="sendingEmailCode"
                    >
                      {{ emailCodeTimer > 0 ? `${emailCodeTimer}秒后重发` : '获取验证码' }}
                    </el-button>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item label="验证码" prop="code">
                <el-input v-model="emailForm.code" placeholder="请输入验证码" />
              </el-form-item>
            </el-form>
            <template #footer>
              <div class="dialog-footer">
                <el-button @click="emailDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleBindEmail" :loading="bindingEmail">
                  确认
                </el-button>
              </div>
            </template>
          </el-dialog>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getStudentProfile,
  updateStudentProfile,
  uploadStudentAvatar,
  changeStudentPassword,
  sendStudentPhoneCode,
  bindStudentPhone,
  sendStudentEmailCode,
  bindStudentEmail
} from '@/api/studentSettings'

// 获取API基础URL
const apiBaseUrl = import.meta.env.VITE_API_URL || '';

// 构建头像URL的方法
const getAvatarUrl = (avatar) => {
  if (!avatar) return '';
  
  // 如果已经是完整URL，直接返回
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar;
  }
  
  // 确保URL有正确的格式
  if (avatar.startsWith('/api/')) {
    return apiBaseUrl + avatar.substring(4); // 去掉/api前缀，因为apiBaseUrl中可能已经包含了它
  } else if (avatar.startsWith('/')) {
    return apiBaseUrl + avatar;
  } else {
    return apiBaseUrl + '/' + avatar;
  }
}

// 当前激活的标签页
const activeTab = ref('profile')

// 个人信息表单
const profileForm = reactive({
  id: '',
  username: '',
  realName: '',
  avatar: '',
  email: '',
  phone: '',
  university: '',
  major: '',
  education: '',
  graduationYear: '',
  location: '',
  introducation: ''
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 手机绑定表单
const phoneForm = reactive({
  phone: '',
  code: ''
})

// 邮箱绑定表单
const emailForm = reactive({
  email: '',
  code: ''
})

// 表单引用
const profileForm$ = ref(null)
const passwordForm$ = ref(null)
const phoneForm$ = ref(null)
const emailForm$ = ref(null)

// 加载状态
const profileLoading = ref(false)
const passwordLoading = ref(false)
const sendingPhoneCode = ref(false)
const bindingPhone = ref(false)
const sendingEmailCode = ref(false)
const bindingEmail = ref(false)

// 对话框显示状态
const phoneDialogVisible = ref(false)
const emailDialogVisible = ref(false)

// 验证码计时器
const phoneCodeTimer = ref(0)
const emailCodeTimer = ref(0)
let phoneInterval = null
let emailInterval = null

// 验证规则
const profileRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应为3-20个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  university: [
    { required: true, message: '请输入学校名称', trigger: 'blur' }
  ],
  major: [
    { required: true, message: '请输入专业名称', trigger: 'blur' }
  ],
  education: [
    { required: true, message: '请选择学历', trigger: 'change' }
  ],
  graduationYear: [
    { required: true, message: '请选择毕业年份', trigger: 'change' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码必须为4位数字', trigger: 'blur' }
  ]
}

const emailRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码必须为4位数字', trigger: 'blur' }
  ]
}

// 获取用户信息
const fetchUserProfile = async () => {
  try {
    console.log('开始获取学生个人信息...');
    const res = await getStudentProfile();
    console.log('获取到的原始数据:', res);
    
    if (res.code === 200) {
      // 清空原有表单数据
      Object.keys(profileForm).forEach(key => {
        profileForm[key] = '';
      });
      
      // 确保数据存在
      if (res.data) {
        // 直接赋值API返回的数据
        Object.keys(res.data).forEach(key => {
          if (key in profileForm && res.data[key] !== null) {
            profileForm[key] = res.data[key];
          }
        });
        
        // 修复可能的undefined值
        Object.keys(profileForm).forEach(key => {
          if (profileForm[key] === undefined || profileForm[key] === null) {
            profileForm[key] = '';
          }
        });
      }
      
      console.log('处理后的表单数据:', JSON.stringify(profileForm, null, 2));
    } else {
      ElMessage.error(res.message || '获取用户信息失败');
    }
  } catch (error) {
    console.error('获取用户信息出错', error);
    ElMessage.error('获取用户信息失败: ' + (error.message || '未知错误'));
  }
}

// 保存个人信息
const saveProfile = async () => {
  try {
    // 检查表单引用是否正确获取到
    if (!profileForm$.value) {
      console.error('表单引用未正确获取');
      ElMessage.error('表单引用错误，请刷新页面重试');
      return;
    }
    
    console.log('开始验证表单...');
    const valid = await profileForm$.value.validate();
    console.log('表单验证结果:', valid);
    
    // 准备提交的数据，复制一份避免直接修改表单数据
    const submitData = { ...profileForm };
    
    // 处理空字符串和默认值
    Object.keys(submitData).forEach(key => {
      if (submitData[key] === '') {
        submitData[key] = null;
      }
    });
    
    // 打印将要提交的数据
    console.log('提交的个人信息数据:', submitData);
    
    profileLoading.value = true;
    try {
      const res = await updateStudentProfile(submitData);
      console.log('更新个人信息响应:', res);
      
      if (res.code === 200) {
        ElMessage.success('个人信息更新成功');
        
        // 更新本地存储的用户信息
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
        userInfo.username = profileForm.username;
        if (profileForm.avatar) {
          userInfo.avatar = profileForm.avatar;
        }
        localStorage.setItem('userInfo', JSON.stringify(userInfo));
      } else {
        ElMessage.error(res.message || '个人信息更新失败');
      }
    } catch (err) {
      console.error('API调用出错:', err);
      ElMessage.error('个人信息更新失败: ' + (err.message || '未知错误'));
    } finally {
      profileLoading.value = false;
    }
  } catch (error) {
    console.error('保存个人信息出错', error);
    if (error?.message) {
      ElMessage.error(error.message);
    } else {
      ElMessage.error('个人信息更新失败');
    }
  }
}

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

// 上传头像
const uploadAvatar = async (options) => {
  try {
    console.log('开始上传头像:', options.file);
    const formData = new FormData()
    formData.append('file', options.file)
    
    const res = await uploadStudentAvatar(formData)
    console.log('头像上传响应:', res);
    
    if (res.code === 200) {
      if (res.data && res.data.url) {
        // 使用getAvatarUrl方法处理头像URL
        const avatarUrl = res.data.url;
        const fullAvatarUrl = getAvatarUrl(avatarUrl);
        
        console.log('最终使用的头像URL:', fullAvatarUrl);
        profileForm.avatar = avatarUrl; // 保存相对路径到表单
        ElMessage.success('头像上传成功');
        
        // 更新本地存储的用户信息
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
        userInfo.avatar = avatarUrl;
        localStorage.setItem('userInfo', JSON.stringify(userInfo));
        
        // 添加时间戳参数，防止浏览器缓存
        const img = document.querySelector('.avatar');
        if (img) {
          img.src = fullAvatarUrl + '?t=' + new Date().getTime();
        }
      } else {
        ElMessage.warning('服务器返回的头像URL为空')
      }
    } else {
      ElMessage.error(res.message || '头像上传失败')
    }
  } catch (error) {
    console.error('上传头像出错', error)
    ElMessage.error('头像上传失败: ' + (error.message || '未知错误'))
  }
}

// 修改密码
const changeUserPassword = async () => {
  try {
    await passwordForm$.value.validate()
    
    passwordLoading.value = true
    const res = await changeStudentPassword(passwordForm)
    passwordLoading.value = false
    
    if (res.code === 200) {
      ElMessage.success('密码修改成功')
      // 清空表单
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } else {
      ElMessage.error(res.message || '密码修改失败')
    }
  } catch (error) {
    passwordLoading.value = false
    console.error('修改密码出错', error)
    if (error?.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('密码修改失败')
    }
  }
}

// 显示手机绑定对话框
const showPhoneBindDialog = () => {
  // 初始化表单
  phoneForm.phone = profileForm.phone || ''
  phoneForm.code = ''
  phoneDialogVisible.value = true
}

// 显示邮箱绑定对话框
const showEmailBindDialog = () => {
  // 初始化表单
  emailForm.email = profileForm.email || ''
  emailForm.code = ''
  emailDialogVisible.value = true
}

// 发送手机验证码
const sendPhoneVerifyCode = async () => {
  try {
    await phoneForm$.value.validateField('phone')
    
    sendingPhoneCode.value = true
    const res = await sendStudentPhoneCode(phoneForm.phone)
    sendingPhoneCode.value = false
    
    if (res.code === 200) {
      ElMessage.success('验证码发送成功')
      // 开始倒计时
      phoneCodeTimer.value = 60
      phoneInterval = setInterval(() => {
        if (phoneCodeTimer.value > 0) {
          phoneCodeTimer.value--
        } else {
          clearInterval(phoneInterval)
          phoneInterval = null
        }
      }, 1000)
    } else {
      ElMessage.error(res.message || '验证码发送失败')
    }
  } catch (error) {
    sendingPhoneCode.value = false
    console.error('发送验证码出错', error)
    if (error?.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('验证码发送失败')
    }
  }
}

// 发送邮箱验证码
const sendEmailVerifyCode = async () => {
  try {
    await emailForm$.value.validateField('email')
    
    sendingEmailCode.value = true
    const res = await sendStudentEmailCode(emailForm.email)
    sendingEmailCode.value = false
    
    if (res.code === 200) {
      ElMessage.success('验证码发送成功')
      // 开始倒计时
      emailCodeTimer.value = 60
      emailInterval = setInterval(() => {
        if (emailCodeTimer.value > 0) {
          emailCodeTimer.value--
        } else {
          clearInterval(emailInterval)
          emailInterval = null
        }
      }, 1000)
    } else {
      ElMessage.error(res.message || '验证码发送失败')
    }
  } catch (error) {
    sendingEmailCode.value = false
    console.error('发送验证码出错', error)
    if (error?.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('验证码发送失败')
    }
  }
}

// 绑定手机
const handleBindPhone = async () => {
  try {
    await phoneForm$.value.validate()
    
    bindingPhone.value = true
    const res = await bindStudentPhone(phoneForm.phone, phoneForm.code)
    bindingPhone.value = false
    
    if (res.code === 200) {
      ElMessage.success('手机绑定成功')
      profileForm.phone = phoneForm.phone
      phoneDialogVisible.value = false
      
      // 更新本地存储的用户信息
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      userInfo.phone = profileForm.phone
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    } else {
      ElMessage.error(res.message || '手机绑定失败')
    }
  } catch (error) {
    bindingPhone.value = false
    console.error('绑定手机出错', error)
    if (error?.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('手机绑定失败')
    }
  }
}

// 绑定邮箱
const handleBindEmail = async () => {
  try {
    await emailForm$.value.validate()
    
    bindingEmail.value = true
    const res = await bindStudentEmail(emailForm.email, emailForm.code)
    bindingEmail.value = false
    
    if (res.code === 200) {
      ElMessage.success('邮箱绑定成功')
      profileForm.email = emailForm.email
      emailDialogVisible.value = false
      
      // 更新本地存储的用户信息
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      userInfo.email = profileForm.email
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    } else {
      ElMessage.error(res.message || '邮箱绑定失败')
    }
  } catch (error) {
    bindingEmail.value = false
    console.error('绑定邮箱出错', error)
    if (error?.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('邮箱绑定失败')
    }
  }
}

// 生命周期钩子
onMounted(() => {
  console.log('学生账号设置页面已挂载');
  console.log('当前API地址前缀:', import.meta.env.VITE_API_URL || '默认API前缀');
  
  // 获取个人信息数据
  fetchUserProfile().then(() => {
    // 在数据获取并处理完成后，检查表单实例是否正确获取
    nextTick(() => {
      console.log('DOM更新完成，表单引用情况:');
      console.log('profileForm$ 引用:', profileForm$.value ? '已获取' : '未获取');
      console.log('passwordForm$ 引用:', passwordForm$.value ? '已获取' : '未获取');
      console.log('phoneForm$ 引用:', phoneForm$.value ? '已获取' : '未获取');
      console.log('emailForm$ 引用:', emailForm$.value ? '已获取' : '未获取');
    });
  });
})

onBeforeUnmount(() => {
  // 清除计时器
  if (phoneInterval) {
    clearInterval(phoneInterval)
  }
  if (emailInterval) {
    clearInterval(emailInterval)
  }
})
</script>

<style scoped>
.settings-container {
  padding: 20px;
}

.settings-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 20px;
}

.security-container {
  padding: 10px 0;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 10px;
  border-bottom: 1px solid #f0f0f0;
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
  color: #909399;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 
<template>
  <div class="settings-container">
    <el-card class="page-header-card">
      <div class="page-header">
        <div class="page-title">
          <h2>系统设置</h2>
        </div>
        <div class="page-actions">
          <el-button type="primary" @click="saveAllSettings">
            <el-icon><i-ep-check /></el-icon>保存所有设置
          </el-button>
        </div>
      </div>
    </el-card>

    <el-tabs v-model="activeTab" type="border-card" class="settings-tabs">
      <!-- 基本设置 -->
      <el-tab-pane label="基本设置" name="basic">
        <el-form :model="basicSettings" label-width="120px" class="settings-form">
          <el-form-item label="系统名称">
            <el-input v-model="basicSettings.systemName" placeholder="输入系统名称" />
          </el-form-item>
          
          <el-form-item label="系统描述">
            <el-input v-model="basicSettings.systemDesc" type="textarea" :rows="3" placeholder="输入系统描述" />
          </el-form-item>
          
          <el-form-item label="系统LOGO">
            <el-upload
              class="logo-uploader"
              action="#"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleLogoChange">
              <img v-if="basicSettings.logoUrl" :src="basicSettings.logoUrl" class="logo-preview" />
              <el-icon v-else class="logo-uploader-icon"><i-ep-plus /></el-icon>
            </el-upload>
            <div class="upload-hint">建议尺寸: 192px × 192px</div>
          </el-form-item>
          
          <el-form-item label="管理员邮箱">
            <el-input v-model="basicSettings.adminEmail" placeholder="管理员联系邮箱" />
          </el-form-item>
          
          <el-form-item label="技术支持电话">
            <el-input v-model="basicSettings.supportPhone" placeholder="技术支持电话" />
          </el-form-item>
          
          <el-divider />
          
          <el-form-item label="系统公告">
            <el-switch v-model="basicSettings.enableAnnouncement" />
          </el-form-item>
          
          <el-form-item label="公告内容" v-if="basicSettings.enableAnnouncement">
            <el-input v-model="basicSettings.announcement" type="textarea" :rows="4" placeholder="输入公告内容" />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="saveBasicSettings">保存基本设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      
      <!-- 注册设置 -->
      <el-tab-pane label="注册设置" name="register">
        <el-form :model="registerSettings" label-width="180px" class="settings-form">
          <el-form-item label="开放注册">
            <el-switch v-model="registerSettings.enableRegister" />
          </el-form-item>
          
          <el-form-item label="学生注册">
            <el-switch v-model="registerSettings.allowStudentRegister" />
          </el-form-item>
          
          <el-form-item label="企业注册">
            <el-switch v-model="registerSettings.allowCompanyRegister" />
          </el-form-item>
          
          <el-form-item label="企业注册需要审核">
            <el-switch v-model="registerSettings.companyNeedVerify" />
          </el-form-item>
          
          <el-form-item label="学生邮箱后缀限制">
            <el-switch v-model="registerSettings.restrictStudentEmail" />
          </el-form-item>
          
          <el-form-item label="允许的邮箱后缀" v-if="registerSettings.restrictStudentEmail">
            <el-select
              v-model="registerSettings.allowedEmailDomains"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="请输入允许的邮箱后缀，如edu.cn">
              <el-option
                v-for="item in registerSettings.allowedEmailDomains"
                :key="item"
                :label="item"
                :value="item">
              </el-option>
            </el-select>
            <div class="form-hint">添加后缀时不需要包含@符号</div>
          </el-form-item>
          
          <el-divider />
          
          <el-form-item label="密码最小长度">
            <el-input-number v-model="registerSettings.passwordMinLength" :min="6" :max="20" />
          </el-form-item>
          
          <el-form-item label="密码复杂度要求">
            <el-checkbox-group v-model="registerSettings.passwordComplexity">
              <el-checkbox label="uppercase">必须包含大写字母</el-checkbox>
              <el-checkbox label="lowercase">必须包含小写字母</el-checkbox>
              <el-checkbox label="number">必须包含数字</el-checkbox>
              <el-checkbox label="special">必须包含特殊字符</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="saveRegisterSettings">保存注册设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      
      <!-- 安全设置 -->
      <el-tab-pane label="安全设置" name="security">
        <el-form :model="securitySettings" label-width="180px" class="settings-form">
          <el-form-item label="登录失败锁定">
            <el-switch v-model="securitySettings.enableLoginLock" />
          </el-form-item>
          
          <el-form-item label="失败尝试次数" v-if="securitySettings.enableLoginLock">
            <el-input-number v-model="securitySettings.maxLoginAttempts" :min="3" :max="10" />
          </el-form-item>
          
          <el-form-item label="锁定时间(分钟)" v-if="securitySettings.enableLoginLock">
            <el-input-number v-model="securitySettings.lockDuration" :min="5" :max="60" />
          </el-form-item>
          
          <el-divider />
          
          <el-form-item label="管理员IP白名单">
            <el-switch v-model="securitySettings.enableIpWhitelist" />
          </el-form-item>
          
          <el-form-item label="允许的IP地址" v-if="securitySettings.enableIpWhitelist">
            <el-select
              v-model="securitySettings.allowedIps"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="请输入允许访问的IP地址">
              <el-option
                v-for="item in securitySettings.allowedIps"
                :key="item"
                :label="item"
                :value="item">
              </el-option>
            </el-select>
          </el-form-item>
          
          <el-divider />
          
          <el-form-item label="会话超时时间(分钟)">
            <el-input-number v-model="securitySettings.sessionTimeout" :min="10" :max="1440" />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="saveSecuritySettings">保存安全设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      
      <!-- 邮件设置 -->
      <el-tab-pane label="邮件设置" name="mail">
        <el-form :model="mailSettings" label-width="180px" class="settings-form">
          <el-form-item label="启用邮件服务">
            <el-switch v-model="mailSettings.enableMail" />
          </el-form-item>
          
          <template v-if="mailSettings.enableMail">
            <el-form-item label="SMTP服务器">
              <el-input v-model="mailSettings.host" placeholder="例如: smtp.example.com" />
            </el-form-item>
            
            <el-form-item label="SMTP端口">
              <el-input-number v-model="mailSettings.port" :min="1" :max="65535" />
              <div class="form-hint">常用端口: 25, 465(SSL), 587(TLS)</div>
            </el-form-item>
            
            <el-form-item label="使用SSL/TLS">
              <el-switch v-model="mailSettings.ssl" />
            </el-form-item>
            
            <el-form-item label="邮箱账号">
              <el-input v-model="mailSettings.username" placeholder="请输入邮箱账号" />
            </el-form-item>
            
            <el-form-item label="邮箱密码">
              <el-input v-model="mailSettings.password" type="password" placeholder="请输入邮箱密码" show-password />
            </el-form-item>
            
            <el-form-item label="发件人邮箱">
              <el-input v-model="mailSettings.from" placeholder="例如: noreply@example.com" />
            </el-form-item>
            
            <el-divider />
            
            <el-form-item label="测试邮件">
              <div class="test-mail-wrapper">
                <el-input v-model="testMailAddress" placeholder="请输入接收测试邮件的邮箱地址" />
                <el-button type="primary" @click="handleSendTestMail" :loading="sendingTestMail">
                  <el-icon><i-ep-message /></el-icon>发送测试邮件
                </el-button>
              </div>
            </el-form-item>
          </template>
          
          <el-form-item>
            <el-button type="primary" @click="saveMailSettings">保存邮件设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      
      <!-- 系统维护 -->
      <el-tab-pane label="系统维护" name="maintenance">
        <el-card class="warning-card">
          <el-icon><i-ep-warning /></el-icon>
          <span>以下操作可能会影响系统正常运行，请谨慎操作！</span>
        </el-card>
        
        <div class="maintenance-actions">
          <el-card class="action-card">
            <template #header>
              <div class="action-header">
                <h3>数据备份</h3>
              </div>
            </template>
            <div class="action-content">
              <p>创建系统数据的完整备份，包括用户数据、职位信息等。</p>
              <el-button type="primary" @click="handleBackup">
                <el-icon><i-ep-download /></el-icon>创建备份
              </el-button>
            </div>
          </el-card>
          
          <el-card class="action-card">
            <template #header>
              <div class="action-header">
                <h3>缓存清理</h3>
              </div>
            </template>
            <div class="action-content">
              <p>清理系统缓存，可能会暂时影响系统性能，但有助于解决一些异常问题。</p>
              <el-button type="warning" @click="handleClearCache">
                <el-icon><i-ep-delete /></el-icon>清理缓存
              </el-button>
            </div>
          </el-card>
          
          <el-card class="action-card">
            <template #header>
              <div class="action-header">
                <h3>系统维护模式</h3>
              </div>
            </template>
            <div class="action-content">
              <p>开启维护模式后，普通用户将无法访问系统，仅管理员可登录。</p>
              <el-switch
                v-model="maintenanceSettings.maintenanceMode"
                active-text="维护模式已开启"
                inactive-text="维护模式已关闭"
                @change="handleMaintenanceToggle"
              />
            </div>
          </el-card>
        </div>
        
        <el-divider content-position="center">危险区域</el-divider>
        
        <el-card class="danger-card">
          <template #header>
            <div class="danger-header">
              <h3>重置系统</h3>
            </div>
          </template>
          <div class="danger-content">
            <p>此操作将会清除所有系统数据，并将系统恢复到初始状态。此操作不可逆，请谨慎操作！</p>
            <el-button type="danger" @click="handleResetSystem">
              <el-icon><i-ep-delete /></el-icon>重置系统
            </el-button>
          </div>
        </el-card>
      </el-tab-pane>
      
      <!-- 个人设置 -->
      <el-tab-pane label="个人设置" name="personal">
        <el-form :model="personalSettings" label-width="120px" class="settings-form">
          <el-form-item label="管理员账号">
            <el-input v-model="personalSettings.username" disabled />
          </el-form-item>
          
          <el-form-item label="姓名">
            <el-input v-model="personalSettings.realName" placeholder="输入您的真实姓名" />
          </el-form-item>
          
          <el-form-item label="邮箱">
            <el-input v-model="personalSettings.email" placeholder="输入您的联系邮箱" />
          </el-form-item>
          
          <el-form-item label="手机号">
            <el-input v-model="personalSettings.phone" placeholder="输入您的联系电话" />
          </el-form-item>
          
          <el-divider />
          
          <el-form-item label="修改密码">
            <el-button type="primary" @click="showChangePasswordDialog = true">修改密码</el-button>
          </el-form-item>
          
          <el-form-item label="两步验证">
            <el-switch v-model="personalSettings.twoFactorAuth" @change="toggleTwoFactorAuth" />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="savePersonalSettings">保存个人设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="showChangePasswordDialog"
      title="修改密码"
      width="500px"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="120px">
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input
            v-model="passwordForm.currentPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="changePassword" :loading="changingPassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  getBasicSettings, 
  updateBasicSettings, 
  uploadLogo, 
  getRegisterSettings, 
  updateRegisterSettings,
  getSecuritySettings,
  updateSecuritySettings,
  getMailSettings,
  updateMailSettings,
  sendTestMail,
  getMaintenanceMode,
  toggleMaintenanceMode,
  backupSystem,
  clearSystemCache,
  resetSystem
} from '../../api/admin-settings';
import { 
  getAdminInfo, 
  updateAdminInfo, 
  changeAdminPassword 
} from '../../api/admin';

// 当前激活的标签页
const activeTab = ref('basic');

// 基本设置
const basicSettings = reactive({
  systemName: '',
  systemDesc: '',
  logoUrl: '',
  adminEmail: '',
  supportPhone: '',
  enableAnnouncement: false,
  announcement: ''
});

// 注册设置
const registerSettings = reactive({
  enableRegister: true,
  allowStudentRegister: true,
  allowCompanyRegister: true,
  companyNeedVerify: true,
  restrictStudentEmail: false,
  allowedEmailDomains: [],
  passwordMinLength: 8,
  passwordComplexity: []
});

// 安全设置
const securitySettings = reactive({
  enableLoginLock: false,
  maxLoginAttempts: 5,
  lockDuration: 30,
  enableIpWhitelist: false,
  allowedIps: [],
  sessionTimeout: 120
});

// 邮件设置
const mailSettings = reactive({
  enableMail: false,
  host: 'smtp.example.com',
  port: 587,
  ssl: true,
  username: '',
  password: '',
  from: ''
});

// 系统维护设置
const maintenanceSettings = reactive({
  maintenanceMode: false
});

// 邮件测试
const testMailAddress = ref('');
const sendingTestMail = ref(false);

// 个人设置
const personalSettings = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
  twoFactorAuth: false
});

// 密码修改表单
const showChangePasswordDialog = ref(false);
const changingPassword = ref(false);
const passwordFormRef = ref(null);
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 密码表单验证规则
const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码长度不能小于8位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

// 加载设置
const loadSettings = () => {
  // 加载个人信息
  getAdminInfo().then(res => {
    if (res.code === 200) {
      personalSettings.username = res.data.username;
      personalSettings.realName = res.data.realName;
      personalSettings.email = res.data.email;
      personalSettings.phone = res.data.phone;
      personalSettings.twoFactorAuth = res.data.twoFactorAuth;
    } else if (res.code === 401) {
      ElMessage.error('登录已过期，请重新登录');
      setTimeout(() => {
        window.location.href = '/admin/login';
      }, 1500);
    } else {
      ElMessage.warning(res.message || '获取个人信息失败');
      console.error('获取个人信息失败:', res);
    }
  }).catch(error => {
    console.error('获取个人信息失败:', error);
    ElMessage.error('获取个人信息失败: ' + (error.message || '服务器错误'));
    if (error.response && error.response.status === 401) {
      setTimeout(() => {
        window.location.href = '/admin/login';
      }, 1500);
    }
  });
  
  // 加载基本设置
  getBasicSettings().then(res => {
    if (res.code === 200) {
      Object.assign(basicSettings, res.data);
    } else {
      ElMessage.warning(res.message || '获取基本设置失败');
    }
  }).catch(error => {
    console.error('获取基本设置失败:', error);
    ElMessage.warning('获取基本设置失败，将使用默认设置');
  });
  
  // 加载注册设置
  getRegisterSettings().then(res => {
    if (res.code === 200) {
      Object.assign(registerSettings, res.data);
    } else {
      ElMessage.warning(res.message || '获取注册设置失败');
    }
  }).catch(error => {
    console.error('获取注册设置失败:', error);
    ElMessage.warning('获取注册设置失败，将使用默认设置');
  });
  
  // 加载安全设置
  getSecuritySettings().then(res => {
    if (res.code === 200) {
      Object.assign(securitySettings, res.data);
    } else {
      ElMessage.warning(res.message || '获取安全设置失败');
    }
  }).catch(error => {
    console.error('获取安全设置失败:', error);
    ElMessage.warning('获取安全设置失败，将使用默认设置');
  });
  
  // 加载维护模式状态
  getMaintenanceMode().then(res => {
    if (res.code === 200) {
      maintenanceSettings.maintenanceMode = res.data.enabled;
    } else {
      ElMessage.warning(res.message || '获取维护模式状态失败');
    }
  }).catch(error => {
    console.error('获取维护模式状态失败:', error);
    ElMessage.warning('获取维护模式状态失败，将使用默认设置');
  });
  
  // 加载邮件设置
  getMailSettings().then(res => {
    if (res.code === 200) {
      // 将后端的mail.enabled映射到前端的enableMail
      mailSettings.enableMail = res.data['mail.enabled'] || false;
      // 映射其他字段
      mailSettings.host = res.data['mail.host'] || 'smtp.example.com';
      mailSettings.port = parseInt(res.data['mail.port']) || 587;
      mailSettings.ssl = res.data['mail.ssl'] || true;
      mailSettings.username = res.data['mail.username'] || '';
      mailSettings.password = res.data['mail.password'] ? '******' : ''; // 密码使用占位符
      mailSettings.from = res.data['mail.from'] || '';
    } else {
      ElMessage.warning(res.message || '获取邮件设置失败');
    }
  }).catch(error => {
    console.error('获取邮件设置失败:', error);
    ElMessage.warning('获取邮件设置失败，将使用默认设置');
  });
};

// 保存所有设置
const saveAllSettings = () => {
  Promise.all([
    saveBasicSettings(),
    saveRegisterSettings(),
    saveSecuritySettings(),
    saveMailSettings(),
    savePersonalSettings()
  ]).then(() => {
    ElMessage.success('所有设置已保存');
  });
};

// 保存基本设置
const saveBasicSettings = () => {
  return updateBasicSettings(basicSettings).then(res => {
    if (res.code === 200) {
      ElMessage.success('基本设置已保存');
    } else {
      ElMessage.error(res.message || '保存基本设置失败');
    }
  }).catch(error => {
    console.error('保存基本设置失败:', error);
    ElMessage.error('保存基本设置失败');
  });
};

// 保存注册设置
const saveRegisterSettings = () => {
  return updateRegisterSettings(registerSettings).then(res => {
    if (res.code === 200) {
      ElMessage.success('注册设置已保存');
    } else {
      ElMessage.error(res.message || '保存注册设置失败');
    }
  }).catch(error => {
    console.error('保存注册设置失败:', error);
    ElMessage.error('保存注册设置失败');
  });
};

// 保存安全设置
const saveSecuritySettings = () => {
  return updateSecuritySettings(securitySettings).then(res => {
    if (res.code === 200) {
      ElMessage.success('安全设置已保存');
    } else {
      ElMessage.error(res.message || '保存安全设置失败');
    }
  }).catch(error => {
    console.error('保存安全设置失败:', error);
    ElMessage.error('保存安全设置失败');
  });
};

// 保存邮件设置
const saveMailSettings = () => {
  // 将前端的数据模型转换为后端需要的键值对格式
  const mailSettingsData = {
    'mail.enabled': mailSettings.enableMail,
    'mail.host': mailSettings.host,
    'mail.port': mailSettings.port,
    'mail.ssl': mailSettings.ssl,
    'mail.username': mailSettings.username,
    'mail.from': mailSettings.from
  };
  
  // 只有当密码不是占位符时才更新密码
  if (mailSettings.password && mailSettings.password !== '******') {
    mailSettingsData['mail.password'] = mailSettings.password;
  }
  
  return updateMailSettings(mailSettingsData).then(res => {
    if (res.code === 200) {
      ElMessage.success('邮件设置已保存');
    } else {
      ElMessage.error(res.message || '保存邮件设置失败');
    }
  }).catch(error => {
    console.error('保存邮件设置失败:', error);
    ElMessage.error('保存邮件设置失败');
  });
};

// 发送测试邮件
const handleSendTestMail = () => {
  if (!testMailAddress.value) {
    ElMessage.warning('请输入测试邮箱地址');
    return;
  }
  
  sendingTestMail.value = true;
  
  // 发送测试邮件前先保存设置
  saveMailSettings().then(() => {
    return sendTestMail(testMailAddress.value);
  }).then(res => {
    if (res.code === 200) {
      ElMessage.success('测试邮件已发送，请检查收件箱');
    } else {
      ElMessage.error(res.message || '发送测试邮件失败');
    }
  }).catch(error => {
    console.error('发送测试邮件失败:', error);
    ElMessage.error('发送测试邮件失败: ' + (error.message || '服务器错误'));
  }).finally(() => {
    sendingTestMail.value = false;
  });
};

// 保存个人设置
const savePersonalSettings = () => {
  return updateAdminInfo(personalSettings).then(res => {
    if (res.code === 200) {
      ElMessage.success('个人设置已保存');
    } else {
      ElMessage.error(res.message || '保存个人设置失败');
    }
  }).catch(error => {
    console.error('保存个人设置失败:', error);
    ElMessage.error('保存个人设置失败');
  });
};

// 处理LOGO上传
const handleLogoChange = (file) => {
  const formData = new FormData();
  formData.append('file', file.raw);
  
  uploadLogo(formData).then(res => {
    if (res.code === 200) {
      basicSettings.logoUrl = res.data;
      ElMessage.success('LOGO上传成功');
    } else {
      ElMessage.error(res.message || 'LOGO上传失败');
    }
  }).catch(error => {
    console.error('LOGO上传失败:', error);
    ElMessage.error('LOGO上传失败');
  });
};

// 切换两步验证
const toggleTwoFactorAuth = (value) => {
  if (value) {
    ElMessageBox.confirm(
      '开启两步验证后，登录时将需要额外的验证码。确定要开启吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    ).then(() => {
      personalSettings.twoFactorAuth = true;
      savePersonalSettings();
    }).catch(() => {
      personalSettings.twoFactorAuth = false;
    });
  } else {
    personalSettings.twoFactorAuth = false;
    savePersonalSettings().then(() => {
      ElMessage.warning('两步验证已关闭');
    });
  }
};

// 切换维护模式
const handleMaintenanceToggle = (value) => {
  const action = value ? '启用' : '关闭';
  
  ElMessageBox.confirm(
    `确定要${action}维护模式吗？${value ? '启用后用户将无法访问系统。' : ''}`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    toggleMaintenanceMode(value).then(res => {
      if (res.code === 200) {
        maintenanceSettings.maintenanceMode = value;
        ElMessage.success(`已${action}维护模式`);
      } else {
        ElMessage.error(res.message || `${action}维护模式失败`);
        maintenanceSettings.maintenanceMode = !value; // 恢复原状态
      }
    }).catch(error => {
      console.error(`${action}维护模式失败:`, error);
      ElMessage.error(`${action}维护模式失败`);
      maintenanceSettings.maintenanceMode = !value; // 恢复原状态
    });
  }).catch(() => {
    maintenanceSettings.maintenanceMode = !value; // 恢复原状态
  });
};

// 备份系统
const handleBackup = () => {
  ElMessageBox.confirm(
    '确定要备份系统数据吗？此操作可能需要一些时间。',
    '提示',
    {
      confirmButtonText: '确定备份',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    backupSystem().then(res => {
      // 处理二进制文件下载
      const blob = new Blob([res], { type: 'application/zip' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `system_backup_${new Date().toISOString().slice(0, 10)}.zip`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      
      ElMessage.success('系统备份成功，文件下载中...');
    }).catch(error => {
      console.error('系统备份失败:', error);
      ElMessage.error('系统备份失败');
    });
  }).catch(() => {});
};

// 清理缓存
const handleClearCache = () => {
  ElMessageBox.confirm(
    '确定要清理系统缓存吗？',
    '提示',
    {
      confirmButtonText: '确定清理',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    clearSystemCache().then(res => {
      if (res.code === 200) {
        ElMessage.success('系统缓存已清理');
      } else {
        ElMessage.error(res.message || '清理系统缓存失败');
      }
    }).catch(error => {
      console.error('清理系统缓存失败:', error);
      ElMessage.error('清理系统缓存失败');
    });
  }).catch(() => {});
};

// 重置系统
const handleResetSystem = () => {
  ElMessageBox.prompt(
    '此操作将清空所有数据，恢复系统到初始状态，且无法恢复。如需继续，请输入"RESET"以确认。',
    '危险操作',
    {
      confirmButtonText: '确认重置',
      cancelButtonText: '取消',
      inputPattern: /^RESET$/,
      inputErrorMessage: '请输入"RESET"以确认',
      type: 'error',
    }
  ).then(({ value }) => {
    resetSystem({ confirmation: value }).then(res => {
      if (res.code === 200) {
        ElMessage.success('系统已重置，即将退出...');
        setTimeout(() => {
          // 重置后退出登录
          window.location.href = '/admin/login';
        }, 1500);
      } else {
        ElMessage.error(res.message || '重置系统失败');
      }
    }).catch(error => {
      console.error('重置系统失败:', error);
      ElMessage.error('重置系统失败');
    });
  }).catch(() => {});
};

// 修改密码
const changePassword = () => {
  passwordFormRef.value.validate((valid) => {
    if (valid) {
      changingPassword.value = true;
      
      changeAdminPassword({
        oldPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword
      }).then(res => {
        if (res.code === 200) {
          ElMessage.success('密码修改成功');
          showChangePasswordDialog.value = false;
          
          // 清空表单
          passwordForm.currentPassword = '';
          passwordForm.newPassword = '';
          passwordForm.confirmPassword = '';
        } else {
          ElMessage.error(res.message || '密码修改失败');
        }
      }).catch(error => {
        console.error('密码修改失败:', error);
        ElMessage.error('密码修改失败');
      }).finally(() => {
        changingPassword.value = false;
      });
    }
  });
};

// 初始化
onMounted(() => {
  loadSettings();
});
</script>

<style scoped>
.settings-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header-card {
  margin-bottom: 4px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.settings-tabs {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.settings-form {
  max-width: 800px;
  margin: 20px auto;
}

.logo-uploader {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 150px;
  height: 150px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.logo-uploader:hover {
  border-color: var(--el-color-primary);
}

.logo-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 150px;
  height: 150px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.logo-preview {
  width: 150px;
  height: 150px;
  object-fit: contain;
}

.upload-hint,
.form-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}

.warning-card {
  background-color: #fff8e6;
  margin-bottom: 16px;
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #e6a23c;
}

.warning-card .el-icon {
  font-size: 20px;
}

.maintenance-actions {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.action-card {
  height: 100%;
}

.action-header h3,
.danger-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.action-content,
.danger-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.action-content p,
.danger-content p {
  margin: 0;
  color: #606266;
}

.danger-card {
  margin-top: 16px;
  border: 1px solid #f56c6c;
}

.danger-header {
  background-color: #fef0f0;
  color: #f56c6c;
}

.test-mail-wrapper {
  display: flex;
  gap: 16px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .page-actions {
    width: 100%;
  }
  
  .maintenance-actions {
    grid-template-columns: 1fr;
  }
}
</style>

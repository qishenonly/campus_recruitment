<template>
  <el-dialog
    v-model="dialogVisible"
    title="完善企业信息"
    width="50%"
    :close-on-click-modal="false"
    :show-close="true"
    class="complete-info-dialog"
    @close="handleClose"
  >
    <div class="dialog-content">
      <div class="form-header">
        <h3>欢迎加入青云直聘</h3>
        <p>请完善企业信息，这将帮助您更好地招聘优秀人才</p>
      </div>

      <el-scrollbar height="calc(80vh - 180px)" class="form-scrollbar">
        <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-width="100px"
          class="info-form"
        >
          <div class="form-section">
            <div class="section-title">基本信息</div>
            <el-form-item label="企业名称" prop="companyName">
              <el-input v-model="formData.companyName" placeholder="请输入企业名称" />
            </el-form-item>
            
            <el-form-item label="企业规模" prop="scale">
              <el-select v-model="formData.scale" placeholder="请选择企业规模" class="w-full">
                <el-option label="少于15人" value="少于15人" />
                <el-option label="15-50人" value="15-50人" />
                <el-option label="50-150人" value="50-150人" />
                <el-option label="150-500人" value="150-500人" />
                <el-option label="500-2000人" value="500-2000人" />
                <el-option label="2000人以上" value="2000人以上" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="所属行业" prop="industry">
              <el-select v-model="formData.industry" placeholder="请选择所属行业" class="w-full">
                <el-option 
                  v-for="item in industryOptions" 
                  :key="item" 
                  :label="item" 
                  :value="item" 
                />
              </el-select>
            </el-form-item>
          </div>

          <div class="form-section">
            <div class="section-title">联系信息</div>
            <el-form-item label="公司地址" prop="location">
              <el-input v-model="formData.location" placeholder="请输入公司地址" />
            </el-form-item>
            
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入联系电话" />
            </el-form-item>
            
            <el-form-item label="公司邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="请输入公司邮箱" />
            </el-form-item>
          </div>

          <div class="form-section">
            <div class="section-title">企业介绍</div>
            <el-form-item label="公司简介" prop="description">
              <el-input 
                v-model="formData.description" 
                type="textarea" 
                :rows="4" 
                placeholder="请简要介绍公司情况" 
              />
            </el-form-item>
            
            <el-form-item label="公司网站" prop="website">
              <el-input v-model="formData.website" placeholder="请输入公司网站，选填" />
            </el-form-item>
          </div>
        </el-form>
      </el-scrollbar>

      <div class="form-footer">
        <el-button type="primary" @click="submitForm" round size="large">
          提交信息
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { updateCompanyInfo, createCompanyInfo } from '@/api/company'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'complete'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 确保对话框在每次打开时重置状态
watch(() => props.modelValue, (newVal) => {
  if (newVal === true) {
    console.log('企业信息完善对话框被打开')
    isSubmitted.value = false
    // 可以在这里初始化表单数据
  }
})

const formRef = ref(null)
const isSubmitted = ref(false) // 添加提交状态标记
const formData = reactive({
  companyName: '',
  scale: '',
  industry: '',
  location: '',
  phone: '',
  email: '',
  description: '',
  website: ''
})

// 表单验证规则
const rules = {
  companyName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  scale: [{ required: true, message: '请选择企业规模', trigger: 'change' }],
  industry: [{ required: true, message: '请选择所属行业', trigger: 'change' }],
  location: [{ required: true, message: '请输入公司地址', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入公司邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  description: [{ required: true, message: '请输入公司简介', trigger: 'blur' }]
}

// 行业选项
const industryOptions = [
  '互联网/IT',
  '金融',
  '教育',
  '医疗健康',
  '电子商务',
  '人工智能',
  '游戏',
  '房地产',
  '制造业',
  '农业',
  '其他'
]

// 修改关闭处理函数
const handleClose = () => {
  // 如果已经成功提交,直接关闭
  if (isSubmitted.value) {
    dialogVisible.value = false
    return
  }
  
  // 否则显示确认对话框
  ElMessageBox.confirm(
    '确定要关闭吗？未保存的信息将会丢失',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      dialogVisible.value = false
    })
    .catch(() => {})
}

// 提交表单函数
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 获取当前用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    
    // 构建提交数据，包含企业信息和团队成员信息
    const submitData = {
      ...formData,
      userId: userInfo.id,
      scale: formData.scale,  // 企业规模
      industry: formData.industry,  // 所属行业
      // 添加团队成员信息（当前登录用户）
      memberName: userInfo.realName || userInfo.username,
      position: "HR专员",  // 默认职位
      memberRole: "admin",  // 默认角色为管理员
      memberEmail: userInfo.email || formData.email,
      memberPhone: userInfo.phone || formData.phone
    }
    
    console.log('提交的企业信息:', submitData)
    
    // 使用创建接口而不是更新接口，因为是首次完善信息
    const response = await createCompanyInfo(submitData)
    
    // 更新用户信息，添加企业ID和公司名称标记
    if (response.data && response.data.companyId) {
      userInfo.companyId = response.data.companyId
      userInfo.companyName = formData.companyName
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    }
    
    ElMessage.success('企业信息提交成功')
    isSubmitted.value = true // 标记为已提交
    dialogVisible.value = false
    emit('complete')
  } catch (error) {
    console.error('提交失败:', error)
    // 显示更详细的错误信息
    let errorMsg = '提交失败'
    if (error.response && error.response.data && error.response.data.message) {
      errorMsg = error.response.data.message
    } else if (error.message) {
      errorMsg = error.message
    }
    ElMessage.error(errorMsg)
  }
}
</script>

<style scoped>
.dialog-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-header {
  text-align: center;
  margin-bottom: 20px;
}

.form-header h3 {
  font-size: 22px;
  margin-bottom: 8px;
  color: var(--primary-color);
}

.form-header p {
  font-size: 14px;
  color: #666;
}

.info-form {
  margin-top: 20px;
}

.form-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

.form-footer {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.w-full {
  width: 100%;
}
</style> 
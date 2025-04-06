<template>
  <div class="company-info-container">
    <el-card class="info-card">
      <template #header>
        <div class="card-header">
          <span>企业信息</span>
          <el-button type="primary" size="small" @click="handleEdit">{{ editing ? '保存' : '编辑' }}</el-button>
        </div>
      </template>
      
      <el-form 
        :model="companyForm" 
        :rules="rules" 
        ref="companyFormRef" 
        label-width="100px"
        :disabled="!editing">
        
        <!-- 企业Logo上传 -->
        <el-form-item label="企业LOGO">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="headers"
            :show-file-list="false"
            :on-success="handleLogoSuccess"
            :before-upload="beforeLogoUpload"
            :disabled="!editing">
            <img v-if="companyForm.logo" :src="companyForm.logo" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">推荐尺寸: 300px × 300px，支持jpg、png格式</div>
        </el-form-item>
        
        <!-- 企业名称 -->
        <el-form-item label="企业名称" prop="name">
          <el-input v-model="companyForm.name" placeholder="请输入企业名称"></el-input>
        </el-form-item>
        
        <!-- 企业简介 -->
        <el-form-item label="企业简介" prop="description">
          <el-input 
            type="textarea" 
            v-model="companyForm.description" 
            placeholder="请输入企业简介"
            :rows="4"></el-input>
        </el-form-item>
        
        <!-- 所在城市 -->
        <el-form-item label="所在城市" prop="city">
          <el-cascader
            v-model="companyForm.city"
            :options="cityOptions"
            placeholder="请选择所在城市"></el-cascader>
        </el-form-item>
        
        <!-- 企业规模 -->
        <el-form-item label="企业规模" prop="size">
          <el-select v-model="companyForm.size" placeholder="请选择企业规模">
            <el-option label="少于15人" value="少于15人"></el-option>
            <el-option label="15-50人" value="15-50人"></el-option>
            <el-option label="50-150人" value="50-150人"></el-option>
            <el-option label="150-500人" value="150-500人"></el-option>
            <el-option label="500-2000人" value="500-2000人"></el-option>
            <el-option label="2000人以上" value="2000人以上"></el-option>
          </el-select>
        </el-form-item>
        
        <!-- 企业行业 -->
        <el-form-item label="所属行业" prop="industry">
          <el-select v-model="companyForm.industry" placeholder="请选择所属行业">
            <el-option label="互联网/IT" value="互联网/IT"></el-option>
            <el-option label="金融" value="金融"></el-option>
            <el-option label="教育" value="教育"></el-option>
            <el-option label="医疗健康" value="医疗健康"></el-option>
            <el-option label="电子商务" value="电子商务"></el-option>
            <el-option label="人工智能" value="人工智能"></el-option>
            <el-option label="游戏" value="游戏"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        
        <!-- 企业融资阶段 -->
        <el-form-item label="融资阶段" prop="financingStage">
          <el-select v-model="companyForm.financingStage" placeholder="请选择融资阶段">
            <el-option label="未融资" value="未融资"></el-option>
            <el-option label="天使轮" value="天使轮"></el-option>
            <el-option label="A轮" value="A轮"></el-option>
            <el-option label="B轮" value="B轮"></el-option>
            <el-option label="C轮" value="C轮"></el-option>
            <el-option label="D轮及以上" value="D轮及以上"></el-option>
            <el-option label="已上市" value="已上市"></el-option>
          </el-select>
        </el-form-item>
        
        <!-- 企业网址 -->
        <el-form-item label="企业网址" prop="website">
          <el-input v-model="companyForm.website" placeholder="请输入企业网址"></el-input>
        </el-form-item>
        
        <!-- 企业地址 -->
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="companyForm.address" placeholder="请输入详细地址"></el-input>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getCompanyInfo, updateCompanyInfo, uploadCompanyLogo } from '@/api/companyInfo'

// 城市数据
const cityOptions = [
  {
    value: '北京',
    label: '北京',
  },
  {
    value: '上海',
    label: '上海',
  },
  {
    value: '广州',
    label: '广州',
  },
  {
    value: '深圳',
    label: '深圳',
  },
  {
    value: '杭州',
    label: '杭州',
  }
]

// 表单引用
const companyFormRef = ref(null)

// 企业表单数据
const companyForm = ref({
  name: '',
  description: '',
  logo: '',
  city: [],
  size: '',
  industry: '',
  financingStage: '',
  website: '',
  address: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入企业名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度应为2-50个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入企业简介', trigger: 'blur' },
    { min: 10, max: 1000, message: '长度应为10-1000个字符', trigger: 'blur' }
  ],
  city: [
    { required: true, message: '请选择所在城市', trigger: 'change' }
  ],
  size: [
    { required: true, message: '请选择企业规模', trigger: 'change' }
  ],
  industry: [
    { required: true, message: '请选择所属行业', trigger: 'change' }
  ]
}

// 编辑状态
const editing = ref(false)

// 上传URL和请求头
const uploadUrl = '/api/company/logo'
const headers = {
  Authorization: `Bearer ${localStorage.getItem('token')}`
}

// 获取企业信息
const fetchCompanyInfo = async () => {
  try {
    const res = await getCompanyInfo()
    if (res.code === 200) {
      const data = res.data || {}
      companyForm.value = {
        name: data.name || '',
        description: data.description || '',
        logo: data.logo || '',
        city: data.city || [],
        size: data.size || '',
        industry: data.industry || '',
        financingStage: data.financingStage || '',
        website: data.website || '',
        address: data.address || ''
      }
    }
  } catch (error) {
    console.error('获取企业信息失败:', error)
    ElMessage.error('获取企业信息失败')
  }
}

// 处理编辑/保存操作
const handleEdit = async () => {
  if (editing.value) {
    // 保存操作
    try {
      // 表单验证
      await companyFormRef.value.validate()
      
      // 提交表单
      const res = await updateCompanyInfo(companyForm.value)
      if (res.code === 200) {
        ElMessage.success('保存成功')
        editing.value = false
      } else {
        ElMessage.error(res.message || '保存失败')
      }
    } catch (error) {
      console.error('保存企业信息失败:', error)
      if (error.message) {
        ElMessage.error(error.message)
      } else {
        ElMessage.error('保存失败，请检查表单信息')
      }
    }
  } else {
    // 进入编辑模式
    editing.value = true
  }
}

// 图片上传前的验证
const beforeLogoUpload = (file) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('上传头像图片只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 图片上传成功的回调
const handleLogoSuccess = (res, file) => {
  if (res.code === 200) {
    companyForm.value.logo = res.data.url
    ElMessage.success('LOGO上传成功')
  } else {
    ElMessage.error(res.message || 'LOGO上传失败')
  }
}

onMounted(() => {
  fetchCompanyInfo()
})
</script>

<style scoped>
.company-info-container {
  padding: 20px;
}

.info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
}

.upload-tip {
  color: #606266;
  font-size: 12px;
  margin-top: 8px;
}
</style> 
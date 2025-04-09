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
          <div class="logo-upload-container">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :headers="headers"
              :show-file-list="false"
              :on-success="handleLogoSuccess"
              :before-upload="beforeLogoUpload"
              :on-error="handleLogoError"
              :disabled="!editing">
              <img 
                v-if="companyForm.logo" 
                :src="getAvatarUrl(companyForm.logo)" 
                class="avatar" 
              />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            
            <!-- 添加备用上传按钮 -->
            <div v-if="editing" class="backup-upload">
              <input
                type="file"
                ref="logoFileInput"
                style="display: none"
                accept="image/jpeg,image/png"
                @change="handleManualFileChange"
              />
              <el-button 
                size="small" 
                type="primary" 
                @click="triggerFileSelect"
                :icon="Upload"
              >
                备用上传
              </el-button>
            </div>
          </div>
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
import { Plus, Upload } from '@element-plus/icons-vue'
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

// 文件输入引用
const logoFileInput = ref(null)

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

// getAvatarUrl
const getAvatarUrl = (url) => {
  return import.meta.env.VITE_API_BASE_URL + url;
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
  console.log('准备上传的文件:', file)
  console.log('文件类型:', file.type)
  console.log('文件大小:', file.size / 1024 / 1024, 'MB')
  
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('上传LOGO图片只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传LOGO图片大小不能超过 2MB!')
    return false
  }
  
  // 检查文件名是否有中文（有些服务器可能会有问题）
  if (/[\u4e00-\u9fa5]/.test(file.name)) {
    // 不直接阻止上传，但给出警告
    console.warn('文件名包含中文，可能在某些服务器上出现问题')
    ElMessage.warning('文件名包含中文，建议使用英文命名')
  }
  
  console.log('文件校验通过，准备上传')
  return true
}

// 图片上传成功的回调
const handleLogoSuccess = (res, file) => {
  if (res.code === 200 && res.data && res.data.url) {
    console.log('LOGO上传成功，返回数据:', res.data)
    
    // 获取正确的URL路径
    let logoUrl = res.data.url
    
    // 确保URL中包含/api前缀
    if (!logoUrl.startsWith('/api') && !logoUrl.startsWith('http')) {
      if (logoUrl.startsWith('/')) {
        logoUrl = '/api' + logoUrl
      } else {
        logoUrl = '/api/' + logoUrl
      }
    }
    
    // 如果URL以/api开头但不是完整URL，添加基础路径
    if (logoUrl.startsWith('/api') && !logoUrl.startsWith('http')) {
      const baseURL = import.meta.env.VITE_API_URL || ''
      // 如果基础URL已经包含/api，避免重复
      if (baseURL && baseURL.endsWith('/api')) {
        logoUrl = baseURL + logoUrl.substring(4) // 移除/api
      } else if (baseURL) {
        // 确保baseURL和logoUrl之间没有重复的斜杠
        if (baseURL.endsWith('/') && logoUrl.startsWith('/')) {
          logoUrl = baseURL + logoUrl.substring(1)
        } else if (!baseURL.endsWith('/') && !logoUrl.startsWith('/')) {
          logoUrl = baseURL + '/' + logoUrl
        } else {
          logoUrl = baseURL + logoUrl
        }
      }
    }
    
    console.log('最终使用的LOGO URL:', logoUrl)
    
    // 添加时间戳参数，防止浏览器缓存
    companyForm.value.logo = logoUrl + '?t=' + new Date().getTime()
    
    ElMessage.success('LOGO上传成功')
  } else {
    console.error('LOGO上传失败，返回数据:', res)
    ElMessage.error(res.message || 'LOGO上传失败')
  }
}

// 图片上传错误的回调
const handleLogoError = (error) => {
  console.error('LOGO上传错误:', error)
  ElMessage.error('LOGO上传错误，请检查网络连接')
}

// 图片加载错误的回调
const handleLogoLoadError = (e) => {
  console.error('LOGO加载错误')
  // 记录当前URL
  const currentUrl = e.target.src
  console.log('加载失败的URL:', currentUrl)
  
  // 显示默认图像
  e.target.src = '/default-logo.png'
  // 不再显示错误提示，避免用户体验不佳
}

// 手动上传LOGO的方法（作为备用方案）
const manualUploadLogo = async (file) => {
  try {
    if (!beforeLogoUpload(file)) {
      return Promise.reject(new Error('文件校验未通过'))
    }

    const formData = new FormData()
    formData.append('file', file)

    // 显示上传中提示
    const loadingInstance = ElMessage({
      message: 'LOGO上传中...',
      type: 'info',
      duration: 0
    })

    try {
      // 调用上传API
      const res = await uploadCompanyLogo(formData)
      
      // 关闭提示
      loadingInstance.close()
      
      // 处理响应
      if (res.code === 200 && res.data) {
        // 使用与handleLogoSuccess相同的逻辑处理
        handleLogoSuccess(res)
        return Promise.resolve(res)
      } else {
        ElMessage.error(res.message || 'LOGO上传失败')
        return Promise.reject(new Error(res.message || 'LOGO上传失败'))
      }
    } catch (error) {
      // 关闭提示
      loadingInstance.close()
      
      console.error('LOGO上传出错:', error)
      ElMessage.error('LOGO上传失败: ' + (error.message || '未知错误'))
      return Promise.reject(error)
    }
  } catch (error) {
    console.error('LOGO上传准备失败:', error)
    return Promise.reject(error)
  }
}

// 触发文件选择的方法
const triggerFileSelect = () => {
  logoFileInput.value.click()
}

// 处理手动文件选择变更的方法
const handleManualFileChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    console.log('手动选择文件:', file)
    manualUploadLogo(file).catch(error => {
      console.error('手动上传LOGO失败:', error)
    }).finally(() => {
      // 重置文件输入，确保同一文件可以再次选择
      event.target.value = ''
    })
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

.logo-upload-container {
  position: relative;
  display: inline-block;
}

.backup-upload {
  position: absolute;
  top: 0;
  left: 0;
  width: 178px;
  height: 178px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.8);
  opacity: 0;
  transition: opacity 0.3s;
}

.backup-upload:hover {
  opacity: 1;
}

.backup-upload input {
  display: none;
}

.backup-upload button {
  margin: 0;
}
</style> 
<template>
  <div class="job-form">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="120px"
      label-position="top"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="职位名称" prop="title">
            <el-input v-model="formData.title" placeholder="请输入职位名称" />
          </el-form-item>
        </el-col>
        
        <el-col :span="12">
          <el-form-item label="所属部门" prop="department">
            <el-input v-model="formData.department" placeholder="请输入所属部门" />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="薪资范围" prop="salary">
            <el-select v-model="formData.salary" placeholder="请选择薪资范围" class="w-100">
              <el-option label="面议" value="面议" />
              <el-option label="3k-5k" value="3k-5k" />
              <el-option label="5k-10k" value="5k-10k" />
              <el-option label="10k-15k" value="10k-15k" />
              <el-option label="15k-20k" value="15k-20k" />
              <el-option label="20k-30k" value="20k-30k" />
              <el-option label="30k-50k" value="30k-50k" />
              <el-option label="50k以上" value="50k以上" />
            </el-select>
          </el-form-item>
        </el-col>
        
        <el-col :span="12">
          <el-form-item label="工作地点" prop="location">
            <el-cascader
              v-model="locationValue"
              :options="areaData"
              placeholder="请选择工作地点"
              @change="handleLocationChange"
              class="w-100"
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="职位类型" prop="positionType">
            <el-select v-model="formData.positionType" placeholder="请选择职位类型" class="w-100">
              <el-option label="全职" value="全职" />
              <el-option label="兼职" value="兼职" />
              <el-option label="实习" value="实习" />
              <el-option label="校招" value="校招" />
              <el-option label="社招" value="社招" />
            </el-select>
          </el-form-item>
        </el-col>
        
        <el-col :span="8">
          <el-form-item label="学历要求" prop="educationRequirement">
            <el-select v-model="formData.educationRequirement" placeholder="请选择学历要求" class="w-100">
              <el-option label="不限" value="不限" />
              <el-option label="大专" value="大专" />
              <el-option label="本科" value="本科" />
              <el-option label="硕士" value="硕士" />
              <el-option label="博士" value="博士" />
            </el-select>
          </el-form-item>
        </el-col>
        
        <el-col :span="8">
          <el-form-item label="经验要求" prop="experienceRequirement">
            <el-select v-model="formData.experienceRequirement" placeholder="请选择经验要求" class="w-100">
              <el-option label="不限" value="不限" />
              <el-option label="应届毕业生" value="应届毕业生" />
              <el-option label="1年以下" value="1年以下" />
              <el-option label="1-3年" value="1-3年" />
              <el-option label="3-5年" value="3-5年" />
              <el-option label="5-10年" value="5-10年" />
              <el-option label="10年以上" value="10年以上" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-form-item label="专业要求" prop="majorRequirement">
        <el-input v-model="formData.majorRequirement" placeholder="请输入专业要求，如：计算机科学、软件工程等" />
      </el-form-item>
      
      <el-form-item label="职位描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="4"
          placeholder="请详细描述该职位的工作内容、职责等"
        />
      </el-form-item>
      
      <el-form-item label="岗位职责" prop="responsibilities">
        <el-input
          v-model="formData.responsibilities"
          type="textarea"
          :rows="4"
          placeholder="请列出该职位的主要职责和工作内容"
        />
      </el-form-item>
      
      <el-form-item label="任职要求" prop="requirements">
        <el-input
          v-model="formData.requirements"
          type="textarea"
          :rows="4"
          placeholder="请列出应聘者需要具备的技能、素质和能力"
        />
      </el-form-item>
      
      <el-form-item label="福利待遇" prop="benefits">
        <el-input
          v-model="formData.benefits"
          type="textarea"
          :rows="3"
          placeholder="请描述该职位的福利待遇，如：五险一金、带薪年假、免费班车等"
        />
      </el-form-item>
      
      <el-form-item label="职位状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio label="PUBLISHED">立即发布</el-radio>
          <el-radio label="DRAFT">暂不发布</el-radio>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item>
        <div class="form-actions">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" @click="submitForm">{{ isEdit ? '保存修改' : '发布职位' }}</el-button>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { areaData } from '@/utils/area-data'

const props = defineProps({
  formData: {
    type: Object,
    required: true
  },
  isEdit: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['submit', 'cancel'])

const formRef = ref(null)
const locationValue = ref([])

// 表单验证规则
const rules = reactive({
  title: [
    { required: true, message: '请输入职位名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  department: [
    { required: true, message: '请输入所属部门', trigger: 'blur' }
  ],
  salary: [
    { required: true, message: '请选择薪资范围', trigger: 'change' }
  ],
  location: [
    { required: true, message: '请选择工作地点', trigger: 'change' }
  ],
  positionType: [
    { required: true, message: '请选择职位类型', trigger: 'change' }
  ],
  educationRequirement: [
    { required: true, message: '请选择学历要求', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入职位描述', trigger: 'blur' }
  ],
  responsibilities: [
    { required: true, message: '请输入岗位职责', trigger: 'blur' }
  ],
  requirements: [
    { required: true, message: '请输入任职要求', trigger: 'blur' }
  ]
})

// 监听表单数据变化，更新地区选择器的值
watch(() => props.formData.location, (newVal) => {
  if (newVal && typeof newVal === 'string') {
    const parts = newVal.split('/')
    if (parts.length >= 2) {
      // 尝试在areaData中找到匹配的值
      const province = parts[0]
      const city = parts[1]
      const district = parts[2] || ''
      
      // 查找省份
      const provinceObj = areaData.find(p => p.label === province)
      if (provinceObj) {
        // 查找城市
        const cityObj = provinceObj.children.find(c => c.label === city)
        if (cityObj && cityObj.children) {
          // 查找区县
          if (district) {
            const districtObj = cityObj.children.find(d => d.label === district)
            if (districtObj) {
              locationValue.value = [provinceObj.value, cityObj.value, districtObj.value]
            }
          } else {
            locationValue.value = [provinceObj.value, cityObj.value]
          }
        }
      }
    }
  } else {
    locationValue.value = []
  }
}, { immediate: true })

// 处理地区选择变化
const handleLocationChange = (value) => {
  if (value && value.length) {
    // 根据选择的值找到对应的标签
    let locationText = ''
    
    // 查找省份
    const province = areaData.find(p => p.value === value[0])
    if (province) {
      locationText += province.label
      
      // 查找城市
      if (value.length > 1) {
        const city = province.children.find(c => c.value === value[1])
        if (city) {
          locationText += '/' + city.label
          
          // 查找区县
          if (value.length > 2 && city.children) {
            const district = city.children.find(d => d.value === value[2])
            if (district) {
              locationText += '/' + district.label
            }
          }
        }
      }
    }
    
    props.formData.location = locationText
  } else {
    props.formData.location = ''
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate((valid, fields) => {
    if (valid) {
      emit('submit', props.formData)
    }
  })
}

// 取消
const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped>
.job-form {
  padding: 20px 0;
}

.w-100 {
  width: 100%;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 20px;
}

@media (max-width: 768px) {
  :deep(.el-form-item__label) {
    padding-bottom: 4px;
  }
}
</style> 
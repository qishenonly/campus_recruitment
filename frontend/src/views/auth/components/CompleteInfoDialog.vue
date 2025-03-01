<template>
  <el-dialog
    v-model="dialogVisible"
    title="完善个人信息"
    width="50%"
    :close-on-click-modal="false"
    :show-close="true"
    class="complete-info-dialog"
    @close="handleClose"
  >
    <div class="dialog-content">
      <div class="form-header">
        <h3>欢迎加入青云直聘</h3>
        <p>请完善您的个人信息，这将帮助您更好地找到合适的工作机会</p>
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
            <el-form-item label="姓名" prop="name">
              <el-input v-model="formData.name" placeholder="请输入姓名" />
            </el-form-item>
            
            <el-form-item label="性别" prop="gender">
              <el-select v-model="formData.gender" placeholder="请选择性别" class="w-full">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>

            <el-form-item label="生日" prop="birthday">
              <el-date-picker
                v-model="formData.birthday"
                type="date"
                placeholder="请选择生日"
                :disabled-date="disabledDate"
                class="w-full"
              />
            </el-form-item>
          </div>

          <div class="form-section">
            <div class="section-title">教育背景</div>
            <el-form-item label="院校" prop="school">
              <el-input v-model="formData.school" placeholder="请输入院校名称" />
            </el-form-item>
            
            <el-form-item label="专业" prop="major">
              <el-input v-model="formData.major" placeholder="请输入专业" />
            </el-form-item>
            
            <el-form-item label="学历" prop="education">
              <el-select v-model="formData.education" placeholder="请选择学历" class="w-full">
                <el-option
                  v-for="item in educationOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="毕业年份" prop="graduationYear">
              <el-date-picker
                v-model="formData.graduationYear"
                type="year"
                placeholder="请选择毕业年份"
                :disabled-date="disabledGraduationYear"
                class="w-full"
              />
            </el-form-item>
          </div>

          <div class="form-section">
            <div class="section-title">求职意向</div>
            <el-form-item label="当前位置" prop="location">
              <el-cascader
                v-model="formData.location"
                :options="areaOptions"
                placeholder="请选择当前位置"
                class="w-full"
              />
            </el-form-item>
            
            <el-form-item label="期望职位" prop="expectedPosition">
              <el-input v-model="formData.expectedPosition" placeholder="请输入期望职位" />
            </el-form-item>
            
            <el-form-item label="期望薪资" prop="expectedSalary">
              <el-select v-model="formData.expectedSalary" placeholder="请选择期望薪资" class="w-full">
                <el-option
                  v-for="item in salaryOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="期望工作地" prop="expectedLocation">
              <el-cascader
                v-model="formData.expectedLocation"
                :options="areaOptions"
                placeholder="请选择期望工作地"
                class="w-full"
              />
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
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { updateUserProfile } from '@/api/user'
import { areaData } from '@/utils/area-data' // 需要自己准备省市区数据

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'complete'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formRef = ref(null)
const isSubmitted = ref(false) // 添加提交状态标记
const formData = reactive({
  name: '',
  gender: '',
  birthday: '',
  school: '',
  major: '',
  education: '',
  graduationYear: '',
  location: [],
  expectedPosition: '',
  expectedSalary: '',
  expectedLocation: []
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  birthday: [{ required: true, message: '请选择生日', trigger: 'change' }],
  school: [{ required: true, message: '请输入院校', trigger: 'blur' }],
  major: [{ required: true, message: '请输入专业', trigger: 'blur' }],
  education: [{ required: true, message: '请选择学历', trigger: 'change' }],
  graduationYear: [{ required: true, message: '请选择毕业年份', trigger: 'change' }],
  location: [{ required: true, message: '请选择当前位置', trigger: 'change' }],
  expectedPosition: [{ required: true, message: '请输入期望职位', trigger: 'blur' }],
  expectedSalary: [{ required: true, message: '请选择期望薪资', trigger: 'change' }],
  expectedLocation: [{ required: true, message: '请选择期望工作地', trigger: 'change' }]
}

// 选项数据
const educationOptions = ['大专', '本科', '硕士', '博士']
const salaryOptions = [
  '3k以下',
  '3k-5k',
  '5k-7k',
  '7k-10k',
  '10k-15k',
  '15k-20k',
  '20k-30k',
  '30k以上'
]
const areaOptions = areaData // 省市区数据

// 日期限制
const disabledDate = (time) => {
  return time.getTime() > Date.now() || time.getTime() < new Date('1970-01-01').getTime()
}

const disabledGraduationYear = (time) => {
  const currentYear = new Date().getFullYear()
  return time.getFullYear() < currentYear || time.getFullYear() > currentYear + 10
}

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

// 修改提交表单函数
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 格式化数据
    const submitData = {
      ...formData,
      birthday: formData.birthday ? formData.birthday.toISOString().split('T')[0] : '',
      graduationYear: formData.graduationYear ? formData.graduationYear.getFullYear().toString() : '',
      location: formData.location.join('/'),
      expectedLocation: formData.expectedLocation.join('/')
    }

    await updateUserProfile(submitData)
    ElMessage.success('信息提交成功')
    isSubmitted.value = true // 标记为已提交
    dialogVisible.value = false
    emit('complete')
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.message || '提交失败')
  }
}
</script>

<style scoped>
.complete-info-dialog {
  :deep(.el-dialog) {
    margin-top: 5vh !important;
    height: 90vh;
    display: flex;
    flex-direction: column;
  }

  :deep(.el-dialog__header) {
    padding: 20px 20px 0;
    margin: 0;
  }

  :deep(.el-dialog__headerbtn) {
    font-size: 20px;
  }

  :deep(.el-dialog__body) {
    flex: 1;
    padding: 0;
    overflow: hidden;
  }
}

.dialog-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.form-header {
  padding: 20px 30px;
  text-align: center;
  flex-shrink: 0;
}

.form-header h3 {
  font-size: 24px;
  color: #333;
  margin-bottom: 12px;
}

.form-header p {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.form-scrollbar {
  flex: 1;
  padding: 0 30px;
}

.form-section {
  margin-bottom: 30px;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
  margin-bottom: 20px;
  padding-left: 12px;
  border-left: 4px solid var(--el-color-primary);
}

.form-footer {
  padding: 20px 30px;
  text-align: center;
  background: #fff;
  border-top: 1px solid #eee;
  flex-shrink: 0;
}

.w-full {
  width: 100%;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-button--large) {
  padding: 12px 40px;
  font-size: 16px;
}

/* 自定义滚动条样式 */
:deep(.el-scrollbar__bar) {
  z-index: 4;
}

:deep(.el-scrollbar__wrap) {
  padding-right: 10px;
}

@media screen and (max-width: 768px) {
  .complete-info-dialog {
    :deep(.el-dialog) {
      width: 90% !important;
    }
  }

  .form-section {
    padding: 15px;
  }

  .form-header {
    padding: 15px 20px;
  }

  .form-scrollbar {
    padding: 0 20px;
  }

  .form-footer {
    padding: 15px 20px;
  }
}
</style> 
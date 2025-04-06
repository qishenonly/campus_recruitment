<template>
  <div class="interview-management">
    <div class="page-header">
      <h2>面试管理</h2>
      <el-button type="primary" @click="handleCreateInterview">创建面试</el-button>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="filter-section">
      <el-select v-model="statusFilter" placeholder="面试状态" @change="handleFilterChange">
        <el-option label="全部" value="" />
        <el-option label="待安排" value="PENDING" />
        <el-option label="已安排" value="SCHEDULED" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELED" />
      </el-select>
      
      <el-input
        v-model="searchKeyword"
        placeholder="搜索候选人/职位"
        clearable
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 面试列表 -->
    <el-table
      v-loading="loading"
      :data="interviewList"
      style="width: 100%"
      border
    >
      <el-table-column prop="candidateName" label="候选人" min-width="120" />
      <el-table-column prop="jobTitle" label="应聘职位" min-width="150" />
      <el-table-column prop="interviewTime" label="面试时间" min-width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.interviewTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="interviewType" label="面试方式" min-width="120">
        <template #default="scope">
          <el-tag :type="getInterviewTypeTag(scope.row.type)">
            {{ getInterviewTypeText(scope.row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" min-width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="200">
        <template #default="scope">
          <el-button 
            type="primary" 
            link 
            @click="handleEdit(scope.row)"
          >
            编辑
          </el-button>
          <el-button 
            type="success" 
            link 
            @click="handleComplete(scope.row)"
            v-if="scope.row.status === 'SCHEDULED'"
          >
            完成
          </el-button>
          <el-button 
            type="danger" 
            link 
            @click="handleCancel(scope.row)"
            v-if="scope.row.status !== 'COMPLETED' && scope.row.status !== 'CANCELED'"
          >
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 面试表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑面试' : '创建面试'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="候选人" prop="candidateName">
          <el-input v-model="form.candidateName" placeholder="请输入候选人姓名" />
        </el-form-item>
        
        <el-form-item label="联系电话" prop="candidatePhone">
          <el-input v-model="form.candidatePhone" placeholder="请输入联系电话" />
        </el-form-item>
        
        <el-form-item label="应聘职位" prop="jobId">
          <el-select v-model="form.jobId" placeholder="请选择职位" style="width: 100%">
            <el-option
              v-for="job in jobOptions"
              :key="job.id"
              :label="job.title"
              :value="job.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="面试时间" prop="interviewTime">
          <el-date-picker
            v-model="form.interviewTime"
            type="datetime"
            placeholder="选择面试时间"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="面试方式" prop="interviewType">
          <el-select v-model="form.interviewType" placeholder="请选择面试方式" style="width: 100%">
            <el-option label="现场面试" value="ONSITE" />
            <el-option label="视频面试" value="ONLINE" />
            <el-option label="电话面试" value="PHONE" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="面试地点" prop="location" v-if="form.interviewType === 'ONSITE'">
          <el-input v-model="form.location" placeholder="请输入面试地点" />
        </el-form-item>
        
        <el-form-item label="视频链接" prop="location" v-if="form.interviewType === 'ONLINE'">
          <el-input v-model="form.location" placeholder="请输入视频会议链接" />
        </el-form-item>
        
        <el-form-item label="备注" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            rows="3"
            placeholder="请输入面试备注信息"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { format } from 'date-fns'
import { getCompanyInterviews, createInterview, updateInterview, updateInterviewStatus, checkCandidate } from '@/api/company-interviews'
import { getCompanyPublishedJobs } from '@/api/company-jobs'

// 数据
const loading = ref(false)
const interviewList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref('')
const searchKeyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const jobOptions = ref([])

// 表单
const formRef = ref(null)
const form = reactive({
  id: '',
  candidateName: '',
  candidatePhone: '',
  jobId: '',
  jobTitle: '',
  interviewTime: '',
  interviewType: 'ONSITE',
  location: '',
  description: '',
  status: 'PENDING'
})

// 表单验证规则
const rules = {
  candidateName: [{ required: true, message: '请输入候选人姓名', trigger: 'blur' }],
  candidatePhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  jobId: [{ required: true, message: '请选择职位', trigger: 'change' }],
  interviewTime: [{ required: true, message: '请选择面试时间', trigger: 'change' }],
  interviewType: [{ required: true, message: '请选择面试方式', trigger: 'change' }]
}

// 获取面试列表
const fetchInterviews = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value-1,
      size: pageSize.value,
      status: statusFilter.value,
      keyword: searchKeyword.value
    }
    
    // 实际项目中应该调用API
    const res = await getCompanyInterviews(params)
    interviewList.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    console.error('获取面试列表失败:', error)
    ElMessage.error('获取面试列表失败')
  } finally {
    loading.value = false
  }
}

// 获取职位列表
const fetchJobs = async () => {
  try {
    // 实际项目中应该调用API
    const res = await getCompanyPublishedJobs()
    console.log(res)
    jobOptions.value = res.data.content
  } catch (error) {
    console.error('获取职位列表失败:', error)
  }
}

// 处理筛选条件变化
const handleFilterChange = () => {
  currentPage.value = 1
  fetchInterviews()
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchInterviews()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchInterviews()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchInterviews()
}

// 创建面试
const handleCreateInterview = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑面试
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

// 完成面试
const handleComplete = (row) => {
  ElMessageBox.confirm('确认将此面试标记为已完成吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      // 实际项目中应该调用API
      // await updateInterviewStatus(row.id, 'COMPLETED')
      ElMessage.success('操作成功')
      fetchInterviews()
    } catch (error) {
      console.error('更新面试状态失败:', error)
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

// 取消面试
const handleCancel = (row) => {
  ElMessageBox.confirm('确认取消此面试吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateInterviewStatus(row.id, 'CANCELED')
      ElMessage.success('操作成功')
      fetchInterviews()
    } catch (error) {
      console.error('更新面试状态失败:', error)
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    id: '',
    candidateName: '',
    candidatePhone: '',
    jobId: '',
    jobTitle: '',
    interviewTime: '',
    interviewType: 'ONSITE',
    location: '',
    description: '',
    status: 'PENDING'
  })
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 检查候选人是否存在
        console.log(form.candidateName);
        const candidateExists = await checkCandidate(form.candidateName)
        if (!candidateExists.data) {
          ElMessage.error('候选人不存在')
          return
        }

        // 获取职位标题
        const job = jobOptions.value.find(item => item.id === form.jobId)
        if (job) {
          form.jobTitle = job.title
        }
        
        if (isEdit.value) {
          await updateInterview(form.id, form)
          ElMessage.success('更新成功')
        } else {
          await createInterview(form)
          ElMessage.success('创建成功')
        }
        
        dialogVisible.value = false
        fetchInterviews()
      } catch (error) {
        console.error('提交面试信息失败:', error)
        ElMessage.error('提交失败')
      }
    }
  })
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return format(new Date(dateTime), 'yyyy-MM-dd HH:mm')
}

// 获取面试方式标签类型
const getInterviewTypeTag = (type) => {
  const typeMap = {
    'ONSITE': 'success',
    'ONLINE': 'primary',
    'PHONE': 'info'
  }
  return typeMap[type] || ''
}

// 获取面试方式文本
const getInterviewTypeText = (type) => {
  console.log(type)
  const textMap = {
    'ONSITE': '现场面试',
    'ONLINE': '视频面试',
    'PHONE': '电话面试'
  }
  return textMap[type] || '未知'
}

// 获取状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'info',
    'ACCEPTED': 'primary',
    'COMPLETED': 'success',
    'REJECTED': 'danger',
    'CANCELED': 'danger'
  }
  return typeMap[status] || ''
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待安排',
    'ACCEPTED': '已安排',
    'COMPLETED': '已完成',
    'REJECTED': '已拒绝',
    'CANCELED': '已取消'
  }
  return textMap[status] || '未知'
}

onMounted(() => {
  fetchInterviews()
  fetchJobs()
})
</script>

<style scoped>
.interview-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-section {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.filter-section .el-select {
  width: 150px;
}

.filter-section .el-input {
  width: 250px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
    gap: 12px;
  }
  
  .filter-section .el-select,
  .filter-section .el-input {
    width: 100%;
  }
}
</style> 
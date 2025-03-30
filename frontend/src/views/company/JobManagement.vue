<template>
  <div class="job-management-page">
    <div class="page-header">
      <h2 class="page-title">职位管理</h2>
      <el-button type="primary" @click="handleAddJob">发布新职位</el-button>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="filter-section">
      <el-input
        v-model="searchQuery"
        placeholder="搜索职位名称"
        class="search-input"
        clearable
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-select v-model="statusFilter" placeholder="职位状态" @change="handleFilterChange">
        <el-option label="全部" value="" />
        <el-option label="已发布" value="PUBLISHED" />
        <el-option label="草稿" value="DRAFT" />
        <el-option label="已关闭" value="CLOSED" />
      </el-select>
    </div>

    <!-- 职位列表 -->
    <el-table
      v-loading="loading"
      :data="jobList"
      style="width: 100%"
      border
      stripe
      class="job-table"
    >
      <el-table-column prop="title" label="职位名称" min-width="180">
        <template #default="{ row }">
          <div class="job-title-cell">
            <span class="job-title">{{ row.title }}</span>
            <el-tag 
              :type="getStatusTagType(row.status)" 
              size="small"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="department" label="部门" width="120" />
      
      <el-table-column prop="salary" label="薪资" width="120" />
      
      <el-table-column prop="location" label="工作地点" width="120" />
      
      <el-table-column prop="applicantCount" label="申请人数" width="100" align="center">
        <template #default="{ row }">
          <el-button 
            type="primary" 
            link 
            @click="viewApplicants(row.id)"
          >
            {{ row.applicantCount || 0 }}
          </el-button>
        </template>
      </el-table-column>
      
      <el-table-column prop="publishTime" label="发布时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.publishTime) }}
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <div class="action-buttons">
            <el-button 
              type="primary" 
              link 
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            
            <el-button 
              v-if="row.status === 'PUBLISHED'"
              type="warning" 
              link 
              @click="handleStatusChange(row, 'CLOSED')"
            >
              关闭
            </el-button>
            
            <el-button 
              v-if="row.status === 'CLOSED'"
              type="success" 
              link 
              @click="handleStatusChange(row, 'PUBLISHED')"
            >
              重新发布
            </el-button>
            
            <el-button 
              type="danger" 
              link 
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 职位表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑职位' : '发布新职位'"
      width="60%"
      destroy-on-close
    >
      <job-form
        ref="jobFormRef"
        :form-data="formData"
        :is-edit="isEdit"
        @submit="handleFormSubmit"
        @cancel="dialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { format } from 'date-fns'
import { 
  getCompanyPublishedJobs, 
  createJob, 
  updateJob, 
  deleteJob, 
  updateJobStatus 
} from '@/api/company-jobs'
import JobForm from '../../components/JobForm.vue'

// 数据
const loading = ref(false)
const jobList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchQuery = ref('')
const statusFilter = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const jobFormRef = ref(null)

const formData = reactive({
  id: '',
  title: '',
  department: '',
  description: '',
  requirements: '',
  responsibilities: '',
  salary: '',
  location: '',
  positionType: '',
  educationRequirement: '',
  experienceRequirement: '',
  majorRequirement: '',
  benefits: '',
  status: 'PUBLISHED'
})

// 生命周期
onMounted(() => {
  fetchJobs()
})

// 方法
const fetchJobs = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      query: searchQuery.value,
      status: statusFilter.value
    }
    
    const res = await getCompanyPublishedJobs(params)
    jobList.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } catch (error) {
    console.error('获取职位列表失败:', error)
    ElMessage.error('获取职位列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchJobs()
}

const handleFilterChange = () => {
  currentPage.value = 1
  fetchJobs()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  fetchJobs()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchJobs()
}

const resetFormData = () => {
  Object.keys(formData).forEach(key => {
    if (key !== 'status') {
      formData[key] = ''
    } else {
      formData[key] = 'PUBLISHED'
    }
  })
}

const handleAddJob = () => {
  isEdit.value = false
  resetFormData()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.keys(formData).forEach(key => {
    formData[key] = row[key]
  })
  dialogVisible.value = true
}

const handleFormSubmit = async (data) => {
  try {
    if (isEdit.value) {
      await updateJob(data.id, data)
      ElMessage.success('职位更新成功')
    } else {
      await createJob(data)
      ElMessage.success('职位发布成功')
    }
    dialogVisible.value = false
    fetchJobs()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

const handleStatusChange = async (row, status) => {
  try {
    await updateJobStatus(row.id, status)
    ElMessage.success('状态更新成功')
    fetchJobs()
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除该职位吗？删除后无法恢复。',
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteJob(row.id)
      ElMessage.success('职位删除成功')
      fetchJobs()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const viewApplicants = (jobId) => {
  // 跳转到应聘者列表页面
  // router.push(`/job-applicants/${jobId}`)
  ElMessage.info('功能开发中')
}

const formatDate = (date) => {
  if (!date) return '-'
  return format(new Date(date), 'yyyy-MM-dd HH:mm')
}

const getStatusText = (status) => {
  const statusMap = {
    'DRAFT': '草稿',
    'PUBLISHED': '已发布',
    'CLOSED': '已关闭',
    'DELETED': '已删除'
  }
  return statusMap[status] || '未知状态'
}

const getStatusTagType = (status) => {
  const typeMap = {
    'DRAFT': 'info',
    'PUBLISHED': 'success',
    'CLOSED': 'warning',
    'DELETED': 'danger'
  }
  return typeMap[status] || ''
}
</script>

<style scoped>
.job-management-page {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.filter-section {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.search-input {
  width: 300px;
}

.job-table {
  margin-bottom: 20px;
}

.job-title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.job-title {
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
    gap: 12px;
  }
  
  .search-input {
    width: 100%;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style> 
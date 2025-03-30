<template>
  <div class="resume-management">
    <div class="page-header">
      <h2>简历管理</h2>
      <div class="filter-section">
        <el-select v-model="statusFilter" placeholder="简历状态" @change="handleFilterChange">
          <el-option label="全部" value="" />
          <el-option label="未查看" value="UNREAD" />
          <el-option label="已查看" value="VIEWED" />
          <el-option label="已联系" value="CONTACTED" />
          <el-option label="已面试" value="INTERVIEWED" />
          <el-option label="已录用" value="HIRED" />
          <el-option label="已拒绝" value="REJECTED" />
        </el-select>
        
        <el-select v-model="jobFilter" placeholder="职位筛选" @change="handleFilterChange">
          <el-option label="全部职位" value="" />
          <el-option 
            v-for="job in jobs" 
            :key="job.id" 
            :label="job.title" 
            :value="job.id" 
          />
        </el-select>
        
        <el-input
          v-model="searchKeyword"
          placeholder="搜索姓名/学校/专业"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <el-table
      v-loading="loading"
      :data="resumeList"
      style="width: 100%"
      @row-click="handleRowClick"
    >
      <el-table-column prop="applicantName" label="姓名" min-width="100" />
      <el-table-column prop="jobTitle" label="应聘职位" min-width="150" />
      <el-table-column prop="education" label="学历" min-width="100">
        <template #default="scope">
          <div>{{ scope.row.education }}</div>
          <div class="text-secondary">{{ scope.row.school }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="major" label="专业" min-width="120" />
      <el-table-column prop="applyTime" label="投递时间" min-width="120">
        <template #default="scope">
          {{ formatDate(scope.row.applyTime) }}
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
            @click.stop="viewResume(scope.row)"
          >
            查看
          </el-button>
          <el-button 
            type="primary" 
            link 
            @click.stop="downloadResume(scope.row.id)"
          >
            下载
          </el-button>
          <el-dropdown 
            @command="(command) => updateStatus(scope.row.id, command)"
            trigger="click"
            @click.stop
          >
            <el-button type="primary" link>
              更新状态<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="VIEWED">已查看</el-dropdown-item>
                <el-dropdown-item command="CONTACTED">已联系</el-dropdown-item>
                <el-dropdown-item command="INTERVIEWED">已面试</el-dropdown-item>
                <el-dropdown-item command="HIRED">已录用</el-dropdown-item>
                <el-dropdown-item command="REJECTED">已拒绝</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 简历详情对话框 -->
    <el-dialog
      v-model="resumeDialogVisible"
      title="简历详情"
      width="70%"
      destroy-on-close
    >
      <div v-if="currentResume" class="resume-detail">
        <div class="resume-header">
          <h3>{{ currentResume.applicantName }}</h3>
          <div class="resume-meta">
            <span>{{ currentResume.gender }}</span>
            <span>{{ currentResume.age }}岁</span>
            <span>{{ currentResume.education }}</span>
            <span>{{ currentResume.workExperience }}</span>
          </div>
          <div class="contact-info">
            <p><i class="el-icon-phone"></i> {{ currentResume.phone }}</p>
            <p><i class="el-icon-message"></i> {{ currentResume.email }}</p>
          </div>
        </div>

        <el-divider />

        <div class="resume-section">
          <h4>教育经历</h4>
          <div class="education-item" v-for="(edu, index) in currentResume.educationExperience" :key="index">
            <div class="edu-header">
              <span class="school">{{ edu.school }}</span>
              <span class="time">{{ edu.startTime }} - {{ edu.endTime }}</span>
            </div>
            <div class="edu-detail">
              <span class="major">{{ edu.major }}</span>
              <span class="degree">{{ edu.degree }}</span>
            </div>
          </div>
        </div>

        <el-divider />

        <div class="resume-section">
          <h4>工作经历</h4>
          <div class="work-item" v-for="(work, index) in currentResume.workExperiences" :key="index">
            <div class="work-header">
              <span class="company">{{ work.company }}</span>
              <span class="time">{{ work.startTime }} - {{ work.endTime }}</span>
            </div>
            <div class="position">{{ work.position }}</div>
            <div class="description">{{ work.description }}</div>
          </div>
        </div>

        <el-divider />

        <div class="resume-section">
          <h4>项目经历</h4>
          <div class="project-item" v-for="(project, index) in currentResume.projectExperiences" :key="index">
            <div class="project-header">
              <span class="project-name">{{ project.name }}</span>
              <span class="time">{{ project.startTime }} - {{ project.endTime }}</span>
            </div>
            <div class="role">{{ project.role }}</div>
            <div class="description">{{ project.description }}</div>
          </div>
        </div>

        <el-divider />

        <div class="resume-section">
          <h4>技能特长</h4>
          <div class="skills">
            <el-tag v-for="(skill, index) in currentResume.skills" :key="index" class="skill-tag">
              {{ skill }}
            </el-tag>
          </div>
        </div>

        <el-divider />

        <div class="resume-section">
          <h4>自我评价</h4>
          <p class="self-evaluation">{{ currentResume.selfEvaluation }}</p>
        </div>

        <el-divider />

        <div class="resume-section">
          <h4>求职意向</h4>
          <div class="job-intention">
            <p><strong>期望职位：</strong>{{ currentResume.jobIntention }}</p>
            <p><strong>期望薪资：</strong>{{ currentResume.expectedSalary }}</p>
            <p><strong>期望城市：</strong>{{ currentResume.expectedCity }}</p>
          </div>
        </div>

        <div class="resume-actions">
          <el-button type="primary" @click="contactApplicant(currentResume)">联系候选人</el-button>
          <el-button @click="downloadResume(currentResume.id)">下载简历</el-button>
          <el-dropdown @command="(command) => updateStatus(currentResume.id, command)">
            <el-button type="primary">
              更新状态<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="VIEWED">已查看</el-dropdown-item>
                <el-dropdown-item command="CONTACTED">已联系</el-dropdown-item>
                <el-dropdown-item command="INTERVIEWED">已面试</el-dropdown-item>
                <el-dropdown-item command="HIRED">已录用</el-dropdown-item>
                <el-dropdown-item command="REJECTED">已拒绝</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Search, ArrowDown } from '@element-plus/icons-vue'
import { showToast } from 'vant'
import { format } from 'date-fns'
import { getCompanyResumes, getResumeDetail, updateResumeStatus, downloadResume as downloadResumeAPI } from '@/api/company-resumes'
import { getCompanyPublishedJobs } from '@/api/company-jobs'

// 状态和筛选
const loading = ref(false)
const resumeList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const statusFilter = ref('')
const jobFilter = ref('')
const searchKeyword = ref('')
const jobs = ref([])

// 简历详情
const resumeDialogVisible = ref(false)
const currentResume = ref(null)

// 获取简历列表
const fetchResumeList = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      status: statusFilter.value,
      jobId: jobFilter.value,
      keyword: searchKeyword.value
    }
    
    const res = await getCompanyResumes(params)
    resumeList.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    console.error('获取简历列表失败:', error)
    showToast('获取简历列表失败')
  } finally {
    loading.value = false
  }
}

// 获取企业职位列表
const fetchJobs = async () => {
  try {
    const res = await getCompanyPublishedJobs()
    jobs.value = res.data
  } catch (error) {
    console.error('获取职位列表失败:', error)
  }
}

// 查看简历详情
const viewResume = async (resume) => {
  try {
    const res = await getResumeDetail(resume.id)
    currentResume.value = res.data
    resumeDialogVisible.value = true
    
    // 如果简历状态是未查看，自动更新为已查看
    if (resume.status === 'UNREAD') {
      await updateStatus(resume.id, 'VIEWED')
      // 更新列表中的状态
      const index = resumeList.value.findIndex(item => item.id === resume.id)
      if (index !== -1) {
        resumeList.value[index].status = 'VIEWED'
      }
    }
  } catch (error) {
    console.error('获取简历详情失败:', error)
    showToast('获取简历详情失败')
  }
}

// 下载简历
const downloadResume = async (resumeId) => {
  try {
    const res = await downloadResumeAPI(resumeId)
    const blob = new Blob([res.data], { type: 'application/pdf' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `简历_${Date.now()}.pdf`
    link.click()
    URL.revokeObjectURL(link.href)
    showToast('简历下载成功')
  } catch (error) {
    console.error('简历下载失败:', error)
    showToast('简历下载失败')
  }
}

// 更新简历状态
const updateStatus = async (resumeId, status) => {
  try {
    await updateResumeStatus(resumeId, status)
    showToast('状态更新成功')
    
    // 更新列表中的状态
    const index = resumeList.value.findIndex(item => item.id === resumeId)
    if (index !== -1) {
      resumeList.value[index].status = status
    }
    
    // 如果当前正在查看该简历，也更新详情中的状态
    if (currentResume.value && currentResume.value.id === resumeId) {
      currentResume.value.status = status
    }
  } catch (error) {
    console.error('更新状态失败:', error)
    showToast('更新状态失败')
  }
}

// 联系候选人
const contactApplicant = (resume) => {
  // 这里可以实现发起聊天或者发送邮件的功能
  showToast('联系功能开发中')
}

// 处理筛选变化
const handleFilterChange = () => {
  currentPage.value = 1
  fetchResumeList()
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchResumeList()
}

// 处理分页
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchResumeList()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchResumeList()
}

// 处理行点击
const handleRowClick = (row) => {
  viewResume(row)
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return format(new Date(date), 'yyyy-MM-dd HH:mm')
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'UNREAD': '未查看',
    'VIEWED': '已查看',
    'CONTACTED': '已联系',
    'INTERVIEWED': '已面试',
    'HIRED': '已录用',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || status
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'UNREAD': '',
    'VIEWED': 'info',
    'CONTACTED': 'warning',
    'INTERVIEWED': 'warning',
    'HIRED': 'success',
    'REJECTED': 'danger'
  }
  return typeMap[status] || ''
}

onMounted(() => {
  fetchResumeList()
  fetchJobs()
})
</script>

<style scoped>
.resume-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin-bottom: 16px;
}

.filter-section {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.filter-section .el-select,
.filter-section .el-input {
  width: 200px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.text-secondary {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

/* 简历详情样式 */
.resume-detail {
  padding: 0 20px;
}

.resume-header {
  margin-bottom: 20px;
}

.resume-header h3 {
  margin-bottom: 10px;
  font-size: 24px;
}

.resume-meta {
  display: flex;
  gap: 16px;
  color: #606266;
  margin-bottom: 10px;
}

.contact-info {
  color: #606266;
}

.resume-section {
  margin-bottom: 20px;
}

.resume-section h4 {
  margin-bottom: 12px;
  font-size: 18px;
  color: #303133;
}

.education-item, .work-item, .project-item {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px dashed #ebeef5;
}

.education-item:last-child, .work-item:last-child, .project-item:last-child {
  border-bottom: none;
}

.edu-header, .work-header, .project-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.school, .company, .project-name {
  font-weight: bold;
  color: #303133;
}

.time {
  color: #909399;
}

.major, .position, .role {
  color: #606266;
  margin-bottom: 8px;
}

.description {
  color: #606266;
  line-height: 1.6;
  white-space: pre-line;
}

.skills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skill-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.self-evaluation {
  color: #606266;
  line-height: 1.6;
  white-space: pre-line;
}

.job-intention {
  color: #606266;
  line-height: 1.6;
}

.resume-actions {
  margin-top: 30px;
  display: flex;
  gap: 16px;
  justify-content: center;
}

@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
  }
  
  .filter-section .el-select,
  .filter-section .el-input {
    width: 100%;
  }
}
</style> 
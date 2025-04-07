<template>
  <div class="applications-container">
    <el-card class="applications-card">
      <template #header>
        <div class="card-header">
          <span>我的投递记录</span>
        </div>
      </template>
      
      <el-empty v-if="loading" description="加载中...">
        <el-skeleton :rows="5" animated />
      </el-empty>
      
      <el-empty v-else-if="applications.length === 0" description="暂无投递记录">
        <el-button type="primary" @click="$router.push('/home')">去投递简历</el-button>
      </el-empty>
      
      <template v-else>
        <div class="application-list">
          <div 
            v-for="item in applications" 
            :key="item.id" 
            class="application-item"
          >
            <div class="application-header">
              <div class="job-info">
                <div class="job-title" @click="goToJobDetail(item.jobId)">
                  {{ item.jobTitle || '未知职位' }}
                </div>
                <div class="company-info">
                  <div class="company-name" @click="goToCompanyDetail(item.companyId)">
                    {{ item.companyName || '未知公司' }}
                  </div>
                  <div class="company-extra" v-if="item.salary || item.location">
                    <span class="salary" v-if="item.salary">{{ item.salary }}</span>
                    <span class="divider" v-if="item.salary && item.location">·</span>
                    <span class="location" v-if="item.location">{{ item.location }}</span>
                  </div>
                </div>
              </div>
              <div class="application-status" :class="getStatusClass(item.status)">
                {{ getStatusText(item.status) }}
              </div>
            </div>
            
            <div class="application-info">
              <div class="info-item">
                <span class="label">投递时间：</span>
                <span class="value">{{ formatDate(item.createTime) }}</span>
              </div>
              <div class="info-item" v-if="item.updateTime && item.updateTime !== item.createTime">
                <span class="label">更新时间：</span>
                <span class="value">{{ formatDate(item.updateTime) }}</span>
              </div>
              <div class="info-item">
                <span class="label">投递简历：</span>
                <span class="value">{{ item.resumeName || '未知简历' }}</span>
              </div>
            </div>
            
            <div class="application-actions">
              <el-button 
                type="primary" 
                size="small" 
                @click="goToChat(item)"
                :disabled="!item.conversationId"
              >
                查看沟通
              </el-button>
              <el-button 
                v-if="item.status === 'PENDING'" 
                type="danger" 
                size="small" 
                @click="withdrawApplication(item.id)"
              >
                撤回投递
              </el-button>
            </div>
          </div>
        </div>
        
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getApplications, withdrawApplication as withdrawApplicationApi, getCompanyByJobId } from '@/api/jobs'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const applications = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取投递记录
const fetchApplications = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1, // 后端从0开始
      size: pageSize.value,
      includeJobInfo: true, // 确保包含职位信息
      includeCompanyInfo: true // 确保包含公司信息
    }
    
    console.log('请求参数:', params)
    const res = await getApplications(params)
    console.log('投递记录原始数据:', res)
    
    if (res.code === 200) {
      // 处理返回的数据
      applications.value = res.data.content.map(app => {
        console.log('处理应用记录:', app)
        return {
          ...app,
          // 确保字段存在，处理null值
          jobTitle: app.jobTitle || app.job?.title || '未知职位',
          companyName: app.companyName || app.company?.name || '未知公司',
          resumeName: app.resumeName || '简历',
          status: app.status || 'PENDING',
          // 添加其他可能的字段
          salary: app.salary || app.job?.salary || '',
          location: app.location || app.job?.location || ''
        }
      })
      
      // 如果某些记录缺少公司信息或职位信息，尝试使用API获取
      await Promise.all(applications.value.map(async (app, index) => {
        if ((app.companyName === '未知公司' || !app.companyLogo || app.jobTitle === '未知职位') && app.jobId) {
          try {
            console.log(`尝试获取职位ID=${app.jobId}的信息`);
            const response = await getCompanyByJobId(app.jobId);
            if (response.code === 200 && response.data) {
              console.log(`获取到职位ID=${app.jobId}的信息:`, response.data);
              
              // 更新公司信息
              if (response.data.company) {
                applications.value[index].companyName = response.data.company.name || app.companyName;
                applications.value[index].companyLogo = response.data.company.logo || app.companyLogo;
                applications.value[index].industry = response.data.company.industry || app.industry;
                
                // 如果仍然缺少地址信息，可以从公司信息中获取
                if (!applications.value[index].location) {
                  applications.value[index].location = response.data.company.city ? 
                    (Array.isArray(response.data.company.city) ? 
                      response.data.company.city[0] : response.data.company.city) : '';
                }
              }
              
              // 更新职位信息
              if (response.data.job) {
                applications.value[index].jobTitle = response.data.job.title || app.jobTitle;
                if (!applications.value[index].salary) {
                  applications.value[index].salary = response.data.job.salary || '';
                }
                if (!applications.value[index].location) {
                  applications.value[index].location = response.data.job.city || '';
                }
              }
            }
          } catch (error) {
            console.error(`获取职位ID=${app.jobId}的信息失败:`, error);
          }
        }
      }));
      
      total.value = res.data.totalElements
    } else {
      ElMessage.error(res.message || '获取投递记录失败')
    }
  } catch (error) {
    console.error('获取投递记录出错:', error)
    ElMessage.error('获取投递记录失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 状态样式
const getStatusClass = (status) => {
  const classMap = {
    PENDING: 'status-pending',
    REVIEWING: 'status-reviewing',
    ACCEPTED: 'status-accepted',
    REJECTED: 'status-rejected',
    WITHDRAWN: 'status-withdrawn'
  }
  return classMap[status] || 'status-pending'
}

// 状态文本
const getStatusText = (status) => {
  const textMap = {
    PENDING: '待处理',
    REVIEWING: '审核中',
    ACCEPTED: '已接受',
    REJECTED: '已拒绝',
    WITHDRAWN: '已撤回'
  }
  return textMap[status] || '待处理'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '未知'
  
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  
  const now = new Date()
  const diffMs = now - date
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
  
  if (diffDays === 0) {
    // 今天
    return `今天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  } else if (diffDays === 1) {
    // 昨天
    return `昨天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  } else if (diffDays < 7) {
    // 一周内
    return `${diffDays}天前`
  } else {
    // 更早
    return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
  }
}

// 撤回申请
const withdrawApplication = async (id) => {
  try {
    await ElMessageBox.confirm('确定要撤回投递吗? 撤回后无法恢复', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    loading.value = true
    const res = await withdrawApplicationApi(id)
    loading.value = false
    
    if (res.code === 200) {
      ElMessage.success('撤回成功')
      // 刷新列表
      await fetchApplications()
    } else {
      ElMessage.error(res.message || '撤回投递失败')
    }
  } catch (error) {
    loading.value = false
    if (error !== 'cancel') {
      console.error('撤回投递失败:', error)
      ElMessage.error('撤回投递失败: ' + (error.message || '未知错误'))
    }
  }
}

// 页面跳转
const goToJobDetail = (jobId) => {
  router.push(`/job/${jobId}`)
}

const goToCompanyDetail = (companyId) => {
  router.push(`/company/${companyId}`)
}

const goToChat = (item) => {
  if (!item.conversationId) {
    ElMessage.warning('还没有对话，请等待企业回应')
    return
  }
  
  console.log('从投递记录跳转到聊天，带参数：', {
    chatId: item.conversationId,
    companyId: item.companyId,
    companyName: item.companyName,
    jobTitle: item.jobTitle,
    jobId: item.jobId
  });
  
  router.push({
    name: 'chat',
    params: {
      chatId: item.conversationId
    },
    query: {
      companyId: item.companyId,
      companyName: item.companyName,
      companyLogo: item.companyLogo || '',
      jobTitle: item.jobTitle || '未知职位',
      jobId: item.jobId
    }
  })
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchApplications()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchApplications()
}

onMounted(() => {
  fetchApplications()
})
</script>

<style scoped>
.applications-container {
  padding: 20px;
}

.applications-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.application-list {
  margin-top: 10px;
}

.application-item {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
  transition: all 0.3s;
}

.application-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.application-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.job-info {
  flex: 1;
}

.job-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
  cursor: pointer;
  color: #409EFF;
}

.job-title:hover {
  text-decoration: underline;
}

.company-info {
  display: flex;
  align-items: center;
  margin-top: 3px;
}

.company-name {
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  margin-right: 10px;
}

.company-name:hover {
  color: #409EFF;
}

.company-extra {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.salary {
  color: #f56c6c;
}

.divider {
  margin: 0 5px;
}

.location {
  color: #909399;
}

.application-status {
  font-size: 14px;
  font-weight: bold;
  padding: 4px 8px;
  border-radius: 4px;
}

.status-pending {
  color: #E6A23C;
  background-color: rgba(230, 162, 60, 0.1);
}

.status-reviewing {
  color: #409EFF;
  background-color: rgba(64, 158, 255, 0.1);
}

.status-accepted {
  color: #67C23A;
  background-color: rgba(103, 194, 58, 0.1);
}

.status-rejected {
  color: #F56C6C;
  background-color: rgba(245, 108, 108, 0.1);
}

.status-withdrawn {
  color: #909399;
  background-color: rgba(144, 147, 153, 0.1);
}

.application-info {
  margin: 10px 0;
  font-size: 14px;
}

.info-item {
  margin-bottom: 5px;
  display: flex;
}

.label {
  color: #909399;
  width: 80px;
}

.value {
  color: #606266;
  flex: 1;
}

.application-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 
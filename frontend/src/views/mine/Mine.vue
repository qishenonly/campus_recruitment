<template>
  <div class="mine-page">
    <!-- 个人信息卡片 -->
    <div class="user-card">
      <div class="user-info">
        <div class="avatar-wrapper">
          <img 
            :src="userInfo.avatar || 'https://placeholder.com/150'" 
            class="avatar"
            alt="用户头像"
          />
          <div class="upload-mask">
            <van-icon name="photograph" />
            <span>更换头像</span>
          </div>
        </div>
        <div class="info-content">
          <h2 class="username">{{ userInfo.name || '未登录' }}</h2>
          <p class="user-desc">{{ userInfo.title || '添加个人介绍' }}</p>
          <div class="user-tags">
            <span class="tag" v-for="tag in userInfo.tags" :key="tag">{{ tag }}</span>
          </div>
        </div>
      </div>
      <div class="user-stats">
        <div class="stat-item">
          <span class="stat-num">{{ statistics.views }}</span>
          <span class="stat-label">简历被查看</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ statistics.delivers }}</span>
          <span class="stat-label">投递记录</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ statistics.collects }}</span>
          <span class="stat-label">职位收藏</span>
        </div>
      </div>
    </div>

    <!-- 功能区域 -->
    <div class="function-area">
      <!-- 简历管理 -->
      <div class="section-card">
        <div class="section-header">
          <h3>我的简历</h3>
          <van-button type="primary" size="small" icon="plus" @click="createResume">
            创建简历
          </van-button>
        </div>
        <div class="resume-list" v-if="resumes.length">
          <div 
            v-for="resume in resumes" 
            :key="resume.id" 
            class="resume-item"
          >
            <div class="resume-info">
              <h4>{{ resume.name }}</h4>
              <p class="resume-update-time">最后更新：{{ formatTime(resume.updateTime) }}</p>
              <div class="resume-progress">
                <div class="progress-bar">
                  <div 
                    class="progress-inner" 
                    :style="{ width: resume.completeness + '%' }"
                  ></div>
                </div>
                <span class="progress-text">完整度 {{ resume.completeness }}%</span>
              </div>
            </div>
            <div class="resume-actions">
              <van-button type="primary" plain size="small" @click="editResume(resume.id)">
                编辑
              </van-button>
              <van-button type="danger" plain size="small" @click="deleteResume(resume.id)">
                删除
              </van-button>
            </div>
          </div>
        </div>
        <div v-else class="empty-block">
          <van-empty description="暂无简历" />
        </div>
      </div>

      <!-- 投递记录 -->
      <div class="section-card">
        <div class="section-header">
          <h3>投递记录</h3>
          <router-link to="/delivers" class="view-all">
            查看全部
            <van-icon name="arrow" />
          </router-link>
        </div>
        <div class="deliver-list" v-if="delivers.length">
          <div 
            v-for="deliver in delivers" 
            :key="deliver.id" 
            class="deliver-item"
          >
            <div class="deliver-info">
              <h4>{{ deliver.jobTitle }}</h4>
              <p class="company-name">{{ deliver.companyName }}</p>
              <p class="deliver-time">投递时间：{{ formatTime(deliver.deliverTime) }}</p>
            </div>
            <div class="deliver-status" :class="deliver.status">
              {{ getStatusText(deliver.status) }}
            </div>
          </div>
        </div>
        <div v-else class="empty-block">
          <van-empty description="暂无投递记录" />
        </div>
      </div>

      <!-- 收藏职位 -->
      <div class="section-card">
        <div class="section-header">
          <h3>收藏职位</h3>
          <router-link to="/collections" class="view-all">
            查看全部
            <van-icon name="arrow" />
          </router-link>
        </div>
        <div class="collection-list" v-if="collections.length">
          <div 
            v-for="job in collections" 
            :key="job.id" 
            class="collection-item"
          >
            <div class="job-info">
              <h4>{{ job.title }}</h4>
              <p class="company-name">{{ job.companyName }}</p>
              <p class="job-salary">{{ job.salary }}K</p>
            </div>
            <div class="collection-actions">
              <van-button type="primary" size="small" @click="applyJob(job.id)">
                投递简历
              </van-button>
            </div>
          </div>
        </div>
        <div v-else class="empty-block">
          <van-empty description="暂无收藏职位" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { formatDistance } from 'date-fns'
import { zhCN } from 'date-fns/locale'

const router = useRouter()

// 用户信息
const userInfo = ref({
  name: '张三',
  avatar: '',
  title: '应届生 | 计算机科学与技术',
  tags: ['Java', 'Spring Boot', 'Vue.js']
})

// 统计数据
const statistics = ref({
  views: 12,
  delivers: 5,
  collects: 8
})

// 简历列表
const resumes = ref([
  {
    id: 1,
    name: '个人简历.pdf',
    updateTime: '2024-01-20T10:00:00',
    completeness: 85
  }
])

// 投递记录
const delivers = ref([
  {
    id: 1,
    jobTitle: '前端开发工程师',
    companyName: 'XX科技有限公司',
    deliverTime: '2024-01-19T15:30:00',
    status: 'pending'
  }
])

// 收藏的职位
const collections = ref([
  {
    id: 1,
    title: 'Java开发工程师',
    companyName: 'XX科技有限公司',
    salary: '15-25'
  }
])

// 格式化时间
const formatTime = (time) => {
  return formatDistance(new Date(time), new Date(), {
    addSuffix: true,
    locale: zhCN
  })
}

// 获取投递状态文本
const getStatusText = (status) => {
  const statusMap = {
    pending: '待处理',
    reviewing: '审核中',
    interviewed: '已面试',
    rejected: '不合适',
    accepted: '已录用'
  }
  return statusMap[status] || status
}

// 创建简历
const createResume = () => {
  router.push('/resume/create')
}

// 编辑简历
const editResume = (id) => {
  router.push(`/resume/edit/${id}`)
}

// 删除简历
const deleteResume = async (id) => {
  try {
    // TODO: 调用删除简历 API
    console.log('删除简历:', id)
  } catch (error) {
    console.error('删除简历失败:', error)
  }
}

// 投递简历
const applyJob = async (jobId) => {
  try {
    // TODO: 调用投递简历 API
    console.log('投递职位:', jobId)
  } catch (error) {
    console.error('投递简历失败:', error)
  }
}
</script>

<style scoped>
.mine-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.user-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.user-info {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
}

.avatar-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 60px;
  overflow: hidden;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-mask {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 8px;
  text-align: center;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
  cursor: pointer;
}

.avatar-wrapper:hover .upload-mask {
  opacity: 1;
}

.info-content {
  flex: 1;
}

.username {
  margin: 0 0 8px;
  font-size: 24px;
  color: #333;
}

.user-desc {
  margin: 0 0 16px;
  color: #666;
  font-size: 16px;
}

.user-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag {
  padding: 4px 12px;
  background: #f5f5f5;
  border-radius: 16px;
  font-size: 14px;
  color: #666;
}

.user-stats {
  display: flex;
  border-top: 1px solid #f0f0f0;
  padding-top: 24px;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-num {
  display: block;
  font-size: 24px;
  font-weight: 500;
  color: var(--primary-color);
  margin-bottom: 8px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.function-area {
  display: grid;
  gap: 24px;
}

.section-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.view-all {
  color: #666;
  text-decoration: none;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.resume-list,
.deliver-list,
.collection-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.resume-item,
.deliver-item,
.collection-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  transition: all 0.3s;
}

.resume-item:hover,
.deliver-item:hover,
.collection-item:hover {
  background: #f0f0f0;
}

.resume-info,
.deliver-info,
.job-info {
  flex: 1;
}

.resume-info h4,
.deliver-info h4,
.job-info h4 {
  margin: 0 0 8px;
  font-size: 16px;
  color: #333;
}

.resume-update-time,
.deliver-time,
.company-name {
  margin: 0;
  font-size: 14px;
  color: #999;
}

.resume-progress {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
}

.progress-inner {
  height: 100%;
  background: var(--primary-color);
  transition: width 0.3s;
}

.progress-text {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
}

.resume-actions,
.collection-actions {
  display: flex;
  gap: 8px;
}

.deliver-status {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 14px;
}

.deliver-status.pending {
  background: #e6f7ff;
  color: var(--primary-color);
}

.deliver-status.reviewing {
  background: #fff7e6;
  color: var(--warning-color);
}

.deliver-status.interviewed {
  background: #f6ffed;
  color: var(--success-color);
}

.deliver-status.rejected {
  background: #fff1f0;
  color: var(--error-color);
}

.deliver-status.accepted {
  background: #f6ffed;
  color: var(--success-color);
}

.empty-block {
  padding: 40px 0;
  text-align: center;
}

@media (max-width: 768px) {
  .user-info {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .user-stats {
    flex-wrap: wrap;
    gap: 16px;
  }

  .resume-item,
  .deliver-item,
  .collection-item {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .resume-actions,
  .collection-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style> 
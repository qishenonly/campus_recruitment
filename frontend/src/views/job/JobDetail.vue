<template>
  <div class="job-detail" v-if="jobInfo">
    <!-- 职位基本信息 -->
    <div class="job-header">
      <div class="job-basic">
        <h1 class="job-title">{{ jobInfo.title }}</h1>
        <div class="job-salary">{{ jobInfo.salary }}</div>
      </div>
      <div class="job-tags">
        <span class="tag">{{ jobInfo.location }}</span>
        <span class="tag">{{ jobInfo.educationRequirement }}</span>
        <span class="tag">{{ jobInfo.positionType }}</span>
        <span class="tag">{{ jobInfo.majorRequirement }}</span>
      </div>
      <div class="company-info">
        <img :src="jobInfo.companyLogo" :alt="jobInfo.companyName" class="company-logo">
        <div class="company-basic">
          <div class="company-title">
            <h3 class="company-name">{{ jobInfo.companyName }}</h3>
            <span v-if="jobInfo.companyVerified" class="verified-badge">
              <van-icon name="success" />认证企业
            </span>
          </div>
          <div class="company-tags">
            <span class="company-tag">{{ jobInfo.industry }}</span>
            <span class="company-tag">{{ jobInfo.companyScale }}</span>
          </div>
        </div>
      </div>
      <div class="publisher-info">
        <span class="publisher-name">{{ jobInfo.publisherName }}</span>
        <span class="publisher-position">{{ jobInfo.publisherPosition }}</span>
      </div>
    </div>

    <!-- 职位操作按钮 -->
    <div class="job-actions">
      <van-button type="primary" size="large" @click="handleApply">
        立即投递
      </van-button>
      <van-button 
        :type="isCollected ? 'warning' : 'default'"
        size="large" 
        @click="toggleCollect"
        class="collect-btn"
      >
        <van-icon 
          :name="isCollected ? 'star' : 'star-o'" 
          :class="{ 'collected-icon': isCollected }"
        />
        <span :class="{ 'collected-text': isCollected }">
          {{ isCollected ? '取消收藏' : '收藏职位' }}
        </span>
      </van-button>
    </div>

    <!-- 职位详情 -->
    <div class="detail-content">
      <div class="section">
        <h2 class="section-title">职位描述</h2>
        <div class="job-description">{{ jobInfo.description }}</div>
      </div>

      <div class="section">
        <h2 class="section-title">职位要求</h2>
        <div class="job-requirements" v-html="formatRequirements"></div>
      </div>

      <div class="section">
        <h2 class="section-title">工作地址</h2>
        <div class="job-address">
          <van-icon name="location-o" />
          <span>{{ jobInfo.location }}</span>
        </div>
      </div>
    </div>

    <!-- 相似职位推荐 -->
    <div class="similar-jobs">
      <h2 class="section-title">相似职位</h2>
      <job-list :jobs="similarJobs" />
    </div>

    <!-- 添加投递按钮 -->
    <div class="action-bar">
      <el-button 
        type="primary" 
        :loading="applying"
        @click="handleApply"
      >
        投递简历
      </el-button>
    </div>
  </div>
  <div v-else class="loading-wrapper">
    <van-loading type="spinner" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getJobDetail, favoriteJob, unfavoriteJob, getFavoriteStatus } from '@/api/jobs'
import JobList from '@/components/JobList.vue'
import { ElMessage } from 'element-plus'
import { applyJob } from '@/api/jobs'
import { getResume } from '@/api/resume'
import { sendMessageAPI } from '@/api/messages'
const route = useRoute()
const router = useRouter()
const jobInfo = ref(null)
const similarJobs = ref([])
const isCollected = ref(false)
const applying = ref(false)

const formatRequirements = computed(() => {
  if (!jobInfo.value?.requirements) return ''
  return jobInfo.value.requirements.split('\n').join('<br>')
})

const checkFavoriteStatus = async (jobId) => {
  return getFavoriteStatus(jobId)
}

const fetchJobDetail = async () => {
  try {
    const id = route.params.id
    const [jobResult, favoriteResult] = await Promise.all([
      getJobDetail(id),
      checkFavoriteStatus(id)
    ])
    
    jobInfo.value = jobResult.data
    isCollected.value = favoriteResult.data
  } catch (error) {
    console.error('获取职位详情失败:', error)
    showToast('获取职位详情失败')
  }
}

const fetchSimilarJobs = async () => {
  try {
    // TODO: 调用获取相似职位 API
    similarJobs.value = []
  } catch (error) {
    console.error('获取相似职位失败:', error)
  }
}

// 从路由参数中获取职位ID
const jobId = computed(() => route.params.id)

// 添加简历相关的状态
const selectedResume = ref(null)
// 修改默认求职信为动态生成
const coverLetter = computed(() => {
  return `您好，我对贵公司的"${jobInfo.value?.title || '该职位'}"职位很感兴趣，已投递简历，期待您的回复。`
})

const handleApply = async () => {
  try {
    applying.value = true
    const resumeRes = await getResume()
    if (!resumeRes.data) {
      ElMessage.warning('请先上传简历')
      router.push('/resume')
      return
    }

    console.log('投递职位：', jobInfo.value);
    console.log('将要发送的职位信息：', {
      jobTitle: jobInfo.value?.title,
      jobId: jobId.value
    });

    // 投递简历
    const res = await applyJob(jobId.value, resumeRes.data.id, coverLetter.value)

    if (res.code === 200) {
      console.log('简历投递成功，返回数据：', res.data);
      // 获取会话 ID
      const conversationId = res.data.conversationId
      
      console.log('发送消息到会话：', conversationId, '职位信息：', {
        jobTitle: jobInfo.value?.title,
        jobId: jobId.value
      });
      
      // 发送带简历链接的消息，添加职位名称
      await sendMessageAPI(conversationId, {
        content: `这是我投递"${jobInfo.value?.title || '该职位'}"的简历，您可以查看`,
        type: 'resume',
        resumeId: resumeRes.data.id,
        jobTitle: jobInfo.value?.title, // 传递职位名称
        jobId: jobId.value // 传递职位ID
      })
      
      showToast({
        message: '投递成功',
        type: 'success'
      })

      console.log('跳转到聊天页面，带参数：', {
        chatId: res.data.conversation.id,
        jobTitle: jobInfo.value?.title,
        jobId: jobId.value
      });

      // 跳转到聊天页面，添加职位信息
      router.push({
        name: 'chat',
        params: {
          chatId: res.data.conversation.id
        },
        query: {
          companyId: res.data.companyId,
          companyName: res.data.companyName,
          companyLogo: jobInfo.value.companyLogo,
          jobTitle: jobInfo.value?.title,
          jobId: jobId.value
        }
      })
    }
  } catch (error) {
    console.error('投递失败:', error)
    showToast({
      message: error.message || '投递失败',
      type: 'fail'
    })
  } finally {
    applying.value = false
  }
}

const toggleCollect = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      showToast('请先登录')
      router.push('/login')
      return
    }

    if (isCollected.value) {
      await unfavoriteJob(jobInfo.value.id)
      isCollected.value = false
      showToast('已取消收藏')
    } else {
      await favoriteJob(jobInfo.value.id)
      isCollected.value = true
      showToast('收藏成功')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    showToast(error.response?.data?.message || '操作失败')
  }
}

onMounted(() => {
  console.log('Component mounted') // 调试日志
  fetchJobDetail()
  fetchSimilarJobs()
})
</script>

<style scoped>
.job-detail {
  max-width: 1200px;
  margin: 24px auto;
  padding: 0 20px;
}

.job-header {
  background: white;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 16px;
}

.job-basic {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.job-title {
  font-size: 24px;
  color: #333;
  margin: 0;
}

.job-salary {
  color: #ff4d4f;
  font-size: 20px;
  font-weight: 500;
}

.job-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.tag {
  padding: 4px 12px;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 14px;
  color: #666;
}

.company-info {
  display: flex;
  gap: 16px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.company-logo {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
}

.company-basic {
  flex: 1;
}

.company-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.company-name {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.verified-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #52c41a;
  font-size: 14px;
}

.company-tags {
  display: flex;
  gap: 8px;
}

.company-tag {
  font-size: 14px;
  color: #666;
}

.job-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  padding: 16px;
}

.job-actions .van-button {
  flex: 1;
  height: 44px;
}

.collect-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.collect-btn.van-button--warning {
  background-color: #ff9800;
  border-color: #ff9800;
}

.collected-icon,
.collected-text {
  color: #ffffff !important;
}

.van-icon {
  font-size: 16px;
  vertical-align: middle;
}

.detail-content {
  background: white;
  border-radius: 8px;
  padding: 24px;
}

.section {
  margin-bottom: 32px;
}

.section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 18px;
  color: #333;
  margin: 0 0 16px;
}

.job-description,
.job-requirements {
  font-size: 14px;
  line-height: 1.6;
  color: #666;
}

.job-address {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
}

.similar-jobs {
  background: white;
  border-radius: 8px;
  padding: 24px;
}

.loading-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
  background: white;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
  z-index: 10;
}

.publisher-info {
  margin: 12px 0;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.publisher-name {
  color: var(--primary-color);
  font-weight: 500;
}

.publisher-position {
  color: #666;
}

.publish-time {
  color: #999;
}

@media (max-width: 768px) {
  .job-actions {
    flex-direction: column;
  }

  .job-basic {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style> 
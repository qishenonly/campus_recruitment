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
    </div>

    <!-- 职位操作按钮 -->
    <div class="job-actions">
      <van-button type="primary" size="large" @click="handleApply">
        立即投递
      </van-button>
      <van-button plain size="large" @click="toggleCollect">
        {{ isCollected ? '取消收藏' : '收藏职位' }}
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
  </div>
  <div v-else class="loading-wrapper">
    <van-loading type="spinner" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getJobDetail } from '../../api/jobs'
import JobList from '@/components/JobList.vue'

const route = useRoute()
const router = useRouter()
const jobInfo = ref(null)
const similarJobs = ref([])
const isCollected = ref(false)

const formatRequirements = computed(() => {
  if (!jobInfo.value?.requirements) return ''
  return jobInfo.value.requirements.split('\n').join('<br>')
})

const fetchJobDetail = async () => {
  try {
    const id = route.params.id
    console.log('Fetching job detail for id:', id) // 调试日志
    const result = await getJobDetail(id)
    console.log('Job detail result:', result) // 调试日志
    jobInfo.value = result.data
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

const handleApply = async () => {
  try {
    showToast('投递成功')
  } catch (error) {
    showToast('投递失败')
  }
}

const toggleCollect = async () => {
  try {
    isCollected.value = !isCollected.value
    showToast(isCollected.value ? '收藏成功' : '已取消收藏')
  } catch (error) {
    showToast('操作失败')
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
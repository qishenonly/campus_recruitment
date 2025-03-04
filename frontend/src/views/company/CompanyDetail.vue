<template>
  <div class="company-detail-page">
    <div class="company-info">
      <img :src="companyLogo" alt="公司Logo" class="company-logo" />
      <div class="company-details">
        <h1>{{ companyName }}</h1>
        <p>{{ companyDescription }}</p>
        <div class="company-tags">
          <span>{{ industry }}</span>
          <span>{{ scale }}</span>
          <span v-if="verified" class="verified">认证企业</span>
        </div>
      </div>
    </div>
    <h2>在招职位</h2>
    <div class="job-list">
      <div 
        v-for="job in jobs" 
        :key="job.id" 
        class="job-card"
        @click="goToJobDetail(job.id)"
      >
        <div class="job-header">
          <h3>{{ job.title }}</h3>
          <span class="salary">{{ job.salary }}</span>
        </div>
        <div class="publisher-info">
          <span class="publisher-name">{{ job.publisherName }}</span>
          <span class="publisher-position">{{ job.publisherPosition }}</span>
          <span class="publish-time">{{ formatPublishTime(job.publishTime) }}</span>
        </div>
        <div class="job-requirements">
          <strong>职位要求:</strong>
          <p>{{ job.requirements }}</p>
        </div>
        <div class="job-details">
          <span>薪资: {{ job.salary }}</span>
          <span>地点: {{ job.location }}</span>
          <span>职位类型: {{ job.positionType }}</span>
          <span>学历要求: {{ job.educationRequirement }}</span>
          <span>专业要求: {{ job.majorRequirement }}</span>
          <span>发布日期: {{ new Date(job.publishDate).toLocaleDateString() }}</span>
          <span>截止日期: {{ new Date(job.deadline).toLocaleDateString() }}</span>
        </div>
        <div class="job-tags">
          <span>{{ job.location }}</span>
          <span>{{ job.educationRequirement }}</span>
          <span>{{ job.positionType }}</span>
          <span>{{ job.majorRequirement }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCompanyJobs } from '@/api/jobs'
import { format } from 'date-fns'

const route = useRoute()
const router = useRouter()
const companyName = ref('')
const companyLogo = ref('')
const companyDescription = ref('')
const industry = ref('')
const scale = ref('')
const verified = ref(false)
const jobs = ref([])

const fetchCompanyJobs = async () => {
  try {
    const companyId = route.params.id
    const response = await getCompanyJobs(companyId)
    companyName.value = response.data.companyName
    companyLogo.value = response.data.companyLogo
    companyDescription.value = response.data.description
    industry.value = response.data.industry
    scale.value = response.data.scale
    verified.value = response.data.verified
    jobs.value = response.data.content
  } catch (error) {
    console.error('获取企业职位列表失败:', error)
  }
}

const goToJobDetail = (jobId) => {
  router.push(`/job/${jobId}`)
}

// 格式化发布时间
const formatPublishTime = (time) => {
  if (!time) return ''
  return format(new Date(time), 'yyyy-MM-dd')
}

onMounted(() => {
  fetchCompanyJobs()
})
</script>

<style scoped>
.company-detail-page {
  padding: 20px;
}

.company-info {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.company-logo {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  margin-right: 20px;
}

.company-details {
  flex: 1;
}

.company-tags {
  margin-top: 10px;
  display: flex;
  gap: 10px;
}

.company-tags span {
  background: #e0e0e0;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 14px;
}

.verified {
  background: #52c41a;
  color: white;
}

.job-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.job-card {
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.job-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.job-tags {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.job-tags span {
  background: #f5f5f5;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 14px;
}

.publisher-info {
  margin: 8px 0;
  display: flex;
  align-items: center;
  gap: 8px;
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
</style> 
<template>
  <div class="job-detail">
    <!-- 职位基本信息 -->
    <div class="job-header">
      <div class="job-basic">
        <h1 class="job-title">{{ jobInfo.title }}</h1>
        <div class="job-salary">{{ jobInfo.salary }}K</div>
      </div>
      <div class="job-tags">
        <span class="tag">{{ jobInfo.city }}</span>
        <span class="tag">{{ jobInfo.experience }}</span>
        <span class="tag">{{ jobInfo.education }}</span>
      </div>
      <div class="company-info">
        <img :src="jobInfo.companyLogo" :alt="jobInfo.companyName" class="company-logo">
        <div class="company-basic">
          <h3 class="company-name">{{ jobInfo.companyName }}</h3>
          <p class="company-desc">{{ jobInfo.companyIndustry }} · {{ jobInfo.companySize }}</p>
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
        <div class="job-description" v-html="jobInfo.description"></div>
      </div>

      <div class="section">
        <h2 class="section-title">职位要求</h2>
        <div class="job-requirements" v-html="jobInfo.requirements"></div>
      </div>

      <div class="section">
        <h2 class="section-title">工作地址</h2>
        <div class="job-address">
          <van-icon name="location-o" />
          <span>{{ jobInfo.address }}</span>
        </div>
      </div>
    </div>

    <!-- 相似职位推荐 -->
    <div class="similar-jobs">
      <h2 class="section-title">相似职位</h2>
      <job-list :jobs="similarJobs" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import JobList from '@/components/JobList.vue'

const route = useRoute()
const router = useRouter()

const jobInfo = ref({
  title: '前端开发工程师',
  salary: '15-25',
  city: '北京',
  experience: '应届生',
  education: '本科及以上',
  companyName: 'XX科技有限公司',
  companyLogo: 'https://placeholder.com/100',
  companyIndustry: '互联网',
  companySize: '500-1000人',
  description: `
    <p>工作职责：</p>
    <ul>
      <li>负责公司产品的前端开发工作</li>
      <li>与后端工程师配合，完成产品功能开发</li>
      <li>优化前端性能，提升用户体验</li>
    </ul>
  `,
  requirements: `
    <p>任职要求：</p>
    <ul>
      <li>本科及以上学历，计算机相关专业</li>
      <li>熟悉 HTML、CSS、JavaScript</li>
      <li>熟悉 Vue.js 等主流前端框架</li>
      <li>有良好的团队协作能力</li>
    </ul>
  `,
  address: '北京市海淀区XX大厦'
})

const similarJobs = ref([])
const isCollected = ref(false)

// 获取职位详情
const fetchJobDetail = async () => {
  try {
    const jobId = route.params.id
    // TODO: 调用获取职位详情 API
    console.log('获取职位详情:', jobId)
  } catch (error) {
    console.error('获取职位详情失败:', error)
  }
}

// 获取相似职位
const fetchSimilarJobs = async () => {
  try {
    // TODO: 调用获取相似职位 API
    similarJobs.value = []
  } catch (error) {
    console.error('获取相似职位失败:', error)
  }
}

// 投递简历
const handleApply = async () => {
  try {
    // TODO: 调用投递简历 API
    console.log('投递职位:', route.params.id)
  } catch (error) {
    console.error('投递简历失败:', error)
  }
}

// 收藏/取消收藏
const toggleCollect = async () => {
  try {
    // TODO: 调用收藏/取消收藏 API
    isCollected.value = !isCollected.value
  } catch (error) {
    console.error('操作失败:', error)
  }
}

onMounted(() => {
  fetchJobDetail()
  fetchSimilarJobs()
})
</script>

<style scoped>
.job-detail {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.job-header {
  background: white;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.job-basic {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.job-title {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.job-salary {
  font-size: 24px;
  color: #ff4d4f;
  font-weight: 500;
}

.job-tags {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
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
  align-items: center;
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

.company-name {
  margin: 0 0 8px;
  font-size: 18px;
  color: #333;
}

.company-desc {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.job-actions {
  position: sticky;
  top: 0;
  z-index: 10;
  background: white;
  padding: 16px;
  margin: 0 -20px 16px;
  display: flex;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.detail-content {
  background: white;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
}

.section {
  margin-bottom: 32px;
}

.section:last-child {
  margin-bottom: 0;
}

.section-title {
  margin: 0 0 16px;
  font-size: 18px;
  color: #333;
}

.job-description,
.job-requirements {
  font-size: 14px;
  line-height: 1.6;
  color: #666;
}

.job-description ul,
.job-requirements ul {
  padding-left: 20px;
  margin: 12px 0;
}

.job-address {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.similar-jobs {
  background: white;
  border-radius: 8px;
  padding: 24px;
}

@media (max-width: 768px) {
  .job-actions {
    flex-direction: column;
  }

  .job-basic {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .company-info {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style> 
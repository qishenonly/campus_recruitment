<template>
  <div class="job-list">
    <div class="job-item" v-for="job in jobs" :key="job.id" @click="goToJobDetail(job.id)">
      <div class="job-info">
        <div class="job-title-row">
          <h3 class="job-title">{{ job.title }}</h3>
          <span class="job-salary">{{ job.salary }}K</span>
        </div>
        <div class="job-tags">
          <span class="tag">{{ job.location }}</span>
          <span class="tag">{{ job.education }}</span>
          <span class="tag">{{ job.experience }}</span>
        </div>
        <div class="company-info">
          <img :src="job.companyLogo" class="company-logo" alt="公司logo" />
          <span class="company-name">{{ job.company }}</span>
          <span class="company-type">{{ job.companyType }}</span>
          <span class="company-size">{{ job.companySize }}</span>
        </div>
      </div>
      <div class="job-actions">
        <van-button type="primary" size="small">立即投递</van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
const props = defineProps({
  jobs: {
    type: Array,
    default: () => []
  }
})

// 跳转到职位详情
const goToJobDetail = (id) => {
  router.push(`/job/${id}`)
}
</script>

<style scoped>
.job-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.job-item {
  padding: 24px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  transition: all 0.3s;
  background: white;
  cursor: pointer;
}

.job-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: var(--primary-color);
}

.job-info {
  flex: 1;
}

.job-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.job-title {
  margin: 0;
  font-size: 18px;
  color: #333;
  font-weight: 500;
}

.job-title:hover {
  color: var(--primary-color);
}

.job-salary {
  color: var(--error-color);
  font-size: 18px;
  font-weight: bold;
}

.job-tags {
  margin-bottom: 12px;
}

.tag {
  background: #f5f5f5;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
  margin-right: 8px;
  transition: all 0.3s;
}

.tag:hover {
  background: #e6f7ff;
  color: var(--primary-color);
}

.company-info {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #f0f0f0;
}

.company-logo {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.company-logo:hover {
  transform: scale(1.05);
}

.company-name {
  font-weight: 500;
}

.job-actions {
  margin-left: 20px;
}

.job-actions .van-button {
  border-radius: 20px;
  padding: 0 24px;
  font-weight: 500;
}
</style>
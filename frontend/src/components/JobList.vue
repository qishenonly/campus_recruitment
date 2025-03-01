<template>
  <div class="job-list">
    <div class="job-item" v-for="job in jobs" :key="job.id" @click="goToJobDetail(job.id)">
      <div class="job-info">
        <div class="job-title-row">
          <h3 class="job-title">{{ job.title }}</h3>
          <span class="job-salary">{{ job.salary }}</span>
        </div>
        <div class="job-tags">
          <span class="tag">{{ job.location }}</span>
          <span class="tag">{{ job.educationRequirement }}</span>
          <span class="tag">{{ job.positionType }}</span>
          <span class="tag">{{ job.majorRequirement }}</span>
        </div>
        <div class="bottom-row">
          <div class="company-info">
            <img :src="job.companyLogo" class="company-logo" alt="公司logo" />
            <div class="company-detail">
              <span class="company-name">{{ job.companyName }}</span>
              <div class="company-tags">
                <span class="company-tag">{{ job.industry }}</span>
                <span class="company-tag">{{ job.companyScale }}</span>
                <span v-if="job.companyVerified" class="company-tag verified">
                  <van-icon name="success" size="12" />认证企业
                </span>
              </div>
            </div>
          </div>
          <div class="action-buttons">
            <van-icon
              :name="job.isFavorited ? 'star' : 'star-o'"
              :class="['favorite-icon', { 'is-favorited': job.isFavorited }]"
              @click.stop="handleFavorite(job)"
            />
            <van-button 
              type="primary" 
              size="small" 
              class="apply-btn"
              @click.stop="handleApply(job.id)"
            >
              立即投递
            </van-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { favoriteJob, unfavoriteJob } from '@/api/jobs'

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

// 投递简历
const handleApply = async (jobId) => {
  try {
    // TODO: 调用投递简历 API
    showToast('投递成功')
  } catch (error) {
    showToast('投递失败')
  }

}

const handleFavorite = async (job) => {
  try {
    if (!localStorage.getItem('token')) {
      showToast('请先登录')
      router.push('/login')
      return
    }

    if (job.isFavorited) {
      await unfavoriteJob(job.id)
      job.isFavorited = false
      showToast('已取消收藏')
    } else {
      await favoriteJob(job.id)
      job.isFavorited = true 
      showToast('收藏成功')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    showToast(error.message || '操作失败')
  }
}
</script>

<style scoped>
.job-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.job-item {
  padding: 20px;
  background: white;
  border-radius: 8px;
  border: 1px solid #eee;
  cursor: pointer;
  transition: all 0.3s;
}

.job-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
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
}

.job-salary {
  color: #ff4d4f;
  font-size: 16px;
  font-weight: 500;
}

.job-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.tag {
  padding: 4px 12px;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 14px;
  color: #666;
}

.bottom-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.company-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.company-logo {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  object-fit: cover;
}

.company-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.company-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.company-tag {
  font-size: 12px;
  color: #666;
  background: #f7f7f7;
  padding: 2px 6px;
  border-radius: 2px;
}

.company-tag.verified {
  color: #52c41a;
  display: flex;
  align-items: center;
  gap: 2px;
}

.company-name {
  font-size: 14px;
  color: #666;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 12px;
}

.favorite-icon {
  font-size: 20px;
  color: #999;
  cursor: pointer;
}

.favorite-icon.is-favorited {
  color: #ff9800;
}

.apply-btn {
  flex-shrink: 0;
}
</style>
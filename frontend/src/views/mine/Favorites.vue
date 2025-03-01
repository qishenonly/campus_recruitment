<template>
  <div class="favorites-page">
    <van-nav-bar
      title="我的收藏"
      left-arrow
      @click-left="router.back()"
    />
    
    <div class="page-header">
      <div class="header-content">
        <h2 class="title">我的收藏职位</h2>
        <p class="subtitle">共收藏了 {{ total }} 个职位</p>
      </div>
    </div>

    <div class="content-wrapper">
      <div class="job-list" v-if="jobs.length > 0">
        <div v-for="job in jobs" :key="job.id" class="job-card" @click="goToJobDetail(job.id)">
          <div class="job-header">
            <div class="left">
              <h3 class="job-title">{{ job.title }}</h3>
              <div class="job-tags">
                <span class="tag">{{ job.location }}</span>
                <span class="tag">{{ job.educationRequirement }}</span>
                <span class="tag">{{ job.positionType }}</span>
                <span class="tag">{{ job.majorRequirement }}</span>
              </div>
            </div>
            <div class="right">
              <span class="job-salary">{{ job.salary }}</span>
            </div>
          </div>

          <div class="company-section">
            <div class="company-info">
              <img :src="job.companyLogo" alt="公司logo" class="company-logo">
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
          </div>

          <div class="job-footer">
            <div class="collect-time">
              <van-icon name="clock-o" />
              收藏于 {{ formatDate(job.createTime) }}
            </div>
            <div class="actions">
              <van-button 
                type="primary" 
                size="small" 
                plain
                @click.stop="handleApply(job.id)"
              >
                立即投递
              </van-button>
              <van-button 
                type="danger" 
                size="small" 
                plain
                @click.stop="handleUnfavorite(job.id)"
              >
                取消收藏
              </van-button>
            </div>
          </div>
        </div>
      </div>

      <div class="empty-state" v-else>
        <van-empty 
          image="search" 
          description="暂无收藏的职位"
        >
          <template #bottom>
            <van-button 
              type="primary" 
              size="small" 
              round
              @click="router.push('/home')"
            >
              去发现好职位
            </van-button>
          </template>
        </van-empty>
      </div>

    </div>
  </div>
</template>

<style scoped>
.favorites-page {
  min-height: 100vh;
  background: #f6f7f9;
}

.page-header {
  background: linear-gradient(135deg, #1989fa, #0960bd);
  padding: 24px 20px;
  color: white;
}

.header-content {
  max-width: 800px;
  margin: 0 auto;
}

.title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.subtitle {
  margin: 8px 0 0;
  font-size: 14px;
  opacity: 0.9;
}

.content-wrapper {
  max-width: 800px;
  margin: -20px auto 0;
  padding: 0 16px 20px;
  position: relative;
}

.job-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.job-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.job-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.job-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px;
}

.job-salary {
  color: #ff4d4f;
  font-size: 18px;
  font-weight: 600;
}

.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  padding: 4px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 13px;
  color: #666;
}

.company-section {
  padding: 16px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
}

.company-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.company-logo {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
}

.company-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.company-name {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}

.company-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.company-tag {
  padding: 2px 8px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.company-tag.verified {
  color: #52c41a;
  display: flex;
  align-items: center;
  gap: 2px;
}

.job-footer {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.collect-time {
  color: #999;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.actions {
  display: flex;
  gap: 8px;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.pagination {
  margin-top: 24px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.job-content {
  cursor: pointer;
}

.actions {
  display: flex;
  gap: 8px;
}

.actions .van-button {
  padding: 0 16px;
  height: 32px;
  font-size: 14px;
}

.actions .van-button--danger {
  --van-button-danger-color: #ff4d4f;
  --van-button-danger-border-color: #ff4d4f;
}

.actions .van-button--primary {
  --van-button-primary-color: #1989fa;
  --van-button-primary-border-color: #1989fa;
}

@media (max-width: 768px) {
  .page-header {
    padding: 16px;
  }
  
  .title {
    font-size: 20px;
  }
  
  .content-wrapper {
    padding: 0 12px 16px;
  }
  
  .job-card {
    padding: 16px;
  }
  
  .job-header {
    flex-direction: column;
  }
  
  .job-salary {
    margin-top: 8px;
  }
  
  .job-footer {
    flex-direction: column;
    gap: 12px;
  }
  
  .actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFavorites, unfavoriteJob, getJobDetail } from '@/api/jobs'
import { format } from 'date-fns'
import { showDialog, showToast } from 'vant'

const router = useRouter()
const jobs = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formatDate = (date) => {
  return format(new Date(date), 'yyyy-MM-dd HH:mm')
}

// 获取职位详情
const getJobDetails = async (favorites) => {
  try {
    const jobDetails = await Promise.all(
      favorites.map(async (favorite) => {
        const response = await getJobDetail(favorite.jobId)
        return {
          ...response.data,
          createTime: favorite.createTime,  // 保留收藏时间
          favoriteId: favorite.id  // 保留收藏记录ID
        }
      })
    )
    return jobDetails
  } catch (error) {
    console.error('获取职位详情失败:', error)
    showToast('获取职位详情失败')
    return []
  }
}

const loadFavorites = async (page = currentPage.value) => {
  try {
    const res = await getFavorites({
      page: page - 1,
      size: pageSize.value
    })
    
    console.log('收藏列表数据:', res.data.content) // 调试日志
    
    // 获取收藏列表中每个职位的详细信息
    const jobDetails = await getJobDetails(res.data.content)
    jobs.value = jobDetails
    total.value = res.data.totalElements
  } catch (error) {
    console.error('获取收藏列表失败:', error)
    showToast('获取收藏列表失败')
  }
}

const handleUnfavorite = async (jobId) => {
  try {
    await showDialog({
      title: '取消收藏',
      message: '确定要取消收藏该职位吗？',
      showCancelButton: true
    })
    
    await unfavoriteJob(jobId)
    showToast('已取消收藏')
    loadFavorites(currentPage.value)
  } catch (error) {
    if (error.cancel) return
    console.error('取消收藏失败:', error)
    showToast('取消收藏失败')
  }
}

const handleApply = (jobId) => {
  router.push(`/job/${jobId}/apply`)
}

const goToJobDetail = (jobId) => {
  router.push(`/job/${jobId}`)
}

onMounted(() => {
  loadFavorites()
})
</script> 
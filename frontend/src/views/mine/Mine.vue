<template>
  <div class="mine-page">
    <!-- 顶部背景和个人信息卡片 -->
    <div class="profile-header">
      <div class="header-bg"></div>
      <div class="profile-card">
        <div class="profile-info">
          <div class="avatar-wrapper">
            <img :src="userProfile?.avatar || 'http'" alt="头像" class="avatar" />
            <div class="online-status"></div>
          </div>
          <div class="user-info">
            <h2 class="name">{{ userProfile?.realName || userProfile?.user?.username }}</h2>
            <p class="title">{{ userProfile?.expectedPosition || '暂无职位' }}</p>
            <div class="tags">
              <el-tag size="small" effect="plain">{{ userProfile?.education }}</el-tag>
              <el-tag size="small" effect="plain" type="success">{{ userProfile?.university }}</el-tag>
              <el-tag size="small" effect="plain" type="warning">{{ userProfile?.major }}</el-tag>
            </div>
          </div>
          <div class="edit-btn">
            <el-button type="primary" plain round @click="handleEdit">
              <el-icon><Edit /></el-icon>编辑资料
            </el-button>
          </div>
        </div>
        
        <!-- 快捷统计信息 -->
        <div class="quick-stats">
          <div class="stat-item">
            <span class="number">12</span>
            <span class="label">投递记录</span>
          </div>
          <div class="stat-item">
            <span class="number">5</span>
            <span class="label">面试邀约</span>
          </div>
          <div class="stat-item">
            <span class="number">8</span>
            <span class="label">收藏职位</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 功能卡片区域 -->
    <div class="feature-cards">
      <div class="feature-card" @click="handleResume">
        <div class="feature-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="feature-info">
          <h3>我的简历</h3>
          <p>完善简历提高求职成功率</p>
        </div>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>

      <div class="feature-card" @click="handleDeliveryRecord">
        <div class="feature-icon blue">
          <el-icon><Paperclip /></el-icon>
        </div>
        <div class="feature-info">
          <h3>投递记录</h3>
          <p>{{ deliveryCount || 0 }}份简历投递中</p>
        </div>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>

      <div class="feature-card" @click="handleFavorites">
        <div class="feature-icon green">
          <el-icon><Star /></el-icon>
        </div>
        <div class="feature-info">
          <h3>收藏职位</h3>
          <p>{{ favoriteCount || 0 }}个职位已收藏</p>
        </div>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>

      <div class="feature-card" @click="handleSettings">
        <div class="feature-icon purple">
          <el-icon><Setting /></el-icon>
        </div>
        <div class="feature-info">
          <h3>账号设置</h3>
          <p>修改密码等安全设置</p>
        </div>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- 详细信息卡片 -->
    <div class="detail-cards">
      <!-- 基本信息卡片 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span><el-icon><User /></el-icon>基本信息</span>
          </div>
        </template>
        <div class="info-grid">
          <div class="info-item">
            <el-icon><Male /></el-icon>
            <span class="label">性别</span>
            <span class="value">{{ userProfile?.gender }}</span>
          </div>
          <div class="info-item">
            <el-icon><Calendar /></el-icon>
            <span class="label">出生日期</span>
            <span class="value">{{ userProfile?.birth }}</span>
          </div>
          <div class="info-item">
            <el-icon><Location /></el-icon>
            <span class="label">所在地</span>
            <span class="value">{{ userProfile?.location }}</span>
          </div>
          <div class="info-item">
            <el-icon><Message /></el-icon>
            <span class="label">邮箱</span>
            <span class="value">{{ userProfile?.user?.email }}</span>
          </div>
        </div>
      </el-card>

      <!-- 教育信息卡片 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span><el-icon><School /></el-icon>教育背景</span>
          </div>
        </template>
        <div class="education-info">
          <div class="school-logo">
            <el-icon size="40"><School /></el-icon>
          </div>
          <div class="education-details">
            <h3>{{ userProfile?.university }}</h3>
            <p>{{ userProfile?.major }} | {{ userProfile?.education }}</p>
            <p class="graduation">预计 {{ userProfile?.graduationYear }} 年毕业</p>
          </div>
        </div>
      </el-card>

      <!-- 求职意向卡片 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span><el-icon><Aim /></el-icon>求职意向</span>
          </div>
        </template>
        <div class="job-intention">
          <div class="intention-item">
            <el-icon><Position /></el-icon>
            <span class="label">期望职位</span>
            <span class="value highlight">{{ userProfile?.expectedPosition }}</span>
          </div>
          <div class="intention-item">
            <el-icon><Money /></el-icon>
            <span class="label">期望薪资</span>
            <span class="value highlight">{{ userProfile?.expectedSalary }}</span>
          </div>
          <div class="intention-item">
            <el-icon><Location /></el-icon>
            <span class="label">期望城市</span>
            <span class="value">{{ userProfile?.expectedCity }}</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUserProfile } from '@/api/user'
import { useDialogStore } from '@/stores/dialog'
import {
  Edit,
  User,
  Male,
  Calendar,
  Location,
  Message,
  School,
  Aim,
  Position,
  Money,
  Document,
  Paperclip,
  Star,
  Setting,
  ArrowRight
} from '@element-plus/icons-vue'

const router = useRouter()
const userProfile = ref(null)
const dialogStore = useDialogStore()
const deliveryCount = ref(0)
const favoriteCount = ref(0)

onMounted(async () => {
  try {
    const res = await getUserProfile()
    userProfile.value = res.data
  } catch (error) {
    console.error('获取用户资料失败:', error)
  }
})

const handleEdit = () => {
  dialogStore.showCompleteInfoDialog()
}

const handleResume = () => {
  router.push('/resume')
}

const handleDeliveryRecord = () => {
  router.push('/delivery-record')
}

const handleFavorites = () => {
  router.push('/favorites')
}

const handleSettings = () => {
  router.push('/settings')
}
</script>

<style scoped>
.mine-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-bottom: 40px;
}

.profile-header {
  position: relative;
  padding-top: 100px;
  margin-bottom: 24px;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200px;
  background: linear-gradient(135deg, var(--el-color-primary), var(--el-color-primary-light-3));
  z-index: 1;
}

.profile-card {
  position: relative;
  z-index: 2;
  max-width: 1000px;
  margin: 0 auto;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  padding: 24px;
}

.profile-info {
  display: flex;
  align-items: center;
  gap: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #eee;
}

.avatar-wrapper {
  position: relative;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 4px solid white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  object-fit: cover;
}

.online-status {
  position: absolute;
  bottom: 5px;
  right: 5px;
  width: 12px;
  height: 12px;
  background-color: #67c23a;
  border-radius: 50%;
  border: 2px solid white;
}

.user-info {
  flex: 1;
}

.name {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 4px;
  color: #333;
}

.title {
  font-size: 16px;
  color: #666;
  margin: 0 0 12px;
}

.tags {
  display: flex;
  gap: 8px;
}

.quick-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-top: 24px;
  text-align: center;
}

.stat-item {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-item .number {
  display: block;
  font-size: 24px;
  font-weight: 600;
  color: var(--el-color-primary);
  margin-bottom: 4px;
}

.stat-item .label {
  color: #666;
  font-size: 14px;
}

.detail-cards {
  max-width: 1000px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
  padding: 0 20px;
}

.info-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item .label {
  color: #999;
  margin-right: 8px;
}

.info-item .value {
  color: #333;
  flex: 1;
}

.education-info {
  display: flex;
  gap: 20px;
  padding: 16px;
}

.school-logo {
  width: 60px;
  height: 60px;
  background: #f5f7fa;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.education-details h3 {
  margin: 0 0 8px;
  font-size: 18px;
  color: #333;
}

.education-details p {
  margin: 0;
  color: #666;
  line-height: 1.5;
}

.graduation {
  margin-top: 8px;
  font-size: 14px;
  color: #999;
}

.job-intention {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.intention-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.intention-item .label {
  color: #666;
  min-width: 80px;
}

.intention-item .value {
  flex: 1;
  color: #333;
}

.intention-item .highlight {
  color: var(--el-color-primary);
  font-weight: 500;
}

@media (max-width: 768px) {
  .profile-header {
    padding-top: 60px;
  }

  .profile-info {
    flex-direction: column;
    text-align: center;
  }

  .edit-btn {
    margin-top: 16px;
  }

  .quick-stats {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .detail-cards {
    grid-template-columns: 1fr;
    padding: 0 12px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}

.feature-cards {
  max-width: 1000px;
  margin: 24px auto;
  padding: 0 20px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
}

.feature-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}

.feature-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.feature-card::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 40%;
  height: 100%;
  background: linear-gradient(to left, rgba(255,255,255,0.8), transparent);
  transform: skewX(-15deg) translateX(100%);
  transition: transform 0.5s ease;
}

.feature-card:hover::after {
  transform: skewX(-15deg) translateX(0);
}

.feature-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.feature-icon.blue {
  background: var(--el-color-info-light-9);
  color: var(--el-color-info);
}

.feature-icon.green {
  background: var(--el-color-success-light-9);
  color: var(--el-color-success);
}

.feature-icon.purple {
  background: var(--el-color-warning-light-9);
  color: var(--el-color-warning);
}

.feature-info {
  flex: 1;
}

.feature-info h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.feature-info p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #999;
}

.arrow {
  color: #ccc;
  font-size: 20px;
  transition: transform 0.3s ease;
}

.feature-card:hover .arrow {
  transform: translateX(4px);
  color: var(--el-color-primary);
}

@media (max-width: 768px) {
  .feature-cards {
    grid-template-columns: 1fr;
    padding: 0 12px;
  }

  .feature-card {
    padding: 16px;
  }

  .feature-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
}
</style>
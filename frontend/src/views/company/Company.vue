<template>
  <div class="company-page">
    <!-- 搜索筛选区 -->
    <div class="filter-section">
      <div class="search-box">
        <van-search
          v-model="searchValue"
          placeholder="搜索公司名称、行业领域"
          @search="onSearch"
        />
      </div>
      <div class="filter-tags">
        <div class="filter-group">
          <span class="filter-label">行业领域：</span>
          <span 
            v-for="item in industries" 
            :key="item.value"
            :class="['filter-tag', { active: currentIndustry === item.value }]"
            @click="handleFilterChange('industry', item.value)"
          >
            {{ item.label }}
          </span>
        </div>
        <div class="filter-group">
          <span class="filter-label">公司规模：</span>
          <span 
            v-for="item in scales" 
            :key="item.value"
            :class="['filter-tag', { active: currentScale === item.value }]"
            @click="handleFilterChange('scale', item.value)"
          >
            {{ item.label }}
          </span>
        </div>
        <div class="filter-group">
          <span class="filter-label">融资阶段：</span>
          <span 
            v-for="item in stages" 
            :key="item.value"
            :class="['filter-tag', { active: currentStage === item.value }]"
            @click="handleFilterChange('stage', item.value)"
          >
            {{ item.label }}
          </span>
        </div>
      </div>
    </div>

    <!-- 公司列表 -->
    <div class="company-list">
      <div 
        v-for="company in companies" 
        :key="company.id" 
        class="company-card"
        @click="goToCompanyDetail(company.id)"
      >
        <div class="company-logo">
          <img :src="company.logo" :alt="company.name">
        </div>
        <div class="company-info">
          <h3 class="company-name">{{ company.companyName }}</h3>
          <div class="company-tags">
            <span class="tag">{{ company.industry }}</span>
            <span class="tag">{{ company.scale }}</span>
          </div>
          <div class="company-desc">{{ company.description }}</div>
          <div class="company-location">{{ company.location }}</div>
          <div class="company-website">
            <a :href="company.website" target="_blank">{{ company.website }}</a>
          </div>
          <div class="company-jobs">
            <span class="job-count">{{ company.jobCount }}个在招职位</span>
            <span class="salary-range">{{ company.salaryRange }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <van-pagination 
        v-model="currentPage" 
        :total-items="total" 
        :items-per-page="pageSize" 
        @change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { searchCompanies } from '@/api/companies' 

const router = useRouter()
const searchValue = ref('')
const currentIndustry = ref('all')
const currentScale = ref('all')
const currentStage = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const companies = ref([])

// 筛选选项
const industries = [
  { label: '全部', value: 'all' },
  { label: '互联网', value: '互联网' },
  { label: '金融', value: '金融' },
  { label: '教育', value: '教育' },
  { label: '医疗健康', value: '医疗健康' }
]

const scales = [
  { label: '全部', value: 'all' },
  { label: '0-20人', value: '0-20人' },
  { label: '20-99人', value: '20-99人' },
  { label: '100-499人', value: '100-499人' },
  { label: '500人以上', value: '500人以上' }
]

const stages = [
  { label: '全部', value: 'all' },
  { label: '未融资', value: '未融资' },
  { label: '天使轮', value: '天使轮' },
  { label: 'A轮', value: 'A轮' },
  { label: 'B轮', value: 'B轮' },
  { label: 'C轮及以上', value: 'C轮及以上' }
]

// 搜索处理
const onSearch = () => {
  currentPage.value = 1
  fetchCompanies()
}

// 筛选处理
const handleFilterChange = (type, value) => {
  switch(type) {
    case 'industry':
      currentIndustry.value = value
      break
    case 'scale':
      currentScale.value = value
      break
    case 'stage':
      currentStage.value = value
      break
  }
  currentPage.value = 1
  fetchCompanies()
}

// 分页处理
const handlePageChange = (page) => {
  currentPage.value = page
  fetchCompanies()
}

// 获取公司列表
const fetchCompanies = async () => {
  try {
    const params = {
      keyword: searchValue.value || '',
    }
    if (currentIndustry.value !== 'all') {
      params.keyword = currentIndustry.value
    }
    if (currentScale.value !== 'all') {
      params.scale = currentScale.value
    }
    if (currentStage.value !== 'all') {
      params.stage = currentStage.value
    }
    const response = await searchCompanies(params)
    companies.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    console.error('获取公司列表失败:', error)
  }
}

// 跳转到公司详情
const goToCompanyDetail = (id) => {
  router.push(`/company/${id}`)
}

onMounted(() => {
  fetchCompanies()
})
</script>

<style scoped>
.company-page {
  padding: 20px;
  background-color: #f4f6f8;
}

.filter-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.filter-tags {
  margin-top: 20px;
}

.filter-group {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-label {
  color: #666;
  margin-right: 8px;
  white-space: nowrap;
  font-weight: bold;
}

.filter-tag {
  padding: 6px 16px;
  border-radius: 16px;
  font-size: 14px;
  cursor: pointer;
  background: #f5f5f5;
  transition: all 0.3s;
}

.filter-tag:hover {
  color: var(--primary-color);
  background: #e6f7ff;
}

.filter-tag.active {
  color: white;
  background: var(--primary-color);
}

.company-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.company-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #eee;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.company-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-color: var(--primary-color);
}

.company-logo {
  width: 80px;
  height: 80px;
  margin-bottom: 16px;
  border-radius: 8px;
  overflow: hidden;
}

.company-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.company-name {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 12px;
  color: #333;
}

.company-tags {
  margin-bottom: 12px;
}

.tag {
  display: inline-block;
  padding: 4px 8px;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
  margin-right: 8px;
}

.company-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.company-location {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.company-website a {
  font-size: 14px;
  color: var(--primary-color);
  text-decoration: none;
}

.company-website a:hover {
  text-decoration: underline;
}

.company-jobs {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.job-count {
  color: var(--primary-color);
}

.salary-range {
  color: #ff4d4f;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

@media (max-width: 768px) {
  .company-list {
    grid-template-columns: 1fr;
  }

  .filter-group {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-label {
    margin-bottom: 8px;
  }
}
</style> 
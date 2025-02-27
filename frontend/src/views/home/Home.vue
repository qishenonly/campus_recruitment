<template>
  <div class="home">
    <!-- 搜索区域优化 -->
    <div class="search-section">
      <div class="search-container">
        <h1 class="search-title">找到理想的工作</h1>
        <p class="search-subtitle">数千个校招职位，助你开启职业生涯</p>
        <div class="search-box">
          <div class="search-input-wrapper">
            <i class="search-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="11" cy="11" r="8"></circle>
                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
            </i>
            <input 
              type="text" 
              v-model="searchValue" 
              placeholder="搜索职位名称、公司名称" 
              class="search-input"
              @keyup.enter="handleSearch"
            />
          </div>
          <button class="search-button" @click="handleSearch">
            <span>立即搜索</span>
          </button>
        </div>
        <div class="hot-search">
          <span class="hot-search-label">热门搜索：</span>
          <a v-for="(tag, index) in hotTags" 
             :key="index" 
             href="#" 
             class="hot-search-item"
             @click.prevent="handleTagClick(tag)">
            {{ tag }}
          </a>
        </div>
      </div>
    </div>

    <!-- 内容区域优化 -->
    <div class="content-section container">
      <!-- 左侧筛选 -->
      <div class="filter-sidebar">
        <div class="filter-group">
          <h3>工作地点</h3>
          <div class="filter-options">
            <span class="filter-option active">全部</span>
            <span class="filter-option">北京</span>
            <span class="filter-option">上海</span>
            <span class="filter-option">广州</span>
            <span class="filter-option">深圳</span>
          </div>
        </div>
        <div class="filter-group">
          <h3>工作经验</h3>
          <div class="filter-options">
            <span class="filter-option active">不限</span>
            <span class="filter-option">应届生</span>
            <span class="filter-option">1-3年</span>
            <span class="filter-option">3-5年</span>
          </div>
        </div>
        <div class="filter-group">
          <h3>学历要求</h3>
          <div class="filter-options">
            <span class="filter-option active">不限</span>
            <span class="filter-option">大专</span>
            <span class="filter-option">本科</span>
            <span class="filter-option">硕士</span>
            <span class="filter-option">博士</span>
          </div>
        </div>
      </div>

      <!-- 右侧职位列表优化 -->
      <div class="job-content">
        <div class="job-header">
          <div class="job-header-left">
            <h2>搜索结果</h2>
            <span class="job-count">共 {{ totalJobs }} 个职位</span>
          </div>
          <div class="job-sort">
            <span 
              v-for="(item, index) in sortOptions" 
              :key="index"
              :class="['sort-item', { active: currentSort === item.value }]"
              @click="handleSort(item.value)"
            >
              {{ item.label }}
            </span>
          </div>
        </div>
        <van-loading v-if="loading" />
        <job-list v-else :jobs="jobs" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import JobList from '@/components/JobList.vue'
import { getJobs, searchJobs } from '@/api/jobs'

const searchValue = ref('')
const jobs = ref([])
const totalJobs = ref(0)
const currentSort = ref('latest')
const loading = ref(false)

const hotTags = ['Java开发', '前端工程师', '产品经理', '运营', 'UI设计师']

const sortOptions = [
  { label: '最新', value: 'latest' },
  { label: '最热', value: 'hot' },
  { label: '薪资最高', value: 'salary' }
]

const handleSearch = async () => {
  if (!searchValue.value.trim()) {
    showToast('请输入搜索关键词')
    return
  }
  loading.value = true
  try {
    const result = await searchJobs({ keyword: searchValue.value })
    jobs.value = result.data.content
    totalJobs.value = result.data.totalElements
  } catch (error) {
    console.error('搜索职位失败:', error)
    showToast('搜索职位失败，请稍后重试')
    jobs.value = []
    totalJobs.value = 0
  } finally {
    loading.value = false
  }
}

const handleTagClick = (tag) => {
  searchValue.value = tag
  handleSearch()
}

const handleSort = (value) => {
  currentSort.value = value
  // 实现排序逻辑
}

const fetchJobs = async () => {
  loading.value = true
  try {
    const result = await getJobs()
    console.log(result)
    jobs.value = result.data.content
    totalJobs.value = result.data.totalElements
  } catch (error) {
    console.error('获取职位列表失败:', error)
    showToast('获取职位列表失败，请稍后重试')
    jobs.value = []
    totalJobs.value = 0
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchJobs()
})
</script>

<style scoped>
.home {
  min-height: 100%;
}

.search-section {
  background: linear-gradient(135deg, #2193b0, #6dd5ed);
  padding: 60px 0;
  margin-bottom: 30px;
  position: relative;
  overflow: hidden;
  border-radius: 0 0 50px 50px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}

.search-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('/path/to/pattern.png') repeat; /* 可选：添加背景图案 */
  opacity: 0.1;
}

.search-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
  text-align: center;
  position: relative;
  z-index: 1;
}

.search-title {
  color: white;
  font-size: 42px;
  font-weight: 600;
  margin: 0 0 15px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
  letter-spacing: 1px;
}

.search-subtitle {
  color: rgba(255,255,255,0.9);
  font-size: 20px;
  margin: 0 0 40px;
  font-weight: 300;
}

.search-box {
  background: white;
  border-radius: 30px;
  padding: 10px;
  display: flex;
  gap: 10px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.15);
  transition: all 0.3s ease;
}

.search-box:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0,0,0,0.2);
}

.search-input-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  padding: 0 25px;
  background: #f8f9fa;
  border-radius: 25px;
  transition: all 0.3s ease;
}

.search-input-wrapper:focus-within {
  background: #fff;
  box-shadow: inset 0 0 0 2px var(--primary-color);
}

.search-icon {
  color: #999;
  margin-right: 15px;
  display: flex;
  align-items: center;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 16px;
  padding: 15px 0;
  width: 100%;
}

.search-input::placeholder {
  color: #999;
}

.search-button {
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 25px;
  padding: 0 35px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.search-button:hover {
  background: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(24,144,255,0.3);
}

.hot-search {
  margin-top: 25px;
  color: rgba(255,255,255,0.9);
}

.hot-search-label {
  font-size: 14px;
  margin-right: 12px;
}

.hot-search-item {
  color: rgba(255,255,255,0.9);
  text-decoration: none;
  margin: 0 12px;
  font-size: 14px;
  padding: 4px 15px;
  background: rgba(255,255,255,0.1);
  border-radius: 20px;
  transition: all 0.3s ease;
}

.hot-search-item:hover {
  color: white;
  background: rgba(255,255,255,0.2);
  transform: translateY(-1px);
}

.content-section {
  display: flex;
  gap: 20px;
}

.filter-sidebar {
  width: 240px;
  background: white;
  padding: 20px;
  border-radius: 4px;
}

.filter-group {
  margin-bottom: 20px;
}

.filter-group h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-option {
  padding: 6px 16px;
  border-radius: 20px;
  background: #f5f5f5;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.filter-option:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.filter-option.active {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

.job-content {
  flex: 1;
  background: white;
  padding: 20px;
  border-radius: 4px;
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.job-header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.job-count {
  color: #666;
  font-size: 14px;
}

.job-sort {
  display: flex;
  gap: 16px;
}

.sort-item {
  position: relative;
  cursor: pointer;
  color: #666;
  padding: 4px 8px;
  transition: all 0.3s;
}

.sort-item::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 50%;
  width: 0;
  height: 2px;
  background: var(--primary-color);
  transition: all 0.3s;
  transform: translateX(-50%);
}

.sort-item:hover::after,
.sort-item.active::after {
  width: 100%;
}

.sort-item.active {
  color: var(--primary-color);
  font-weight: 500;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 响应式优化 */
@media (max-width: 768px) {
  .content-section {
    flex-direction: column;
  }

  .filter-sidebar {
    width: 100%;
    margin-bottom: 20px;
  }

  .job-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .job-sort {
    width: 100%;
    overflow-x: auto;
    padding-bottom: 8px;
  }
}
</style>
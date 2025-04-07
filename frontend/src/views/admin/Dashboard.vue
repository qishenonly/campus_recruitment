<template>
  <div class="dashboard-container">
    <!-- 欢迎卡片 -->
    <el-card class="welcome-card">
      <template #header>
        <div class="welcome-header">
          <h2>管理后台</h2>
          <el-tag type="success">系统正常运行中</el-tag>
        </div>
      </template>
      <div class="welcome-content">
        <div class="welcome-info">
          <h3>欢迎回来，管理员</h3>
          <p>今天是 {{ currentDate }}，系统数据概览如下</p>
        </div>
        <div class="welcome-action">
          <el-button type="primary" @click="refreshDashboard">
            <el-icon><i-ep-refresh /></el-icon>刷新数据
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <div class="stats-cards" v-loading="statsLoading">
      <!-- 学生用户 -->
      <el-card class="stats-card" shadow="hover">
        <div class="stats-card-content">
          <div class="stats-card-title">
            <el-icon class="icon"><UserFilled /></el-icon>
            <span>学生用户</span>
          </div>
          <div class="stats-card-value">{{ statsData.studentCount }}</div>
          <div class="stats-card-growth">
            较上月
            <span :class="statsData.studentGrowth >= 0 ? 'positive' : 'negative'">
              {{ statsData.studentGrowth >= 0 ? '+' : '' }}{{ statsData.studentGrowth }}%
            </span>
          </div>
        </div>
        <i class="el-icon-user stats-card-bg"></i>
      </el-card>

      <!-- 企业用户 -->
      <el-card class="stats-card" shadow="hover">
        <div class="stats-card-content">
          <div class="stats-card-title">
            <el-icon class="icon"><OfficeBuilding /></el-icon>
            <span>企业用户</span>
          </div>
          <div class="stats-card-value">{{ statsData.companyCount }}</div>
          <div class="stats-card-growth">
            较上月
            <span :class="statsData.companyGrowth >= 0 ? 'positive' : 'negative'">
              {{ statsData.companyGrowth >= 0 ? '+' : '' }}{{ statsData.companyGrowth }}%
            </span>
          </div>
        </div>
        <i class="el-icon-office-building stats-card-bg"></i>
      </el-card>

      <!-- 职位数量 -->
      <el-card class="stats-card" shadow="hover">
        <div class="stats-card-content">
          <div class="stats-card-title">
            <el-icon class="icon"><Briefcase /></el-icon>
            <span>职位数量</span>
          </div>
          <div class="stats-card-value">{{ statsData.jobCount }}</div>
          <div class="stats-card-growth">
            较上月
            <span :class="statsData.jobGrowth >= 0 ? 'positive' : 'negative'">
              {{ statsData.jobGrowth >= 0 ? '+' : '' }}{{ statsData.jobGrowth }}%
            </span>
          </div>
        </div>
        <i class="el-icon-briefcase stats-card-bg"></i>
      </el-card>

      <!-- 申请数量 -->
      <el-card class="stats-card" shadow="hover">
        <div class="stats-card-content">
          <div class="stats-card-title">
            <el-icon class="icon"><Document /></el-icon>
            <span>申请数量</span>
          </div>
          <div class="stats-card-value">{{ statsData.applicationCount }}</div>
          <div class="stats-card-growth">
            较上月
            <span :class="statsData.applicationGrowth >= 0 ? 'positive' : 'negative'">
              {{ statsData.applicationGrowth >= 0 ? '+' : '' }}{{ statsData.applicationGrowth }}%
            </span>
          </div>
        </div>
        <i class="el-icon-document stats-card-bg"></i>
      </el-card>
    </div>

    <!-- 图表部分 -->
    <div class="chart-section">
      <!-- 用户增长趋势图 -->
      <el-card class="chart-container" shadow="hover" v-loading="userChartLoading">
        <template #header>
          <div class="chart-header">
            <h3 class="chart-title">用户增长趋势</h3>
            <div class="time-range-selector">
              <span class="label">时间范围:</span>
              <el-radio-group v-model="userChartTimeRange" size="small">
                <el-radio-button label="week">近一周</el-radio-button>
                <el-radio-button label="month">近一月</el-radio-button>
                <el-radio-button label="quarter">近一季</el-radio-button>
                <el-radio-button label="year">近一年</el-radio-button>
              </el-radio-group>
            </div>
          </div>
        </template>
        <div ref="userChart" class="chart-content"></div>
      </el-card>

      <!-- 职位分布图 -->
      <el-card class="chart-container" shadow="hover" v-loading="jobChartLoading">
        <template #header>
          <div class="chart-header">
            <h3 class="chart-title">职位分布</h3>
            <div class="chart-type-selector">
              <span class="label">分类方式:</span>
              <el-radio-group v-model="jobChartType" size="small">
                <el-radio-button label="city">城市分布</el-radio-button>
                <el-radio-button label="industry">行业分布</el-radio-button>
              </el-radio-group>
            </div>
          </div>
        </template>
        <div ref="jobChart" class="chart-content"></div>
      </el-card>
    </div>

    <!-- 最近活动 -->
    <el-card class="recent-activity" shadow="hover">
      <template #header>
        <div class="activity-header">
          <h3>最近活动</h3>
          <el-button type="primary" text @click="goToSystemLogs">查看全部</el-button>
        </div>
      </template>
      
      <el-table 
        :data="recentActivities" 
        style="width: 100%" 
        :max-height="300" 
        v-loading="logsLoading"
        empty-text="暂无活动记录">
        <el-table-column prop="time" label="时间" width="140" />
        <el-table-column prop="type" label="分类" width="100">
          <template #default="{ row }">
            <el-tag :type="getTagType(row.type)" effect="light" size="small">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="操作" width="120" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column prop="user" label="操作者" width="100" show-overflow-tooltip />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewLogDetail(row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加日志详情对话框 -->
    <el-dialog
      v-model="logDetailVisible"
      title="日志详情"
      width="600px"
      destroy-on-close
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ currentLog.time }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="getTagType(currentLog.type)" effect="light">
            {{ currentLog.type }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作">{{ currentLog.description }}</el-descriptions-item>
        <el-descriptions-item label="内容">{{ currentLog.content }}</el-descriptions-item>
        <el-descriptions-item label="操作者">{{ currentLog.user }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ipAddress }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue';
import { format } from 'date-fns';
import * as echarts from 'echarts/core';
import { BarChart, LineChart, PieChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { 
  getBasicStats, 
  getUserRegisterTrend, 
  getJobStats, 
  getRecentLogs as fetchRecentLogs
} from '../../api/admin-dashboard';
import { 
  UserFilled, 
  OfficeBuilding, 
  Briefcase, 
  Document 
} from '@element-plus/icons-vue';

// 注册 echarts 组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  BarChart,
  LineChart,
  PieChart,
  CanvasRenderer
]);

const router = useRouter();

// 当前日期
const currentDate = format(new Date(), 'yyyy年MM月dd日');

// 图表引用
const userChart = ref(null);
const jobChart = ref(null);

// 加载状态
const userChartLoading = ref(false);
const jobChartLoading = ref(false);
const statsLoading = ref(false);

// 图表配置
const userChartTimeRange = ref('month');
const jobChartType = ref('city');

// 统计数据
const statsData = reactive({
  studentCount: 0,
  studentGrowth: 0,
  companyCount: 0,
  companyGrowth: 0,
  jobCount: 0,
  jobGrowth: 0,
  applicationCount: 0,
  applicationGrowth: 0
});

// 获取Tag类型
const getTagType = (type) => {
  // 如果type为undefined或null，返回空字符串
  if (!type) {
    return '';
  }
  
  const typeMap = {
    '职位管理': 'success',
    '学生管理': 'info',
    '企业管理': 'warning',
    '系统设置': 'danger',
    'HTTP请求': '',
  };
  
  // 匹配前缀
  for (const [key, value] of Object.entries(typeMap)) {
    if (type.startsWith(key)) {
      return value;
    }
  }
  
  return '';
};

// 最近活动数据
const recentActivities = ref([]);
const logsLoading = ref(false);
const logDetailVisible = ref(false);
const currentLog = ref({});

// 获取基础统计数据
const fetchBasicStats = async () => {
  statsLoading.value = true;
  try {
    const { data } = await getBasicStats();
    if (data) {
      statsData.studentCount = data.studentCount || 0;
      statsData.studentGrowth = data.studentGrowth || 0;
      statsData.companyCount = data.companyCount || 0;
      statsData.companyGrowth = data.companyGrowth || 0;
      statsData.jobCount = data.jobCount || 0;
      statsData.jobGrowth = data.jobGrowth || 0;
      statsData.applicationCount = data.applicationCount || 0;
      statsData.applicationGrowth = data.applicationGrowth || 0;
    }
  } catch (error) {
    console.error('获取基础统计数据失败:', error);
    ElMessage.error('获取统计数据失败');
  } finally {
    statsLoading.value = false;
  }
};

// 获取用户趋势数据
const fetchUserTrendData = async () => {
  if (!userChart.value) return;
  
  userChartLoading.value = true;
  try {
    // 根据选择的时间范围确定请求参数
    const now = new Date();
    let startDate, endDate, type;
    
    switch (userChartTimeRange.value) {
      case 'week':
        // 过去7天
        startDate = format(new Date(now.setDate(now.getDate() - 7)), 'yyyy-MM-dd');
        endDate = format(new Date(), 'yyyy-MM-dd');
        type = 'daily';
        break;
      case 'month':
        // 过去30天
        startDate = format(new Date(now.setDate(now.getDate() - 30)), 'yyyy-MM-dd');
        endDate = format(new Date(), 'yyyy-MM-dd');
        type = 'daily';
        break;
      case 'quarter':
        // 过去90天
        startDate = format(new Date(now.setDate(now.getDate() - 90)), 'yyyy-MM-dd');
        endDate = format(new Date(), 'yyyy-MM-dd');
        type = 'weekly';
        break;
      case 'year':
        // 过去一年
        startDate = format(new Date(now.setFullYear(now.getFullYear() - 1)), 'yyyy-MM-dd');
        endDate = format(new Date(), 'yyyy-MM-dd');
        type = 'monthly';
        break;
      default:
        startDate = format(new Date(now.setDate(now.getDate() - 30)), 'yyyy-MM-dd');
        endDate = format(new Date(), 'yyyy-MM-dd');
        type = 'daily';
    }
    
    const { data } = await getUserRegisterTrend({ 
      type, 
      startDate, 
      endDate 
    });
    
    if (data) {
      renderUserChart(data);
    }
  } catch (error) {
    console.error('获取用户趋势数据失败:', error);
    ElMessage.error('获取用户趋势数据失败');
  } finally {
    userChartLoading.value = false;
  }
};

// 获取职位数据
const fetchJobData = async () => {
  if (!jobChart.value) return;
  
  jobChartLoading.value = true;
  try {
    const { data } = await getJobStats();
    if (data) {
      renderJobChart(data);
    }
  } catch (error) {
    console.error('获取职位数据失败:', error);
    ElMessage.error('获取职位数据失败');
  } finally {
    jobChartLoading.value = false;
  }
};

// 获取最近活动
const fetchRecentActivities = async () => {
  logsLoading.value = true;
  try {
    const response = await fetchRecentLogs(10);
    // 确保响应存在且有data属性
    if (!response || !response.data) {
      console.error('获取日志数据失败或数据为空');
      recentActivities.value = [];
      return;
    }
    
    const { data } = response;
    console.log('获取到的日志数据:', data);
    
    // 处理数据，确保格式正确
    if (Array.isArray(data)) {
      recentActivities.value = data.map(log => ({
        id: log.id || 0,
        time: log.time || '未知时间',
        type: log.type || '其他操作',
        action: log.action || '',
        description: log.description || log.action || '未知操作',
        content: log.content || '无操作内容',
        user: log.user || '未知用户',
        ipAddress: log.ipAddress || '-'
      }));
    } else if (data.logs && Array.isArray(data.logs)) {
      recentActivities.value = data.logs.map(log => ({
        id: log.id || 0,
        time: log.time || '未知时间',
        type: log.type || '其他操作',
        action: log.action || '',
        description: log.description || log.action || '未知操作',
        content: log.content || '无操作内容',
        user: log.user || '未知用户',
        ipAddress: log.ipAddress || '-'
      }));
    } else if (data.list && Array.isArray(data.list)) {
      // 适配后端返回的分页格式
      recentActivities.value = data.list.map(log => ({
        id: log.id || 0,
        time: log.time || '未知时间',
        type: log.type || '其他操作',
        action: log.action || '',
        description: log.description || log.action || '未知操作',
        content: log.content || '无操作内容',
        user: log.user || '未知用户',
        ipAddress: log.ipAddress || '-'
      }));
    } else {
      console.error('收到的日志数据格式不符合预期', data);
      recentActivities.value = [];
    }
  } catch (error) {
    console.error('获取最近活动失败:', error);
    ElMessage.error('获取最近活动数据失败');
    recentActivities.value = [];
  } finally {
    logsLoading.value = false;
  }
};

// 渲染用户注册趋势图表
const renderUserChart = (data) => {
  // 检查DOM元素是否存在
  if (!userChart.value) {
    console.error('用户趋势图DOM元素未找到');
    return;
  }
  
  try {
    console.log('渲染用户趋势图，数据:', data);
    
    // 销毁已有实例，避免重复渲染问题
    let chart = echarts.getInstanceByDom(userChart.value);
    if (chart) {
      chart.dispose();
    }
    
    // 创建新的图表实例
    chart = echarts.init(userChart.value);
    
    // 从后端返回的实际数据格式中提取数据
    const xAxisData = data.dates || [];
    const studentData = data.studentData || []; // 修改为studentData
    const companyData = data.companyData || []; // 修改为companyData
    
    console.log('图表数据:', {
      xAxisData,
      studentData,
      companyData
    });
    
    const option = {
      title: {
        text: '用户增长趋势',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['学生用户', '企业用户'],
        bottom: 0
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '10%',
        top: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: xAxisData
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '学生用户',
          type: 'line',
          smooth: true,
          data: studentData
        },
        {
          name: '企业用户',
          type: 'line',
          smooth: true,
          data: companyData
        }
      ]
    };
    
    chart.setOption(option);
    
    // 响应窗口大小变化
    window.addEventListener('resize', () => {
      chart.resize();
    });
  } catch (error) {
    console.error('渲染用户趋势图失败:', error);
  }
};

// 渲染职位分布图表
const renderJobChart = (data) => {
  // 检查DOM元素是否存在
  if (!jobChart.value) {
    console.error('职位分布图DOM元素未找到');
    return;
  }
  
  try {
    console.log('渲染职位分布图，数据:', data);
    
    // 销毁已有实例，避免重复渲染问题
    let chart = echarts.getInstanceByDom(jobChart.value);
    if (chart) {
      chart.dispose();
    }
    
    // 创建新的图表实例
    chart = echarts.init(jobChart.value);
    
    // 确定要显示的数据
    let chartData = [];
    let chartTitle = '';
    
    if (jobChartType.value === 'city') {
      chartData = data.cityDistribution || [];
      chartTitle = '职位城市分布';
    } else if (jobChartType.value === 'industry') {
      chartData = data.industryDistribution || [];
      chartTitle = '职位行业分布';
    }
    
    console.log('选择的图表数据:', {
      type: jobChartType.value,
      chartData,
      chartTitle
    });
    
    // 处理数据格式 - 适配后端实际返回的数据结构
    // 注意这里使用了name和value直接传递，不再需要转换
    const formattedData = chartData.map(item => ({
      name: item.name,
      value: item.value // 修改为value而不是count
    }));
    
    const option = {
      title: {
        text: chartTitle,
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        type: 'scroll',
        orient: 'horizontal',
        bottom: 0,
        data: formattedData.map(item => item.name)
      },
      series: [
        {
          name: chartTitle,
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '18',
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: formattedData
        }
      ]
    };
    
    chart.setOption(option);
    
    // 响应窗口大小变化
    window.addEventListener('resize', () => {
      chart.resize();
    });
  } catch (error) {
    console.error('渲染职位分布图失败:', error);
  }
};

// 监听时间范围变化，重新获取数据
watch(userChartTimeRange, () => {
  fetchUserTrendData();
});

// 监听职位图表类型变化，重新绘制图表
watch(jobChartType, () => {
  fetchJobData();
});

// 刷新仪表盘数据
const refreshDashboard = async () => {
  try {
    ElMessage.info('正在刷新数据...');
    
    // 重新获取所有数据
    await fetchBasicStats();
    await fetchUserTrendData();
    await fetchJobData();
    await fetchRecentActivities();
    
    ElMessage.success('数据已更新');
  } catch (error) {
    console.error('刷新数据失败:', error);
    ElMessage.error('刷新数据失败');
  }
};

// 查看日志详情
const viewLogDetail = (id) => {
  // 从日志列表中查找对应的日志
  const log = recentActivities.value.find(item => item.id === id);
  if (log) {
    currentLog.value = log;
    logDetailVisible.value = true;
  } else {
    ElMessage.warning('找不到对应的日志记录');
  }
};

// 跳转到系统日志页面
const goToSystemLogs = () => {
  router.push('/admin/logs');
};

// 初始化
onMounted(async () => {
  await fetchBasicStats();
  
  // 确保DOM已渲染
  await nextTick();
  
  fetchUserTrendData();
  fetchJobData();
  fetchRecentActivities();
});
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stats-card {
  height: 120px;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.stats-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1);
}

.stats-card-content {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  padding: 20px;
  z-index: 2;
  position: relative;
}

.stats-card-title {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  font-size: 16px;
  color: #606266;
}

.stats-card-title .icon {
  margin-right: 8px;
  font-size: 20px;
}

.stats-card-value {
  font-size: 26px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stats-card-growth {
  font-size: 14px;
  color: #909399;
}

.stats-card-growth .positive {
  color: #67C23A;
}

.stats-card-growth .negative {
  color: #F56C6C;
}

.stats-card-bg {
  position: absolute;
  right: -20px;
  bottom: -20px;
  font-size: 120px;
  opacity: 0.05;
  transform: rotate(-15deg);
  z-index: 1;
}

.chart-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-container {
  position: relative;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  border-bottom: 1px solid #EBEEF5;
}

.chart-title {
  font-size: 16px;
  font-weight: bold;
  margin: 0;
}

.chart-content {
  height: 400px;
}

.time-range-selector,
.chart-type-selector {
  display: flex;
  align-items: center;
}

.time-range-selector .label,
.chart-type-selector .label {
  margin-right: 10px;
  color: #606266;
}

.recent-activity {
  margin-bottom: 20px;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-header h3 {
  margin: 0;
}

@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .chart-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
}
</style> 
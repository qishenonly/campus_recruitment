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

    <!-- 数据概览卡片 -->
    <div class="data-overview">
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="data-card" shadow="hover">
            <div class="data-icon student-icon">
              <el-icon><i-ep-user /></el-icon>
            </div>
            <div class="data-info">
              <div class="data-title">学生用户</div>
              <div class="data-value">{{ statsData.studentCount }}</div>
              <div class="data-trend up">
                <span>较上月</span>
                <el-icon><i-ep-top /></el-icon>
                <span>{{ statsData.studentGrowth }}%</span>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="data-card" shadow="hover">
            <div class="data-icon company-icon">
              <el-icon><i-ep-office-building /></el-icon>
            </div>
            <div class="data-info">
              <div class="data-title">企业用户</div>
              <div class="data-value">{{ statsData.companyCount }}</div>
              <div class="data-trend up">
                <span>较上月</span>
                <el-icon><i-ep-top /></el-icon>
                <span>{{ statsData.companyGrowth }}%</span>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="data-card" shadow="hover">
            <div class="data-icon job-icon">
              <el-icon><i-ep-briefcase /></el-icon>
            </div>
            <div class="data-info">
              <div class="data-title">职位数量</div>
              <div class="data-value">{{ statsData.jobCount }}</div>
              <div class="data-trend up">
                <span>较上月</span>
                <el-icon><i-ep-top /></el-icon>
                <span>{{ statsData.jobGrowth }}%</span>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="data-card" shadow="hover">
            <div class="data-icon app-icon">
              <el-icon><i-ep-message /></el-icon>
            </div>
            <div class="data-info">
              <div class="data-title">申请数量</div>
              <div class="data-value">{{ statsData.applicationCount }}</div>
              <div class="data-trend down">
                <span>较上月</span>
                <el-icon><i-ep-bottom /></el-icon>
                <span>{{ statsData.applicationGrowth }}%</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="chart-section">
      <el-row :gutter="16">
        <el-col :xs="24" :md="12">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="chart-header">
                <h3>用户增长趋势</h3>
                <el-radio-group v-model="userChartTimeRange" size="small">
                  <el-radio-button label="week">周</el-radio-button>
                  <el-radio-button label="month">月</el-radio-button>
                  <el-radio-button label="year">年</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div class="chart-container" ref="userChart"></div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :md="12">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="chart-header">
                <h3>职位分布</h3>
                <el-select v-model="jobChartType" size="small" style="width: 120px">
                  <el-option label="城市分布" value="city" />
                  <el-option label="行业分布" value="industry" />
                </el-select>
              </div>
            </template>
            <div class="chart-container" ref="jobChart"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 最近活动 -->
    <el-card class="recent-activity" shadow="hover">
      <template #header>
        <div class="activity-header">
          <h3>最近活动</h3>
          <el-button type="primary" text>查看全部</el-button>
        </div>
      </template>
      
      <el-table :data="recentActivities" style="width: 100%" :max-height="300">
        <el-table-column prop="time" label="时间" width="160" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag
              :type="activityTypeMap[row.type].type"
              effect="light"
              size="small">
              {{ activityTypeMap[row.type].label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column prop="user" label="用户" width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="100">
          <template #default>
            <el-button type="primary" link size="small">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
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
import { 
  getBasicStats, 
  getUserRegisterTrend, 
  getJobStats, 
  getRecentLogs 
} from '../../api/admin-dashboard';

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

// 当前日期
const currentDate = format(new Date(), 'yyyy年MM月dd日');

// 图表引用
const userChart = ref(null);
const jobChart = ref(null);

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

// 活动类型映射
const activityTypeMap = {
  register: { label: '注册', type: 'success' },
  login: { label: '登录', type: 'info' },
  apply: { label: '申请', type: '' },
  post: { label: '发布', type: 'warning' },
  interview: { label: '面试', type: 'danger' }
};

// 最近活动数据
const recentActivities = ref([]);

// 刷新仪表盘数据
const refreshDashboard = () => {
  fetchDashboardData();
  ElMessage.success('数据已更新');
};

// 获取仪表盘数据
const fetchDashboardData = async () => {
  try {
    // 获取基础统计数据
    const statsRes = await getBasicStats();
    if (statsRes.code === 200 && statsRes.data) {
      Object.assign(statsData, statsRes.data);
    }
    
    // 获取最近活动
    const logsRes = await getRecentLogs();
    if (logsRes.code === 200 && logsRes.data) {
      recentActivities.value = logsRes.data;
    }
    
    // 初始化图表
    initCharts();
  } catch (error) {
    console.error('获取仪表盘数据失败:', error);
    ElMessage.error('获取数据失败，请稍后重试');
  }
};

// 获取用户注册趋势数据
const fetchUserTrendData = async () => {
  try {
    const res = await getUserRegisterTrend({
      type: userChartTimeRange.value
    });
    
    if (res.code === 200 && res.data) {
      renderUserChart(res.data);
    }
  } catch (error) {
    console.error('获取用户趋势数据失败:', error);
  }
};

// 获取职位分布数据
const fetchJobDistributionData = async () => {
  try {
    const res = await getJobStats();
    
    if (res.code === 200 && res.data) {
      renderJobChart(res.data);
    }
  } catch (error) {
    console.error('获取职位分布数据失败:', error);
  }
};

// 渲染用户趋势图表
const renderUserChart = (data) => {
  if (userChart.value) {
    const chart = echarts.init(userChart.value);
    
    const option = {
      color: ['#1677FF', '#13C2C2'],
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['学生用户', '企业用户'],
        right: 0
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: data.dates || []
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '学生用户',
          type: 'line',
          smooth: true,
          data: data.studentData || []
        },
        {
          name: '企业用户',
          type: 'line',
          smooth: true,
          data: data.companyData || []
        }
      ]
    };
    
    chart.setOption(option);
    
    // 窗口大小变化时重绘图表
    window.addEventListener('resize', () => {
      chart.resize();
    });
  }
};

// 渲染职位分布图表
const renderJobChart = (data) => {
  if (jobChart.value) {
    const chart = echarts.init(jobChart.value);
    
    // 根据类型选择合适的数据
    const jobData = jobChartType.value === 'city' ? 
      (data.cityDistribution || []) : 
      (data.industryDistribution || []);
    
    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: 10,
        top: 'center',
        data: jobData.map(item => item.name)
      },
      series: [
        {
          name: '职位分布',
          type: 'pie',
          radius: ['50%', '70%'],
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
              fontSize: 16,
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: jobData
        }
      ]
    };
    
    chart.setOption(option);
    
    // 窗口大小变化时重绘图表
    window.addEventListener('resize', () => {
      chart.resize();
    });
  }
};

// 监听图表类型变化
const initCharts = () => {
  nextTick(() => {
    fetchUserTrendData();
    fetchJobDistributionData();
  });
};

// 监听图表类型变化
watch(userChartTimeRange, () => {
  fetchUserTrendData();
});

watch(jobChartType, () => {
  fetchJobDistributionData();
});

// 页面加载时初始化
onMounted(() => {
  fetchDashboardData();
});
</script>

<style scoped>
.dashboard-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.welcome-card {
  background-color: #fff;
  margin-bottom: 4px;
}

.welcome-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-header h2 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-info h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 500;
  color: #222;
}

.welcome-info p {
  margin: 0;
  color: #666;
}

.data-overview {
  margin-bottom: 4px;
}

.data-card {
  height: 140px;
  padding: 10px;
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  overflow: hidden;
  position: relative;
  transition: all 0.3s;
}

.data-card:hover {
  transform: translateY(-5px);
}

.data-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 16px;
}

.data-icon .el-icon {
  font-size: 32px;
  color: #fff;
}

.student-icon {
  background-color: #1677FF;
}

.company-icon {
  background-color: #13C2C2;
}

.job-icon {
  background-color: #722ED1;
}

.app-icon {
  background-color: #EB2F96;
}

.data-info {
  flex: 1;
}

.data-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.data-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.data-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.data-trend.up {
  color: #52c41a;
}

.data-trend.down {
  color: #f5222d;
}

.chart-section {
  margin-bottom: 4px;
}

.chart-card {
  margin-bottom: 16px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.chart-container {
  height: 300px;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.recent-activity {
  margin-bottom: 20px;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .welcome-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .data-card {
    height: auto;
  }
  
  .chart-container {
    height: 250px;
  }
}
</style> 
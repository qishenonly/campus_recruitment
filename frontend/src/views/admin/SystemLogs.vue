<template>
  <div class="system-logs-container">
    <el-card class="system-logs-card">
      <template #header>
        <div class="card-header">
          <h2>系统日志</h2>
          <div class="header-actions">
            <el-button type="primary" @click="fetchLogs">
              <el-icon><i-ep-refresh /></el-icon>刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 搜索筛选区域 -->
      <div class="filter-container">
        <el-form :inline="true" :model="queryParams" class="demo-form-inline">
          <el-form-item label="操作类型">
            <el-select
              v-model="queryParams.type"
              placeholder="选择类型"
              clearable
              style="width: 150px"
            >
              <el-option label="全部" value="" />
              <el-option label="学生管理" value="学生管理" />
              <el-option label="企业管理" value="企业管理" />
              <el-option label="职位管理" value="职位管理" />
              <el-option label="系统设置" value="系统设置" />
              <el-option label="用户认证" value="用户认证" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="操作者">
            <el-input 
              v-model="queryParams.operator"
              placeholder="操作者"
              clearable
              style="width: 150px"
            />
          </el-form-item>
          
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD HH:mm:ss"
              :default-time="['00:00:00', '23:59:59']"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><i-ep-search /></el-icon>搜索
            </el-button>
            <el-button @click="resetQuery">
              <el-icon><i-ep-refresh /></el-icon>重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 日志数据表格 -->
      <el-table
        :data="logsList"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="time" label="操作时间" width="160" sortable />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTagType(row.type)" effect="light" size="small">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="操作" width="150" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="user" label="操作者" width="120" />
        <el-table-column prop="ipAddress" label="IP地址" width="130" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 日志详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
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
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getSystemLogs } from '@/api/systemLog';

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  type: '',
  operator: '',
  startTime: '',
  endTime: ''
});

// 日期范围
const dateRange = ref([]);

// 日志列表数据
const logsList = ref([]);
const total = ref(0);
const loading = ref(false);
const dialogVisible = ref(false);
const currentLog = ref({});

// 获取标签类型
const getTagType = (type) => {
  if (!type) return '';
  
  const typeMap = {
    '职位管理': 'success',
    '学生管理': 'info',
    '企业管理': 'warning',
    '系统设置': 'danger',
    '用户认证': 'primary',
  };
  
  // 匹配前缀
  for (const [key, value] of Object.entries(typeMap)) {
    if (type.startsWith(key)) {
      return value;
    }
  }
  
  return '';
};

// 查看日志详情
const handleViewDetail = (row) => {
  currentLog.value = row;
  dialogVisible.value = true;
};

// 搜索按钮点击事件
const handleSearch = () => {
  // 处理时间范围
  if (dateRange.value && dateRange.value.length === 2) {
    queryParams.startTime = dateRange.value[0];
    queryParams.endTime = dateRange.value[1];
  } else {
    queryParams.startTime = '';
    queryParams.endTime = '';
  }
  
  queryParams.pageNum = 1;
  fetchLogs();
};

// 重置查询条件
const resetQuery = () => {
  // 重置查询表单
  queryParams.type = '';
  queryParams.operator = '';
  queryParams.startTime = '';
  queryParams.endTime = '';
  dateRange.value = [];
  queryParams.pageNum = 1;
  
  // 重新加载数据
  fetchLogs();
};

// 处理页码变化
const handleCurrentChange = (page) => {
  queryParams.pageNum = page;
  fetchLogs();
};

// 处理每页记录数变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size;
  fetchLogs();
};

// 获取日志列表
const fetchLogs = async () => {
  loading.value = true;
  try {
    // 处理请求参数
    const params = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
      type: queryParams.type,
      operator: queryParams.operator || undefined,
      startTime: queryParams.startTime,
      endTime: queryParams.endTime
    };
    
    const response = await getSystemLogs(params);
    console.log('系统日志响应:', response);
    
    if (response.code === 200 && response.data) {
      // 根据实际返回格式调整：后端返回的日志数据在data.list中，而不是data.records
      if (response.data.list && Array.isArray(response.data.list)) {
        logsList.value = response.data.list.map(log => ({
          id: log.id,
          time: log.time || '',
          type: log.type || '其他',
          action: log.action || '',
          description: log.description || log.action || '未知操作',
          content: log.content || '',
          user: log.user || '未知用户',
          ipAddress: log.ipAddress || '-'
        }));
        total.value = response.data.total || 0;
      } else {
        logsList.value = [];
        total.value = 0;
        ElMessage.warning('未获取到日志数据');
      }
    } else {
      ElMessage.error(response.message || '获取日志列表失败');
    }
  } catch (error) {
    console.error('获取日志列表失败:', error);
    ElMessage.error('获取日志列表失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 页面加载时获取数据
onMounted(() => {
  fetchLogs();
});
</script>

<style scoped>
.system-logs-container {
  padding: 20px;
}

.system-logs-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
}

.filter-container {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 
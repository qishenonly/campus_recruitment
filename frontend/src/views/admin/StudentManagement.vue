<template>
  <div class="student-management">
    <el-card class="page-header-card">
      <div class="page-header">
        <div class="page-title">
          <h2>学生用户管理</h2>
          <el-tag type="info">{{ totalStudents }} 名学生</el-tag>
        </div>
        <div class="page-actions">
          <el-button type="primary" @click="exportStudentList">
            <el-icon><i-ep-download /></el-icon>导出数据
          </el-button>
          <el-button type="success" @click="showImportDialog">
            <el-icon><i-ep-upload /></el-icon>批量导入
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 搜索筛选区 -->
    <el-card class="filter-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="姓名">
          <el-input 
            v-model="queryParams.name" 
            placeholder="请输入学生姓名" 
            clearable 
            @clear="handleQuery"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="学校">
          <el-select 
            v-model="queryParams.school" 
            placeholder="选择学校" 
            clearable
            @change="handleQuery"
          >
            <el-option 
              v-for="item in schoolOptions" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="专业">
          <el-select 
            v-model="queryParams.major" 
            placeholder="选择专业" 
            clearable
            @change="handleQuery"
          >
            <el-option 
              v-for="item in majorOptions" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="queryParams.status" 
            placeholder="账号状态" 
            clearable
            @change="handleQuery"
          >
            <el-option label="正常" value="ACTIVE" />
            <el-option label="待审核" value="INACTIVE" />
            <el-option label="已禁用" value="BLOCKED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><i-ep-search /></el-icon>搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><i-ep-refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card">
      <div class="table-operations">
        <div class="batch-actions">
          <el-dropdown @command="handleBatchCommand" v-if="selectedStudents.length > 0">
            <el-button type="primary">
              批量操作<el-icon class="el-icon--right"><i-ep-arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="verify">通过审核</el-dropdown-item>
                <el-dropdown-item command="disable">禁用账号</el-dropdown-item>
                <el-dropdown-item command="enable">启用账号</el-dropdown-item>
                <el-dropdown-item command="delete" divided>
                  <span style="color: #f56c6c;">删除账号</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <span v-if="selectedStudents.length > 0" class="selected-count">
            已选择 {{ selectedStudents.length }} 项
          </span>
        </div>
        <div class="table-settings">
          <el-tooltip content="刷新数据" placement="top">
            <el-button circle @click="refreshTable">
              <el-icon><i-ep-refresh /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="列设置" placement="top">
            <el-button circle @click="showColumnSettings = true">
              <el-icon><i-ep-set-up /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="studentList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        border
        highlight-current-row
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" v-if="visibleColumns.includes('id')" />
        <el-table-column prop="name" label="姓名" width="120" show-overflow-tooltip />
        <el-table-column prop="gender" label="性别" width="80" v-if="visibleColumns.includes('gender')">
          <template #default="{ row }">
            {{ row.gender === 'male' ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="school" label="学校" min-width="180" show-overflow-tooltip />
        <el-table-column prop="major" label="专业" min-width="160" show-overflow-tooltip v-if="visibleColumns.includes('major')" />
        <el-table-column prop="education" label="学历" width="100" v-if="visibleColumns.includes('education')" />
        <el-table-column prop="graduationYear" label="毕业年份" width="120" v-if="visibleColumns.includes('graduationYear')" />
        <el-table-column prop="phone" label="手机号" width="140" v-if="visibleColumns.includes('phone')" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip v-if="visibleColumns.includes('email')" />
        <el-table-column prop="registerTime" label="注册时间" width="180" v-if="visibleColumns.includes('registerTime')" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'ACTIVE' ? 'success' : row.status === 'INACTIVE' ? 'warning' : 'danger'"
              effect="light"
            >
              {{ row.status === 'ACTIVE' ? '正常' : row.status === 'INACTIVE' ? '待审核' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewStudentDetail(row)">查看</el-button>
            <el-button link type="primary" @click="editStudent(row)">编辑</el-button>
            <el-dropdown trigger="click">
              <el-button link type="primary">
                更多<el-icon class="el-icon--right"><i-ep-arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="toggleStatus(row)">
                    {{ row.status === 'ACTIVE' ? '禁用账号' : '启用账号' }}
                  </el-dropdown-item>
                  <el-dropdown-item @click="resetPassword(row)">重置密码</el-dropdown-item>
                  <el-dropdown-item divided @click="confirmDelete(row)">
                    <span style="color: #f56c6c;">删除账号</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalStudents"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 列设置对话框 -->
    <el-dialog
      v-model="showColumnSettings"
      title="列设置"
      width="400px"
    >
      <el-checkbox-group v-model="visibleColumns">
        <el-checkbox v-for="col in allColumns" :key="col.prop" :label="col.prop">{{ col.label }}</el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="showColumnSettings = false">取消</el-button>
        <el-button type="primary" @click="saveColumnSettings">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog
      v-model="showImport"
      title="批量导入学生数据"
      width="500px"
    >
      <el-upload
        class="upload-demo"
        drag
        action="#"
        :auto-upload="false"
        :on-change="handleFileChange"
        :limit="1"
        accept=".xlsx,.xls,.csv"
      >
        <el-icon class="el-icon--upload"><i-ep-upload-filled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            请上传Excel或CSV格式文件，<el-link type="primary" @click="downloadTemplate">下载模板</el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="showImport = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="importStudents">确认导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  getStudentList as fetchStudentList, 
  getStudentDetail, 
  updateStudent, 
  verifyStudent, 
  disableStudent, 
  enableStudent, 
  deleteStudent, 
  resetStudentPassword, 
  batchVerifyStudents, 
  batchDisableStudents, 
  batchEnableStudents, 
  batchDeleteStudents, 
  exportStudents, 
  importStudents as doImportStudents, 
  downloadStudentTemplate 
} from '../../api/admin-students';

// 数据状态
const loading = ref(false);
const totalStudents = ref(0);
const studentList = ref([]);
const selectedStudents = ref([]);
const showColumnSettings = ref(false);
const showImport = ref(false);
const importing = ref(false);
const importFile = ref(null);

// 搜索参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  school: '',
  major: '',
  status: ''
});

// 学校选项
const schoolOptions = [
  { label: '清华大学', value: '清华大学' },
  { label: '北京大学', value: '北京大学' },
  { label: '浙江大学', value: '浙江大学' },
  { label: '上海交通大学', value: '上海交通大学' },
  { label: '复旦大学', value: '复旦大学' },
  { label: '南京大学', value: '南京大学' },
  { label: '中国人民大学', value: '中国人民大学' },
  { label: '武汉大学', value: '武汉大学' },
];

// 专业选项
const majorOptions = [
  { label: '计算机科学与技术', value: '计算机科学与技术' },
  { label: '软件工程', value: '软件工程' },
  { label: '电子信息工程', value: '电子信息工程' },
  { label: '通信工程', value: '通信工程' },
  { label: '数据科学与大数据技术', value: '数据科学与大数据技术' },
  { label: '人工智能', value: '人工智能' },
  { label: '信息安全', value: '信息安全' },
  { label: '自动化', value: '自动化' },
];

// 所有可选列
const allColumns = [
  { prop: 'id', label: 'ID' },
  { prop: 'gender', label: '性别' },
  { prop: 'major', label: '专业' },
  { prop: 'education', label: '学历' },
  { prop: 'graduationYear', label: '毕业年份' },
  { prop: 'phone', label: '手机号' },
  { prop: 'email', label: '邮箱' },
  { prop: 'registerTime', label: '注册时间' }
];

// 默认可见列
const visibleColumns = ref(['id', 'gender', 'major', 'education', 'registerTime']);

// 处理查询
const handleQuery = () => {
  queryParams.pageNum = 1;
  getStudentList();
};

// 重置查询条件
const resetQuery = () => {
  queryParams.name = '';
  queryParams.school = '';
  queryParams.major = '';
  queryParams.status = '';
  handleQuery();
};

// 获取学生列表
const getStudentList = () => {
  loading.value = true;
  
  // 调用API获取数据
  fetchStudentList(queryParams).then(res => {
    if (res.code === 200) {
      studentList.value = res.data.list || [];
      totalStudents.value = res.data.total || 0;
    } else {
      ElMessage.error(res.message || '获取学生列表失败');
    }
  }).catch(error => {
    console.error('获取学生列表失败:', error);
    ElMessage.error('获取学生列表失败');
  }).finally(() => {
    loading.value = false;
  });
};

// 页码改变
const handleCurrentChange = (val) => {
  queryParams.pageNum = val;
  getStudentList();
};

// 每页数量改变
const handleSizeChange = (val) => {
  queryParams.pageSize = val;
  getStudentList();
};

// 表格选择改变
const handleSelectionChange = (selection) => {
  selectedStudents.value = selection;
};

// 刷新表格
const refreshTable = () => {
  getStudentList();
  ElMessage.success('数据已刷新');
};

// 保存列设置
const saveColumnSettings = () => {
  showColumnSettings.value = false;
  localStorage.setItem('adminStudentColumns', JSON.stringify(visibleColumns.value));
  ElMessage.success('列设置已保存');
};

// 查看学生详情
const viewStudentDetail = (row) => {
  // 调用API获取学生详情
  getStudentDetail(row.id).then(res => {
    if (res.code === 200) {
      // 显示详情，根据实际需求处理
      ElMessage.info(`查看学生：${row.name}`);
    }
  }).catch(error => {
    console.error('获取学生详情失败:', error);
    ElMessage.error('获取学生详情失败');
  });
};

// 编辑学生
const editStudent = (row) => {
  // 实际项目中应打开编辑对话框
  ElMessage.info(`编辑学生：${row.name}`);
};

// 切换状态
const toggleStatus = (row) => {
  const newStatus = row.status === 'ACTIVE' ? 'BLOCKED' : 'ACTIVE';
  const action = newStatus === 'ACTIVE' ? '启用' : '禁用';
  
  ElMessageBox.confirm(
    `确定要${action}该学生账号吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    // 调用相应的API
    const apiCall = newStatus === 'ACTIVE' ? enableStudent : disableStudent;
    
    apiCall(row.id).then(res => {
      if (res.code === 200) {
        row.status = newStatus;
        ElMessage.success(`已${action}账号`);
      } else {
        ElMessage.error(res.message || `${action}账号失败`);
      }
    }).catch(error => {
      console.error(`${action}账号失败:`, error);
      ElMessage.error(`${action}账号失败`);
    });
  }).catch(() => {});
};

// 重置密码
const resetPassword = (row) => {
  ElMessageBox.confirm(
    '确定要重置该用户的密码吗？密码将重置为初始密码并发送到用户邮箱。',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    // 调用重置密码API
    resetStudentPassword(row.id).then(res => {
      if (res.code === 200) {
        ElMessage.success(`已重置 ${row.name} 的密码`);
      } else {
        ElMessage.error(res.message || '重置密码失败');
      }
    }).catch(error => {
      console.error('重置密码失败:', error);
      ElMessage.error('重置密码失败');
    });
  }).catch(() => {});
};

// 确认删除
const confirmDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除该学生账号吗？此操作不可恢复。',
    '警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error',
    }
  ).then(() => {
    // 调用删除API
    deleteStudent(row.id).then(res => {
      if (res.code === 200) {
        // 从列表中移除
        const index = studentList.value.findIndex(item => item.id === row.id);
        if (index !== -1) {
          studentList.value.splice(index, 1);
          totalStudents.value--;
        }
        ElMessage.success('删除成功');
      } else {
        ElMessage.error(res.message || '删除失败');
      }
    }).catch(error => {
      console.error('删除失败:', error);
      ElMessage.error('删除失败');
    });
  }).catch(() => {});
};

// 批量操作
const handleBatchCommand = (command) => {
  if (selectedStudents.value.length === 0) {
    ElMessage.warning('请选择要操作的学生');
    return;
  }
  
  const operations = {
    verify: {
      title: '批量审核通过',
      message: `确定要审核通过选中的 ${selectedStudents.value.length} 个学生账号吗？`,
      success: '批量审核成功',
      api: batchVerifyStudents,
      action: (res) => {
        selectedStudents.value.forEach(student => {
          if (student.status === 'INACTIVE') {
            student.status = 'ACTIVE';
          }
        });
      }
    },
    disable: {
      title: '批量禁用账号',
      message: `确定要禁用选中的 ${selectedStudents.value.length} 个学生账号吗？`,
      success: '批量禁用成功',
      api: batchDisableStudents,
      action: (res) => {
        selectedStudents.value.forEach(student => {
          student.status = 'BLOCKED';
        });
      }
    },
    enable: {
      title: '批量启用账号',
      message: `确定要启用选中的 ${selectedStudents.value.length} 个学生账号吗？`,
      success: '批量启用成功',
      api: batchEnableStudents,
      action: (res) => {
        selectedStudents.value.forEach(student => {
          student.status = 'ACTIVE';
        });
      }
    },
    delete: {
      title: '批量删除账号',
      message: `确定要删除选中的 ${selectedStudents.value.length} 个学生账号吗？此操作不可恢复。`,
      success: '批量删除成功',
      api: batchDeleteStudents,
      action: (res) => {
        const ids = selectedStudents.value.map(student => student.id);
        studentList.value = studentList.value.filter(student => !ids.includes(student.id));
        totalStudents.value -= ids.length;
      },
      type: 'error'
    }
  };
  
  const operation = operations[command];
  
  ElMessageBox.confirm(
    operation.message,
    operation.title,
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: operation.type || 'warning',
    }
  ).then(() => {
    // 调用批量操作API
    const ids = selectedStudents.value.map(student => student.id);
    operation.api(ids).then(res => {
      if (res.code === 200) {
        operation.action(res);
        ElMessage.success(operation.success);
      } else {
        ElMessage.error(res.message || '操作失败');
      }
    }).catch(error => {
      console.error('批量操作失败:', error);
      ElMessage.error('操作失败');
    });
  }).catch(() => {});
};

// 导出学生列表
const exportStudentList = () => {
  // 导出的查询参数，可选
  const exportParams = {
    name: queryParams.name,
    school: queryParams.school,
    major: queryParams.major,
    status: queryParams.status
  };
  
  exportStudents(exportParams).then(res => {
    // 处理二进制文件下载
    const blob = new Blob([res], { type: 'application/vnd.ms-excel' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    // 设置文件名
    link.setAttribute('download', `学生数据_${new Date().getTime()}.xlsx`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
    ElMessage.success('导出成功');
  }).catch(error => {
    console.error('导出失败:', error);
    ElMessage.error('导出失败');
  });
};

// 显示导入对话框
const showImportDialog = () => {
  showImport.value = true;
};

// 处理文件变化
const handleFileChange = (file) => {
  importFile.value = file.raw;
};

// 下载模板
const downloadTemplate = () => {
  downloadStudentTemplate().then(res => {
    // 处理二进制文件下载
    const blob = new Blob([res], { type: 'application/vnd.ms-excel' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', '学生导入模板.xlsx');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
    ElMessage.success('模板下载中...');
  }).catch(error => {
    console.error('下载模板失败:', error);
    ElMessage.error('下载模板失败');
  });
};

// 导入学生
const importStudents = () => {
  if (!importFile.value) {
    ElMessage.warning('请先选择要导入的文件');
    return;
  }
  
  importing.value = true;
  
  // 创建表单数据
  const formData = new FormData();
  formData.append('file', importFile.value);
  
  // 调用导入API
  doImportStudents(formData).then(res => {
    if (res.code === 200) {
      importing.value = false;
      showImport.value = false;
      ElMessage.success(res.message || '导入成功');
      getStudentList(); // 刷新列表
    } else {
      ElMessage.error(res.message || '导入失败');
    }
  }).catch(error => {
    console.error('导入失败:', error);
    ElMessage.error('导入失败');
  }).finally(() => {
    importing.value = false;
  });
};

// 初始化
onMounted(() => {
  // 尝试从本地存储获取列设置
  const savedColumns = localStorage.getItem('adminStudentColumns');
  if (savedColumns) {
    visibleColumns.value = JSON.parse(savedColumns);
  }
  
  getStudentList();
});
</script>

<style scoped>
.student-management {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header-card {
  margin-bottom: 4px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.filter-card {
  margin-bottom: 4px;
}

.table-card {
  margin-bottom: 4px;
}

.table-operations {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selected-count {
  font-size: 14px;
  color: #909399;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.el-tag {
  text-align: center;
  min-width: 60px;
}

/* 导入上传样式 */
.upload-demo {
  text-align: center;
}

.el-upload {
  width: 100%;
}

.el-upload-dragger {
  width: 100%;
}

@media (max-width: 768px) {
  .page-header, 
  .table-operations {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .page-actions,
  .table-settings {
    width: 100%;
    display: flex;
    justify-content: space-between;
  }
}
</style>

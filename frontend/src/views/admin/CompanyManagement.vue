<template>
  <div class="company-management">
    <el-card class="page-header-card">
      <div class="page-header">
        <div class="page-title">
          <h2>企业用户管理</h2>
          <el-tag type="info">{{ totalCompanies }} 家企业</el-tag>
        </div>
        <div class="page-actions">
          <el-button type="primary" @click="handleAddCompany">
            <el-icon><i-ep-plus /></el-icon>添加企业
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 搜索筛选区 -->
    <el-card class="filter-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="企业名称">
          <el-input 
            v-model="queryParams.name" 
            placeholder="请输入企业名称" 
            clearable 
            @clear="handleQuery"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="行业">
          <el-select 
            v-model="queryParams.industry" 
            placeholder="选择行业" 
            clearable
            @change="handleQuery"
          >
            <el-option 
              v-for="item in industryOptions" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="规模">
          <el-select 
            v-model="queryParams.scale" 
            placeholder="企业规模" 
            clearable
            @change="handleQuery"
          >
            <el-option label="初创企业" value="startup" />
            <el-option label="中小企业" value="small" />
            <el-option label="大型企业" value="large" />
            <el-option label="上市公司" value="listed" />
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
          <el-dropdown @command="handleBatchCommand" v-if="selectedCompanies.length > 0">
            <el-button type="primary">
              批量操作<el-icon class="el-icon--right"><i-ep-arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="verify">批量审核</el-dropdown-item>
                <el-dropdown-item command="disable">批量禁用</el-dropdown-item>
                <el-dropdown-item command="enable">批量启用</el-dropdown-item>
                <el-dropdown-item command="delete" divided>
                  <span style="color: #f56c6c;">批量删除</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <span v-if="selectedCompanies.length > 0" class="selected-count">
            已选择 {{ selectedCompanies.length }} 项
          </span>
        </div>
        <div class="table-settings">
          <el-button 
            type="success" 
            @click="exportCompanyList" 
            :loading="exporting"
            :disabled="exporting"
          >
            <el-icon><i-ep-download /></el-icon>导出
          </el-button>
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
        :data="companyList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        border
        highlight-current-row
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" v-if="visibleColumns.includes('id')" />
        <el-table-column prop="name" label="企业名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="industry" label="行业" min-width="120" show-overflow-tooltip v-if="visibleColumns.includes('industry')" />
        <el-table-column prop="scale" label="规模" width="100" v-if="visibleColumns.includes('scale')">
          <template #default="{ row }">
            {{ scaleMap[row.scale] || row.scale }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="所在地" width="120" v-if="visibleColumns.includes('location')" />
        <el-table-column prop="contactPerson" label="联系人" width="100" v-if="visibleColumns.includes('contactPerson')" />
        <el-table-column prop="phone" label="联系电话" width="140" v-if="visibleColumns.includes('phone')" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip v-if="visibleColumns.includes('email')" />
        <el-table-column prop="jobCount" label="职位数" width="90" v-if="visibleColumns.includes('jobCount')" />
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
            <el-button link type="primary" @click="viewCompanyDetail(row)">查看</el-button>
            <el-button link type="primary" @click="editCompany(row)">编辑</el-button>
            <el-dropdown trigger="click">
              <el-button link type="primary">
                更多<el-icon class="el-icon--right"><i-ep-arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="toggleStatus(row)">
                    {{ row.status === 'ACTIVE' ? '禁用账号' : '启用账号' }}
                  </el-dropdown-item>
                  <el-dropdown-item @click="viewJobs(row)">查看职位</el-dropdown-item>
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
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalCompanies"
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

    <!-- 企业表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑企业' : '添加企业'"
      width="600px"
      append-to-body
    >
      <el-form 
        ref="companyFormRef"
        :model="companyForm"
        :rules="companyRules"
        label-width="100px"
      >
        <el-form-item label="企业名称" prop="name">
          <el-input v-model="companyForm.name" placeholder="请输入企业名称" />
        </el-form-item>
        <el-form-item label="行业" prop="industry">
          <el-input v-model="companyForm.industry" placeholder="请输入所属行业" />
        </el-form-item>
        <el-form-item label="规模" prop="scale">
          <el-select v-model="companyForm.scale" placeholder="请选择企业规模" style="width: 100%">
            <el-option label="初创企业" value="startup" />
            <el-option label="中小企业" value="small" />
            <el-option label="大型企业" value="large" />
            <el-option label="上市公司" value="listed" />
          </el-select>
        </el-form-item>
        <el-form-item label="所在地" prop="location">
          <el-input v-model="companyForm.location" placeholder="请输入企业所在地" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="companyForm.contactPerson" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="companyForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="companyForm.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="companyForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="正常" value="ACTIVE" />
            <el-option label="待审核" value="INACTIVE" />
            <el-option label="已禁用" value="BLOCKED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  getCompanyList as fetchCompanyList, 
  getCompanyDetail, 
  addCompany as createCompany,
  updateCompany, 
  verifyCompany, 
  disableCompany, 
  enableCompany, 
  deleteCompany, 
  getCompanyJobs,
  batchVerifyCompanies, 
  batchDisableCompanies, 
  batchEnableCompanies, 
  batchDeleteCompanies, 
  exportCompanies
} from '@/api/admin-companies';

// 数据状态
const loading = ref(false);
const totalCompanies = ref(0);
const companyList = ref([]);
const selectedCompanies = ref([]);
const showColumnSettings = ref(false);

// 映射表
const scaleMap = {
  'startup': '初创企业',
  'small': '中小企业',
  'large': '大型企业',
  'listed': '上市公司'
};

// 查询参数
const queryParams = reactive({
  name: '',
  industry: '',
  scale: '',
  status: '',
  dateRange: []
});

// 分页参数
const currentPage = ref(1);
const pageSize = ref(10);
const sortBy = ref('createTime');
const sortDir = ref('desc');

// 行业选项
const industryOptions = [
  { label: '互联网/IT', value: '互联网/IT' },
  { label: '金融', value: '金融' },
  { label: '教育', value: '教育' },
  { label: '医疗健康', value: '医疗健康' },
  { label: '房地产', value: '房地产' },
  { label: '制造业', value: '制造业' },
  { label: '消费品', value: '消费品' },
  { label: '服务业', value: '服务业' },
];

// 所有可选列
const allColumns = [
  { prop: 'id', label: 'ID' },
  { prop: 'industry', label: '行业' },
  { prop: 'scale', label: '规模' },
  { prop: 'location', label: '所在地' },
  { prop: 'contactPerson', label: '联系人' },
  { prop: 'phone', label: '联系电话' },
  { prop: 'email', label: '邮箱' },
  { prop: 'jobCount', label: '职位数' },
  { prop: 'registerTime', label: '注册时间' }
];

// 默认可见列
const visibleColumns = ref(['id', 'industry', 'scale', 'location', 'contactPerson', 'jobCount']);

// 表单相关
const dialogVisible = ref(false);
const isEdit = ref(false);
const companyFormRef = ref(null);
const submitLoading = ref(false);
const companyForm = reactive({
  id: undefined,
  name: '',
  industry: '',
  scale: '',
  location: '',
  contactPerson: '',
  phone: '',
  email: '',
  status: 'ACTIVE'
});

// 表单校验规则
const companyRules = {
  name: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  industry: [{ required: true, message: '请输入所属行业', trigger: 'blur' }],
  scale: [{ required: true, message: '请选择企业规模', trigger: 'change' }],
  location: [{ required: true, message: '请输入企业所在地', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
};

// 处理查询
const handleQuery = () => {
  currentPage.value = 1;
  getCompanyList();
};

// 重置查询条件
const resetQuery = () => {
  queryParams.name = '';
  queryParams.industry = '';
  queryParams.scale = '';
  queryParams.status = '';
  handleQuery();
};

// 获取企业列表数据
const getCompanyList = () => {
  loading.value = true;
  console.log('开始获取企业列表数据，查询参数:', queryParams, '分页参数:', currentPage.value, pageSize.value);

  // 构建请求参数
  const params = {
    pageNum: currentPage.value,
    pageSize: pageSize.value,
    name: queryParams.name || undefined,
    industry: queryParams.industry || undefined,
    scale: queryParams.scale || undefined,
    status: queryParams.status || undefined,
    sortBy: sortBy.value,
    sortDir: sortDir.value
  };

  // 移除所有undefined的属性
  Object.keys(params).forEach(key => params[key] === undefined && delete params[key]);

  console.log('发送请求参数:', params);

  // 从API获取数据 
  fetchCompanyList(params)
    .then(res => {
      console.log('获取企业列表成功:', res);
      if (res.code === 200) {
        companyList.value = res.data.list || [];
        totalCompanies.value = res.data.total || 0;
      } else {
        ElMessage.warning(res.message || '获取企业列表失败');
      }
    })
    .catch(error => {
      console.error('获取企业列表错误:', error);
      ElMessage.error('获取企业列表失败: ' + (error.message || '服务器错误'));
    })
    .finally(() => {
      loading.value = false;
    });
};

// 页码改变
const handleCurrentChange = (val) => {
  currentPage.value = val;
  getCompanyList();
};

// 每页条数改变
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  getCompanyList();
};

// 排序方式改变
const handleSortChange = ({ prop, order }) => {
  if (prop && order) {
    sortBy.value = prop;
    sortDir.value = order === 'ascending' ? 'asc' : 'desc';
    getCompanyList();
  }
};

// 表格选择改变
const handleSelectionChange = (selection) => {
  selectedCompanies.value = selection;
};

// 刷新表格
const refreshTable = () => {
  getCompanyList();
  ElMessage.success('数据已刷新');
};

// 保存列设置
const saveColumnSettings = () => {
  showColumnSettings.value = false;
  localStorage.setItem('adminCompanyColumns', JSON.stringify(visibleColumns.value));
  ElMessage.success('列设置已保存');
};

// 查看企业详情
const viewCompanyDetail = (row) => {
  getCompanyDetail(row.id).then(res => {
    if (res.code === 200) {
      // 实际项目中应打开详情对话框
      ElMessage.info(`查看企业：${row.name}`);
    } else {
      ElMessage.error(res.message || '获取企业详情失败');
    }
  }).catch(error => {
    console.error('获取企业详情失败:', error);
    ElMessage.error('获取企业详情失败');
  });
};

// 编辑企业
const editCompany = (row) => {
  isEdit.value = true;
  resetForm();
  Object.keys(companyForm).forEach(key => {
    if (key in row) {
      companyForm[key] = row[key];
    }
  });
  dialogVisible.value = true;
};

// 重置表单
const resetForm = () => {
  if (companyFormRef.value) {
    companyFormRef.value.resetFields();
  }
  Object.keys(companyForm).forEach(key => {
    if (key === 'status') {
      companyForm[key] = 'ACTIVE';
    } else {
      companyForm[key] = '';
    }
  });
  companyForm.id = undefined;
};

// 提交表单
const submitForm = () => {
  if (!companyFormRef.value) return;
  
  companyFormRef.value.validate((valid) => {
    if (!valid) return;
    
    submitLoading.value = true;
    
    const saveData = { ...companyForm };
    
    // 调用API保存数据
    const savePromise = isEdit.value 
      ? updateCompany(saveData.id, saveData) 
      : createCompany(saveData);
    
    savePromise.then(res => {
      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '编辑成功' : '添加成功');
        dialogVisible.value = false;
        
        // 刷新列表
        getCompanyList();
      } else {
        ElMessage.error(res.message || (isEdit.value ? '编辑失败' : '添加失败'));
      }
    }).catch(error => {
      console.error(isEdit.value ? '编辑失败' : '添加失败', error);
      ElMessage.error((isEdit.value ? '编辑' : '添加') + '失败: ' + (error.message || '服务器错误'));
    }).finally(() => {
      submitLoading.value = false;
    });
  });
};

// 查看企业职位
const viewJobs = (row) => {
  getCompanyJobs(row.id, { pageNum: 1, pageSize: 10 }).then(res => {
    if (res.code === 200) {
      // 实际项目中应打开职位列表对话框
      ElMessage.info(`${row.name} 共有 ${res.data.total || 0} 个职位`);
    } else {
      ElMessage.error(res.message || '获取企业职位失败');
    }
  }).catch(error => {
    console.error('获取企业职位失败:', error);
    ElMessage.error('获取企业职位失败');
  });
};

// 添加企业
const handleAddCompany = () => {
  isEdit.value = false;
  resetForm();
  dialogVisible.value = true;
};

// 切换企业状态
const toggleStatus = (row) => {
  const newStatus = row.status === 'ACTIVE' ? 'BLOCKED' : 'ACTIVE';
  const action = row.status === 'ACTIVE' ? '禁用' : '启用';
  
  ElMessageBox.confirm(
    `确定要${action}该企业账号吗？`,
    '操作确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    if (newStatus === 'ACTIVE') {
      enableCompany(row.id).then(res => {
        if (res.code === 200) {
          ElMessage.success(`企业账号已${action}`);
          row.status = newStatus;
        } else {
          ElMessage.error(res.message || `${action}企业账号失败`);
        }
      }).catch(error => {
        console.error(`${action}企业账号失败:`, error);
        ElMessage.error(`${action}企业账号失败`);
      });
    } else {
      disableCompany(row.id).then(res => {
        if (res.code === 200) {
          ElMessage.success(`企业账号已${action}`);
          row.status = newStatus;
        } else {
          ElMessage.error(res.message || `${action}企业账号失败`);
        }
      }).catch(error => {
        console.error(`${action}企业账号失败:`, error);
        ElMessage.error(`${action}企业账号失败`);
      });
    }
  }).catch(() => {
    // 用户取消操作
  });
};

// 确认删除企业
const confirmDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除该企业账号吗？此操作不可恢复！',
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'danger'
    }
  ).then(() => {
    deleteCompany(row.id).then(res => {
      if (res.code === 200) {
        ElMessage.success('企业账号已删除');
        // 从列表中移除该企业
        companyList.value = companyList.value.filter(item => item.id !== row.id);
        totalCompanies.value -= 1;
      } else {
        ElMessage.error(res.message || '删除企业账号失败');
      }
    }).catch(error => {
      console.error('删除企业账号失败:', error);
      ElMessage.error('删除企业账号失败');
    });
  }).catch(() => {
    // 用户取消操作
  });
};

// 批量操作
const handleBatchCommand = (command) => {
  if (selectedCompanies.value.length === 0) {
    ElMessage.warning('请选择要操作的企业');
    return;
  }
  
  const ids = selectedCompanies.value.map(company => company.id);
  
  const operations = {
    verify: {
      title: '批量审核',
      message: `确定要审核通过选中的 ${selectedCompanies.value.length} 个企业账号吗？`,
      success: '批量审核成功',
      api: () => batchVerifyCompanies(ids, 'ACTIVE'),
      action: (res) => {
        selectedCompanies.value.forEach(company => {
          if (company.status === 'INACTIVE') {
            company.status = 'ACTIVE';
          }
        });
      }
    },
    disable: {
      title: '批量禁用',
      message: `确定要禁用选中的 ${selectedCompanies.value.length} 个企业账号吗？`,
      success: '批量禁用成功',
      api: () => batchDisableCompanies({ ids }),
      action: (res) => {
        selectedCompanies.value.forEach(company => {
          company.status = 'BLOCKED';
        });
      }
    },
    enable: {
      title: '批量启用',
      message: `确定要启用选中的 ${selectedCompanies.value.length} 个企业账号吗？`,
      success: '批量启用成功',
      api: () => batchEnableCompanies({ ids }),
      action: (res) => {
        selectedCompanies.value.forEach(company => {
          company.status = 'ACTIVE';
        });
      }
    },
    delete: {
      title: '批量删除',
      message: `确定要删除选中的 ${selectedCompanies.value.length} 个企业账号吗？此操作不可恢复！`,
      success: '批量删除成功',
      api: () => batchDeleteCompanies({ ids }),
      action: (res) => {
        companyList.value = companyList.value.filter(company => !ids.includes(company.id));
        totalCompanies.value -= ids.length;
      },
      type: 'danger'
    }
  };
  
  const operation = operations[command];
  if (!operation) {
    return;
  }
  
  ElMessageBox.confirm(
    operation.message,
    operation.title,
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: operation.type || 'warning'
    }
  ).then(() => {
    operation.api().then(res => {
      if (res.code === 200) {
        ElMessage.success(operation.success);
        operation.action(res);
      } else {
        ElMessage.error(res.message || '操作失败');
      }
    }).catch(error => {
      console.error('批量操作失败:', error);
      ElMessage.error('批量操作失败: ' + (error.message || '服务器错误'));
    });
  }).catch(() => {
    // 用户取消操作
  });
};

// 导出企业列表
const exporting = ref(false);
const exportCompanyList = () => {
  const params = {
    name: queryParams.name || undefined,
    industry: queryParams.industry || undefined,
    scale: queryParams.scale || undefined,
    status: queryParams.status || undefined,
    startTime: queryParams.dateRange && queryParams.dateRange[0] ? queryParams.dateRange[0] : undefined,
    endTime: queryParams.dateRange && queryParams.dateRange[1] ? queryParams.dateRange[1] : undefined,
    sortBy: sortBy.value,
    sortDir: sortDir.value
  };

  // 移除所有undefined的属性
  Object.keys(params).forEach(key => params[key] === undefined && delete params[key]);

  exporting.value = true;
  ElMessage.info('正在生成导出文件，请稍候...');
  
  exportCompanies(params)
    .then(data => {
      // 创建blob链接并下载
      const blob = new Blob([data], { type: 'application/vnd.ms-excel' });
      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = `企业列表_${new Date().toISOString().split('T')[0]}.xlsx`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(link.href);
      ElMessage.success('导出成功，文件下载中...');
    })
    .catch(error => {
      console.error('导出失败:', error);
      ElMessage.error('导出失败: ' + (error.message || '服务器错误'));
    })
    .finally(() => {
      exporting.value = false;
    });
};

// 初始化
onMounted(() => {
  // 尝试从本地存储获取列设置
  const savedColumns = localStorage.getItem('adminCompanyColumns');
  if (savedColumns) {
    visibleColumns.value = JSON.parse(savedColumns);
  }
  
  // 获取企业列表数据
  getCompanyList();
  
  console.log('企业管理页面已加载，正在获取数据...');
});
</script>

<style scoped>
.company-management {
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
}

.filter-card {
  margin-bottom: 10px;
}

.table-card {
  flex: 1;
}

.table-operations {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.selected-count {
  color: #606266;
  font-size: 14px;
}

.table-settings {
  display: flex;
  gap: 8px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-card__body) {
  padding: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
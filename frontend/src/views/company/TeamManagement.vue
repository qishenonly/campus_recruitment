<template>
  <div class="team-management-container">
    <el-card class="team-card">
      <template #header>
        <div class="card-header">
          <span>团队管理</span>
          <el-button type="primary" size="small" @click="handleAddMember">添加成员</el-button>
        </div>
      </template>
      
      <!-- 团队成员列表 -->
      <div class="team-list">
        <el-table
          v-loading="loading"
          :data="teamMembers"
          style="width: 100%"
          border>
          <el-table-column prop="name" label="姓名" width="120"></el-table-column>
          <el-table-column prop="position" label="职位" width="150"></el-table-column>
          <el-table-column prop="email" label="邮箱"></el-table-column>
          <el-table-column prop="phone" label="手机号" width="120"></el-table-column>
          <el-table-column prop="role" label="角色" width="120">
            <template #default="scope">
              <el-tag :type="scope.row.role === 'admin' ? 'danger' : 'primary'">
                {{ scope.row.role === 'admin' ? '管理员' : '普通成员' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button 
                size="small" 
                type="primary" 
                @click="handleEditMember(scope.row)"
                :disabled="userInfo.id !== adminId && scope.row.role === 'admin'">
                编辑
              </el-button>
              <el-button 
                size="small" 
                type="danger" 
                @click="handleDeleteMember(scope.row.id)"
                :disabled="userInfo.id !== adminId || scope.row.id === userInfo.id || scope.row.role === 'admin' && userInfo.role !== 'admin'">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange">
          </el-pagination>
        </div>
      </div>
    </el-card>

    <!-- 成员编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加团队成员' : '编辑团队成员'"
      width="500px">
      <el-form 
        :model="memberForm" 
        :rules="memberRules" 
        ref="memberFormRef"
        label-width="100px">

        <el-form-item label="姓名" prop="name">
          <el-input v-model="memberForm.name" placeholder="请输入姓名"></el-input>
        </el-form-item>

        <el-form-item label="职位" prop="position">
          <el-input v-model="memberForm.position" placeholder="请输入职位"></el-input>
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="memberForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="memberForm.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>

        <el-form-item label="角色" prop="role">
          <el-select v-model="memberForm.role" placeholder="请选择角色">
            <el-option label="管理员" value="admin"></el-option>
            <el-option label="普通成员" value="member"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item v-if="dialogType === 'add'" label="密码" prop="password">
          <el-input v-model="memberForm.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>

        <el-form-item v-if="dialogType === 'add'" label="确认密码" prop="confirmPassword">
          <el-input v-model="memberForm.confirmPassword" type="password" placeholder="请再次输入密码"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitMemberForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTeamMembers, addTeamMember, updateTeamMember, deleteTeamMember } from '@/api/teamManagement'

// 当前登录用户信息
const userInfo = computed(() => {
  return JSON.parse(localStorage.getItem('userInfo') || '{}')
})

// 公司创始人/超级管理员ID
const adminId = ref(0)

// 加载状态
const loading = ref(false)

// 团队成员列表数据
const teamMembers = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 对话框控制
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const memberFormRef = ref(null)
const memberForm = ref({
  id: null,
  name: '',
  position: '',
  email: '',
  phone: '',
  role: 'member',
  password: '',
  confirmPassword: ''
})

// 表单验证规则
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (memberForm.value.confirmPassword !== '') {
      if (!memberFormRef.value) return
      memberFormRef.value.validateField('confirmPassword', () => null)
    }
    callback()
  }
}
const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== memberForm.value.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const memberRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度应为2-20个字符', trigger: 'blur' }
  ],
  position: [
    { required: true, message: '请输入职位', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  password: [
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validatePass2, trigger: 'blur' }
  ]
}

// 获取团队成员列表
const fetchTeamMembers = async () => {
  loading.value = true
  try {
    const res = await getTeamMembers({
      page: currentPage.value,
      size: pageSize.value
    })
    if (res.code === 200) {
      teamMembers.value = res.data.records || []
      total.value = res.data.total || 0
      
      // 查找管理员ID
      const admin = teamMembers.value.find(item => item.role === 'admin')
      if (admin) {
        adminId.value = admin.id
      }
    }
  } catch (error) {
    console.error('获取团队成员失败:', error)
    ElMessage.error('获取团队成员失败')
  } finally {
    loading.value = false
  }
}

// 处理分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchTeamMembers()
}

// 处理当前页变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchTeamMembers()
}

// 添加成员按钮
const handleAddMember = () => {
  dialogType.value = 'add'
  memberForm.value = {
    id: null,
    name: '',
    position: '',
    email: '',
    phone: '',
    role: 'member',
    password: '',
    confirmPassword: ''
  }
  dialogVisible.value = true
}

// 编辑成员按钮
const handleEditMember = (row) => {
  dialogType.value = 'edit'
  memberForm.value = {
    id: row.id,
    name: row.name,
    position: row.position,
    email: row.email,
    phone: row.phone,
    role: row.role
  }
  dialogVisible.value = true
}

// 删除成员按钮
const handleDeleteMember = (id) => {
  ElMessageBox.confirm('确定要删除该团队成员吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteTeamMember(id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchTeamMembers()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除团队成员失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 提交成员表单
const submitMemberForm = async () => {
  try {
    await memberFormRef.value.validate()
    
    if (dialogType.value === 'add') {
      // 添加成员
      const res = await addTeamMember(memberForm.value)
      if (res.code === 200) {
        ElMessage.success('添加成功')
        dialogVisible.value = false
        fetchTeamMembers()
      } else {
        ElMessage.error(res.message || '添加失败')
      }
    } else {
      // 更新成员
      const res = await updateTeamMember(memberForm.value.id, memberForm.value)
      if (res.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        fetchTeamMembers()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    }
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

onMounted(() => {
  fetchTeamMembers()
})
</script>

<style scoped>
.team-management-container {
  padding: 20px;
}

.team-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 
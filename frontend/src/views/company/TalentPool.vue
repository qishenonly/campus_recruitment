<template>
  <div class="talent-pool">
    <div class="page-header">
      <h2>人才库</h2>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="filter-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索姓名/技能/专业"
        clearable
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      
      <el-select v-model="tagFilter" placeholder="标签筛选" @change="handleFilterChange">
        <el-option label="全部" value="" />
        <el-option
          v-for="tag in tagOptions"
          :key="tag"
          :label="tag"
          :value="tag"
        />
      </el-select>
    </div>

    <!-- 人才列表 -->
    <el-table
      v-loading="loading"
      :data="talentList"
      style="width: 100%"
      border
    >
      <el-table-column prop="name" label="姓名" min-width="100" />
      <el-table-column prop="education" label="学历" min-width="120">
        <template #default="scope">
          <div>{{ scope.row.education }}</div>
          <div class="text-secondary">{{ scope.row.school }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="major" label="专业" min-width="120" />
      <el-table-column prop="skills" label="技能" min-width="200">
        <template #default="scope">
          <el-tag
            v-for="skill in scope.row.skills"
            :key="skill"
            size="small"
            class="skill-tag"
          >
            {{ skill }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="tags" label="标签" min-width="150">
        <template #default="scope">
          <el-tag
            v-for="tag in scope.row.tags"
            :key="tag"
            type="success"
            size="small"
            class="tag-item"
          >
            {{ tag }}
          </el-tag>
          <el-button
            type="primary"
            link
            size="small"
            @click="handleEditTags(scope.row)"
          >
            编辑
          </el-button>
        </template>
      </el-table-column>
      <el-table-column prop="notes" label="备注" min-width="200">
        <template #default="scope">
          <div class="notes-cell">
            <span>{{ scope.row.notes || '无备注' }}</span>
            <el-button
              type="primary"
              link
              size="small"
              @click="handleEditNotes(scope.row)"
            >
              编辑
            </el-button>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="120">
        <template #default="scope">
          <el-button 
            type="primary" 
            link 
            @click="handleViewResume(scope.row)"
          >
            查看简历
          </el-button>
          <el-button 
            type="danger" 
            link 
            @click="handleRemove(scope.row)"
          >
            移除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 编辑标签对话框 -->
    <el-dialog
      v-model="tagDialogVisible"
      title="编辑标签"
      width="400px"
    >
      <div v-if="currentTalent" class="tag-edit-container">
        <el-tag
          v-for="tag in selectedTags"
          :key="tag"
          closable
          @close="handleRemoveTag(tag)"
          class="tag-item"
        >
          {{ tag }}
        </el-tag>
        
        <el-select
          v-model="newTag"
          filterable
          allow-create
          default-first-option
          placeholder="请选择或创建标签"
          @change="handleAddTag"
        >
          <el-option
            v-for="tag in availableTags"
            :key="tag"
            :label="tag"
            :value="tag"
          />
        </el-select>
      </div>
      
      <template #footer>
        <el-button @click="tagDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTags">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 编辑备注对话框 -->
    <el-dialog
      v-model="notesDialogVisible"
      title="编辑备注"
      width="500px"
    >
      <div v-if="currentTalent" class="notes-edit-container">
        <el-input
          v-model="editingNotes"
          type="textarea"
          rows="5"
          placeholder="请输入备注信息"
        />
      </div>
      
      <template #footer>
        <el-button @click="notesDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveNotes">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCompanyTalents, updateTalentTags, updateTalentNotes, deleteTalent } from '@/api/company-talents'

// 数据
const loading = ref(false)
const talentList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const tagFilter = ref('')
const tagOptions = ref(['优秀', '潜力股', '待观察', '技术强', '沟通好', '有经验'])
const tagDialogVisible = ref(false)
const notesDialogVisible = ref(false)
const currentTalent = ref(null)
const selectedTags = ref([])
const newTag = ref('')
const editingNotes = ref('')

// 计算可用标签（排除已选择的）
const availableTags = computed(() => {
  return tagOptions.value.filter(tag => !selectedTags.value.includes(tag))
})

// 获取人才列表
const fetchTalents = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value,
      tag: tagFilter.value
    }
    
    // 实际项目中应该调用API
    // const res = await getCompanyTalents(params)
    const res = {
      data: [
        {
          id: 1,
          name: '张三',
          education: '本科',
          school: '北京大学',
          major: '计算机科学与技术',
          skills: ['Java', 'Spring Boot', 'MySQL', 'Redis'],
          tags: ['技术强', '沟通好'],
          notes: '有3年Java开发经验，技术基础扎实，沟通能力强'
        },
        {
          id: 2,
          name: '李四',
          education: '硕士',
          school: '清华大学',
          major: '软件工程',
          skills: ['Python', 'Django', 'Vue', 'Docker'],
          tags: ['优秀', '潜力股'],
          notes: '有全栈开发能力，学习能力强'
        },
        {
          id: 3,
          name: '王五',
          education: '本科',
          school: '浙江大学',
          major: '信息管理与信息系统',
          skills: ['产品设计', 'Axure', 'Sketch', '用户研究'],
          tags: ['待观察'],
          notes: '有2年产品经理经验，对用户体验比较关注'
        }
      ],
      total: 3
    }
    
    talentList.value = res.data
    total.value = res.total
  } catch (error) {
    console.error('获取人才列表失败:', error)
    ElMessage.error('获取人才列表失败')
  } finally {
    loading.value = false
  }
}

// 处理筛选条件变化
const handleFilterChange = () => {
  currentPage.value = 1
  fetchTalents()
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchTalents()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchTalents()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchTalents()
}

// 编辑标签
const handleEditTags = (row) => {
  currentTalent.value = row
  selectedTags.value = [...row.tags]
  tagDialogVisible.value = true
}

// 编辑备注
const handleEditNotes = (row) => {
  currentTalent.value = row
  editingNotes.value = row.notes || ''
  notesDialogVisible.value = true
}

// 查看简历
const handleViewResume = (row) => {
  ElMessage.info('查看简历功能开发中')
}

// 移除人才
const handleRemove = (row) => {
  ElMessageBox.confirm('确认从人才库中移除此人才吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 实际项目中应该调用API
      // await deleteTalent(row.id)
      ElMessage.success('移除成功')
      fetchTalents()
    } catch (error) {
      console.error('移除人才失败:', error)
      ElMessage.error('移除失败')
    }
  }).catch(() => {})
}

// 添加标签
const handleAddTag = (tag) => {
  if (tag && !selectedTags.value.includes(tag)) {
    selectedTags.value.push(tag)
  }
  newTag.value = ''
}

// 移除标签
const handleRemoveTag = (tag) => {
  selectedTags.value = selectedTags.value.filter(t => t !== tag)
}

// 保存标签
const saveTags = async () => {
  if (!currentTalent.value) return
  
  try {
    // 实际项目中应该调用API
    // await updateTalentTags(currentTalent.value.id, selectedTags.value)
    currentTalent.value.tags = [...selectedTags.value]
    ElMessage.success('标签更新成功')
    tagDialogVisible.value = false
  } catch (error) {
    console.error('更新标签失败:', error)
    ElMessage.error('更新标签失败')
  }
}

// 保存备注
const saveNotes = async () => {
  if (!currentTalent.value) return
  
  try {
    // 实际项目中应该调用API
    // await updateTalentNotes(currentTalent.value.id, editingNotes.value)
    currentTalent.value.notes = editingNotes.value
    ElMessage.success('备注更新成功')
    notesDialogVisible.value = false
  } catch (error) {
    console.error('更新备注失败:', error)
    ElMessage.error('更新备注失败')
  }
}

onMounted(() => {
  fetchTalents()
})
</script>

<style scoped>
.talent-pool {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}

.filter-section {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.filter-section .el-input {
  width: 300px;
}

.filter-section .el-select {
  width: 150px;
}

.skill-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

.tag-item {
  margin-right: 5px;
  margin-bottom: 5px;
}

.notes-cell {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.text-secondary {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

.tag-edit-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.tag-edit-container .el-select {
  margin-top: 8px;
  width: 100%;
}

@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
    gap: 12px;
  }
  
  .filter-section .el-input,
  .filter-section .el-select {
    width: 100%;
  }
}
</style> 
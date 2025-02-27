<template>
  <div class="message-page">
    <!-- 消息类型切换 -->
    <div class="message-tabs">
      <div 
        v-for="tab in tabs" 
        :key="tab.type"
        :class="['tab-item', { active: currentTab === tab.type }]"
        @click="handleTabChange(tab.type)"
      >
        <span class="tab-name">{{ tab.name }}</span>
        <span v-if="tab.unread" class="unread-badge">{{ tab.unread }}</span>
      </div>
    </div>

    <!-- 消息列表 -->
    <div class="message-list">
      <template v-if="messages.length">
        <div 
          v-for="message in messages" 
          :key="message.id" 
          :class="['message-item', { unread: !message.isRead }]"
          @click="handleMessageClick(message)"
        >
          <div class="message-icon">
            <van-icon :name="getMessageIcon(message.type)" :color="getMessageColor(message.type)" size="24" />
          </div>
          <div class="message-content">
            <div class="message-header">
              <span class="message-title">{{ message.title }}</span>
              <span class="message-time">{{ formatTime(message.createTime) }}</span>
            </div>
            <div class="message-body">{{ message.content }}</div>
            <div v-if="message.type === 'job'" class="message-actions">
              <van-button size="small" type="primary" @click.stop="viewJobDetail(message.jobId)">
                查看职位
              </van-button>
            </div>
          </div>
        </div>
      </template>
      
      <!-- 空状态 -->
      <div v-else class="empty-state">
        <van-empty description="暂无消息" />
      </div>
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore" class="load-more">
      <van-button plain block @click="loadMore">加载更多</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { formatDistance } from 'date-fns'
import { zhCN } from 'date-fns/locale'

const router = useRouter()
const currentTab = ref('all')
const messages = ref([])
const hasMore = ref(true)
const page = ref(1)
const pageSize = ref(10)

// 消息类型标签
const tabs = [
  { name: '全部消息', type: 'all', unread: 5 },
  { name: '系统通知', type: 'system', unread: 2 },
  { name: '投递反馈', type: 'job', unread: 3 },
]

// 切换消息类型
const handleTabChange = (type) => {
  currentTab.value = type
  page.value = 1
  fetchMessages()
}

// 获取消息图标
const getMessageIcon = (type) => {
  switch (type) {
    case 'system':
      return 'info-o'
    case 'job':
      return 'certificate'
    default:
      return 'bell'
  }
}

// 获取消息图标颜色
const getMessageColor = (type) => {
  switch (type) {
    case 'system':
      return '#1890ff'
    case 'job':
      return '#52c41a'
    default:
      return '#666'
  }
}

// 格式化时间
const formatTime = (time) => {
  return formatDistance(new Date(time), new Date(), {
    addSuffix: true,
    locale: zhCN
  })
}

// 处理消息点击
const handleMessageClick = (message) => {
  if (!message.isRead) {
    markAsRead(message.id)
  }
}

// 查看职位详情
const viewJobDetail = (jobId) => {
  router.push(`/job/${jobId}`)
}

// 标记消息已读
const markAsRead = async (messageId) => {
  try {
    // TODO: 调用标记已读 API
    const index = messages.value.findIndex(msg => msg.id === messageId)
    if (index !== -1) {
      messages.value[index].isRead = true
    }
  } catch (error) {
    console.error('标记消息已读失败:', error)
  }
}

// 获取消息列表
const fetchMessages = async () => {
  try {
    // TODO: 替换为实际的 API 调用
    const mockMessages = [
      {
        id: 1,
        type: 'system',
        title: '简历被查看通知',
        content: '您的简历已被XX公司查看',
        createTime: '2024-01-20T10:00:00',
        isRead: false
      },
      {
        id: 2,
        type: 'job',
        title: '面试通知',
        content: '恭喜您通过简历筛选，请准时参加面试',
        createTime: '2024-01-19T15:30:00',
        isRead: true,
        jobId: 123
      }
    ]
    
    if (page.value === 1) {
      messages.value = mockMessages
    } else {
      messages.value.push(...mockMessages)
    }
    
    hasMore.value = mockMessages.length === pageSize.value
  } catch (error) {
    console.error('获取消息列表失败:', error)
  }
}

// 加载更多
const loadMore = () => {
  page.value += 1
  fetchMessages()
}

onMounted(() => {
  fetchMessages()
})
</script>

<style scoped>
.message-page {
  background: white;
  border-radius: 8px;
  padding: 20px;
  min-height: 600px;
}

.message-tabs {
  display: flex;
  gap: 30px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
  padding-bottom: 15px;
}

.tab-item {
  position: relative;
  cursor: pointer;
  padding: 8px 0;
  font-size: 16px;
  color: #666;
  transition: all 0.3s;
}

.tab-item::after {
  content: '';
  position: absolute;
  bottom: -16px;
  left: 0;
  width: 100%;
  height: 2px;
  background: var(--primary-color);
  transform: scaleX(0);
  transition: transform 0.3s;
}

.tab-item:hover {
  color: var(--primary-color);
}

.tab-item.active {
  color: var(--primary-color);
  font-weight: 500;
}

.tab-item.active::after {
  transform: scaleX(1);
}

.unread-badge {
  position: absolute;
  top: 0;
  right: -12px;
  background: #ff4d4f;
  color: white;
  font-size: 12px;
  padding: 0 6px;
  border-radius: 10px;
  transform: translateY(-50%);
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  gap: 16px;
  padding: 20px;
  border-radius: 8px;
  background: #fafafa;
  cursor: pointer;
  transition: all 0.3s;
}

.message-item:hover {
  background: #f0f0f0;
}

.message-item.unread {
  background: #e6f7ff;
}

.message-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 20px;
  background: white;
}

.message-content {
  flex: 1;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.message-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.message-time {
  font-size: 14px;
  color: #999;
}

.message-body {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  line-height: 1.5;
}

.message-actions {
  display: flex;
  gap: 12px;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.load-more {
  margin-top: 20px;
  text-align: center;
}

@media (max-width: 768px) {
  .message-tabs {
    overflow-x: auto;
    padding-bottom: 12px;
  }

  .message-item {
    padding: 15px;
  }

  .message-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style> 
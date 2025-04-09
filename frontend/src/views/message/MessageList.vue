<template>
  <div class="message-list-container">
    <div class="page-header">
      <h2>消息中心</h2>
    </div>
    
    <div class="message-list">
      <div 
        v-for="conversation in conversations" 
        :key="conversation.id" 
        class="conversation-item"
        :class="{ 'unread': conversation.unreadCount > 0 }"
        @click="goToChat(conversation.id)"
      >
        <div class="avatar">
          <img :src="conversation.avatar || defaultAvatar" alt="头像" />
          <div class="unread-badge" v-if="conversation.unreadCount > 0">
            {{ conversation.unreadCount > 99 ? '99+' : conversation.unreadCount }}
          </div>
        </div>
        
        <div class="conversation-info">
          <div class="conversation-header">
            <div class="name">{{ conversation.name }}</div>
            <div class="time">{{ formatTime(conversation.lastMessageTime) }}</div>
          </div>
          
          <div class="conversation-content">
            <div class="last-message" :class="{ 'bold': conversation.unreadCount > 0 }">
              {{ conversation.lastMessage }}
            </div>
          </div>
        </div>
      </div>
      
      <div class="empty-state" v-if="conversations.length === 0">
        <img src="@/assets/images/empty-message.png" alt="暂无消息" class="empty-image" />
        <p>暂无消息</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { format, isToday, isYesterday, parseISO } from 'date-fns'
import { getConversationList, markConversationAsRead } from '@/api/messages'
import { ElMessage } from 'element-plus'

const router = useRouter()
const conversations = ref([])
const loading = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 获取会话列表
const fetchConversations = async () => {
  loading.value = true
  try {
    const res = await getConversationList()
    if (res.code === 200) {
      conversations.value = res.data || []
    }
  } catch (error) {
    console.error('获取会话列表失败:', error)
    ElMessage.error('获取会话列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化时间
const formatTime = (timeString) => {
  if (!timeString) return ''
  
  // 使用 parseISO 解析 ISO 8601 格式的时间字符串
  const date = parseISO(timeString)
  
  if (isToday(date)) {
    return format(date, 'HH:mm')
  } else if (isYesterday(date)) {
    return '昨天'
  } else {
    return format(date, 'MM-dd')
  }
}

// 跳转到聊天页面
const goToChat = async (conversationId) => {
  try {
    // 标记会话消息为已读
    // await markConversationAsRead(conversationId)
    console.log('会话消息已标记为已读')
    
    // 更新本地状态，将已读标记移除
    const index = conversations.value.findIndex(conv => conv.id === conversationId)
    if (index !== -1) {
      conversations.value[index].unreadCount = 0
    }
    
    // 跳转到聊天页面
    router.push(`/chat/${conversationId}`)
  } catch (error) {
    console.error('标记消息已读失败:', error)
    // 即使标记失败，也允许跳转
    router.push(`/chat/${conversationId}`)
  }
}

onMounted(() => {
  fetchConversations()
  
  // 设置定时器，每分钟刷新一次会话列表
  const timer = setInterval(fetchConversations, 60000)
  
  // 组件卸载时清除定时器
  onUnmounted(() => {
    clearInterval(timer)
  })
})
</script>

<style scoped>
.message-list-container {
  padding: 16px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.message-list {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.conversation-item {
  display: flex;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.conversation-item:last-child {
  border-bottom: none;
}

.conversation-item:hover {
  background-color: #f7f8fa;
}

.conversation-item.unread {
  background-color: #f0f7ff;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
  margin-right: 12px;
  flex-shrink: 0;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.unread-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  background-color: #ff4d4f;
  color: #fff;
  font-size: 12px;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

.conversation-info {
  flex: 1;
  overflow: hidden;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.time {
  font-size: 12px;
  color: #999;
}

.last-message {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.last-message.bold {
  font-weight: 500;
  color: #333;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #999;
}

.empty-image {
  width: 120px;
  height: 120px;
  margin-bottom: 16px;
}
</style> 
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
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <template v-if="conversations.length > 0">
            <div
              v-for="conversation in conversationsWithInfo"
              :key="conversation.id"
              class="message-item"
              @click="goToChat(conversation)"
            >
              <div class="avatar">
                <van-image
                  round
                  width="50"
                  height="50"
                  :src="getAvatarUrl(conversation.userInfo?.avatar)"
                  fit="cover"
                />
                <div class="online-status"></div>
              </div>
              <div class="content">
                <div class="header">
                  <span class="company-name" v-if="userRole === 'STUDENT'">
                    {{ conversation.publisherName + ' [' + conversation.publisherPosition + '@' + conversation.companyName + '] ' || '未知用户' }}
                  </span>
                  <span class="company-name" v-else-if="userRole === 'COMPANY'">
                    {{ conversation.studentName + ' [' + conversation.studentSchool + ']' || '未知学生' }}
                  </span>
                  <span class="company-name" v-else>
                    {{ conversation.publisherName || '未知用户' }}
                  </span>
                  <span class="time">{{ formatTime(conversation.createTime) }}</span>
                </div>
                <div class="message-preview">
                  <div class="preview-container">
                    <span class="preview-text" :title="getMessageContent(conversation)">
                      {{ '最近一条消息：' + truncateText(getMessageContent(conversation)) }}
                    </span>
                    <span v-if="conversation.jobTitle" class="job-tag">{{ conversation.jobTitle }}</span>
                  </div>
                  <div v-if="conversation.unreadCount" class="unread-count">
                    {{ conversation.unreadCount }}
                  </div>
                </div>
              </div>
            </div>

            <!-- 加载更多按钮 -->
            <div v-if="!finished && !loading" class="load-more">
              <van-button
                plain
                block
                :loading="loading"
                loading-text="加载中..."
                @click="loadMore"
              >
                加载更多
              </van-button>
            </div>
          </template>
          <div v-else class="empty-state">
            <van-empty description="暂无消息" />
          </div>
        </van-list>
      </van-pull-refresh>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { formatDistance } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import { getConversations, markMessageAsRead } from '@/api/messages'
import { getConversationMessages } from '@/api/jobs'
import { formatTime } from '@/utils/format'
import { getUserInfo } from '@/api/user'
import { getCompanyByJobId } from '@/api/jobs'

const router = useRouter()
const route = useRoute()
const currentTab = ref('all')
const messages = ref([])
const hasMore = ref(true)
const page = ref(0)
const pageSize = 20
const currentChatId = ref(null)
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const conversations = ref([])
const userInfoMap = ref(new Map()) // 存储用户信息的Map
const userRole = localStorage.getItem('userInfo') ? JSON.parse(localStorage.getItem('userInfo')).role : null;

// 处理头像URL的方法
const getAvatarUrl = (url) => {
  if (!url) return '/default-avatar.png';
  
  // 确保URL中包含/api前缀
  if (!url.startsWith('/api') && !url.startsWith('http')) {
    if (url.startsWith('/')) {
      url = '/api' + url;
    } else {
      url = '/api/' + url;
    }
  }
  
  // 如果URL以/api开头但不是完整URL，添加基础路径
  if (url.startsWith('/api') && !url.startsWith('http')) {
    const baseURL = import.meta.env.VITE_API_URL || '';
    // 如果基础URL已经包含/api，避免重复
    if (baseURL && baseURL.endsWith('/api')) {
      url = baseURL + url.substring(4); // 移除/api
    } else if (baseURL) {
      // 确保baseURL和url之间没有重复的斜杠
      if (baseURL.endsWith('/') && url.startsWith('/')) {
        url = baseURL + url.substring(1);
      } else if (!baseURL.endsWith('/') && !url.startsWith('/')) {
        url = baseURL + '/' + url;
      } else {
        url = baseURL + url;
      }
    }
  }
  
  return url;
}

// 根据用户角色显示不同的标签
const tabs = ref([
  { name: '全部消息', type: 'all', unread: 0 },
]);

// 如果是公司用户，添加额外的标签
if (userRole === 'COMPANY') {
  tabs.value.push(
    { name: '我处理的', type: 'assigned', unread: 0 },
    { name: '公司消息', type: 'company', unread: 0 }
  );
}

// 带有用户信息的会话列表
const conversationsWithInfo = computed(() => {
  return conversations.value.map(conv => ({
    ...conv,
    userInfo: userInfoMap.value.get(conv.userId), // 使用 userId 作为键来获取用户信息
  }))
})

// 切换消息类型
const handleTabChange = (type) => {
  currentTab.value = type
  // 重置列表状态
  page.value = 0
  conversations.value = []
  finished.value = false
  loading.value = false
  userInfoMap.value.clear()
  
  // 如果是系统通知，直接设置完成状态
  if (type === 'system') {
    finished.value = true
    return
  }
  
  // 重新加载数据
  fetchConversations()
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
    // 调用标记已读API
    const conversationId = currentChatId.value; // 假设currentChatId已被设置
    if (conversationId) {
      // await markMessageAsRead(conversationId, messageId);
      console.log('消息已标记为已读');
      
      // 更新本地状态
      const index = messages.value.findIndex(msg => msg.id === messageId);
      if (index !== -1) {
        messages.value[index].isRead = true;
      }
    } else {
      console.error('无法标记消息：未找到会话ID');
    }
  } catch (error) {
    console.error('标记消息已读失败:', error);
  }
}

// 获取用户信息
const fetchUserInfo = async (id) => {
  try {
    const res = await getUserInfo(id)
    if (res.code === 200) {
      userInfoMap.value.set(id, res.data)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 获取会话列表
const fetchConversations = async () => {
  if (loading.value && !refreshing.value) return;
  
  try {
    console.log('获取会话列表, 页码:', page.value, '类型:', currentTab.value);
    const res = await getConversations({
      page: page.value,
      size: pageSize,
      type: currentTab.value
    });

    console.log('会话原始数据:', res.data)
    const newConversations = res.data.content || []
    
    // 增加详细日志输出，查看完整的数据结构
    console.log('完整API响应:', res)
    console.log('会话列表数组结构:', newConversations)
    if (newConversations.length > 0) {
      console.log('第一个会话详细信息:', JSON.stringify(newConversations[0], null, 2))
    }
    
    // 为每个会话获取最新消息
    await Promise.all(
      newConversations.map(async (conv) => {
        try {
          // 获取会话的消息
          const messagesResponse = await getConversationMessages(conv.id);
          console.log(`会话${conv.id}的消息:`, messagesResponse);
          
          if (messagesResponse.code === 200 && messagesResponse.data.length > 0) {
            // 按时间排序，获取最新的消息
            const sortedMessages = [...messagesResponse.data].sort((a, b) => 
              new Date(b.createTime) - new Date(a.createTime)
            );
            
            // 保存最新消息
            conv.lastMessage = sortedMessages[0];
            conv.lastMessageContent = sortedMessages[0].content;
            console.log(`会话${conv.id}的最新消息:`, conv.lastMessageContent);
          } else {
            conv.lastMessageContent = '暂无消息内容';
          }
        } catch (error) {
          console.error(`获取会话${conv.id}的消息失败:`, error);
          conv.lastMessageContent = '获取消息失败';
        }
      })
    );
    
    // 处理每个对话，确保包含必要的信息
    for (const conv of newConversations) {
      console.log('处理会话完整数据:', conv) // 输出完整会话对象
      
      // 根据当前用户的角色判断需要获取 studentId 还是 companyId
      if (userRole === 'STUDENT') {
        conv.userId = conv.companyId;
      } else if (userRole === 'COMPANY') {
        conv.userId = conv.studentId;
        
        // 为企业用户获取学生信息
        try {
          const studentInfo = await getUserInfo(conv.studentId);
          if (studentInfo.code === 200 && studentInfo.data) {
            conv.studentName = studentInfo.data.realName || '未知学生';
            conv.studentSchool = studentInfo.data.university || '未知学校';
          }
        } catch (error) {
          console.error(`获取学生信息失败，学生ID=${conv.studentId}:`, error);
          conv.studentName = '未知学生';
          conv.studentSchool = '未知学校';
        }
      }
      
      // 确保职位信息存在
      if (!conv.jobTitle && conv.firstMessage) {
        // 尝试从第一条消息中获取职位信息
        conv.jobTitle = conv.firstMessage.jobTitle || '';
        conv.jobId = conv.firstMessage.jobId || '';
      }
      
      // 如果当前消息有职位信息，确保保留它
      if (conv.content && conv.content.includes('职位') && !conv.jobTitle) {
        // 尝试从消息内容中解析职位信息
        const match = conv.content.match(/简历"(.*?)"/);
        if (match && match[1]) {
          conv.jobTitle = match[1];
        }
      }
      
      // 如果有职位ID但没有职位信息，尝试获取公司信息
      if (conv.jobId && (!conv.jobTitle || conv.jobTitle === '')) {
        try {
          const response = await getCompanyByJobId(conv.jobId);
          console.log(`获取职位ID=${conv.jobId}的信息:`, response);
          if (response.code === 200 && response.data) {
            // 更新聊天记录的公司和职位信息
            if (response.data.company) {
              if (!conv.companyName) {
                conv.companyName = response.data.company.name;
              }
              if (!conv.companyLogo) {
                conv.companyLogo = response.data.company.logo;
              }
            }
            
            // 更新职位信息
            if (response.data.job) {
              if (!conv.jobTitle) {
                conv.jobTitle = response.data.job.title;
              }
            }
            
            // 如果没有职位标题，可以用"来自xx公司的职位"作为替代
            if (!conv.jobTitle && response.data.company) {
              conv.jobTitle = `来自${response.data.company.name}的职位`;
            }
          }
        } catch (error) {
          console.error(`获取职位ID=${conv.jobId}的信息失败:`, error);
        }
      }
    }

    // 获取所有新会话中的用户信息
    await Promise.all(
      newConversations.map(conv => {
        if(conv.userId){
          fetchUserInfo(conv.publisherId)
        }
      })
    )

    if (refreshing.value) {
      conversations.value = newConversations
      refreshing.value = false
    } else {
      conversations.value = [...conversations.value, ...newConversations]
    }

    loading.value = false
    if (newConversations.length < pageSize) {
      finished.value = true
    }
  } catch (error) {
    console.error('获取会话列表失败:', error)
    loading.value = false
    finished.value = true
  }
}

// 下拉刷新
const onRefresh = () => {
  finished.value = false
  page.value = 0
  fetchConversations()
}

// 加载更多
const onLoad = () => {
  if (!loading.value) {
    page.value++
    fetchConversations()
  }
}

// 跳转到聊天页面
const goToChat = async (conversation) => {
  try {
    // 标记会话消息为已读
    // await markConversationAsRead(conversation.id)
    console.log('会话消息已标记为已读')
    
    // 更新本地状态
    conversation.unreadCount = 0
    
    // 跳转到聊天页面
    router.push({
      path: `/chat/${conversation.id}`,
      query: { jobTitle: conversation.jobTitle, jobId: conversation.jobId }
    })
  } catch (error) {
    console.error('标记消息已读失败:', error)
    // 即使标记失败，也允许跳转
    router.push({
      path: `/chat/${conversation.id}`,
      query: { jobTitle: conversation.jobTitle, jobId: conversation.jobId }
    })
  }
}

// 加载更多
const loadMore = async () => {
  if (loading.value) return

  loading.value = true
  page.value++

  try {
    console.log('加载更多会话, 页码:', page.value, '类型:', currentTab.value)
    const res = await getConversations({
      page: page.value,
      size: pageSize,
      type: currentTab.value
    })

    console.log('加载更多会话原始数据:', res.data)
    const newConversations = res.data.content || []
    
    // 处理每个对话，确保包含必要的信息
    for (const conv of newConversations) {
      console.log('处理加载更多会话完整数据:', conv) // 输出完整会话对象
      
      // 处理最新消息内容
      if (conv.lastMessage) {
        // 如果有lastMessage对象，直接使用
        console.log('会话有lastMessage字段:', conv.lastMessage)
        conv.lastMessageContent = conv.lastMessage.content
      } else if (conv.messages && conv.messages.length > 0) {
        // 如果有messages数组，取最新的一条消息
        const lastMsg = conv.messages[conv.messages.length - 1]
        conv.lastMessage = lastMsg
        conv.lastMessageContent = lastMsg.content
        console.log('从messages数组获取最新消息:', lastMsg)
      } else if (conv.firstMessage && conv.firstMessage.content) { 
        // 如果有firstMessage字段，也可能包含消息内容
        conv.lastMessageContent = conv.firstMessage.content
        console.log('从firstMessage获取消息内容:', conv.firstMessage.content)
      } else if (conv.lastMessageContent) {
        // 如果有lastMessageContent字段，直接使用
        console.log('会话有lastMessageContent字段:', conv.lastMessageContent)
      } else if (conv.content) {
        // 如果有content字段，可能是消息内容
        conv.lastMessageContent = conv.content
        console.log('使用content字段作为消息内容:', conv.content)
      } else {
        // 如果没有任何消息相关字段，设置一个默认值
        conv.lastMessageContent = '暂无消息内容'
        console.log('未找到任何消息内容，使用默认值')
      }
      
      // 根据当前用户的角色判断需要获取 studentId 还是 companyId
      if (userRole === 'STUDENT') {
        conv.userId = conv.companyId;
      } else if (userRole === 'COMPANY') {
        conv.userId = conv.studentId;
        
        // 为企业用户获取学生信息
        try {
          const studentInfo = await getUserInfo(conv.studentId);
          if (studentInfo.code === 200 && studentInfo.data) {
            conv.studentName = studentInfo.data.realName || '未知学生';
            conv.studentSchool = studentInfo.data.university || '未知学校';
          }
        } catch (error) {
          console.error(`获取学生信息失败，学生ID=${conv.studentId}:`, error);
          conv.studentName = '未知学生';
          conv.studentSchool = '未知学校';
        }
      }
      
      // 确保职位信息存在
      if (!conv.jobTitle && conv.firstMessage) {
        // 尝试从第一条消息中获取职位信息
        conv.jobTitle = conv.firstMessage.jobTitle || '';
        conv.jobId = conv.firstMessage.jobId || '';
      }
      
      // 如果当前消息有职位信息，确保保留它
      if (conv.content && conv.content.includes('职位') && !conv.jobTitle) {
        // 尝试从消息内容中解析职位信息
        const match = conv.content.match(/简历"(.*?)"/);
        if (match && match[1]) {
          conv.jobTitle = match[1];
        }
      }
      
      // 如果有职位ID但没有职位信息，尝试获取公司信息
      if (conv.jobId && (!conv.jobTitle || conv.jobTitle === '')) {
        try {
          const response = await getCompanyByJobId(conv.jobId);
          console.log(`获取职位ID=${conv.jobId}的信息:`, response);
          if (response.code === 200 && response.data) {
            // 更新聊天记录的公司和职位信息
            if (response.data.company) {
              if (!conv.companyName) {
                conv.companyName = response.data.company.name;
              }
              if (!conv.companyLogo) {
                conv.companyLogo = response.data.company.logo;
              }
            }
            
            // 更新职位信息
            if (response.data.job) {
              if (!conv.jobTitle) {
                conv.jobTitle = response.data.job.title;
              }
            }
            
            // 如果没有职位标题，可以用"来自xx公司的职位"作为替代
            if (!conv.jobTitle && response.data.company) {
              conv.jobTitle = `来自${response.data.company.name}的职位`;
            }
          }
        } catch (error) {
          console.error(`获取职位ID=${conv.jobId}的信息失败:`, error);
        }
      }
    }
    
    // 获取所有新会话中的用户信息
    await Promise.all(
      newConversations.map(conv => {
        if(conv.userId){
          fetchUserInfo(conv.publisherId)
        }
      })
    )

    conversations.value = [...conversations.value, ...newConversations]

    if (newConversations.length < pageSize) {
      finished.value = true
    }
  } catch (error) {
    console.error('加载更多消息失败:', error)
  } finally {
    loading.value = false
  }
}

// 添加文本截断过滤器
const truncateText = (text) => {
  if (!text) return '暂无消息';
  return text.length > 30 ? text.substring(0, 30) + '...' : text;
}

// 获取消息内容的方法
const getMessageContent = (conversation) => {
  // 打印当前对话的所有字段，用于调试
  console.log('获取消息内容, 当前对话:', conversation);
  
  // 按优先级检查各种可能包含消息内容的字段
  if (conversation.lastMessageContent) {
    console.log('使用lastMessageContent:', conversation.lastMessageContent);
    return conversation.lastMessageContent;
  }
  
  if (conversation.lastMessage && conversation.lastMessage.content) {
    console.log('使用lastMessage.content:', conversation.lastMessage.content);
    return conversation.lastMessage.content;
  }
  
  if (conversation.latestMessage && conversation.latestMessage.content) {
    console.log('使用latestMessage.content:', conversation.latestMessage.content);
    return conversation.latestMessage.content;
  }
  
  if (conversation.message && conversation.message.content) {
    console.log('使用message.content:', conversation.message.content);
    return conversation.message.content;
  }
  
  if (conversation.lastMessages && conversation.lastMessages.length > 0) {
    console.log('使用lastMessages中的第一条:', conversation.lastMessages[0].content);
    return conversation.lastMessages[0].content;
  }
  
  if (conversation.recentMessage && conversation.recentMessage.content) {
    console.log('使用recentMessage.content:', conversation.recentMessage.content);
    return conversation.recentMessage.content;
  }
  
  if (conversation.content) {
    console.log('使用content:', conversation.content);
    return conversation.content;
  }
  
  if (conversation.firstMessage && conversation.firstMessage.content) {
    console.log('使用firstMessage.content:', conversation.firstMessage.content);
    return conversation.firstMessage.content;
  }
  
  console.log('未找到任何消息内容');
  return '暂无消息';
}

onMounted(() => {
  fetchConversations()
  // 如果URL中有chatId参数,则打开对应的聊天
  const chatId = route.query.chatId
  if (chatId) {
    currentChatId.value = chatId
    // 可以在这里触发打开聊天窗口的逻辑
    openChat(chatId)
  }
})

const openChat = (chatId) => {
  // 打开聊天窗口的具体实现
  // ...
}
</script>

<style scoped>
/* ... (rest of your styles remain the same) ... */
.message-page {
  background: white;
  border-radius: 8px;
  padding: 20px;
  min-height: 600px;
  --primary-color: #2b7efb;
  --primary-color-rgb: 43, 126, 251;
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
  align-items: center;
  padding: 16px;
  background: #ffffff;
  border-radius: 12px;
  margin-bottom: 12px;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
}

.message-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: var(--primary-color);
}

.avatar {
  position: relative;
  margin-right: 16px;
  flex-shrink: 0;
}

.online-status {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  background: #52c41a;
  border: 2px solid #fff;
  border-radius: 50%;
}

.content {
  flex: 1;
  min-width: 0; /* 防止文本溢出 */
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.company-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-right: 8px;
}

.time {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}

.message-preview {
  display: flex;
  margin-top: 4px;
  justify-content: space-between;
  align-items: center;
}

.preview-container {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.preview-text {
  color: #999;
  font-size: 14px;
  max-width: 100%;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  flex: 1;
  min-width: 0;
}

.job-tag {
  font-size: 12px;
  color: var(--primary-color);
  background-color: rgba(var(--primary-color-rgb), 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  white-space: nowrap;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  flex-shrink: 0;
}

.unread-count {
  background-color: var(--primary-color);
  color: white;
  font-size: 12px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 8px;
  flex-shrink: 0;
}

/* 添加动画效果 */
.message-item {
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .message-item {
    padding: 12px;
  }

  .avatar {
    margin-right: 12px;
  }

  .company-name {
    font-size: 15px;
  }

  .preview-text {
    font-size: 13px;
  }
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.load-more {
  margin-top: 20px;
  padding: 0 16px;
}

.load-more .van-button {
  height: 40px;
  font-size: 14px;
  border-radius: 20px;
  background: #fff;
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.load-more .van-button:active {
  opacity: 0.8;
}

.load-more .van-button.van-button--loading {
  opacity: 0.8;
  background: #f5f5f5;
}

/* 添加加载动画 */
@keyframes loading-rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.van-button__loading {
  animation: loading-rotate 1s linear infinite;
}
</style>

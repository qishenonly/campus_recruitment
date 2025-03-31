<template>
    <div class="chat-container">
      <!-- 头部 -->
      <div class="chat-header">
        <div class="header-left">
          <div class="back-button" @click="goBack">
            <i class="el-icon-arrow-left"></i>
          </div>
          <div class="avatar">
            <img :src="getOtherUserAvatar()" alt="头像" />
          </div>
          <div class="header-info">
            <div class="chat-name">{{ chatInfo.name || '聊天' }}</div>
            <div class="chat-status">{{ chatInfo.online ? '在线' : '离线' }}</div>
          </div>
        </div>
        <div class="header-right">
          <i class="el-icon-more"></i>
        </div>
      </div>
      
      <!-- 消息列表 -->
      <div class="message-list" ref="messageListRef">
        <div v-for="(message, index) in messages" :key="message.id" class="message-wrapper">
          <!-- 日期分割线 -->
          <div class="message-date-divider" v-if="shouldShowDateDivider(message, index)">
            {{ formatDate(message.createdAt) }}
          </div>
          
          <!-- 消息气泡 -->
          <div :class="['message-item', isSelfMessage(message) ? 'message-self' : 'message-other']">
            <!-- 对方头像 (左侧) -->
            <div class="avatar" v-if="!isSelfMessage(message)">
              <img :src="getOtherUserAvatar()" alt="头像" />
            </div>
            
            <div class="message-content">
              <!-- 发送者名称 -->
              <div class="sender-name" v-if="!isSelfMessage(message) && isGroupChat">
                {{ message.senderName }}
              </div>
              
              <!-- 消息气泡 -->
              <div class="message-bubble">
                <div class="message-text">{{ message.content }}</div>
              </div>
              
              <!-- 消息时间 - 确保每条消息都显示时间 -->
              <div class="message-time">{{ formatMessageTime(message.createdAt) }}</div>
            </div>
            
            <!-- 自己的头像 (右侧) -->
            <div class="avatar self-avatar" v-if="isSelfMessage(message)">
              <img :src="getSelfAvatar()" alt="头像" />
            </div>
          </div>
        </div>
      </div>
      
      <!-- 快捷语句 -->
      <div class="quick-phrases" v-if="showQuickPhrases">
        <div 
          v-for="(phrase, index) in quickPhrases" 
          :key="index" 
          class="phrase-item"
          @click="selectQuickPhrase(phrase)"
        >
          {{ phrase }}
        </div>
      </div>
      
      <!-- 输入区域 -->
      <div class="message-input-container">
        <div class="toolbar">
          <!-- 修复图标显示问题 -->
          <div class="tool-icon" @click="toggleQuickPhrases">
            <svg class="icon" aria-hidden="true">
              <use xlink:href="#icon-chat"></use>
            </svg>
            <span>快捷语</span>
          </div>
          <div class="tool-icon">
            <svg class="icon" aria-hidden="true">
              <use xlink:href="#icon-emoji"></use>
            </svg>
            <span>表情</span>
          </div>
          <div class="tool-icon">
            <svg class="icon" aria-hidden="true">
              <use xlink:href="#icon-image"></use>
            </svg>
            <span>图片</span>
          </div>
          <div class="tool-icon">
            <svg class="icon" aria-hidden="true">
              <use xlink:href="#icon-file"></use>
            </svg>
            <span>文件</span>
          </div>
        </div>
        
        <div class="input-area">
          <textarea 
            v-model="messageText" 
            placeholder="请输入消息..." 
            class="message-textarea"
            @keydown.enter.prevent="handleEnterPress"
          ></textarea>
          
          <div class="send-button" :class="{ 'active': messageText.trim() }" @click="sendMessage">
            发送
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, nextTick, computed } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { format, isToday, isYesterday, isSameDay } from 'date-fns'
  import { getConversationMessages } from '@/api/jobs'
  import { sendMessageAPI } from '@/api/messages'
  import { ElMessage } from 'element-plus'
  
  const route = useRoute()
  const router = useRouter()
  const chatId = route.params.chatId
  const messages = ref([])
  const messageText = ref('')
  const messageListRef = ref(null)
  const chatInfo = ref({})
  const isGroupChat = ref(false)
  const showQuickPhrases = ref(false)
  
  // 快捷语句
  const quickPhrases = ref([
    '您好，很高兴认识您！',
    '请问您对这个职位还有兴趣吗？',
    '我们可以约个时间详细聊聊',
    '感谢您的回复',
    '您的简历我们已经收到了',
    '请问您方便什么时候面试？',
    '祝您工作顺利！'
  ])
  
  // 获取当前用户信息
  const userInfo = computed(() => {
    return JSON.parse(localStorage.getItem('userInfo') || '{}')
  })
  
  // 判断消息是否为自己发送的
  const isSelfMessage = (message) => {
    return message.senderId === userInfo.value.id
  }
  
  // 获取自己的头像
  const getSelfAvatar = () => {
    return userInfo.value.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  }
  
  // 获取对方的头像
  const getOtherUserAvatar = () => {
    return chatInfo.value.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
  }
  
  // 格式化日期
  const formatDate = (dateString) => {
    if (!dateString) return ''
    
    const date = new Date(dateString)
    
    if (isToday(date)) {
      return '今天'
    } else if (isYesterday(date)) {
      return '昨天'
    } else {
      return format(date, 'yyyy年MM月dd日')
    }
  }
  
  // 格式化消息时间
  const formatMessageTime = (dateString) => {
    if (!dateString) return ''
    
    const date = new Date(dateString)
    return format(date, 'HH:mm')
  }
  
  // 判断是否需要显示日期分割线
  const shouldShowDateDivider = (message, index) => {
    if (index === 0) return true // 确保第一条消息也显示日期
    
    const currentDate = new Date(message.createdAt)
    const prevDate = new Date(messages.value[index - 1].createdAt)
    
    return !isSameDay(currentDate, prevDate)
  }
  
  // 获取聊天消息
  const fetchMessages = async () => {
    try {
      const res = await getConversationMessages(chatId)
      if (res.code === 200) {
        messages.value = res.data || []
        chatInfo.value = res.data.conversationInfo || {}
        isGroupChat.value = res.data.conversationInfo?.type === 'GROUP'
        
        // 滚动到底部
        await nextTick()
        scrollToBottom()
      }
    } catch (error) {
      console.error('获取消息失败:', error)
      ElMessage.error('获取消息失败')
    }
  }
  
  // 发送消息
  const sendMessage = async () => {
    if (!messageText.value.trim()) {
      return
    }
    
    try {
      const data = {
        conversationId: chatId,
        content: messageText.value,
        type: 'TEXT'
      }
      
      // 先添加到本地消息列表，提升用户体验
      const tempMessage = {
        id: 'temp-' + Date.now(),
        content: messageText.value,
        createdAt: new Date().toISOString(),
        senderId: userInfo.value.id,
        senderName: userInfo.value.name,
        senderRole: userInfo.value.role,
        status: 'SENDING'
      }
      
      messages.value.push(tempMessage)
      
      // 清空输入框并关闭快捷语句面板
      messageText.value = ''
      showQuickPhrases.value = false
      
      // 滚动到底部
      await nextTick()
      scrollToBottom()
      
      // 发送消息到服务器
      const res = await sendMessageAPI(chatId, data)
      
      if (res.code === 200) {
        // 更新临时消息状态
        const index = messages.value.findIndex(m => m.id === tempMessage.id)
        if (index !== -1) {
          messages.value[index] = {
            ...tempMessage,
            id: res.data.id || tempMessage.id,
            status: 'SENT'
          }
        }
      } else {
        // 发送失败，更新状态
        const index = messages.value.findIndex(m => m.id === tempMessage.id)
        if (index !== -1) {
          messages.value[index].status = 'FAILED'
        }
        ElMessage.error('发送失败')
      }
    } catch (error) {
      console.error('发送消息失败:', error)
      ElMessage.error('发送消息失败')
      
      // 更新临时消息状态为发送失败
      const index = messages.value.findIndex(m => m.id.startsWith('temp-'))
      if (index !== -1) {
        messages.value[index].status = 'FAILED'
      }
    }
  }
  
  // 处理Enter键发送消息
  const handleEnterPress = (e) => {
    if (e.ctrlKey || e.metaKey) {
      // Ctrl+Enter 或 Command+Enter 换行
      messageText.value += '\n'
    } else {
      // Enter 直接发送
      sendMessage()
    }
  }
  
  // 滚动到底部
  const scrollToBottom = () => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  }
  
  // 返回上一页
  const goBack = () => {
    router.back()
  }
  
  // 切换快捷语句面板
  const toggleQuickPhrases = () => {
    showQuickPhrases.value = !showQuickPhrases.value
  }
  
  // 选择快捷语句
  const selectQuickPhrase = (phrase) => {
    messageText.value = phrase
    showQuickPhrases.value = false
  }
  
  onMounted(() => {
    fetchMessages()
    
    // 加载图标字体库
    const script = document.createElement('script')
    script.src = '//at.alicdn.com/t/font_2553510_zvt5nus3ae.js'
    document.head.appendChild(script)
  })
  </script>
  
  <style scoped>
  .chat-container {
    height: 600px;
    margin: 20px auto;
    display: flex;
    flex-direction: column;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    position: relative;
  }
  
  /* 头部样式 */
  .chat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background-color: #fff;
    border-bottom: 1px solid #eaeaea;
    height: 60px;
  }
  
  .header-left {
    display: flex;
    align-items: center;
  }
  
  .back-button {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    border-radius: 4px;
    margin-right: 12px;
  }
  
  .back-button:hover {
    background-color: #f5f5f5;
  }
  
  .header-left .avatar {
    width: 36px;
    height: 36px;
    margin-right: 12px;
  }
  
  .header-info {
    margin-left: 12px;
  }
  
  .chat-name {
    font-size: 16px;
    font-weight: 500;
    color: #333;
  }
  
  .chat-status {
    font-size: 12px;
    color: #8f959e;
    margin-top: 2px;
  }
  
  .header-right {
    cursor: pointer;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 4px;
  }
  
  .header-right:hover {
    background-color: #f5f5f5;
  }
  
  /* 消息列表样式 */
  .message-list {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    background-color: #f7f8fa;
  }
  
  .message-date-divider {
    text-align: center;
    margin: 16px 0;
    color: #8f959e;
    font-size: 12px;
    position: relative;
  }
  
  .message-date-divider::before,
  .message-date-divider::after {
    content: '';
    position: absolute;
    top: 50%;
    width: calc(50% - 50px);
    height: 1px;
    background-color: #e8e8e8;
  }
  
  .message-date-divider::before {
    left: 0;
  }
  
  .message-date-divider::after {
    right: 0;
  }
  
  .message-wrapper {
    margin-bottom: 16px;
  }
  
  .message-item {
    display: flex;
    margin-bottom: 8px;
    align-items: flex-start;
  }
  
  .message-self {
    flex-direction: row-reverse;
  }
  
  .avatar {
    width: 36px;
    height: 36px;
    border-radius: 4px;
    overflow: hidden;
    flex-shrink: 0;
  }
  
  .self-avatar {
    margin-left: 12px;
  }
  
  .avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .message-content {
    max-width: 70%;
    margin: 0 12px;
  }
  
  .sender-name {
    font-size: 12px;
    color: #8f959e;
    margin-bottom: 4px;
  }
  
  .message-bubble {
    position: relative;
  }
  
  .message-text {
    padding: 10px 12px;
    border-radius: 6px;
    word-break: break-word;
    line-height: 1.5;
    font-size: 14px;
  }
  
  .message-self .message-text {
    background-color: #2b7efb;
    color: #fff;
    border-top-right-radius: 0;
  }
  
  .message-other .message-text {
    background-color: #fff;
    color: #333;
    border-top-left-radius: 0;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  }
  
  .message-time {
    font-size: 12px;
    color: #8f959e;
    margin-top: 4px;
    text-align: right;
  }
  
  /* 快捷语句样式 */
  .quick-phrases {
    position: absolute;
    bottom: 120px;
    left: 16px;
    right: 16px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    padding: 12px;
    z-index: 10;
    max-height: 200px;
    overflow-y: auto;
  }
  
  .phrase-item {
    padding: 10px 12px;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.2s;
    margin-bottom: 4px;
  }
  
  .phrase-item:hover {
    background-color: #f5f7fa;
  }
  
  .phrase-item:last-child {
    margin-bottom: 0;
  }
  
  /* 输入区域样式 */
  .message-input-container {
    background-color: #fff;
    border-top: 1px solid #eaeaea;
    padding: 12px 16px;
  }
  
  .toolbar {
    display: flex;
    gap: 16px;
    padding: 8px 0;
    margin-bottom: 8px;
  }
  
  .tool-icon {
    display: flex;
    align-items: center;
    color: #666;
    font-size: 14px;
    cursor: pointer;
    border-radius: 4px;
    transition: all 0.2s;
    padding: 4px 8px;
  }
  
  .tool-icon:hover {
    background-color: #f5f5f5;
    color: #2b7efb;
  }
  
  .tool-icon .icon {
    width: 20px;
    height: 20px;
    margin-right: 4px;
  }
  
  .tool-icon span {
    font-size: 12px;
  }
  
  .input-area {
    display: flex;
    align-items: flex-end;
  }
  
  .message-textarea {
    flex: 1;
    border: 1px solid #e8e8e8;
    border-radius: 4px;
    outline: none;
    resize: none;
    padding: 8px 12px;
    max-height: 120px;
    min-height: 24px;
    font-size: 14px;
    line-height: 1.5;
    font-family: inherit;
    background-color: #f7f8fa;
  }
  
  .message-textarea:focus {
    border-color: #2b7efb;
  }
  
  .send-button {
    margin-left: 12px;
    padding: 8px 16px;
    background-color: #e8e8e8;
    color: #8f959e;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    transition: all 0.2s;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .send-button.active {
    background-color: #2b7efb;
    color: #fff;
  }
  
  /* 使用 Element Plus 图标 */
  [class^="el-icon-"] {
    font-size: 20px;
    color: #606266;
  }
  </style>
<template>
    <div class="chat-page">
      <van-nav-bar
        class="chat-header"
        :title="companyName"
        left-arrow
        @click-left="onClickLeft"
      >
        <template #right>
          <van-icon name="more-o" size="18" />
        </template>
      </van-nav-bar>
  
      <div class="chat-content" ref="chatContent">
        <div 
          v-for="msg in sortedMessages" 
          :key="msg.id"
          :class="['message-wrapper', { 'message-self': msg.senderRole === 'STUDENT' }]"
        >
          <div class="avatar" v-if="msg.senderRole !== 'STUDENT'">
            <img :src="companyLogo || '/default-avatar.png'" alt="HR">
          </div>

          <div class="message-box">
            <div class="sender-name" v-if="msg.senderRole !== 'STUDENT'">
              {{ companyName }}
            </div>
            
            <div class="message-content" :class="{ 'resume-type': msg.type === 'resume' }">
              <div class="message-text">
                {{ msg.content }}
              </div>
              <a 
                v-if="msg.content.includes('这是我的简历，您可以查看')" 
                class="resume-link-text" 
                @click="handleViewResume(msg.resumeId)"
              >
                查看简历
              </a>
            </div>
            
            <div class="message-time">
              {{ formatTime(msg.createTime) }}
            </div>
          </div>
          
          <div class="avatar" v-if="msg.senderRole === 'STUDENT'">
            <img :src="msg.senderAvatar || '/default-avatar.png'" alt="我">
          </div>
        </div>
      </div>
  
      <div class="chat-footer">
        <van-field
          v-model="inputMessage"
          placeholder="请输入消息"
          class="message-input"
        >
          <template #button>
            <van-button 
              size="small" 
              type="primary"
              @click="sendMessage"
            >发送</van-button>
          </template>
        </van-field>
      </div>

      <!-- 替换为原生 dialog -->
      <dialog ref="pdfDialog" class="pdf-dialog">
        <div class="dialog-header">
          <span class="dialog-title">简历预览</span>
          <span class="close-btn" @click="closePdfDialog">×</span>
        </div>
        <iframe
          v-if="pdfUrl"
          :src="pdfUrl"
          class="pdf-preview"
        ></iframe>
      </dialog>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, nextTick, computed, watch } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { getConversationMessages } from '@/api/jobs'
  import { formatTime } from '@/utils/format'
  import { sendMessageAPI } from '@/api/messages'
  import { getResumePDF } from '@/api/resume'
  import { ElMessage } from 'element-plus'
  const route = useRoute()
  const router = useRouter()
  const messages = ref([])
  const chatContent = ref(null)
  const inputMessage = ref('')
  const currentUserId = localStorage.getItem('userInfo')?.id
  const userAvatar = localStorage.getItem('userInfo')?.avatar
  const companyLogo = route.query.companyLogo
  const companyName = route.query.companyName
  const conversationId = route.params.chatId
  const showPdfDialog = ref(false)
  const pdfUrl = ref('')
  const pdfDialog = ref(null)
  
  // 计算属性，按时间排序消息
  const sortedMessages = computed(() => {
    return messages.value.slice().sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
  })
  
  onMounted(async () => {
    await fetchMessages()
  })
  
  const fetchMessages = async () => {
    try {
      const res = await getConversationMessages(conversationId)
      messages.value = res.data
      scrollToBottom()
    } catch (error) {
      console.error('获取消息失败:', error)
    }
  }
  
  const scrollToBottom = () => {
    nextTick(() => {
      if (chatContent.value) {
        chatContent.value.scrollTop = chatContent.value.scrollHeight
      }
    })
  }
  
  const onClickLeft = () => {
    router.back()
  }
  
  const sendMessage = async () => {
    if (!inputMessage.value.trim()) {
      ElMessage.error('请输入消息内容')
      return
    }

    try {
      const response = await sendMessageAPI(conversationId, {
        content: inputMessage.value,
        type: 'text'  // 明确指定类型为 text
      })
      if (response.code === 200) {
        ElMessage.success('消息发送成功')
        inputMessage.value = ''
        await fetchMessages()
      } else {
        ElMessage.error('消息发送失败')
      }
    } catch (error) {
      console.error('发送消息失败:', error)
      ElMessage.error('发送消息失败')
    }
  }
  
  // 修改查看简历方法
  const handleViewResume = async (resumeId) => {
    try {
      const response = await getResumePDF()
      const blob = new Blob([response.data], { type: 'application/pdf' })
      pdfUrl.value = window.URL.createObjectURL(blob)
      // 使用原生 showModal 方法打开弹窗
      pdfDialog.value?.showModal()
    } catch (error) {
      console.error('获取简历失败:', error)
      ElMessage.error('获取简历失败')
    }
  }

  // 添加关闭弹窗方法
  const closePdfDialog = () => {
    if (pdfUrl.value) {
      window.URL.revokeObjectURL(pdfUrl.value)
      pdfUrl.value = ''
    }
    pdfDialog.value?.close()
  }
  </script>
  
  <style scoped>
  html, body {
    height: 100%;
    margin: 0;
    overflow: hidden; /* 防止页面整体滚动 */
  }
  
  .chat-page {
    height: 100vh;
    display: flex;
    flex-direction: column;
    background: #f5f5f5;
  }
  
  .chat-header {
    background: #fff;
    border-bottom: 1px solid #eee;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  .chat-content {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    background: #e9ecef; /* 更柔和的背景色 */
  }
  
  .message-wrapper {
    display: flex;
    align-items: flex-start;
    margin-bottom: 20px;
    gap: 8px;
  }
  
  .message-self {
    flex-direction: row-reverse;
  }
  
  .avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%; /* 圆形头像 */
    overflow: hidden;
    flex-shrink: 0;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  .avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .message-box {
    max-width: 70%;
  }
  
  .sender-name {
    font-size: 12px;
    color: #6c757d;
    margin-bottom: 4px;
  }
  
  .message-content {
    padding: 12px 16px;
    border-radius: 20px; /* 圆角 */
    background: #fff;
    font-size: 14px;
    line-height: 1.5;
    position: relative;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); /* 阴影 */
  }
  
  .message-self .message-content {
    background: #007bff;
    color: #fff;
  }
  
  .message-time {
    font-size: 12px;
    color: #adb5bd;
    margin-top: 4px;
    text-align: right;
  }
  
  .chat-footer {
    padding: 10px;
    background: #fff;
    border-top: 1px solid #eee;
    box-shadow: 0 -2px 4px rgba(0, 0, 0, 0.1);
    position: sticky;
    bottom: 0;
    z-index: 10;
  }
  
  .message-input {
    background: #f5f5f5;
    border-radius: 20px;
  }
  
  .message-input :deep(.van-field__body) {
    background: #f5f5f5;
  }
  
  /* 添加消息气泡的小三角 */
  .message-content::before {
    content: '';
    position: absolute;
    top: 12px;
    border: 6px solid transparent;
  }
  
  .message-self .message-content::before {
    right: -12px;
    border-left-color: #007bff;
  }
  
  .message-content::before {
    left: -12px;
    border-right-color: #fff;
  }

  .message-content.resume-type {
    background: #f8f9fa;
    border: 1px solid #e9ecef;
  }

  .resume-message {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .resume-link {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    color: var(--primary-color);
    cursor: pointer;
    padding: 8px 12px;
    background: #fff;
    border: 1px solid var(--primary-color);
    border-radius: 4px;
    font-size: 14px;
    transition: all 0.3s;
  }

  .resume-link:hover {
    background: var(--primary-color);
    color: #fff;
  }

  .resume-link .van-icon {
    font-size: 16px;
  }

  .message-self .message-content.resume-type {
    background: #e6f7ff;
    border: 1px solid #91d5ff;
  }

  .message-self .resume-link {
    background: #fff;
  }

  .message-self .resume-link:hover {
    background: var(--primary-color);
    color: #fff;
  }

  .resume-link-text {
    font-size: 12px;
    color: #ff4d4f;
    text-decoration: none;
    cursor: pointer;
    margin-top: 4px;
    display: inline-block;
  }

  .resume-link-text:hover {
    text-decoration: underline;
    color: #ff7875;
  }

  .message-self .resume-link-text {
    color: #ff4d4f;
  }

  .message-self .resume-link-text:hover {
    color: #ff7875;
  }

  .pdf-dialog {
    width: 100%;
    height: 100%;
    padding: 0;
    margin: 0;
    border: none;
    background: white;
  }

  .pdf-dialog::backdrop {
    background: rgba(0, 0, 0, 0.5);
  }

  .dialog-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid #eee;
    background: #fff;
  }

  .dialog-title {
    font-size: 16px;
    font-weight: 500;
    color: #333;
  }

  .close-btn {
    font-size: 24px;
    color: #999;
    cursor: pointer;
    padding: 4px 8px;
  }

  .close-btn:hover {
    color: #666;
  }

  .pdf-preview {
    width: 100%;
    height: calc(100% - 53px);
    border: none;
    display: block;
  }
  </style>
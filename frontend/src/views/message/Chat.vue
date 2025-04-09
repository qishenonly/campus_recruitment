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
            <div class="chat-name">
              <!-- 根据用户角色显示不同的聊天对象信息 -->
              <template v-if="userRole === 'STUDENT'">
                {{ chatInfo.name || '聊天' }}
              </template>
              <template v-else-if="userRole === 'COMPANY'">
                {{ studentInfo.realName || '未知学生' }}
              </template>
              <template v-else>
                {{ chatInfo.name || '聊天' }}
              </template>
            </div>
            <div class="chat-meta" v-if="jobInfo.title && userRole === 'STUDENT'">
              <span class="job-title">{{ jobInfo.title }}</span>
            </div>
            <div class="chat-meta" v-else-if="userRole === 'COMPANY'">
              <span class="job-title">{{ studentInfo.university || '未知学校' }}</span>
            </div>
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
                <div v-if="isResumeLink(message.content)" class="resume-preview-container">
                  <div class="resume-preview-card">
                    <div class="resume-icon">
                      <el-icon><Document /></el-icon>
                    </div>
                    <div class="resume-info">
                      <div class="resume-title">求职简历</div>
                      <div class="resume-desc">点击查看详细简历信息</div>
                    </div>
                    <el-button 
                      type="primary" 
                      size="small" 
                      class="view-resume-btn"
                      @click="handleViewResume()"
                    >
                      查看简历
                    </el-button>
                  </div>
                </div>
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
    
    <!-- 简历预览弹窗 -->
    <el-dialog
      v-model="resumeDialogVisible"
      title="简历预览"
      width="80%"
      :before-close="closeResumeDialog"
      class="resume-dialog"
      fullscreen
    >
      <div class="resume-dialog-content">
        <div v-if="resumeLoading" class="resume-loading">
          <el-icon class="loading-icon"><Loading /></el-icon>
          <span>简历加载中...</span>
        </div>
        <div v-else-if="resumeError" class="resume-error">
          <el-icon><CircleClose /></el-icon>
          <span>{{ resumeError }}</span>
        </div>
        <div v-else class="pdf-container">
          <!-- 添加PDF预览控制栏 -->
          <div class="preview-toolbar">
            <el-button-group>
              <el-button :disabled="currentPage <= 1" @click="changePage(-1)">
                上一页
              </el-button>
              <el-button :disabled="currentPage >= totalPages" @click="changePage(1)">
                下一页
              </el-button>
            </el-button-group>
            <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
            <el-button-group>
              <el-button @click="changeScale(-0.2)">缩小</el-button>
              <el-button @click="changeScale(0.2)">放大</el-button>
            </el-button-group>
          </div>
          <!-- 替换iframe为canvas -->
          <div class="canvas-container">
            <canvas ref="canvasRef"></canvas>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeResumeDialog">关闭</el-button>
          <el-button type="primary" @click="downloadResume" :loading="downloadLoading">
            下载简历
          </el-button>
        </span>
      </template>
    </el-dialog>
  </template>
  
  <script setup>
  import { ref, onMounted, nextTick, computed, watch, onUnmounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { format, isToday, isYesterday, isSameDay, parseISO } from 'date-fns'
  import { sendMessageAPI, markConversationAsRead } from '@/api/messages'
  import { getCompanyByJobId, getConversationMessages } from '@/api/jobs'
  import { getUserInfo } from '@/api/user'
  import { ElMessage } from 'element-plus'
  import { Document, Loading, CircleClose } from '@element-plus/icons-vue'
  import { getResumePDF, getResumePDFById } from '@/api/resume'
  // 修改 PDF.js 导入
  import * as pdfjsLib from 'pdfjs-dist'

  // 设置 worker 路径为本地路径
  pdfjsLib.GlobalWorkerOptions.workerSrc = new URL(
    'pdfjs-dist/build/pdf.worker.mjs',
    import.meta.url
  ).href
    
  // 处理头像URL的方法
  const getAvatarUrl = (url) => {
    if (!url) return '';
    
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
      const baseURL = import.meta.env.VITE_API_BASE_URL || '';
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

  const route = useRoute()
  const router = useRouter()
  const chatId = route.params.chatId
  const messages = ref([])
  const messageText = ref('')
  const messageListRef = ref(null)
  const chatInfo = ref({})
  const studentInfo = ref({}) // 添加学生信息对象
  const isGroupChat = ref(false)
  const showQuickPhrases = ref(false)
  const jobInfo = ref({
    title: route.query.jobTitle || '',
    id: route.query.jobId || ''
  })
  
  // 获取当前用户角色
  const userRole = computed(() => {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return userInfo.role || ''
  })
  
  // 安全地解析日期
  const safeParseDate = (dateString) => {
    if (!dateString) {
      return new Date(0); // 返回一个默认日期（1970年1月1日）
    }
    
    try {
      return parseISO(dateString);
    } catch (error) {
      console.error('日期解析错误:', error, dateString);
      return new Date(0); // 解析失败时返回默认日期
    }
  }
  
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
    return getAvatarUrl(userInfo.value.avatar) || '/images/default-avatar.png'
  }
  
  // 获取对方的头像
  const getOtherUserAvatar = () => {
    // 根据用户角色返回不同的头像
    if (userRole.value === 'COMPANY' && studentInfo.value.avatar) {
      return getAvatarUrl(studentInfo.value.avatar);
    }
    return getAvatarUrl(chatInfo.value.avatar) || '/images/default-avatar.png'
  }
  
  // 判断是否需要显示日期分割线
  const shouldShowDateDivider = (message, index) => {
    if (index === 0) return true;
    
    if (!message.createTime || !messages.value[index - 1].createTime) {
      return false;
    }
    
    const currentDate = safeParseDate(message.createTime);
    const prevDate = safeParseDate(messages.value[index - 1].createTime);
    
    return !isSameDay(currentDate, prevDate);
  }
  
  // 格式化日期
  const formatDate = (dateString) => {
    if (!dateString) return '';
    
    try {
      const date = safeParseDate(dateString);
      
      if (isToday(date)) {
        return '今天';
      } else if (isYesterday(date)) {
        return '昨天';
      } else {
        return format(date, 'yyyy年MM月dd日');
      }
    } catch (error) {
      console.error('格式化日期错误:', error);
      return '';
    }
  }
  
  // 格式化消息时间
  const formatMessageTime = (dateString) => {
    if (!dateString) return '';
    
    try {
      const date = safeParseDate(dateString);
      return format(date, 'HH:mm');
    } catch (error) {
      console.error('格式化时间错误:', error);
      return '';
    }
  }
  
  // 获取聊天消息
  const fetchMessages = async () => {
    try {
      console.log('获取聊天id:', chatId, '的消息');
      const res = await getConversationMessages(chatId);
      if (res.code === 200) {
        // 确保每条消息都有有效的createdAt属性
        messages.value = res.data || []

        // 按时间顺序排序
        messages.value.sort((a, b) => {
          return new Date(a.createTime) - new Date(b.createTime);
        });
        
        console.log('获取到的聊天消息:', messages.value);
        chatInfo.value = res.data.conversationInfo || {};
        
        // 如果是企业用户，获取学生信息
        if (userRole.value === 'COMPANY' && messages.value.length > 0) {
          const studentId = messages.value.find(m => m.senderRole === 'STUDENT')?.senderId;
          if (studentId) {
            try {
              const res = await getUserInfo(studentId);
              if (res.code === 200 && res.data) {
                studentInfo.value = res.data;
                console.log('获取到学生信息:', studentInfo.value);
                
                // 确保调试时可以看到返回的数据结构
                console.log('学生信息字段:', Object.keys(studentInfo.value));
              }
            } catch (error) {
              console.error('获取学生信息失败:', error);
            }
          }
        }
        
        // 自动标记会话中所有消息为已读
        // try {
        //   await markConversationAsRead(chatId);
        //   console.log('所有消息已标记为已读');
        // } catch (error) {
        //   console.error('标记消息已读失败:', error);
        // }
        
        // 获取第一条消息，查找是否包含职位信息
        if (messages.value.length > 0) {
          const firstMessage = messages.value[0];
          console.log('第一条消息:', firstMessage);
          
          // 检查是否有职位信息
          if (firstMessage.jobTitle && !jobInfo.value.title) {
            console.log('设置职位标题:', firstMessage.jobTitle);
            jobInfo.value.title = firstMessage.jobTitle;
          }
          if (firstMessage.jobId && !jobInfo.value.id) {
            console.log('设置职位ID:', firstMessage.jobId);
            jobInfo.value.id = firstMessage.jobId;
            
            // 如果有职位ID，尝试获取公司信息
            if (firstMessage.jobId) {
              try {
                const companyResponse = await getCompanyByJobId(firstMessage.jobId);
                console.log('根据职位ID获取到的信息:', companyResponse);
                if (companyResponse.code === 200 && companyResponse.data) {
                  // 更新聊天头部显示的公司信息
                  if (companyResponse.data.company) {
                    chatInfo.value.name = companyResponse.data.company.name;
                    chatInfo.value.avatar = companyResponse.data.company.logo;
                    // 将公司信息添加到jobInfo中
                    jobInfo.value.companyName = companyResponse.data.company.name;
                    jobInfo.value.companyLogo = companyResponse.data.company.logo;
                    jobInfo.value.companySize = companyResponse.data.company.size;
                    jobInfo.value.companyIndustry = companyResponse.data.company.industry;
                  }
                  
                  // 更新职位信息
                  if (companyResponse.data.job && (!jobInfo.value.title || jobInfo.value.title === '')) {
                    jobInfo.value.title = companyResponse.data.job.title;
                  }
                }
              } catch (error) {
                console.error('获取公司信息失败:', error);
              }
            }
          }
          
          // 尝试从消息内容中提取职位信息
          if (!jobInfo.value.title && firstMessage.content && firstMessage.content.includes('投递')) {
            const match = firstMessage.content.match(/投递"(.*?)"/);
            if (match && match[1]) {
              console.log('从内容中提取职位标题:', match[1]);
              jobInfo.value.title = match[1];
            }
          }
        }
        
        // 如果仍然没有职位信息，查找其他消息
        if (!jobInfo.value.title) {
          console.log('在所有消息中寻找职位信息');
          for (const msg of messages.value) {
            if (msg.jobTitle) {
              jobInfo.value.title = msg.jobTitle;
              console.log('从消息中找到职位标题:', msg.jobTitle);
              break;
            }
            if (msg.content && msg.content.includes('投递')) {
              const match = msg.content.match(/投递"(.*?)"/);
              if (match && match[1]) {
                console.log('从内容中提取职位标题:', match[1]);
                jobInfo.value.title = match[1];
                break;
              }
            }
          }
        }
        
        // 从URL参数中获取
        if (!jobInfo.value.title && route.query.jobTitle) {
          console.log('从URL参数获取职位标题:', route.query.jobTitle);
          jobInfo.value.title = route.query.jobTitle;
        }
        
        // 如果存在jobInfo，确保在聊天头部显示
        console.log('最终职位信息:', jobInfo.value);
        
        // 滚动到底部
        await nextTick();
        scrollToBottom();
      }
    } catch (error) {
      console.error('获取消息失败:', error);
      ElMessage.error('获取消息失败');
    }
  }
  
  // 发送消息
  const sendMessage = async () => {
    if (!messageText.value.trim()) {
      return;
    }
    
    // 确保有职位信息
    console.log('发送消息时的职位信息:', jobInfo.value);
    
    const tempId = Date.now().toString();
    const tempMessage = {
      id: tempId,
      content: messageText.value,
      senderId: userInfo.value.id,
      senderName: userInfo.value.name,
      senderRole: userInfo.value.role,
      createdAt: new Date().toISOString(),
      status: 'sending',
      jobTitle: jobInfo.value.title,
      jobId: jobInfo.value.id
    };
    
    // 添加临时消息
    messages.value.push(tempMessage);
    
    // 清空输入框
    const content = messageText.value;
    messageText.value = '';
    
    // 关闭快捷语句面板
    showQuickPhrases.value = false;
    
    // 滚动到底部
    await nextTick();
    scrollToBottom();
    
    try {
      console.log('发送消息到API:', {
        content: content,
        jobTitle: jobInfo.value.title,
        jobId: jobInfo.value.id
      });
      
      const res = await sendMessageAPI(chatId, {
        content: content,
        jobTitle: jobInfo.value.title,
        jobId: jobInfo.value.id
      });
      
      if (res.code === 200) {
        console.log('消息发送成功，服务器响应:', res.data);
        // 确保返回的消息有createdAt属性
        const responseMessage = res.data;
        if (!responseMessage.createdAt) {
          responseMessage.createdAt = new Date().toISOString();
        }
        
        // 更新临时消息状态
        const index = messages.value.findIndex(msg => msg.id === tempId);
        if (index !== -1) {
          messages.value[index] = {
            ...responseMessage,
            status: 'sent',
            jobTitle: jobInfo.value.title,
            jobId: jobInfo.value.id
          };
        }
      } else {
        throw new Error(res.message || '发送失败');
      }
    } catch (error) {
      console.error('发送消息失败:', error);
      
      // 更新临时消息状态为发送失败
      const index = messages.value.findIndex(msg => msg.id === tempId);
      if (index !== -1) {
        messages.value[index].status = 'failed';
      }
      
      ElMessage.error('发送消息失败');
    }
  }
  
  // 处理Enter键
  const handleEnterPress = (e) => {
    if (e.ctrlKey) {
      // Ctrl+Enter 换行
      messageText.value += '\n'
    } else {
      // Enter 发送
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
  
  // 加载图标字体库
  const loadIconFont = () => {
    try {
      const script = document.createElement('script')
      script.src = 'https://at.alicdn.com/t/font_2553510_zvt5hy0d4i.js'
      script.onerror = () => {
        console.warn('图标字体加载失败，使用备用图标')
      }
      document.body.appendChild(script)
    } catch (error) {
      console.error('加载图标字体出错:', error)
    }
  }
  
  // 简历相关功能
  const resumeDialogVisible = ref(false)
  const resumeLoading = ref(false)
  const resumeError = ref('')
  const pdfSrc = ref('')
  const downloadLoading = ref(false)
  const currentResumeData = ref(null)
  const canvasRef = ref(null)
  const currentPage = ref(1)
  const totalPages = ref(0)
  const scale = ref(1.5)
  let pdfDoc = null
  
  // 检测消息是否包含简历链接
  const isResumeLink = (content) => {
    return content && content.includes('简历，您可以查看')
  }

  function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  // 处理查看简历
  const handleViewResume = async () => {
    resumeDialogVisible.value = true
    resumeLoading.value = true
    resumeError.value = ''
    pdfSrc.value = ''

    try {
      if (localStorage.getItem('lastVisitedRole') === 'COMPANY') {
        const rsp = await getConversationMessages(chatId)
        const lens = rsp.data.length
        if (lens > 0) {
          const lastMsg = rsp.data[lens - 1]
          const response = await getResumePDFById(lastMsg.senderId)

          // 获取二进制数据
          console.log("response.data: ", response);
          const arrayBuffer = response
          const bytes = new Uint8Array(arrayBuffer)

          if (bytes.length === 0) {
            throw new Error('简历数据为空')
          }

          // 加载 PDF 文档
          pdfDoc = await pdfjsLib.getDocument({ data: bytes }).promise
          totalPages.value = pdfDoc.numPages
          
          // 延迟一秒后渲染第一页
          await delay(1000)
          resumeLoading.value = false
          
          // 等待DOM更新后渲染PDF
          await nextTick()
          renderPage(1)
          
          ElMessage.success('简历加载成功')
        }
      } else {
        // 调用API获取简历数据
        const response = await getResumePDF()
        
        // 获取二进制数据
        const arrayBuffer = response.data
        const bytes = new Uint8Array(arrayBuffer)

        if (bytes.length === 0) {
          throw new Error('简历数据为空')
        }

        // 加载 PDF 文档
        pdfDoc = await pdfjsLib.getDocument({ data: bytes }).promise
        totalPages.value = pdfDoc.numPages
        
        // 延迟一秒后渲染第一页
        await delay(1000)
        resumeLoading.value = false
        
        // 等待DOM更新后渲染PDF
        await nextTick()
        renderPage(1)
        
        ElMessage.success('简历加载成功')
      }

      
    } catch (error) {
      console.error('获取简历失败:', error)
      resumeError.value = '获取简历失败: ' + (error.message || '未知错误')
      resumeLoading.value = false
    }
  }
  
  // 渲染PDF页面
  const renderPage = async (pageNumber) => {
    if (!pdfDoc) return

    try {
      const page = await pdfDoc.getPage(pageNumber)
      const canvas = canvasRef.value
      const context = canvas.getContext('2d')

      const viewport = page.getViewport({ scale: scale.value })
      canvas.height = viewport.height
      canvas.width = viewport.width

      await page.render({
        canvasContext: context,
        viewport: viewport
      }).promise

      currentPage.value = pageNumber
    } catch (error) {
      console.error('渲染页面失败:', error)
      ElMessage.error('渲染页面失败')
    }
  }

  // 翻页功能
  const changePage = async (delta) => {
    const newPage = currentPage.value + delta
    if (newPage >= 1 && newPage <= totalPages.value) {
      await renderPage(newPage)
    }
  }

  // 缩放功能
  const changeScale = async (delta) => {
    scale.value = Math.max(0.5, Math.min(2.5, scale.value + delta))
    await renderPage(currentPage.value)
  }

  // 下载简历
  const downloadResume = async () => {
    if (!pdfDoc) {
      ElMessage.warning('简历数据不存在')
      return
    }
    
    downloadLoading.value = true
    
    try {
      // 模拟下载延迟
      await delay(1000)
      
      // 创建下载链接
      const response = await getResumePDF()
      const blob = new Blob([response.data], { type: 'application/pdf' })
      const url = URL.createObjectURL(blob)
      
      // 创建下载链接并点击
      const link = document.createElement('a')
      link.href = url
      link.download = '求职简历.pdf'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      // 释放URL对象
      URL.revokeObjectURL(url)
      
      ElMessage.success('简历下载成功')
    } catch (error) {
      console.error('下载简历失败:', error)
      ElMessage.error('下载简历失败')
    } finally {
      downloadLoading.value = false
    }
  }
  
  // 关闭简历预览
  const closeResumeDialog = () => {
    resumeDialogVisible.value = false
    
    // 释放PDF资源前先检查是否存在
    if (pdfDoc) {
      try {
        // 安全地关闭PDF文档
        pdfDoc.destroy().catch(e => console.error('PDF销毁失败:', e))
      } catch (error) {
        console.error('关闭PDF文档时出错:', error)
      }
    }
    
    // 重置所有相关状态
    pdfDoc = null
    currentPage.value = 1
    totalPages.value = 0
    scale.value = 1.5
    resumeLoading.value = false
    resumeError.value = ''
    
    // 清除canvas内容
    if (canvasRef.value) {
      const ctx = canvasRef.value.getContext('2d')
      if (ctx) {
        ctx.clearRect(0, 0, canvasRef.value.width, canvasRef.value.height)
      }
    }
  }
    
  // 添加定时器变量
  let timer = null

  onMounted(() => {
    fetchMessages()
    loadIconFont()
    
    // 设置定时器，每30秒刷新一次消息
    timer = setInterval(fetchMessages, 30000)
  })

  // 组件卸载时清除定时器和资源
  onUnmounted(() => {
    if (timer) {
      clearInterval(timer)
    }

    // 释放PDF资源
    if (pdfDoc) {
      try {
        pdfDoc.destroy().catch(e => console.error('PDF销毁失败:', e))
      } catch (error) {
        console.error('组件卸载时关闭PDF文档出错:', error)
      }
      pdfDoc = null
    }
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
  
  .chat-meta {
    font-size: 12px;
    color: #8f959e;
    margin-top: 2px;
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
    display: inline-block;
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
  
  /* 简历预览卡片样式 */
  .resume-preview-container {
    margin-top: 10px;
  }
  
  .resume-preview-card {
    display: flex;
    align-items: center;
    background-color: #f5f7fa;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    padding: 12px;
    transition: all 0.3s;
  }
  
  .resume-preview-card:hover {
    background-color: #eef1f6;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
  
  .resume-icon {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 40px;
    height: 40px;
    background-color: #409eff;
    color: white;
    border-radius: 8px;
    margin-right: 12px;
  }
  
  .resume-info {
    flex: 1;
  }
  
  .resume-title {
    font-weight: bold;
    font-size: 16px;
    color: #303133;
    margin-bottom: 4px;
  }
  
  .resume-desc {
    font-size: 12px;
    color: #909399;
  }
  
  .view-resume-btn {
    margin-left: 12px;
  }
  
  /* 简历弹窗样式 */
  :deep(.resume-dialog .el-dialog__header) {
    border-bottom: 1px solid #e4e7ed;
    padding: 16px 20px;
  }
  
  :deep(.resume-dialog .el-dialog__body) {
    padding: 0;
    height: calc(100vh - 120px);
    overflow: hidden;
  }
  
  .resume-loading {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #909399;
  }
  
  .loading-icon {
    font-size: 32px;
    margin-bottom: 16px;
    animation: rotate 1.5s linear infinite;
  }
  
  @keyframes rotate {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
  }
  
  .resume-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #f56c6c;
  }
  
  .pdf-container {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
  }
  
  /* 添加PDF预览工具栏样式 */
  .preview-toolbar {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 16px;
    padding: 12px;
    border-bottom: 1px solid #e4e7ed;
    background-color: #f5f7fa;
  }
  
  .page-info {
    font-size: 14px;
    color: #606266;
    margin: 0 12px;
  }
  
  /* 添加canvas容器样式 */
  .canvas-container {
    flex: 1;
    overflow: auto;
    display: flex;
    justify-content: center;
    align-items: flex-start;
    padding: 20px;
    background-color: #f5f5f5;
  }
  
  canvas {
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    background-color: white;
  }
  </style>
<template>
  <div class="resume-page">
    <div class="resume-header">
      <h2>我的简历</h2>
      <p class="subtitle">完善的简历可以帮助你获得更多面试机会</p>
    </div>

    <div class="resume-content">
      <!-- 简历上传卡片 -->
      <div class="resume-upload-card">
        <!-- 只在没有PDF时显示上传区域 -->
        <div class="upload-area" @click="triggerUpload" v-if="!hasPDF">
          <el-icon class="upload-icon"><Upload /></el-icon>
          <p>点击上传简历</p>
          <p class="upload-tip">支持 PDF、Word 格式，大小不超过 10MB</p>
        </div>
        
        <!-- 有PDF时显示预览区域 -->
        <div class="resume-preview-area" v-else>
          <div class="preview-container">
            <canvas ref="canvasRef"></canvas>
          </div>
          <div class="preview-controls">
            <el-button-group>
              <el-button :disabled="currentPage <= 1" @click="changePage(-1)">
                上一页
              </el-button>
              <el-button :disabled="currentPage >= totalPages" @click="changePage(1)">
                下一页
              </el-button>
            </el-button-group>
            <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
          </div>
          <div class="resume-actions">
            <el-button type="primary" @click="previewResume">全屏预览</el-button>
            <el-button @click="replaceResume">替换</el-button>
            <el-button type="danger" @click="handleDelete">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 全屏预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="简历预览"
      width="90%"
      class="resume-preview-dialog"
      :close-on-click-modal="false"
      :destroy-on-close="true"
    >
      <div class="preview-container">
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
        <div class="canvas-container">
          <canvas ref="canvasRef"></canvas>
        </div>
      </div>
    </el-dialog>

    <!-- 隐藏的文件上传输入框 -->
    <input
      type="file"
      ref="fileInput"
      style="display: none"
      accept=".pdf,.doc,.docx"
      @change="handleFileChange"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Upload, Document, Files } from '@element-plus/icons-vue'
import { format } from 'date-fns'
import { uploadResume as uploadResumeAPI, getResume, deleteResume, getResumePDF } from '@/api/resume'

// 修改 PDF.js 导入
import * as pdfjsLib from 'pdfjs-dist'

// 设置 worker 路径为本地路径
pdfjsLib.GlobalWorkerOptions.workerSrc = new URL(
  'pdfjs-dist/build/pdf.worker.mjs',
  import.meta.url
).href

const fileInput = ref(null)
const resumeFile = ref(null)
const previewVisible = ref(false)
const currentPage = ref(1)
const totalPages = ref(0)
const scale = ref(1.5)
const canvasRef = ref(null)
const hasPDF = ref(false)
let pdfDoc = null
let pdfBytes = null

// 修改 Base64 解码函数
const base64ToUint8Array = (base64) => {
  try {
    // 移除可能的换行符和空格
    const cleanBase64 = base64.replace(/[\n\r\s]/g, '')
    
    // 如果有 data:application/pdf;base64, 前缀，移除它
    const rawBase64 = cleanBase64.replace(/^data:application\/pdf;base64,/, '')
    
    // 使用 btoa 进行解码
    const binaryString = decodeURIComponent(escape(window.atob(rawBase64)))
    const bytes = new Uint8Array(binaryString.length)
    for (let i = 0; i < binaryString.length; i++) {
      bytes[i] = binaryString.charCodeAt(i) & 0xff
    }
    return bytes
  } catch (error) {
    console.error('Base64解码失败:', error)
    
    // 尝试使用替代方法
    try {
      const binary = atob(base64)
      const bytes = new Uint8Array(binary.length)
      for (let i = 0; i < binary.length; i++) {
        bytes[i] = binary.charCodeAt(i)
      }
      return bytes
    } catch (fallbackError) {
      console.error('备用解码方法也失败:', fallbackError)
      throw new Error('PDF格式错误')
    }
  }
}

// 修改初始化函数
const initResume = async () => {
  const loadingInstance = ElLoading.service({
    text: '正在加载简历...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  try {
    // 修改 API 调用方式，直接获取二进制数据
    const response = await getResumePDF()
    console.log('response :>> ', response);


    // 直接获取二进制数据
    const arrayBuffer = response.data
    const bytes = new Uint8Array(arrayBuffer)

    if (bytes.length === 0) {
      hasPDF.value = false
      return
    }

    // 加载 PDF 文档
    pdfDoc = await pdfjsLib.getDocument({ data: bytes }).promise
    totalPages.value = pdfDoc.numPages
    hasPDF.value = true
    
    // 设置简历文件信息
    resumeFile.value = {
      name: '我的简历.pdf',
      type: 'application/pdf',
      uploadTime: new Date(),
      size: bytes.length
    }
    
    // 自动渲染第一页
    await renderPage(1)
    ElMessage.success('简历加载成功')
  } catch (error) {
    console.error('获取简历信息失败:', error)
    ElMessage.error('获取简历失败，请稍后重试')
    hasPDF.value = false
  } finally {
    loadingInstance.close()
  }
}

// 修改渲染页面函数
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

// 修改翻页函数
const changePage = async (delta) => {
  const newPage = currentPage.value + delta
  if (newPage >= 1 && newPage <= totalPages.value) {
    await renderPage(newPage)
  }
}

// 修改缩放函数
const changeScale = async (delta) => {
  scale.value = Math.max(0.5, Math.min(2.5, scale.value + delta))
  await renderPage(currentPage.value)
}

// 预览简历（直接使用已加载的PDF数据）
const previewResume = async () => {
  if (!pdfDoc) {
    ElMessage.warning('暂无简历')
    return
  }

  previewVisible.value = true
  // 渲染第一页
  renderPage(1)
}

// 触发文件选择
const triggerUpload = () => {
  fileInput.value.click()
}

// 处理文件选择
const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件大小和类型
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过10MB')
    return
  }

  const allowedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只支持PDF和Word格式文件')
    return
  }

  // 上传文件到服务器
  uploadResume(file)
}

// 上传简历
const uploadResume = async (file) => {
  const userInfos = JSON.parse(localStorage.getItem('userInfo'));
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('name', file.name)
    formData.append('studentId', userInfos.id)
    
    // TODO: 调用上传API
    const response = await uploadResumeAPI(formData)
    
    // 模拟上传成功
    resumeFile.value = {
      name: file.name,
      size: file.size,
      type: file.type,
      uploadTime: new Date(),
      url: URL.createObjectURL(file)
    }
    
    ElMessage.success('简历上传成功')
    initResume()
  } catch (error) {
    ElMessage.error('上传失败，请重试')
  }
}

// 替换简历
const replaceResume = () => {
  triggerUpload()
}

// 删除简历
const handleDelete = async () => {
  try {
    // TODO: 调用删除API
    // await deleteResumeAPI()
    
    resumeFile.value = null
    ElMessage.success('简历已删除')
  } catch (error) {
    ElMessage.error('删除失败，请重试')
  }
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

// 格式化日期
const formatDate = (date) => {
  return format(date, 'yyyy-MM-dd HH:mm')
}

// 页面挂载时初始化
onMounted(() => {
  initResume()
})
</script>

<style scoped>
.resume-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.resume-header {
  text-align: center;
  margin-bottom: 30px;
}

.resume-header h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
}

.subtitle {
  color: #666;
  font-size: 14px;
}

.resume-upload-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.upload-area {
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-area:hover {
  border-color: var(--el-color-primary);
  background: #f5f7fa;
}

.upload-icon {
  font-size: 48px;
  color: #909399;
  margin-bottom: 16px;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}

.resume-preview-area {
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.preview-container {
  width: 100%;
  min-height: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f5f5;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 16px;
}

.preview-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.resume-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.page-info {
  font-size: 14px;
  color: #666;
}

canvas {
  max-width: 100%;
  height: auto;
}

@media (max-width: 768px) {
  .preview-controls {
    flex-direction: column;
    gap: 8px;
  }
  
  .resume-actions {
    flex-direction: column;
  }
}
</style> 
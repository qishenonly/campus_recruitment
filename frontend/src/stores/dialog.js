import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useDialogStore = defineStore('dialog', () => {
  const showCompleteInfo = ref(false)
  const showCompleteCompanyInfo = ref(false)

  const showCompleteInfoDialog = () => {
    console.log('显示学生信息完善对话框')
    showCompleteInfo.value = true
  }

  const hideCompleteInfoDialog = () => {
    console.log('隐藏学生信息完善对话框')
    showCompleteInfo.value = false
  }
  
  const showCompleteCompanyInfoDialog = () => {
    console.log('显示企业信息完善对话框')
    showCompleteCompanyInfo.value = true
  }
  
  const hideCompleteCompanyInfoDialog = () => {
    console.log('隐藏企业信息完善对话框')
    showCompleteCompanyInfo.value = false
  }

  return {
    showCompleteInfo,
    showCompleteInfoDialog,
    hideCompleteInfoDialog,
    showCompleteCompanyInfo,
    showCompleteCompanyInfoDialog,
    hideCompleteCompanyInfoDialog
  }
}) 
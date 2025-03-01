import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useDialogStore = defineStore('dialog', () => {
  const showCompleteInfo = ref(false)

  const showCompleteInfoDialog = () => {
    showCompleteInfo.value = true
  }

  const hideCompleteInfoDialog = () => {
    showCompleteInfo.value = false
  }

  return {
    showCompleteInfo,
    showCompleteInfoDialog,
    hideCompleteInfoDialog
  }
}) 
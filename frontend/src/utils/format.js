import { format, isToday, isYesterday } from 'date-fns'
import { zhCN } from 'date-fns/locale'

/**
 * 格式化聊天消息时间
 * @param {string|Date} time - 时间
 * @returns {string} 格式化后的时间字符串
 */
export function formatTime(time) {
  if (!time) return ''
  
  const date = new Date(time)
  
  if (isToday(date)) {
    // 今天的消息只显示时间
    return format(date, 'HH:mm', { locale: zhCN })
  } else if (isYesterday(date)) {
    // 昨天的消息显示"昨天 时间"
    return `昨天 ${format(date, 'HH:mm', { locale: zhCN })}`
  } else {
    // 更早的消息显示完整日期和时间
    return format(date, 'yyyy-MM-dd HH:mm', { locale: zhCN })
  }
}

export default {
  formatTime
} 
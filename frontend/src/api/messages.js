import request from '@/utils/request'

// 获取会话列表
export function getConversations(params) {
  return request({
    url: '/conversations',
    method: 'get',
    params: {
      page: params.page || 0,
      size: params.size || 10,
      type: params.type || 'all' // 新增类型参数：all=所有, assigned=分配给自己的, company=公司的所有
    }
  })
}

// 已有的发送消息接口
export function sendMessageAPI(conversationId, data) {
  return request({
    url: `/conversations/${conversationId}/messages`,
    method: 'post',
    data: {
      content: data.content,
      jobTitle: data.jobTitle,
      jobId: data.jobId
    }
  })
} 

// 添加一个标记整个会话消息为已读的接口
export function markConversationAsRead(conversationId) {
  return request({
    url: `/conversations/${conversationId}/read`,
    method: 'post'
  })
}

// 添加一个标记单条消息为已读的接口
export function markMessageAsRead(conversationId, messageId) {
  return request({
    url: `/conversations/${conversationId}/messages/${messageId}/read`,
    method: 'post'
  })
}

export default {
  getConversations,
  sendMessageAPI,
  markConversationAsRead,
  markMessageAsRead
}
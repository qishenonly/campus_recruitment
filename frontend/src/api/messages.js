import request from '@/utils/request'

// 获取会话列表
export function getConversations(params) {
  return request({
    url: '/conversations',
    method: 'get',
    params
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

export default {
  getConversations,
  sendMessageAPI
}
import request from '../utils/request'

// 获取职位列表
export function getJobs(params) {
  return request({
    url: '/jobs',
    method: 'get',
    params
  }).then(response => {
    console.log('原始API响应:', response);
    
    // 检查响应是否为空
    if (!response) {
      console.error('API响应为空');
      return { data: [] };
    }
    
    // 如果响应是数组，则包装在预期格式中
    if (Array.isArray(response)) {
      console.log('API返回了数组格式');
      return { 
        data: response,
        totalElements: response.length 
      };
    }
    
    // 如果响应中没有data字段，而是直接返回了内容
    if (!response.data && (response.content || Array.isArray(response))) {
      console.log('API返回了非标准格式');
      const content = response.content || response;
      return {
        data: {
          content: content,
          totalElements: content.length || response.totalElements || 0
        }
      };
    }
    
    // 标准响应，直接返回
    return response;
  }).catch(error => {
    console.error('获取职位列表请求失败:', error);
    return { data: [] }; // 返回空数据而不是抛出错误
  });
}

// 获取职位详情
export function getJobDetail(id) {
  return request({
    url: `/jobs/${id}`,
    method: 'get'
  }).then(response => {
    console.log('职位详情原始API响应:', response);
    
    // 检查响应是否为空
    if (!response) {
      console.error('职位详情API响应为空');
      return { data: null };
    }
    
    // 如果响应是一个完整的对象，但没有 data 字段
    if (response && !response.data && response.id) {
      console.log('职位详情API返回了非标准格式');
      return { data: response };
    }
    
    // 如果响应有 code 和 data 字段，但 data 是 null
    if (response && response.code === 200 && response.data === null) {
      console.error('职位详情 API 返回空数据');
      return { data: null };
    }
    
    // 标准响应，直接返回
    return response;
  }).catch(error => {
    console.error('获取职位详情请求失败:', error);
    return { data: null }; // 返回空数据而不是抛出错误
  });
}

// 根据职位ID获取公司信息
export function getCompanyByJobId(id) {
  return request({
    url: `/jobs/${id}/company`,
    method: 'get'
  }).then(response => {
    console.log('根据职位ID获取公司信息原始API响应:', response);
    
    // 检查响应是否为空
    if (!response) {
      console.error('获取公司信息API响应为空');
      return { 
        data: {
          company: {
            id: null,
            companyName: '未知公司',
            logo: '',
            description: '',
            industry: '',
            scale: '',
            verified: false
          },
          job: null
        }
      };
    }
    
    // 如果响应中已包含 data 字段但其中的 company 为 null
    if (response.data && response.data.company === null) {
      response.data.company = {
        id: null,
        companyName: '未知公司',
        logo: '',
        description: '',
        industry: '',
        scale: '',
        verified: false
      };
    }
    
    // 标准响应或其他情况，直接返回
    return response;
  }).catch(error => {
    console.error('获取公司信息请求失败:', error);
    return { 
      data: {
        company: {
          id: null,
          companyName: '未知公司',
          logo: '',
          description: '',
          industry: '',
          scale: '',
          verified: false
        },
        job: null
      }
    };
  });
}

// 搜索职位
export function searchJobs(params) {
  return request({
    url: '/jobs/search',
    method: 'get',
    params
  }).then(response => {
    console.log('搜索API原始响应:', response);
    
    // 检查响应是否为空
    if (!response) {
      console.error('搜索API响应为空');
      return { data: [] };
    }
    
    // 如果响应是数组，则包装在预期格式中
    if (Array.isArray(response)) {
      console.log('搜索API返回了数组格式');
      return { 
        data: response,
        totalElements: response.length 
      };
    }
    
    // 如果响应中没有data字段，而是直接返回了内容
    if (!response.data && (response.content || Array.isArray(response))) {
      console.log('搜索API返回了非标准格式');
      const content = response.content || response;
      return {
        data: {
          content: content,
          totalElements: content.length || response.totalElements || 0
        }
      };
    }
    
    // 标准响应，直接返回
    return response;
  }).catch(error => {
    console.error('搜索职位请求失败:', error);
    return { data: [] }; // 返回空数据而不是抛出错误
  });
}

// 获取企业职位列表
export function getCompanyJobs(companyId) {
  return request({
    url: `/jobs/company/${companyId}`,
    method: 'get'
  }).then(response => {
    console.log('企业职位列表原始API响应:', response);
    
    // 检查响应是否为空
    if (!response) {
      console.error('企业职位列表API响应为空');
      return { 
        data: {
          companyName: '未知公司',
          companyLogo: '',
          description: '',
          industry: '',
          scale: '',
          verified: false,
          content: []
        }
      };
    }
    
    // 如果响应是数组，则包装在预期格式中
    if (Array.isArray(response)) {
      console.log('企业职位列表API返回了数组格式');
      return { 
        data: {
          companyName: response[0]?.companyName || '未知公司',
          companyLogo: response[0]?.companyLogo || '',
          description: '',
          industry: response[0]?.industry || '',
          scale: response[0]?.companyScale || '',
          verified: response[0]?.companyVerified || false,
          content: response
        }
      };
    }
    
    // 如果响应中已包含 content 字段
    if (response.content) {
      console.log('企业职位列表API返回了带content字段的格式');
      return {
        data: {
          companyName: response.companyName || '未知公司',
          companyLogo: response.companyLogo || '',
          description: response.description || '',
          industry: response.industry || '',
          scale: response.scale || '',
          verified: response.verified || false,
          content: response.content
        }
      };
    }
    
    // 如果响应中只有 data 字段，但没有 content 字段
    if (response.data && !response.data.content && Array.isArray(response.data)) {
      console.log('企业职位列表API返回了data数组格式');
      return {
        data: {
          companyName: response.data[0]?.companyName || '未知公司',
          companyLogo: response.data[0]?.companyLogo || '',
          description: '',
          industry: response.data[0]?.industry || '',
          scale: response.data[0]?.companyScale || '',
          verified: response.data[0]?.companyVerified || false,
          content: response.data
        }
      };
    }
    
    // 标准响应，直接返回
    // 如果 response.data.content 存在，确保返回格式正确
    if (response.data && response.data.content) {
      // 确保返回的数据结构包含所有必要字段
      if (!response.data.companyName) {
        response.data.companyName = response.data.content[0]?.companyName || '未知公司';
        response.data.companyLogo = response.data.content[0]?.companyLogo || '';
        response.data.industry = response.data.content[0]?.industry || '';
        response.data.scale = response.data.content[0]?.companyScale || '';
        response.data.verified = response.data.content[0]?.companyVerified || false;
        response.data.description = '';
      }
    }
    
    return response;
  }).catch(error => {
    console.error('获取企业职位列表请求失败:', error);
    return { 
      data: {
        companyName: '未知公司',
        companyLogo: '',
        description: '',
        industry: '',
        scale: '',
        verified: false,
        content: []
      }
    };
  });
}

// 收藏职位
export function favoriteJob(jobId) {
  return request({
    url: `/jobs/${jobId}/favorite`,
    method: 'post'
  })
}

// 取消收藏
export function unfavoriteJob(jobId) {
  return request({
    url: `/jobs/${jobId}/favorite`, 
    method: 'delete'
  })
}

// 获取收藏状态
export function getFavoriteStatus(jobId) {
  return request({
    url: `/jobs/${jobId}/favorite`,
    method: 'get'
  })
}

// 获取收藏列表
export function getFavorites() {
  return request({
    url: '/jobs/favorites',
    method: 'get'
  })
}

// 投递职位
export function applyJob(jobId, resumeId, coverLetter) {
  return request({
    url: `/jobs/${jobId}/apply`,
    method: 'post',
    data: {
      resumeId,
      coverLetter
    }
  })
}

// 获取对话消息
export function getConversationMessages(conversationId) {
  return request({
    url: `/conversations/${conversationId}/messages`,
    method: 'get'
  })
}

// 获取投递记录
export function getApplications(params) {
  return request({
    url: '/jobs/applications',
    method: 'get',
    params: {
      ...params,
      includeJobInfo: true, // 包含职位信息
      includeCompanyInfo: true // 包含公司信息
    }
  })
}

// 撤回投递
export function withdrawApplication(applicationId) {
  return request({
    url: `/jobs/applications/${applicationId}/withdraw`,
    method: 'put'
  })
}

// 确保导出所有需要的函数
export default {
  getJobs,
  getJobDetail,
  getCompanyByJobId,
  searchJobs,
  getCompanyJobs,
  favoriteJob,
  unfavoriteJob,
  getFavoriteStatus,
  getFavorites,
  applyJob,
  getConversationMessages,
  getApplications,
  withdrawApplication
}
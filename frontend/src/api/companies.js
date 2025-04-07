import request from '../utils/request'

// 获取企业列表
export function getCompanies(params) {
  return request({
    url: '/companies',
    method: 'get',
    params
  }).then(response => {
    console.log('企业列表原始API响应:', response);
    
    // 检查响应是否为空
    if (!response) {
      console.error('企业列表API响应为空');
      return { data: [] };
    }
    
    // 如果响应是数组，则包装在预期格式中
    if (Array.isArray(response)) {
      console.log('企业列表API返回了数组格式');
      return { 
        data: response,
        totalElements: response.length 
      };
    }
    
    // 如果响应中没有data字段，而是直接返回了内容
    if (!response.data && (response.content || Array.isArray(response))) {
      console.log('企业列表API返回了非标准格式');
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
    console.error('获取企业列表请求失败:', error);
    return { data: [] }; // 返回空数据而不是抛出错误
  });
}

// 搜索企业
export function searchCompanies(params) {
  return request({
    url: '/companies/search',
    method: 'get',
    params
  }).then(response => {
    console.log('搜索企业原始API响应:', response);
    
    // 检查响应是否为空
    if (!response) {
      console.error('搜索企业API响应为空');
      return { data: [] };
    }
    
    // 如果响应是数组，则包装在预期格式中
    if (Array.isArray(response)) {
      console.log('搜索企业API返回了数组格式');
      return { 
        data: response,
        totalElements: response.length 
      };
    }
    
    // 如果响应中没有data字段，而是直接返回了内容
    if (!response.data && (response.content || Array.isArray(response))) {
      console.log('搜索企业API返回了非标准格式');
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
    console.error('搜索企业请求失败:', error);
    return { data: [] }; // 返回空数据而不是抛出错误
  });
}

export default {
  getCompanies,
  searchCompanies
}
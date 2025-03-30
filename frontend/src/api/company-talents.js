import request from '@/utils/request'

// 获取企业人才库列表
export function getCompanyTalents(params) {
  return request({
    url: '/company/talents',
    method: 'get',
    params
  })
}

// 获取人才详情
export function getTalentDetail(talentId) {
  return request({
    url: `/company/talents/${talentId}`,
    method: 'get'
  })
}

// 添加人才到人才库
export function addTalent(data) {
  return request({
    url: '/company/talents',
    method: 'post',
    data
  })
}

// 从简历添加到人才库
export function addResumeToTalents(resumeId) {
  return request({
    url: '/company/talents/from-resume',
    method: 'post',
    data: { resumeId }
  })
}

// 更新人才标签
export function updateTalentTags(talentId, tags) {
  return request({
    url: `/company/talents/${talentId}/tags`,
    method: 'put',
    data: { tags }
  })
}

// 更新人才备注
export function updateTalentNotes(talentId, notes) {
  return request({
    url: `/company/talents/${talentId}/notes`,
    method: 'put',
    data: { notes }
  })
}

// 删除人才
export function deleteTalent(talentId) {
  return request({
    url: `/company/talents/${talentId}`,
    method: 'delete'
  })
}

export default {
  getCompanyTalents,
  getTalentDetail,
  addTalent,
  addResumeToTalents,
  updateTalentTags,
  updateTalentNotes,
  deleteTalent
} 
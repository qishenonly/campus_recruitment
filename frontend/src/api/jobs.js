import request from '../utils/request'

export function getJobs(params) {
  return request({
    url: '/jobs',
    method: 'get',
    params
  })
}
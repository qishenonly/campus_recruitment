import request from '@/utils/request'
import { expandKeys } from 'element-plus/es/components/table-v2/src/common.mjs'

// 上传简历
export function uploadResume(data) {
  return request({
    url: '/resume-parse/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取简历信息
export function getResume() {
  return request({
    url: '/resumes',
    method: 'get'
  })
}

// 删除简历
export function deleteResume() {
  return request({
    url: '/resumes',
    method: 'delete'
  })
}

// 获取简历PDF
export function getResumePDF() {
  return request({
    url: '/resumes/pdf',
    method: 'get',
    responseType: 'arraybuffer',
    headers: {
        'Accept': 'application/pdf'
      }
  })
}

// 
export function getResumePDFById(id ) {
  return request({
    url: '/resumes/pdfById',
    method: 'get',
    responseType: 'arraybuffer',
    headers: {
        'Accept': 'application/pdf'
      },
    params: {
      userId:id
    }
  })
}

export default {
  uploadResume,
  getResume,
  deleteResume,
  getResumePDF,
  getResumePDFById
}
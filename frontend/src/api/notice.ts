import request from './request'

export interface NoticeItem {
  id: number
  tag: string
  tagType: 'primary' | 'success' | 'warning' | 'info' | 'danger'
  title: string
  summary?: string
  publishDate: string
  pinned: boolean
  path?: string
}

export interface NoticePageResult {
  records: NoticeItem[]
  total: number
  page: number
  size: number
  hasMore: boolean
}

export const getNoticePage = (page = 1, size = 6) => {
  return request.get<NoticePageResult>('/notice/page', {
    params: { page, size }
  })
}


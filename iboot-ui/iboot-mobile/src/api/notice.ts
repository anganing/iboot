import type { PageResult } from '@/http/types'
import { http } from '@/http/http'

export interface NoticeItem {
  id: number
  title: string
  type: number
  content: string
  createTime: number | string
}

/** 获取当前用户可见的公告，后端已按启用状态过滤。 */
export function getMyNoticePage(pageNo = 1, pageSize = 5) {
  return http.get<PageResult<NoticeItem>>('/system/notice/my-page', { pageNo, pageSize })
}

/** 获取当前用户可见公告的完整富文本内容。 */
export function getMyNotice(id: number) {
  return http.get<NoticeItem>('/system/notice/get-my', { id })
}

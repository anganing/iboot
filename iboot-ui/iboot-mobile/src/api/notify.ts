import { http } from '@/http/http'

/** 站内信列表项 */
export interface NotifyMessageItem {
  id: number
  /** 发送人名称 */
  templateNickname: string
  /** 消息内容 */
  templateContent: string
  /** 是否已读，false=未读 */
  readStatus: boolean
  createTime: string
  readTime: string | null
}

/** 站内信分页返回结构 */
export interface NotifyMessagePageResult {
  list: NotifyMessageItem[]
  total: number
}

/** 我的站内信分页查询参数 */
export interface MyNotifyMessagePageParams {
  pageNo: number
  pageSize: number
  /** 不传查全部，传 false 查未读 */
  readStatus?: boolean
}

/** 分页查询我的站内信 */
export async function getMyNotifyMessagePage(params: MyNotifyMessagePageParams): Promise<NotifyMessagePageResult> {
  return http.get<NotifyMessagePageResult>('/system/notify-message/my-page', params)
}

/** 标记站内信已读 */
export function updateNotifyMessageRead(ids: number[]) {
  return http.put<void>('/system/notify-message/update-read', undefined, { ids: ids.join(',') })
}

/** 全部标记已读 */
export function updateAllNotifyMessageRead() {
  return http.put<void>('/system/notify-message/update-all-read')
}

/** 获取未读站内信数量 */
export function getUnreadNotifyCount() {
  return http.get<number>('/system/notify-message/get-unread-count')
}

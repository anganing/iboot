import type { PageResult } from '@/http/types'
import { http } from '@/http/http'

export interface BpmUser {
  id: number
  nickname: string
  avatar?: string
}

export interface BpmSummaryItem {
  key: string
  value: string
}

export interface BpmTaskItem {
  id: string
  name: string
  createTime: string
  endTime?: string
  reason?: string
  processInstanceId: string
  processInstance?: {
    id: string
    name: string
    createTime: string
    startUser?: BpmUser
    summary?: BpmSummaryItem[]
  }
}

export interface BpmCopyItem {
  id: number
  processInstanceId: string
  processInstanceName: string
  processInstanceStartTime: string
  activityId?: string
  activityName?: string
  taskId?: string
  reason?: string
  startUser?: BpmUser
  createTime: string
  summary?: BpmSummaryItem[]
}

export interface BpmApprovalNodeTask {
  id: string
  status: number
  reason?: string
  assigneeUser?: BpmUser
}

export interface BpmApprovalNode {
  id: string
  name: string
  status: number
  startTime?: string
  endTime?: string
  tasks?: BpmApprovalNodeTask[]
}

export interface BpmApprovalDetail {
  status: number
  activityNodes: BpmApprovalNode[]
  processInstance?: {
    id: string
    name: string
    createTime: string
    startUser?: BpmUser
    summary?: BpmSummaryItem[]
  }
}

interface BpmPageParams {
  pageNo: number
  pageSize: number
}

export function getTaskTodoPage(params: BpmPageParams) {
  return http.get<PageResult<BpmTaskItem>>('/bpm/task/todo-page', params)
}

export function getTaskDonePage(params: BpmPageParams) {
  return http.get<PageResult<BpmTaskItem>>('/bpm/task/done-page', params)
}

export function getTaskTodoCount() {
  return http.get<number>('/bpm/task/todo-count')
}

export function getProcessInstanceCopyPage(params: BpmPageParams) {
  return http.get<PageResult<BpmCopyItem>>('/bpm/process-instance/copy/page', params)
}

export function getApprovalDetail(params: { activityId?: string, processInstanceId: string, taskId?: string }) {
  return http.get<BpmApprovalDetail>('/bpm/process-instance/get-approval-detail', params)
}

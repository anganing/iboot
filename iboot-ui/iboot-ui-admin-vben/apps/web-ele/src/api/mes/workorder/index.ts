import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MesWorkOrderApi {
  /** MES-作业工单信息 */
  export interface WorkOrder {
    id: number; // 主键
    workOrderNo?: string; // 作业工单号
    prodOrderId?: number; // 生产工单编号
    prodOrderNo?: string; // 生产工单号快照
    plantId?: number; // 工厂编号
    workshopId?: number; // 车间编号
    wcId?: number; // 工作中心编号
    projectId?: number; // 项目编号，0表示无项目
    projectTaskId?: number; // 项目任务编号，0表示无项目任务
    plannedQty?: number; // 计划数量
    completedQty?: number; // 已完成数量
    plannedStartTime: string | Dayjs; // 计划开始时间
    plannedEndTime: string | Dayjs; // 计划结束时间
    actualStartTime: string | Dayjs; // 实际开始时间
    actualEndTime: string | Dayjs; // 实际结束时间
    kitStatus?: string; // 齐套状态
    status?: string; // 状态：PENDING、READY、RUNNING、HOLD、DONE、CANCELLED
    version?: number; // 乐观锁版本
    remark: string; // 备注
  }
}

/** 查询MES-作业工单分页 */
export function getWorkOrderPage(params: PageParam) {
  return requestClient.get<PageResult<MesWorkOrderApi.WorkOrder>>(
    '/mes/work-order/page',
    { params },
  );
}

/** 查询MES-作业工单详情 */
export function getWorkOrder(id: number) {
  return requestClient.get<MesWorkOrderApi.WorkOrder>(
    `/mes/work-order/get?id=${id}`,
  );
}

/** 新增MES-作业工单 */
export function createWorkOrder(data: MesWorkOrderApi.WorkOrder) {
  return requestClient.post('/mes/work-order/create', data);
}

/** 修改MES-作业工单 */
export function updateWorkOrder(data: MesWorkOrderApi.WorkOrder) {
  return requestClient.put('/mes/work-order/update', data);
}

/** 删除MES-作业工单 */
export function deleteWorkOrder(id: number) {
  return requestClient.delete(`/mes/work-order/delete?id=${id}`);
}

/** 批量删除MES-作业工单 */
export function deleteWorkOrderList(ids: number[]) {
  return requestClient.delete(
    `/mes/work-order/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出MES-作业工单 */
export function exportWorkOrder(params: any) {
  return requestClient.download('/mes/work-order/export-excel', { params });
}

/** 下载MES-作业工单导入模板 */
export function importWorkOrderTemplate() {
  return requestClient.download('/mes/work-order/get-import-template');
}

/** 导入MES-作业工单 */
export function importWorkOrder(file: File) {
  return requestClient.upload('/mes/work-order/import', { file });
}


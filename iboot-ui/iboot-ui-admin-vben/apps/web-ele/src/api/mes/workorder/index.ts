import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MesWorkOrderApi {
  /**
   * MES-作业工单。
   *
   * 生产工单号、工厂、车间、项目维度都由后端从生产工单继承；已完成数量由工序任务推进；
   * 齐套状态由齐套检查写入；实际起止时间由业务动作写入。这些字段前端只读。
   */
  export interface WorkOrder {
    id?: number;
    workOrderNo?: string;
    prodOrderId?: number;
    prodOrderNo?: string;
    matCode?: string;
    matName?: string;
    plantId?: number;
    plantName?: string;
    workshopId?: number;
    workshopName?: string;
    wcId?: number;
    wcName?: string;
    projectId?: number;
    projectName?: string;
    projectTaskId?: number;
    projectTaskName?: string;
    plannedQty?: number;
    completedQty?: number;
    plannedStartTime?: number | string;
    plannedEndTime?: number | string;
    actualStartTime?: string;
    actualEndTime?: string;
    kitStatus?: string;
    status?: string;
    version?: number;
    remark?: string;
    createTime?: string;
  }
}

/** 查询作业工单分页 */
export function getWorkOrderPage(params: PageParam) {
  return requestClient.get<PageResult<MesWorkOrderApi.WorkOrder>>(
    '/mes/work-order/page',
    { params },
  );
}

/** 查询作业工单详情 */
export function getWorkOrder(id: number) {
  return requestClient.get<MesWorkOrderApi.WorkOrder>(
    `/mes/work-order/get?id=${id}`,
  );
}

/** 导出作业工单 */
export function exportWorkOrder(params: any) {
  return requestClient.download('/mes/work-order/export-excel', { params });
}

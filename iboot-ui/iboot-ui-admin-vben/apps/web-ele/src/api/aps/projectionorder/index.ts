import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace ApsProjectionOrderApi {
  /** APS-MES需求工单任务投影信息 */
  export interface ProjectionOrder {
    id: number; // 主键
    scenarioId?: number; // 排程场景编号
    scenarioCode?: string; // 排程场景编码
    scenarioName?: string; // 排程场景名称
    sourceType?: string; // 来源类型：DEMAND、PROD_ORDER、OP_TASK
    sourceId?: number; // MES来源对象编号
    sourceNo?: string; // MES来源对象单号
    prodOrderId?: number; // 生产工单编号，0表示尚未生成工单
    workOrderId?: number; // 作业工单编号，0表示尚未生成作业工单
    opTaskId?: number; // 工序任务编号，0表示非工序投影
    plantId?: number; // 工厂编号
    workshopId?: number; // 车间编号，0表示未指定
    projectId?: number; // 项目编号，0表示无项目
    projectTaskId?: number; // 项目任务编号，0表示无项目任务
    matId?: number; // 生产物料编号
    matCode?: string; // 物料编码快照
    matName?: string; // 物料名称
    routeOpId?: number; // 路线工序编号，0表示需求或整单
    resId?: number; // 当前资源编号，0表示未分配
    resName?: string; // 当前资源名称
    qty?: number; // 计划数量或剩余数量
    completedQty?: number; // 已完成数量快照
    dueDate?: string | Dayjs; // 交期
    priority?: number; // 优先级
    currentStatus?: string; // 来源对象状态快照
    actualStartTime: string | Dayjs; // 实际开始时间
    actualEndTime: string | Dayjs; // 实际结束时间
    lockedFlag?: boolean; // 是否已开工或业务锁定
    sourceUpdateTime?: string | Dayjs; // 来源数据更新时间
    snapshotTime?: string | Dayjs; // 投影快照时间
    remark?: string; // 备注
  }
}

/** 查询APS-MES需求工单任务投影分页 */
export function getProjectionOrderPage(params: PageParam) {
  return requestClient.get<PageResult<ApsProjectionOrderApi.ProjectionOrder>>(
    '/aps/projection-order/page',
    { params },
  );
}

/** 查询APS-MES需求工单任务投影详情 */
export function getProjectionOrder(id: number) {
  return requestClient.get<ApsProjectionOrderApi.ProjectionOrder>(
    `/aps/projection-order/get?id=${id}`,
  );
}

/** 导出APS-MES需求工单任务投影 */
export function exportProjectionOrder(params: any) {
  return requestClient.download('/aps/projection-order/export-excel', { params });
}



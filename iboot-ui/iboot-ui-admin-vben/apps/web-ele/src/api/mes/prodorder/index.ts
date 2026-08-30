import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MesProdOrderApi {
  /** MES-生产工单信息 */
  export interface ProdOrder {
    id: number; // 主键
    orderNo?: string; // 生产工单号
    demandId: number; // 生产需求编号
    plantId?: number; // 工厂编号
    workshopId?: number; // 车间编号
    projectId?: number; // 项目编号，0表示无项目
    projectTaskId?: number; // 项目任务编号，0表示无项目任务
    matId?: number; // 生产物料编号
    matCode?: string; // 物料编码快照
    matName?: string; // 物料名称快照
    matSpec: string; // 物料规格快照
    qty?: number; // 计划生产数量
    completedQty?: number; // 已完成数量
    scrapQty?: number; // 累计报废数量
    uomId?: number; // 计量单位编号
    uomCode?: string; // 计量单位编码快照
    bomId?: number; // 锁定的BOM编号
    bomVersion?: string; // 锁定的BOM版本
    routeId?: number; // 锁定的工艺路线编号
    routeVersion?: string; // 锁定的工艺路线版本
    dueDate?: string | Dayjs; // 工单交期
    priority?: number; // 优先级
    kitStatus?: string; // 齐套状态：UNCHECKED、FULL_KIT、PARTIAL_KIT、SHORTAGE
    status?: string; // 状态：DRAFT、RELEASED、READY、RUNNING、HOLD、DONE、CLOSED、CANCELLED
    releaseTime: string | Dayjs; // 下达时间
    actualStartTime: string | Dayjs; // 实际开始时间
    actualEndTime: string | Dayjs; // 实际结束时间
    version?: number; // 乐观锁版本
    remark: string; // 备注
  }
}

/** 查询MES-生产工单分页 */
export function getProdOrderPage(params: PageParam) {
  return requestClient.get<PageResult<MesProdOrderApi.ProdOrder>>(
    '/mes/prod-order/page',
    { params },
  );
}

/** 查询MES-生产工单详情 */
export function getProdOrder(id: number) {
  return requestClient.get<MesProdOrderApi.ProdOrder>(
    `/mes/prod-order/get?id=${id}`,
  );
}

/** 新增MES-生产工单 */
export function createProdOrder(data: MesProdOrderApi.ProdOrder) {
  return requestClient.post('/mes/prod-order/create', data);
}

/** 修改MES-生产工单 */
export function updateProdOrder(data: MesProdOrderApi.ProdOrder) {
  return requestClient.put('/mes/prod-order/update', data);
}

/** 删除MES-生产工单 */
export function deleteProdOrder(id: number) {
  return requestClient.delete(`/mes/prod-order/delete?id=${id}`);
}

/** 批量删除MES-生产工单 */
export function deleteProdOrderList(ids: number[]) {
  return requestClient.delete(
    `/mes/prod-order/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出MES-生产工单 */
export function exportProdOrder(params: any) {
  return requestClient.download('/mes/prod-order/export-excel', { params });
}

/** 下载MES-生产工单导入模板 */
export function importProdOrderTemplate() {
  return requestClient.download('/mes/prod-order/get-import-template');
}

/** 导入MES-生产工单 */
export function importProdOrder(file: File) {
  return requestClient.upload('/mes/prod-order/import', { file });
}


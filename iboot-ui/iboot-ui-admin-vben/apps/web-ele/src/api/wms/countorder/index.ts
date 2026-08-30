import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace WmsCountOrderApi {
  /** 盘点明细信息 */
  export interface CountOrderLine {
            id: number; // 主键
            orderId?: number; // 盘点单编号
            lineNo?: number; // 行号
            balanceId?: number; // 库存余额编号
            matId?: number; // 物料编号
            matCode?: string; // 物料编码快照
            matName?: string; // 物料名称快照
            batchId?: number; // 批次编号
            lotNo?: string; // 批次号快照
            locId?: number; // 库位编号
            locCode?: string; // 库位编码快照
            containerId?: number; // 容器编号，0表示散件
            qualityStatus?: string; // 质量状态
            projectId?: number; // 项目编号，0表示无项目
            projectTaskId?: number; // 项目任务编号，0表示无项目任务
            bookQty?: number; // 账面数量
            actualQty: number; // 实盘数量
            diffQty: number; // 差异数量
            adjustBizNo: string; // 差异调整入出库单号
            status?: string; // 明细状态：PENDING、COUNTED、DIFF、ADJUSTED
            remark: string; // 备注
  }

  /** 盘点单信息 */
  export interface CountOrder {
    id: number; // 主键
    countNo?: string; // 盘点单号
    countType?: string; // 盘点类型：FULL、SAMPLE、ABC_CYCLE、DYNAMIC
    invOrgId?: number; // 库存组织编号
    whId?: number; // 仓库编号
    zoneId?: number; // 库区编号，0表示全部库区
    projectId?: number; // 项目编号，0表示不限项目
    snapshotTime?: string | Dayjs; // 账面快照时间
    blindCountFlag?: boolean; // 是否盲盘
    status?: string; // 状态：DRAFT、FROZEN、COUNTING、DIFF_CONFIRMED、DONE、CANCELLED
    idempotencyKey: string; // 创建幂等键
    remark: string; // 备注
  }
}

/** 查询盘点单分页 */
export function getCountOrderPage(params: PageParam) {
  return requestClient.get<PageResult<WmsCountOrderApi.CountOrder>>(
    '/wms/count-order/page',
    { params },
  );
}

/** 查询盘点单详情 */
export function getCountOrder(id: number) {
  return requestClient.get<WmsCountOrderApi.CountOrder>(
    `/wms/count-order/get?id=${id}`,
  );
}

/** 新增盘点单 */
export function createCountOrder(data: WmsCountOrderApi.CountOrder) {
  return requestClient.post('/wms/count-order/create', data);
}

/** 修改盘点单 */
export function updateCountOrder(data: WmsCountOrderApi.CountOrder) {
  return requestClient.put('/wms/count-order/update', data);
}

/** 删除盘点单 */
export function deleteCountOrder(id: number) {
  return requestClient.delete(`/wms/count-order/delete?id=${id}`);
}

/** 批量删除盘点单 */
export function deleteCountOrderList(ids: number[]) {
  return requestClient.delete(
    `/wms/count-order/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出盘点单 */
export function exportCountOrder(params: any) {
  return requestClient.download('/wms/count-order/export-excel', { params });
}

/** 下载盘点单导入模板 */
export function importCountOrderTemplate() {
  return requestClient.download('/wms/count-order/get-import-template');
}

/** 导入盘点单 */
export function importCountOrder(file: File) {
  return requestClient.upload('/wms/count-order/import', { file });
}


// ==================== 子表（盘点明细） ====================

/** 获得盘点明细分页 */
export function getCountOrderLinePage(params: PageParam) {
  return requestClient.get<PageResult<WmsCountOrderApi.CountOrderLine>>(
    `/wms/count-order/count-order-line/page`,
    { params },
  );
}
/** 新增盘点明细 */
export function createCountOrderLine(data: WmsCountOrderApi.CountOrderLine) {
  return requestClient.post(`/wms/count-order/count-order-line/create`, data);
}

/** 修改盘点明细 */
export function updateCountOrderLine(data: WmsCountOrderApi.CountOrderLine) {
  return requestClient.put(`/wms/count-order/count-order-line/update`, data);
}

/** 删除盘点明细 */
export function deleteCountOrderLine(id: number) {
  return requestClient.delete(`/wms/count-order/count-order-line/delete?id=${id}`);
}

/** 批量删除盘点明细 */
export function deleteCountOrderLineList(ids: number[]) {
  return requestClient.delete(
    `/wms/count-order/count-order-line/delete-list?ids=${ids.join(',')}`,
  );
}

/** 获得盘点明细 */
export function getCountOrderLine(id: number) {
  return requestClient.get<WmsCountOrderApi.CountOrderLine>(
    `/wms/count-order/count-order-line/get?id=${id}`,
  );
}

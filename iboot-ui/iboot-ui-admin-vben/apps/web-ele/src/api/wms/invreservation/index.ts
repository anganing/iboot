import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace WmsInvReservationApi {
  /** WMS-库存预留信息 */
  export interface InvReservation {
    id: number; // 主键
    reservationNo?: string; // 预留编号
    bizType?: string; // 来源业务类型
    bizNo?: string; // 来源业务单号
    bizLineNo: string; // 来源业务行号
    invOrgId?: number; // 库存组织编号
    whId?: number; // 仓库编号
    locId?: number; // 库位编号
    matId?: number; // 物料编号
    batchId?: number; // 批次编号
    containerId?: number; // 容器编号，0表示散件
    projectId?: number; // 项目编号，0表示无项目
    projectTaskId?: number; // 项目任务编号，0表示无项目任务
    qualityStatus?: string; // 质量状态
    qty?: number; // 预留数量，基准单位
    releasedQty?: number; // 已释放或已核销数量
    status?: string; // 状态：ACTIVE、PARTIAL、RELEASED、CONSUMED、CANCELLED
    expireTime: string | Dayjs; // 预留失效时间
    idempotencyKey?: string; // 预留幂等键
    remark: string; // 备注
  }
}

/** 查询WMS-库存预留分页 */
export function getInvReservationPage(params: PageParam) {
  return requestClient.get<PageResult<WmsInvReservationApi.InvReservation>>(
    '/wms/inv-reservation/page',
    { params },
  );
}

/** 查询WMS-库存预留详情 */
export function getInvReservation(id: number) {
  return requestClient.get<WmsInvReservationApi.InvReservation>(
    `/wms/inv-reservation/get?id=${id}`,
  );
}

/** 新增WMS-库存预留 */
export function createInvReservation(data: WmsInvReservationApi.InvReservation) {
  return requestClient.post('/wms/inv-reservation/create', data);
}

/** 修改WMS-库存预留 */
export function updateInvReservation(data: WmsInvReservationApi.InvReservation) {
  return requestClient.put('/wms/inv-reservation/update', data);
}

/** 删除WMS-库存预留 */
export function deleteInvReservation(id: number) {
  return requestClient.delete(`/wms/inv-reservation/delete?id=${id}`);
}

/** 批量删除WMS-库存预留 */
export function deleteInvReservationList(ids: number[]) {
  return requestClient.delete(
    `/wms/inv-reservation/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出WMS-库存预留 */
export function exportInvReservation(params: any) {
  return requestClient.download('/wms/inv-reservation/export-excel', { params });
}

/** 下载WMS-库存预留导入模板 */
export function importInvReservationTemplate() {
  return requestClient.download('/wms/inv-reservation/get-import-template');
}

/** 导入WMS-库存预留 */
export function importInvReservation(file: File) {
  return requestClient.upload('/wms/inv-reservation/import', { file });
}


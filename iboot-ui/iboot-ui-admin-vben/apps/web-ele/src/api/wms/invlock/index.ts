import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace WmsInvLockApi {
  /** WMS-库存冻结记录信息 */
  export interface InvLock {
    id: number; // 主键
    lockNo?: string; // 冻结编号
    bizType?: string; // 来源业务类型
    bizNo?: string; // 来源业务单号
    invOrgId?: number; // 库存组织编号
    whId?: number; // 仓库编号
    locId?: number; // 库位编号
    matId?: number; // 物料编号
    batchId?: number; // 批次编号
    containerId?: number; // 容器编号，0表示散件
    projectId?: number; // 项目编号，0表示无项目
    projectTaskId?: number; // 项目任务编号，0表示无项目任务
    qualityStatus?: string; // 质量状态
    qty?: number; // 冻结数量，基准单位
    releasedQty?: number; // 已解冻数量
    reason?: string; // 冻结原因
    status?: string; // 状态：ACTIVE、PARTIAL、RELEASED、CANCELLED
    idempotencyKey?: string; // 冻结幂等键
    remark: string; // 备注
  }
}

/** 查询WMS-库存冻结记录分页 */
export function getInvLockPage(params: PageParam) {
  return requestClient.get<PageResult<WmsInvLockApi.InvLock>>(
    '/wms/inv-lock/page',
    { params },
  );
}

/** 查询WMS-库存冻结记录详情 */
export function getInvLock(id: number) {
  return requestClient.get<WmsInvLockApi.InvLock>(
    `/wms/inv-lock/get?id=${id}`,
  );
}

/** 新增WMS-库存冻结记录 */
export function createInvLock(data: WmsInvLockApi.InvLock) {
  return requestClient.post('/wms/inv-lock/create', data);
}

/** 修改WMS-库存冻结记录 */
export function updateInvLock(data: WmsInvLockApi.InvLock) {
  return requestClient.put('/wms/inv-lock/update', data);
}

/** 删除WMS-库存冻结记录 */
export function deleteInvLock(id: number) {
  return requestClient.delete(`/wms/inv-lock/delete?id=${id}`);
}

/** 批量删除WMS-库存冻结记录 */
export function deleteInvLockList(ids: number[]) {
  return requestClient.delete(
    `/wms/inv-lock/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出WMS-库存冻结记录 */
export function exportInvLock(params: any) {
  return requestClient.download('/wms/inv-lock/export-excel', { params });
}

/** 下载WMS-库存冻结记录导入模板 */
export function importInvLockTemplate() {
  return requestClient.download('/wms/inv-lock/get-import-template');
}

/** 导入WMS-库存冻结记录 */
export function importInvLock(file: File) {
  return requestClient.upload('/wms/inv-lock/import', { file });
}


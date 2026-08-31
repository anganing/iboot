import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace WmsInvLockApi {
  /** 库存冻结信息 */
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
    status?: string; // 状态
    idempotencyKey?: string; // 冻结幂等键
    remark?: string; // 备注
    createTime?: string; // 创建时间
    whCode?: string; // 仓库编码
    whName?: string; // 仓库名称
    locCode?: string; // 库位编码
    lotNo?: string; // 批次号
    matCode?: string; // 物料编码
    matName?: string; // 物料名称
  }
}

/** 查询库存冻结分页 */
export function getInvLockPage(params: PageParam) {
  return requestClient.get<PageResult<WmsInvLockApi.InvLock>>(
    '/wms/inv-lock/page',
    { params },
  );
}

/** 查询库存冻结详情 */
export function getInvLock(id: number) {
  return requestClient.get<WmsInvLockApi.InvLock>(
    `/wms/inv-lock/get?id=${id}`,
  );
}

/** 新增库存冻结 */
export function createInvLock(data: WmsInvLockApi.InvLock) {
  return requestClient.post('/wms/inv-lock/create', data);
}

/** 修改库存冻结 */
export function updateInvLock(data: WmsInvLockApi.InvLock) {
  return requestClient.put('/wms/inv-lock/update', data);
}

/** 删除库存冻结 */
export function deleteInvLock(id: number) {
  return requestClient.delete(`/wms/inv-lock/delete?id=${id}`);
}

/** 批量删除库存冻结 */
export function deleteInvLockList(ids: number[]) {
  return requestClient.delete(
    `/wms/inv-lock/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出库存冻结 */
export function exportInvLock(params: any) {
  return requestClient.download('/wms/inv-lock/export-excel', { params });
}

/** 下载库存冻结导入模板 */
export function importInvLockTemplate() {
  return requestClient.download('/wms/inv-lock/get-import-template');
}

/** 导入库存冻结 */
export function importInvLock(file: File) {
  return requestClient.upload('/wms/inv-lock/import', { file });
}



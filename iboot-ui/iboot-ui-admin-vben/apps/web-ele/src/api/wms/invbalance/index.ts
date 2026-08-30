import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace WmsInvBalanceApi {
  /** WMS-多维库存余额信息 */
  export interface InvBalance {
    id: number; // 主键
    invOrgId?: number; // 库存组织编号
    plantId?: number; // 工厂编号
    whId?: number; // 仓库编号
    locId?: number; // 库位编号
    matId?: number; // 物料编号
    batchId?: number; // 批次编号；无批次物料挂默认批次
    containerId?: number; // 容器编号，0表示散件
    projectId?: number; // 项目编号，0表示无项目归属
    projectTaskId?: number; // 项目任务编号，0表示无项目任务归属
    stockScope?: string; // 库存范围：COMMON、PROJECT_DEDICATED
    qualityStatus?: string; // 质量状态：QUALIFIED、QC_PENDING、REJECTED、FROZEN
    qtyOnhand?: number; // 现存数量，基准单位
    qtyReserved?: number; // 预留数量，基准单位
    qtyFrozen?: number; // 冻结数量，基准单位
    matCode?: string; // 物料编码快照
    matName?: string; // 物料名称快照
    matSpec: string; // 物料规格快照
    uomCode?: string; // 基准单位编码快照
    lotNo?: string; // 批次号快照
    whCode?: string; // 仓库编码快照
    locCode?: string; // 库位编码快照
    version?: number; // 乐观锁版本
    remark: string; // 备注
  }
}

/** 查询WMS-多维库存余额分页 */
export function getInvBalancePage(params: PageParam) {
  return requestClient.get<PageResult<WmsInvBalanceApi.InvBalance>>(
    '/wms/inv-balance/page',
    { params },
  );
}

/** 查询WMS-多维库存余额详情 */
export function getInvBalance(id: number) {
  return requestClient.get<WmsInvBalanceApi.InvBalance>(
    `/wms/inv-balance/get?id=${id}`,
  );
}

/** 新增WMS-多维库存余额 */
export function createInvBalance(data: WmsInvBalanceApi.InvBalance) {
  return requestClient.post('/wms/inv-balance/create', data);
}

/** 修改WMS-多维库存余额 */
export function updateInvBalance(data: WmsInvBalanceApi.InvBalance) {
  return requestClient.put('/wms/inv-balance/update', data);
}

/** 删除WMS-多维库存余额 */
export function deleteInvBalance(id: number) {
  return requestClient.delete(`/wms/inv-balance/delete?id=${id}`);
}

/** 批量删除WMS-多维库存余额 */
export function deleteInvBalanceList(ids: number[]) {
  return requestClient.delete(
    `/wms/inv-balance/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出WMS-多维库存余额 */
export function exportInvBalance(params: any) {
  return requestClient.download('/wms/inv-balance/export-excel', { params });
}

/** 下载WMS-多维库存余额导入模板 */
export function importInvBalanceTemplate() {
  return requestClient.download('/wms/inv-balance/get-import-template');
}

/** 导入WMS-多维库存余额 */
export function importInvBalance(file: File) {
  return requestClient.upload('/wms/inv-balance/import', { file });
}


import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MesKitSnapshotApi {
  /** MES-齐套检查快照信息 */
  export interface KitSnapshot {
    id: number; // 主键
    checkNo?: string; // 齐套检查编号
    checkScope?: string; // 检查范围：PROD_ORDER、WORK_ORDER、OP_TASK
    prodOrderId?: number; // 生产工单编号
    workOrderId?: number; // 作业工单编号，0表示整单检查
    opTaskId?: number; // 工序任务编号，0表示非工序检查
    lineNo?: number; // 检查明细行号
    matId?: number; // 需求物料编号
    matCode?: string; // 物料编码快照
    matName?: string; // 物料名称快照
    reqQty?: number; // 需求数量
    availQty?: number; // 检查时可用数量
    reservedQty?: number; // 检查时本需求已预留数量
    issuedQty?: number; // 检查时已领用数量
    shortQty?: number; // 短缺数量
    altMatId?: number; // 实际采用替代料编号，0表示未采用
    result?: string; // 检查结果：FULL_KIT、PARTIAL_KIT、SHORTAGE
    checkTime?: string | Dayjs; // 检查时间
    remark: string; // 备注
  }
}

/** 查询MES-齐套检查快照分页 */
export function getKitSnapshotPage(params: PageParam) {
  return requestClient.get<PageResult<MesKitSnapshotApi.KitSnapshot>>(
    '/mes/kit-snapshot/page',
    { params },
  );
}

/** 查询MES-齐套检查快照详情 */
export function getKitSnapshot(id: number) {
  return requestClient.get<MesKitSnapshotApi.KitSnapshot>(
    `/mes/kit-snapshot/get?id=${id}`,
  );
}

/** 新增MES-齐套检查快照 */
export function createKitSnapshot(data: MesKitSnapshotApi.KitSnapshot) {
  return requestClient.post('/mes/kit-snapshot/create', data);
}

/** 修改MES-齐套检查快照 */
export function updateKitSnapshot(data: MesKitSnapshotApi.KitSnapshot) {
  return requestClient.put('/mes/kit-snapshot/update', data);
}

/** 删除MES-齐套检查快照 */
export function deleteKitSnapshot(id: number) {
  return requestClient.delete(`/mes/kit-snapshot/delete?id=${id}`);
}

/** 批量删除MES-齐套检查快照 */
export function deleteKitSnapshotList(ids: number[]) {
  return requestClient.delete(
    `/mes/kit-snapshot/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出MES-齐套检查快照 */
export function exportKitSnapshot(params: any) {
  return requestClient.download('/mes/kit-snapshot/export-excel', { params });
}

/** 下载MES-齐套检查快照导入模板 */
export function importKitSnapshotTemplate() {
  return requestClient.download('/mes/kit-snapshot/get-import-template');
}

/** 导入MES-齐套检查快照 */
export function importKitSnapshot(file: File) {
  return requestClient.upload('/mes/kit-snapshot/import', { file });
}


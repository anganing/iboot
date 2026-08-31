import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace WmsBatchApi {
  /** 批次信息 */
  export interface Batch {
    id: number; // 主键
    matId?: number; // 物料编号
    matCode?: string; // 物料编码快照
    matName?: string; // 物料名称快照
    lotNo?: string; // 批次号；无批次物料使用默认批次
    snNo?: string; // 序列号；SN物料以数量为1的批次承载
    supplierId?: number; // 供应商编号
    supplierCode?: string; // 供应商编码快照
    supplierLotNo?: string; // 供应商批次号
    furnaceLotNo?: string; // 炉批号
    produceDate?: string; // 生产日期
    expiryDate?: string; // 失效日期
    status?: number; // 状态
    remark?: string; // 备注
    createTime?: string; // 创建时间
  }
}

/** 查询批次分页 */
export function getBatchPage(params: PageParam) {
  return requestClient.get<PageResult<WmsBatchApi.Batch>>(
    '/wms/batch/page',
    { params },
  );
}

/** 查询批次详情 */
export function getBatch(id: number) {
  return requestClient.get<WmsBatchApi.Batch>(
    `/wms/batch/get?id=${id}`,
  );
}

/** 新增批次 */
export function createBatch(data: WmsBatchApi.Batch) {
  return requestClient.post('/wms/batch/create', data);
}

/** 修改批次 */
export function updateBatch(data: WmsBatchApi.Batch) {
  return requestClient.put('/wms/batch/update', data);
}

/** 删除批次 */
export function deleteBatch(id: number) {
  return requestClient.delete(`/wms/batch/delete?id=${id}`);
}

/** 批量删除批次 */
export function deleteBatchList(ids: number[]) {
  return requestClient.delete(
    `/wms/batch/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出批次 */
export function exportBatch(params: any) {
  return requestClient.download('/wms/batch/export-excel', { params });
}

/** 下载批次导入模板 */
export function importBatchTemplate() {
  return requestClient.download('/wms/batch/get-import-template');
}

/** 导入批次 */
export function importBatch(file: File) {
  return requestClient.upload('/wms/batch/import', { file });
}



import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MesQcResultApi {
  /** MES-检验结果信息 */
  export interface QcResult {
    id: number; // 主键
    taskId?: number; // 质检任务编号
    itemId?: number; // 检验项目编号
    sampleNo?: number; // 样本序号
    itemCode?: string; // 检验项编码快照
    itemName?: string; // 检验项名称快照
    actualValue: string; // 实测值
    passFlag?: boolean; // 是否合格
    inspectorId?: number; // 检验员编号
    inspectTime?: string | Dayjs; // 检验时间
    qtyQualified?: number; // 本项判定合格数量
    qtyRejected?: number; // 本项判定不合格数量
    conclusion?: string; // 项目结论：PASS、FAIL
    remark: string; // 备注
  }
}

/** 查询MES-检验结果分页 */
export function getQcResultPage(params: PageParam) {
  return requestClient.get<PageResult<MesQcResultApi.QcResult>>(
    '/mes/qc-result/page',
    { params },
  );
}

/** 查询MES-检验结果详情 */
export function getQcResult(id: number) {
  return requestClient.get<MesQcResultApi.QcResult>(
    `/mes/qc-result/get?id=${id}`,
  );
}

/** 新增MES-检验结果 */
export function createQcResult(data: MesQcResultApi.QcResult) {
  return requestClient.post('/mes/qc-result/create', data);
}

/** 修改MES-检验结果 */
export function updateQcResult(data: MesQcResultApi.QcResult) {
  return requestClient.put('/mes/qc-result/update', data);
}

/** 删除MES-检验结果 */
export function deleteQcResult(id: number) {
  return requestClient.delete(`/mes/qc-result/delete?id=${id}`);
}

/** 批量删除MES-检验结果 */
export function deleteQcResultList(ids: number[]) {
  return requestClient.delete(
    `/mes/qc-result/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出MES-检验结果 */
export function exportQcResult(params: any) {
  return requestClient.download('/mes/qc-result/export-excel', { params });
}

/** 下载MES-检验结果导入模板 */
export function importQcResultTemplate() {
  return requestClient.download('/mes/qc-result/get-import-template');
}

/** 导入MES-检验结果 */
export function importQcResult(file: File) {
  return requestClient.upload('/mes/qc-result/import', { file });
}


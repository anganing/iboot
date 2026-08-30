import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MesQcPlanApi {
  /** MES-检验方案信息 */
  export interface QcPlan {
    id: number; // 主键
    code?: string; // 检验方案编码
    name?: string; // 检验方案名称
    versionNo?: string; // 方案版本号
    sampleType?: string; // 抽样类型：FULL、FIXED_QTY、PERCENT、AQL
    sampleValue: number; // 固定数量或抽样比例
    aql: string; // AQL接收质量限
    acceptanceRule: string; // 接收判定规则
    status?: string; // 状态：DRAFT、PUBLISHED、DISABLED
    remark: string; // 备注
  }
}

/** 查询MES-检验方案分页 */
export function getQcPlanPage(params: PageParam) {
  return requestClient.get<PageResult<MesQcPlanApi.QcPlan>>(
    '/mes/qc-plan/page',
    { params },
  );
}

/** 查询MES-检验方案详情 */
export function getQcPlan(id: number) {
  return requestClient.get<MesQcPlanApi.QcPlan>(
    `/mes/qc-plan/get?id=${id}`,
  );
}

/** 新增MES-检验方案 */
export function createQcPlan(data: MesQcPlanApi.QcPlan) {
  return requestClient.post('/mes/qc-plan/create', data);
}

/** 修改MES-检验方案 */
export function updateQcPlan(data: MesQcPlanApi.QcPlan) {
  return requestClient.put('/mes/qc-plan/update', data);
}

/** 删除MES-检验方案 */
export function deleteQcPlan(id: number) {
  return requestClient.delete(`/mes/qc-plan/delete?id=${id}`);
}

/** 批量删除MES-检验方案 */
export function deleteQcPlanList(ids: number[]) {
  return requestClient.delete(
    `/mes/qc-plan/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出MES-检验方案 */
export function exportQcPlan(params: any) {
  return requestClient.download('/mes/qc-plan/export-excel', { params });
}

/** 下载MES-检验方案导入模板 */
export function importQcPlanTemplate() {
  return requestClient.download('/mes/qc-plan/get-import-template');
}

/** 导入MES-检验方案 */
export function importQcPlan(file: File) {
  return requestClient.upload('/mes/qc-plan/import', { file });
}


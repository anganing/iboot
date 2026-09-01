import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MesQcPlanApi {
  /** MES-检验方案。已发布的方案会被质检任务锁定引用，只允许改状态与备注。 */
  export interface QcPlan {
    id?: number;
    code?: string;
    name?: string;
    versionNo?: string;
    sampleType?: string;
    sampleValue?: number;
    aql?: string;
    acceptanceRule?: string;
    status?: string;
    remark?: string;
    createTime?: string;
  }
}

/** 查询检验方案分页 */
export function getQcPlanPage(params: PageParam) {
  return requestClient.get<PageResult<MesQcPlanApi.QcPlan>>(
    '/mes/qc-plan/page',
    { params },
  );
}

/** 查询检验方案详情 */
export function getQcPlan(id: number) {
  return requestClient.get<MesQcPlanApi.QcPlan>(`/mes/qc-plan/get?id=${id}`);
}

/** 新增检验方案 */
export function createQcPlan(data: MesQcPlanApi.QcPlan) {
  return requestClient.post('/mes/qc-plan/create', data);
}

/** 修改检验方案 */
export function updateQcPlan(data: MesQcPlanApi.QcPlan) {
  return requestClient.put('/mes/qc-plan/update', data);
}

/** 删除检验方案 */
export function deleteQcPlan(id: number) {
  return requestClient.delete(`/mes/qc-plan/delete?id=${id}`);
}

/** 批量删除检验方案 */
export function deleteQcPlanList(ids: number[]) {
  return requestClient.delete(`/mes/qc-plan/delete-list?ids=${ids.join(',')}`);
}

/** 导出检验方案 */
export function exportQcPlan(params: any) {
  return requestClient.download('/mes/qc-plan/export-excel', { params });
}

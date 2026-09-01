import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MesQcRuleApi {
  /**
   * MES-质检触发规则。
   *
   * 物料、工序、工作中心名称由 Service 走 mdm 门面补全；方案编码与名称由模块内 JOIN 带出。
   */
  export interface QcRule {
    id?: number;
    code?: string;
    name?: string;
    triggerPoint?: string;
    objectType?: string;
    matId?: number;
    matName?: string;
    matCatId?: number;
    opId?: number;
    opName?: string;
    wcId?: number;
    wcName?: string;
    supplierId?: number;
    qcPlanId?: number;
    qcPlanCode?: string;
    qcPlanName?: string;
    qcMode?: string;
    priority?: number;
    status?: number;
    remark?: string;
    createTime?: string;
  }
}

/** 查询质检触发规则分页 */
export function getQcRulePage(params: PageParam) {
  return requestClient.get<PageResult<MesQcRuleApi.QcRule>>(
    '/mes/qc-rule/page',
    { params },
  );
}

/** 查询质检触发规则详情 */
export function getQcRule(id: number) {
  return requestClient.get<MesQcRuleApi.QcRule>(`/mes/qc-rule/get?id=${id}`);
}

/** 新增质检触发规则 */
export function createQcRule(data: MesQcRuleApi.QcRule) {
  return requestClient.post('/mes/qc-rule/create', data);
}

/** 修改质检触发规则 */
export function updateQcRule(data: MesQcRuleApi.QcRule) {
  return requestClient.put('/mes/qc-rule/update', data);
}

/** 删除质检触发规则 */
export function deleteQcRule(id: number) {
  return requestClient.delete(`/mes/qc-rule/delete?id=${id}`);
}

/** 批量删除质检触发规则 */
export function deleteQcRuleList(ids: number[]) {
  return requestClient.delete(`/mes/qc-rule/delete-list?ids=${ids.join(',')}`);
}

/** 导出质检触发规则 */
export function exportQcRule(params: any) {
  return requestClient.download('/mes/qc-rule/export-excel', { params });
}

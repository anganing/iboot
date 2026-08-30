import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MesQcRuleApi {
  /** MES-质检触发规则信息 */
  export interface QcRule {
    id: number; // 主键
    code?: string; // 质检规则编码
    name?: string; // 质检规则名称
    triggerPoint?: string; // 触发点：IQC、IPQC、FQC、OQC、FAI
    objectType?: string; // 检验对象类型：MATERIAL、BATCH、OP_TASK、PROD_ORDER、OUT_ORDER
    matId?: number; // 指定物料编号，0表示不限
    matCatId?: number; // 指定物料分类编号，0表示不限
    opId?: number; // 指定工序编号，0表示不限
    wcId?: number; // 指定工作中心编号，0表示不限
    supplierId?: number; // 指定供应商编号，0表示不限
    qcPlanId?: number; // 检验方案编号
    qcMode?: string; // 检验模式：PRE_INBOUND、POST_INBOUND_PENDING、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE
    priority?: number; // 优先级，数值越大越优先
    status?: number; // 状态：0-启用，1-停用
    remark: string; // 备注
  }
}

/** 查询MES-质检触发规则分页 */
export function getQcRulePage(params: PageParam) {
  return requestClient.get<PageResult<MesQcRuleApi.QcRule>>(
    '/mes/qc-rule/page',
    { params },
  );
}

/** 查询MES-质检触发规则详情 */
export function getQcRule(id: number) {
  return requestClient.get<MesQcRuleApi.QcRule>(
    `/mes/qc-rule/get?id=${id}`,
  );
}

/** 新增MES-质检触发规则 */
export function createQcRule(data: MesQcRuleApi.QcRule) {
  return requestClient.post('/mes/qc-rule/create', data);
}

/** 修改MES-质检触发规则 */
export function updateQcRule(data: MesQcRuleApi.QcRule) {
  return requestClient.put('/mes/qc-rule/update', data);
}

/** 删除MES-质检触发规则 */
export function deleteQcRule(id: number) {
  return requestClient.delete(`/mes/qc-rule/delete?id=${id}`);
}

/** 批量删除MES-质检触发规则 */
export function deleteQcRuleList(ids: number[]) {
  return requestClient.delete(
    `/mes/qc-rule/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出MES-质检触发规则 */
export function exportQcRule(params: any) {
  return requestClient.download('/mes/qc-rule/export-excel', { params });
}

/** 下载MES-质检触发规则导入模板 */
export function importQcRuleTemplate() {
  return requestClient.download('/mes/qc-rule/get-import-template');
}

/** 导入MES-质检触发规则 */
export function importQcRule(file: File) {
  return requestClient.upload('/mes/qc-rule/import', { file });
}


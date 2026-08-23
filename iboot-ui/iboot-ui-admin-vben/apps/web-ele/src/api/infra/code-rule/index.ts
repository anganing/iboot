import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace InfraCodeRuleApi {
  /** 编码规则 */
  export interface CodeRule {
    id?: number;
    ruleCode: string;
    name: string;
    prefix?: string;
    yearEnabled: boolean;
    monthEnabled: boolean;
    dayEnabled: boolean;
    suffix?: string;
    serialLength: number;
    serialStart: number;
    serialStep: number;
    resetCycle: number;
    status: number;
    remark?: string;
    createTime?: Date;
  }
}

/** 查询编码规则分页 */
export function getCodeRulePage(params: PageParam) {
  return requestClient.get<PageResult<InfraCodeRuleApi.CodeRule>>(
    '/infra/code-rule/page',
    { params },
  );
}

/** 查询编码规则详情 */
export function getCodeRule(id: number) {
  return requestClient.get<InfraCodeRuleApi.CodeRule>(
    `/infra/code-rule/get?id=${id}`,
  );
}

/** 创建编码规则 */
export function createCodeRule(data: InfraCodeRuleApi.CodeRule) {
  return requestClient.post('/infra/code-rule/create', data);
}

/** 更新编码规则 */
export function updateCodeRule(data: InfraCodeRuleApi.CodeRule) {
  return requestClient.put('/infra/code-rule/update', data);
}

/** 删除编码规则 */
export function deleteCodeRule(id: number) {
  return requestClient.delete(`/infra/code-rule/delete?id=${id}`);
}

/** 预览下一个编码 */
export function previewCode(ruleCode: string) {
  return requestClient.get<string>(
    `/infra/code-rule/preview?ruleCode=${encodeURIComponent(ruleCode)}`,
  );
}

import { requestClient } from '#/api/request';

/** 按规则分配一个编码。 */
export function generateCode(ruleCode: string) {
  return requestClient.post<string>(
    `/infra/code-rule/generate?ruleCode=${encodeURIComponent(ruleCode)}`,
  );
}

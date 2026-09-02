import type { ApsStrategyApi } from '#/api/aps/strategy';

import { getConstraintMetadata } from '#/api/aps/strategy';

/**
 * 约束元数据的会话级缓存。
 *
 * 约束清单与默认权重在一次会话里不会变，权重编辑器和策略表单都要用，缓存在这里共享一份。
 * 缓存的是 Promise 而不是结果，连续打开两次弹窗也只会打一次接口；请求失败时清掉缓存，
 * 否则一次网络抖动会让这个会话再也拉不到约束清单。
 */
let metadataPromise:
  | Promise<ApsStrategyApi.ConstraintMetadataResult>
  | undefined;

export function loadConstraintMetadata() {
  metadataPromise ||= getConstraintMetadata().catch((error) => {
    metadataPromise = undefined;
    throw error;
  });
  return metadataPromise;
}

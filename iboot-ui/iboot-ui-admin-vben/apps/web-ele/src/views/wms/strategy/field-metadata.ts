import type { WmsStrategyApi } from '#/api/wms/strategy';

import { getFieldMetadata } from '#/api/wms/strategy';

/**
 * 字段元数据的会话级缓存。
 *
 * 每种策略类型能筛什么、能按什么排，在一次会话里不会变，配置编辑器和策略表单都要用，
 * 缓存在这里共享一份。缓存的是 Promise 而不是结果，连续打开两次弹窗也只打一次接口；
 * 请求失败时清掉缓存，否则一次网络抖动会让这个会话再也拉不到字段清单。
 */
let metadataPromise: Promise<WmsStrategyApi.FieldMetadataResult> | undefined;

export function loadFieldMetadata() {
  metadataPromise ||= getFieldMetadata().catch((error) => {
    metadataPromise = undefined;
    throw error;
  });
  return metadataPromise;
}

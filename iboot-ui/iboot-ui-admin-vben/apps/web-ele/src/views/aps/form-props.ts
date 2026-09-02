/**
 * APS 表单控件的公共配置。
 *
 * 与 mes / wms 保持一致，避免每个 data.ts 重复一遍，也避免再次写错 valueFormat。
 */

/**
 * 日期时间控件：必须用毫秒时间戳提交。
 *
 * <p>后端 `TimestampLocalDateTimeDeserializer` 只认数字（`getValueAsLong()`），
 * 传 'YYYY-MM-DD HH:mm:ss' 字符串会被解析成 0，落到后端就是 1970-01-01，
 * 于是「计划开始时间必须早于结束时间」这类区间校验会莫名失败。
 */
export const DATETIME_PROPS = {
  class: '!w-full',
  format: 'YYYY-MM-DD HH:mm:ss',
  showTime: true,
  type: 'datetime' as const,
  valueFormat: 'x',
};

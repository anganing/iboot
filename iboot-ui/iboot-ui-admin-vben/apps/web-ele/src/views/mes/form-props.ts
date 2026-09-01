/**
 * MES 表单控件的公共配置。
 *
 * 数量精度对齐库表的 decimal(20,6)，时间控件统一带时分秒，避免每个 data.ts 重复一遍。
 */

/** 数量控件：非负、六位小数，与库表精度一致。 */
export const QTY_PROPS = {
  class: '!w-full',
  controlsPosition: 'right' as const,
  min: 0,
  precision: 6,
};

/** 优先级、行号这类整数控件。 */
export const INT_PROPS = {
  class: '!w-full',
  controlsPosition: 'right' as const,
  min: 0,
  precision: 0,
};

/** 日期时间控件，与 wms 保持一致用毫秒时间戳提交。 */
export const DATETIME_PROPS = {
  class: '!w-full',
  format: 'YYYY-MM-DD HH:mm:ss',
  showTime: true,
  valueFormat: 'x',
};

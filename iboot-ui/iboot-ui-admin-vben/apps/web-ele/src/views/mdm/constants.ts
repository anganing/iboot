export const MDM_MAT_TYPE_OPTIONS = [
  { label: '原料', value: 'RAW' },
  { label: '半成品', value: 'SEMI_FINISHED' },
  { label: '成品', value: 'FINISHED' },
  { label: '辅料', value: 'AUXILIARY' },
  { label: '包材', value: 'PACKAGING' },
];

export const MDM_RES_TYPE_OPTIONS = [
  { label: '设备', value: 'EQUIPMENT' },
  { label: '产线', value: 'PRODUCTION_LINE' },
  { label: '人员组', value: 'WORK_GROUP' },
  { label: '模具', value: 'MOULD' },
];

/**
 * 列表行的可读标识，供删除确认气泡与删除中/删除成功提示使用。
 *
 * 主键是雪花 ID，超出 JS 安全整数，展示成 `2095200000000000006` 这种串用户既读不懂也核对不了；
 * 编码 + 名称才是业务上能对上号的信息。删除确认必须让用户看清删的是哪条记录。
 */
export function getMdmRowLabel(row: { code?: string; name?: string }) {
  return [row.code, row.name].filter(Boolean).join(' ') || '-';
}

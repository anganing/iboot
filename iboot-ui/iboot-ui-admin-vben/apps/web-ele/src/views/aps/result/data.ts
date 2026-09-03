import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsVersionApi } from '#/api/aps/version';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

/** 曾经生效过的状态，与后端 ApsVersionStatusEnum 的白名单一致 */
const EFFECTIVE_STATUSES = new Set(['PUBLISHED', 'REPLACED']);

/**
 * 列表的搜索表单。
 *
 * <p>只有场景编码与状态两项。场景用文本模糊匹配而不是 RelationSelect：选择器要打
 * `/aps/scenario/page`，那个接口挂 `aps:scenario:query`，为了一个筛选框给车间开
 * 排程场景的查询权不合适。工厂、车间不做筛选条件，场景本身已经绑定了这两者。
 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'scenarioCode',
      label: '场景编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入场景编码',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        // 不给 clearable：清空会回落成只查已发布，下拉却显示为空，看起来像"全部"
        options: getDictOptions(DICT_TYPE.APS_VERSION_STATUS, 'string').filter(
          (item) => EFFECTIVE_STATUSES.has(String(item.value)),
        ),
        placeholder: '请选择状态',
      },
      // 默认只看当前生效的计划，要对账上一版再手动切到已被替换
      defaultValue: 'PUBLISHED',
    },
  ];
}

/**
 * 列表的字段。
 *
 * <p>没有勾选列：这个页面只读，没有任何批量动作。不放创建时间，车间关心计划什么时候下来的，
 * 而不是版本什么时候生成的。得分、求解信息这些排程评审用的字段也不出现在这里。
 */
export function useGridColumns(): VxeTableGridOptions<ApsVersionApi.Version>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'scenarioCode', title: '场景编码', minWidth: 150 },
    { field: 'scenarioName', title: '场景名称', minWidth: 170 },
    { field: 'plantName', title: '工厂', minWidth: 130 },
    {
      field: 'workshopName',
      title: '车间',
      minWidth: 130,
      // 车间编号 0 是全工厂的占位，补全拿不到名称，这里落成可读文案
      formatter: ({ row }) => row.workshopName || '全工厂',
    },
    { field: 'versionNo', title: '版本号', minWidth: 90 },
    {
      field: 'status',
      title: '状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.APS_VERSION_STATUS },
      },
    },
    {
      field: 'planStartTime',
      title: '计划开始时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'planEndTime',
      title: '计划结束时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'publishedTime',
      title: '发布时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    { field: 'publishedByName', title: '发布人', minWidth: 120 },
    {
      title: '操作',
      width: 110,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';

import { htmlToText } from '#/utils';

/** 列表的字段。只读列表，不给勾选列 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    {
      field: 'title',
      title: '公告标题',
      minWidth: 240,
    },
    {
      field: 'type',
      title: '公告类型',
      width: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.SYSTEM_NOTICE_TYPE },
      },
    },
    {
      field: 'content',
      title: '内容摘要',
      minWidth: 300,
      // 公告内容是富文本，单元格里要剥成纯文本，否则会显示成一串标签
      formatter: ({ cellValue }) => htmlToText(cellValue, 80),
    },
    {
      field: 'createTime',
      title: '发布时间',
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 100,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

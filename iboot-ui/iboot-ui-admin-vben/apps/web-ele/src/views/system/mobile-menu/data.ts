
import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemMobileMenuApi } from '#/api/system/mobile-menu';

import {
  CommonStatusEnum,
  DICT_TYPE,
} from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { handleTree } from '@vben/utils';

import { z } from '#/adapter/form';
import { getSimpleMobileMenuList } from '#/api/system/mobile-menu';

/** 移动端菜单类型常量（无现成字典，本地维护） */
export const MobileMenuTypeEnum = {
  GROUP: 1, // 分组
  ENTRY: 2, // 入口
  BUTTON: 3, // 按钮
};

/** 移动端菜单类型选项 */
export const mobileMenuTypeOptions = [
  { label: '分组', value: MobileMenuTypeEnum.GROUP },
  { label: '入口', value: MobileMenuTypeEnum.ENTRY },
  { label: '按钮', value: MobileMenuTypeEnum.BUTTON },
];

/** 类型转中文 */
function formatMobileMenuType(type: number | undefined): string {
  return (
    mobileMenuTypeOptions.find((item) => item.value === type)?.label ?? '-'
  );
}

/** 查询表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入名称',
        clearable: true,
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择状态',
        clearable: true,
      },
    },
  ];
}

/** 新增/修改的表单 */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'id',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'parentId',
      label: '上级分组',
      component: 'ApiTreeSelect',
      componentProps: {
        clearable: true,
        api: async () => {
          const data = await getSimpleMobileMenuList();
          data.unshift({
            id: 0,
            parentId: 0,
            name: '顶级',
            type: MobileMenuTypeEnum.GROUP,
            status: CommonStatusEnum.ENABLE,
          } as SystemMobileMenuApi.SimpleMobileMenu);
          // 上级分组仅可选 type=1 分组，过滤掉入口/按钮
          return handleTree(
            data.filter((item) => item.type === MobileMenuTypeEnum.GROUP),
          );
        },
        labelField: 'name',
        valueField: 'id',
        childrenField: 'children',
        placeholder: '请选择上级分组',
        showSearch: true,
        defaultExpandedKeys: [0],
      },
      rules: 'selectRequired',
    },
    {
      fieldName: 'name',
      label: '名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'type',
      label: '类型',
      component: 'RadioGroup',
      componentProps: {
        options: mobileMenuTypeOptions,
      },
      rules: z.number().default(MobileMenuTypeEnum.GROUP),
    },
    {
      fieldName: 'icon',
      label: '图标',
      component: 'IconPicker',
      componentProps: {
        placeholder: '请选择图标',
        prefix: 'carbon',
      },
      dependencies: {
        triggerFields: ['type'],
        show: (values) => {
          return [MobileMenuTypeEnum.ENTRY, MobileMenuTypeEnum.GROUP].includes(
            values.type,
          );
        },
      },
    },
    {
      fieldName: 'path',
      label: '页面路径',
      component: 'Input',
      componentProps: {
        placeholder: 'uni-app 页面路径，如 /pages/notify/index',
      },
      dependencies: {
        triggerFields: ['type'],
        show: (values) => {
          return values.type === MobileMenuTypeEnum.ENTRY;
        },
      },
    },
    {
      fieldName: 'permission',
      label: '权限标识',
      component: 'Input',
      componentProps: {
        placeholder: '请输入权限标识',
      },
      dependencies: {
        triggerFields: ['type'],
        show: (values) => {
          return [MobileMenuTypeEnum.BUTTON, MobileMenuTypeEnum.ENTRY].includes(
            values.type,
          );
        },
      },
    },
    {
      fieldName: 'sort',
      label: '排序',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        placeholder: '请输入排序',
        controlsPosition: 'right',
        class: '!w-full',
      },
      rules: 'required',
      defaultValue: 0,
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
      rules: z.number().default(CommonStatusEnum.ENABLE),
    },
    {
      fieldName: 'visible',
      label: '是否可见',
      component: 'Switch',
      rules: 'required',
      defaultValue: true,
      help: '选择隐藏时，移动端入口将不会展示，但仍然可以访问',
      dependencies: {
        triggerFields: ['type'],
        show: (values) => {
          return [MobileMenuTypeEnum.ENTRY, MobileMenuTypeEnum.GROUP].includes(
            values.type,
          );
        },
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入备注',
        rows: 3,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<SystemMobileMenuApi.MobileMenu>['columns'] {
  return [
    {
      field: 'name',
      title: '名称',
      minWidth: 250,
      align: 'left',
      fixed: 'left',
      treeNode: true,
    },
    {
      field: 'type',
      title: '类型',
      minWidth: 100,
      formatter: ({ cellValue }) => formatMobileMenuType(cellValue),
    },
    {
      field: 'path',
      title: '页面路径',
      minWidth: 200,
    },
    {
      field: 'icon',
      title: '图标',
      minWidth: 120,
      formatter: ({ cellValue }) => cellValue || '-',
    },
    {
      field: 'permission',
      title: '权限标识',
      minWidth: 200,
      formatter: ({ cellValue }) => cellValue || '-',
    },
    {
      field: 'sort',
      title: '排序',
      minWidth: 80,
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 150,
      formatter: ({ cellValue }) => cellValue || '-',
    },
    {
      title: '操作',
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 分配角色移动端菜单的表单 */
export function useAssignMobileMenuFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'name',
      label: '角色名称',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
    },
    {
      fieldName: 'menuIds',
      label: '移动端菜单',
      component: 'Input',
      formItemClass: 'items-start',
    },
  ];
}

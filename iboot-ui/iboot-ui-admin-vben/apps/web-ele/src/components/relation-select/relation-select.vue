<script lang="ts" setup>
import type { RelationSelectColumn, RelationSelectQueryField } from './types';

import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { computed, nextTick, ref, useAttrs, watch } from 'vue';

import { CircleX, Search } from '@vben/icons';

import {
  ElButton,
  ElDialog,
  ElInput,
  ElMessage,
  ElTooltip,
} from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { requestClient } from '#/api/request';

defineOptions({ name: 'RelationSelect', inheritAttrs: false });

type RelationValue = null | number | number[] | string | string[] | undefined;
type Recordable = Record<string, any>;

const props = withDefaults(
  defineProps<{
    allUri?: string;
    clearable?: boolean;
    columns?: RelationSelectColumn[];
    detailUri?: string;
    dialogTitle?: string;
    dialogWidth?: string;
    disabled?: boolean;
    displayFields?: string[];
    extraParams?: Recordable;
    gridHeight?: number;
    labelSeparator?: string;
    modelValue?: RelationValue;
    multiple?: boolean;
    pageSize?: number;
    pageUri: string;
    placeholder?: string;
    queryAll?: boolean;
    queryAllPageSize?: number;
    queryFields?: RelationSelectQueryField[];
    valueField?: string;
  }>(),
  {
    clearable: true,
    columns: () => [],
    disabled: false,
    dialogWidth: 'min(1200px, calc(100vw - 32px))',
    displayFields: () => ['name'],
    dialogTitle: '选择关联数据',
    extraParams: () => ({}),
    gridHeight: 520,
    labelSeparator: ' / ',
    modelValue: undefined,
    multiple: false,
    pageSize: 10,
    placeholder: '请选择关联数据',
    queryAll: false,
    queryAllPageSize: 1000,
    queryFields: () => [],
    valueField: 'id',
  },
);

const emit = defineEmits<{
  change: [item: Recordable | Recordable[] | undefined];
  'update:modelValue': [value: RelationValue];
}>();

const attrs = useAttrs();
const open = ref(false);
const hovering = ref(false);
const selectedRows = ref<Recordable[]>([]);

const displayLabel = computed(() =>
  selectedRows.value.map((item) => formatLabel(item)).join('、'),
);

const showClear = computed(
  () => props.clearable && !props.disabled && hovering.value && hasValue(),
);

function getSelectedValues(value: RelationValue) {
  if (Array.isArray(value)) {
    return props.multiple ? value : value.slice(0, 1);
  }
  return value === null || value === undefined || value === '' ? [] : [value];
}

function hasValue() {
  return getSelectedValues(props.modelValue).length > 0;
}

function formatLabel(item: Recordable) {
  const label = props.displayFields
    .map((field) => item[field])
    .filter((value) => value !== undefined && value !== null && value !== '')
    .join(props.labelSeparator);
  return label || String(item[props.valueField] ?? '');
}

function formatUri(uri: string, params: Recordable) {
  return uri.replaceAll(/\{(\w+)\}/g, (_, key) =>
    encodeURIComponent(params[key] ?? ''),
  );
}

function sameValue(left: any, right: any) {
  return String(left) === String(right);
}

function normalizeResult(data: any): { list: Recordable[]; total: number } {
  if (Array.isArray(data)) {
    return { list: data, total: data.length };
  }
  if (Array.isArray(data?.list)) {
    return { list: data.list, total: Number(data.total ?? data.list.length) };
  }
  if (Array.isArray(data?.records)) {
    return {
      list: data.records,
      total: Number(data.total ?? data.records.length),
    };
  }
  return { list: [], total: 0 };
}

function buildGridFormSchema(): VbenFormSchema[] {
  return props.queryFields.map((field: RelationSelectQueryField) => ({
    fieldName: field.field,
    label: field.label,
    component: field.type === 'select' ? 'Select' : 'Input',
    componentProps: {
      clearable: true,
      options: field.options,
      placeholder:
        field.placeholder ||
        (field.type === 'select'
          ? `请选择${field.label}`
          : `请输入${field.label}`),
    },
  }));
}

function buildGridColumns(
  isMultiple = false,
): VxeTableGridOptions<Recordable>['columns'] {
  const configuredColumns: RelationSelectColumn[] =
    props.columns.length > 0
      ? props.columns
      : [...new Set([props.valueField, ...props.displayFields])].map(
          (field) => ({
            field,
            label: field,
            minWidth: 120,
          }),
        );
  return [
    {
      type: isMultiple ? 'checkbox' : 'radio',
      width: 50,
    },
    ...configuredColumns.map((column) => ({
      field: column.field,
      title: column.label,
      minWidth: column.minWidth,
      width: column.width,
    })),
  ];
}

function getGridHeight() {
  if (typeof window === 'undefined') {
    return props.gridHeight;
  }
  return Math.min(props.gridHeight, Math.max(280, window.innerHeight - 300));
}

async function queryData({ page }: any, formValues: Recordable) {
  const params = {
    // 父级固定条件（如出库单编号）由调用方通过 extraParams 传入，用户不可改
    ...props.extraParams,
    ...formValues,
    ...(props.queryAll
      ? { pageNo: 1, pageSize: props.queryAllPageSize }
      : {
          pageNo: page.currentPage,
          pageSize: page.pageSize,
        }),
  };
  const data = await requestClient.get(
    props.queryAll && props.allUri ? props.allUri : props.pageUri,
    { params },
  );
  return normalizeResult(data);
}

async function resolveItemByValue(value: any) {
  if (props.detailUri) {
    return await requestClient.get(formatUri(props.detailUri, { id: value }));
  }
  const data = await requestClient.get(props.pageUri, {
    params: { ...props.extraParams, pageNo: 1, pageSize: props.queryAllPageSize },
  });
  return normalizeResult(data).list.find((item) =>
    sameValue(item[props.valueField], value),
  );
}

async function resolveSelectedRows(value: RelationValue) {
  const values = getSelectedValues(value);
  if (values.length === 0) {
    selectedRows.value = [];
    return;
  }
  if (
    selectedRows.value.length === values.length &&
    selectedRows.value.every((item) =>
      values.some((valueItem) => sameValue(valueItem, item[props.valueField])),
    )
  ) {
    return;
  }
  const rows = await Promise.all(
    values.map((item) => resolveItemByValue(item)),
  );
  selectedRows.value = rows.filter(Boolean);
}

function getMultipleSelectedRows() {
  const selectedMap = new Map(
    selectedRows.value.map((item) => [String(item[props.valueField]), item]),
  );
  const records = [
    ...(gridApi.grid.getCheckboxReserveRecords?.() ?? []),
    ...(gridApi.grid.getCheckboxRecords?.() ?? []),
  ] as Recordable[];
  for (const row of records) {
    selectedMap.set(String(row[props.valueField]), row);
  }
  const currentRows = (gridApi.grid.getData?.() ?? []) as Recordable[];
  for (const row of currentRows) {
    if (!gridApi.grid.isCheckedByCheckboxRow(row)) {
      selectedMap.delete(String(row[props.valueField]));
    }
  }
  return [...selectedMap.values()];
}

function handleCheckboxSelectChange() {
  selectedRows.value = getMultipleSelectedRows();
}

function handleRadioChange(row: Recordable) {
  selectedRows.value = [row];
}

async function toggleMultipleRow(row: Recordable) {
  const selected = gridApi.grid.isCheckedByCheckboxRow(row);
  await gridApi.grid.setCheckboxRow(row, !selected);
  selectedRows.value = getMultipleSelectedRows();
}

async function handleCellDblclick({ row }: { row: Recordable }) {
  if (props.multiple) {
    await toggleMultipleRow(row);
    return;
  }
  selectedRows.value = [row];
  await gridApi.grid.setRadioRow(row);
  handleConfirm();
}

async function clearGridSelection() {
  await gridApi.grid.clearCheckboxRow();
  await gridApi.grid.clearCheckboxReserve();
  await gridApi.grid.clearRadioRow();
}

async function applyPreSelection() {
  const selectedValues = getSelectedValues(props.modelValue);
  if (selectedValues.length === 0) {
    return;
  }
  const rows = (gridApi.grid.getData?.() ?? []) as Recordable[];
  for (const row of rows) {
    const rowValue = row[props.valueField];
    if (!selectedValues.some((value) => sameValue(value, rowValue))) {
      continue;
    }
    if (props.multiple) {
      await gridApi.grid.setCheckboxRow(row, true);
    } else {
      await gridApi.grid.setRadioRow(row);
      selectedRows.value = [row];
      return;
    }
  }
}

const [Grid, gridApi] = useVbenVxeGrid<Recordable>({
  formOptions: {
    schema: buildGridFormSchema(),
  },
  gridOptions: {
    columns: buildGridColumns(false),
    height: getGridHeight(),
    keepSource: true,
    checkboxConfig: {
      highlight: true,
      range: true,
      reserve: true,
    },
    pagerConfig: {
      enabled: !props.queryAll,
      pageSize: props.pageSize,
    },
    proxyConfig: {
      ajax: {
        query: queryData,
      },
    },
    radioConfig: {
      highlight: true,
      trigger: 'row',
    },
    rowConfig: {
      keyField: props.valueField,
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<Recordable>,
  gridEvents: {
    cellDblclick: handleCellDblclick,
    checkboxAll: handleCheckboxSelectChange,
    checkboxChange: handleCheckboxSelectChange,
    radioChange: ({ row }: { row: Recordable }) => {
      handleRadioChange(row);
    },
  },
});

async function clearSelected() {
  selectedRows.value = [];
  await clearGridSelection();
  if (props.multiple) {
    emit('update:modelValue', []);
    emit('change', []);
    return;
  }
  emit('update:modelValue', undefined);
  emit('change', undefined);
}

async function handleClick(event: MouseEvent) {
  if (props.disabled) {
    return;
  }
  const target = event.target as HTMLElement;
  if (showClear.value && target.closest('.el-input__suffix')) {
    event.stopPropagation();
    await clearSelected();
    return;
  }
  open.value = true;
  await nextTick();
  gridApi.setGridOptions({
    columns: buildGridColumns(props.multiple),
    height: getGridHeight(),
    pagerConfig: {
      enabled: !props.queryAll,
      pageSize: props.pageSize,
    },
    rowConfig: {
      keyField: props.valueField,
      isHover: true,
    },
  });
  await clearGridSelection();
  await gridApi.formApi.resetForm();
  await gridApi.query();
  await nextTick();
  await applyPreSelection();
}

function closeModal() {
  open.value = false;
}

function handleConfirm() {
  const rows = props.multiple ? getMultipleSelectedRows() : selectedRows.value;
  if (rows.length === 0) {
    ElMessage.warning(props.multiple ? '请至少选择一条数据' : '请选择一条数据');
    return;
  }
  if (props.multiple) {
    const values = rows.map((row) => row[props.valueField]);
    selectedRows.value = rows;
    emit('update:modelValue', values);
    emit('change', rows);
  } else {
    const row = rows[0]!;
    selectedRows.value = [row];
    emit('update:modelValue', row[props.valueField]);
    emit('change', row);
  }
  open.value = false;
}

watch(() => props.modelValue, resolveSelectedRows, {
  deep: true,
  immediate: true,
});
</script>

<template>
  <div
    v-bind="attrs"
    class="w-full"
    :class="disabled ? 'cursor-not-allowed' : 'cursor-pointer'"
    @click="handleClick"
    @mouseenter="hovering = true"
    @mouseleave="hovering = false"
  >
    <ElTooltip
      :disabled="selectedRows.length === 0"
      placement="top"
      :show-after="500"
    >
      <template #content>
        <div class="max-w-[520px] space-y-1">
          <div v-for="item in selectedRows" :key="item[valueField]">
            {{ formatLabel(item) }}
          </div>
        </div>
      </template>
      <ElInput
        :disabled="disabled"
        :model-value="displayLabel"
        :placeholder="placeholder"
        readonly
      >
        <template #suffix>
          <CircleX v-if="showClear" class="size-4" />
          <Search v-else class="size-4" />
        </template>
      </ElInput>
    </ElTooltip>
  </div>

  <ElDialog
    v-model="open"
    append-to-body
    :title="dialogTitle"
    top="5vh"
    :width="dialogWidth"
    :z-index="3000"
  >
    <Grid :table-title="dialogTitle" />
    <template #footer>
      <ElButton @click="closeModal">取消</ElButton>
      <ElButton type="primary" @click="handleConfirm">确定</ElButton>
    </template>
  </ElDialog>
</template>

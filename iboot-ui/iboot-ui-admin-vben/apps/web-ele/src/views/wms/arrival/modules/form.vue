<script lang="ts" setup>
import type { LocalLineRow } from '../../order-form';

import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsArrivalApi } from '#/api/wms/arrival';

import { computed, ref } from 'vue';

import { confirm, useVbenModal } from '@vben/common-ui';
import { isEmpty } from '@vben/utils';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  createArrival,
  getArrival,
  getArrivalLinePage,
  updateArrival,
} from '#/api/wms/arrival';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import {
  LOCAL_LINE_KEY,
  nextLocalLineKey,
  renumberLines,
  stripLocalKey,
  WMS_ARRIVAL_STATUS,
} from '../../order-form';
import { useArrivalLineEditorColumns, useFormSchema } from '../data';
import ArrivalLineForm from './arrival-line-form.vue';

/** 明细区里的一行，带前端本地标识 */
type LineRow = LocalLineRow<WmsArrivalApi.ArrivalLine>;

const emit = defineEmits(['success']);
const formData = ref<WmsArrivalApi.Arrival>();
/** 供应商编码与名称快照，只能从供应商选择器带回来：领域侧不回填这两列 */
const supplierSnapshot = ref<{ code?: string; name?: string }>({});
/**
 * 明细行，本地数组就是数据源。
 *
 * 明细随单头一次提交：后端 `updateArrival` 是"旧行整批删除后按新参数重建"，
 * 所以改单时要先把现有明细读进来，提交时也必须带上全部行，不能只传变动的行。
 */
const lines = ref<LineRow[]>([]);
const checkedLineKeys = ref<string[]>([]);

const getTitle = computed(() =>
  formData.value?.id
    ? $t('ui.actionTitle.edit', ['到货登记'])
    : $t('ui.actionTitle.create', ['到货登记']),
);

const formLayout = getMdmFormLayout(2, 'w-[1100px]');

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: formLayout.formItemClass,
    labelClass: 'justify-start',
    labelWidth: 110,
  },
  wrapperClass: formLayout.wrapperClass,
  layout: 'horizontal',
  schema: useFormSchema((field, row) => {
    if (field === 'supplierId') {
      supplierSnapshot.value = { code: row?.code, name: row?.name };
    }
  }),
  showDefaultActions: false,
});

function handleLineCheckboxChange({ records }: { records: LineRow[] }) {
  checkedLineKeys.value = records.map((row) => row._localKey!);
}

/** 明细区，不发任何请求 */
const [LineGrid, lineGridApi] = useVbenVxeGrid({
  gridOptions: {
    border: true,
    columns: useArrivalLineEditorColumns(),
    data: [],
    height: 260,
    keepSource: true,
    pagerConfig: { enabled: false },
    rowConfig: { keyField: LOCAL_LINE_KEY, isHover: true },
    toolbarConfig: {
      custom: false,
      refresh: false,
      search: false,
      zoom: false,
    },
  } as VxeTableGridOptions<LineRow>,
  gridEvents: {
    checkboxAll: handleLineCheckboxChange,
    checkboxChange: handleLineCheckboxChange,
  },
});

const [LineFormModal, lineFormModalApi] = useVbenModal({
  connectedComponent: ArrivalLineForm,
  destroyOnClose: true,
});

/** 明细区重新渲染并清空勾选 */
function syncLines(next: LineRow[]) {
  lines.value = next;
  checkedLineKeys.value = [];
  lineGridApi.setGridOptions({ data: next });
}

/** 新增明细：打开行弹窗，确定后只落到本地数组 */
function handleLineCreate() {
  lineFormModalApi.setData(null).open();
}

/** 修改明细 */
function handleLineEdit(row: LineRow) {
  lineFormModalApi.setData(row).open();
}

/** 删除明细 */
function handleLineDelete(row: LineRow) {
  const localKey = row._localKey;
  syncLines(lines.value.filter((line) => line._localKey !== localKey));
}

/** 批量删除明细 */
async function handleLineDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const removing = new Set(checkedLineKeys.value);
  syncLines(lines.value.filter((line) => !removing.has(line._localKey!)));
}

/** 行弹窗确定：新增追加到本地数组，修改覆盖对应项 */
function handleLineSubmit(line: LineRow) {
  const localKey = line._localKey;
  if (localKey) {
    syncLines(
      lines.value.map((item) => (item._localKey === localKey ? line : item)),
    );
    return;
  }
  syncLines([...lines.value, { ...line, _localKey: nextLocalLineKey() }]);
}

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    if (isEmpty(lines.value)) {
      ElMessage.warning('请至少添加一行到货明细');
      return;
    }
    modalApi.lock();
    try {
      const values = (await formApi.getValues()) as WmsArrivalApi.Arrival;
      const data: WmsArrivalApi.Arrival = {
        ...values,
        supplierCode: supplierSnapshot.value.code ?? formData.value?.supplierCode,
        supplierName: supplierSnapshot.value.name ?? formData.value?.supplierName,
        // 状态由领域 Service 写死为草稿，这里只为满足请求 VO 的必填校验
        status: WMS_ARRIVAL_STATUS.DRAFT,
        lines: renumberLines(lines.value).map((line) => stripLocalKey(line)),
      };
      await (formData.value?.id ? updateArrival(data) : createArrival(data));
      await modalApi.close();
      emit('success');
      ElMessage.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      supplierSnapshot.value = {};
      return;
    }
    await formApi.resetForm();
    supplierSnapshot.value = {};
    syncLines([]);
    const data = modalApi.getData<WmsArrivalApi.Arrival>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getArrival(data.id);
      await formApi.setValues(formData.value);
      const page = await getArrivalLinePage({
        arrivalId: data.id,
        pageNo: 1,
        pageSize: 100,
      });
      syncLines(
        (page.list ?? []).map((line) => ({
          ...line,
          _localKey: nextLocalLineKey(),
        })),
      );
    } finally {
      modalApi.unlock();
    }
  },
});
</script>
<template>
  <Modal :class="formLayout.modalClass" :title="getTitle">
    <LineFormModal @success="handleLineSubmit" />
    <Form class="mx-4" />
    <div class="mx-4 mt-2">
      <LineGrid table-title="到货明细">
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: '添加明细',
                type: 'primary',
                icon: ACTION_ICON.ADD,
                auth: ['wms:arrival:create'],
                onClick: handleLineCreate,
              },
              {
                label: $t('ui.actionTitle.deleteBatch'),
                type: 'danger',
                icon: ACTION_ICON.DELETE,
                auth: ['wms:arrival:create'],
                disabled: isEmpty(checkedLineKeys),
                onClick: handleLineDeleteBatch,
              },
            ]"
          />
        </template>
        <template #actions="{ row }">
          <TableAction
            :actions="[
              {
                label: $t('common.edit'),
                type: 'primary',
                link: true,
                icon: ACTION_ICON.EDIT,
                onClick: handleLineEdit.bind(null, row),
              },
              {
                label: $t('common.delete'),
                type: 'danger',
                link: true,
                icon: ACTION_ICON.DELETE,
                popConfirm: {
                  title: $t('ui.actionMessage.deleteConfirm', [
                    row.matCode || row.lineNo,
                  ]),
                  confirm: handleLineDelete.bind(null, row),
                },
              },
            ]"
          />
        </template>
      </LineGrid>
    </div>
  </Modal>
</template>

<script lang="ts" setup>
import type { LocalLineRow } from '../../order-form';

import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsTransferOrderApi } from '#/api/wms/transferorder';

import { computed, ref } from 'vue';

import { confirm, useVbenModal } from '@vben/common-ui';
import { isEmpty } from '@vben/utils';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  createTransferOrder,
  getTransferOrder,
  getTransferOrderLinePage,
  updateTransferOrder,
} from '#/api/wms/transferorder';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import {
  LOCAL_LINE_KEY,
  newCreateIdempotencyKey,
  nextLocalLineKey,
  renumberLines,
  stripLocalKey,
  WMS_ORDER_STATUS,
  zeroIfBlank,
} from '../../order-form';
import { useFormSchema, useTransferOrderLineEditorColumns } from '../data';
import TransferOrderLineForm from './transfer-order-line-form.vue';

/** 明细区里的一行，带前端本地标识 */
type LineRow = LocalLineRow<WmsTransferOrderApi.TransferOrderLine>;

const emit = defineEmits(['success']);
const formData = ref<WmsTransferOrderApi.TransferOrder>();
/** 创建幂等键，弹窗打开时生成一次，提交失败重试复用同一个键，避免建出第二张单 */
const createKey = ref<string>();
/**
 * 明细行，本地数组就是数据源。
 *
 * 明细随单头一次提交：后端 `updateTransferOrder` 是"旧行整批删除后按新参数重建"，
 * 所以改单时要先把现有明细读进来，提交时也必须带上全部行，不能只传变动的行。
 */
const lines = ref<LineRow[]>([]);
const checkedLineKeys = ref<string[]>([]);

const getTitle = computed(() =>
  formData.value?.id
    ? $t('ui.actionTitle.edit', ['移库调拨单'])
    : $t('ui.actionTitle.create', ['移库调拨单']),
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
  schema: useFormSchema(),
  showDefaultActions: false,
});

function handleLineCheckboxChange({ records }: { records: LineRow[] }) {
  checkedLineKeys.value = records.map((row) => row._localKey!);
}

/** 明细区，不发任何请求 */
const [LineGrid, lineGridApi] = useVbenVxeGrid({
  gridOptions: {
    border: true,
    columns: useTransferOrderLineEditorColumns(),
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
  connectedComponent: TransferOrderLineForm,
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
      ElMessage.warning('请至少添加一行调拨明细');
      return;
    }
    modalApi.lock();
    try {
      const values =
        (await formApi.getValues()) as WmsTransferOrderApi.TransferOrder;
      const data: WmsTransferOrderApi.TransferOrder = {
        ...values,
        // 状态由领域 Service 写死为新建，这里只为满足请求 VO 的必填校验
        status: WMS_ORDER_STATUS.DRAFT,
        fromProjectId: zeroIfBlank(values.fromProjectId) as number,
        toProjectId: zeroIfBlank(values.toProjectId) as number,
        idempotencyKey: formData.value?.id
          ? formData.value.idempotencyKey
          : createKey.value,
        lines: renumberLines(lines.value).map((line) => stripLocalKey(line)),
      };
      await (formData.value?.id
        ? updateTransferOrder(data)
        : createTransferOrder(data));
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
      createKey.value = undefined;
      return;
    }
    await formApi.resetForm();
    syncLines([]);
    const data = modalApi.getData<WmsTransferOrderApi.TransferOrder>();
    if (!data || !data.id) {
      // 新建：幂等键在这里生成一次，之后每次重试都用它
      createKey.value = newCreateIdempotencyKey('TRANSFER_ORDER');
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getTransferOrder(data.id);
      await formApi.setValues(formData.value);
      const page = await getTransferOrderLinePage({
        orderId: data.id,
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
      <LineGrid table-title="调拨明细">
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: '添加明细',
                type: 'primary',
                icon: ACTION_ICON.ADD,
                auth: ['wms:transfer-order:create'],
                onClick: handleLineCreate,
              },
              {
                label: $t('ui.actionTitle.deleteBatch'),
                type: 'danger',
                icon: ACTION_ICON.DELETE,
                auth: ['wms:transfer-order:create'],
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

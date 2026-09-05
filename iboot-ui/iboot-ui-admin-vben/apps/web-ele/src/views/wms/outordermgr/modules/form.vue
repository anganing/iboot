<script lang="ts" setup>
import type { LocalLineRow } from '../../order-form';

import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsOutOrderApi } from '#/api/wms/outordermgr';

import { computed, ref } from 'vue';

import { confirm, useVbenModal } from '@vben/common-ui';
import { isEmpty } from '@vben/utils';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  createOutOrder,
  getOutOrder,
  getOutOrderLinePage,
  updateOutOrder,
} from '#/api/wms/outordermgr';
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
import { useFormSchema, useOutOrderLineEditorColumns } from '../data';
import OutOrderLineForm from './out-order-line-form.vue';

/** 明细区里的一行，带前端本地标识 */
type LineRow = LocalLineRow<WmsOutOrderApi.OutOrderLine>;

const emit = defineEmits(['success']);
const formData = ref<WmsOutOrderApi.OutOrder>();
/** 来源仓库编码快照，只能从仓库选择器带回来：后端该字段必填 */
const whCode = ref<string>();
/** 创建幂等键，弹窗打开时生成一次，提交失败重试复用同一个键，避免建出第二张单 */
const createKey = ref<string>();
/**
 * 明细行，本地数组就是数据源。
 *
 * 明细随单头一次提交：后端 `updateOutOrder` 是"旧行整批删除后按新参数重建"，
 * 所以改单时要先把现有明细读进来，提交时也必须带上全部行。
 */
const lines = ref<LineRow[]>([]);
const checkedLineKeys = ref<string[]>([]);

const getTitle = computed(() =>
  formData.value?.id
    ? $t('ui.actionTitle.edit', ['出库单'])
    : $t('ui.actionTitle.create', ['出库单']),
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
    if (field === 'whId') {
      whCode.value = row?.code;
    }
  }),
  showDefaultActions: false,
});

function handleLineCheckboxChange({
  records,
}: {
  records: LineRow[];
}) {
  checkedLineKeys.value = records.map((row) => row._localKey!);
}

/** 明细区，不发任何请求 */
const [LineGrid, lineGridApi] = useVbenVxeGrid({
  gridOptions: {
    border: true,
    columns: useOutOrderLineEditorColumns(),
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
  connectedComponent: OutOrderLineForm,
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
  syncLines(
    lines.value.filter((line) => line._localKey !== localKey),
  );
}

/** 批量删除明细 */
async function handleLineDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const removing = new Set(checkedLineKeys.value);
  syncLines(
    lines.value.filter((line) => !removing.has(line._localKey!)),
  );
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
      ElMessage.warning('请至少添加一行出库明细');
      return;
    }
    modalApi.lock();
    try {
      const values = (await formApi.getValues()) as WmsOutOrderApi.OutOrder;
      const data: WmsOutOrderApi.OutOrder = {
        ...values,
        whCode: whCode.value ?? formData.value?.whCode,
        // 状态由领域 Service 写死为新建，这里只为满足请求 VO 的必填校验
        status: WMS_ORDER_STATUS.DRAFT,
        projectId: zeroIfBlank(values.projectId) as number,
        projectTaskId: zeroIfBlank(values.projectTaskId) as number,
        toInvOrgId: zeroIfBlank(values.toInvOrgId) as number,
        toWhId: zeroIfBlank(values.toWhId) as number,
        idempotencyKey: formData.value?.id
          ? formData.value.idempotencyKey
          : createKey.value,
        lines: renumberLines(lines.value).map((line) => ({
          // 界面手工建的出库单没有上游预留，预留由 MES 领料建
          reservationId: 0,
          ...stripLocalKey(line),
        })),
      };
      await (formData.value?.id ? updateOutOrder(data) : createOutOrder(data));
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
      whCode.value = undefined;
      createKey.value = undefined;
      return;
    }
    await formApi.resetForm();
    syncLines([]);
    const data = modalApi.getData<WmsOutOrderApi.OutOrder>();
    if (!data || !data.id) {
      // 新建：幂等键在这里生成一次，之后每次重试都用它
      createKey.value = newCreateIdempotencyKey('OUT_ORDER');
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getOutOrder(data.id);
      whCode.value = formData.value.whCode;
      await formApi.setValues(formData.value);
      const page = await getOutOrderLinePage({
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
      <LineGrid table-title="出库明细">
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: '添加明细',
                type: 'primary',
                icon: ACTION_ICON.ADD,
                auth: ['wms:out-order:create'],
                onClick: handleLineCreate,
              },
              {
                label: $t('ui.actionTitle.deleteBatch'),
                type: 'danger',
                icon: ACTION_ICON.DELETE,
                auth: ['wms:out-order:create'],
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

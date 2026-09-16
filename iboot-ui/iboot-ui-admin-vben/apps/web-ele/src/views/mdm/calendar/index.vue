<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmCalendarApi } from '#/api/mdm/calendar';

import { ref } from 'vue';

import { confirm, Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteCalendar,
  deleteCalendarList,
  exportCalendar,
  getCalendarPage,
} from '#/api/mdm/calendar';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import CalendarExceptionList from './modules/calendar-exception-list.vue';
import CalendarRuleList from './modules/calendar-rule-list.vue';
import Form from './modules/form.vue';
import GenerateModal from './modules/generate.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [RuleDrawer, ruleDrawerApi] = useVbenDrawer({
  connectedComponent: CalendarRuleList,
  destroyOnClose: true,
});
const [ExceptionDrawer, exceptionDrawerApi] = useVbenDrawer({
  connectedComponent: CalendarExceptionList,
  destroyOnClose: true,
});
const [GenerateManageModal, generateModalApi] = useVbenModal({
  connectedComponent: GenerateModal,
  destroyOnClose: true,
});

function handleRefresh() {
  gridApi.query();
}

function handleCreate() {
  formModalApi.setData(null).open();
}

function handleEdit(row: MdmCalendarApi.Calendar) {
  formModalApi.setData(row).open();
}

function handleRule(row: MdmCalendarApi.Calendar) {
  ruleDrawerApi.setData(row).open();
}

function handleException(row: MdmCalendarApi.Calendar) {
  exceptionDrawerApi.setData(row).open();
}

function handleGenerate(row: MdmCalendarApi.Calendar) {
  generateModalApi.setData(row).open();
}

async function handleDelete(row: MdmCalendarApi.Calendar) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.id]),
  });
  try {
    await deleteCalendar(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.id]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteCalendarList(checkedIds.value);
    checkedIds.value = [];
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

const checkedIds = ref<number[]>([]);
function handleRowCheckboxChange({
  records,
}: {
  records: MdmCalendarApi.Calendar[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

async function handleExport() {
  const data = await exportCalendar(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '日历.xls', source: data });
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getCalendarPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MdmCalendarApi.Calendar>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <RuleDrawer />
    <ExceptionDrawer />
    <GenerateManageModal />
    <Grid table-title="日历模板列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['日历']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['mdm:calendar:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mdm:calendar:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['mdm:calendar:delete'],
              disabled: isEmpty(checkedIds),
              onClick: handleDeleteBatch,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '规则',
              type: 'primary',
              link: true,
              icon: 'lucide:calendar-clock',
              auth: ['mdm:calendar:query'],
              onClick: handleRule.bind(null, row),
            },
            {
              label: '例外',
              type: 'primary',
              link: true,
              icon: 'lucide:calendar-x',
              auth: ['mdm:calendar:query'],
              onClick: handleException.bind(null, row),
            },
            {
              label: '生成日历',
              type: 'primary',
              link: true,
              icon: 'lucide:calendar-plus',
              auth: ['mdm:calendar:update'],
              onClick: handleGenerate.bind(null, row),
            },
          ]"
          :drop-down-actions="[
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['mdm:calendar:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['mdm:calendar:delete'],
              popConfirm: {
                title: `确定删除日历“${row.code ?? row.name ?? row.id}”吗？`,
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>

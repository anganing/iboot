<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmCalendarApi } from '#/api/mdm/calendar';

import { computed, ref } from 'vue';

import { confirm, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { isEmpty } from '@vben/utils';

import { ElAlert, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteCalendarException,
  deleteCalendarExceptionList,
  getCalendarExceptionPage,
} from '#/api/mdm/calendar';
import { $t } from '#/locales';

import { useExceptionGridColumns } from '../data';
import CalendarExceptionForm from './calendar-exception-form.vue';

defineOptions({ name: 'CalendarExceptionList' });

const calendar = ref<MdmCalendarApi.Calendar>();
const checkedIds = ref<number[]>([]);

const drawerTitle = computed(
  () =>
    [calendar.value?.code, calendar.value?.name].filter(Boolean).join(' · ') ||
    '日历模板',
);

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: CalendarExceptionForm,
  destroyOnClose: true,
});

function handleRowCheckboxChange({
  records,
}: {
  records: MdmCalendarApi.CalendarException[];
}) {
  checkedIds.value = records.map((item) => item.id);
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useExceptionGridColumns(),
    height: '460px',
    keepSource: true,
    pagerConfig: { enabled: false },
    proxyConfig: {
      ajax: {
        query: async () => {
          if (!calendar.value?.id) {
            return { list: [], total: 0 };
          }
          return await getCalendarExceptionPage({
            calendarId: calendar.value.id,
            pageNo: 1,
            pageSize: 200,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      custom: true,
      refresh: true,
      search: false,
      zoom: true,
    },
  } as VxeTableGridOptions<MdmCalendarApi.CalendarException>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});

async function handleRefresh() {
  checkedIds.value = [];
  await gridApi.query();
}

function handleCreate() {
  if (!calendar.value?.id) {
    return;
  }
  formModalApi.setData({ calendarId: calendar.value.id }).open();
}

function handleEdit(row: MdmCalendarApi.CalendarException) {
  formModalApi.setData(row).open();
}

async function handleDelete(row: MdmCalendarApi.CalendarException) {
  await deleteCalendarException(row.id);
  ElMessage.success('例外已删除');
  await handleRefresh();
}

async function handleDeleteBatch() {
  await confirm('确定批量删除选中的日期例外吗？');
  await deleteCalendarExceptionList(checkedIds.value);
  ElMessage.success('例外已批量删除');
  await handleRefresh();
}

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      calendar.value = undefined;
      checkedIds.value = [];
      return;
    }
    calendar.value = drawerApi.getData<MdmCalendarApi.Calendar>();
    checkedIds.value = [];
    await gridApi.query();
  },
});
</script>

<template>
  <Drawer :title="`日期例外 - ${drawerTitle}`" class="w-full lg:w-3/5">
    <FormModal @success="handleRefresh" />
    <div class="flex flex-col gap-3 px-4 pb-4">
      <ElAlert
        :closable="false"
        show-icon
        title="停工例外可不选班次（表示全天停工）；加班与计划保养必须指定班次和可用分钟数。"
        type="info"
      />
      <Grid table-title="日期例外列表">
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: '新增例外',
                type: 'primary',
                icon: ACTION_ICON.ADD,
                auth: ['mdm:calendar:update'],
                onClick: handleCreate,
              },
              {
                label: '批量删除',
                type: 'danger',
                icon: ACTION_ICON.DELETE,
                auth: ['mdm:calendar:update'],
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
                auth: ['mdm:calendar:update'],
                popConfirm: {
                  title: `确定删除 ${row.exceptionDate ?? row.id} 的例外吗？`,
                  confirm: handleDelete.bind(null, row),
                },
              },
            ]"
          />
        </template>
      </Grid>
    </div>
  </Drawer>
</template>

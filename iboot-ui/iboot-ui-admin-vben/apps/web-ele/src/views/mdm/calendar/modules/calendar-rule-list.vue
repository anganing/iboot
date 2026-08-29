<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmCalendarApi } from '#/api/mdm/calendar';

import { computed, ref } from 'vue';

import { confirm, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { isEmpty } from '@vben/utils';

import { ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteCalendarRule,
  deleteCalendarRuleList,
  getCalendarRulePage,
} from '#/api/mdm/calendar';
import { $t } from '#/locales';

import { useRuleGridColumns } from '../data';
import CalendarRuleForm from './calendar-rule-form.vue';

defineOptions({ name: 'CalendarRuleList' });

const calendar = ref<MdmCalendarApi.Calendar>();
const checkedIds = ref<number[]>([]);

const drawerTitle = computed(
  () =>
    [calendar.value?.code, calendar.value?.name].filter(Boolean).join(' · ') ||
    '日历模板',
);

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: CalendarRuleForm,
  destroyOnClose: true,
});

function handleRowCheckboxChange({
  records,
}: {
  records: MdmCalendarApi.CalendarRule[];
}) {
  checkedIds.value = records.map((item) => item.id);
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useRuleGridColumns(),
    height: '520px',
    keepSource: true,
    pagerConfig: { enabled: false },
    proxyConfig: {
      ajax: {
        query: async () => {
          if (!calendar.value?.id) {
            return { list: [], total: 0 };
          }
          return await getCalendarRulePage({
            calendarId: calendar.value.id,
            pageNo: 1,
            pageSize: 100,
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
  } as VxeTableGridOptions<MdmCalendarApi.CalendarRule>,
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

function handleEdit(row: MdmCalendarApi.CalendarRule) {
  formModalApi.setData(row).open();
}

async function handleDelete(row: MdmCalendarApi.CalendarRule) {
  await deleteCalendarRule(row.id);
  ElMessage.success('规则已删除');
  await handleRefresh();
}

async function handleDeleteBatch() {
  await confirm('确定批量删除选中的常规规则吗？');
  await deleteCalendarRuleList(checkedIds.value);
  ElMessage.success('规则已批量删除');
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
  <Drawer :title="`常规工作模式规则 - ${drawerTitle}`" class="w-full lg:w-3/5">
    <FormModal @success="handleRefresh" />
    <div class="px-4 pb-4">
      <Grid table-title="常规规则列表">
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: '新增规则',
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
                  title: `确定删除“${row.shiftName ?? row.shiftCode ?? row.id}”的规则吗？`,
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

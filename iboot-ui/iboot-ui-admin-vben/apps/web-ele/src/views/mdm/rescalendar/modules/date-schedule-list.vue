<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmResCalendarApi } from '#/api/mdm/rescalendar';

import { computed, ref, useTemplateRef } from 'vue';

import { confirm, useVbenDrawer } from '@vben/common-ui';
import { isEmpty } from '@vben/utils';

import { ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteResCalendar,
  deleteResCalendarList,
  getResCalendarList,
} from '#/api/mdm/rescalendar';
import { $t } from '#/locales';

import { useGridColumns } from '../data';

defineOptions({ name: 'MdmResCalendarDateScheduleList' });

const emit = defineEmits<{
  create: [workDate: string];
  edit: [id: number];
  success: [];
}>();

const workDate = ref<string>();
const checkedIds = ref<number[]>([]);
const drawerTitle = computed(() =>
  workDate.value ? `${workDate.value} 资源安排` : '当日资源安排',
);

function handleRowCheckboxChange({
  records,
}: {
  records: MdmResCalendarApi.ResCalendar[];
}) {
  checkedIds.value = records.map((item) => item.id);
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useGridColumns(),
    height: '520px',
    keepSource: true,
    pagerConfig: { enabled: false },
    proxyConfig: {
      ajax: {
        query: async () => {
          if (!workDate.value) {
            return { list: [], total: 0 };
          }
          const list = await getResCalendarList({
            dateFrom: workDate.value,
            dateTo: workDate.value,
          });
          return { list, total: list.length };
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
  } as VxeTableGridOptions<MdmResCalendarApi.ResCalendar>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});

const gridRef = useTemplateRef('gridRef');
const shouldQueryWhenReady = ref(false);
const isGridReady = ref(false);

function markGridReady() {
  if (!gridRef.value || isGridReady.value) {
    return;
  }
  isGridReady.value = true;
  if (!shouldQueryWhenReady.value) {
    return;
  }
  shouldQueryWhenReady.value = false;
  refresh();
}

async function refresh() {
  checkedIds.value = [];
  await gridApi.query();
}

function handleCreate() {
  if (workDate.value) {
    emit('create', workDate.value);
  }
}

function handleEdit(row: MdmResCalendarApi.ResCalendar) {
  emit('edit', row.id);
}

async function handleDelete(row: MdmResCalendarApi.ResCalendar) {
  await deleteResCalendar(row.id);
  ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.id]));
  await refresh();
  emit('success');
}

async function handleDeleteBatch() {
  await confirm('确定批量删除选中的资源安排吗？');
  await deleteResCalendarList(checkedIds.value);
  ElMessage.success($t('ui.actionMessage.deleteSuccess'));
  await refresh();
  emit('success');
}

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      workDate.value = undefined;
      checkedIds.value = [];
      return;
    }
    workDate.value = drawerApi.getData<{ workDate: string }>().workDate;
    if (!isGridReady.value) {
      shouldQueryWhenReady.value = true;
      return;
    }
    await refresh();
  },
});

defineExpose({ refresh });
</script>

<template>
  <Drawer :title="drawerTitle" class="w-full lg:w-4/5">
    <div class="px-4 pb-4">
      <Grid ref="gridRef" :table-title="drawerTitle" @vue:mounted="markGridReady">
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: $t('ui.actionTitle.create', ['资源日历']),
                type: 'primary',
                icon: ACTION_ICON.ADD,
                auth: ['mdm:res-calendar:create'],
                onClick: handleCreate,
              },
              {
                label: $t('ui.actionTitle.deleteBatch'),
                type: 'danger',
                icon: ACTION_ICON.DELETE,
                auth: ['mdm:res-calendar:delete'],
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
                auth: ['mdm:res-calendar:update'],
                onClick: handleEdit.bind(null, row),
              },
              {
                label: $t('common.delete'),
                type: 'danger',
                link: true,
                icon: ACTION_ICON.DELETE,
                auth: ['mdm:res-calendar:delete'],
                popConfirm: {
                  title: `确定删除资源安排“${row.resCode ?? row.resName ?? row.id}”吗？`,
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

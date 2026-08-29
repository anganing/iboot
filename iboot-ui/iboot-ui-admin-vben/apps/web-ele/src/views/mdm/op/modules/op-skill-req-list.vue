<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmOpApi } from '#/api/mdm/op';
import type { MdmSkillApi } from '#/api/mdm/skill';

import { computed, ref } from 'vue';

import { confirm, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { isEmpty } from '@vben/utils';

import { ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteOpSkillReq,
  deleteOpSkillReqList,
  getOpSkillReqPage,
} from '#/api/mdm/skill';
import { $t } from '#/locales';

import { useOpSkillReqGridColumns } from '../op-skill-req-data';
import OpSkillReqForm from './op-skill-req-form.vue';

defineOptions({ name: 'OpSkillReqList' });

const op = ref<MdmOpApi.Op>();
const checkedIds = ref<number[]>([]);

const drawerTitle = computed(
  () => [op.value?.code, op.value?.name].filter(Boolean).join(' · ') || '工序',
);
const gridTitle = computed(() =>
  op.value?.name ? `技能要求列表 - ${op.value.name}` : '技能要求列表',
);

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: OpSkillReqForm,
  destroyOnClose: true,
});

function handleRowCheckboxChange({
  records,
}: {
  records: MdmSkillApi.OpSkillReq[];
}) {
  checkedIds.value = records.map((item) => item.id);
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useOpSkillReqGridColumns(),
    height: '520px',
    keepSource: true,
    pagerConfig: { enabled: false },
    proxyConfig: {
      ajax: {
        query: async () => {
          if (!op.value?.id) {
            return { list: [], total: 0 };
          }
          return await getOpSkillReqPage({
            opId: op.value.id,
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
  } as VxeTableGridOptions<MdmSkillApi.OpSkillReq>,
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
  if (!op.value?.id) {
    return;
  }
  formModalApi.setData({ opId: op.value.id }).open();
}

function handleEdit(row: MdmSkillApi.OpSkillReq) {
  formModalApi.setData(row).open();
}

async function handleDelete(row: MdmSkillApi.OpSkillReq) {
  await deleteOpSkillReq(row.id);
  ElMessage.success('技能要求已删除');
  await handleRefresh();
}

async function handleDeleteBatch() {
  await confirm('确定批量删除选中的技能要求吗？');
  await deleteOpSkillReqList(checkedIds.value);
  ElMessage.success('技能要求已批量删除');
  await handleRefresh();
}

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      op.value = undefined;
      checkedIds.value = [];
      return;
    }
    op.value = drawerApi.getData<MdmOpApi.Op>();
    checkedIds.value = [];
    await gridApi.query();
  },
});
</script>

<template>
  <Drawer :title="`工序技能要求 - ${drawerTitle}`" class="w-full lg:w-3/5">
    <FormModal @success="handleRefresh" />
    <div class="px-4 pb-4">
      <Grid :table-title="gridTitle">
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: '新增技能要求',
                type: 'primary',
                icon: ACTION_ICON.ADD,
                auth: ['mdm:skill:update'],
                onClick: handleCreate,
              },
              {
                label: '批量删除',
                type: 'danger',
                icon: ACTION_ICON.DELETE,
                auth: ['mdm:skill:update'],
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
                auth: ['mdm:skill:update'],
                onClick: handleEdit.bind(null, row),
              },
              {
                label: $t('common.delete'),
                type: 'danger',
                link: true,
                icon: ACTION_ICON.DELETE,
                auth: ['mdm:skill:update'],
                popConfirm: {
                  title: `确定删除技能要求“${row.skillCode ?? row.skillName ?? row.id}”吗？`,
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

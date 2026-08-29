<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmTeamApi } from '#/api/mdm/team';

import { computed, ref } from 'vue';

import { confirm, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { isEmpty } from '@vben/utils';

import { ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteTeamMember,
  deleteTeamMemberList,
  getTeamMemberPage,
} from '#/api/mdm/team';
import { $t } from '#/locales';

import { useTeamMemberGridColumns } from '../team-member-data';
import TeamMemberForm from './team-member-form.vue';

defineOptions({ name: 'TeamMemberList' });

const emit = defineEmits(['success']);

const team = ref<MdmTeamApi.Team>();
const checkedIds = ref<number[]>([]);

const drawerTitle = computed(() =>
  [team.value?.code, team.value?.name].filter(Boolean).join(' · ') || '班组成员',
);
const gridTitle = computed(() =>
  team.value?.name ? `班组成员列表 - ${team.value.name}` : '班组成员列表',
);

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: TeamMemberForm,
  destroyOnClose: true,
});

function handleRowCheckboxChange({
  records,
}: {
  records: MdmTeamApi.TeamMember[];
}) {
  checkedIds.value = records.map((item) => item.id);
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useTeamMemberGridColumns(),
    height: '520px',
    keepSource: true,
    pagerConfig: { enabled: false },
    proxyConfig: {
      ajax: {
        query: async () => {
          if (!team.value?.id) {
            return { list: [], total: 0 };
          }
          return await getTeamMemberPage({
            teamId: team.value.id,
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
  } as VxeTableGridOptions<MdmTeamApi.TeamMember>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});

/** 成员变动后刷新列表，并通知外层刷新班组的成员数量 */
async function handleRefresh() {
  checkedIds.value = [];
  await gridApi.query();
  emit('success');
}

function handleCreate() {
  if (!team.value?.id) {
    return;
  }
  formModalApi.setData({ teamId: team.value.id }).open();
}

function handleEdit(row: MdmTeamApi.TeamMember) {
  formModalApi.setData(row).open();
}

async function handleDelete(row: MdmTeamApi.TeamMember) {
  await deleteTeamMember(row.id);
  ElMessage.success('成员已移除');
  await handleRefresh();
}

async function handleDeleteBatch() {
  await confirm('确定批量移除选中的班组成员吗？');
  await deleteTeamMemberList(checkedIds.value);
  ElMessage.success('成员已批量移除');
  await handleRefresh();
}

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      team.value = undefined;
      checkedIds.value = [];
      return;
    }
    team.value = drawerApi.getData<MdmTeamApi.Team>();
    checkedIds.value = [];
    await gridApi.query();
  },
});
</script>

<template>
  <Drawer :title="`班组成员管理 - ${drawerTitle}`" class="w-full lg:w-3/5">
    <FormModal @success="handleRefresh" />
    <div class="px-4 pb-4">
      <Grid :table-title="gridTitle">
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: '新增成员',
                type: 'primary',
                icon: ACTION_ICON.ADD,
                auth: ['mdm:team:update'],
                onClick: handleCreate,
              },
              {
                label: '批量移除',
                type: 'danger',
                icon: ACTION_ICON.DELETE,
                auth: ['mdm:team:update'],
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
                auth: ['mdm:team:update'],
                onClick: handleEdit.bind(null, row),
              },
              {
                label: '移除',
                type: 'danger',
                link: true,
                icon: ACTION_ICON.DELETE,
                auth: ['mdm:team:update'],
                popConfirm: {
                  title: `确定移除成员“${row.userName ?? row.id}”吗？`,
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

<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmSkillApi } from '#/api/mdm/skill';

import { computed, ref } from 'vue';

import { confirm, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { isEmpty } from '@vben/utils';

import { ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteMemberSkill,
  deleteMemberSkillList,
  getMemberSkillPage,
} from '#/api/mdm/skill';
import { $t } from '#/locales';

import { useMemberSkillGridColumns } from '../member-skill-data';
import MemberSkillForm from './member-skill-form.vue';

defineOptions({ name: 'MemberSkillList' });

const skill = ref<MdmSkillApi.Skill>();
const checkedIds = ref<number[]>([]);

const drawerTitle = computed(
  () =>
    [skill.value?.code, skill.value?.name].filter(Boolean).join(' · ') ||
    '人员技能',
);
const gridTitle = computed(() =>
  skill.value?.name ? `持有人员列表 - ${skill.value.name}` : '持有人员列表',
);

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: MemberSkillForm,
  destroyOnClose: true,
});

function handleRowCheckboxChange({
  records,
}: {
  records: MdmSkillApi.MemberSkill[];
}) {
  checkedIds.value = records.map((item) => item.id);
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useMemberSkillGridColumns(),
    height: '520px',
    keepSource: true,
    pagerConfig: { enabled: false },
    proxyConfig: {
      ajax: {
        query: async () => {
          if (!skill.value?.id) {
            return { list: [], total: 0 };
          }
          return await getMemberSkillPage({
            skillId: skill.value.id,
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
  } as VxeTableGridOptions<MdmSkillApi.MemberSkill>,
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
  if (!skill.value?.id) {
    return;
  }
  formModalApi.setData({ skillId: skill.value.id }).open();
}

function handleEdit(row: MdmSkillApi.MemberSkill) {
  formModalApi.setData(row).open();
}

async function handleDelete(row: MdmSkillApi.MemberSkill) {
  await deleteMemberSkill(row.id);
  ElMessage.success('人员技能已删除');
  await handleRefresh();
}

async function handleDeleteBatch() {
  await confirm('确定批量删除选中的人员技能吗？');
  await deleteMemberSkillList(checkedIds.value);
  ElMessage.success('人员技能已批量删除');
  await handleRefresh();
}

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      skill.value = undefined;
      checkedIds.value = [];
      return;
    }
    skill.value = drawerApi.getData<MdmSkillApi.Skill>();
    checkedIds.value = [];
    await gridApi.query();
  },
});
</script>

<template>
  <Drawer :title="`人员技能 - ${drawerTitle}`" class="w-full lg:w-3/5">
    <FormModal @success="handleRefresh" />
    <div class="px-4 pb-4">
      <Grid :table-title="gridTitle">
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: '新增人员技能',
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
                  title: `确定删除人员“${row.userName ?? row.id}”的技能记录吗？`,
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

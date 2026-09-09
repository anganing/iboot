<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemMobileMenuApi } from '#/api/system/mobile-menu';

import { ref } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteMobileMenu, getMobileMenuList } from '#/api/system/mobile-menu';
import { $t } from '#/locales';

import {
  MobileMenuTypeEnum,
  useGridColumns,
  useGridFormSchema,
} from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建移动端菜单 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 添加下级移动端菜单 */
function handleAppend(row: SystemMobileMenuApi.MobileMenu) {
  formModalApi.setData({ parentId: row.id }).open();
}

/** 编辑移动端菜单 */
function handleEdit(row: SystemMobileMenuApi.MobileMenu) {
  formModalApi.setData(row).open();
}

/** 删除移动端菜单 */
async function handleDelete(row: SystemMobileMenuApi.MobileMenu) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.name]),
  });
  try {
    await deleteMobileMenu(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 切换树形展开/收缩状态 */
const isExpanded = ref(false);
function handleExpand() {
  isExpanded.value = !isExpanded.value;
  gridApi.grid.setAllTreeExpand(isExpanded.value);
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: false,
    },
    proxyConfig: {
      ajax: {
        query: async (_params, formValues) => {
          return await getMobileMenuList(formValues);
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
    treeConfig: {
      parentField: 'parentId',
      rowField: 'id',
      transform: true,
      reserve: true,
    },
  } as VxeTableGridOptions<SystemMobileMenuApi.MobileMenu>,
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="移动端菜单列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['移动端菜单']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['system:mobile-menu:create'],
              onClick: handleCreate,
            },
            {
              label: isExpanded ? '收缩' : '展开',
              type: 'primary',
              onClick: handleExpand,
            },
          ]"
        />
      </template>
      <template #name="{ row }">
        <div class="flex w-full items-center gap-1">
          <div class="size-5 flex-shrink-0">
            <IconifyIcon
              v-if="row.type === MobileMenuTypeEnum.BUTTON"
              icon="carbon:square-outline"
              class="size-full"
            />
            <IconifyIcon
              v-else-if="row.icon"
              :icon="row.icon"
              class="size-full"
            />
            <IconifyIcon v-else icon="carbon:circle-dash" class="size-full" />
          </div>
          <span class="flex-auto">{{ row.name }}</span>
        </div>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '新增下级',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.ADD,
              auth: ['system:mobile-menu:create'],
              onClick: handleAppend.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['system:mobile-menu:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['system:mobile-menu:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.name]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>

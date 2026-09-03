<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemNoticeApi } from '#/api/system/notice';

import { Page, useVbenModal } from '@vben/common-ui';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getMyNoticePage } from '#/api/system/notice';

import { useGridColumns } from './data';
import Detail from './modules/detail.vue';

const [DetailModal, detailModalApi] = useVbenModal({
  connectedComponent: Detail,
  destroyOnClose: true,
});

/** 查看公告详情 */
function handleDetail(row: SystemNoticeApi.Notice) {
  detailModalApi.setData({ id: row.id }).open();
}

const [Grid] = useVbenVxeGrid({
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }) => {
          return await getMyNoticePage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
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
    },
  } as VxeTableGridOptions<SystemNoticeApi.Notice>,
});
</script>

<template>
  <Page auto-content-height>
    <DetailModal />
    <Grid table-title="我的公告">
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '查看',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.VIEW,
              onClick: handleDetail.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>

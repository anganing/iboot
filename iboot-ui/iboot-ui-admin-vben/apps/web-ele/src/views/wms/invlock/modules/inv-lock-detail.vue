<script lang="ts" setup>
import type { WmsInvLockApi } from '#/api/wms/invlock';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { formatDateTime } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { getInvLock } from '#/api/wms/invlock';

/**
 * 库存冻结只读详情。
 *
 * 列表 16 列放不下全部字段（来源业务类型、幂等键、库存组织/容器/项目维度只在详情里看），
 * 而冻结记录没有新增/修改弹窗（后端只留查询、导出、解冻），双击冻结编号是唯一看全字段的入口。
 */
defineOptions({ name: 'WmsInvLockDetail' });

const lock = ref<WmsInvLockApi.InvLock>();

const drawerTitle = computed(() =>
  lock.value?.lockNo ? `库存冻结 ${lock.value.lockNo}` : '库存冻结详情',
);

/** 剩余冻结与列表列同一口径：冻结量减已解冻量，不落库，避免两处算法不一致 */
const remainQty = computed(
  () => Number(lock.value?.qty ?? 0) - Number(lock.value?.releasedQty ?? 0),
);

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      lock.value = undefined;
      return;
    }
    const { id } = drawerApi.getData<{ id?: number }>();
    if (!id) return;
    drawerApi.setState({ loading: true });
    try {
      // 必须回查 get：列表行是裁剪过的展示字段，详情要拿 JOIN 仓库/库位/批次后的完整数据
      lock.value = await getInvLock(id);
    } catch {
      // 列表刷新后记录可能已被删除，后端返回 null 或抛“不存在”，此时保持空态即可
      lock.value = undefined;
    } finally {
      drawerApi.setState({ loading: false });
    }
  },
});

/** 空值统一显示短横线，避免抽屉里出现空白格 */
function text(value: null | number | string | undefined) {
  return value === null || value === undefined || value === '' ? '-' : value;
}
</script>

<template>
  <Drawer
    :title="drawerTitle"
    class="w-full lg:w-3/5"
    content-class="flex h-full min-h-0 flex-col overflow-hidden p-0"
  >
    <div class="flex h-full min-h-0 flex-col overflow-y-auto px-4 pb-4">
      <ElDescriptions v-if="lock" :column="2" border size="small">
        <ElDescriptionsItem label="冻结编号">
          {{ text(lock.lockNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="冻结状态">
          {{
            getDictLabel(DICT_TYPE.WMS_LOCK_STATUS, lock.status) ||
            text(lock.status)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源业务类型">
          {{
            getDictLabel(DICT_TYPE.WMS_SOURCE_BIZ_TYPE, lock.bizType) ||
            text(lock.bizType)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源业务单号">
          {{ text(lock.bizNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="物料编码">{{ text(lock.matCode) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="物料名称">{{ text(lock.matName) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="批次号">{{ text(lock.lotNo) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="质量状态">
          {{
            getDictLabel(DICT_TYPE.WMS_QUALITY_STATUS, lock.qualityStatus) ||
            text(lock.qualityStatus)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="仓库">{{ text(lock.whName) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="仓库编码">{{ text(lock.whCode) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="库位编码">{{ text(lock.locCode) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="冻结数量">{{ text(lock.qty) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="已解冻数量">
          {{ text(lock.releasedQty) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="剩余冻结">
          {{ remainQty }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="冻结原因">{{ text(lock.reason) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="备注">{{ text(lock.remark) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="幂等键">
          {{ text(lock.idempotencyKey) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="创建时间">
          {{ lock.createTime ? formatDateTime(lock.createTime) : '-' }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <div v-else class="py-10 text-center text-gray-400">未找到该记录</div>
    </div>
  </Drawer>
</template>

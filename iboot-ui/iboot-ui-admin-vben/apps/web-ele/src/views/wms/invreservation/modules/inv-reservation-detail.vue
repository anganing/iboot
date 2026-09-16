<script lang="ts" setup>
import type { WmsInvReservationApi } from '#/api/wms/invreservation';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { formatDateTime } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { getInvReservation } from '#/api/wms/invreservation';

/**
 * 库存预留只读详情。
 *
 * 列表 17 列放不下全部字段（来源业务类型、幂等键、库存组织/容器/项目维度只在详情里看），
 * 而预留记录没有新增/修改弹窗（后端只留查询、导出、释放），双击预留编号是唯一看全字段的入口。
 */
defineOptions({ name: 'WmsInvReservationDetail' });

const reservation = ref<WmsInvReservationApi.InvReservation>();

const drawerTitle = computed(() =>
  reservation.value?.reservationNo
    ? `库存预留 ${reservation.value.reservationNo}`
    : '库存预留详情',
);

/** 剩余占用与列表列同一口径：预留量减已释放量，不落库，避免两处算法不一致 */
const remainQty = computed(
  () =>
    Number(reservation.value?.qty ?? 0) -
    Number(reservation.value?.releasedQty ?? 0),
);

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      reservation.value = undefined;
      return;
    }
    const { id } = drawerApi.getData<{ id?: number }>();
    if (!id) return;
    drawerApi.setState({ loading: true });
    try {
      // 必须回查 get：列表行是裁剪过的展示字段，详情要拿 JOIN 仓库/库位/批次后的完整数据
      reservation.value = await getInvReservation(id);
    } catch {
      // 列表刷新后记录可能已被删除，后端返回 null 或抛“不存在”，此时保持空态即可
      reservation.value = undefined;
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
      <ElDescriptions v-if="reservation" :column="2" border size="small">
        <ElDescriptionsItem label="预留编号">
          {{ text(reservation.reservationNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="预留状态">
          {{
            getDictLabel(
              DICT_TYPE.WMS_RESERVATION_STATUS,
              reservation.status,
            ) || text(reservation.status)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源业务类型">
          {{
            getDictLabel(DICT_TYPE.WMS_SOURCE_BIZ_TYPE, reservation.bizType) ||
            text(reservation.bizType)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源业务单号">
          {{ text(reservation.bizNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="来源业务行号">
          {{ text(reservation.bizLineNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="质量状态">
          {{
            getDictLabel(
              DICT_TYPE.WMS_QUALITY_STATUS,
              reservation.qualityStatus,
            ) || text(reservation.qualityStatus)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="物料编码">
          {{ text(reservation.matCode) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="物料名称">
          {{ text(reservation.matName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="批次号">
          {{ text(reservation.lotNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="仓库">
          {{ text(reservation.whName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="仓库编码">
          {{ text(reservation.whCode) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="库位编码">
          {{ text(reservation.locCode) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="预留数量">
          {{ text(reservation.qty) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="已处理数量">
          {{ text(reservation.releasedQty) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="剩余数量">{{ remainQty }}</ElDescriptionsItem>
        <ElDescriptionsItem label="失效时间">
          {{ reservation.expireTime ? formatDateTime(reservation.expireTime) : '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="幂等键">
          {{ text(reservation.idempotencyKey) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="备注">
          {{ text(reservation.remark) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="创建时间">
          {{
            reservation.createTime ? formatDateTime(reservation.createTime) : '-'
          }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <div v-else class="py-10 text-center text-gray-400">未找到该记录</div>
    </div>
  </Drawer>
</template>

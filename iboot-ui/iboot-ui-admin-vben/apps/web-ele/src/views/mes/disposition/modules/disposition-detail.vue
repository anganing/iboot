<script lang="ts" setup>
import type { MesDispositionApi } from '#/api/mes/disposition';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { formatDate } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { getDisposition } from '#/api/mes/disposition';

/**
 * 不合格品处置单只读详情。
 *
 * 列表按展示裁剪，处置单的执行结果落在“联动业务类型/单号”上，审批侧还要看审批人与流程审批时间，
 * 这些都在详情里一次给全。字段清单与审批嵌入用的 disposition-process-detail.vue 对齐，
 * 那个组件由 bpm 挂载、容器不同，不能直接复用。
 */
defineOptions({ name: 'MesDispositionDetail' });

const disposition = ref<MesDispositionApi.Disposition>();

const drawerTitle = computed(() =>
  disposition.value?.dispositionNo
    ? `不合格品处置单 ${disposition.value.dispositionNo}`
    : '处置单详情',
);

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      disposition.value = undefined;
      return;
    }
    const { id } = drawerApi.getData<{ id?: number }>();
    if (!id) return;
    drawerApi.setState({ loading: true });
    try {
      // 必须回查 get：列表行是裁剪过的展示字段，详情要拿 JOIN 后的完整数据
      disposition.value = await getDisposition(id);
    } catch {
      // 列表刷新后记录可能已被删除，后端会抛“不存在”，此时保持空态即可
      disposition.value = undefined;
    } finally {
      drawerApi.setState({ loading: false });
    }
  },
});

/** 空值统一显示短横线，避免抽屉里出现空白格 */
function text(value: null | number | string | undefined) {
  return value === null || value === undefined || value === '' ? '-' : value;
}

/** 时间空值给短横线：formatDate 传空会渲染成 Invalid Date */
function time(value: number | string | undefined) {
  return value ? formatDate(value) : '-';
}
</script>

<template>
  <Drawer
    :title="drawerTitle"
    class="w-full lg:w-3/5"
    content-class="flex h-full min-h-0 flex-col overflow-hidden p-0"
  >
    <div class="flex h-full min-h-0 flex-col overflow-y-auto px-4 pb-4">
      <ElDescriptions v-if="disposition" :column="2" border size="small">
        <ElDescriptionsItem label="处置单号">
          {{ text(disposition.dispositionNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="质检任务号">
          {{ text(disposition.taskNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="物料编码">
          {{ text(disposition.matCode) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="物料名称">
          {{ text(disposition.matName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="处置类型">
          {{
            getDictLabel(
              DICT_TYPE.MES_DISPOSITION_TYPE,
              disposition.dispositionType,
            ) || text(disposition.dispositionType)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="处置数量">
          {{ text(disposition.qty) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="联动业务类型">
          {{
            getDictLabel(DICT_TYPE.MES_BIZ_TYPE, disposition.targetBizType) ||
            text(disposition.targetBizType)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="联动业务单号">
          {{ text(disposition.targetBizNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="单据状态">
          {{
            getDictLabel(DICT_TYPE.MES_DISPOSITION_STATUS, disposition.status) ||
            text(disposition.status)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="流程状态">
          <!-- approvalStatus 为空表示该单据未走过流程，渲染成 0 会变成「已取消」的假标签 -->
          <template
            v-if="
              disposition.approvalStatus !== undefined &&
              disposition.approvalStatus !== null
            "
          >
            {{
              getDictLabel(
                DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS,
                disposition.approvalStatus,
              ) || disposition.approvalStatus
            }}
          </template>
          <template v-else>-</template>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="审批人">
          {{ text(disposition.approvedByName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="审批时间">
          {{ time(disposition.approveTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="审批完成时间">
          {{ time(disposition.approvalTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="创建时间">
          {{ time(disposition.createTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="审批意见" :span="2">
          {{ text(disposition.approvalReason) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="处置说明" :span="2">
          {{ text(disposition.remark) }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <div v-else class="py-10 text-center text-gray-400">未找到该记录</div>
    </div>
  </Drawer>
</template>

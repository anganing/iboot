<script lang="ts" setup>
import type { MesProdReportApi } from '#/api/mes/prodreport';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { formatDate } from '@vben/utils';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { getProdReport } from '#/api/mes/prodreport';

/**
 * 生产报工记录只读详情。
 *
 * 报工是过账类留痕，页面只有“取消”一个动作、没有修改弹窗，17 列列表又放不下
 * 幂等键与作业起止时间，双击报工单号开本抽屉是看全字段的唯一入口。
 */
defineOptions({ name: 'MesProdReportDetail' });

const prodReport = ref<MesProdReportApi.ProdReport>();

const drawerTitle = computed(() =>
  prodReport.value?.reportNo
    ? `生产报工 ${prodReport.value.reportNo}`
    : '生产报工详情',
);

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      prodReport.value = undefined;
      return;
    }
    const { id } = drawerApi.getData<{ id?: number }>();
    if (!id) return;
    drawerApi.setState({ loading: true });
    try {
      // 必须回查 get：列表行是裁剪过的展示字段，详情要拿 JOIN 后的完整数据
      prodReport.value = await getProdReport(id);
    } catch {
      // 列表刷新后记录可能已被删除，后端会抛“不存在”，此时保持空态即可
      prodReport.value = undefined;
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
      <ElDescriptions v-if="prodReport" :column="2" border size="small">
        <ElDescriptionsItem label="报工单号">
          {{ text(prodReport.reportNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          {{
            getDictLabel(DICT_TYPE.MES_PROD_REPORT_STATUS, prodReport.status) ||
            text(prodReport.status)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="生产工单号">
          {{ text(prodReport.prodOrderNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="作业工单号">
          {{ text(prodReport.workOrderNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="工序任务号">
          {{ text(prodReport.opTaskNo) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="工序名称">
          {{ text(prodReport.opName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="报工人员">
          {{ text(prodReport.workerName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="生产资源">
          {{ text(prodReport.resName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="班次">
          {{ text(prodReport.shiftName) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="合格数量">
          {{ text(prodReport.qtyGood) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="报废数量">
          {{ text(prodReport.qtyScrap) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="返工数量">
          {{ text(prodReport.qtyRework) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="作业开始时间">
          {{ time(prodReport.startTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="作业结束时间">
          {{ time(prodReport.endTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="报工时间">
          {{ time(prodReport.reportTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="创建时间">
          {{ time(prodReport.createTime) }}
        </ElDescriptionsItem>
        <!-- 幂等键是防止同一作业动作重复报账的证据，列表没有这列，只在详情可查；
             workerId / resId / shiftId 是裸外键，一律不展示 -->
        <ElDescriptionsItem label="幂等键" :span="2">
          {{ text(prodReport.idempotencyKey) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="备注" :span="2">
          {{ text(prodReport.remark) }}
        </ElDescriptionsItem>
      </ElDescriptions>
      <div v-else class="py-10 text-center text-gray-400">未找到该记录</div>
    </div>
  </Drawer>
</template>

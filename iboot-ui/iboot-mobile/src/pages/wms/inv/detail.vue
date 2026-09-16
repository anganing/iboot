<script lang="ts" setup>
import type { InvBalance } from '@/api/wms/invbalance'
import { getInvBalance } from '@/api/wms/invbalance'
import { dictLabel } from '@/utils/dict'
import { availableQty, formatQty, qualityStatusTagType } from './utils'

defineOptions({ name: 'WmsInvDetail' })

definePage({
  style: {
    navigationBarTitleText: '库存详情',
  },
})

/**
 * 主键用字符串承接，**不要 `Number()`**。
 *
 * 后端主键是雪花 ID（19 位，如 `2098710811613970434`），超出 JS 的
 * `Number.MAX_SAFE_INTEGER`（9007199254740991，16 位）——转成 number 会丢精度
 * （实测末三位 `434` → `400`），回查必然查不到，且不报错、只显示"不存在"。
 * URL query 本身就是字符串，直接透传即可。
 */
const id = ref('')
const detail = ref<InvBalance>()
const loading = ref(true)

async function loadDetail() {
  if (!id.value)
    return
  loading.value = true
  try {
    detail.value = await getInvBalance(id.value)
  }
  catch (error) {
    console.error('获取库存详情失败', error)
  }
  finally {
    loading.value = false
  }
}

onLoad((options) => {
  id.value = options?.id ? String(options.id) : ''
  loadDetail()
})
</script>

<template>
  <view class="min-h-screen bg-[#f5f7fb] p-4">
    <view v-if="loading" class="pt-20 text-center text-sm text-gray-400">
      加载中...
    </view>

    <template v-else-if="detail">
      <view class="rounded-lg bg-white p-4 shadow-sm">
        <text class="block text-base text-gray-900 font-semibold">{{ detail.matCode }} {{ detail.matName }}</text>
        <text v-if="detail.matSpec" class="mt-1 block text-xs text-gray-400">规格：{{ detail.matSpec }}</text>
      </view>

      <!-- 可用数量是现场最关心的指标（还能领多少），做最大；现存/预留/冻结是拆解项，做次要信息 -->
      <view class="mt-3 rounded-lg bg-white p-4 shadow-sm">
        <view class="text-center">
          <text class="text-2xs text-gray-400">可用数量</text>
          <view class="mt-1 flex items-baseline justify-center">
            <text class="text-3xl text-primary font-bold">{{ formatQty(availableQty(detail)) }}</text>
            <text class="ml-1 text-xs text-gray-400">{{ detail.uomCode || '' }}</text>
          </view>
        </view>
        <view class="mt-4 flex items-center border-t border-[#edf0f4] pt-3">
          <view class="flex-1 text-center">
            <text class="block text-2xs text-gray-400">现存</text>
            <text class="mt-1 block text-sm text-gray-700">{{ formatQty(detail.qtyOnhand) }}</text>
          </view>
          <view class="flex-1 text-center">
            <text class="block text-2xs text-gray-400">预留</text>
            <text class="mt-1 block text-sm text-gray-700">{{ formatQty(detail.qtyReserved) }}</text>
          </view>
          <view class="flex-1 text-center">
            <text class="block text-2xs text-gray-400">冻结</text>
            <text class="mt-1 block text-sm text-gray-700">{{ formatQty(detail.qtyFrozen) }}</text>
          </view>
        </view>
      </view>

      <wd-cell-group custom-class="mt-3 rounded-2xl overflow-hidden">
        <wd-cell title="物料编码" :value="detail.matCode || '-'" />
        <wd-cell title="物料名称" :value="detail.matName || '-'" />
        <wd-cell title="规格型号" :value="detail.matSpec || '-'" />
        <wd-cell title="基准单位" :value="detail.uomCode || '-'" />
        <wd-cell title="批次号" :value="detail.lotNo || '-'" />
        <wd-cell title="仓库" :value="detail.whName || detail.whCode || '-'" />
        <wd-cell title="库位" :value="detail.locCode || '-'" />
        <wd-cell v-if="detail.projectName" title="归属项目" :value="detail.projectName" />
        <wd-cell title="质量状态">
          <wd-tag :type="qualityStatusTagType(detail.qualityStatus)" size="small" variant="light">
            {{ dictLabel('wms_quality_status', detail.qualityStatus) }}
          </wd-tag>
        </wd-cell>
        <wd-cell title="库存范围" :value="dictLabel('wms_stock_scope', detail.stockScope)" />
        <wd-cell v-if="detail.remark" title="备注" :value="detail.remark" />
      </wd-cell-group>
    </template>

    <view v-else class="pt-20 text-center text-sm text-gray-400">
      未找到库存记录
    </view>
  </view>
</template>

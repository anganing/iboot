<script lang="ts" setup>
import type { InvBalance } from '@/api/wms/invbalance'
import { getInvBalancePage } from '@/api/wms/invbalance'
import { dictLabel } from '@/utils/dict'
import { availableQty, formatQty, qualityStatusTagType } from './utils'

defineOptions({ name: 'WmsInv' })

definePage({
  style: {
    navigationBarTitleText: '库存查询',
  },
})

const pagingRef = ref<ZPagingRef>()
const dataList = ref<InvBalance[]>([])
const matCode = ref('')
const lotNo = ref('')

/**
 * 现场查料的高频动作就是「扫/输个编码或批次」，所以只留两个条件。
 * 仓库、质量状态这类低频条件不在移动端堆叠。
 */
async function queryList(pageNo: number, pageSize: number) {
  try {
    const result = await getInvBalancePage({
      pageNo,
      pageSize,
      matCode: matCode.value.trim() || undefined,
      lotNo: lotNo.value.trim() || undefined,
    })
    pagingRef.value?.completeByTotal(result.list, result.total)
  }
  catch (error) {
    console.error('获取库存列表失败', error)
    pagingRef.value?.complete(false)
  }
}

function onFilter() {
  pagingRef.value?.reload()
}

function onReset() {
  matCode.value = ''
  lotNo.value = ''
  pagingRef.value?.reload()
}

function onItemClick(item: InvBalance) {
  uni.navigateTo({ url: `/pages/wms/inv/detail?id=${item.id}` })
}
</script>

<template>
  <view class="h-full bg-[#f5f7fb]">
    <z-paging
      ref="pagingRef"
      v-model="dataList"
      :default-page-size="10"
      bg-color="#f5f7fb"
      empty-view-text="没有查到库存"
      safe-area-inset-bottom
      @query="queryList"
    >
      <template #top>
        <view class="bg-white px-4 pb-3 pt-3">
          <view class="flex items-center rounded-lg bg-[#f5f7fb] px-3 py-2">
            <wd-icon name="search" size="18px" class="shrink-0 text-gray-400" />
            <input
              v-model="matCode"
              class="ml-2 min-w-0 flex-1 bg-transparent text-sm"
              placeholder="物料编码"
              placeholder-class="text-gray-400"
              confirm-type="search"
              @confirm="onFilter"
            >
          </view>
          <view class="mt-2 flex items-center rounded-lg bg-[#f5f7fb] px-3 py-2">
            <!-- 本版零扫码能力，不放 scan 图标：二维码图标不响应点击会诱导用户去点 -->
            <wd-icon name="inventory-management" size="18px" class="shrink-0 text-gray-400" />
            <input
              v-model="lotNo"
              class="ml-2 min-w-0 flex-1 bg-transparent text-sm"
              placeholder="批次号"
              placeholder-class="text-gray-400"
              confirm-type="search"
              @confirm="onFilter"
            >
          </view>
          <view class="mt-3 flex items-center">
            <wd-button size="small" plain custom-class="mr-2 flex-1" @click="onReset">
              重置
            </wd-button>
            <wd-button size="small" custom-class="flex-1" @click="onFilter">
              筛选
            </wd-button>
          </view>
        </view>
      </template>

      <view
        v-for="item in dataList"
        :key="item.id"
        class="mx-4 mt-3 rounded-lg bg-white p-4 shadow-sm"
        @click="onItemClick(item)"
      >
        <view class="flex items-start">
          <view class="min-w-0 flex-1">
            <view class="flex items-center">
              <text class="truncate text-sm text-gray-800 font-semibold">{{ item.matCode }}</text>
              <text class="ml-2 truncate text-sm text-gray-600">{{ item.matName }}</text>
            </view>
            <text v-if="item.matSpec" class="mt-1 block truncate text-xs text-gray-400">
              规格：{{ item.matSpec }}
            </text>
            <view class="mt-1 flex items-center text-xs text-gray-400">
              <text class="truncate">{{ item.whName || item.whCode || '-' }}</text>
              <text class="mx-1">/</text>
              <text class="truncate">{{ item.locCode || '-' }}</text>
            </view>
            <view v-if="item.lotNo" class="mt-1 flex items-center text-xs text-gray-400">
              <text>批次：{{ item.lotNo }}</text>
            </view>
          </view>
          <view class="ml-3 shrink-0 text-right">
            <text class="text-2xs text-gray-400">可用</text>
            <view class="flex items-baseline justify-end">
              <text class="text-xl text-primary font-bold">{{ formatQty(availableQty(item)) }}</text>
              <text class="ml-1 text-2xs text-gray-400">{{ item.uomCode || '' }}</text>
            </view>
          </view>
        </view>
        <view class="mt-3 flex items-center border-t border-[#edf0f4] pt-2">
          <text class="text-2xs text-gray-400">
            现存 {{ formatQty(item.qtyOnhand) }} · 预留 {{ formatQty(item.qtyReserved) }} · 冻结 {{ formatQty(item.qtyFrozen) }}
          </text>
          <view class="ml-auto shrink-0">
            <wd-tag :type="qualityStatusTagType(item.qualityStatus)" size="small" variant="light">
              {{ dictLabel('wms_quality_status', item.qualityStatus) }}
            </wd-tag>
          </view>
        </view>
      </view>
    </z-paging>
  </view>
</template>

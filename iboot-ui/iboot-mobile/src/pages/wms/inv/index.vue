<script lang="ts" setup>
import type { InvBalance } from '@/api/wms/invbalance'
import { getInvBalancePage } from '@/api/wms/invbalance'
import { dictLabel } from '@/utils/dict'
import { availableQty, formatQty } from './utils'

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
 * 表格数据源。
 *
 * 不能用 dataList 本身：后端 `qtyOnhand` 等字段名与表头文案不同名是正常的，
 * 但 wd-table 的 `data` 是 `Record<string, any>[]`，而 InvBalance 不含索引签名，
 * 直接传会在 `vue-tsc` 下报类型不兼容。这里统一放宽成 Record。
 */
const tableData = computed<Record<string, unknown>[]>(() => dataList.value)

/**
 * 表格可视高度。
 *
 * wd-table 不传 height 就不纵向滚动（`scrollY` 由 `isDef(height)` 决定），
 * 而 z-paging 的滚动区是 flex 撑开的，其 scroll-view 自身高度要等组件
 * `_setAutoHeight` 用 `windowHeight - scrollViewTop` 算完才有具体值，
 * 所以不能写 `height="100%"`（百分比相对一个高度未定的父级会塌成 0）。
 * 这里按「屏幕高 - 查询条件区估算高度 - 安全区」给一个具体值，表头固定、表体滚动。
 * z-paging 的 `@query` 返回数据后底部会自动追加「加载更多」，表体滚动到底即触发下一页。
 */
const tableHeight = computed(() => {
  const { windowHeight } = uni.getSystemInfoSync()
  // 查询条件区（含 z-paging #top）约 170px，底部安全区预留 24px
  return Math.max(windowHeight - 170 - 24, 240)
})

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

/**
 * 点行进详情。
 *
 * wd-table 的 `row-click` 只给 `{ rowIndex }`（不含行数据），需要自己按索引回 tableData 取。
 * 窄屏表格放不下操作列，行点击是详情页的唯一入口，必须保留。
 */
function onRowClick({ rowIndex }: { rowIndex: number }) {
  const row = tableData.value[rowIndex]
  if (!row)
    return
  uni.navigateTo({ url: `/pages/wms/inv/detail?id=${row.id}` })
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

      <!--
        库存是多列对照数据（物料/批次/仓库/库位/现存/预留/冻结/可用），
        卡片纵向堆叠不便于横向比对，因此改用表格。
        列顺序即展示顺序：fixed 列必须写在最前，wd-table 只支持固定左侧。
      -->
      <view class="inv-table-wrap px-4 pt-3">
        <wd-table
          :data="tableData"
          :height="tableHeight"
          :border="false"
          :ellipsis="true"
          @row-click="onRowClick"
        >
          <wd-table-column prop="matCode" label="物料编码" :width="120" fixed />
          <wd-table-column prop="matName" label="物料名称" :width="140" />
          <wd-table-column prop="matSpec" label="规格" :width="110" />
          <wd-table-column prop="lotNo" label="批次号" :width="110">
            <template #value="{ row }">
              <text>{{ row.lotNo || '-' }}</text>
            </template>
          </wd-table-column>
          <wd-table-column prop="whName" label="仓库" :width="110">
            <template #value="{ row }">
              <text>{{ row.whName || row.whCode || '-' }}</text>
            </template>
          </wd-table-column>
          <wd-table-column prop="locCode" label="库位" :width="100">
            <template #value="{ row }">
              <text>{{ row.locCode || '-' }}</text>
            </template>
          </wd-table-column>
          <!-- 可用量 = 现存 - 预留 - 冻结（后端无此字段，见 utils.ts），是本页主指标 -->
          <wd-table-column prop="qtyOnhand" label="可用" :width="100" align="right">
            <template #value="{ row }">
              <text class="text-primary font-bold">{{ formatQty(availableQty(row)) }}</text>
            </template>
          </wd-table-column>
          <wd-table-column prop="qtyOnhand" label="现存" :width="100" align="right">
            <template #value="{ row }">
              <text>{{ formatQty(row.qtyOnhand) }}</text>
            </template>
          </wd-table-column>
          <wd-table-column prop="qtyReserved" label="预留" :width="100" align="right">
            <template #value="{ row }">
              <text>{{ formatQty(row.qtyReserved) }}</text>
            </template>
          </wd-table-column>
          <wd-table-column prop="qtyFrozen" label="冻结" :width="100" align="right">
            <template #value="{ row }">
              <text>{{ formatQty(row.qtyFrozen) }}</text>
            </template>
          </wd-table-column>
          <wd-table-column prop="qualityStatus" label="质量状态" :width="100" align="center">
            <template #value="{ row }">
              <text>{{ dictLabel('wms_quality_status', row.qualityStatus) || '-' }}</text>
            </template>
          </wd-table-column>
        </wd-table>
      </view>
    </z-paging>
  </view>
</template>

<style lang="scss" scoped>
/*
 * wd-table 的固定列用 position: sticky + 内联 left 偏移实现，祖先元素不能是
 * overflow: hidden / auto（否则 sticky 失效、横向滚动时编码列会跟着跑掉），
 * 所以这里不加 overflow 相关约束。
 */
.inv-table-wrap {
  width: 100%;
  box-sizing: border-box;
}
</style>

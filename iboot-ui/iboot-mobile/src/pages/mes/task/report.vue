<script lang="ts" setup>
import type { OpTask } from '@/api/mes/optask'
import type { ProdReportSubmitParams } from '@/api/mes/prodreport'
import { createProdReport } from '@/api/mes/prodreport'
import { getOpTask } from '@/api/mes/optask'
import { useUserStore } from '@/store/user'
import { opTaskStatusLabel } from '@/utils/dict'

defineOptions({ name: 'MesTaskReport' })

definePage({
  style: {
    navigationBarTitleText: '生产报工',
  },
})

const userStore = useUserStore()

/**
 * 页面参数缺失（从别处直链打开、参数被截断）时只显示错误态，不发任何请求。
 * `opTaskId` 是后端 `@NotNull` 的必填项，缺了必失败，发出去只会在控制台留一条 400。
 */
/**
 * 工序任务主键用字符串承接，**不要 `Number()`**——后端主键是雪花 ID（19 位），
 * 超出 JS 安全整数范围，转 number 会丢精度（末位被改写），提交后端查不到该任务。
 */
const opTaskId = ref('')
const taskNo = ref('')
/** 进页面时补全工序名与计划数量，让工人报工前能核对"报的是哪道工序、还剩多少" */
const detail = ref<OpTask>()
const paramInvalid = computed(() => !opTaskId.value)

// ==================== 幂等键 ====================

/**
 * 本次报工的幂等键，**在打开页面时生成一次，整个页面生命周期内沿用**。
 *
 * `seq` 是本次报工的 `seq`（本页用生成时刻的时间戳充当"第几次报工"），
 * 存进 `ref` 而不是每次提交现算，是为了让**失败重试复用同一个键**：
 * - 提交失败 → 用户改数量 → 再点提交 → 键不变 → 后端幂等命中，不会重复写记录、不会二次推进数量
 * - 每次点击都新造随机键 → 与根本没做幂等等价（契约第七节原话）
 *
 * `rollIdempotencyKey()` 只在**提交成功之后**调用：那次报工已经落定，用户再报一次是
 * 新的一笔业务，必须换新键，否则第二次报工会被判重、数量永远推进不上去。
 *
 * 键必须在 `onLoad` 里就绪，不能等用户点提交才生成——否则先失败一次再进重试路径时，
 * 键的生成时机就变成了"每次点击"。
 */
const seq = ref(0)

/** 生成/重置本次报工的幂等键。仅在页面加载与提交成功后调用 */
function rollIdempotencyKey() {
  seq.value = Date.now()
}

/** 构造幂等键；`taskNo` 为空时回落工序任务 id，保证键始终可读且不含空段 */
function buildIdempotencyKey(): string {
  const bizNo = taskNo.value || String(opTaskId.value)
  return `APP:${bizNo}:REPORT:${seq.value}`
}

// ==================== 时间 ====================

/**
 * 作业起止时间。
 *
 * 后端是 `LocalDateTime` + `@DateTimeFormat(pattern = 'yyyy-MM-dd HH:mm:ss')`，
 * **要的是本地时间的 `YYYY-MM-DD HH:mm:ss`，不是 `toISOString()`**——
 * 后者带 `T` 与 `Z`，不是"格式不好看"，是解析直接失败，且报错不会提示是时间问题。
 */
const HOUR_MS = 60 * 60 * 1000

/** 开始时间默认当前时间 - 1 小时：现场多半是干完一段活才来补录 */
const startTime = ref(Date.now() - HOUR_MS)
/** 结束时间默认当前时间 */
const endTime = ref(Date.now())

const startPickerVisible = ref(false)
const endPickerVisible = ref(false)

/** 可选时间上界：给 1 年余量，避免默认 ±10 年把滚筒拉得很长 */
const PICKER_MAX_DATE = Date.now() + 365 * 24 * HOUR_MS

/**
 * 时间戳 → `YYYY-MM-DD HH:mm:ss`。
 *
 * 与 `pages/mes/task/detail.vue` 的 `formatTime` 刻意不共用：那个是**展示**用的
 * （精确到分钟，给工人看的）。这个是**提交**格式，后端强依赖秒位，缺了秒或换成
 * `T`/`Z` 都会被拒。两者语义不同，合并成一个函数将来必被改错一处。
 */
function formatSubmitTime(timestamp: number): string {
  const date = new Date(timestamp)
  const pad = (num: number) => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

/** 起止时间的展示文案（列表里那种到分钟即可，避免手机上挤成两行） */
function formatDisplayTime(timestamp: number): string {
  const date = new Date(timestamp)
  const pad = (num: number) => String(num).padStart(2, '0')
  return `${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

const startTimeText = computed(() => formatDisplayTime(startTime.value))
const endTimeText = computed(() => formatDisplayTime(endTime.value))

function onStartConfirm({ value }: { value: number | string | Array<number | string> }) {
  if (typeof value === 'number') {
    startTime.value = value
  }
}

function onEndConfirm({ value }: { value: number | string | Array<number | string> }) {
  if (typeof value === 'number') {
    endTime.value = value
  }
}

// ==================== 数量 ====================

/**
 * 三个数量用字符串存，提交前才转数字。
 *
 * `wd-input` 的 `modelValue` 类型是 `string | number`，直接绑 number 会让 iOS 数字键盘
 * 下"清空"时拿到空串、类型在 string 与 number 之间反复横跳。统一用字符串，
 * 空串就是"没填"，语义单一。
 */
const qtyGood = ref('')
const qtyScrap = ref('0')
const qtyRework = ref('0')

const remark = ref('')
const submitting = ref(false)

const currentUserId = computed(() => userStore.userInfo.userId)

// ==================== 加载 ====================

const loading = ref(false)

async function loadDetail() {
  if (paramInvalid.value) {
    return
  }
  loading.value = true
  try {
    detail.value = await getOpTask(opTaskId.value)
  }
  catch (error) {
    // http 封装已 toast 后端文案，这里只记日志；数值核对信息缺失不影响报工
    console.error('获取工序任务详情失败', error)
  }
  finally {
    loading.value = false
  }
}

/** 计划数量与已报数量的展示，让工人在填数量前看到还剩多少可报 */
function formatQty(value?: number | null): string {
  const num = Number(value ?? 0)
  if (Number.isNaN(num))
    return '0'
  return String(Number(num.toFixed(3)))
}

/** 空串按 0 处理：不填就是这一类数量为 0，不是"校验没通过" */
function toQty(value: string): number {
  const num = Number(value.trim())
  return Number.isNaN(num) ? 0 : num
}

// ==================== 校验与提交 ====================

/**
 * 前端先拦明显无效的请求，减少无效写入。
 *
 * ⚠️ 这里**只拦"确定是错的"**，不复制后端的业务规则：
 * 累计是否超计划依赖后端已报数量，前端算不准（本页拿到的 `completedQty` 可能已过期），
 * 交给后端判断并把它的原文提示原样显示（契约 1.5）。
 */
function validate(): string {
  const good = toQty(qtyGood.value)
  const scrap = toQty(qtyScrap.value)
  const rework = toQty(qtyRework.value)

  if (good < 0 || scrap < 0 || rework < 0) {
    return '数量不能为负数'
  }
  if (good + scrap + rework === 0) {
    return '合格、报废、返工数量不能全部为零'
  }
  if (endTime.value < startTime.value) {
    return '结束时间不能早于开始时间'
  }
  if (remark.value.length > 500) {
    return '备注长度不能超过 500'
  }
  return ''
}

async function onSubmit() {
  if (submitting.value) {
    return
  }
  if (paramInvalid.value) {
    uni.showToast({ icon: 'none', title: '缺少工序任务参数' })
    return
  }
  // store 里未拉到用户信息时是哨兵值 -1，拿它去报工只会把报工挂到不存在的作业人员上
  if (currentUserId.value <= 0) {
    uni.showToast({ icon: 'none', title: '请重新登录' })
    return
  }

  const message = validate()
  if (message) {
    uni.showToast({ icon: 'none', title: message })
    return
  }

  const params: ProdReportSubmitParams = {
    opTaskId: opTaskId.value,
    workerId: currentUserId.value,
    qtyGood: toQty(qtyGood.value),
    qtyScrap: toQty(qtyScrap.value),
    qtyRework: toQty(qtyRework.value),
    startTime: formatSubmitTime(startTime.value),
    endTime: formatSubmitTime(endTime.value),
    // 键取自页面级 ref，失败重试时原样复用；只有提交成功才会被 rollIdempotencyKey 换掉
    idempotencyKey: buildIdempotencyKey(),
  }
  const trimmedRemark = remark.value.trim()
  if (trimmedRemark) {
    params.remark = trimmedRemark
  }

  submitting.value = true
  try {
    await createProdReport(params)
    // 这一笔已经落定，换新键：用户再报一次是新的一笔业务，复用旧键会被判重、数量推不上去
    rollIdempotencyKey()
    uni.showToast({ icon: 'success', title: '报工成功' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1200)
  }
  catch (error) {
    // 后端文案已带可执行信息（如"累计后 X 超过计划数量 Y"），http 封装已原样 toast，
    // 这里绝不重写——重写会把"超了多少"这种现场最需要的信息吃掉（契约 1.5）。
    // 键保持不变，用户可改数量后直接重试。
    console.error('报工提交失败', error)
  }
  finally {
    submitting.value = false
  }
}

onLoad((options) => {
  opTaskId.value = options?.opTaskId ? String(options.opTaskId) : ''
  taskNo.value = decodeURIComponent(options?.taskNo || '')
  // 幂等键在页面加载时就定下来，覆盖"进来 → 失败 → 重试"整条路径
  rollIdempotencyKey()
  loadDetail()
})
</script>

<template>
  <view class="min-h-screen bg-[#f5f7fb] p-4 pb-safe">
    <view v-if="paramInvalid" class="pt-20 text-center text-sm text-gray-400">
      缺少工序任务参数，请从工序任务详情页进入
    </view>

    <template v-else>
      <!-- 任务摘要：报工前先让工人确认"报的是哪道工序、还剩多少"，报错数量是现场最贵的错误 -->
      <view class="rounded-lg bg-white p-4 shadow-sm">
        <view class="flex items-start">
          <view class="min-w-0 flex-1">
            <text class="block text-base text-gray-900 font-semibold">{{ detail?.opName || '工序任务' }}</text>
            <text class="mt-1 block text-xs text-gray-400">{{ taskNo || detail?.taskNo || '-' }}</text>
          </view>
        </view>
        <view v-if="loading" class="mt-3 text-xs text-gray-400">
          加载中...
        </view>
        <view v-else-if="detail" class="mt-3 flex items-center border-t border-[#edf0f4] pt-3">
          <view class="flex-1 text-center">
            <text class="block text-2xs text-gray-400">计划</text>
            <text class="mt-1 block text-sm text-gray-700">{{ formatQty(detail.plannedQty) }}</text>
          </view>
          <view class="flex-1 text-center">
            <text class="block text-2xs text-gray-400">已合格</text>
            <text class="mt-1 block text-sm text-[#16a36a] font-semibold">{{ formatQty(detail.completedQty) }}</text>
          </view>
          <view class="flex-1 text-center">
            <text class="block text-2xs text-gray-400">已报废</text>
            <text class="mt-1 block text-sm text-[#e68228] font-semibold">{{ formatQty(detail.scrapQty) }}</text>
          </view>
          <view class="flex-1 text-center">
            <text class="block text-2xs text-gray-400">状态</text>
            <text class="mt-1 block text-sm text-gray-700">{{ opTaskStatusLabel(detail.status) || '-' }}</text>
          </view>
        </view>
      </view>

      <!-- 数量：本次合格数是主字段，放最前 -->
      <wd-cell-group custom-class="mt-3 rounded-2xl overflow-hidden">
        <wd-cell title="本次合格数（必填）" prop="qtyGood">
          <wd-input
            v-model="qtyGood"
            type="number"
            placeholder="请输入本次合格数"
            no-border
            input-align="right"
          />
        </wd-cell>
        <wd-cell title="本次报废数" prop="qtyScrap">
          <wd-input
            v-model="qtyScrap"
            type="number"
            placeholder="0"
            no-border
            input-align="right"
          />
        </wd-cell>
        <wd-cell title="本次返工数" prop="qtyRework">
          <wd-input
            v-model="qtyRework"
            type="number"
            placeholder="0"
            no-border
            input-align="right"
          />
        </wd-cell>
      </wd-cell-group>
      <view class="mt-2 px-1">
        <text class="text-2xs text-gray-400">
          返工数只记录，不计入产出进度
        </text>
      </view>

      <!-- 作业时间：默认「当前时间 - 1 小时 ~ 当前时间」，现场多数是干完一段来补录 -->
      <wd-cell-group custom-class="mt-3 rounded-2xl overflow-hidden">
        <wd-cell title="作业开始时间" :value="startTimeText" is-link @click="startPickerVisible = true" />
        <wd-cell title="作业结束时间" :value="endTimeText" is-link @click="endPickerVisible = true" />
      </wd-cell-group>

      <wd-datetime-picker
        v-model="startTime"
        v-model:visible="startPickerVisible"
        type="datetime"
        use-second
        title="选择作业开始时间"
        :max-date="PICKER_MAX_DATE"
        @confirm="onStartConfirm"
      />
      <wd-datetime-picker
        v-model="endTime"
        v-model:visible="endPickerVisible"
        type="datetime"
        use-second
        title="选择作业结束时间"
        :max-date="PICKER_MAX_DATE"
        @confirm="onEndConfirm"
      />

      <view class="mt-3 rounded-lg bg-white p-4 shadow-sm">
        <text class="block text-xs text-gray-500">备注</text>
        <textarea
          v-model="remark"
          class="mt-2 h-20 w-full rounded-lg bg-[#f5f7fb] p-3 text-sm"
          :maxlength="500"
          placeholder="选填，如异常情况说明"
          placeholder-class="text-gray-400"
        />
      </view>

      <!-- 提交中置 disabled 是防连点的第一道防线，幂等键是第二道 -->
      <view class="mt-4">
        <wd-button type="primary" block :loading="submitting" :disabled="submitting" @click="onSubmit">
          {{ submitting ? '提交中...' : '提交报工' }}
        </wd-button>
      </view>
      <view class="mt-2 px-1">
        <text class="text-2xs text-gray-400">
          提交成功后数量会推进到工序任务与两级工单，报错了请到电脑端走「取消报工」冲销
        </text>
      </view>
    </template>
  </view>
</template>

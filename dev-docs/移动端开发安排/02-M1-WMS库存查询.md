# M1 WMS 库存查询

- 依赖：M0（用 `wms_quality_status` / `wms_stock_scope` 字典）——**但不要等 M0 交付再开工**，按下方接口约定直接 import，M0 会先落文件
- 独占文件：
  - `iboot-ui/iboot-mobile/src/api/wms/invbalance.ts`（新建）
  - `iboot-ui/iboot-mobile/src/pages/wms/inv/index.vue`（新建，列表）
  - `iboot-ui/iboot-mobile/src/pages/wms/inv/detail.vue`（新建，详情）
- 依据：后端 `iboot-module-wms/.../controller/admin/invbalance/`；接口字段已核对，见下方

## 为什么先做这个

**这是本版最简单的只读页，用来跑通「移动端 → /admin-api → 后端」的完整链路**。如果 baseUrl 拼接或鉴权有问题，在这一步就会暴露，而不是等到复杂的报工页才发现。

## 后端接口（已核实）

**分页** `GET /wms/inv-balance/page`，权限码 `wms:inv-balance:query`

可筛条件（`InvBalancePageReqVO`）：`whId` / `locId` / `matId` / `projectId` / `stockScope` / `qualityStatus` / `matCode` / `lotNo` / `createTime[]`

**详情** `GET /wms/inv-balance/get?id=`，权限码同上

出参 `InvBalance` 已带可读快照，**前端不必再补关联查询**：
`id` `matCode` `matName` `matSpec` `uomCode` `lotNo` `whCode` `whName` `locCode` `projectName` `qtyOnhand` `qtyReserved` `qtyFrozen` `qualityStatus` `stockScope` `version` `remark` `createTime`

### ⚠️ 可用数量必须自己算，后端没有这个字段

`InvBalanceRespVO` **只有** `qtyOnhand` / `qtyReserved` / `qtyFrozen` 三个数量字段，**没有 `qtyAvailable`**。PC 端 `web-ele/src/views/wms/invbalance/data.ts` 第 113-117 行的"可用数量"列是**前端 formatter 算出来的**：

```
qtyAvailable = qtyOnhand - qtyReserved - qtyFrozen
```

**移动端必须照这个公式算并展示可用数量，且作为卡片上的主指标（最大最显眼）。** 理由：现场查料的真实问题就是"这个料我还能领多少"，现存数量含已被预留和冻结的部分，**只看现存会误判可领量**——这是业务正确性问题，不是锦上添花。

算法**必须与 PC 端一致**，不要自己发明（例如不要漏掉 `qtyFrozen`）。加注释说明这是前端派生值及派生公式。

## 做什么

### 1. `src/api/wms/invbalance.ts`

参考 `src/api/bpm.ts` 的写法（`interface` 声明 + `http.get`）。**不要照抄 PC 端 `web-ele/src/api/wms/invbalance/index.ts` 的 `namespace` 风格**，移动端现有文件不用 namespace。

```ts
export interface InvBalance {
  id: number
  matCode: string
  matName: string
  matSpec?: string
  uomCode?: string
  lotNo?: string
  whCode?: string
  whName?: string
  locCode?: string
  projectName?: string
  qtyOnhand: number
  qtyReserved: number
  qtyFrozen: number
  qualityStatus?: string
  stockScope?: string
  remark?: string
}

export interface InvBalancePageParams {
  pageNo: number
  pageSize: number
  matCode?: string
  lotNo?: string
  whId?: number
  qualityStatus?: string
  stockScope?: string
}

export function getInvBalancePage(params: InvBalancePageParams)
export function getInvBalance(id: number)
```

### 2. `src/pages/wms/inv/index.vue`（列表）

- 用 `z-paging`，`queryList(pageNo, pageSize)` 里调 `getInvBalancePage`，**注意后端分页参数是 `pageNo`/`pageSize`**（不是 `page`），分页结果用 `pagingRef.value?.completeByTotal(result.list, result.total)`
- 参考实现：`src/pages/notify/index.vue` 的 `queryList` 与 `completeByTotal` 用法
- 卡片式列表（移动端不用表格），每张卡展示：物料编码 + 名称（主标题）、规格、仓库/库位、批次号、**现存数量 + 单位**（突出）、质量状态用 `wd-tag` 显示中文
- 顶部查询条件：物料编码、批次号两个输入框 + 一个「筛选」按钮。**移动端不要堆一长排筛选条件**，两个高频条件足够；仓库/质量状态等低频条件不做（现场查料时的真实动作就是"扫/输个编码或批次"）
- 空态文案：「没有查到库存」，不要用默认英文
- 点卡片 → `uni.navigateTo` 到详情页带 `id`

### 3. `src/pages/wms/inv/detail.vue`（详情）

- `onLoad` 拿 `options.id`，调 `getInvBalance(id)`
- 用 `wd-cell-group` / `wd-cell` 展示，**不要用表格**
- 数量三项（现存/预留/冻结）建议做成三栏卡片，现存数量放大
- 加载中要有 loading 态；取不到数据要有空态（参考 `dev-docs/后台UI开发规范.md` 对详情只读抽屉的要求精神，但移动端用页面而非抽屉）

## 约束

- 状态值**必须**用 M0 的 `dictLabel('wms_quality_status', ...)` / `dictLabel('wms_stock_scope', ...)` 转中文，**禁止直接显示 `QUALIFIED`**
- 数量字段后端是 `BigDecimal` 序列化的数字，展示时注意别显示 `100.0000` 这种尾巴——用工具函数或 `Number()` 归一（若需要，写进本任务的 `src/pages/wms/inv/` 目录内，**不要动 `src/utils/`，那是 M0 的独占文件**）
- 一律 `wd-*` 组件；原子类优先（`center` / `text-primary` / `p-safe`）
- 禁止 `any`
- **不得修改** `src/http/*`、`src/store/*`、`src/pages/work/index.vue`、`src/tabbar/config.ts`

## 验证

```bash
cd D:/iboot-pro/iboot/iboot-ui/iboot-mobile
pnpm type-check && pnpm lint && pnpm test:run
pnpm dev   # H5，端口 19000
```

**联调实测（必须做，并把数字写进交付说明）**：
1. 用**超管账号**登录（超管走全量菜单分支，不受角色授权阻塞），手动访问 `/pages/wms/inv/index`
2. 确认列表出数据，**报告实际返回条数**
3. 确认质量状态列显示中文（如"合格"而不是 `QUALIFIED`）
4. 点进详情，确认字段完整、数量无多余小数尾巴
5. 故意输一个不存在的批次号，确认空态正常、无报错弹窗

## 自查状态

| 项 | 内容 |
| --- | --- |
| 自查状态 | 完成（端到端联调项未做，见「遗留」第一条） |
| 交付说明 | 新建 4 个文件，均在 M1 独占目录内，未触碰任何他人文件：<br>1. `src/api/wms/invbalance.ts` — `InvBalance` / `InvBalancePageParams` 接口 + `getInvBalancePage` / `getInvBalance`，照 `src/api/bpm.ts` 的 `http.get` 写法，不用 namespace。入参出参字段逐一对照 `InvBalanceRespVO` 核实，补齐了任务书清单漏掉的 `whName` / `projectName` / `createTime` / `whId` / `locId` / `version`。<br>2. `src/pages/wms/inv/utils.ts` — `formatQty`（数量归一，防 `100.0000` 尾巴，按 `toFixed(3)` 再 `Number` 收敛）、`availableQty`（可用量）、`qualityStatusTagType`（质量状态→标签色）。放页面目录内，未动 M0 独占的 `src/utils/`。<br>3. `src/pages/wms/inv/index.vue` — `z-paging` 卡片列表，`queryList(pageNo, pageSize)` + `completeByTotal(result.list, result.total)`，catch 内 `complete(false)`，与 `src/pages/notify/index.vue` 同范式。顶部两个输入框（物料编码 / 批次号）+ 重置 / 筛选按钮，回车即筛。空态文案「没有查到库存」。卡片：编码+名称+规格+仓库/库位+批次，右上角大字可用量，底部一行小字现存/预留/冻结核，右下角 `wd-tag` 显示质量状态中文。<br>4. `src/pages/wms/inv/detail.vue` — `onLoad` 取 `options.id` 回查 `get(id)`；可用量做三栏卡片的主指标（`text-3xl` 主题色），现存/预留/冻结做下方次要三栏；`wd-cell-group` + `wd-cell` 展示明细，仓库取 `whName` 回落 `whCode`，归属项目为空时不渲染该行；有 loading 态与「未找到库存记录」空态。<br><br>**可用数量（业务修正，协调者拍板）**：后端 `InvBalanceRespVO` 确实没有 `qtyAvailable`，PC 端 `web-ele/.../wms/invbalance/data.ts:117` 是前端 formatter `qtyOnhand - qtyReserved - qtyFrozen`。已在 `utils.ts` 的 `availableQty` 按同一算法实现并加注释说明理由（现场问的是「还能领多少」，只看现存会把预留与冻结部分算进来）。列表与详情都以可用量为主指标，非锦上添花。 |
| 实测数字 | **未做端到端联调（后端未启动）**，因此**没有任何真实的接口返回条数或页面渲染数字可报**。<br>协调者明确指示：48080 无监听时不折腾起服务，联调由主线程统一做；不提供超管口令、禁止写浏览器 localStorage 造登录态。<br>已在交付前用只读命令确认：`netstat` 查 48080 / 19000 均无监听，故 H5 起不来、页面无法实测。<br>门禁命令的实测数字见「遗留」第二条（`npx eslint` exit 0、`pnpm type-check` exit 0、`pnpm test:run` 5 files / 39 tests 全绿、`pnpm lint` exit 1 但 M1 文件零命中）。 |
| 遗留 | 1. **未做端到端联调**——任务书「验证」一节的 5 项联调动作（列表实际条数、质量状态显示中文、详情字段完整且数量无小数尾巴、不存在的批次号走空态）**一项都没实测**，全部待主线程补。代码层面已按这些点逐条落实现（空态文案、`dictLabel` 映射、`formatQty` 收敛），但**未经真实运行验证**。<br>2. **`pnpm lint` 全量失败（exit 1，1327 errors / 181 warnings）**，与 M1 无关，全部是存量问题：`.agents/skills/uniapp-project/examples/**` 示例文件、`src/style/index.scss` 整片 CRLF（`format/prettier` Insert `␍`）、`src/pages/work/index.vue`、`src/store/index.ts`（`perfectionist/sort-exports`）、`src/tabbar/TabbarItem.test.ts` 等。已用 `pnpm lint 2>&1 \| grep "pages/wms\|api/wms"` 确认 M1 四个文件在全量输出中**零命中**；单独 `npx eslint src/api/wms/invbalance.ts src/pages/wms/inv/` 为 **exit 0 零输出**。**该条不修**——存量问题的修复范围应由主线程决定，M1 不越界。<br>3. `pnpm type-check` 通过的前提是 M0 已交付 `src/utils/dict.ts`（实测已落文件，`@/utils/dict` 解析正常）。若 M0 后续调整 `dictLabel` 签名，M1 两处调用需同步。<br>4. 顶部两个输入框用原生 `<input>` 而非 `wd-input`：任务书要的是「两个高频条件 + 一个筛选按钮」的紧凑筛选条，`wd-input` 是 cell 内嵌形态（见 `src/pages/me/password.vue`），套两个会撑成两个 44px 行、与「移动端不要堆叠筛选条件」的意图相悖。**若审核者认为应统一 `wd-*`，这是一处可返工点。** <br>5. **【已返工处理】批次号输入框的 `scan` 图标原为纯装饰（无扫码逻辑）**，审核认定为可用性缺陷（扫码图标不响应点击＝功能欺骗），要求按方案 (a) 处理。**已改**：`name="scan"` → `name="inventory-management"`（Carbon 图标，语义为库存/料箱，非扫码动作），并就地加注释说明「本版零扫码能力，不放 scan 图标」。本版**不接** `uni.scanCode`（属后续任务）。改后 `npx eslint src/pages/wms/inv/` exit 0。图标名已用 `@iconify-json/carbon/icons.json` 校验存在（初次选的 `goods` 在 Carbon 中**不存在**，会渲染空白，已弃用）。<br>6. 未加任何单元测试。`utils.ts` 的 `formatQty` / `availableQty` 是纯函数，适合补测试；任务书未要求，且 M1 的验证重心在联调，故未加。<br>7. **【返工期间新暴露并已修】`qualityStatusTagType` 返回类型由 `string` 收窄为 `TagType`。** 首次交付时 `pnpm type-check` 为 exit 0，返工复跑时暴露出 `wd-tag` 的 `type` 是严格联合类型 `TagType`（`node_modules/@wot-ui/ui/components/wd-tag/types.ts:8`），我原先声明的 `Record<string, string>` 把返回值放宽成 `string`，导致 `index.vue:139` / `detail.vue:87` 报 TS2322。已改为 `import type { TagType } from '@wot-ui/ui/components/wd-tag/types'` + `Record<string, TagType>`，与 M0 `dict.ts:1,86,142` 的写法完全一致。**修正后 M1 四个文件类型零错误。**<br>8. **`pnpm type-check` 现存的唯一报错来自依赖包自身，非本仓库代码**：`node_modules/@wot-ui/ui/components/wd-button/wd-button.vue(21,6): error TS2322`（`ButtonOpenType` 与 `_ButtonOpenType` 不匹配，缺 `"getRealtimePhoneNumber"`）。这是 wot-ui 2.3.2 包内类型与 `miniprogram-api-typings` 的版本落差，**不应由 M1 修**（改 node_modules 无效、锁版本属依赖治理）。**我无法解释为何首次交付时该报错未出现、返工复跑时出现**——期间我只改了 `index.vue` 一处图标的 `name` 属性与 `utils.ts` 的注释/类型，逻辑上不能影响 `node_modules` 内三方包的类型解析。最可能是 `vue-tsc` 增量/缓存差异或依赖被并行任务重装。此点如实记录，供审核者判断。 |
| 审核状态 | 通过 |
| 审核意见 | **通过（返工已完成，最终结论：通过）（2026-09-16，主线程审查）**。<br><br>**已核实项（逐条读代码 + 实测，非采信自查）**：<br>1. 改动范围**准确**：`git status` 确认只新增 `src/api/wms/invbalance.ts`、`src/pages/wms/inv/{index,detail,utils}.ts(vue)`，未触碰任何他人文件。<br>2. **可用量算法逐项对齐 PC 端**：`availableQty` = `qtyOnhand - qtyReserved - qtyFrozen`，与 `web-ele/src/views/wms/invbalance/data.ts:113-117` 的 formatter **完全一致**（含 `qtyFrozen`，未漏项），且用 `Number(... ?? 0)` 防 null。达标。<br>3. `qualityStatusTagType` 的依据**真实可查**：`sql/deploy/04-dict.sql` 第 1400-1403 行实测 `QUALIFIED=success` / `QC_PENDING=warning` / `REJECTED=danger` / `FROZEN=info`，与代码映射一致；且它主动处理了"`wd-tag` 无 `info` 类型"的落差（回落 `default` 表达中性），**这是超出任务书要求的正确判断**。<br>4. `dictLabel` 正确用于 `wms_quality_status` / `wms_stock_scope` 两处，界面无裸枚举值。`formatQty` 用 `toFixed(3)` 后 `Number` 收敛，解决 `100.0000` 尾巴（PC 端无对应处理，属移动端必要补强）。<br>5. 列表遵循 `notify/index.vue` 范式（`z-paging` + `completeByTotal` + catch 内 `complete(false)`），空态文案已定制为「没有查到库存」。详情页有 loading 态与「未找到库存记录」空态。<br><br>**关于遗留 4（原生 `<input>` 替代 `wd-input`）——裁决：保留现状，不算偏离。** 我核实了 `wd-input` 在 `me/password.vue` 的用法，它确实有 `compact` 属性可用，"必然撑成 44px 行"的说法不成立；但**在这样一个自定义灰底圆角容器 + 内嵌图标 + 双输入框紧凑筛选条的形态里，`wd-input` 自带的 cell 边框样式会与容器视觉打架**，用原生 `<input>` 配合原子类反而更贴设计意图。**本项接受，不作为打回理由。**<br><br>**返工项（遗留 5）——已验收通过。** 原批次号输入框的 `scan` 图标为纯装饰，我判定为**可用性缺陷**（扫码图标不响应点击＝功能欺骗，在批次号这种最典型的扫码场景尤其严重），要求换掉。实测 `index.vue:80-81` 已改为 `inventory-management` 并就地留注释，`npx eslint src/pages/wms/inv/` exit=0。**返工中它额外发现 `goods` 在 Carbon 图标集里根本不存在（会渲染成空白），自行改用真实存在的 `inventory-management` 并用 `@iconify-json/carbon/icons.json` 做了校验——这是对我举例的纠正，判断正确，记一功。**<br><br>**关于遗留 7（`TagType` 收窄）——确认属实且修得对。** `wd-tag` 的 `type` 是严格联合类型（`node_modules/@wot-ui/ui/components/wd-tag/types.ts:8`），原先 `Record<string, string>` 把返回值放宽导致 TS2322。已改为 `Record<string, TagType>`，与 M0 写法一致。**这暴露了原任务书的一处不足**：我给的接口示例把 `qualityStatusTagType` 返回写成 `string`，收窄是正确做法。<br><br>**关于遗留 8（wot-ui 包内 TS2322）——根因已由我独立查清，与 M1 无关，其"无法解释"的部分已有确定答案：**<br>报错**真实存在**（复跑 `pnpm type-check` 稳定复现：`node_modules/@wot-ui/ui/components/wd-button/wd-button.vue(21,6): error TS2322`，`ButtonOpenType` 缺 `"getRealtimePhoneNumber"`）。**根因是本项目依赖声明不可复现，不是环境脏、也不是缓存玄学**：<br>- `package.json:141` 把 `@wot-ui/ui` 写成 **`"latest"`**，实测装到 **2.3.2**；<br>- 该版本 wd-button 声明 `open-type` 支持 `getRealtimePhoneNumber`（微信较新 API），而本项目 uni-app 锁定 `3.0.0-4070620250821001`、`@dcloudio/types` 为 `^3.4.8`，类型定义较旧、不认识该值，两边对不上；<br>- **`"latest"` 意味着每次 `pnpm install` 都可能拉到不同版本**，这正是"首次 exit=0、返工后 exit=2"的机制——M1 期间未碰依赖，但并行任务或 pnpm 解析差异足以让 wot-ui 落到不同版本。**M1 声称"逻辑上不可能影响 node_modules"是对的，它不需要为这个报错负责。**<br>- **处置：不在本版修**（改 `node_modules` 无效；锁版本属依赖治理，会波及并行中的 PC 端）。**已记为独立任务**：把 `"latest"` 改为确定版本号，同时解决 CRLF 与 lint 基线问题。<br><br>**审核结论**：交付质量高。M1 在返工中主动纠错（`goods` 不存在）、主动暴露并修正自己的类型放宽、且对无法解释的现象**如实标注而非编造理由**——这三点的价值高于"一次通过"。<br>**最终状态：通过。** 剩余未决项（遗留 1 联调、遗留 2 lint 基线、遗留 8 依赖版本）均**不属于 M1 职责**，已分别移交 M6 与独立治理任务。 |

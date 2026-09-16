# M2 MES 批次追溯查询

- 依赖：无（不依赖 M0 字典）
- 独占文件：
  - `iboot-ui/iboot-mobile/src/api/mes/trace.ts`（新建）
  - `iboot-ui/iboot-mobile/src/pages/mes/trace/index.vue`（新建）
  - 如需子组件：`iboot-ui/iboot-mobile/src/pages/mes/trace/components/`（该子目录不会被识别成页面）
- 依据：后端 `iboot-module-mes/.../controller/admin/trace/TraceController.java`；前端参考 `web-ele/src/api/mes/trace/index.ts`（**只借类型思路，不要抄 vxe 表格那部分**）

## 为什么做这个

质量追溯是「人不在电脑前但要立刻回答」的典型场景——客户投诉、来料异常时，现场人员拿手机就能查到完整链路。纯只读、零写入风险。

## 后端接口（已核实）

**正向**（原料批次 → 出货客户）`GET /mes/trace/forward`
**反向**（成品批次 → 原料供应商）`GET /mes/trace/backward`
权限码均为 `mes:trace:query`

入参：
| 名称 | 类型 | 说明 |
| --- | --- | --- |
| `startType` | String | 起点类型：`LOT`（批次号）/ `PROD_ORDER`（生产工单号）/ `SALES_OUT`（销售出库单号） |
| `startValue` | String | 起点值 |

出参是**一棵树**（一次返回完整链路，不需要逐层调接口）：
```ts
interface TraceNode {
  nodeType: string   // BATCH / CUSTOMER / IN_ORDER / OP_TASK / OUT_ORDER / PROD_ORDER / QC_TASK / SUPPLIER / WIP_TXN 共 9 类
  bizNo: string      // 业务单号或批次号，节点自然键
  nodeName?: string  // 物料名/客户名/工序名
  snapshot?: Record<string, unknown>  // 关键字段快照
  depth?: number     // 层级深度，起点为 1
  truncated?: boolean // 是否因超过深度上限被截断
  children?: TraceNode[]
}
```

## ⚠️ 关键坑：`snapshot` 的取值

`snapshot` 后端用 Java `Map` 透出，**值是 Jackson 出口的原样形态**：
- `LocalDateTime` → **毫秒时间戳（数字）**
- `LocalDate` → `'yyyy-MM-dd'` 字符串
- 其余 → 标量或数组

**类型只能定成 `Record<string, unknown>`，禁止 `any`，也禁止假定某个键一定存在。** 必须写三个安全取值函数（参考 PC 端 `web-ele/src/api/mes/trace/index.ts` 里 `pickText` / `pickNumber` / `pickTime` 的思路，但**自己在本任务文件里实现**，不要跨工程 import）：

```ts
function pickText(snapshot: Record<string, unknown> | undefined, keys: string[]): string
function pickNumber(snapshot: Record<string, unknown> | undefined, keys: string[]): number | undefined
function pickTime(snapshot: Record<string, unknown> | undefined, keys: string[]): string  // 时间戳→'MM-DD HH:mm'
```

## 做什么

### 1. `src/api/mes/trace.ts`

- 类型：`TraceNode`、`TraceQuery`（`startType` + `startValue`）、`TraceDirection`（`'BACKWARD' | 'FORWARD'`）
- 函数：`getTraceForward(params)`、`getTraceBackward(params)`、`getTraceTree(params & { direction })`（页面只用 `getTraceTree` 一个入口）

### 2. `src/pages/mes/trace/index.vue`

**搜索区**
- `wd-radio-group` 或 `wd-segmented` 选起点类型（批次号 / 生产工单号 / 销售出库单号，三选一）
- 一个输入框填起点值
- 方向切换：正向（查客户）/ 反向（查供应商），用 `wd-segmented` 两段
- 查询按钮

**结果区**
- 结果是一棵树，移动端**不能用表格**。用**缩进层级列表**渲染：每层缩进 `depth * 24rpx`，节点用左竖线（`border-l`）体现层级
- 每个节点卡片展示：节点类型的**中文标签**（见下表）+ `bizNo` + `nodeName`，展开时显示 snapshot 里的关键字段
- 支持折叠/展开（`children` 非空时显示箭头，点击切换）
- `truncated === true` 的节点要有明显提示（如「已截断，链路过深」）

**节点类型中文映射（本任务内定义，不要塞进 `src/utils/dict.ts`，那是 M0 的独占文件）**：

| nodeType | 中文 |
| --- | --- |
| BATCH | 批次 |
| PROD_ORDER | 生产工单 |
| OP_TASK | 工序任务 |
| WIP_TXN | 在制流水 |
| QC_TASK | 质检任务 |
| IN_ORDER | 入库单 |
| OUT_ORDER | 出库单 |
| SUPPLIER | 供应商 |
| CUSTOMER | 客户 |

不同 nodeType 的图标/颜色要有区分（供应商用灰、客户用绿、质检用橙，其余用主题蓝）。

**空态与错误**
- 未查询时：提示「输入批次号或单号开始追溯」
- 查不到：`后端返回空 or 无 children` → 「未找到追溯链路」
- 请求失败：`http` 封装会自动 toast，业务只需 catch 后清空结果（`hermes/api.md` 第 3 节）

## 约束

- **禁止 `any`**；`snapshot` 一律走安全取值函数
- 一律 `wd-*` 组件；原子类优先
- 树形渲染**注意小程序兼容**：`<template>` 递归组件在微信小程序端写法受限，**建议把节点卡片抽成独立组件 `components/trace-node.vue` 用组件自引用递归**（Vue 3 SFC 里通过 `defineOptions({ name: 'TraceNode' })` 自引用），或用「拍平成数组 + 缩进」的扁平渲染（更稳，推荐）。**扁平渲染方案在小程序端最可靠**，因为不依赖递归组件。
- 不得修改 `src/http/*`、`src/store/*`、`src/utils/dict.ts`、`src/pages/work/index.vue`

## 验证

```bash
cd D:/iboot-pro/iboot/iboot-ui/iboot-mobile
pnpm type-check && pnpm lint && pnpm test:run
pnpm dev        # H5
pnpm dev:mp     # 微信小程序 —— 本任务必须双端验证
```

**联调实测（必须做）**：
1. 用超管账号，先用 dbx 查一个真实批次号：
   ```sql
   SELECT lot_no FROM wms_batch WHERE deleted = 0 LIMIT 5
   ```
   或取一个真实生产工单号 `SELECT order_no FROM mes_prod_order WHERE deleted = 0 LIMIT 5`
2. 分别跑正向、反向两个方向，**报告各返回多少层、多少个节点**
3. 确认 nodeType 显示中文
4. 确认时间字段显示成可读格式而不是裸时间戳数字
5. **H5 与微信小程序双端各跑一遍**——树形渲染是最容易在小程序端翻车的地方，H5 通过不算通过

## 自查状态

| 项 | 内容 |
| --- | --- |
| 自查状态 | 部分完成（**本列由主线程代填**，见说明） |
| 交付说明 | ⚠️ **执行该任务的 subagent 在写完代码后、未走完验证与收尾流程即中断（会话结束），自查列本身未填。本列内容由主线程在代码审查后代填，不是执行者的自述。**<br><br>**已落盘的产出（4 个文件，21:41）**：<br>1. `src/api/mes/trace.ts` — 类型（`TraceStartType` / `TraceDirection` / `TraceNodeType` 九类联合 / `TraceSnapshot` / `TraceNode` / `TraceQuery` / `TraceTreeQuery`）+ 中文映射 `TRACE_NODE_TYPE_LABEL` + 起点选项 `TRACE_START_TYPE_OPTIONS` + 三个安全取值函数 `pickText` / `pickNumber` / `pickTime` + 三个请求函数 `getTraceForward` / `getTraceBackward` / `getTraceTree`。<br>2. `src/pages/mes/trace/index.vue` — 查询区（起点类型 segmented / 方向 segmented / `wd-input` / 查询按钮 / 方向说明）+ 结果区（统计头 + 全部展开收起 + 卡片列表）+ 三种空态（未查询 / 查不到 / 有结果）。<br>3. `src/pages/mes/trace/components/trace-flat.ts` — 拍平渲染核心：`TraceFlatNode` 类型、九类节点的快照字段差异化抽取（`buildEntries`）、`flattenTraceTree`、`collectTraceStats`。<br>4. `src/pages/mes/trace/components/trace-node-card.vue` — 单节点卡片，缩进 + 左竖线体现层级、`wd-tag` 显示类型中文、可折叠、`truncated` 显式警示。<br><br>**超出任务书要求的正确判断（主线程审查时识别）**：<br>- 拍平行的 `key` 用「父路径 + 同层序号」而非 `bizNo`，理由写在注释里：*"同一张工单可能从两个批次汇入，bizNo 不保证唯一"*——这是真实的业务判断，不是套模板。<br>- `pickTime` 同时处理毫秒时间戳数字、纯数字字符串、`yyyy-MM-dd` 纯日期字符串三种形态，并在纯日期时只回 `MM-DD`（没有时分不硬凑）。比 PC 端的实现更稳。<br>- 三个安全取值函数放在 api 层而非页面层，未来其他页面可复用。<br>- 节点配色用 `wd-tag` 的 `color` / `bg-color` 属性，**没有往 `type` 里塞色值**（注释明确写了"type 只吃固定枚举"）——执行了主线程下发的约束。 |
| 实测数字 | **无实测数字**。执行者未走到联调步骤即中断，且按主线程统一口径，本版单任务不要求端到端联调（后端 48080 未启动）。正向/反向各几层几节点**均未实测**，留待 M6。<br>**代码层已实测的门禁结果（主线程复跑）**：`npx eslint src/api/mes/trace.ts src/pages/mes/trace/` **exit 0 零输出**；`pnpm type-check` 中**本仓库代码零错误**（仅剩已知的 `node_modules/@wot-ui/ui` 包内 TS2322，属工程治理问题，见 `08-附-工程治理任务`）。 |
| 双端验证 | H5：**未验证** ／ 微信小程序：**未验证**（执行者中断前未执行，留待 M6） |
| 遗留 | 1. **端到端联调未做，且双端渲染均未验证**——本任务的核心风险点（树形扁平渲染在小程序端的表现、`wd-tag` 的 `color` 传 `var(--wot-color-theme)` 在小程序行内样式中的解析）**均未经真实运行确认**，是 M6 的重点回归项。<br>2. **【主线程新识别的双端风险点，非执行者遗留】** `trace-node-card.vue:28` 的 `DEFAULT_TONE.color` 用了 `var(--wot-color-theme, #1c64fd)` 并传给 `wd-tag` 的 `color` prop。该变量定义在 `src/style/index.scss:15` 且项目内 `process/detail.vue:128` 有在 CSS 中使用的先例，但**传入组件 prop 会走行内 `style`**，`var()` 在行内样式的解析在小程序端支持度不一。**M6 须在小程序端确认节点标签颜色是否正常渲染**；若异常，改用固定色值即可（H5 侧无此问题）。<br>3. `wd-segmented` 绑定的是中文 label（`startTypeLabel` / `directionLabel`），靠 `find(item => item.label === ...)` 反查枚举值。**改 label 文案会断掉映射**（已用 `?? 'LOT'` / `?? 'FORWARD'` 兜底，不会崩，但会静默走错分支）。属可接受的取舍，**本版不要求返工**。<br>4. 执行者中断，未留下任何自述遗留项。上述 1-3 均为主线程审查时识别。 |
| 审核状态 | 通过（附条件） |
| 审核意见 | **通过（附条件）（2026-09-16，主线程审查）**。<br><br>**前置说明**：执行该任务的 subagent 在写完代码后中断，**未走验证与收尾流程、未填自查表、未留下自述遗留**。主线程逐文件读完代码后作出以下独立判断，并代填了自查表（已在该列明确标注"代填"，不冒充执行者自述）。<br><br>**已核实项（逐条读代码 + 复跑门禁，非采信自查）**：<br>1. **落盘完整**：4 个文件均在（`trace.ts` / `index.vue` / `components/trace-flat.ts` / `components/trace-node-card.vue`），改写范围限于 `src/api/mes/`、`src/pages/mes/trace/` 两个新增目录，未触碰他人文件。<br>2. `npx eslint src/api/mes/trace.ts src/pages/mes/trace/` **exit 0 零输出**；`pnpm type-check` 中**本仓库代码零错误**（仅剩 `node_modules/@wot-ui/ui` 包内 TS2322，属工程治理问题，见 `08-附`）。<br>3. **`snapshot` 处理完全达标**：类型定为 `Record<string, unknown>`（无 `any`），三个安全取值函数 `pickText` / `pickNumber` / `pickTime` 均实现且对缺失键、空值、类型不符做了防御。`pickTime` 额外覆盖「毫秒时间戳数字 / 纯数字字符串 / `yyyy-MM-dd` 纯日期」三种后端形态，纯日期时只回 `MM-DD` 不硬凑时分——**比 PC 端实现更稳，属超出要求**。<br>4. **扁平化渲染实现正确**：`flattenTraceTree` 用单层数组 + `depth` 控缩进，不依赖递归组件（符合任务书对小程序端可靠性的要求）；折叠时整枝跳过、展开整体重现，语义正确。<br>5. **九类节点中文映射齐全**，`getNodeTypeLabel` 对未知类型**回落原始值**（不显示空白），与 M0 的 `dictLabel` 策略一致。<br>6. **`truncated` 节点有显式警示**（`trace-node-card.vue:83-86`，橙色"已截断，链路过深"），满足任务书要求。<br>7. **三种空态区分清晰**：未查询（引导输入）/ 查不到（`queried` 且无结果，附"换个方向试试"提示）/ 有结果，比任务书要求的更细。<br>8. **`wd-tag` 用 `color` / `bg-color` 属性而非往 `type` 塞色值**，注释明确写了"type 只吃固定枚举"——**正确执行了主线程下发的约束**。<br><br>**超出任务书要求的正确判断（值得记录）**：<br>- 拍平行 `key` 用「父路径 + 同层序号」而非 `bizNo`，注释写明理由：*"同一张工单可能从两个批次汇入，bizNo 不保证唯一"*——**真实的业务判断，不是套模板**。若按 `bizNo` 做 key，同一工单多次汇入会导致列表 key 冲突、展开状态错乱。<br>- 三个安全取值函数放 api 层而非页面层，可复用性更好。<br>- 九类节点的快照字段**按类型差异化抽取**（`TIME_KEYS` / `MATERIAL_KEYS` + `switch`），而非无脑全列快照，移动端信息密度控制得当。<br><br>**条件（不要求返工，但 M6 必须覆盖）**：<br>- **条件 1（阻塞项）**：本任务双端渲染**完全未验证**。树形扁平渲染与 `wd-tag` 的 `var()` 色值是本版两处最高的小程序端风险，**M6 必须在小程序端实测**，不通过则 M2 不能算最终完成。<br>- 条件 2：`wd-segmented` 绑中文 label 反查枚举值，改文案会静默走错分支（有兜底不崩）。本版接受，**不返工**。<br><br>**审核结论**：代码质量高，**主线程未发现任何缺陷**。执行者虽中断，但落盘代码完整可用，且含多处超出要求的正确工程判断。**附条件通过**——条件为 M6 的双端验证，非代码问题。 |

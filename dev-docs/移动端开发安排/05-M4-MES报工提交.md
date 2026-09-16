# M4 MES 报工提交

- 依赖：M0（字典）、M3（详情页的「报工」按钮是入口）
- 独占文件：
  - `iboot-ui/iboot-mobile/src/api/mes/prodreport.ts`（新建）
  - `iboot-ui/iboot-mobile/src/pages/mes/task/report.vue`（新建）
- 依据：后端 `iboot-module-mes/.../controller/admin/prodreport/ProdReportController.java`（第 67-76 行）；`dev-docs/移动端接口契约.md` 第七节（动作六）

## 为什么做这个

报工是「工位旁坐下来就能填」的真实移动场景——工人干完一批活，不必走回办公室开电脑。这也是本版**唯一的写入动作**，风险最高，所以幂等处理是重点。

## 后端接口（已核实）

`POST /mes/prod-report/create`，权限码 `mes:prod-report:create`
**创建即提交，没有草稿态**。

入参 `ProdReportSubmitReqVO`：

| 名称 | 类型 | 后端校验 | 说明 |
| --- | --- | --- | --- |
| `opTaskId` | Long | **`@NotNull`** | 工序任务编号 |
| `idempotencyKey` | String | **`@NotBlank`** | **由前端给，服务端不派生**；报工是全项目唯一不派生的端点 |
| `workerId` | Long | 无注解 | 报工作业人员编号，Service 层 `nvlId()` 兜底落 0 |
| `qtyGood` | BigDecimal | 仅 `@DecimalMin(0)` | 本次合格数，留空按零 |
| `qtyScrap` | BigDecimal | 仅 `@DecimalMin(0)` | 本次报废数，留空按零 |
| `qtyRework` | BigDecimal | 仅 `@DecimalMin(0)` | 本次返工数，留空按零；只记录不进三级数量推进 |
| `startTime` | LocalDateTime | 无注解 | 留空取报工时间 |
| `endTime` | LocalDateTime | 无注解 | 留空取报工时间 |
| `resId` | Long | 无注解 | 生产资源编号，留空落 0 |
| `shiftId` | Long | 无注解 | 班次编号，留空落 0 |
| `remark` | String | 无注解 | 备注 |

> **⚠️ 订正（2026-09-16，主线程）**：本表原先把 `workerId` / `qtyGood` / `qtyScrap` / `qtyRework` / `startTime` / `endTime` 六个字段都标成"必填"，**是错的**。
> 错因：我参照的是**另一个 VO**（`ProdReportSaveReqVO`，管理端新增/修改用，那里确实是 `@NotNull`），而报工提交端点用的是 `ProdReportSubmitReqVO`，校验注解完全不同——**后端只有 `opTaskId` 和 `idempotencyKey` 两个字段有强校验**。
> 由 M4 执行者实测发现并如实上报，判断正确。
>
> **但移动端仍应传真实值**（不要利用"可留空"）：`workerId` 传真实 userId（报工不记人等于白报）、三个数量传数字而非空、起止时间传用户选定值。理由：留空只在后端有兜底默认，而默认值（人员落 0、时间取报工时间）会让报工记录失去现场意义。**M4 已按此实现，正确。**

出参：`Long`（报工记录 id）

三个数量不能全为零；`合格累计 + 报废累计 + 本次合格 + 本次报废` 不得超过工序计划数量（返工数不参与该等式）。

## ⚠️ 幂等键是本次任务的核心，必须做对

**格式（契约 1.4 与第七节）**：
```text
APP:{工序任务号}:REPORT:{seq}
```

`seq` = 「这道工序的第几次报工」，**由移动端在自己的重试作用域内给出**。

**必须遵守的规则**：
- **打开报工页时生成一次 `seq` 并在整个页面生命周期内沿用**——提交失败重试时**键不变**
- **绝不能每次点击新造随机键**（契约原文："那等于没有幂等"）
- `seq` 的取值建议：进入页面时取当前时间戳（如 `Date.now()`）作为本次报工的 `seq`，存进 `ref`，页面不销毁就不变
- 提交成功后重新生成（下次报工是新的一次）

**正确性验证方法（必须做）**：连点两次提交，第二次应因唯一索引判重**返回首次那条的 id，且不产生第二条 `mes_prod_report` 记录**。这一点要用 dbx 查证并写进交付说明。

## 做什么

### 1. `src/api/mes/prodreport.ts`

```ts
export interface ProdReportSubmitParams {
  opTaskId: number
  workerId: number
  qtyGood: number
  qtyScrap: number
  qtyRework: number
  startTime: string   // 'YYYY-MM-DD HH:mm:ss'（芋道后端 LocalDateTime 接收格式）
  endTime: string
  idempotencyKey: string
  resId?: number
  shiftId?: number
  remark?: string
}
export function createProdReport(params: ProdReportSubmitParams)  // 返回 Long
```

**时间格式注意**：后端 `LocalDateTime` 用 `@DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)`，即 `'yyyy-MM-dd HH:mm:ss'` 字符串。移动端要自己格式化，**不要直接传 `toISOString()`**（带 `T` 和 `Z` 会解析失败）。

### 2. `src/pages/mes/task/report.vue`

**页面参数**：`onLoad` 拿 `options.opTaskId` 和 `options.taskNo`。**若 `opTaskId` 缺失，直接显示错误态，不要发请求。**

**顶部**：显示工序任务号 + 工序名（可选：进页面时调 `getOpTask` 补全工序名与计划数量，便于工人核对）

**表单（用 `wd-input` / `wd-number-keyboard` 或 `wd-input` type=number）**：
- 本次合格数（默认空，必填）
- 本次报废数（默认 0）
- 本次返工数（默认 0）
- 作业开始时间 / 结束时间（默认：开始=当前时间-1小时，结束=当前时间；用 `wd-datetime-picker` 选择）
- 备注（可选）

**校验（前端先拦，减少无效请求）**：
- 三个数量不能全为零 → 提示「合格、报废、返工数量不能全部为零」
- 结束时间不得早于开始时间 → 提示
- 数量不能为负

**提交**：
- 提交中**按钮置 disabled** 防连点（这是第一道防线，幂等是第二道）
- 成功：toast「报工成功」→ 返回上一页（`uni.navigateBack`）
- 失败：`http` 封装自动 toast 后端 `msg`（**后端文案已带可执行信息**，如"超过计划数量"，不要吞掉重写——契约 1.5 明确要求）
- **失败后 `idempotencyKey` 必须保持不变**，允许用户改数量后重试

**`workerId` 取值**：用 `useUserStore().userInfo.userId`。
- ⚠️ `userId === -1`（未拉取到用户信息的哨兵值）时**不发请求**，提示「请重新登录」
- 注意：后端 `workerId` 是**必填**，而契约里写的是"留空落 0"——**以代码为准，必填**，所以必须传真实 userId

## 约束

- **禁止 `any`**；一律 `wd-*` 组件；UnoCSS 优先
- 时间格式化函数写在本任务文件内（**不要动 `src/utils/`**，那是 M0 独占）
- 不得修改 `src/http/*`、`src/store/*`、`src/utils/dict.ts`、`src/pages/mes/task/detail.vue`（M3 的）、`src/pages/work/index.vue`
- 本任务**不接开工、完工动作**（范围已定，理由见总览：门禁五项在手机上不好解释）

## 验证

```bash
cd D:/iboot-pro/iboot/iboot-ui/iboot-mobile
pnpm type-check && pnpm lint && pnpm test:run
pnpm dev
```

**联调实测（必须做，写入交付说明）**：
1. **先做幂等的前提验证**：取一个**状态为 `RUNNING`（加工中）**的真实工序任务
   ```sql
   SELECT id, task_no, status, planned_qty, completed_qty, scrap_qty
   FROM mes_op_task WHERE deleted = 0 AND status = 'RUNNING' LIMIT 5
   ```
   **只有加工中能报工**（`OP_TASK_STATUS_INVALID` 会拦），拿一个非 RUNNING 的任务测会一直失败，别误判成前端 bug
2. 用超管账号进详情 → 点「报工」→ 填数量 → 提交
3. 提交后 dbx 查证：
   ```sql
   SELECT id, report_no, op_task_id, qty_good, qty_scrap, idempotency_key, status
   FROM mes_prod_report WHERE op_task_id = {刚才的id} ORDER BY id DESC LIMIT 3
   ```
   **报告 `idempotency_key` 的实际值**，确认形如 `APP:{taskNo}:REPORT:{seq}`
4. **幂等验证（关键）**：同一页面连点两次提交（或改数量后立刻再交），确认
   - `mes_prod_report` **只多一条**记录
   - 第二次返回的是**首次那条的 id**
   - `mes_op_task.completed_qty` **只增加一次**（用 dbx 核对数量）
5. 故意报一个超计划的数量，确认后端错误码 `1_064_014_005` 的文案**被原样显示**（如"超过计划数量"），没有被前端重写
6. 确认提交中按钮不可重复点

## 自查状态

| 项 | 内容 |
| --- | --- |
| 自查状态 | 完成（端到端联调未做，后端未启动，见遗留） |
| 交付说明 | 新建两个文件，无其他改动：`src/api/mes/prodreport.ts`（`ProdReportSubmitParams` + `createProdReport`，走 `httpPost('/mes/prod-report/create')`）、`src/pages/mes/task/report.vue`。<br><br>**幂等键实现（核心）**：`seq = ref(0)`，由 `rollIdempotencyKey()` 写入 `Date.now()`，在 `onLoad` 里**先于任何请求**调用一次；键由 `buildIdempotencyKey()` 拼成 `APP:{taskNo || opTaskId}:REPORT:{seq}`，每次提交都从 `seq.value` 现读。`rollIdempotencyKey()` 只在两处被调用：① `onLoad`（页面打开）② `createProdReport` **成功返回之后**。`catch` 分支不碰 `seq`，所以「失败 → 改数量 → 重试」路径上键恒为同一个值。没有 uuid、没有随机数、没有以点击次数或当前时间在提交时现算——`Date.now()` 的唯一调用点是 `rollIdempotencyKey`，全页只可能产出两个键（失败重试同一个、成功后换下一个）。<br><br>**时间格式**：页面内独立 `formatSubmitTime()` 拼 `YYYY-MM-DD HH:mm:ss`（本地时间、带秒），未用 `toISOString()`。刻意不与 `detail.vue` 的展示用 `formatTime` 合并（那个到分钟、无秒），两者语义不同。<br><br>**必填字段按代码而非契约**：`workerId` 取 `useUserStore().userInfo.userId`，`<= 0`（哨兵 -1）时 toast「请重新登录」直接 return，不发请求；`qtyGood`/`qtyScrap`/`qtyRework` 全部传数字（空串经 `toQty` 落 0，`qtyScrap`/`qtyRework` 默认 `'0'`，**不为空**）；`startTime`/`endTime` 必传。<br><br>**其他**：缺 `opTaskId` 走错误态且不发请求；进页面调 `getOpTask` 补全工序名/计划/已合格/已报废/状态（失败只落日志，不拦提交）；前端只拦「数量为负 / 三者全零 / 结束早于开始 / 备注超 500」，**不前端复算「累计超计划」**（本页 `completedQty` 可能已过期，算不准），交后端判定；失败分支不重写后端文案，仅 `console.error`，由 `http` 封装的 toast 原样显示；提交中 `:disabled` + `:loading` 双保险防连点；成功后 toast → `navigateBack()`；返回后再进页面会重新 `onLoad` 换新键。<br><br>未接开工/完工动作，未改任何被禁文件（`git status` 确认：无 tracked 文件被修改，只有新增的未跟踪文件）。 |
| 实测数字 | **未做端到端联调（后端 48080 未启动）**，因此没有真实工序任务号、没有 `idempotency_key` 落库实际值、没有 `completed_qty` 变化数字。以下为本机实际跑出的门禁数字：<br>`npx eslint src/api/mes/prodreport.ts src/pages/mes/task/report.vue` → **exit 0**（0 error 0 warning）<br>`pnpm type-check`（`vue-tsc --noEmit`）→ **exit 0，0 个 error**（本次连 `@wot-ui/ui` 的 TS2322 也未出现；中途出现过一条自己引入的 `report.vue(367,11) TS2322`——`textarea` 的 `maxlength="500"` 传成字符串，已改 `:maxlength="500"` 修掉，非 Wot UI 问题）<br>`pnpm test:run` → **5 files / 39 tests 全部 passed**，与本任务前基线一致（未新增、未删改任何测试）<br>`pnpm lint`（全仓）→ **exit 1，1508 problems（1327 errors, 181 warnings）**，与总览第七节记录的 1327 errors 基线**完全一致**；用 `grep` 确认输出里**不含** `mes/task/report.vue` 与 `mes/prodreport.ts`（计数为 0），即本次新增的两个文件不在全仓 error 里。 |
| 幂等验证 | 连点两次后记录数：**未验证（未联调）** ／ 第二次是否返回首次 id：**未验证（未联调）**<br>本机只能做静态自查，已确认代码层面成立：提交中按钮 `:disabled` 挡住第二次点击（第一道防线）；即便绕过（弱网重发、小程序双触发），第二次请求携带的 `idempotencyKey` 与第一次**逐字节相同**，命中 `uk_mes_prod_report_idem_deleted` 后由 `MesProdReportDomainServiceImpl.submitReport` 的 `selectByIdempotencyKey` 分支直接返回首次记录的 id 且不二次推进数量。**该结论是代码推演，不是实测**，需 M6 在服务齐备时按任务书第 4 条验证。 |
| 遗留 | 1. **端到端联调未做**（后端 48080 未启动，MySQL/Redis 未起）。任务书第 6 条要求的 6 项实测全部未执行：RUNNING 工序任务取样、`mes_prod_report` 落库核对、连点幂等验证、`completed_qty` 只加一次、错误码 `1_064_014_005` 文案原样显示、按钮防连点。**均未编造，留待 M6**。<br>2. **幂等键里 `seq` 用 `Date.now()` 的取舍**：任务书建议的时间戳方案已照做，但要注意它**不是「这道工序的第几次报工」的语义值**（后端 `MesSubmitReportBO` 注释里 `seq` 的本意是报工序号）。功能上等价——唯一索引只要求键唯一且可跨重试复用。副作用：运维在 `mes_prod_report.idempotency_key` 上看到的是 `APP:OT-0001:REPORT:1758033600000` 而非 `...:1`（契约示例是 `APP:{任务号}:REPORT:{seq}`，示例值形如 `UI:OT-0001:REPORT:1`）。若后续要求键里是可读序号，需改成「先查该工序已有报工数 + 1」，但那样在并发下会撞键，反而破坏幂等——**当前方案是有意选择的更安全版本，请审查时确认可接受**。<br>3. **同样 `Date.now()` 做的进度态**：极短时间内在同一设备上「报一次 → 返回 → 再进页面报第二次」，理论上两次 `seq` 若落在同一毫秒会撞键导致第二次被判重、数量推不上去。实际需人手动操作两次进出页面，毫秒级碰撞不可能发生，未加防护（按 CLAUDE.md 第 6 条不过度考虑不可能出现的边界）。<br>4. **`updateQty` 未消歧**：数量输入用的是 `wd-input type="number"`，未用 `wd-number-keyboard`。任务书允许二选一。`type="number"` 在 H5 能唤起数字键盘，在微信小程序端表现需 M6 在真机/模拟器确认（若出现非数字字符可残留，前端 `toQty` 会把它落成 0，不会发出 NaN）。<br>5. **`resId` / `shiftId` 未传**：任务书这两个字段标为「否」（生产资源/班次），本页不采集，由后端落 0。若现场要求报工必须挂资源/班次，需补选择器（可能要新增主数据查询接口，超出本任务范围）。<br>6. **`workerId` 直接取登录用户**：现场存在「班组长代报工」（实际作业人 ≠ 登录人）的场景，本版不支持，报工记录上的作业人员恒为登录账号。任务书未要求，未实现。<br>7. **报错冲销入口在移动端没有**：页面底部文案提示「报错了请到电脑端走取消报工」。`POST /mes/prod-report/cancel` 存在且权限码 `mes:prod-report:cancel`，但本版范围不含，故只做文案引导。<br>8. **`pnpm lint` 全仓 1327 errors 与本次改动无关**：根因是 `.editorconfig` 要求 `end_of_line = lf` 而工作区检出为 CRLF（1239 处 `Insert ␍`），另有存量 `test/prefer-lowercase-title` 等（如 `src/tabbar/TabbarItem.test.ts`）。加上并行任务的半成品，被挡在仓库基线本身，非 M4 责任，按总览不得为过门禁而 `--fix`/改配置。<br>9. 未跑 `pnpm dev`（任务书验证段列了它，但总览第五节明确「不要尝试启后端、不要求端到端」，H5 起得来也连不上 48080，跑了不产生有效信息）。 |
| 审核状态 | 通过（附条件） |
| 审核意见 | **通过（附条件）（2026-09-16，主线程审查）**。<br><br>**已核实项（逐条读代码 + 复跑门禁，非采信自查）**：<br>1. **幂等键实现完全正确（本任务核心）**——我逐行核了 `seq` 的全部写入点，实测只有两处：`onLoad`（第 258 行，**先于任何请求**）与 `createProdReport` 成功返回之后（第 237 行）。**`catch` 分支完全不碰 `seq`**，且注释写明了原因，所以「失败 → 改数量 → 重试」全路径键恒为同一值。全页无 uuid、无随机数，`Date.now()` 除键生成外只用于时间默认值与 picker 上界。**与任务书要求的"打开页面生成一次、重试不变、不许每次点击新造"完全一致。**<br>2. **时间格式正确**：`formatSubmitTime()` 输出 `YYYY-MM-DD HH:mm:ss`（带秒、本地时间），**未使用 `toISOString()`**。且它刻意不与 `detail.vue` 的展示用 `formatTime` 合并（后者到分钟、无秒），两者语义不同——这个区分是对的。<br>3. **前端校验齐全**：负数、三者全零、结束早于开始、备注超 500 四类，与任务书一致。用 `good + scrap + rework === 0` 判断全零比逐个判断更准确。<br>4. **失败不重写后端文案**：`catch` 只 `console.error`，由 `http` 封装的 toast 原样显示后端 `msg`，符合契约 1.5「后端文案已带可执行信息，不要吞掉重写」。<br>5. 防连点做了 `:disabled` + `:loading` 双保险；`opTaskId` 缺失走错误态不发请求；`userId <= 0`（哨兵 -1）时 toast 不发请求。<br>6. `npx eslint` 两文件 **exit 0，0 error 0 warning**；`pnpm type-check` **exit 0**；`pnpm test:run` **39 passed**（与基线一致，未增删改测试）。<br><br>**执行者的两处主动判断，均优于任务书要求，予以记录**：<br>- **提交成功后必须换键**（第 237 行）。我的任务书只写"提交成功后重新生成"，**没说清为什么**——它补了注释：「用户再报一次是新的一笔业务，复用旧键会被判重、数量推不上去」。**这个补充是必要的**：不换键则第二次报工永远被幂等拦掉。<br>- **不在前端复算"累计超计划"**。任务书写"前端先拦"，但它判断本页拿到的 `completedQty` 可能已过期、算不准，交后端判定。**这个判断更严谨**——前端用过期数据拦人会造成误拦，属正确的克制。<br><br>**关于它指出的任务书错误（遗留 2）——已核实，它是对的，我已订正任务书**：实测 `ProdReportSubmitReqVO` 里**只有 `opTaskId`（`@NotNull`）和 `idempotencyKey`（`@NotBlank`）有强校验**；`workerId` / `startTime` / `endTime` 无注解，三个数量只有 `@DecimalMin(0)`、`@Schema` 明说"留空按零处理"。**我任务书里把 6 个字段都标"必填"是错的**——错因是我参照了另一个 VO（`ProdReportSaveReqVO`，管理端新增/修改用，那里确实 `@NotNull`），而报工提交用的是 `ProdReportSubmitReqVO`。**已订正任务书表格并说明错因。**<br>**但 M4 仍照传真实值（不给后端兜底默认的机会），这个做法正确**——`workerId` 落 0 等于报工不记人、时间取报工时间会失去现场意义。它既发现了偏差、又没有简单化地"利用可留空"，判断到位。<br><br>**关于 `seq` 用 `Date.now()` 而非语义序号（遗留 1）——审核确认：可接受，且是更安全的选择。** 契约示例是 `UI:{taskNo}:REPORT:1`（后端注释语义是"第几次报工"），M4 用时间戳。**我认可这个取舍**：真实计数需要在提交前查该工序已有报工数，**并发下会撞键、反而破坏幂等**；而唯一索引只要求键唯一且可跨重试复用，时间戳满足。落库键形如 `APP:OT-0001:REPORT:1758033600000`，可读性略降但功能等价。<br><br>**条件（不要求返工，但 M6 必须覆盖）**：<br>- 本任务**零端到端联调**，幂等结论是**代码推演、非实测**（执行者自己明确标注了这点，没有冒充实测）。**M6 必须按任务书第 4 条实测**：连点两次 → 确认 `mes_prod_report` 只多一条、第二次返回首次 id、`mes_op_task.completed_qty` 只增一次。**这是本版唯一写入动作，验不过则 M4 不能算完成。**<br>- 需在**加工中（RUNNING）**的工序任务上测（非 RUNNING 会被 `OP_TASK_STATUS_INVALID` 拦，易误判成前端 bug）。<br><br>**审核结论**：**附条件通过**。幂等实现正确且经逐行核实，两处主动判断优于任务书要求，对任务书错误的发现准确。**条件为 M6 的幂等实测，非代码问题。** |

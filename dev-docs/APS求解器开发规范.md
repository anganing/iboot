# APS 求解器开发规范

本文用于 `iboot-module-aps` 里一切与 Timefold 求解相关的改动：调整约束、扩展求解域、接入新的排程能力。已落地的参考实现是"工序 → 资源 + 时序"排程（`com.iboot.pf.module.aps.solver` 包 + `service/solve` 包），新能力一律沿用本文结构。

参考项目 `业务建模/参考项目/jvs-aps` 用的是 OptaPlanner 9.44，它的很多写法在 Timefold Community 版会直接报错，**不要照抄**，具体差异见第 1 节和第 3 节。

## 1. 许可红线（最优先，违反会让服务起不来）

### 1.1 版本锁定 1.34.0，不许升 2.x

`iboot-dependencies` 里 `<timefold.version>1.34.0</timefold.version>`，两条硬理由：

| 理由 | 说明 |
| --- | --- |
| Java 版本 | Timefold 2.x 的 `timefold-solver-build-parent` 是 `maven.compiler.release=21`，本工程是 Java 17；1.x 全线是 17 |
| 可解释性能力收费 | 2.x 的 `Feature` 枚举新增 `SCORE_ANALYSIS` 与 `RECOMMENDATIONS`，把评分分析、推荐分配、方案对比一并划入商业版；约束命中快照依赖第一项 |

2.x 相对 1.34.0 新增的收费项按 `TimefoldSolverEnterpriseService.Feature` 枚举逐条核对如下（1.34.0 只有 `MULTITHREADED_SOLVING`、`PARTITIONED_SEARCH`、`NEARBY_SELECTION`、`AUTOMATIC_NODE_SHARING`、`MULTISTAGE_MOVE`、`CONSTRAINT_PROFILING` 六项）：

| 新增收费能力 | 闸门 | 涉及 API | 对本工程的影响 |
| --- | --- | --- | --- |
| 评分分析 | `SCORE_ANALYSIS` | `SolutionManager.analyze()`、`ScoreAnalysis`、`AnalyzableIncrementalScoreCalculator`（`ScoreExplanation` 2.x 直接删除） | 约束命中快照的唯一数据来源，升级即断 |
| 推荐分配 | `RECOMMENDATIONS` | `recommendAssignment()`（1.x 叫 `recommendFit()`）、`RecommendedAssignment` | 当前未使用，无影响 |
| 方案对比 | `RECOMMENDATIONS` | `SolutionManager.diff()`、`PlanningSolutionDiff` | 当前未使用；注意它复用推荐分配的闸门 |

1.34.0 是 1.x 线最后一个版本，Apache-2.0，`DefaultSolutionManager` 字节码里没有任何 enterprise 闸门。要升 2.x 必须同时评估整体升 JDK 21 与购买 Enterprise 许可，属于独立立项。

### 1.2 禁止出现的配置项

Community 版用 `TimefoldSolverEnterpriseService.Feature` 枚举当闸门，命中即抛 `EnterpriseLicenseException`。以下配置项**一个都不许出现**：

| 配置项 | 对应收费功能 |
| --- | --- |
| `moveThreadCount` / `moveThreadBufferSize` | 多线程增量求解 |
| `PartitionedSearchPhaseConfig` | 分区搜索 |
| `NearbySelectionConfig` | 近邻选择 |
| `MultistageMoveSelectorConfig` | 多阶段移动选择器 |
| `constraintStreamProfilingEnabled` | 约束剖析 |
| 自动节点共享 | Automatic node sharing |

网上绝大多数 OptaPlanner 示例都带 `withMoveThreadCount("AUTO")`，jvs-aps 的 `SolveManageConfigurator` 也是（它的 OptaPlanner 9.44 没有这道闸门）。抄过来即炸。

`ApsSolverLicenseGuardTest` 是这条线的护栏，改求解配置后必须保证它绿。

### 1.3 性能不够时的免费路线

按顺序尝试，**不许偷偷打开收费开关**：

1. 收紧终止条件（`unimprovedSpentLimit` 调小）
2. 精简约束流（减少 `join`，多用 `groupBy` 聚合）
3. 缩小场景计划窗口，拆成多场景并行——并行度靠 `SolverManagerConfig.withParallelSolverCount`，这个类没有 enterprise 闸门，免费
4. 以上都不够再评估 Enterprise 许可

## 2. 输入必须来自投影，不许回查主数据

求解输入只能读四张投影表（`aps_projection_mat` / `_inv` / `_order` / `_resource`）。这是"同一场景重复求解结果可复现"的前提：主数据改一次工时，历史版本就没法复算，追溯链直接断。

唯一例外是 `mdm_res_changeover` 换型矩阵：配置型主数据、量小、不随场景变化，单独建投影表的收益不抵维护成本。新增例外必须在评审里说明理由。

### 2.1 投影表承载的求解参数

| 表 | 求解用到的列 | 来源口径 |
| --- | --- | --- |
| `aps_projection_resource` | `shift_start_time` / `shift_end_time` | `ShiftApi` 班次起止，用于展开可用时间窗 |
| | `capability_json` | `ResourceApi.getResourceCapabilityListByResIds`，元素结构见 `ApsResourceCapability` |
| `aps_projection_order` | `op_id` / `op_code` / `op_name` | 工序任务自带，缺失时回落路线工序上的 `opId` |
| | `seq` | 生成同工单内前后序依赖 |
| | `setup_minutes` / `std_minutes_per_unit` / `queue_minutes` / `move_minutes` | **`mdm_route_op` 的 override 优先，回落 `mdm_op` 标准值** |
| | `teardown_minutes` / `duration_type` / `std_duration` / `man_qty` | 取 `mdm_op` 标准值（路线层没有 override 字段） |
| | `changeover_group` | 成品物料 `mdm_mat.changeover_group` |
| | `material_shortage_flag` / `material_short_qty` | `ApsMaterialKitCalculator`：BOM 展开的用料需求对冲库存投影的合格可用量 |

新增求解参数的固定动作：dbx 加列 → 改 DO/RespVO → 在投影刷新里冻结取值 → 同步 `src/test/resources/sql/create_tables.sql`。漏最后一步会让单测建表和生产库脱节。

### 2.2 求解前置条件

跑求解前场景必须已经刷过**资源投影**和**工单投影**，否则直接报 `SOLVE_NO_RESOURCE` / `SOLVE_NO_TASK`。这不是防御性代码，是真实的操作顺序：投影没刷等于没有输入。

资源能力矩阵 `mdm_res_capability` 为空时**不硬卡资源**（见 `ApsSolverTask#isResourceEligible`）：现场刚上线时能力矩阵往往是空的，硬卡会让所有任务都排不出来。代价是能力约束此时等于不生效，上线后要尽快把能力矩阵补齐。

### 2.3 物料齐套结论在投影阶段冻结

齐套判定放在刷新工单投影时做（`ApsMaterialKitCalculator`），而不是求解时现算：库存每分钟都在动，现算会让同一场景重复求解得出不同结论，追溯链断掉。齐套结论属于输入的一部分。

三条口径上的取舍：

1. **按交期先后分配库存**，而不是各工序独立判断。同一种料被多个工单争用时，独立判断会让每个工单都以为自己有料，算出一份全都齐套的假结论。先到期的先占，与车间实际分配一致。
2. **只认合格库存**，待检、不合格、冻结的料不参与对冲，口径与 `refreshShortage` 一致。
3. **只算现有库存，不含在途采购与上游工序产出**，结论偏保守：采购件下周到货、本可先排上的工序会被判为缺料。要放开需要 WMS 提供在途量门面，属于独立范围。

BOM 只展开一层：领料清单本来就是单层 BOM，多层展开靠 MRP 逐层下达。虚拟件（`issueMethod = PHANTOM`）跳过，它不占实物库存。

## 3. 求解域建模

```text
solver/
├── ApsSolverProblemBuilder.java      # 投影 -> 求解输入
├── ApsConstraintWeightResolver.java  # 策略配置 -> ConstraintWeightOverrides
├── model/
│   ├── ApsSolution.java              # @PlanningSolution
│   ├── ApsSolverResource.java        # @PlanningEntity，持列表变量
│   ├── ApsSolverTask.java            # @PlanningEntity，被排对象 + 影子变量
│   ├── ApsTimeWindow.java            # 可用时间窗
│   └── ApsSolverConstants.java
├── calculate/
│   ├── ApsDurationCalculator.java    # 工时与换型
│   └── ApsCalendarCalculator.java    # 按日历推进时间
├── constraint/
│   ├── ApsConstraintProvider.java
│   └── ApsConstraintNames.java       # 约束名常量
└── config/
    ├── ApsStrategyConfig.java        # config_json 结构
    └── ApsStrategyConfigParser.java
```

`ai.timefold` 类型不许出这个包，`api` 门面与 VO 一律不暴露。`ApiContractTest` 守着这条线。

### 3.1 决策变量只有"排哪台设备、排第几位"

资源持 `@PlanningListVariable`，工序任务是列表元素。这样"同一资源上任务时间不重叠"由数据结构保证，**不需要写重叠惩罚约束**。jvs-aps 用的是普通规划变量才需要那条约束，照搬只会白算一遍笛卡尔积。

时间不是决策变量：资源与顺序确定后，起止时间唯一确定。搜索空间因此小几个数量级。

### 3.2 时间推导用声明式影子变量，不要手写监听器

固定写法（见 `ApsSolverTask`）：

```java
@ShadowVariable(supplierName = "startTimeSupplier")
private LocalDateTime startTime;

@ShadowSources({"resource", "previousTask.endTime", "predecessors[].endTime"})
public LocalDateTime startTimeSupplier(ApsSolution solution) { ... }
```

`predecessors[].endTime` 这条集合路径是**跨资源传播**的声明：前道工序无论排在哪台设备上，它的完工时间变了都会触发本任务重算。

不用手写 `ListVariableListener` 的两个原因：

1. Timefold 1.34 的 legacy 自定义监听器在 `beforeEntityAdded` 里会把列表元素当实体传进来，直接 `ClassCastException`
2. 手写要自己实现跨实体传播与环路保护，漏一处就是 score corruption——排出来的计划看着可行，实际违反前后序

也不要用 `@CascadingUpdateShadowVariable`：它只沿同一个列表向后传播，覆盖不到跨资源的工序前后序。

环路处理：求解中间态可能出现"前道排在后道之后"的循环等待，求解器会把相关影子变量置空并把 `@ShadowVariablesInconsistent` 字段置真。必须有一条硬约束罚掉它，**且必须用 `forEachUnfiltered`**——`forEach` 会把不一致的实体过滤掉，罚不到。

### 3.3 锁定任务用 `@PlanningPinToIndex`

已开工任务在 `ApsSolverProblemBuilder#assignPinnedTasks` 里按原时间顺序预先塞进原资源队列，再设 `firstUnpinnedIndex`。

不要在任务上打 `@PlanningPin`：列表变量模型里"钉住某个值"的官方机制是按下标切分资源队列。也不要把锁定任务从值域里剔除——值域必须包含全部任务，剔除会让值域与列表内容不一致，求解器直接报错。

### 3.4 工时口径

```text
duration = changeover + setup + qty × perUnit ÷ (资源效率 × 能力效率) + teardown
```

- 只有加工工时按效率折算，准备与收尾是固定动作
- `duration_type = FIXED` 时直接取 `std_duration`，不随数量与效率变化
- 效率配成 0 或负数时回落 1，避免除零把工时算成无穷
- 加工分钟数**向上取整**：宁可多留产能，也不让计划比实际乐观
- `queue_minutes` / `move_minutes` **不计入 duration**：它们是工序间的等待与转移，只把后道开工时间往后推，不占资源产能。混进去会让资源负荷虚高
- 换型时长按"同资源上前一道任务的换型分组 → 本道分组"查换型矩阵；分组相同或任一为空时不产生换型

### 3.5 得分类型用 `HardSoftLongScore`

不用 int 版本：交期延误、总完工时间按分钟累加，上千任务很容易越过 int 上限，溢出后得分变负，求解器会朝错误方向优化。

## 4. 约束与权重

### 4.1 约束名是三处的对齐锚点

`ApsConstraintNames` 里的常量同时是：`asConstraint()` 的入参、`aps_strategy.config_json` 的权重键、约束命中快照的分类键。**任何一处改名都会让历史策略配置与历史快照对不上**，所以只能在这个类里定义，不许在别处写字面量。

用英文而不是中文：这些键会进 JSON 配置和快照，中文键在跨系统传输与日志里容易出编码问题。面向人的那部分信息（中文名、硬软类别、"权重调大调小会怎样"的业务说明）登记在 `ApsConstraintCatalog`，中文展示走 `ApsConstraintCatalog.displayName()`。

两者分开的理由是职责不同：`ApsConstraintNames` 是名字锚点，改动即破坏兼容；catalog 是展示层的说明书，随时可以润色文案。catalog 同时是策略配置页面的数据源——`GET /aps/strategy/constraint-metadata` 把它和 `ApsConstraintWeightResolver` 的默认权重一起返回给前端，页面上"有哪些约束可调、默认调到多少"全部由后端决定，前端不维护第二份清单。

### 4.2 现有约束清单

| 约束名 | 类型 | 惩罚量 |
| --- | --- | --- |
| `resourceCapability` | 硬 | 计数 |
| `operationPrecedence` | 硬 | 提前的分钟数 |
| `pinnedTaskImmutable` | 硬 | 计数 |
| `calendarCapacity` | 硬 | 计数 |
| `shadowInconsistent` | 硬 | 计数 |
| `materialShortage` | 软 | 同资源上被它挡住的有料工序数量 |
| `dueDateDelay` | 软 | 延误分钟 × 优先级权重 |
| `totalMakespan` | 软 | 最晚完工时刻（分钟） |
| `priorityInversion` | 软 | 优先级差 |
| `precedenceIdleGap` | 软 | 超出必要间隔的空等分钟 |
| `changeoverTime` | 软 | 换型分钟 |

硬约束的惩罚量优先取"违反了多少"而不是简单计数：只按计数罚会让"提前 1 分钟"和"提前 3 天"同分，搜索没有梯度。

`materialShortage` 是软约束而不是硬约束，有两个理由。缺料是问题事实，求解器没有任何移动能把它变成不缺，硬约束会让整个版本永远判为不可行（`hardScore < 0` 直接 `FAILED`，计划员连看一眼都做不到）。软约束才能真正改变分派结果：把缺料工序往后挪、把产能让给现在就能干的活。它由 `aps_strategy.config_json` 的 `materialConstrained` 开关控制，关掉时约束方法直接放行，**不要把它从 `defineConstraints` 数组里摘掉**——约束集合必须稳定，否则历史命中快照的分类键会对不上，权重配置也会因"未知约束名"被拒。

### 4.3 权重从策略配置来，不写死

默认权重定义在 `ApsConstraintWeightResolver`，`ConstraintProvider` 里的 `ONE_HARD`/`ONE_SOFT` 只是占位单位。软约束权重数量级刻意拉开：

```
交期延误 100 > 总完工 10 > 优先级 5 > 等待与换型 1
```

齐套让路的默认权重是 20，排在交期之后、总完工之前：让有料的活先干有真实收益，但不值得为它牺牲交期。

都设成 1 的话，几百个任务的等待分钟数会盖过交期延误，求解器会为了压缩空隙而牺牲交期，这与制造业"先保交期"的常识相反。

`config_json` 里出现未知约束名时 `ApsStrategyConfigParser` 直接报错。静默忽略会让人以为权重生效了，实际排程结果完全不受影响。

新增约束的固定动作：在 `ApsConstraintNames` 加常量 → 在 `ApsConstraintCatalog` 登记中文名、硬软类别与业务说明 → 加约束方法 → 加进 `defineConstraints` 数组 → 在 `ApsConstraintWeightResolver` 里给默认权重 → 补 `ConstraintVerifier` 单测 → 更新 `ApsConstraintProviderTest` 里的约束数量断言。漏掉 catalog 那步不会让编译失败，但页面上会少一条可调项，由 `ApsConstraintCatalogTest` 兜底。

### 4.4 策略配置的读写两条路径

`aps_strategy.config_json` 对外不再是字符串：`StrategySaveReqVO.config` 收结构化对象，Service 用 `ApsStrategyConfigConverter` 序列化后落库。两条路径的严格程度刻意不同：

- **写路径严格**。Bean Validation 管数值范围（耗时上限 60–3600 秒、无改进终止 10–3600 秒、单条权重 0–1000000），落库前再过一遍 `ApsStrategyConfigParser` 兜住"约束名是否存在"。能存进去的一定能拿去求解。
- **读路径宽松**。解析失败不抛异常，回落全默认配置并置 `configInvalid` + `configInvalidReason`。库里存在早于结构化配置的历史数据（权重键不是约束名、值不是 `{hard, soft}` 结构），严格化会让它们永远打不开，只能靠 DML 修；宽松读 + 保存即修正不需要迁移脚本。

权重全量提交而不是只提交与默认值不同的项：让每条约束的实际权重在库里与快照里都显式可见，避免"没写就是走默认"这种要靠猜的状态。

求解耗时上限封顶 3600 秒是被场景锁的 TTL 约束的：`aps:solve:lock:{scenarioId}` 只保 2 小时，求解时长必须显著小于它，否则锁过期后同场景能并发求解，两份计划互相覆盖。要放开得先改锁的续期策略。

## 5. 求解编排

### 5.1 事务边界

`ApsSolveServiceImpl#doSolve` **不许加 `@Transactional`**。跨越整轮求解（几十秒到几分钟）的事务会占着数据库连接不放，中途的进度也无法提交。事务边界在 `ApsSolveResultWriter#save` 与 `ApsVersionService#markSolved/markFailed` 各自的方法里。

### 5.2 场景锁与线程池

- 场景级 Redis 锁 `aps:solve:lock:{scenarioId}`，TTL 2 小时（比默认求解上限留余量，进程被强杀时自动释放）
- **先占锁再建版本**：反过来会在并发时留下一堆 `SOLVING` 的空版本
- 求解编排跑在独立线程池 `apsSolverExecutor`（core 1 / max 2 / 有界队列 / 拒绝时抛异常），不占用框架共享的 `@Async` 池——求解一次几分钟，挤在共享池里会把日志落库、消息推送这些短任务全堵死
- 异步线程里必须整体 try-catch-finally：异常没人接会让版本永远停在 `SOLVING`、锁到过期才释放，场景在这期间完全排不了

### 5.3 幂等键的两种形态

| 动作 | 幂等键 | 理由 |
| --- | --- | --- |
| 确认、发布 | `UI:CONFIRM_VERSION:{versionId}` 固定键 | 对同一版本的一次性动作，重复点击必须拦 |
| 生成排程 | 带时间戳 | 每次都要产出新版本，固定键会让第二次生成返回第一次的版本编号 |

生成排程的并发保护靠 Redis 场景锁，不靠幂等键。

### 5.4 结果落库

`aps_task` / `aps_task_dep` **整表物理重建**，不做增量更新：求解产出的是一份完整新计划，逐条比对既复杂又容易留下上一轮残留。必须物理删除，因为唯一索引带 `deleted` 列，逻辑删除后重算会撞唯一键。删除顺序是先依赖后任务。

`hardScore < 0` 视为无可行解：版本置 `FAILED`，写 `ERROR` 快照，**不允许发布**。宁可判失败也不发一份违反约束的计划。

### 5.5 四类快照的分工

| 类型 | 回答的问题 | 内容 |
| --- | --- | --- |
| `INPUT` | 这次喂进去的是什么 | 输入指纹：规模 + 策略配置 + 各投影快照时间 |
| `SOLUTION` | 排出来的分派是什么 | 精简分派结果 |
| `CONSTRAINT_MATCH` | 为什么是这个分数、哪条约束拖后腿 | `SolutionManager.analyze()` 按约束聚合 |
| `ERROR` | 为什么没排出来 | 异常摘要 |

`INPUT` 不存整份输入：几千任务的完整 JSON 会把 `snapshot_json` 撑爆，而投影表本身就是输入的持久化形式。

`checksum` 用内容 SHA-256，写前先按"版本 + 类型 + checksum"查一次跳过，避免撞 `uk_aps_solver_snapshot_deleted`。

`CONSTRAINT_MATCH` 是四类里唯一有专门读取通路的：`ApsConstraintMatchConverter` 把快照 JSON 翻成"约束 / 生效权重 / 命中次数 / 扣分"的对照表，由 `GET /aps/solver-snapshot/constraint-match?versionId=` 按版本取最新一条，前端在排程版本行的"约束命中"入口与快照详情弹窗里共用同一个展示组件。

两条约定必须守住。分数字符串（形如 `0hard/-1soft`）只在后端解析，不许让前端认这个格式，否则将来换分数类型页面会静默显示空白；权重展示取绝对值，与策略配置页填的正数同量纲，计划员才能照着这张表回去调权重。解析读的是历史留痕数据，认不出的结构一律返回空清单让前端回落到原始 JSON，绝不抛错——一条老快照不该让详情页打不开，护栏是 `ApsConstraintMatchConverterTest`。

### 5.6 进度双通路

WebSocket 定向推给发起人（消息类型 `aps-solve-progress`）+ Redis 存最新进度（`aps:solve:progress:{versionId}`，TTL 24h）+ REST 补偿查询 `GET /aps/version/solve-progress`。

三者缺一不可：只推不存，刷新页面就什么都看不到；只存不推，要靠前端轮询，长求解下实时性和频率没法兼顾。参考项目 jvs-aps 是纯轮询方案，我们保留 REST 只作兜底。

推送失败只记日志，绝不让一次 WebSocket 抖动把整轮求解带崩——进度是观测手段，不是业务数据。`WebSocketSenderApi` 用 `@Autowired(required = false)` 注入，因为 `iboot.websocket.enable` 可以关。

## 6. 版本状态机

```
SOLVING ──> SOLVED ──> CONFIRMED ──> PUBLISHED ──> REPLACED
   └──────> FAILED（终态）
```

`FAILED` 是终态：失败版本不允许确认发布，重排要生成新版本。保留失败记录而不是删掉，是为了让"这个场景什么时候排不出来过"可追溯。

`SOLVING → CONFIRMED` 保留（允许人工确认还在求解中的版本），这是历史行为，不要顺手删掉。

## 7. 测试要求

| 层次 | 覆盖内容 | 参考类 |
| --- | --- | --- |
| 许可守卫 | 无收费开关、`buildSolver()` 能构建 | `ApsSolverLicenseGuardTest` |
| 工时计算 | FIXED / PER_UNIT / 双效率折算 / 向上取整 / 换型叠加 | `ApsDurationCalculatorTest` |
| 日历推进 | 跨班次 / 跨天 / 卡窗口边界 / 产能耗尽返回 null | `ApsCalendarCalculatorTest` |
| 策略配置 | 默认值 / 非正数回落 / 未知约束名报错 / 倒排拒绝 | `ApsStrategyConfigParserTest` |
| 约束 | 每条约束用 `ConstraintVerifier` 单独验证惩罚量 | `ApsConstraintProviderTest` |
| 端到端 | 真实求解，断言硬分为 0 + 四条不变量 | `ApsSolverEndToEndTest` |
| 编排 | 幂等 / 场景锁并发拦截 / 取消前置校验 | `ApsSolveServiceImplTest` |

两条测试写法上的注意：

1. **约束必须逐条验证**。只跑整体求解看不出某条约束是否真的生效：分数是所有约束叠加的结果，一条写错很容易被掩盖。为此 `ApsConstraintProvider` 的约束方法是包级可见而非 private。
2. **编排测试要把线程池换成"只收不跑"的执行器**。真跑求解会让锁在几秒后释放，并发拦截的断言就成了和后台线程赛跑；同步执行也不行，那会让求解在返回前结束，观察不到"正在求解"这个中间态。

验证命令：

```powershell
mvn -pl iboot-module-aps -am test
pnpm exec eslint --no-error-on-unmatched-pattern "apps/web-ele/src/views/aps/**/*.{vue,ts}"
```

## 8. 本轮未做的能力

改动前先确认是否落在这些空白里，避免重复设计：

| 能力 | 现状 |
| --- | --- |
| 倒排（`BACKWARD`） | 配置成它会在解析阶段报错；需要另一套时间推导逻辑 |
| `parallel_capacity > 1` | 按 1 处理；多工位并行要把一台资源拆成多个工位实体 |
| 人员 / 班组双资源联合排程 | 未做；`man_qty` 已投影但不参与求解 |
| 齐套约束的在途量 | 只算现有库存，不含在途采购与上游工序产出，结论偏保守；需要 WMS 补在途量门面 |
| 齐套的物料到位时间 | 只有"缺/不缺"二值，没有"什么时候到"，所以只能让路、不能按到货日期精确后推 |

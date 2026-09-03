# MDM 基础数据补齐方案（支撑 WMS/MES/APS）

- 版本：v1.0.0
- 日期：2026-08-28
- 状态：待实施
- 适用范围：`iboot-module-mdm` 及配套 `iboot-ui/iboot-ui-admin-vben/apps/web-ele`
- 实施约定：本文档为实施依据，编码前必须先读 [`dev-docs/通用CRUD开发流程.md`](通用CRUD开发流程.md)

---

## 1. 背景与判定依据

### 1.1 现状盘点

`iboot-module-mdm` 已建 22 张表，覆盖《衡枢智造平台架构基石 v3.0.0》中标注【内建】的结构性基石：

| 已具备 | 对应表 | 基石编号 |
| --- | --- | --- |
| 组织三元组分离 | `mdm_inv_org`、`mdm_plant`、`mdm_workshop` | #9 |
| 物料/分类/单位/换算 | `mdm_mat`、`mdm_mat_cat`、`mdm_uom`、`mdm_uom_conv` | #7 |
| BOM 版本化 | `mdm_bom`、`mdm_bom_line` | #8 |
| 工艺路线版本化 | `mdm_route`、`mdm_route_op`、`mdm_route_op_mat` | #8 |
| 产能模型骨架 | `mdm_wc`、`mdm_res`、`mdm_shift`、`mdm_res_calendar` | — |
| 项目归属预留 | `mdm_project`、`mdm_project_task` | #10 |
| 替代料 | `mdm_mat_alt` | — |
| 供应商/客户档案 | `mdm_supplier`、`mdm_customer` | — |
| 标准工序 | `mdm_op` | — |

WMS/MES/APS 三个模块已建 57 张表（`wms_*` 23 张、`mes_*` 21 张、`aps_*` 13 张），业务模块骨架完整。

### 1.2 核心问题

**结构完整，但"可算"的数据不够。** 下游表结构已建好并预留了消费点，MDM 侧缺少对应数据源字段，属于"契约已定、数据源为空"。必须在 WMS/MES/APS 业务编码前补齐，否则产生三类返工：

| # | 问题 | 下游已预留的消费点 | MDM 侧缺口 | 后果 |
| --- | --- | --- | --- | --- |
| 1 | **APS 无法计算工序占用时长** | `aps_task.duration_minutes`、`aps_projection_resource.capability_json` | `mdm_op` 仅有单一 `std_duration`；`mdm_res` 只有 `wc_id/res_type/code/name/status` | 排程算不出任何工序时长，Timefold 无法建模 |
| 2 | **WMS 上架校验无法执行** | `wms_loc.max_weight/max_volume/max_length/max_width/max_height/dimension_check_mode`、`wms_container_type.max_weight/max_volume` | `mdm_mat` 无重量、尺寸、体积 | 三档尺寸校验模式永远跑不起来，立库上架必然爆仓或压坏 |
| 3 | **MES 齐套需求算不准** | `mes_kit_snapshot.req_qty`、`mes_material_req_line.must_issue_before_start` | `mdm_bom_line`、`mdm_route_op_mat` 均只有 `qty`，无损耗率与供料方式 | 倒冲件与虚拟件被错误纳入齐套与领料，开工门禁误判 |

次级缺口：`mes_dispatch_task` 已有 `team_id`/`operator_id` 但无班组主数据；`mes_defect.defect_code` 为自由文本，无缺陷代码库，缺陷帕累托分析无从做起。

### 1.3 ESOP 归属决策

ESOP 工艺指导书落在 **MDM**，挂 `mdm_route_op` 之下。理由：

1. 指导书是"产品×工序"的工艺依据，属主数据而非执行数据。
2. `RouteServiceImpl.validateRouteEditable` 已实现"route 启用即锁"机制，ESOP 明细天然随 route 版本冻结，无需额外冻结字段。
3. `mes_prod_order` 已锁 `route_id + route_version`，工单锁版本即锁指导书，追溯链完整，无需快照表。
4. MES 侧只做现场展示与阅读确认，不持有文档数据。

> 说明：架构基石文档 7.4 节将 ESOP 定档为【演进】，本方案将其提前至 P1 实施。原因是 ESOP 挂载点必须与 route 版本冻结机制耦合，后补需回头改 route 发布流程与版本复制逻辑，符合"后补成本三问"中的两问，不宜再推迟。

---

## 2. 实施范围与优先级

| 阶段 | 内容 | 阻塞对象 |
| --- | --- | --- |
| **P0** | 工时模型、资源产能与能力、工作日历模板、BOM 与工序用料补充、物料计划与储运属性 | APS 排程、WMS 上架、MES 齐套 |
| **P1** | ESOP 工艺指导书、缺陷代码库、班组与技能、工装模具刀具、换型矩阵 | MES 现场执行准确性、APS 换型优化 |

P0 与 P1 本期一次性补齐。P0 在关键路径上，优先开工；P1 可并行。

---

## 3. P0 实施内容

### 3.1 工时模型

**改 `mdm_op`（扩字段）**

现有 `std_duration`（标准时长/分钟）语义含混，既可能表示固定工时也可能表示单件工时，无法计算。重构为可计算模型：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `setup_minutes` | int | 准备/换型工时 |
| `std_minutes_per_unit` | decimal(18,6) | 单件加工工时 |
| `teardown_minutes` | int | 收尾工时 |
| `queue_minutes` | int | 排队等待时间 |
| `move_minutes` | int | 转移时间 |
| `duration_type` | varchar(32) | `FIXED` 固定工时 / `PER_UNIT` 按件累加 |
| `man_qty` | int | 标准人数 |

`std_duration` 保留不删，仅作为 `FIXED` 场景取值，不再承担双重语义。保留原因见 6.9。

**改 `mdm_route_op`（新增 override 字段）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `override_setup_minutes` | int | 为空则继承标准工序 |
| `override_std_minutes_per_unit` | decimal(18,6) | 为空则继承标准工序 |
| `override_queue_minutes` | int | 为空则继承标准工序 |
| `override_move_minutes` | int | 为空则继承标准工序 |

产品专属工时差异不污染标准工序主档。

**时长计算口径（统一约定）**

```text
duration = setup + qty × per_unit ÷ 资源效率 + teardown
```

`queue_minutes` / `move_minutes` 作为工序间间隔参与排程，但不占用资源。MDM 侧只提供参数，计算实现落在 APS 投影层。

### 3.2 资源产能与能力

**改 `mdm_res`（扩字段）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `efficiency` | decimal(8,4) | 效率系数，默认 1.0 |
| `parallel_capacity` | int | 可同时承载任务数，默认 1 |
| `res_group` | varchar(64) | 资源组，同组可互替排程 |
| `calendar_id` | bigint | 默认日历模板 |
| `plant_id` | bigint | 冗余归属 |
| `workshop_id` | bigint | 冗余归属 |

`plant_id`/`workshop_id` 冗余理由：`aps_projection_resource` 已按 `plant_id`/`workshop_id`/`wc_id` 三级投影，冗余可避免每次投影反查 `wc → workshop → plant` 三级链路。

**新增 `mdm_res_capability`（资源能力）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `res_id` | bigint | 生产资源编号 |
| `op_id` | bigint | 标准工序编号 |
| `preferred_flag` | bit(1) | 是否首选资源 |
| `efficiency` | decimal(8,4) | 该资源做该工序的专属效率系数 |
| `status` | tinyint | 状态：0-启用，1-停用 |

唯一索引：`res_id + op_id + deleted`。

这是 `aps_projection_resource.capability_json` 的唯一数据源，也是"工序可在哪些资源上做"的候选集。缺它排程只能人工指定资源，APS 失去意义。

### 3.3 工作日历模板

现有 `mdm_res_calendar` 是"资源×日期×班次×可用分钟"的展开明细，适合 APS 直接读，但无生成来源，靠人工逐天维护不可用。补齐模板层：

**新增 `mdm_calendar`（日历主档）**

`id`、`code`、`name`、`status`、审计字段。

**新增 `mdm_calendar_rule`（常规工作模式）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `calendar_id` | bigint | 日历编号 |
| `day_of_week` | tinyint | 星期几，1-7 |
| `shift_id` | bigint | 班次编号 |
| `available_minutes` | int | 可用分钟数 |

**新增 `mdm_calendar_exception`（日期例外）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `calendar_id` | bigint | 日历编号 |
| `exception_date` | date | 例外日期 |
| `exception_type` | varchar(32) | `HOLIDAY` 停工 / `OVERTIME` 加班 / `MAINTENANCE` 计划保养 |
| `shift_id` | bigint | 班次编号 |
| `available_minutes` | int | 可用分钟数 |

例外覆盖常规规则。

**改 `mdm_res_calendar`（新增来源标记）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `source_calendar_id` | bigint | 生成来源日历模板编号，0 表示人工录入 |
| `generated_flag` | bit(1) | 是否模板生成 |

表结构其余不变，改由"按日历模板展开生成"填充。提供按资源 + 日期区间的**幂等生成接口**：同键覆盖，不产生重复行；`generated_flag = false` 的人工调整行重生成时不被覆盖。

### 3.4 BOM 与工序用料补充

**改 `mdm_bom_line`**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `scrap_rate` | decimal(8,6) | 损耗率 |
| `issue_method` | varchar(32) | `PICK` 领料 / `BACKFLUSH` 倒冲 / `PHANTOM` 虚拟件 / `DIRECT` 直送线边 |
| `route_op_seq` | int | 用料绑定的工序顺序号，实现工序级齐套 |
| `position_no` | varchar(64) | 位号/装配位置 |
| `effective_date` | date | 行级生效日期 |
| `expiry_date` | date | 行级失效日期 |

**改 `mdm_route_op_mat`**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `scrap_rate` | decimal(8,6) | 损耗率 |
| `issue_method` | varchar(32) | 同上 |
| `must_issue_before_start` | bit(1) | 是否必须开工前发料 |

`must_issue_before_start` 与 `mes_material_req_line.must_issue_before_start` 对齐，作为开工门禁的数据源。

**需求量计算口径（统一约定）**

```text
req_qty = 父项数量 × 单位用量 ÷ (1 - scrap_rate)
```

- `PHANTOM`：不产生需求，直接向下展开子项。
- `BACKFLUSH`：不进齐套检查，报工时反冲扣减。

MDM 提供参数，计算实现落在 MES 齐套与领料服务。

### 3.5 物料计划与储运属性

**改 `mdm_mat`**

储运属性（支撑 WMS 上架尺寸/承重校验、容器装箱）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `net_weight` | decimal(20,6) | 净重 |
| `gross_weight` | decimal(20,6) | 毛重 |
| `weight_uom_id` | bigint | 重量单位编号 |
| `length` | decimal(20,6) | 长，毫米 |
| `width` | decimal(20,6) | 宽，毫米 |
| `height` | decimal(20,6) | 高，毫米 |
| `volume` | decimal(20,6) | 体积，立方毫米 |

计划参数（支撑 APS 缺料分析与补货建议）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `plan_strategy` | varchar(32) | `MTO` 按单 / `MTS` 备库 |
| `lead_time_days` | int | 生产/采购提前期 |
| `safety_stock` | decimal(20,6) | 安全库存 |
| `min_order_qty` | decimal(20,6) | 最小订货量 |
| `order_multiple` | decimal(20,6) | 订货倍数 |

仓储默认值：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `default_wh_id` | bigint | 默认仓库编号 |
| `default_loc_id` | bigint | 默认库位编号 |
| `pack_qty` | decimal(20,6) | 标准包装量 |
| `abc_class` | varchar(8) | ABC 分类，供循环盘点与拣货策略使用 |

尺寸单位必须与 `wms_loc`、`wms_container_type` 现有字段保持一致（毫米）。

---

## 4. P1 实施内容

### 4.1 ESOP 工艺指导书

**新增 `mdm_route_op_esop`（作业步骤）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `route_op_id` | bigint | 路线工序编号 |
| `seq` | int | 展示顺序 |
| `title` | varchar(128) | 标题 |
| `content` | text | 作业要点文本 |
| `param_json` | text | 工艺参数结构化 JSON |
| `required_confirm_flag` | bit(1) | 是否要求作业者确认阅读 |
| `status` | tinyint | 状态：0-启用，1-停用 |

唯一索引 `uk_mdm_route_op_esop_route_op_seq_deleted (route_op_id, seq, deleted)`，同一工序内顺序号不重复。

**新增 `mdm_route_op_esop_file`（作业步骤附件）**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `esop_id` | bigint | 作业步骤编号 |
| `seq` | int | 附件展示顺序 |
| `file_type` | varchar(32) | `IMAGE`/`VIDEO`/`PDF`/`DRAWING`/`OFFICE`/`OTHER`，后端按扩展名判定 |
| `file_name` | varchar(255) | 原始文件名，由上传组件回传 |
| `file_url` | varchar(512) | 去签名的对象地址 |
| `file_id` | bigint | infra 文件编号 |

实施要求：

1. 文件存储复用 infra 文件服务，不自建存储。一个作业步骤可挂多个附件（图/视频/图纸并存），`file_url` 只存去掉签名参数的对象地址，读取时后端用 `FileApi.presignGetUrl` 重新签发，避免预签名过期。
2. 写入受 `validateRouteEditable` 保护：route 启用后 ESOP 明细不可增删改，与 `mdm_route_op` 行为一致。
3. **route 复制新版本时一并复制 ESOP 明细与附件子行**，`route_op_id`、`esop_id` 正确重指向新记录。否则每次改版需重挂全部文档。
4. 提供按 `route_op_id` 批量查询接口。MES 工序任务持有 `route_op_id`，直接反查，不做快照落库。
5. `content`、`param_json`、`files` 三者不可同时为空，校验下沉到 Service 层。

### 4.2 缺陷代码库

**新增 `mdm_defect_code`**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | varchar(64) | 缺陷编码 |
| `name` | varchar(128) | 缺陷名称 |
| `level` | varchar(32) | `CRITICAL` / `MAJOR` / `MINOR` |
| `parent_id` | bigint | 缺陷分类树父级 |
| `status` | tinyint | 状态：0-启用，1-停用 |

**新增 `mdm_defect_code_op`、`mdm_defect_code_mat_cat`（适用范围多对多）**

一种缺陷可适用多道工序、多个物料分类（如"外观划伤"适用打磨、喷涂、总装），故适用范围建成关联表而非主表单值字段；无关联行即表示不限。两表均为 `defect_code_id` + 关联键 + 审计字段，并建唯一索引防重复。

`mes_defect` 现有 `defect_code`/`defect_name`/`defect_level` 为自由填写，无法做缺陷帕累托分析。MES 侧改为从主档选取并快照编码名称。

### 4.3 班组与技能

`mes_dispatch_task` 已有 `team_id`、`operator_id`，但无主数据，派工无法校验资质。

| 新增表 | 字段要点 |
| --- | --- |
| `mdm_team` | `code`、`name`、`workshop_id`、`shift_id`、`leader_user_id`、`status` |
| `mdm_team_member` | `team_id`、`user_id`（关联 system 用户）、`status` |
| `mdm_skill` | `code`、`name`、`level_desc`（等级体系说明）、`status` |
| `mdm_op_skill_req` | `op_id`、`skill_id`、`min_level`、`status` |
| `mdm_member_skill` | `user_id`、`skill_id`、`level`、`effective_date`、`expiry_date`、`status` |

设计决策：

- 技能要求挂 `op_id`（标准工序）而非 `route_op_id`，避免每条路线重复维护。
- 不新建人员主档，直接引用 system 用户 `user_id`，MDM 不依赖 HRM。
- `mdm_member_skill` 带有效期，派工时校验资质有效性（证书到期即失效）。

### 4.4 工装模具刀具

| 新增表 | 字段要点 |
| --- | --- |
| `mdm_tool` | `code`、`name`、`tool_type`（工装/模具/刀具/量具）、`wc_id`、`qty_total` 保有数量、`life_type`（次数/时长）、`life_limit`、`status` |
| `mdm_route_op_tool` | `route_op_id`、`tool_id`、`qty`、`required_flag` 是否必备 |

用途：开工门禁校验项 + APS 辅助资源约束（同一工装被多工序争用需串行）。

### 4.5 换型矩阵

**新增 `mdm_res_changeover`**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `res_id` | bigint | 资源编号，0 表示按资源组生效 |
| `res_group` | varchar(64) | 资源组 |
| `from_group` | varchar(64) | 换出换型分组 |
| `to_group` | varchar(64) | 换入换型分组 |
| `changeover_minutes` | int | 换型分钟数 |
| `status` | tinyint | 状态：0-启用，1-停用 |

**改 `mdm_mat`**：新增 `changeover_group`（换型分组，如颜色/材质/型号族），作为换型分组来源。

用途：APS 顺序相关准备时间（sequence-dependent setup）优化的唯一数据源。`aps_strategy.config_json` 的换型权重配置需要它才有意义；缺它排程只能按固定 setup 处理，换型损失无法优化。

---

## 5. 配套改动

### 5.1 编码规则常量

`MdmCodeRuleConstants` 现有 13 项，补充 5 项，命名风格保持一致：

```java
String CALENDAR = "MDM_CALENDAR_CODE";
String DEFECT_CODE = "MDM_DEFECT_CODE";
String TEAM = "MDM_TEAM_CODE";
String SKILL = "MDM_SKILL_CODE";
String TOOL = "MDM_TOOL_CODE";
```

### 5.2 字典项

新增字典：`mdm_duration_type`、`mdm_issue_method`、`mdm_plan_strategy`、`mdm_abc_class`、`mdm_calendar_exception_type`、`mdm_esop_file_type`、`mdm_defect_level`、`mdm_tool_type`、`mdm_life_type`。布尔字段统一复用系统既有 `infra_boolean_string`，不新建。

### 5.3 代码生成与数据访问

1. 新增表优先使用 infra 代码生成器；生成前在"字段管理"中人工维护外键列的"关联表名"和"显示字段"，由生成器产出 XML JOIN 查询和列表关联展示列。
2. 关联数据查询分两种边界：**同模块内**（`mdm_*` 表之间）一律在 Mapper 层用 SQL `JOIN` 一次取回可读字段，禁止先查主表再在应用层拼接外键数据；**跨模块**（例如 `system_users`）禁止跨库 `JOIN`，一律在 Service 层调用对方模块的 `XxxApi` 门面批量补全，DO 和 Mapper 不得跨模块暴露，为将来微服务拆分保留边界。
3. 数据库操作全程走 dbx MCP，连接名 `芋道-dev`，库 `ruoyi-vue-pro-20260817`，不向 `sql/` 目录新增脚本。

### 5.4 菜单与权限

新增模块在 MDM 菜单下按现有层级挂载，权限标识沿用 `mdm:<业务>:query|create|update|delete|export`。页面未完成前菜单保持隐藏。

### 5.5 前端页面

在 `iboot-ui/iboot-ui-admin-vben/apps/web-ele` 下按 `dev-docs/通用CRUD开发流程.md` 页面结构实现。ESOP、日历规则、资源能力、工序用料为主子表编辑，采用与现有 route 详情页一致的父子联动结构。

### 5.6 APS 投影契约（仅记录，随 APS 模块实现）

- `aps_projection_mat` 补 `lead_time_days`、`safety_stock`、`plan_strategy` 投影字段。
- `aps_projection_resource.capability_json` 明确由 `mdm_res_capability` 序列化填充。

---

## 6. 假设与默认取值

1. 数据库操作全程走 dbx MCP（连接名 `芋道-dev`，库 `ruoyi-vue-pro-20260817`），不额外维护 sql 脚本文件。
2. 当前为单工厂单库存组织场景，`plant_id`/`workshop_id`/`project_id` 等维度字段建齐但不实现跨工厂业务逻辑。
3. 人员不新建 MDM 主档，`mdm_team_member`、`mdm_member_skill` 直接引用 system 用户 `user_id`；`iboot-module-hrm` 员工档案本期不打通，避免 MDM 依赖 HRM。
4. SN 沿用架构文档结论，由 `wms_batch`（数量为 1 的批次）承载，MDM 不建独立 SN 模型。
5. 工时单位统一为分钟；重量单位由 `weight_uom_id` 指定；尺寸统一毫米、体积统一立方毫米，与 `wms_loc`、`wms_container_type` 现有字段一致。
6. `mdm_op.std_duration` 保留不删，避免与已生成的前端页面和 `RouteServiceImpl` 现有校验冲突；新语义由 `duration_type` 显式区分。
7. 本期不做（沿用架构文档"明确不建设"与"演进"定档）：DNC 程序管理、设备点检与保养计划（属 EAM 范畴）、SPC/8D/FMEA/MSA、批次编码规则配置界面、计件工资。

---

## 7. 测试计划

后端沿用 JUnit 5 + Mockito，DB 相关测试继承 `BaseDbUnitTest`，测试包结构与生产代码一致。

### 7.1 P0 用例

| 主题 | 用例 |
| --- | --- |
| 工时模型 | `mdm_route_op` override 为空时继承 `mdm_op`；非空时覆盖；`duration_type = FIXED` 时时长不随数量变化 |
| 资源能力 | 同一 `res_id + op_id` 唯一；同一 `op_id` 下允许多资源但至少一个可用；停用资源不进入能力候选集 |
| 日历生成 | 按模板生成指定日期区间幂等（重复执行不产生重复行）；`mdm_calendar_exception` 覆盖 `mdm_calendar_rule`；`generated_flag = false` 的人工调整行重生成时不被覆盖 |
| BOM 用量 | `scrap_rate` 参与需求量计算；`PHANTOM` 不产生需求直接向下展开；`BACKFLUSH` 不计入齐套需求 |
| 物料属性 | 储运字段单位与 `wms_loc` 校验一致（毫米）；`plan_strategy = MTS` 时 `safety_stock` 必填 |

### 7.2 P1 用例

| 主题 | 用例 |
| --- | --- |
| ESOP 版本冻结 | route 状态为启用时，新增/修改/删除 ESOP 明细均抛 `ROUTE_ENABLED_LOCKED` |
| ESOP 版本复制 | route 复制新版本时 ESOP 明细一并复制，且 `route_op_id` 正确重指向新版本工序 |
| 技能校验 | `mdm_member_skill` 过期后不满足 `mdm_op_skill_req`；班组成员引用的 `user_id` 必须存在 |
| 换型矩阵 | 同一 `res_id + from_group + to_group` 唯一；`from_group` 等于 `to_group` 时换型时间为 0 |
| 删除保护 | 被 `mdm_route_op` 引用的 `mdm_op`、被 `mdm_res_capability` 引用的 `mdm_res`、被 `mdm_route_op_tool` 引用的 `mdm_tool` 不允许删除 |

### 7.3 验证命令

```bash
mvn -pl iboot-module-mdm -am test
```

前端：在 `iboot-ui/iboot-ui-admin-vben` 执行 `pnpm lint`。

---

## 8. 审核清单（交付前自检）

实施完成后按以下清单逐项自查，审核方将按同一清单验收：

### 8.1 数据库

- [ ] 所有新增表命名符合 `mdm_<业务>`，字段下划线命名
- [ ] 唯一索引均包含 `deleted`
- [ ] 审计字段齐全：`creator`、`create_time`、`updater`、`update_time`、`deleted`
- [ ] 已用 dbx 回查表结构与 `information_schema.statistics` 确认主键和唯一索引
- [ ] 未向 `sql/` 目录新增本次脚本
- [ ] 尺寸/重量单位与 `wms_loc`、`wms_container_type` 一致

### 8.2 后端

- [ ] 目录分层符合 `dev-docs/通用CRUD开发流程.md` 约定
- [ ] 同模块关联查询在 Mapper 层用 JOIN；跨模块无跨库 JOIN，走 `XxxApi` 门面在 Service 层批量补全
- [ ] Convert 使用 MapStruct，Controller 无手工逐字段复制
- [ ] 业务校验在 Service 层，Controller 不写业务判断
- [ ] 错误码集中在 `ErrorCodeConstants`
- [ ] 编码规则常量已补 5 项
- [ ] ESOP 写入受 route 启用锁保护
- [ ] route 版本复制已一并复制 ESOP 明细
- [ ] 日历生成接口幂等，人工调整行不被覆盖
- [ ] `mdm_op.std_duration` 未被删除

### 8.3 前端与权限

- [ ] 页面在 `apps/web-ele` 下，`component` 路径与 `system_menu` 一致
- [ ] 权限标识为 `mdm:<业务>:query|create|update|delete|export`
- [ ] 角色授权已写入并回查
- [ ] 主子表页面父子联动与现有 route 详情页结构一致
- [ ] 字典项已建齐，前端下拉取自字典而非硬编码

### 8.4 测试

- [ ] `mvn -pl iboot-module-mdm -am test` 通过
- [ ] `pnpm lint` 通过
- [ ] 第 7 节全部用例已覆盖
- [ ] 测试包结构与生产代码一致，DB 测试继承 `BaseDbUnitTest`

---

## 9. 优先级判断说明

- **工时模型与资源能力（3.1、3.2）在关键路径上**：缺它 APS 完全无法建模，是 P0 中的 P0。
- **ESOP（4.1）是现场准确性问题**，不阻塞排程，但因挂载点与 route 版本冻结机制耦合，后补代价高，故本期一并做。
- 其余项按 P0 → P1 顺序推进，P1 各项之间无依赖，可并行。

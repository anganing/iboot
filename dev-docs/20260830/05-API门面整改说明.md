# 跨模块 API 门面整改说明

- 版本：v1.0.0
- 日期：2026-08-30
- 状态：待整改
- 依据：`dev-docs/20260830/00-跨模块API门面开发总览.md` 及 01～04 四份模块文档
- 验收基线：`mvn -pl iboot-module-mdm,iboot-module-wms,iboot-module-mes,iboot-module-aps test` 全绿，且下文每条整改项均可复核

---

## 0. 总体结论

| 模块 | 结论 | 主要问题 | 整改工作量 |
| --- | --- | --- | --- |
| MDM | 达标，无需整改 | 无 | 0 |
| WMS | 基本达标 | MDM 依赖零引用、API 层无测试、pom 缩进损坏 | 小 |
| MES | 严重未达标，退回重做 | Service 为空实现、无 dal 层、DTO 退化为万能对象、未调用 WMS | 大 |
| APS | 部分达标 | 两处任务校验未实现、无 DB 测试、代码风格违规 | 中 |

当前测试分布：MDM 101 个、WMS 34 个、MES 3 个、APS 5 个。MES 与 APS 的用例数量本身即说明覆盖不足。

分派建议：MES 单独一个 AI 重做；WMS、APS 各一个 AI 整改；MDM 不动。三者互不冲突，可并行。

已知无关失败：`mvn -am` 全量构建时 `iboot-module-infra` 有 42 个 `CodegenEngine*Test` 失败，报的是代码生成器模板中 `com.iboot.pf` 目录路径断言，属既有问题，本次不处理，也不要顺手改。

---

## 1. MES 模块整改（P0，退回重做）

### 1.1 问题根因

`MesApiServiceImpl` 是空类，`MesApiService` 接口的所有业务方法都是 default 实现并抛 `UnsupportedOperationException("MES xxx 业务 Service 尚未接入领域表")`。10 个 API 门面只有签名，没有任何落库逻辑。整个模块无 `dal` 包，`mes_*` 20 张表一张都没接。

连带后果：APS 的 `refreshOrderProjection` 因为拿不到 `OrderProjectionSource` 实现，永远抛 `PROJECTION_SOURCE_UNAVAILABLE`。

### 1.2 必须删除的临时结构

以下三个类是绕过契约的临时产物，重做时删除，不要保留兼容：

```text
iboot-module-mes/src/main/java/com/iboot/pf/module/mes/service/MesApiService.java
iboot-module-mes/src/main/java/com/iboot/pf/module/mes/service/MesApiServiceImpl.java
iboot-module-mes/src/main/java/com/iboot/pf/module/mes/api/dto/MesRespDTO.java
iboot-module-mes/src/main/java/com/iboot/pf/module/mes/api/dto/MesCommandDTOs.java
```

保留 `MesCommandReqDTO`（幂等基类字段正确）和 `MesBusinessRules`（状态机、报工数量、损耗率公式实现正确，但目前零调用方，重做时必须由领域 Service 调用）。

### 1.3 DTO 契约必须按域拆分

当前 `ProdOrderApi.getProdOrder` 返回万能 `MesRespDTO`（只有 id/code/name/status/prodOrderId/matId/qty/occurredTime），调用方拿不到 `bomVersion`、`routeVersion`，"工单下达锁定 BOM 与工艺版本"这条核心契约在门面上直接消失。按域建独立 DTO：

| 域 | 响应 DTO | 必须包含的关键字段 |
| --- | --- | --- |
| 生产需求 | `ProdDemandRespDTO` | `id`、`demandNo`、`sourceType`、`matId`、`matCode`、`qty`、`dueDate`、`priority`、`projectId`、`status` |
| 生产工单 | `ProdOrderRespDTO` | `id`、`orderNo`、`demandId`、`matId`、`matCode`、`matSpec`、`qty`、`completedQty`、`scrapQty`、`uomId`、`bomId`、`bomVersion`、`routeId`、`routeVersion`、`dueDate`、`priority`、`kitStatus`、`status`、`releaseTime` |
| 作业工单 | `WorkOrderRespDTO` | `id`、`prodOrderId`、`workshopId`、`wcId`、`status` |
| 工序任务 | `OpTaskRespDTO` | `id`、`taskNo`、`workOrderId`、`routeOpId`、`opId`、`opCode`、`opName`、`seq`、`parallelGroup`、`execMode`、`qcMode`、`keyFlag`、`plannedQty`、`completedQty`、`scrapQty`、`resId`、`plannedStartTime`、`plannedEndTime`、`actualStartTime`、`actualEndTime`、`status` |
| 齐套 | `KitCheckResultDTO`、`KitSnapshotRespDTO` | `prodOrderId`、`matId`、`reqQty`、`availQty`、`shortQty`、`checkTime`、`kitStatus` |
| 领料 | `MaterialReqRespDTO` | `id`、`reqNo`、`prodOrderId`、`opTaskId`、`reqType`、`status`、`wmsOrderNo` |
| 报工 | `ProdReportRespDTO`、`WipTxnRespDTO` | `opTaskId`、`workerId`、`resId`、`qtyGood`、`qtyScrap`、`startTime`、`endTime` |
| 返工 | `ReworkRespDTO` | `id`、`sourceQcTaskId`、`prodOrderId`、`opTaskId`、`qty`、`status` |
| 质量 | `QualityTaskRespDTO`、`QualityResultRespDTO` | `id`、`taskNo`、`triggerPoint`、`bizType`、`bizNo`、`matId`、`batchId`、`qty`、`status`、`conclusion` |
| 缺陷 | `DefectRespDTO` | `id`、`taskId`、`defectCodeId`、`qty`、`description` |

命令 DTO 同样按域拆：`CreateProdOrderReqDTO`、`ReleaseProdOrderReqDTO`、`CloseProdOrderReqDTO`、`ChangeOpTaskStatusReqDTO`、`FinishOpTaskReqDTO`、`CreateKitSnapshotReqDTO`、`CreateMaterialReqReqDTO`、`ConfirmMaterialReqReqDTO`、`SubmitQualityResultReqDTO`、`CreateDispositionReqDTO` 等，全部继承 `MesCommandReqDTO`。

### 1.4 必须落地的 dal 层

按下表建 DO 与 Mapper，包路径 `com.iboot.pf.module.mes.dal.dataobject.<域>` 与 `com.iboot.pf.module.mes.dal.mysql.<域>`。表结构已在库中，用 dbx 查列定义，不要自己造字段。

| 表 | 列数 | 域 |
| --- | --- | --- |
| `mes_prod_demand` | 24 | demand |
| `mes_prod_order` | 34 | order |
| `mes_work_order` | 24 | workorder |
| `mes_op_task` | 32 | optask |
| `mes_kit_snapshot` | 24 | kit |
| `mes_material_req` / `mes_material_req_line` | 20 / 22 | materialreq |
| `mes_prod_report` | 22 | report |
| `mes_wip_txn` | 21 | report |
| `mes_rework` | 16 | rework |
| `mes_qc_rule` / `mes_qc_plan` / `mes_qc_item` / `mes_qc_task` / `mes_qc_result` | 20 / 15 / 18 / 34 / 19 | quality |
| `mes_disposition` | 17 | quality |
| `mes_defect` | 16 | defect |

`mes_op_skip_record`、`mes_dispatch_task`、`mes_rgv_task` 本轮不接。

### 1.5 幂等

库中已建唯一索引，直接用，不要新建命令记录表：

```text
uk_mes_prod_demand_idem_deleted   (mes_prod_demand.idempotency_key)
uk_mes_material_req_idem_deleted  (mes_material_req.idempotency_key)
uk_mes_prod_report_idem_deleted   (mes_prod_report.idempotency_key)
uk_mes_qc_task_idem_deleted       (mes_qc_task.idempotency_key)
uk_mes_disposition_idem_deleted   (mes_disposition.idempotency_key)
uk_mes_wip_txn_idem_deleted       (mes_wip_txn.idempotency_key)
```

`mes_prod_order` 无幂等列，工单创建的幂等按 `demand_id` + `order_no` 判重，或用 dbx 补一列 `idempotency_key` 并建 `uk_mes_prod_order_idem_deleted (idempotency_key, deleted)`，二者选一并在实现里注明理由。

### 1.6 必须实现的跨模块调用

`iboot-module-mes/pom.xml` 已声明 `iboot-module-wms` 依赖，但 `rg "com.iboot.pf.module.wms" iboot-module-mes/src` 结果为 0，属于加了用不上的依赖。必须补齐：

1. `MaterialReqApi.confirmMaterialReq`：领料确认调 `wms.api.outorder.OutOrderApi#createOutOrder`；退料确认调 `wms.api.inorder.InOrderApi#createInOrder`。WMS 返回的单号写回 `mes_material_req`，形成双向追溯。
2. `KitSnapshotApi.checkKitSnapshot`：可用量走 `wms.api.inventory.InventoryApi#getAvailableQtyMap`，需求量走 `mdm.api.bom.BomApi#getBomLineList` + `MesBusinessRules.requiredQty`。`PHANTOM` 虚拟件不产生需求继续向下展开，`BACKFLUSH` 倒冲件不计入齐套需求。
3. 齐套占用库存走 `wms.api.reservation.InventoryReservationApi#reserveInventory`，快照本身不占库存。
4. `ProdOrderApi.createProdOrder`：物料校验走 `mdm.api.material.MaterialApi#validateMaterialList`。
5. `ProdOrderApi.releaseProdOrder`：BOM 与工艺版本走 `mdm.api.bom.BomApi#getEffectiveBom`、`mdm.api.route.RouteApi#getEffectiveRoute` 取，落库后不允许再改。

调用顺序固定为：先落本模块命令记录，再调对方 API，结果回写。WMS 调用失败不吞异常，本模块状态不前移。

### 1.7 状态机

`MesBusinessRules.nextOpTaskStatus` 目前只覆盖 `PENDING`、`RUNNING`、`PAUSED`，但 `mes_op_task.status` 的实际取值是 `PENDING、READY、RUNNING、PAUSED、QC_WAITING、HOLD、DONE、SKIPPED、CANCELLED`。补齐迁移表并与库字段注释对齐，同时补 `mes_prod_order.status` 的 `DRAFT、RELEASED、READY、RUNNING、HOLD、DONE、CLOSED、CANCELLED` 状态机。状态定义集中放 `enums` 包，禁止散落魔法字符串。

`start` 前必须执行齐套、资源、工装、技能、前置工序校验；`finish` 必须校验合格数 + 报废数 = 计划数（复用 `MesBusinessRules.validateFinishQty`）；状态推进写 `mes_wip_txn`。

### 1.8 测试要求

当前仅 `MesBusinessRulesTest` 3 个用例，测的是静态工具类。补齐 `src/test/resources/sql/create_tables.sql` 与 `application-unit-test.yaml`（参照 `iboot-module-wms/src/test/resources`），DB 测试继承 `BaseDbUnitTest`，覆盖：

| 主题 | 用例 |
| --- | --- |
| 工单下达 | 锁定 `bom_id/bom_version/route_id/route_version`，锁定后不可改 |
| 工单关闭 | 存在未完成工序任务或未关闭质检任务时拒绝 |
| 工序任务 | 全部合法迁移逐条通过，非法迁移抛 `OP_TASK_STATUS_INVALID` |
| 报工 | 合格 + 报废 = 计划数量，否则抛 `OP_TASK_REPORT_QTY_INVALID`；写 `mes_wip_txn` |
| 齐套 | 损耗率参与计算；`PHANTOM` 不产生需求；`BACKFLUSH` 不计入 |
| 领料 | 确认后调 WMS 创建出库单，单号回写 |
| 质检 | 合格 + 不合格 = 抽检数量，否则抛 `QUALITY_QTY_INVALID` |
| 处置 | 返工生成 `mes_rework`；报废、冻结联动 WMS |
| 幂等 | 每个命令重复提交返回原结果，不产生第二条记录 |

### 1.9 MES 交付清单

- [ ] 删除 `MesApiService`、`MesApiServiceImpl`、`MesRespDTO`、`MesCommandDTOs`
- [ ] 10 个 API 门面全部有真实 Service 实现，无 `UnsupportedOperationException`
- [ ] `dal.dataobject` 与 `dal.mysql` 覆盖 1.4 节全部表
- [ ] DTO 按域拆分，`ProdOrderRespDTO` 含 `bomVersion`、`routeVersion`
- [ ] `MesBusinessRules` 被领域 Service 实际调用
- [ ] `mdm.api` 与 `wms.api` 均有实际引用
- [ ] 每个命令走库中已有的 `idempotency_key` 唯一索引
- [ ] 1.8 节全部用例通过
- [ ] `mvn -pl iboot-module-mes -am test` 通过

---

## 2. APS 模块整改（P1）

### 2.1 已达标部分，不要动

版本状态机、发布与替代同事务、`aps_command_record` 幂等表（唯一索引 `uk_aps_command_idempotency_deleted` 已建）、投影刷新调 MDM/WMS API、投影表按 `scenarioId` 物理删除后重建，这些实现是对的，保留。

### 2.2 两处校验未实现

`ErrorCodeConstants` 里定义了两个错误码，但 `rg` 全模块只匹配到定义处，零引用：

```java
ErrorCode TASK_TIME_INVALID    = new ErrorCode(1_065_003_001, "任务开始时间必须早于结束时间");
ErrorCode TASK_RESOURCE_OVERLAP = new ErrorCode(1_065_003_002, "同一资源上的任务时间不允许重叠");
```

`ApsScheduleTaskServiceImpl` 只做了锁定与解锁。补齐：任务落库或更新时校验 `startTime < endTime`；同一 `resId` 上的任务时间区间不允许重叠。校验放 Service 层，不放 API 实现类。

### 2.3 缺 DB 测试

当前 5 个用例：`ApiContractTest`（反射检查返回类型是否含内部包名）+ `VersionStatusEnumTest`，全是纯内存断言，`src/test/resources` 目录不存在。补齐测试资源目录后覆盖：

| 主题 | 用例 |
| --- | --- |
| 场景 | 创建记录 `snapshotTime`；关闭后再生成版本抛 `SCENARIO_CLOSED` |
| 版本 | 合法迁移逐条通过，非法迁移抛 `VERSION_STATUS_INVALID` |
| 发布 | 同一场景发布新版本后，旧 `PUBLISHED` 自动变 `REPLACED`，且同时只有一个 `PUBLISHED` |
| 已发布只读 | 对 `PUBLISHED` 版本的任务改动抛 `VERSION_PUBLISHED_READ_ONLY` |
| 任务 | `startTime >= endTime` 抛 `TASK_TIME_INVALID`；同资源重叠抛 `TASK_RESOURCE_OVERLAP` |
| 投影 | 物料、库存刷新分别命中 MDM、WMS API；刷新失败整体回滚不留半条数据 |
| 缺料 | `shortQty = reqQty - availQty` 且不为负；状态在 `OPEN/PARTIAL/RESOLVED` 间正确切换 |
| 负荷 | 占用分钟超出资源日历可用分钟抛 `LOAD_OVER_CAPACITY` |
| 幂等 | 每个命令重复提交不产生第二条 `aps_command_record` |

### 2.4 代码风格违规

违反《阿里巴巴 Java 开发手册》与仓库约定，需要整理：

1. 多方法挤在一行：`ProjectionApiImpl` 整个类 8 行、6 个方法一行一个；`LoadApiImpl` 全部逻辑塞 4 行；`ApsLoadServiceImpl` 51 行写完全部逻辑；`ApsVersionServiceImpl` 94 行含 12 个方法。一律拆成正常缩进的多行。
2. 通配 import：全模块 39 处 `import xxx.*`，改为显式 import。
3. `@Override` 普遍省略，API 实现类里的重写方法全部补上。
4. 字段声明挤在一行：`ApsTaskDO` 把 4 个字段写在同一行，拆开并补 Javadoc。

### 2.5 APS 交付清单

- [ ] `TASK_TIME_INVALID` 与 `TASK_RESOURCE_OVERLAP` 均有实际校验逻辑
- [ ] `src/test/resources` 补齐，DB 测试继承 `BaseDbUnitTest`
- [ ] 2.3 节全部用例通过
- [ ] 无一行多方法、无通配 import、`@Override` 齐全
- [ ] `mvn -pl iboot-module-aps -am test` 通过
- [ ] 未引入 Timefold，未暴露求解器类型

---

## 3. WMS 模块整改（P1）

### 3.1 已达标部分，不要动

库存过账幂等、余额乐观锁 `version`、余额只能经 `WmsInventoryService#post` 修改、批次在创建阶段注册并回填 `batchId`、出库前用 `getAvailableQty` 拦截，这些实现是对的，34 个 Service 测试全绿，保留。

### 3.2 MDM 依赖零引用

`iboot-module-wms/pom.xml` 已加 `iboot-module-mdm`，但 `rg "com.iboot.pf.module.mdm" iboot-module-wms/src` 结果为 0。要么用起来，要么去掉依赖，二者选一。建议用起来：

1. `WmsBatchServiceImpl#registerBatch`：`matId` 走 `mdm.api.material.MaterialApi#validateMaterialList` 校验，批次属性冗余快照的 `matCode`、`matName` 从 `getMaterialMap` 批量取，不再依赖调用方传入。
2. `WmsInOrderServiceImpl` / `WmsOutOrderServiceImpl` 创建单据时，`matId`、`uomId` 走 MDM 门面批量校验，一次调用完成，不要在循环里单查。
3. 仓储组织字段 `invOrgId`、`plantId` 走 `mdm.api.org.OrgApi` 校验存在性。

补相应错误码到 `1-063` 段。

### 3.3 API 层无测试

`src/test/java/com/iboot/pf/module/wms/api` 是空目录，10 个 API 门面一个测试都没有。补齐覆盖：

| 主题 | 用例 |
| --- | --- |
| DTO 转换 | `InventoryQueryReqDTO` → `WmsInventoryQueryBO` 全字段映射不丢项 |
| 可用量填充 | `InventoryApiImpl#fillAvailable` 正确算出 `qtyAvailable = qtyOnhand - qtyReserved - qtyFrozen` |
| 批量查询 | 传入空集合返回空集合，不抛 NPE |
| `getXxxMap` | 键为 ID，缺失 ID 不产生键 |
| 命令校验 | 缺 `idempotencyKey` 时 `@Validated` 拦截 |
| 幂等透传 | 重复命令经 API 层返回原 ID |

### 3.4 pom 缩进损坏

`iboot-module-wms/pom.xml` 结尾的 `</dependencies>` 被改成顶格，恢复为四空格缩进。

### 3.5 测试命名与断言相反

`WmsInventoryServiceImplTest#testOut_availableNotEnough_rejected` 方法名说"被拒绝"，方法体里 `post` 实际执行成功并断言余额被扣到 40。注释解释了原因（可用量由出库单 Service 拦截，过账层只校验在库量），但方法名会误导后来人。改名为 `testOut_availableNotEnough_postStillSucceeds` 或类似能反映真实行为的名字。

### 3.6 WMS 交付清单

- [ ] `mdm.api` 有实际引用，或移除该 Maven 依赖
- [ ] `api` 包测试覆盖 3.3 节全部主题
- [ ] `pom.xml` 缩进恢复
- [ ] 误导性测试方法名已改
- [ ] `mvn -pl iboot-module-wms -am test` 通过
- [ ] 未新增 MES / APS 依赖

---

## 4. 工作区清理

以下未跟踪文件看不出交付意图，确认后处理，不要提交：

```text
CLAUDE.md      # 内容是 AGENTS.md 的副本
.workbuddy/    # 工具产生的目录
```

---

## 5. 通用红线（三个模块都适用）

1. API 只暴露 DTO，不暴露 DO、VO、Mapper、Service、内部枚举。
2. 依赖方向单向：MDM ← WMS ← MES ← APS，禁止反向 Maven 依赖。
3. 跨模块取数走对方 `XxxApi` 批量方法，一次 SQL，不允许 N+1，不允许跨库 JOIN。
4. 查询方法加 `@DataPermission(enable = false)`，命令实现类加 `@Validated`。
5. 命令幂等键格式 `sourceModule:sourceBizNo:action:sourceBizLineNo`，命中时返回原结果不抛异常。
6. 库存数量只能由 WMS 过账修改，工单状态只能由 MES 推进，APS 只读不反写。
7. 错误码各用自己的段：MDM `1-062`、WMS `1-063`、MES `1-064`、APS `1-065`，不跨模块引用对方 `ErrorCodeConstants`。
8. 数据库操作走 dbx MCP，连接 `芋道-dev`，库 `ruoyi-vue-pro-20260817`，不向 `sql/` 目录新增脚本。
9. 本轮不新增 HTTP Controller，不新增前端页面。
10. 遇到现有 Service 方法不够用，在本模块内补 Service 方法，不要把 Mapper 或 DO 跨模块暴露出去。


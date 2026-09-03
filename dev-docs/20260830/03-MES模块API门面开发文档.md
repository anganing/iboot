# MES 模块 API 门面开发文档

- 版本：v1.0.0
- 日期：2026-08-30
- 状态：待实施
- 模块：`iboot-module-mes`
- 定位：生产执行与质量事实源，向 APS / WMS / Integration / UI 提供执行与质量门面

---

## 1. 范围

### 1.1 本轮要做

1. 新增 `com.iboot.pf.module.mes.api` 包。
2. 建设 10 个 API 门面：生产需求、工单、作业工单、工序任务、齐套、领料、报工、返工、质量、缺陷。
3. 建 Service / Mapper / DO 骨架时，接口方法与 API 契约同步落地。
4. 命令类方法按幂等键实现。
5. 为 API 层和 Service 层核心规则编写测试。

### 1.2 本轮不做

1. 不做完整 PDA / H5 页面。
2. 不做完整质检方案配置界面。
3. 不做设备与 DNC 集成。
4. 不做排程求解，只提供 APS 所需事实。
5. 不依赖 APS。

## 2. Maven 依赖

新增依赖：

```xml
<dependency>
    <groupId>com.iboot</groupId>
    <artifactId>iboot-module-mdm</artifactId>
    <version>${revision}</version>
</dependency>
<dependency>
    <groupId>com.iboot</groupId>
    <artifactId>iboot-module-wms</artifactId>
    <version>${revision}</version>
</dependency>
```

禁止新增：

```xml
iboot-module-aps
```

## 3. 包结构

```text
com.iboot.pf.module.mes.api
├── package-info.java
├── demand
│   ├── ProdDemandApi.java
│   ├── ProdDemandApiImpl.java
│   └── dto
├── order
│   ├── ProdOrderApi.java
│   ├── ProdOrderApiImpl.java
│   └── dto
├── workorder
│   ├── WorkOrderApi.java
│   ├── WorkOrderApiImpl.java
│   └── dto
├── optask
│   ├── OpTaskApi.java
│   ├── OpTaskApiImpl.java
│   └── dto
├── kit
│   ├── KitSnapshotApi.java
│   ├── KitSnapshotApiImpl.java
│   └── dto
├── materialreq
│   ├── MaterialReqApi.java
│   ├── MaterialReqApiImpl.java
│   └── dto
├── report
│   ├── ProdReportApi.java
│   ├── ProdReportApiImpl.java
│   └── dto
├── rework
│   ├── ReworkApi.java
│   ├── ReworkApiImpl.java
│   └── dto
├── quality
│   ├── QualityApi.java
│   ├── QualityApiImpl.java
│   └── dto
└── defect
    ├── DefectApi.java
    ├── DefectApiImpl.java
    └── dto
```

## 4. API 契约清单

| API | 核心能力 | 主要消费者 |
| --- | --- | --- |
| `ProdDemandApi` | 需求创建、查询、取消 | APS、Integration、UI |
| `ProdOrderApi` | 工单创建、下达、关闭、查询 | APS、Integration、UI |
| `WorkOrderApi` | 作业工单查询 | APS、UI |
| `OpTaskApi` | 工序任务生成、状态推进、查询 | APS、Integration、UI |
| `KitSnapshotApi` | 齐套检查、快照查询 | APS、UI |
| `MaterialReqApi` | 领退补料申请、状态查询 | WMS、APS、UI |
| `ProdReportApi` | 报工、在制流水查询 | WMS、APS、UI |
| `ReworkApi` | 返工任务创建与查询 | WMS、APS、UI |
| `QualityApi` | 质检任务、结果、处置 | WMS、APS、UI |
| `DefectApi` | 缺陷记录查询 | APS、UI |

## 5. 关键接口设计

### 5.1 ProdOrderApi

```java
public interface ProdOrderApi {

    Long createProdOrder(CreateProdOrderReqDTO reqDTO);

    void releaseProdOrder(ReleaseProdOrderReqDTO reqDTO);

    void closeProdOrder(CloseProdOrderReqDTO reqDTO);

    ProdOrderRespDTO getProdOrder(Long id);

    List<ProdOrderRespDTO> getProdOrderList(Collection<Long> ids);
}
```

核心规则：

1. 创建工单必须校验物料存在。
2. 下达工单时锁定 BOM 与工艺路线版本，锁定的 `bom_id / bom_version / route_id / route_version` 不允许再改。
3. 下达时生成作业工单与工序任务。
4. 关闭前必须校验完工数量、未完成任务与未关闭质检任务。
5. 所有命令必须幂等。

### 5.2 OpTaskApi

```java
public interface OpTaskApi {

    List<OpTaskRespDTO> getOpTaskListByOrderIds(Collection<Long> prodOrderIds);

    List<OpTaskRespDTO> getOpTaskListByRouteOpIds(Collection<Long> routeOpIds);

    void startOpTask(ChangeOpTaskStatusReqDTO reqDTO);

    void pauseOpTask(ChangeOpTaskStatusReqDTO reqDTO);

    void finishOpTask(FinishOpTaskReqDTO reqDTO);

    void skipOpTask(ChangeOpTaskStatusReqDTO reqDTO);
}
```

状态机：

| 当前状态 | 允许动作 | 目标状态 |
| --- | --- | --- |
| `PENDING` | `start` | `RUNNING` |
| `PENDING` | `skip` | `SKIPPED` |
| `RUNNING` | `pause` | `PAUSED` |
| `RUNNING` | `finish` | `DONE` |
| `PAUSED` | `start` | `RUNNING` |
| `PAUSED` | `finish` | `DONE` |
| `DONE` | 无 | 终态 |
| `SKIPPED` | 无 | 终态 |

要求：

1. `start` 前执行齐套、资源、工装、技能、前置工序校验。
2. `finish` 必须提交合格数、报废数，合格 + 报废 = 计划数量。
3. 状态迁移集中在 Service 层，禁止魔法数。
4. 状态推进写 `mes_wip_txn`。

### 5.3 KitSnapshotApi

```java
public interface KitSnapshotApi {

    KitCheckResultDTO checkKitSnapshot(CreateKitSnapshotReqDTO reqDTO);

    List<KitSnapshotRespDTO> getKitSnapshotList(Long prodOrderId);
}
```

计算规则：

```text
req_qty = parent_qty × qty ÷ (1 - scrap_rate)
```

处理规则：

1. `PHANTOM` 虚拟件不产生需求，继续向下展开。
2. `BACKFLUSH` 倒冲件不计入齐套需求。
3. 可用量从 WMS `InventoryApi` 获取。
4. 快照只记录结果，不占用库存。
5. 占用库存必须走 `InventoryReservationApi`。

### 5.4 MaterialReqApi

```java
public interface MaterialReqApi {

    Long createMaterialReq(CreateMaterialReqReqDTO reqDTO);

    void cancelMaterialReq(CancelMaterialReqReqDTO reqDTO);

    void confirmMaterialReq(ConfirmMaterialReqReqDTO reqDTO);
}
```

核心规则：

1. 领料申请确认后调用 WMS 创建出库单。
2. 退料申请确认后调用 WMS 创建入库单。
3. WMS 单据号写回申请记录，形成追溯。
4. 申请必须绑定工单和工序任务。
5. 所有命令必须幂等。

### 5.5 QualityApi

```java
public interface QualityApi {

    QualityTaskRespDTO createQualityTask(CreateQualityTaskReqDTO reqDTO);

    void submitQualityResult(SubmitQualityResultReqDTO reqDTO);

    void createDisposition(CreateDispositionReqDTO reqDTO);

    QualityTaskRespDTO getQualityTask(Long id);
}
```

核心规则：

1. 质检任务由业务动作触发，可来自到货、工序、完工、出库。
2. 检验结果合格数量 + 不合格数量 = 抽检数量。
3. 不合格处置必须生成处置单：返工、报废、让步、冻结。
4. 返工生成 `mes_rework`。
5. 报废、冻结联动 WMS 出库或冻结。
6. 质量状态是 WMS 库存维度字段，质量结论由 MES 维护。

## 6. 状态与事实边界

1. 工单状态只能由 MES 推进。
2. 库存数量只能由 WMS 过账修改。
3. MES 通过 WMS API 创建入库 / 出库单，不直接写库存表。
4. APS 只读 MES 工单、工序任务与报工数据，不反向推进状态。
5. MES 报工后可调用 WMS 完工入库，但入库事实以 WMS 单据与流水为准。

## 7. 幂等与事务

1. 命令表均建 `idempotency_key` 唯一索引。
2. 幂等键格式：`sourceModule:sourceBizNo:action:sourceBizLineNo`。
3. 状态推进与在制流水在同一事务内。
4. 跨模块调用 WMS 时，必须先落本模块命令记录，再调用 WMS，调用结果回写。
5. WMS 调用失败不吞异常，本模块状态不前移。

## 8. 测试计划

| 主题 | 用例 |
| --- | --- |
| 工单下达 | 锁定 BOM / 工艺版本 |
| 工序任务 | 状态机合法迁移与非法迁移拒绝 |
| 齐套 | 损耗率、虚拟件、倒冲件 |
| 领料 | 确认后调用 WMS 创建出库单 |
| 报工 | 合格 + 报废 = 计划数量 |
| 质检 | 合格 + 不合格 = 抽检数量 |
| 处置 | 返工、报废、让步、冻结联动 |
| 幂等 | 重复命令返回原结果 |

验证命令：

```bash
mvn -pl iboot-module-mes -am test
```

## 9. 交付清单

- [ ] `api` 包与 `package-info.java`
- [ ] 10 个 API 接口全部落地
- [ ] DTO 无 DO / VO 泄漏
- [ ] 工单版本锁定规则正确
- [ ] 工序任务状态机集中定义
- [ ] 跨模块调用只走 WMS API
- [ ] 命令 DTO 均有 `idempotencyKey`
- [ ] 单元测试通过
- [ ] 未新增 APS 依赖

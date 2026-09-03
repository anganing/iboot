# APS 模块 API 门面开发文档

- 版本：v1.0.0
- 日期：2026-08-30
- 状态：待实施
- 模块：`iboot-module-aps`
- 定位：只读消费 MDM / WMS / MES，向 UI / Integration 提供排程场景、版本与结果门面

---

## 1. 范围

### 1.1 本轮要做

1. 新增 `com.iboot.pf.module.aps.api` 包。
2. 建设 6 个 API 门面：场景、版本、任务、投影、缺料、负荷。
3. 建 Service / Mapper / DO 骨架时，接口方法与 API 契约同步落地。
4. 实现数据投影刷新骨架。
5. 命令类方法按幂等键实现。
6. 为 API 层和 Service 层核心规则编写测试。

### 1.2 本轮不做

1. 不接入 Timefold 求解器。
2. 不实现完整排程算法。
3. 不反向推进 MES 工单或 WMS 库存。
4. 不新增前端排程页面。
5. 不把 Timefold 类型暴露到模块外。

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
<dependency>
    <groupId>com.iboot</groupId>
    <artifactId>iboot-module-mes</artifactId>
    <version>${revision}</version>
</dependency>
```

APS 是顶层只读消费模块，允许依赖 MDM / WMS / MES；禁止任何核心模块反向依赖 APS。

## 3. 包结构

```text
com.iboot.pf.module.aps.api
├── package-info.java
├── scenario
│   ├── ScenarioApi.java
│   ├── ScenarioApiImpl.java
│   └── dto
├── version
│   ├── VersionApi.java
│   ├── VersionApiImpl.java
│   └── dto
├── task
│   ├── ScheduleTaskApi.java
│   ├── ScheduleTaskApiImpl.java
│   └── dto
├── projection
│   ├── ProjectionApi.java
│   ├── ProjectionApiImpl.java
│   └── dto
├── shortage
│   ├── ShortageApi.java
│   ├── ShortageApiImpl.java
│   └── dto
└── load
    ├── LoadApi.java
    ├── LoadApiImpl.java
    └── dto
```

## 4. API 契约清单

| API | 核心能力 | 主要消费者 |
| --- | --- | --- |
| `ScenarioApi` | 场景创建、复制、关闭、查询 | UI、Integration |
| `VersionApi` | 版本生成、确认、发布、替代、查询 | UI、MES、Integration |
| `ScheduleTaskApi` | 工序计划查询、锁定、解锁 | UI、MES |
| `ProjectionApi` | 物料 / 库存 / 工单投影刷新与查询 | UI、Integration |
| `ShortageApi` | 缺料清单查询与刷新 | UI、MES |
| `LoadApi` | 资源负荷查询与刷新 | UI、MES |

## 5. 关键接口设计

### 5.1 ScenarioApi

```java
public interface ScenarioApi {

    Long createScenario(CreateScenarioReqDTO reqDTO);

    ScenarioRespDTO getScenario(Long id);

    List<ScenarioRespDTO> getScenarioList(Collection<Long> ids);

    void closeScenario(CloseScenarioReqDTO reqDTO);
}
```

场景规则：

1. 场景是排程输入快照，创建时必须记录 `snapshotTime`。
2. 同一场景可多次求解，产生多个版本。
3. 场景关闭后不允许再生成新版本。

### 5.2 VersionApi

```java
public interface VersionApi {

    Long generateVersion(GenerateVersionReqDTO reqDTO);

    void confirmVersion(ChangeVersionStatusReqDTO reqDTO);

    void publishVersion(ChangeVersionStatusReqDTO reqDTO);

    void replaceVersion(ReplaceVersionReqDTO reqDTO);
}
```

版本状态机：

| 当前状态 | 动作 | 目标状态 |
| --- | --- | --- |
| `SOLVING` | `confirm` | `CONFIRMED` |
| `CONFIRMED` | `publish` | `PUBLISHED` |
| `PUBLISHED` | `replace` | `REPLACED` |

规则：

1. `SOLVING` 是初始状态，本轮仅落骨架，不实际求解。
2. 一个场景同一时刻只允许一个 `PUBLISHED` 版本。
3. 发布新版本时，旧 `PUBLISHED` 版本自动置为 `REPLACED`。
4. 已发布版本的排程结果不允许修改。
5. 状态推进必须幂等。

### 5.3 ScheduleTaskApi

```java
public interface ScheduleTaskApi {

    List<ScheduleTaskRespDTO> getTaskListByVersionId(Long versionId);

    List<ScheduleTaskRespDTO> getTaskListByOrderIds(Collection<Long> prodOrderIds);

    void lockTask(LockTaskReqDTO reqDTO);

    void unlockTask(LockTaskReqDTO reqDTO);
}
```

规则：

1. `lockedFlag = true` 表示已开工或人工锁定的任务，后续求解不可移动。
2. 任务时间必须满足 `startTime < endTime`。
3. 同一资源上任务时间不允许重叠。
4. 本轮只校验和落库，不做求解。

### 5.4 ProjectionApi

```java
public interface ProjectionApi {

    void refreshMaterialProjection(RefreshProjectionReqDTO reqDTO);

    void refreshInventoryProjection(RefreshProjectionReqDTO reqDTO);

    void refreshOrderProjection(RefreshProjectionReqDTO reqDTO);

    List<MaterialProjectionRespDTO> getMaterialProjectionList(MaterialProjectionQueryReqDTO query);

    List<InventoryProjectionRespDTO> getInventoryProjectionList(InventoryProjectionQueryReqDTO query);

    List<OrderProjectionRespDTO> getOrderProjectionList(OrderProjectionQueryReqDTO query);
}
```

数据来源：

| 投影 | 来源 API |
| --- | --- |
| 物料投影 | `MDM MaterialApi` |
| 库存投影 | `WMS InventoryApi` |
| 工单投影 | `MES ProdOrderApi` / `OpTaskApi` |

规则：

1. 投影表是 APS 内部只读快照，不反向更新核心模块。
2. 刷新命令必须幂等。
3. 投影记录必须带 `snapshotTime`。
4. 刷新失败不落部分数据，使用事务回滚。

### 5.5 ShortageApi / LoadApi

```java
public interface ShortageApi {

    List<ShortageRespDTO> getShortageListByVersionId(Long versionId);

    void refreshShortage(RefreshShortageReqDTO reqDTO);
}

public interface LoadApi {

    List<LoadRespDTO> getLoadListByVersionId(Long versionId);

    void refreshLoad(RefreshLoadReqDTO reqDTO);
}
```

规则：

1. 缺料数量 = 需求数量 - 可用数量。
2. 负荷按资源、日期汇总任务占用分钟。
3. 负荷不能超过资源日历可用分钟。
4. 本轮只实现汇总与校验，不做优化。

## 6. 数据模型与 Service 骨架

### 6.1 核心表

| 表 | 用途 |
| --- | --- |
| `aps_projection_mat` | 物料投影 |
| `aps_projection_inv` | 库存投影 |
| `aps_projection_order` | 工单投影 |
| `aps_scenario` | 排程场景 |
| `aps_version` | 排程版本 |
| `aps_task` | 工序时间计划 |
| `aps_shortage` | 缺料清单 |
| `aps_load` | 资源负荷 |

### 6.2 Service 骨架

```text
service/scenario
service/version
service/task
service/projection
service/shortage
service/load
```

要求：

1. Service 负责 API 契约中的业务规则。
2. API 实现类只做 DTO 转换与委托。
3. 投影刷新统一提供事务边界。
4. 状态机集中定义在 enums 包中。

## 7. 幂等与事务

1. 命令表均建 `idempotency_key` 唯一索引。
2. 幂等键格式：`sourceModule:sourceBizNo:action`。
3. 版本状态推进必须幂等。
4. 投影刷新使用事务，失败回滚。
5. 同一场景发布新版本时，替代旧版本与新版本创建在同一事务内。

## 8. 测试计划

| 主题 | 用例 |
| --- | --- |
| 场景 | 创建记录快照时间，关闭后拒绝生成版本 |
| 版本 | 状态机合法迁移与非法迁移拒绝 |
| 发布 | 同一场景只有一个 `PUBLISHED` 版本 |
| 任务 | 时间区间校验，同一资源不重叠 |
| 投影 | 分别调用 MDM / WMS / MES API |
| 缺料 | 需求 - 可用计算正确 |
| 负荷 | 不超过日历可用分钟 |
| 幂等 | 重复命令返回原结果 |

验证命令：

```bash
mvn -pl iboot-module-aps -am test
```

## 9. 交付清单

- [ ] `api` 包与 `package-info.java`
- [ ] 6 个 API 接口全部落地
- [ ] DTO 无 DO / VO 泄漏
- [ ] 只消费 MDM / WMS / MES API
- [ ] 未暴露 Timefold 类型
- [ ] 版本状态机集中定义
- [ ] 命令 DTO 均有 `idempotencyKey`
- [ ] 单元测试通过
- [ ] 未被核心模块反向依赖


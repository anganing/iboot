# WMS 模块 API 门面开发文档

- 版本：v1.0.0
- 日期：2026-08-30
- 状态：待实施
- 模块：`iboot-module-wms`
- 定位：库存唯一事实源，向 MES / APS / Integration / UI 提供库存与库存作业门面

---

## 1. 范围

### 1.1 本轮要做

1. 新增 `com.iboot.pf.module.wms.api` 包。
2. 建设 10 个 API 门面：仓库结构、批次、容器、库存、预留、冻结、入库、出库、转移、盘点。
3. 建 Service / Mapper / DO 骨架时，接口方法与 API 契约同步落地。
4. 命令类方法按幂等键实现。
5. 为 API 层和 Service 层核心规则编写测试。

### 1.2 本轮不做

1. 不做完整 admin 页面。
2. 不做设备任务下发与 WCS / AGV 集成。
3. 不做策略引擎完整实现。
4. 不做 APS 排程，只提供库存事实与投影查询。
5. 不依赖 MES / APS。

## 2. Maven 依赖

新增依赖：

```xml
<dependency>
    <groupId>com.iboot</groupId>
    <artifactId>iboot-module-mdm</artifactId>
    <version>${revision}</version>
</dependency>
```

禁止新增：

```xml
iboot-module-mes
iboot-module-aps
```

## 3. 包结构

```text
com.iboot.pf.module.wms.api
├── package-info.java
├── warehouse
│   ├── WarehouseApi.java
│   ├── WarehouseApiImpl.java
│   └── dto
├── batch
│   ├── BatchApi.java
│   ├── BatchApiImpl.java
│   └── dto
├── container
│   ├── ContainerApi.java
│   ├── ContainerApiImpl.java
│   └── dto
├── inventory
│   ├── InventoryApi.java
│   ├── InventoryApiImpl.java
│   └── dto
├── reservation
│   ├── InventoryReservationApi.java
│   ├── InventoryReservationApiImpl.java
│   └── dto
├── lock
│   ├── InventoryLockApi.java
│   ├── InventoryLockApiImpl.java
│   └── dto
├── inorder
│   ├── InOrderApi.java
│   ├── InOrderApiImpl.java
│   └── dto
├── outorder
│   ├── OutOrderApi.java
│   ├── OutOrderApiImpl.java
│   └── dto
├── transfer
│   ├── TransferOrderApi.java
│   ├── TransferOrderApiImpl.java
│   └── dto
└── count
    ├── CountOrderApi.java
    ├── CountOrderApiImpl.java
    └── dto
```

## 4. API 契约清单

| API | 核心能力 | 主要消费者 |
| --- | --- | --- |
| `WarehouseApi` | 仓库、库区、库位结构查询 | MES、APS、UI |
| `BatchApi` | 批次主档查询与注册 | MES、Integration、UI |
| `ContainerApi` | 容器状态与位置查询 | MES、Integration |
| `InventoryApi` | 余额、可用量、库存流水查询 | MES、APS、UI |
| `InventoryReservationApi` | 预留、释放、核销 | MES、APS、UI |
| `InventoryLockApi` | 冻结、解冻、冻结记录查询 | MES、UI |
| `InOrderApi` | 入库单创建、取消、过账 | MES、Integration、UI |
| `OutOrderApi` | 出库单创建、取消、过账 | MES、Integration、UI |
| `TransferOrderApi` | 转移单创建、取消、过账 | MES、UI |
| `CountOrderApi` | 盘点单创建、实盘、差异过账 | UI、Integration |

## 5. 关键接口设计

### 5.1 InventoryApi

```java
public interface InventoryApi {

    List<InventoryBalanceRespDTO> getInventoryBalanceList(InventoryQueryReqDTO query);

    Map<Long, BigDecimal> getAvailableQtyMap(InventoryQueryReqDTO query);

    List<InventoryTxnRespDTO> getInventoryTxnList(InventoryTxnQueryReqDTO query);

    PageResult<InventoryBalanceRespDTO> getInventoryBalancePage(InventoryPageReqDTO pageReq);
}
```

可用量计算规则：

```text
qty_available = qty_onhand - qty_reserved - qty_frozen
```

约束：

1. 只统计 `quality_status = QUALIFIED` 的库存。
2. 可用量为负数时原样返回，调用方自行判断是否异常。
3. 不提供修改余额的 API，所有变动必须走单据过账。

### 5.2 InventoryReservationApi

```java
public interface InventoryReservationApi {

    Long reserveInventory(CreateReservationReqDTO reqDTO);

    void releaseInventory(ReleaseReservationReqDTO reqDTO);

    void consumeReservation(ConsumeReservationReqDTO reqDTO);
}
```

核心规则：

1. 预留占用不扣减在库量。
2. 预留成功必须校验可用量充足。
3. 释放和核销都必须幂等。
4. 释放或核销数量不能超过原预留剩余数量。
5. 预留记录必须记录来源单号与来源行号。

### 5.3 InOrderApi / OutOrderApi

```java
public interface InOrderApi {

    Long createInOrder(CreateInOrderReqDTO reqDTO);

    void cancelInOrder(CancelOrderReqDTO reqDTO);

    void confirmInOrder(ConfirmOrderReqDTO reqDTO);
}

public interface OutOrderApi {

    Long createOutOrder(CreateOutOrderReqDTO reqDTO);

    void cancelOutOrder(CancelOrderReqDTO reqDTO);

    void confirmOutOrder(ConfirmOrderReqDTO reqDTO);
}
```

通用规则：

1. 单据状态：`0` 新建 / `1` 已确认 / `2` 已过账 / `3` 已取消。
2. 只有新建状态可取消。
3. 只有新建或已确认状态可过账。
4. 过账必须写 `wms_inv_txn`，再更新 `wms_inv_balance`。
5. 入库过账可创建或更新批次。
6. 出库过账前必须校验库位、批次、数量可用。
7. 所有命令必须带幂等键。

### 5.4 InventoryLockApi

```java
public interface InventoryLockApi {

    Long lockInventory(CreateLockReqDTO reqDTO);

    void unlockInventory(ReleaseLockReqDTO reqDTO);

    List<InventoryLockRespDTO> getLockList(InventoryLockQueryReqDTO query);
}
```

### 5.5 TransferOrderApi / CountOrderApi

```java
public interface TransferOrderApi {

    Long createTransferOrder(CreateTransferOrderReqDTO reqDTO);

    void cancelTransferOrder(CancelOrderReqDTO reqDTO);

    void confirmTransferOrder(ConfirmOrderReqDTO reqDTO);
}

public interface CountOrderApi {

    Long createCountOrder(CreateCountOrderReqDTO reqDTO);

    void submitCountResult(SubmitCountResultReqDTO reqDTO);

    void confirmCountDiff(ConfirmCountDiffReqDTO reqDTO);
}
```

盘点规则：

1. 盘点创建时可按仓库、库区、物料生成盘点行。
2. 提交实盘后计算 `diff_qty = actual_qty - book_qty`。
3. 有差异时确认动作生成盘盈入库单或盘亏出库单。
4. 差异确认必须走单据过账，禁止直接修改余额。

## 6. 数据模型与 Service 骨架

### 6.1 核心表

| 表 | 用途 |
| --- | --- |
| `wms_wh` / `wms_zone` / `wms_loc` | 仓库结构 |
| `wms_batch` | 批次主档 |
| `wms_container_type` / `wms_container` | 容器 |
| `wms_inv_balance` | 库存余额 |
| `wms_inv_txn` | 库存流水 |
| `wms_inv_reservation` | 预留记录 |
| `wms_inv_lock` | 冻结记录 |
| `wms_in_order` / `wms_in_order_line` | 入库单 |
| `wms_out_order` / `wms_out_order_line` | 出库单 |
| `wms_transfer_order` / `wms_transfer_order_line` | 转移单 |
| `wms_count_order` / `wms_count_order_line` | 盘点单 |

### 6.2 Service 骨架

按子域建立：

```text
service/warehouse
service/batch
service/container
service/inventory
service/reservation
service/lock
service/inorder
service/outorder
service/transfer
service/count
```

每个 Service 必须承担状态校验、幂等、流水与余额一致性。

## 7. 幂等与事务

1. 命令表均建 `idempotency_key` 唯一索引。
2. 幂等键格式：`sourceModule:sourceBizNo:action:sourceBizLineNo`。
3. 过账使用 Spring 事务。
4. 余额更新使用乐观锁 `version` 字段。
5. 单据过账与流水写入必须在同一事务内。
6. 幂等命中时返回原单据 ID，不抛异常。

## 8. 测试计划

| 主题 | 用例 |
| --- | --- |
| 可用量 | 预留与冻结参与扣减 |
| 预留 | 可用量不足拒绝；重复提交返回原 ID |
| 入库过账 | 写流水、更新余额、更新批次 |
| 出库过账 | 可用量不足拒绝；写流水、更新余额 |
| 取消 | 非新建状态拒绝 |
| 转移 | 库位 / 仓库 / 质量 / 项目转移过账 |
| 盘点 | 差异生成调整单，不直接改余额 |
| 幂等 | 重复命令返回原结果 |
| 乐观锁 | 并发过账冲突重试或失败 |

验证命令：

```bash
mvn -pl iboot-module-wms -am test
```

## 9. 交付清单

- [ ] `api` 包与 `package-info.java`
- [ ] 10 个 API 接口全部落地
- [ ] DTO 无 DO / VO 泄漏
- [ ] 查询方法批量执行，无 N+1
- [ ] 命令 DTO 均有 `idempotencyKey`
- [ ] 过账只通过 Service 修改库存
- [ ] 幂等键唯一约束生效
- [ ] 单元测试通过
- [ ] 未新增 MES / APS 依赖

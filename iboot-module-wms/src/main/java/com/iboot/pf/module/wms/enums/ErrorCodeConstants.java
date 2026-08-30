package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.exception.ErrorCode;

/** WMS 错误码，使用 1-063 段。 */
public interface ErrorCodeConstants {

    // ========== 通用 ==========
    ErrorCode QTY_MUST_BE_POSITIVE = new ErrorCode(1_063_000_001, "数量必须大于零");
    ErrorCode IDEMPOTENCY_KEY_BLANK = new ErrorCode(1_063_000_002, "幂等键不能为空");
    ErrorCode SOURCE_BIZ_BLANK = new ErrorCode(1_063_000_003, "来源模块与来源单号不能为空");

    // ========== 仓库结构 ==========
    ErrorCode WH_NOT_EXISTS = new ErrorCode(1_063_001_000, "仓库不存在");
    ErrorCode WH_DISABLED = new ErrorCode(1_063_001_001, "仓库已停用");
    ErrorCode ZONE_NOT_EXISTS = new ErrorCode(1_063_001_002, "库区不存在");
    ErrorCode ZONE_DISABLED = new ErrorCode(1_063_001_003, "库区已停用");
    ErrorCode LOC_NOT_EXISTS = new ErrorCode(1_063_001_004, "库位不存在");
    ErrorCode LOC_DISABLED = new ErrorCode(1_063_001_005, "库位已停用，不能收发存");

    // ========== 批次 ==========
    ErrorCode BATCH_NOT_EXISTS = new ErrorCode(1_063_002_000, "批次不存在");
    ErrorCode BATCH_DISABLED = new ErrorCode(1_063_002_001, "批次已停用");
    ErrorCode BATCH_LOT_NO_BLANK = new ErrorCode(1_063_002_002, "批次号不能为空");

    // ========== 容器 ==========
    ErrorCode CONTAINER_NOT_EXISTS = new ErrorCode(1_063_003_000, "容器不存在");
    ErrorCode CONTAINER_TYPE_NOT_EXISTS = new ErrorCode(1_063_003_001, "容器类型不存在");
    ErrorCode CONTAINER_CLOSED = new ErrorCode(1_063_003_002, "容器已关闭，不能再使用");

    // ========== 库存 ==========
    ErrorCode INV_BALANCE_NOT_EXISTS = new ErrorCode(1_063_004_000, "库存余额不存在");
    ErrorCode INV_AVAILABLE_QTY_NOT_ENOUGH = new ErrorCode(1_063_004_001, "库存可用量不足");
    ErrorCode INV_ONHAND_QTY_NOT_ENOUGH = new ErrorCode(1_063_004_002, "库存现存数量不足");
    ErrorCode INV_BALANCE_VERSION_CONFLICT = new ErrorCode(1_063_004_003, "库存余额已被并发更新，请重试");
    ErrorCode INV_POST_TYPE_INVALID = new ErrorCode(1_063_004_004, "库存事务类型与收发方向不匹配");
    ErrorCode INV_POST_QTY_INVALID = new ErrorCode(1_063_004_005, "库存过账数量必须大于零");

    // ========== 预留 ==========
    ErrorCode RESERVATION_NOT_EXISTS = new ErrorCode(1_063_005_000, "库存预留不存在");
    ErrorCode RESERVATION_NOT_ACTIVE = new ErrorCode(1_063_005_001, "库存预留已释放或已核销，不能重复处理");
    ErrorCode RESERVATION_REMAIN_NOT_ENOUGH = new ErrorCode(1_063_005_002, "释放或核销数量超过预留剩余数量");

    // ========== 冻结 ==========
    ErrorCode LOCK_NOT_EXISTS = new ErrorCode(1_063_006_000, "库存冻结记录不存在");
    ErrorCode LOCK_NOT_ACTIVE = new ErrorCode(1_063_006_001, "库存冻结记录已解冻或已取消");
    ErrorCode LOCK_REMAIN_NOT_ENOUGH = new ErrorCode(1_063_006_002, "解冻数量超过冻结剩余数量");
    ErrorCode LOCK_REASON_BLANK = new ErrorCode(1_063_006_003, "冻结原因不能为空");

    // ========== 入库单 ==========
    ErrorCode IN_ORDER_NOT_EXISTS = new ErrorCode(1_063_007_000, "入库单不存在");
    ErrorCode IN_ORDER_CANNOT_CANCEL = new ErrorCode(1_063_007_001, "只有新建状态的入库单可以取消");
    ErrorCode IN_ORDER_CANNOT_CONFIRM = new ErrorCode(1_063_007_002, "只有新建或已确认状态的入库单可以过账");
    ErrorCode IN_ORDER_LINE_EMPTY = new ErrorCode(1_063_007_003, "入库单至少包含一条明细");
    ErrorCode IN_ORDER_LINE_NOT_EXISTS = new ErrorCode(1_063_007_004, "入库单明细不存在");
    ErrorCode IN_ORDER_LOC_REQUIRED = new ErrorCode(1_063_007_005, "入库明细必须指定目标库位");

    // ========== 出库单 ==========
    ErrorCode OUT_ORDER_NOT_EXISTS = new ErrorCode(1_063_008_000, "出库单不存在");
    ErrorCode OUT_ORDER_CANNOT_CANCEL = new ErrorCode(1_063_008_001, "只有新建状态的出库单可以取消");
    ErrorCode OUT_ORDER_CANNOT_CONFIRM = new ErrorCode(1_063_008_002, "只有新建或已确认状态的出库单可以过账");
    ErrorCode OUT_ORDER_LINE_EMPTY = new ErrorCode(1_063_008_003, "出库单至少包含一条明细");
    ErrorCode OUT_ORDER_LINE_NOT_EXISTS = new ErrorCode(1_063_008_004, "出库单明细不存在");
    ErrorCode OUT_ORDER_LOC_REQUIRED = new ErrorCode(1_063_008_005, "出库明细必须指定来源库位");

    // ========== 转移单 ==========
    ErrorCode TRANSFER_ORDER_NOT_EXISTS = new ErrorCode(1_063_009_000, "转移单不存在");
    ErrorCode TRANSFER_ORDER_CANNOT_CANCEL = new ErrorCode(1_063_009_001, "只有新建状态的转移单可以取消");
    ErrorCode TRANSFER_ORDER_CANNOT_CONFIRM = new ErrorCode(1_063_009_002, "只有新建或已确认状态的转移单可以过账");
    ErrorCode TRANSFER_ORDER_LINE_EMPTY = new ErrorCode(1_063_009_003, "转移单至少包含一条明细");
    ErrorCode TRANSFER_ORDER_LOC_REQUIRED = new ErrorCode(1_063_009_004, "转移明细必须指定来源与目标库位");
    ErrorCode TRANSFER_ORDER_SAME_TARGET = new ErrorCode(1_063_009_005, "转移来源与目标完全一致，无需转移");

    // ========== 盘点单 ==========
    ErrorCode COUNT_ORDER_NOT_EXISTS = new ErrorCode(1_063_010_000, "盘点单不存在");
    ErrorCode COUNT_ORDER_STATUS_INVALID = new ErrorCode(1_063_010_001, "盘点单当前状态不允许该操作");
    ErrorCode COUNT_ORDER_LINE_NOT_EXISTS = new ErrorCode(1_063_010_002, "盘点明细不存在");
    ErrorCode COUNT_ORDER_NO_DIFF = new ErrorCode(1_063_010_003, "盘点单没有需要确认的差异");
    ErrorCode TRANSFER_ORDER_LINE_NOT_EXISTS = new ErrorCode(1_063_009_006, "转移单明细不存在");

    ErrorCode INV_TXN_NOT_EXISTS = new ErrorCode(1_063_004_006, "库存流水不存在");
    ErrorCode INV_RESERVATION_NOT_EXISTS = new ErrorCode(1_063_005_003, "库存预留记录不存在");
    ErrorCode INV_LOCK_NOT_EXISTS = new ErrorCode(1_063_006_004, "库存冻结记录不存在");
    ErrorCode ARRIVAL_NOT_EXISTS = new ErrorCode(1_063_011_000, "到货登记不存在");
    ErrorCode ARRIVAL_LINE_NOT_EXISTS = new ErrorCode(1_063_011_001, "到货登记明细不存在");
    ErrorCode WAVE_NOT_EXISTS = new ErrorCode(1_063_012_000, "出库波次不存在");
    ErrorCode WAVE_LINE_NOT_EXISTS = new ErrorCode(1_063_012_001, "波次明细不存在");
    ErrorCode DEVICE_TASK_NOT_EXISTS = new ErrorCode(1_063_013_000, "设备搬运任务不存在");
    ErrorCode STRATEGY_NOT_EXISTS = new ErrorCode(1_063_014_000, "仓储策略不存在");

}

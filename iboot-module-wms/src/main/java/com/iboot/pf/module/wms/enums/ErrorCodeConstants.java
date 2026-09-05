package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.exception.ErrorCode;

/** WMS 错误码，使用 1-063 段。 */
public interface ErrorCodeConstants {

    // ========== 通用 ==========
    ErrorCode QTY_MUST_BE_POSITIVE = new ErrorCode(1_063_000_001, "数量必须大于零");
    ErrorCode IDEMPOTENCY_KEY_BLANK = new ErrorCode(1_063_000_002, "幂等键不能为空");
    ErrorCode SOURCE_BIZ_BLANK = new ErrorCode(1_063_000_003, "来源模块与来源单号不能为空");
    ErrorCode LINE_MAT_NOT_EXISTS = new ErrorCode(1_063_000_004, "明细的物料不存在");
    ErrorCode LINE_UOM_NOT_EXISTS = new ErrorCode(1_063_000_005, "明细的计量单位不存在");

    // ========== 仓库结构 ==========
    ErrorCode WH_NOT_EXISTS = new ErrorCode(1_063_001_000, "仓库不存在");
    ErrorCode WH_DISABLED = new ErrorCode(1_063_001_001, "仓库已停用");
    ErrorCode ZONE_NOT_EXISTS = new ErrorCode(1_063_001_002, "库区不存在");
    ErrorCode ZONE_DISABLED = new ErrorCode(1_063_001_003, "库区已停用");
    ErrorCode LOC_NOT_EXISTS = new ErrorCode(1_063_001_004, "库位不存在");
    ErrorCode LOC_DISABLED = new ErrorCode(1_063_001_005, "库位已停用，不能收发存");
    ErrorCode WH_CODE_DUPLICATE = new ErrorCode(1_063_001_006, "同一库存组织下仓库编码已存在");
    ErrorCode ZONE_CODE_DUPLICATE = new ErrorCode(1_063_001_007, "同一仓库下库区编码已存在");
    ErrorCode LOC_CODE_DUPLICATE = new ErrorCode(1_063_001_008, "同一仓库下库位编码已存在");
    ErrorCode ZONE_WH_NOT_MATCH = new ErrorCode(1_063_001_009, "库区不属于所选仓库");
    ErrorCode WH_HAS_ZONE = new ErrorCode(1_063_001_010, "仓库下存在库区，不能删除");
    ErrorCode ZONE_HAS_LOC = new ErrorCode(1_063_001_011, "库区下存在库位，不能删除");
    ErrorCode WH_HAS_LOC = new ErrorCode(1_063_001_012, "仓库下存在库位，不能删除");
    ErrorCode WH_INV_ORG_NOT_EXISTS = new ErrorCode(1_063_001_013, "库存组织不存在");
    ErrorCode WH_PLANT_NOT_EXISTS = new ErrorCode(1_063_001_014, "工厂不存在");

    // ========== 批次 ==========
    ErrorCode BATCH_NOT_EXISTS = new ErrorCode(1_063_002_000, "批次不存在");
    ErrorCode BATCH_DISABLED = new ErrorCode(1_063_002_001, "批次已停用");
    ErrorCode BATCH_LOT_NO_BLANK = new ErrorCode(1_063_002_002, "批次号不能为空");
    ErrorCode BATCH_LOT_NO_DUPLICATE = new ErrorCode(1_063_002_003, "同一物料下批次号已存在");
    ErrorCode BATCH_SN_NO_DUPLICATE = new ErrorCode(1_063_002_004, "序列号已存在");

    // ========== 容器 ==========
    ErrorCode CONTAINER_NOT_EXISTS = new ErrorCode(1_063_003_000, "容器不存在");
    ErrorCode CONTAINER_TYPE_NOT_EXISTS = new ErrorCode(1_063_003_001, "容器类型不存在");
    ErrorCode CONTAINER_CLOSED = new ErrorCode(1_063_003_002, "容器已关闭，不能再使用");
    ErrorCode CONTAINER_NO_DUPLICATE = new ErrorCode(1_063_003_003, "容器编号已存在");
    ErrorCode CONTAINER_TYPE_CODE_DUPLICATE = new ErrorCode(1_063_003_004, "容器类型编码已存在");
    ErrorCode CONTAINER_TYPE_HAS_CONTAINER = new ErrorCode(1_063_003_005, "容器类型已被容器引用，不能删除");

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
    ErrorCode DEVICE_TASK_NO_DUPLICATE = new ErrorCode(1_063_013_001, "设备任务号已存在");
    ErrorCode STRATEGY_NOT_EXISTS = new ErrorCode(1_063_014_000, "仓储策略不存在");
    ErrorCode STRATEGY_CODE_DUPLICATE = new ErrorCode(1_063_014_001, "同类型下策略编码与版本已存在");
    ErrorCode STRATEGY_TYPE_UNSUPPORTED = new ErrorCode(1_063_014_002, "不支持的仓储策略类型");
    ErrorCode STRATEGY_CONFIG_INVALID = new ErrorCode(1_063_014_003, "仓储策略配置解析失败：{}");
    ErrorCode STRATEGY_CONFIG_INCOMPLETE = new ErrorCode(1_063_014_004, "策略配置不完整，无法启用：{}");

    // ========== 单据编号唯一性 ==========
    ErrorCode ARRIVAL_NO_DUPLICATE = new ErrorCode(1_063_011_002, "到货登记单号已存在");
    ErrorCode ARRIVAL_LINE_NO_DUPLICATE = new ErrorCode(1_063_011_003, "同一到货单下行号已存在");
    ErrorCode ARRIVAL_LINE_MAT_NOT_EXISTS = new ErrorCode(1_063_011_004, "到货明细的物料不存在");
    ErrorCode ARRIVAL_LINE_UOM_NOT_EXISTS = new ErrorCode(1_063_011_005, "到货明细的计量单位不存在");
    ErrorCode IN_ORDER_NO_DUPLICATE = new ErrorCode(1_063_007_006, "入库单号已存在");
    ErrorCode IN_ORDER_LINE_NO_DUPLICATE = new ErrorCode(1_063_007_007, "同一入库单下行号已存在");
    ErrorCode OUT_ORDER_NO_DUPLICATE = new ErrorCode(1_063_008_006, "出库单号已存在");
    ErrorCode OUT_ORDER_LINE_NO_DUPLICATE = new ErrorCode(1_063_008_007, "同一出库单下行号已存在");
    ErrorCode TRANSFER_ORDER_NO_DUPLICATE = new ErrorCode(1_063_009_007, "转移单号已存在");
    ErrorCode TRANSFER_ORDER_LINE_NO_DUPLICATE = new ErrorCode(1_063_009_008, "同一转移单下行号已存在");
    ErrorCode COUNT_ORDER_NO_DUPLICATE = new ErrorCode(1_063_010_004, "盘点单号已存在");
    ErrorCode COUNT_ORDER_LINE_NO_DUPLICATE = new ErrorCode(1_063_010_005, "同一盘点单下行号已存在");
    ErrorCode COUNT_ORDER_LINE_BALANCE_DUPLICATE = new ErrorCode(1_063_010_006, "同一盘点单下该库存余额已加入");
    ErrorCode WAVE_NO_DUPLICATE = new ErrorCode(1_063_012_002, "波次编号已存在");
    ErrorCode WAVE_LINE_NO_DUPLICATE = new ErrorCode(1_063_012_003, "同一波次下行号已存在");
    ErrorCode WAVE_LINE_ORDER_DUPLICATE = new ErrorCode(1_063_012_004, "同一波次下该出库明细已加入");
    ErrorCode WAVE_LINE_ORDER_NOT_MATCH = new ErrorCode(1_063_012_005, "出库明细不属于所选出库单");
    ErrorCode INV_LOCK_NO_DUPLICATE = new ErrorCode(1_063_006_005, "冻结编号已存在");
    ErrorCode INV_RESERVATION_NO_DUPLICATE = new ErrorCode(1_063_005_004, "预留编号已存在");
    ErrorCode INV_TXN_NO_DUPLICATE = new ErrorCode(1_063_004_007, "库存流水号已存在");
    ErrorCode INV_BALANCE_DIMENSION_DUPLICATE = new ErrorCode(1_063_004_008, "相同库存维度的余额已存在");

    // ========== 单据状态推进（T0-06 追加） ==========
    ErrorCode IN_ORDER_CANNOT_SUBMIT = new ErrorCode(1_063_007_008, "只有新建状态的入库单可以提交");
    ErrorCode IN_ORDER_CANNOT_MODIFY = new ErrorCode(1_063_007_009, "只有新建状态的入库单可以修改或删除");
    ErrorCode OUT_ORDER_CANNOT_SUBMIT = new ErrorCode(1_063_008_008, "只有新建状态的出库单可以提交");
    ErrorCode OUT_ORDER_CANNOT_MODIFY = new ErrorCode(1_063_008_009, "只有新建状态的出库单可以修改或删除");
    ErrorCode TRANSFER_ORDER_CANNOT_SUBMIT = new ErrorCode(1_063_009_009, "只有新建状态的转移单可以提交");
    ErrorCode TRANSFER_ORDER_CANNOT_MODIFY = new ErrorCode(1_063_009_010, "只有新建状态的转移单可以修改或删除");
    ErrorCode COUNT_ORDER_CANNOT_FREEZE = new ErrorCode(1_063_010_007, "只有新建状态的盘点单可以冻结盘点范围");
    ErrorCode COUNT_ORDER_CANNOT_MODIFY = new ErrorCode(1_063_010_008, "只有新建状态的盘点单可以修改或删除");
    ErrorCode COUNT_ORDER_LINE_EMPTY = new ErrorCode(1_063_010_009, "盘点单至少包含一条明细");

    // ========== 到货登记动作（T0-07 追加） ==========
    ErrorCode ARRIVAL_CANNOT_CONFIRM = new ErrorCode(1_063_011_006, "只有新建状态的到货登记可以确认到货");
    ErrorCode ARRIVAL_CANNOT_MODIFY = new ErrorCode(1_063_011_007, "只有新建状态的到货登记可以修改或删除");
    ErrorCode ARRIVAL_CANNOT_CANCEL = new ErrorCode(1_063_011_008, "已完成或已取消的到货登记不能取消");
    ErrorCode ARRIVAL_LINE_EMPTY = new ErrorCode(1_063_011_009, "到货登记至少包含一条明细");
    ErrorCode ARRIVAL_LINE_NOT_READY = new ErrorCode(1_063_011_010, "只有待入库状态的到货明细可以生成入库单");
    ErrorCode ARRIVAL_LINE_NOT_MATCH = new ErrorCode(1_063_011_011, "到货明细不属于所选到货登记");

}

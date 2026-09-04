package com.iboot.pf.module.wms.enums;

/**
 * WMS 跨模块命令类型。
 *
 * <p>写进 {@code wms_command_record.command_type}，配合幂等键做重复下发拦截。
 * 集中定义而不是在各 Service 里写字面量：改一个名字就会和历史记录对不上。
 *
 * <p>只登记没有落库载体的状态推进型命令。创建型命令（建入库单、建出库单、预留、冻结、过账）
 * 单据表自带 {@code idempotency_key} 唯一索引，走列判重，不进这里。
 */
public enum WmsCommandTypeEnum {

    /** 提交入库单。 */
    SUBMIT_IN_ORDER,
    /** 确认入库单并过账。 */
    CONFIRM_IN_ORDER,
    /** 取消入库单。 */
    CANCEL_IN_ORDER,
    /** 提交出库单。 */
    SUBMIT_OUT_ORDER,
    /** 确认出库单并过账。 */
    CONFIRM_OUT_ORDER,
    /** 取消出库单。 */
    CANCEL_OUT_ORDER,
    /** 提交转移单。 */
    SUBMIT_TRANSFER,
    /** 确认转移单并过账。 */
    CONFIRM_TRANSFER,
    /** 取消转移单。 */
    CANCEL_TRANSFER,
    /** 冻结盘点范围。 */
    FREEZE_COUNT,
    /** 提交实盘结果。 */
    SUBMIT_COUNT_RESULT,
    /** 确认盘点差异并过账。 */
    CONFIRM_COUNT_DIFF,
    /** 确认到货。 */
    CONFIRM_ARRIVAL,
    /** 释放库存预留。 */
    RELEASE_RESERVATION,
    /** 解冻库存。 */
    UNLOCK;

}

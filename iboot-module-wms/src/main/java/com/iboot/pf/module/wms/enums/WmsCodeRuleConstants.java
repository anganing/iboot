package com.iboot.pf.module.wms.enums;

/**
 * WMS 编码规则码。
 *
 * <p>取值必须与 {@code infra_code_rule.rule_code} 完全一致，否则生成单号时会抛“编码规则不存在”。
 */
public interface WmsCodeRuleConstants {

    String WH = "WMS_WH_CODE";

    String ZONE = "WMS_ZONE_CODE";

    String LOC = "WMS_LOC_CODE";

    String IN_ORDER = "WMS_IN_ORDER_NO";

    String OUT_ORDER = "WMS_OUT_ORDER_NO";

    String TRANSFER_ORDER = "WMS_TRANSFER_NO";

    String COUNT_ORDER = "WMS_COUNT_NO";

    String CONTAINER = "WMS_CONTAINER_NO";

    String CONTAINER_TYPE = "WMS_CONTAINER_TYPE_CODE";

    String BATCH_LOT = "WMS_BATCH_LOT_NO";

    String ARRIVAL = "WMS_ARRIVAL_NO";

    String WAVE = "WMS_WAVE_NO";

    String DEVICE_TASK = "WMS_DEVICE_TASK_NO";

    String STRATEGY = "WMS_STRATEGY_CODE";

    String TXN = "WMS_INV_TXN_NO";

    String RESERVATION = "WMS_RESERVATION_NO";

    String LOCK = "WMS_LOCK_NO";

}

package com.iboot.pf.module.wms.enums;

/**
 * WMS 字典类型编码。
 *
 * <p>与数据库 {@code system_dict_type.type} 一一对应，供 Excel 导出的 {@code @DictFormat} 和
 * 需要字典校验的场景引用，避免字典编码字符串散落在各个类里。
 */
public interface WmsDictTypeConstants {

    /** 通用启停状态，来自 system 模块。 */
    String COMMON_STATUS = "common_status";

    /** Boolean 是否类型，来自 infra 模块。 */
    String BOOLEAN_STRING = "infra_boolean_string";

    /** 物料类型，来自 mdm 模块。仓储策略按物料类型筛候选时要拿它渲染下拉。 */
    String MAT_TYPE = "mdm_mat_type";

    String WH_TYPE = "wms_wh_type";

    String MANAGED_BY = "wms_managed_by";

    String ZONE_TYPE = "wms_zone_type";

    String LOC_TYPE = "wms_loc_type";

    String LOC_STATUS = "wms_loc_status";

    String DIMENSION_CHECK_MODE = "wms_dimension_check_mode";

    String CONTAINER_STATUS = "wms_container_status";

    String STOCK_SCOPE = "wms_stock_scope";

    String QUALITY_STATUS = "wms_quality_status";

    String IO_TYPE = "wms_io_type";

    String TXN_TYPE = "wms_txn_type";

    String LOCK_STATUS = "wms_lock_status";

    String RESERVATION_STATUS = "wms_reservation_status";

    String ORDER_STATUS = "wms_order_status";

    String ORDER_LINE_STATUS = "wms_order_line_status";

    String IN_BIZ_TYPE = "wms_in_biz_type";

    String OUT_BIZ_TYPE = "wms_out_biz_type";

    String TRANSFER_TYPE = "wms_transfer_type";

    String COUNT_TYPE = "wms_count_type";

    String COUNT_LINE_STATUS = "wms_count_line_status";

    String ARRIVAL_STATUS = "wms_arrival_status";

    String ARRIVAL_LINE_STATUS = "wms_arrival_line_status";

    String ARRIVAL_SOURCE_TYPE = "wms_arrival_source_type";

    String IQC_MODE = "wms_iqc_mode";

    String HOOK_MODE = "wms_hook_mode";

    String WAVE_STATUS = "wms_wave_status";

    String WAVE_TYPE = "wms_wave_type";

    String STRATEGY_TYPE = "wms_strategy_type";

    String STRATEGY_STATUS = "wms_strategy_status";

    String DEVICE_TYPE = "wms_device_type";

    String DEVICE_TASK_TYPE = "wms_device_task_type";

    String DEVICE_TASK_STATUS = "wms_device_task_status";

    String DISPATCH_STATUS = "wms_dispatch_status";

    String POST_STATUS = "wms_post_status";

    String SOURCE_MODULE = "wms_source_module";

    String SOURCE_BIZ_TYPE = "wms_source_biz_type";

    String ORIGIN_TYPE = "wms_origin_type";

}

package com.iboot.pf.module.aps.enums;

/**
 * APS 字典类型编码。
 *
 * <p>与数据库 {@code system_dict_type.type} 一一对应，供 Excel 导出的 {@code @DictFormat} 引用，
 * 避免字典编码字符串散落在各个 VO 里。
 */
public interface ApsDictTypeConstants {

    /** 通用启停状态，来自 system 模块。 */
    String COMMON_STATUS = "common_status";

    /** Boolean 是否类型，来自 infra 模块。 */
    String BOOLEAN_STRING = "infra_boolean_string";

    String SCENARIO_STATUS = "aps_scenario_status";

    String VERSION_STATUS = "aps_version_status";

    String TASK_STATUS = "aps_task_status";

    String TASK_DEP_TYPE = "aps_task_dep_type";

    String TASK_LOCK_TYPE = "aps_task_lock_type";

    String TASK_LOCK_STATUS = "aps_task_lock_status";

    String STRATEGY_STATUS = "aps_strategy_status";

    String SHORTAGE_STATUS = "aps_shortage_status";

    String SNAPSHOT_TYPE = "aps_snapshot_type";

    String PROJECTION_SOURCE_TYPE = "aps_projection_source_type";

    /** 物料类型快照，取自 mdm 物料主数据。 */
    String MAT_TYPE = "mdm_mat_type";

    /** 资源类型快照，取自 mdm 生产资源主数据。 */
    String RES_TYPE = "mdm_res_type";

    /** 库存质量状态快照，取自 wms。 */
    String QUALITY_STATUS = "wms_quality_status";

    /** 库存范围快照，取自 wms。 */
    String STOCK_SCOPE = "wms_stock_scope";

    /** 工序任务状态快照，取自 mes。 */
    String OP_TASK_STATUS = "mes_op_task_status";

}

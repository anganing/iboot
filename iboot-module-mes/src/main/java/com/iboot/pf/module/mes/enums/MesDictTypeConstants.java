package com.iboot.pf.module.mes.enums;

/**
 * MES 字典类型编码。
 *
 * <p>与数据库 {@code system_dict_type.type} 一一对应，供 Excel 导出的 {@code @DictFormat} 和
 * 需要字典校验的场景引用，避免字典编码字符串散落在各个类里。
 */
public interface MesDictTypeConstants {

    /** 通用启停状态，来自 system 模块。 */
    String COMMON_STATUS = "common_status";

    /** Boolean 是否类型，来自 infra 模块。 */
    String BOOLEAN_STRING = "infra_boolean_string";

    String DEMAND_SOURCE_TYPE = "mes_demand_source_type";

    String DEMAND_STATUS = "mes_demand_status";

    String PROD_ORDER_STATUS = "mes_prod_order_status";

    String WORK_ORDER_STATUS = "mes_work_order_status";

    String OP_TASK_STATUS = "mes_op_task_status";

    String OP_EXEC_MODE = "mes_op_exec_mode";

    String QC_MODE = "mes_qc_mode";

    String SKIP_POLICY = "mes_skip_policy";

    String KIT_STATUS = "mes_kit_status";

    String KIT_CHECK_SCOPE = "mes_kit_check_scope";

    String MATERIAL_REQ_TYPE = "mes_material_req_type";

    String MATERIAL_REQ_STATUS = "mes_material_req_status";

    String MATERIAL_REQ_LINE_STATUS = "mes_material_req_line_status";

    String PROD_REPORT_STATUS = "mes_prod_report_status";

    String REWORK_STATUS = "mes_rework_status";

    String QC_TRIGGER_POINT = "mes_qc_trigger_point";

    String QC_OBJECT_TYPE = "mes_qc_object_type";

    String QC_TASK_STATUS = "mes_qc_task_status";

    String QC_CONCLUSION = "mes_qc_conclusion";

    String QC_ITEM_CONCLUSION = "mes_qc_item_conclusion";

    String QC_ITEM_TYPE = "mes_qc_item_type";

    String QC_PLAN_STATUS = "mes_qc_plan_status";

    String QC_SAMPLE_TYPE = "mes_qc_sample_type";

    String DEFECT_LEVEL = "mes_defect_level";

    String DEFECT_FILE_TYPE = "mes_defect_file_type";

    String DISPOSITION_TYPE = "mes_disposition_type";

    String DISPOSITION_STATUS = "mes_disposition_status";

    String DISPATCH_TASK_STATUS = "mes_dispatch_task_status";

    String SKIP_APPROVE_STATUS = "mes_skip_approve_status";

    String SKIP_RECORD_STATUS = "mes_skip_record_status";

    String SKIP_REASON_TYPE = "mes_skip_reason_type";

    String RGV_TASK_TYPE = "mes_rgv_task_type";

    String RGV_DISPATCH_STATUS = "mes_rgv_dispatch_status";

    String RGV_TASK_STATUS = "mes_rgv_task_status";

    String WIP_TXN_TYPE = "mes_wip_txn_type";

    String BIZ_TYPE = "mes_biz_type";

}

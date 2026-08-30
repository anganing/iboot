package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.exception.ErrorCode;

/** MES 错误码，使用 1-064 段。 */
public interface ErrorCodeConstants {

    ErrorCode PROD_ORDER_NOT_EXISTS = new ErrorCode(1_064_001_000, "生产工单不存在");
    ErrorCode PROD_ORDER_STATUS_INVALID = new ErrorCode(1_064_001_001, "生产工单当前状态不允许该操作");
    ErrorCode OP_TASK_STATUS_INVALID = new ErrorCode(1_064_002_000, "工序任务当前状态不允许该操作");
    ErrorCode OP_TASK_REPORT_QTY_INVALID = new ErrorCode(1_064_002_001, "报工数量必须等于计划数量");
    ErrorCode OP_TASK_NOT_EXISTS = new ErrorCode(1_064_002_002, "工序任务不存在");
    ErrorCode QUALITY_QTY_INVALID = new ErrorCode(1_064_003_000, "合格数量与不合格数量之和必须等于抽检数量");

    ErrorCode PROD_DEMAND_NOT_EXISTS = new ErrorCode(1_064_010_000, "生产需求不存在");
    ErrorCode WORK_ORDER_NOT_EXISTS = new ErrorCode(1_064_011_000, "作业工单不存在");
    ErrorCode KIT_SNAPSHOT_NOT_EXISTS = new ErrorCode(1_064_012_000, "齐套快照不存在");
    ErrorCode MATERIAL_REQ_NOT_EXISTS = new ErrorCode(1_064_013_000, "领退补料申请不存在");
    ErrorCode MATERIAL_REQ_LINE_NOT_EXISTS = new ErrorCode(1_064_013_001, "领退补料申请明细不存在");
    ErrorCode PROD_REPORT_NOT_EXISTS = new ErrorCode(1_064_014_000, "生产报工记录不存在");
    ErrorCode REWORK_NOT_EXISTS = new ErrorCode(1_064_015_000, "返工任务不存在");
    ErrorCode QC_TASK_NOT_EXISTS = new ErrorCode(1_064_016_000, "质检任务不存在");
    ErrorCode DEFECT_NOT_EXISTS = new ErrorCode(1_064_017_000, "缺陷记录不存在");
    ErrorCode DISPOSITION_NOT_EXISTS = new ErrorCode(1_064_018_000, "不合格品处置单不存在");
    ErrorCode WIP_TXN_NOT_EXISTS = new ErrorCode(1_064_019_000, "在制流转流水不存在");
    ErrorCode QC_ITEM_NOT_EXISTS = new ErrorCode(1_064_020_000, "检验项目不存在");
    ErrorCode QC_PLAN_NOT_EXISTS = new ErrorCode(1_064_021_000, "检验方案不存在");
    ErrorCode QC_RESULT_NOT_EXISTS = new ErrorCode(1_064_022_000, "检验结果不存在");
    ErrorCode QC_RULE_NOT_EXISTS = new ErrorCode(1_064_023_000, "质检触发规则不存在");
    ErrorCode DISPATCH_TASK_NOT_EXISTS = new ErrorCode(1_064_024_000, "派工任务不存在");
    ErrorCode OP_SKIP_RECORD_NOT_EXISTS = new ErrorCode(1_064_025_000, "工序跳过记录不存在");
    ErrorCode RGV_TASK_NOT_EXISTS = new ErrorCode(1_064_026_000, "RGV 工位流转任务不存在");
}

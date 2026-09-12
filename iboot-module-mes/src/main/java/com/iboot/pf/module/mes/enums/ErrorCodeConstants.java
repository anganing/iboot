package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.exception.ErrorCode;

/** MES 错误码，使用 1-064 段。 */
public interface ErrorCodeConstants {

    // ========== 通用 ==========
    ErrorCode QTY_MUST_BE_POSITIVE = new ErrorCode(1_064_000_001, "数量必须大于零");
    ErrorCode MAT_NOT_EXISTS = new ErrorCode(1_064_000_002, "物料不存在");
    ErrorCode UOM_NOT_EXISTS = new ErrorCode(1_064_000_003, "计量单位不存在");
    ErrorCode BOM_NOT_EXISTS = new ErrorCode(1_064_000_004, "BOM 不存在");
    ErrorCode ROUTE_NOT_EXISTS = new ErrorCode(1_064_000_005, "工艺路线不存在");
    ErrorCode ROUTE_OP_NOT_EXISTS = new ErrorCode(1_064_000_006, "工艺路线工序不存在");
    ErrorCode PLANT_NOT_EXISTS = new ErrorCode(1_064_000_007, "工厂不存在");
    ErrorCode WORKSHOP_NOT_EXISTS = new ErrorCode(1_064_000_008, "车间不存在");
    ErrorCode WC_NOT_EXISTS = new ErrorCode(1_064_000_009, "工作中心不存在");
    ErrorCode RES_NOT_EXISTS = new ErrorCode(1_064_000_010, "生产资源不存在");
    ErrorCode SHIFT_NOT_EXISTS = new ErrorCode(1_064_000_011, "班次不存在");
    ErrorCode TEAM_NOT_EXISTS = new ErrorCode(1_064_000_012, "班组不存在");
    ErrorCode BATCH_NOT_EXISTS = new ErrorCode(1_064_000_013, "批次不存在");
    ErrorCode DEFECT_CODE_NOT_EXISTS = new ErrorCode(1_064_000_014, "缺陷代码不存在");
    ErrorCode IDEMPOTENCY_KEY_DUPLICATE = new ErrorCode(1_064_000_015, "幂等键已存在，请勿重复提交");
    ErrorCode PROJECT_NOT_EXISTS = new ErrorCode(1_064_000_016, "项目不存在");
    ErrorCode PROJECT_TASK_NOT_EXISTS = new ErrorCode(1_064_000_017, "项目任务不存在");
    ErrorCode OP_NOT_EXISTS = new ErrorCode(1_064_000_018, "标准工序不存在");

    // ========== 生产工单 ==========
    ErrorCode PROD_ORDER_NOT_EXISTS = new ErrorCode(1_064_001_000, "生产工单不存在");
    ErrorCode PROD_ORDER_STATUS_INVALID = new ErrorCode(1_064_001_001, "生产工单当前状态不允许该操作");
    ErrorCode PROD_ORDER_NO_DUPLICATE = new ErrorCode(1_064_001_002, "生产工单号已存在");
    ErrorCode PROD_ORDER_HAS_WORK_ORDER = new ErrorCode(1_064_001_003, "生产工单下存在作业工单，不能删除");
    ErrorCode PROD_ORDER_QTY_INVALID = new ErrorCode(1_064_001_004, "计划生产数量必须大于零");
    ErrorCode PROD_ORDER_VERSION_SNAPSHOT_LOCKED =
            new ErrorCode(1_064_001_005, "生产工单已下达，锁定的 BOM 与工艺路线版本不允许修改");
    ErrorCode PROD_ORDER_ROUTE_OP_EMPTY =
            new ErrorCode(1_064_001_006, "工艺路线下没有工序，无法展开作业工单与工序任务");
    ErrorCode PROD_ORDER_CONCURRENT_CONFLICT =
            new ErrorCode(1_064_001_007, "生产工单正被其他操作更新，请刷新后重试");
    ErrorCode PROD_ORDER_WORKSHOP_REQUIRED =
            new ErrorCode(1_064_001_008, "生产工单未指派车间，下达前请先指定车间");
    ErrorCode PROD_ORDER_INBOUND_QTY_EXCEEDED =
            new ErrorCode(1_064_001_009, "生产工单({})本次完工入库 {}，累计入库 {} 超过已完工数量 {}");
    ErrorCode PROD_ORDER_INBOUND_STOCK_REQUIRED =
            new ErrorCode(1_064_001_010, "成品物料({})未配置默认仓库与默认库位，无法完工入库");
    ErrorCode PROD_ORDER_BACKFLUSH_STOCK_REQUIRED =
            new ErrorCode(1_064_001_011, "倒冲物料({})未配置默认仓库与默认库位，无法反冲扣料");
    ErrorCode PROD_ORDER_BACKFLUSH_WH_CONFLICT =
            new ErrorCode(1_064_001_012, "倒冲物料({})的默认仓库互不相同，关闭工单时请指定统一的发料仓库");
    ErrorCode PROD_ORDER_BACKFLUSH_QTY_INSUFFICIENT =
            new ErrorCode(1_064_001_013, "倒冲物料({})可用量不足，按实际产量需反冲 {}，当前可用 {}");

    // ========== 工序任务 ==========
    ErrorCode OP_TASK_STATUS_INVALID = new ErrorCode(1_064_002_000, "工序任务当前状态不允许该操作");
    ErrorCode OP_TASK_REPORT_QTY_INVALID = new ErrorCode(1_064_002_001, "报工数量必须等于计划数量");
    ErrorCode OP_TASK_NOT_EXISTS = new ErrorCode(1_064_002_002, "工序任务不存在");
    ErrorCode OP_TASK_NO_DUPLICATE = new ErrorCode(1_064_002_003, "工序任务号已存在");
    ErrorCode OP_TASK_ROUTE_OP_DUPLICATE = new ErrorCode(1_064_002_004, "同一作业工单下该路线工序已生成任务");
    ErrorCode OP_TASK_HAS_DISPATCH = new ErrorCode(1_064_002_005, "工序任务下存在派工任务，不能删除");
    ErrorCode OP_TASK_PREV_NOT_DONE = new ErrorCode(1_064_002_006, "前序工序未完工：{}");
    ErrorCode OP_TASK_NOT_KITTED = new ErrorCode(1_064_002_007, "开工前必发料未发齐：{}");
    ErrorCode OP_TASK_FAI_NOT_PASSED =
            new ErrorCode(1_064_002_008, "首件检验未通过，工序({})不能继续加工：{}");
    ErrorCode OP_TASK_RES_UNAVAILABLE = new ErrorCode(1_064_002_009, "资源({})不可用：{}");
    ErrorCode OP_TASK_SKILL_NOT_MATCH = new ErrorCode(1_064_002_010, "操作者技能不满足工序({})要求：{}");
    ErrorCode OP_TASK_TOOL_NOT_READY = new ErrorCode(1_064_002_011, "工序({})所需工装不可用：{}");
    ErrorCode OP_TASK_CONCURRENT_CONFLICT =
            new ErrorCode(1_064_002_012, "工序任务正被其他操作更新，请刷新后重试");
    ErrorCode OP_TASK_FINISH_QTY_MISMATCH =
            new ErrorCode(1_064_002_013, "完工数量与工序任务当前累计不一致：本次提交合格 {} 报废 {}，实际累计合格 {} 报废 {}");

    // ========== 质量共用 ==========
    ErrorCode QUALITY_QTY_INVALID = new ErrorCode(1_064_003_000, "合格数量与不合格数量之和必须等于抽检数量");

    // ========== 生产需求 ==========
    ErrorCode PROD_DEMAND_NOT_EXISTS = new ErrorCode(1_064_010_000, "生产需求不存在");
    ErrorCode PROD_DEMAND_NO_DUPLICATE = new ErrorCode(1_064_010_001, "生产需求编号已存在");
    ErrorCode PROD_DEMAND_STATUS_INVALID = new ErrorCode(1_064_010_002, "生产需求当前状态不允许该操作");
    ErrorCode PROD_DEMAND_HAS_PROD_ORDER = new ErrorCode(1_064_010_003, "生产需求已生成生产工单，不能删除");
    ErrorCode PROD_DEMAND_TO_ORDER_QTY_EXCEEDED = new ErrorCode(1_064_010_004, "本次转单数量超出生产需求的未转单数量");

    // ========== 作业工单 ==========
    ErrorCode WORK_ORDER_NOT_EXISTS = new ErrorCode(1_064_011_000, "作业工单不存在");
    ErrorCode WORK_ORDER_NO_DUPLICATE = new ErrorCode(1_064_011_001, "作业工单号已存在");
    ErrorCode WORK_ORDER_STATUS_INVALID = new ErrorCode(1_064_011_002, "作业工单当前状态不允许该操作");
    ErrorCode WORK_ORDER_HAS_OP_TASK = new ErrorCode(1_064_011_003, "作业工单下存在工序任务，不能删除");
    ErrorCode WORK_ORDER_CONCURRENT_CONFLICT =
            new ErrorCode(1_064_011_004, "作业工单正被其他操作更新，请刷新后重试");

    // ========== 齐套快照 ==========
    ErrorCode KIT_SNAPSHOT_NOT_EXISTS = new ErrorCode(1_064_012_000, "齐套快照不存在");
    ErrorCode KIT_CHECK_SCOPE_TARGET_REQUIRED =
            new ErrorCode(1_064_012_001, "齐套检查范围({})不合法，必须是 PROD_ORDER、WORK_ORDER 或 OP_TASK");
    ErrorCode KIT_CHECK_BOM_DEPTH_EXCEEDED =
            new ErrorCode(1_064_012_002, "虚拟件展开层数超过 {} 层，请检查 BOM 是否存在循环引用");
    ErrorCode KIT_CHECK_PHANTOM_BOM_MISSING =
            new ErrorCode(1_064_012_003, "虚拟件物料({})没有生效 BOM 或 BOM 无明细，无法向下展开子件");

    // ========== 领退补料申请 ==========
    ErrorCode MATERIAL_REQ_NOT_EXISTS = new ErrorCode(1_064_013_000, "领退补料申请不存在");
    ErrorCode MATERIAL_REQ_LINE_NOT_EXISTS = new ErrorCode(1_064_013_001, "领退补料申请明细不存在");
    ErrorCode MATERIAL_REQ_NO_DUPLICATE = new ErrorCode(1_064_013_002, "领退补料申请单号已存在");
    ErrorCode MATERIAL_REQ_LINE_NO_DUPLICATE = new ErrorCode(1_064_013_003, "同一申请下行号已存在");
    ErrorCode MATERIAL_REQ_STATUS_INVALID = new ErrorCode(1_064_013_004, "领退补料申请当前状态不允许该操作");
    ErrorCode MATERIAL_REQ_RETURN_BATCH_REQUIRED = new ErrorCode(1_064_013_005, "退料明细必须指定批次");
    ErrorCode MATERIAL_REQ_NOT_APPROVED = new ErrorCode(1_064_013_012, "超发领料申请未审批通过，不能确认出库");
    ErrorCode MATERIAL_REQ_LINE_EMPTY =
            new ErrorCode(1_064_013_006, "领退补料申请没有明细行，请检查 BOM 或工序用料配置");
    ErrorCode MATERIAL_REQ_AVAILABLE_QTY_INSUFFICIENT =
            new ErrorCode(1_064_013_007, "物料({})可用量不足，本次申请 {}，当前可用 {}");
    ErrorCode MATERIAL_REQ_WAREHOUSE_REQUIRED =
            new ErrorCode(1_064_013_008, "领退补料申请必须指定库存组织与仓库");
    ErrorCode MATERIAL_REQ_LOC_REQUIRED =
            new ErrorCode(1_064_013_009, "物料({})未指定库位，确认发料前请指定库位");
    ErrorCode MATERIAL_REQ_CONFIRM_QTY_EXCEEDED =
            new ErrorCode(1_064_013_010, "物料({})本次确认 {}，超过剩余待处理量 {}");
    ErrorCode MATERIAL_REQ_CONFIRM_QTY_EMPTY =
            new ErrorCode(1_064_013_011, "本次确认数量全部为零，没有可发料的明细行");

    // ========== 生产报工 ==========
    ErrorCode PROD_REPORT_NOT_EXISTS = new ErrorCode(1_064_014_000, "生产报工记录不存在");
    ErrorCode PROD_REPORT_NO_DUPLICATE = new ErrorCode(1_064_014_001, "报工单号已存在");
    ErrorCode PROD_REPORT_STATUS_INVALID = new ErrorCode(1_064_014_002, "报工记录当前状态不允许该操作");
    ErrorCode PROD_REPORT_QTY_EMPTY = new ErrorCode(1_064_014_003, "合格、报废、返工数量不能全部为零");
    ErrorCode PROD_REPORT_TIME_INVALID = new ErrorCode(1_064_014_004, "作业结束时间不能早于开始时间");
    ErrorCode PROD_REPORT_QTY_EXCEEDED =
            new ErrorCode(1_064_014_005, "工序({})本次报工合格 {} 报废 {}，累计后 {} 超过计划数量 {}");
    ErrorCode PROD_REPORT_CANCEL_OP_TASK_DONE =
            new ErrorCode(1_064_014_006, "工序({})已完工，它的报工不允许取消");

    // ========== 返工任务 ==========
    ErrorCode REWORK_NOT_EXISTS = new ErrorCode(1_064_015_000, "返工任务不存在");
    ErrorCode REWORK_NO_DUPLICATE = new ErrorCode(1_064_015_001, "返工任务编号已存在");
    ErrorCode REWORK_STATUS_INVALID = new ErrorCode(1_064_015_002, "返工任务当前状态不允许该操作");

    // ========== 质检任务 ==========
    ErrorCode QC_TASK_NOT_EXISTS = new ErrorCode(1_064_016_000, "质检任务不存在");
    ErrorCode QC_TASK_NO_DUPLICATE = new ErrorCode(1_064_016_001, "质检任务号已存在");
    ErrorCode QC_TASK_STATUS_INVALID = new ErrorCode(1_064_016_002, "质检任务当前状态不允许该操作");
    ErrorCode QC_TASK_SAMPLE_QTY_INVALID = new ErrorCode(1_064_016_003, "抽样数量不能大于送检数量");
    ErrorCode QC_TASK_HAS_RESULT = new ErrorCode(1_064_016_004, "质检任务下存在检验结果，不能删除");
    ErrorCode QC_TASK_PLAN_NOT_PUBLISHED = new ErrorCode(1_064_016_005, "只能引用已发布的检验方案");

    // ========== 缺陷记录 ==========
    ErrorCode DEFECT_NOT_EXISTS = new ErrorCode(1_064_017_000, "缺陷记录不存在");
    ErrorCode DEFECT_RESULT_TASK_NOT_MATCH = new ErrorCode(1_064_017_001, "检验结果不属于所选质检任务");

    // ========== 不合格品处置单 ==========
    ErrorCode DISPOSITION_NOT_EXISTS = new ErrorCode(1_064_018_000, "不合格品处置单不存在");
    ErrorCode DISPOSITION_NO_DUPLICATE = new ErrorCode(1_064_018_001, "不合格处置单号已存在");
    ErrorCode DISPOSITION_STATUS_INVALID = new ErrorCode(1_064_018_002, "处置单当前状态不允许该操作");
    ErrorCode DISPOSITION_QTY_EXCEEDED = new ErrorCode(1_064_018_003, "处置数量不能大于质检任务的不合格数量");
    ErrorCode DISPOSITION_APPROVER_REQUIRED = new ErrorCode(1_064_018_004, "审批通过或驳回时必须填写审批人");

    // ========== 在制流转流水 ==========
    ErrorCode WIP_TXN_NOT_EXISTS = new ErrorCode(1_064_019_000, "在制流转流水不存在");
    ErrorCode WIP_TXN_NO_DUPLICATE = new ErrorCode(1_064_019_001, "在制事务编号已存在");

    // ========== 检验项目 ==========
    ErrorCode QC_ITEM_NOT_EXISTS = new ErrorCode(1_064_020_000, "检验项目不存在");
    ErrorCode QC_ITEM_CODE_DUPLICATE = new ErrorCode(1_064_020_001, "同一检验方案下检验项编码已存在");
    ErrorCode QC_ITEM_LIMIT_REQUIRED = new ErrorCode(1_064_020_002, "数值型检验项必须给出上限或下限");
    ErrorCode QC_ITEM_LIMIT_INVALID = new ErrorCode(1_064_020_003, "数值上限不能小于下限");
    ErrorCode QC_ITEM_PLAN_NOT_DRAFT = new ErrorCode(1_064_020_004, "只有草稿状态的检验方案允许维护检验项");

    // ========== 检验方案 ==========
    ErrorCode QC_PLAN_NOT_EXISTS = new ErrorCode(1_064_021_000, "检验方案不存在");
    ErrorCode QC_PLAN_CODE_DUPLICATE = new ErrorCode(1_064_021_001, "检验方案编码与版本已存在");
    ErrorCode QC_PLAN_STATUS_INVALID = new ErrorCode(1_064_021_002, "检验方案当前状态不允许该操作");
    ErrorCode QC_PLAN_SAMPLE_VALUE_REQUIRED = new ErrorCode(1_064_021_003, "固定数量或按比例抽检必须给出抽样值");
    ErrorCode QC_PLAN_AQL_REQUIRED = new ErrorCode(1_064_021_004, "AQL 抽检必须给出接收质量限");
    ErrorCode QC_PLAN_HAS_RULE = new ErrorCode(1_064_021_005, "检验方案已被质检规则引用，不能删除");

    // ========== 检验结果 ==========
    ErrorCode QC_RESULT_NOT_EXISTS = new ErrorCode(1_064_022_000, "检验结果不存在");
    ErrorCode QC_RESULT_SAMPLE_DUPLICATE = new ErrorCode(1_064_022_001, "同一质检任务下该检验项的样本序号已存在");
    ErrorCode QC_RESULT_CONCLUSION_CONFLICT = new ErrorCode(1_064_022_002, "项目结论与是否合格标记不一致");
    ErrorCode QC_RESULT_TASK_DONE = new ErrorCode(1_064_022_003, "质检任务已完成，不能再维护检验结果");
    ErrorCode QC_RESULT_HAS_DEFECT = new ErrorCode(1_064_022_004, "检验结果下存在缺陷记录，不能删除");

    // ========== 质检触发规则 ==========
    ErrorCode QC_RULE_NOT_EXISTS = new ErrorCode(1_064_023_000, "质检触发规则不存在");
    ErrorCode QC_RULE_CODE_DUPLICATE = new ErrorCode(1_064_023_001, "质检规则编码已存在");
    ErrorCode QC_RULE_HAS_TASK = new ErrorCode(1_064_023_002, "质检规则已被质检任务引用，不能删除");

    // ========== 派工任务 ==========
    ErrorCode DISPATCH_TASK_NOT_EXISTS = new ErrorCode(1_064_024_000, "派工任务不存在");
    ErrorCode DISPATCH_TASK_NO_DUPLICATE = new ErrorCode(1_064_024_001, "派工单号已存在");
    ErrorCode DISPATCH_TASK_STATUS_INVALID = new ErrorCode(1_064_024_002, "派工任务当前状态不允许该操作");
    ErrorCode DISPATCH_TASK_OPERATOR_REQUIRED = new ErrorCode(1_064_024_003, "作业人员与作业班组至少填一个");
    ErrorCode DISPATCH_TASK_TIME_INVALID = new ErrorCode(1_064_024_004, "计划结束时间不能早于计划开始时间");

    // ========== 工序跳过记录 ==========
    ErrorCode OP_SKIP_RECORD_NOT_EXISTS = new ErrorCode(1_064_025_000, "工序跳过记录不存在");
    ErrorCode OP_SKIP_NOT_ALLOWED = new ErrorCode(1_064_025_001, "该工序任务的跳序策略不允许跳过");
    ErrorCode OP_SKIP_APPROVER_REQUIRED = new ErrorCode(1_064_025_002, "审批通过或驳回时必须填写审批人");
    ErrorCode OP_SKIP_APPROVE_REQUIRED = new ErrorCode(1_064_025_003, "该工序任务跳序需要审批通过后才能生效");
    ErrorCode OP_SKIP_REASON_REQUIRED = new ErrorCode(1_064_025_004, "跳序必须填写原因类型与原因说明");
    ErrorCode OP_SKIP_EFFECT_REMARK_REQUIRED =
            new ErrorCode(1_064_025_005, "需审批的跳序必须填写对后续工序、质检及排程的影响说明");
    ErrorCode OP_SKIP_RECORD_STATUS_INVALID =
            new ErrorCode(1_064_025_006, "工序跳过记录当前状态不允许该操作");

    // ========== RGV 工位流转任务 ==========
    ErrorCode RGV_TASK_NOT_EXISTS = new ErrorCode(1_064_026_000, "RGV 工位流转任务不存在");
    ErrorCode RGV_TASK_NO_DUPLICATE = new ErrorCode(1_064_026_001, "RGV 任务号已存在");
    ErrorCode RGV_TASK_EXTERNAL_NO_DUPLICATE = new ErrorCode(1_064_026_002, "同一外部系统下外部任务号已存在");
    ErrorCode RGV_TASK_STATUS_INVALID = new ErrorCode(1_064_026_003, "RGV 任务当前状态不允许该操作");
    ErrorCode RGV_TASK_SAME_STATION = new ErrorCode(1_064_026_004, "来源工位与目标工位不能相同");

    // ========== 质检规则触发（质量子域 1_064_100 段） ==========
    ErrorCode QC_PLAN_NOT_PUBLISHED = new ErrorCode(1_064_100_001, "质检规则引用的检验方案未发布或已停用");
    // ========== 检验方案领域动作（质量子域 1_064_101 段） ==========
    ErrorCode QC_PLAN_PUBLISH_WITHOUT_ITEM = new ErrorCode(1_064_101_001, "检验方案没有检验项，不能发布");
    ErrorCode QC_PLAN_IN_USE = new ErrorCode(1_064_101_002, "检验方案已被启用中的质检规则引用，不能停用");
    // ========== 质检任务生成（质量子域 1_064_102 段） ==========
    ErrorCode QC_TASK_PLAN_ITEM_EMPTY =
            new ErrorCode(1_064_102_001, "检验方案({})下没有启用的检验项，无法生成质检任务");
    ErrorCode QC_TASK_CONCURRENT_CONFLICT =
            new ErrorCode(1_064_102_002, "质检任务正被其他操作更新，请刷新后重试");
    ErrorCode QC_TASK_NOT_INSPECTING =
            new ErrorCode(1_064_102_003, "质检任务({})不在检验中，不能录入检验结果");
    // ========== 缺陷录入（质量子域 1_064_103 段） ==========
    ErrorCode DEFECT_CODE_NOT_AVAILABLE =
            new ErrorCode(1_064_103_001, "缺陷代码({})不在当前物料分类与工序的可选范围内");
    ErrorCode QC_TASK_REJECT_WITHOUT_DEFECT =
            new ErrorCode(1_064_103_002, "拒收结论必须至少有一条缺陷记录，请先录入缺陷");
    // ========== FQC 完工检验（质量子域 1_064_104 段） ==========
    ErrorCode FINISH_INBOUND_QC_NOT_PASSED =
            new ErrorCode(1_064_104_001, "完工检验未通过或合格数量不足，不能完工入库");
    // ========== 不合格处置执行（质量子域 1_064_105 段） ==========
    ErrorCode DISPOSITION_HANDLER_NOT_FOUND =
            new ErrorCode(1_064_105_001, "处置类型没有对应执行器");
    ErrorCode DISPOSITION_TARGET_EMPTY =
            new ErrorCode(1_064_105_002, "处置执行完成但未返回联动业务凭据");
    ErrorCode DISPOSITION_CONCESSION_WITHOUT_REASON =
            new ErrorCode(1_064_105_003, "让步接收必须填写依据说明");

    // ========== 批次追溯（1_064_027 段） ==========
    ErrorCode TRACE_START_TYPE_INVALID =
            new ErrorCode(1_064_027_001, "追溯起点类型({})不合法，必须是 LOT、PROD_ORDER 或 SALES_OUT");
    ErrorCode TRACE_START_NOT_FOUND =
            new ErrorCode(1_064_027_002, "追溯起点({}:{})不存在或没有可追溯的链路");

}

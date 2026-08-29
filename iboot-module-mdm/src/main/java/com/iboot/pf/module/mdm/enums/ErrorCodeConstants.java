package com.iboot.pf.module.mdm.enums;

import com.iboot.pf.framework.common.exception.ErrorCode;

/** MDM 错误码，使用 1-062 段。 */
public interface ErrorCodeConstants {

    ErrorCode INV_ORG_NOT_EXISTS = new ErrorCode(1_062_001_000, "库存组织不存在");
    ErrorCode INV_ORG_CODE_DUPLICATE = new ErrorCode(1_062_001_001, "库存组织编码重复");
    ErrorCode UOM_NOT_EXISTS = new ErrorCode(1_062_002_000, "计量单位不存在");
    ErrorCode UOM_CODE_DUPLICATE = new ErrorCode(1_062_002_001, "计量单位编码重复");
    ErrorCode MAT_CATEGORY_NOT_EXISTS = new ErrorCode(1_062_003_000, "物料分类不存在");
    ErrorCode MAT_CATEGORY_CODE_DUPLICATE = new ErrorCode(1_062_003_001, "物料分类编码重复");
    ErrorCode MAT_CATEGORY_PARENT_INVALID = new ErrorCode(1_062_003_002, "物料分类父级不存在或不能设置为自身");
    ErrorCode MATERIAL_NOT_EXISTS = new ErrorCode(1_062_004_000, "物料不存在");
    ErrorCode MATERIAL_CODE_DUPLICATE = new ErrorCode(1_062_004_001, "物料编码重复");
    ErrorCode MATERIAL_CATEGORY_NOT_EXISTS = new ErrorCode(1_062_004_002, "物料分类不存在");
    ErrorCode MATERIAL_UOM_NOT_EXISTS = new ErrorCode(1_062_004_003, "基准计量单位不存在");

    ErrorCode PLANT_NOT_EXISTS = new ErrorCode(1_062_005_000, "工厂不存在");
    ErrorCode WORKSHOP_NOT_EXISTS = new ErrorCode(1_062_006_000, "车间不存在");
    ErrorCode WC_NOT_EXISTS = new ErrorCode(1_062_007_000, "工作中心不存在");
    ErrorCode RES_NOT_EXISTS = new ErrorCode(1_062_008_000, "资源不存在");
    ErrorCode RES_CALENDAR_NOT_EXISTS = new ErrorCode(1_062_009_000, "资源日历不存在");
    ErrorCode SHIFT_NOT_EXISTS = new ErrorCode(1_062_010_000, "班次不存在");
    ErrorCode CUSTOMER_NOT_EXISTS = new ErrorCode(1_062_011_000, "客户不存在");
    ErrorCode SUPPLIER_NOT_EXISTS = new ErrorCode(1_062_012_000, "供应商不存在");
    ErrorCode PROJECT_NOT_EXISTS = new ErrorCode(1_062_013_000, "项目不存在");
    ErrorCode PROJECT_TASK_NOT_EXISTS = new ErrorCode(1_062_014_000, "项目任务不存在");
    ErrorCode OP_NOT_EXISTS = new ErrorCode(1_062_015_000, "工序不存在");
    ErrorCode BOM_NOT_EXISTS = new ErrorCode(1_062_016_000, "物料清单不存在");
    ErrorCode BOM_LINE_NOT_EXISTS = new ErrorCode(1_062_016_001, "物料清单明细不存在");
    ErrorCode BOM_STATUS_INVALID = new ErrorCode(1_062_016_002, "BOM 状态只能是启用或停用");
    ErrorCode BOM_DATE_INVALID = new ErrorCode(1_062_016_003, "BOM 失效日期不能早于生效日期");
    ErrorCode BOM_ENABLED_LOCKED = new ErrorCode(1_062_016_004, "已启用的 BOM 不允许修改或删除");
    ErrorCode BOM_ENABLE_WITHOUT_LINE = new ErrorCode(1_062_016_005, "启用 BOM 前至少需要一条明细");
    ErrorCode ROUTE_NOT_EXISTS = new ErrorCode(1_062_017_000, "工艺路线不存在");
    ErrorCode ROUTE_OP_NOT_EXISTS = new ErrorCode(1_062_017_001, "工艺路线工序不存在");
    ErrorCode ROUTE_OP_MAT_NOT_EXISTS = new ErrorCode(1_062_017_002, "工序用料不存在");
    ErrorCode ROUTE_STATUS_INVALID = new ErrorCode(1_062_017_003, "工艺路线状态只能是启用或停用");
    ErrorCode ROUTE_DATE_INVALID = new ErrorCode(1_062_017_004, "工艺路线失效日期不能早于生效日期");
    ErrorCode ROUTE_ENABLED_LOCKED = new ErrorCode(1_062_017_005, "已启用的工艺路线不允许修改或删除");
    ErrorCode ROUTE_ENABLE_WITHOUT_OP = new ErrorCode(1_062_017_006, "启用工艺路线前至少需要一道工序");
    ErrorCode ROUTE_OP_SORT_INVALID = new ErrorCode(1_062_017_007, "工艺路线工序排序数据不完整或不属于当前路线");
    ErrorCode ROUTE_VERSION_DUPLICATE = new ErrorCode(1_062_017_008, "该物料下已存在相同版本的工艺路线");
    ErrorCode MAT_ALT_NOT_EXISTS = new ErrorCode(1_062_018_000, "替代料不存在");
    ErrorCode MAT_SAFETY_STOCK_REQUIRED = new ErrorCode(1_062_018_002, "备库生产（MTS）的物料必须填写安全库存");
    ErrorCode UOM_CONV_NOT_EXISTS = new ErrorCode(1_062_019_000, "单位换算不存在");
    ErrorCode UOM_CONV_SAME_UOM = new ErrorCode(1_062_019_001, "来源计量单位与目标计量单位不能相同");
    ErrorCode MAT_ALT_SAME_MAT = new ErrorCode(1_062_018_001, "物料不能将自身设置为替代料");

    // ========== 工时模型 ==========
    ErrorCode OP_TIME_MODEL_INVALID = new ErrorCode(1_062_015_001, "按件累加工时必须填写单件加工工时");
    ErrorCode OP_DELETE_REFERENCED = new ErrorCode(1_062_015_002, "工序已被工艺路线引用，不能删除");

    // ========== 资源能力 ==========
    ErrorCode RES_CAPABILITY_NOT_EXISTS = new ErrorCode(1_062_020_000, "资源能力不存在");
    ErrorCode RES_CAPABILITY_DUPLICATE = new ErrorCode(1_062_020_001, "同一资源与工序的能力记录已存在");

    // ========== 日历模板 ==========
    ErrorCode CALENDAR_NOT_EXISTS = new ErrorCode(1_062_021_000, "日历不存在");
    ErrorCode CALENDAR_CODE_DUPLICATE = new ErrorCode(1_062_021_001, "日历编码重复");
    ErrorCode CALENDAR_RULE_NOT_EXISTS = new ErrorCode(1_062_021_002, "日历规则不存在");
    ErrorCode CALENDAR_RULE_DUPLICATE = new ErrorCode(1_062_021_003, "同一日历下该星期与班次的规则已存在");
    ErrorCode CALENDAR_EXCEPTION_NOT_EXISTS = new ErrorCode(1_062_021_004, "日历例外不存在");
    ErrorCode CALENDAR_EXCEPTION_DUPLICATE = new ErrorCode(1_062_021_005, "同一日历下该日期与班次的例外已存在");
    ErrorCode CALENDAR_GENERATE_DATE_INVALID = new ErrorCode(1_062_021_006, "生成日期区间不合法");
    ErrorCode CALENDAR_EXCEPTION_SHIFT_REQUIRED = new ErrorCode(1_062_021_007, "加班与计划保养例外必须指定班次");
    ErrorCode CALENDAR_EXCEPTION_MINUTES_REQUIRED = new ErrorCode(1_062_021_008, "加班与计划保养例外必须填写可用分钟数");
    ErrorCode RES_DELETE_REFERENCED = new ErrorCode(1_062_021_009, "资源已配置能力数据，不能删除");

    // ========== ESOP ==========
    ErrorCode ROUTE_OP_ESOP_NOT_EXISTS = new ErrorCode(1_062_022_000, "ESOP 指导书不存在");
    ErrorCode ROUTE_OP_ESOP_CONTENT_REQUIRED = new ErrorCode(1_062_022_001, "作业要点、工艺参数、附件至少填写一项");
    ErrorCode ROUTE_OP_ESOP_SEQ_DUPLICATE = new ErrorCode(1_062_022_002, "同一工序下的作业步骤顺序重复");

    // ========== 缺陷代码 ==========
    ErrorCode DEFECT_CODE_NOT_EXISTS = new ErrorCode(1_062_023_000, "缺陷代码不存在");
    ErrorCode DEFECT_CODE_DUPLICATE = new ErrorCode(1_062_023_001, "缺陷编码重复");
    ErrorCode DEFECT_CODE_PARENT_INVALID = new ErrorCode(1_062_023_002, "缺陷分类父级不存在或不能设置为自身");
    ErrorCode DEFECT_CODE_PARENT_IS_CHILD = new ErrorCode(1_062_023_003, "不能将缺陷代码设置为自身的下级");
    ErrorCode DEFECT_CODE_HAS_CHILDREN = new ErrorCode(1_062_023_004, "缺陷代码存在下级，不能删除");
    ErrorCode DEFECT_CODE_OP_NOT_EXISTS = new ErrorCode(1_062_023_005, "缺陷代码适用工序不存在");
    ErrorCode DEFECT_CODE_MAT_CAT_NOT_EXISTS = new ErrorCode(1_062_023_006, "缺陷代码适用物料分类不存在");

    // ========== 班组与技能 ==========
    ErrorCode TEAM_NOT_EXISTS = new ErrorCode(1_062_024_000, "班组不存在");
    ErrorCode TEAM_CODE_DUPLICATE = new ErrorCode(1_062_024_001, "班组编码重复");
    ErrorCode TEAM_MEMBER_NOT_EXISTS = new ErrorCode(1_062_024_002, "班组成员不存在");
    ErrorCode TEAM_MEMBER_DUPLICATE = new ErrorCode(1_062_024_003, "该成员已加入班组");
    ErrorCode TEAM_MEMBER_USER_NOT_EXISTS = new ErrorCode(1_062_024_004, "班组成员用户不存在");
    ErrorCode SKILL_NOT_EXISTS = new ErrorCode(1_062_025_000, "技能不存在");
    ErrorCode SKILL_CODE_DUPLICATE = new ErrorCode(1_062_025_001, "技能编码重复");
    ErrorCode OP_SKILL_REQ_NOT_EXISTS = new ErrorCode(1_062_025_002, "工序技能要求不存在");
    ErrorCode OP_SKILL_REQ_DUPLICATE = new ErrorCode(1_062_025_003, "同一工序的该技能要求已存在");
    ErrorCode MEMBER_SKILL_NOT_EXISTS = new ErrorCode(1_062_025_004, "人员技能不存在");
    ErrorCode MEMBER_SKILL_DUPLICATE = new ErrorCode(1_062_025_005, "该人员的该技能记录已存在");
    ErrorCode MEMBER_SKILL_DATE_INVALID = new ErrorCode(1_062_025_006, "人员技能失效日期不能早于生效日期");
    ErrorCode SKILL_DELETE_REFERENCED = new ErrorCode(1_062_025_007, "技能已被工序技能要求引用，不能删除");

    // ========== 工装模具刀具 ==========
    ErrorCode TOOL_NOT_EXISTS = new ErrorCode(1_062_026_000, "工装不存在");
    ErrorCode TOOL_CODE_DUPLICATE = new ErrorCode(1_062_026_001, "工装编码重复");
    ErrorCode ROUTE_OP_TOOL_NOT_EXISTS = new ErrorCode(1_062_026_002, "工序工装不存在");
    ErrorCode ROUTE_OP_TOOL_DUPLICATE = new ErrorCode(1_062_026_003, "同一工序的该工装记录已存在");
    ErrorCode TOOL_DELETE_REFERENCED = new ErrorCode(1_062_026_004, "工装已被工序引用，不能删除");

    // ========== 换型矩阵 ==========
    ErrorCode RES_CHANGEOVER_NOT_EXISTS = new ErrorCode(1_062_027_000, "换型矩阵不存在");
    ErrorCode RES_CHANGEOVER_DUPLICATE = new ErrorCode(1_062_027_001, "同一资源下该换型分组组合已存在");
    ErrorCode RES_CHANGEOVER_SAME_GROUP = new ErrorCode(1_062_027_002, "换出与换入分组相同时无需配置换型");

    // 保持与生成代码一致；后续服务重构时统一为 MAT_CATEGORY_* 命名。
    ErrorCode MAT_CAT_NOT_EXISTS = new ErrorCode(1_062_003_000, "物料分类不存在");
    ErrorCode MAT_CAT_EXITS_CHILDREN = new ErrorCode(1_062_003_003, "物料分类存在下级分类，不能删除");
    ErrorCode MAT_CAT_NAME_DUPLICATE = new ErrorCode(1_062_003_004, "同级物料分类名称重复");
    ErrorCode MAT_CAT_PARENT_ERROR = new ErrorCode(1_062_003_005, "不能将物料分类设置为自身的下级分类");
    ErrorCode MAT_CAT_PARENT_NOT_EXITS = new ErrorCode(1_062_003_006, "父级物料分类不存在");
    ErrorCode MAT_CAT_PARENT_IS_CHILD = new ErrorCode(1_062_003_007, "父级物料分类不能为自身");
    ErrorCode MAT_NOT_EXISTS = new ErrorCode(1_062_004_000, "物料不存在");

    ErrorCode MASTER_TYPE_INVALID = new ErrorCode(1_062_000_001, "主数据类型不存在");
    ErrorCode MASTER_DATA_NOT_EXISTS = new ErrorCode(1_062_000_002, "主数据不存在");
    ErrorCode MASTER_DATA_CODE_DUPLICATE = new ErrorCode(1_062_000_003, "主数据唯一编码重复");
    ErrorCode MASTER_DATA_REFERENCE_INVALID = new ErrorCode(1_062_000_004, "关联主数据不存在");
    ErrorCode MASTER_DATA_DELETE_USED = new ErrorCode(1_062_000_005, "主数据已被引用，不能删除");
    ErrorCode MASTER_DATA_FIELD_INVALID = new ErrorCode(1_062_000_006, "主数据字段不合法");

}

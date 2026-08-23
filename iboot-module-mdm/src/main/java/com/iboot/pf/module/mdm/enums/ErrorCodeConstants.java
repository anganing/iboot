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
    ErrorCode BOM_STATUS_INVALID = new ErrorCode(1_062_016_002, "BOM 状态只能为 DRAFT 或 PUBLISHED");
    ErrorCode BOM_DATE_INVALID = new ErrorCode(1_062_016_003, "BOM 失效日期不能早于生效日期");
    ErrorCode BOM_PUBLISHED_LOCKED = new ErrorCode(1_062_016_004, "已发布 BOM 不允许修改或删除");
    ErrorCode BOM_PUBLISH_WITHOUT_LINE = new ErrorCode(1_062_016_005, "发布 BOM 前至少需要一条明细");
    ErrorCode ROUTE_NOT_EXISTS = new ErrorCode(1_062_017_000, "工艺路线不存在");
    ErrorCode ROUTE_OP_NOT_EXISTS = new ErrorCode(1_062_017_001, "工艺路线工序不存在");
    ErrorCode ROUTE_OP_MAT_NOT_EXISTS = new ErrorCode(1_062_017_002, "工序用料不存在");
    ErrorCode ROUTE_STATUS_INVALID = new ErrorCode(1_062_017_003, "工艺路线状态只能为 DRAFT 或 PUBLISHED");
    ErrorCode ROUTE_DATE_INVALID = new ErrorCode(1_062_017_004, "工艺路线失效日期不能早于生效日期");
    ErrorCode ROUTE_PUBLISHED_LOCKED = new ErrorCode(1_062_017_005, "已发布工艺路线不允许修改或删除");
    ErrorCode ROUTE_PUBLISH_WITHOUT_OP = new ErrorCode(1_062_017_006, "发布工艺路线前至少需要一道工序");
    ErrorCode MAT_ALT_NOT_EXISTS = new ErrorCode(1_062_018_000, "替代料不存在");
    ErrorCode UOM_CONV_NOT_EXISTS = new ErrorCode(1_062_019_000, "单位换算不存在");
    ErrorCode UOM_CONV_SAME_UOM = new ErrorCode(1_062_019_001, "来源计量单位与目标计量单位不能相同");
    ErrorCode MAT_ALT_SAME_MAT = new ErrorCode(1_062_018_001, "物料不能将自身设置为替代料");

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

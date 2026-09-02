package com.iboot.pf.module.aps.solver.constraint;

/**
 * 单条约束的登记信息。
 *
 * @param constraintName 约束名，与 {@link ApsConstraintNames} 的常量一致
 * @param displayName 中文名，页面与快照展示用
 * @param category 约束类别
 * @param description 业务说明：这条罚什么、权重调大调小会怎样
 */
public record ApsConstraintDefinition(String constraintName, String displayName,
                                     ApsConstraintCategoryEnum category, String description) {
}

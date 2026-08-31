package com.iboot.pf.module.aps.enums;

/**
 * APS 跨模块命令类型。
 *
 * <p>写进 {@code aps_command_record.command_type}，配合幂等键做重复下发拦截。
 * 之前这些值以字面量散落在各个 Service 里，改一个名字就会和历史记录对不上。
 */
public enum ApsCommandTypeEnum {

    /** 创建排程场景。 */
    CREATE_SCENARIO,
    /** 复制排程场景。 */
    COPY_SCENARIO,
    /** 关闭排程场景。 */
    CLOSE_SCENARIO,
    /** 生成排程版本。 */
    GENERATE_VERSION,
    /** 确认排程版本。 */
    CONFIRM_VERSION,
    /** 发布排程版本。 */
    PUBLISH_VERSION,
    /** 替换排程版本。 */
    REPLACE_VERSION,
    /** 锁定排程任务。 */
    LOCK_TASK,
    /** 解锁排程任务。 */
    UNLOCK_TASK,
    /** 刷新物料投影。 */
    REFRESH_MATERIAL_PROJECTION,
    /** 刷新库存投影。 */
    REFRESH_INVENTORY_PROJECTION,
    /** 刷新工单投影。 */
    REFRESH_ORDER_PROJECTION,
    /** 刷新资源投影。 */
    REFRESH_RESOURCE_PROJECTION,
    /** 刷新缺料清单。 */
    REFRESH_SHORTAGE,
    /** 刷新资源负荷。 */
    REFRESH_LOAD;

}

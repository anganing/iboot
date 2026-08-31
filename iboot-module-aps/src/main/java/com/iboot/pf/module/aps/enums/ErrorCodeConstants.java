package com.iboot.pf.module.aps.enums;

import com.iboot.pf.framework.common.exception.ErrorCode;

/**
 * APS 错误码常量。
 *
 * <p>编号段按业务对象划分：场景 1_065_001、版本 1_065_002、任务 1_065_003、投影 1_065_004、
 * 缺料 1_065_005、负荷 1_065_006、任务依赖与锁定 1_065_007、策略 1_065_008、求解快照 1_065_009、
 * 跨模块主数据校验 1_065_010。
 */
public interface ErrorCodeConstants {

    ErrorCode SCENARIO_NOT_EXISTS = new ErrorCode(1_065_001_000, "排程场景不存在");
    ErrorCode SCENARIO_CLOSED = new ErrorCode(1_065_001_001, "排程场景已关闭");
    ErrorCode SCENARIO_TIME_INVALID = new ErrorCode(1_065_001_002, "计划窗口开始时间必须早于结束时间");
    ErrorCode SCENARIO_CODE_DUPLICATE = new ErrorCode(1_065_001_003, "排程场景编码已存在");
    ErrorCode SCENARIO_HAS_VERSION = new ErrorCode(1_065_001_004, "排程场景已生成版本，不允许删除");
    ErrorCode VERSION_NOT_EXISTS = new ErrorCode(1_065_002_000, "排程版本不存在");
    ErrorCode VERSION_STATUS_INVALID = new ErrorCode(1_065_002_001, "排程版本当前状态不允许该操作");
    ErrorCode VERSION_PUBLISHED_READ_ONLY = new ErrorCode(1_065_002_002, "已发布版本的排程结果不允许修改");
    ErrorCode VERSION_NO_DUPLICATE = new ErrorCode(1_065_002_003, "同一场景下的排程版本号已存在");
    ErrorCode TASK_NOT_EXISTS = new ErrorCode(1_065_003_000, "排程任务不存在");
    ErrorCode TASK_TIME_INVALID = new ErrorCode(1_065_003_001, "任务开始时间必须早于结束时间");
    ErrorCode TASK_RESOURCE_OVERLAP = new ErrorCode(1_065_003_002, "同一资源上的任务时间不允许重叠");
    ErrorCode TASK_CANNOT_UNLOCK = new ErrorCode(1_065_003_003, "已开工任务不允许解锁");
    ErrorCode TASK_NO_DUPLICATE = new ErrorCode(1_065_003_004, "排程任务编号已存在");
    ErrorCode TASK_OP_DUPLICATE = new ErrorCode(1_065_003_005, "同一版本下的工序任务已排定");
    ErrorCode TASK_HAS_DEP = new ErrorCode(1_065_003_006, "排程任务存在依赖关系，不允许删除");
    ErrorCode TASK_HAS_LOCK = new ErrorCode(1_065_003_007, "排程任务存在锁定记录，不允许删除");
    ErrorCode PROJECTION_SOURCE_UNAVAILABLE = new ErrorCode(1_065_004_000, "投影来源 API 尚未就绪");
    ErrorCode PROJECTION_MAT_NOT_EXISTS = new ErrorCode(1_065_004_001, "物料投影不存在");
    ErrorCode PROJECTION_INV_NOT_EXISTS = new ErrorCode(1_065_004_002, "库存投影不存在");
    ErrorCode PROJECTION_ORDER_NOT_EXISTS = new ErrorCode(1_065_004_003, "工单投影不存在");
    ErrorCode PROJECTION_RESOURCE_NOT_EXISTS = new ErrorCode(1_065_004_004, "资源投影不存在");
    ErrorCode SHORTAGE_NOT_EXISTS = new ErrorCode(1_065_005_000, "缺料清单不存在");
    ErrorCode SHORTAGE_QTY_INVALID = new ErrorCode(1_065_005_001, "缺料的可用数量不能大于需求数量");
    ErrorCode LOAD_OVER_CAPACITY = new ErrorCode(1_065_006_000, "资源负荷超过日历可用分钟");
    ErrorCode LOAD_NOT_EXISTS = new ErrorCode(1_065_006_001, "资源负荷不存在");
    ErrorCode LOAD_DUPLICATE = new ErrorCode(1_065_006_002, "同一版本下该资源当日负荷已存在");
    ErrorCode TASK_DEP_NOT_EXISTS = new ErrorCode(1_065_007_000, "排程任务依赖不存在");
    ErrorCode TASK_LOCK_NOT_EXISTS = new ErrorCode(1_065_007_001, "排程任务锁定记录不存在");
    ErrorCode TASK_DEP_DUPLICATE = new ErrorCode(1_065_007_002, "相同的任务依赖已存在");
    ErrorCode TASK_DEP_SELF_REFERENCE = new ErrorCode(1_065_007_003, "任务依赖的前置与后置任务不能相同");
    ErrorCode TASK_DEP_VERSION_MISMATCH = new ErrorCode(1_065_007_004, "任务依赖的前后任务必须属于同一排程版本");
    ErrorCode TASK_LOCK_ALREADY_LOCKED = new ErrorCode(1_065_007_005, "该任务已存在锁定中的记录");
    ErrorCode STRATEGY_NOT_EXISTS = new ErrorCode(1_065_008_000, "排程策略不存在");
    ErrorCode STRATEGY_CODE_DUPLICATE = new ErrorCode(1_065_008_001, "排程策略编码与版本号已存在");
    ErrorCode STRATEGY_HAS_SCENARIO = new ErrorCode(1_065_008_002, "排程策略已被场景引用，不允许删除");
    ErrorCode STRATEGY_DISABLED = new ErrorCode(1_065_008_003, "排程策略未启用，不允许被场景引用");
    ErrorCode SOLVER_SNAPSHOT_NOT_EXISTS = new ErrorCode(1_065_009_000, "求解快照不存在");
    ErrorCode SOLVER_SNAPSHOT_DUPLICATE = new ErrorCode(1_065_009_001, "相同校验值的求解快照已存在");
    ErrorCode PLANT_NOT_EXISTS = new ErrorCode(1_065_010_000, "工厂不存在");
    ErrorCode WORKSHOP_NOT_EXISTS = new ErrorCode(1_065_010_001, "车间不存在");
    ErrorCode RES_NOT_EXISTS = new ErrorCode(1_065_010_002, "生产资源不存在");
    ErrorCode MAT_NOT_EXISTS = new ErrorCode(1_065_010_003, "物料不存在");
    ErrorCode UOM_NOT_EXISTS = new ErrorCode(1_065_010_004, "计量单位不存在");
    ErrorCode PROJECT_NOT_EXISTS = new ErrorCode(1_065_010_005, "项目不存在");
    ErrorCode PROJECT_TASK_NOT_EXISTS = new ErrorCode(1_065_010_006, "项目任务不存在");
    ErrorCode SHIFT_NOT_EXISTS = new ErrorCode(1_065_010_007, "班次不存在");
    ErrorCode WC_NOT_EXISTS = new ErrorCode(1_065_010_008, "工作中心不存在");
}

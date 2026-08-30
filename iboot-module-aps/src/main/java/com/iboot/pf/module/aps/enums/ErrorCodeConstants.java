package com.iboot.pf.module.aps.enums;

import com.iboot.pf.framework.common.exception.ErrorCode;

/** APS 错误码常量。 */
public interface ErrorCodeConstants {

    ErrorCode SCENARIO_NOT_EXISTS = new ErrorCode(1_065_001_000, "排程场景不存在");
    ErrorCode SCENARIO_CLOSED = new ErrorCode(1_065_001_001, "排程场景已关闭");
    ErrorCode SCENARIO_TIME_INVALID = new ErrorCode(1_065_001_002, "计划窗口开始时间必须早于结束时间");
    ErrorCode VERSION_NOT_EXISTS = new ErrorCode(1_065_002_000, "排程版本不存在");
    ErrorCode VERSION_STATUS_INVALID = new ErrorCode(1_065_002_001, "排程版本当前状态不允许该操作");
    ErrorCode VERSION_PUBLISHED_READ_ONLY = new ErrorCode(1_065_002_002, "已发布版本的排程结果不允许修改");
    ErrorCode TASK_NOT_EXISTS = new ErrorCode(1_065_003_000, "排程任务不存在");
    ErrorCode TASK_TIME_INVALID = new ErrorCode(1_065_003_001, "任务开始时间必须早于结束时间");
    ErrorCode TASK_RESOURCE_OVERLAP = new ErrorCode(1_065_003_002, "同一资源上的任务时间不允许重叠");
    ErrorCode TASK_CANNOT_UNLOCK = new ErrorCode(1_065_003_003, "已开工任务不允许解锁");
    ErrorCode PROJECTION_SOURCE_UNAVAILABLE = new ErrorCode(1_065_004_000, "投影来源 API 尚未就绪");
    ErrorCode PROJECTION_MAT_NOT_EXISTS = new ErrorCode(1_065_004_001, "物料投影不存在");
    ErrorCode PROJECTION_INV_NOT_EXISTS = new ErrorCode(1_065_004_002, "库存投影不存在");
    ErrorCode PROJECTION_ORDER_NOT_EXISTS = new ErrorCode(1_065_004_003, "工单投影不存在");
    ErrorCode PROJECTION_RESOURCE_NOT_EXISTS = new ErrorCode(1_065_004_004, "资源投影不存在");
    ErrorCode SHORTAGE_NOT_EXISTS = new ErrorCode(1_065_005_000, "缺料清单不存在");
    ErrorCode LOAD_OVER_CAPACITY = new ErrorCode(1_065_006_000, "资源负荷超过日历可用分钟");
    ErrorCode LOAD_NOT_EXISTS = new ErrorCode(1_065_006_001, "资源负荷不存在");
    ErrorCode TASK_DEP_NOT_EXISTS = new ErrorCode(1_065_007_000, "排程任务依赖不存在");
    ErrorCode TASK_LOCK_NOT_EXISTS = new ErrorCode(1_065_007_001, "排程任务锁定记录不存在");
    ErrorCode STRATEGY_NOT_EXISTS = new ErrorCode(1_065_008_000, "排程策略不存在");
    ErrorCode SOLVER_SNAPSHOT_NOT_EXISTS = new ErrorCode(1_065_009_000, "求解快照不存在");
}

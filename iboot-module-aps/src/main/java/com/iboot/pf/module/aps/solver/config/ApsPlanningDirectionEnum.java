package com.iboot.pf.module.aps.solver.config;

/**
 * 排程方向。
 *
 * <p>本轮只实现正排：从计划起点往未来推。倒排（按交期往前推）需要另一套时间推导逻辑，
 * 且和日历、锁定任务的交互完全不同，不适合混在同一个监听器里，留待后续。
 */
public enum ApsPlanningDirectionEnum {

    /** 正排：从计划起点往后安排。 */
    FORWARD,
    /** 倒排：从交期往前安排。本轮不支持，配置成它会在解析时报错。 */
    BACKWARD

}

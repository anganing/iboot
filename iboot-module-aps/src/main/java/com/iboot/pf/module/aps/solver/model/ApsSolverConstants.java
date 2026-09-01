package com.iboot.pf.module.aps.solver.model;

/** 求解域内部常量。仅在 solver 包内使用，不对外暴露。 */
public interface ApsSolverConstants {

    /** 待排任务值域标识，{@code @ValueRangeProvider} 与 {@code @PlanningListVariable} 靠它对齐。 */
    String TASK_RANGE_ID = "taskRange";

    /** 换型矩阵键的分隔符。 */
    String CHANGEOVER_KEY_SEPARATOR = "->";

}

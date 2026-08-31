package com.iboot.pf.module.aps.enums;

import java.math.BigDecimal;

/**
 * APS 通用常量。
 *
 * <p>车间、项目、项目任务、作业工单、工序任务、班次这些可选维度在库表里是 NOT NULL DEFAULT 0，
 * 必须落成 0 而不是 NULL，否则唯一索引失效、同一份投影会被拆成多行。
 */
public interface ApsConstants {

    /** 无车间（全工厂）、无项目、无项目任务、无资源、日汇总班次的占位编号。 */
    Long NONE_ID = 0L;

    /** 数量归零值，投影与缺料的进度类数量创建时统一用它。 */
    BigDecimal ZERO_QTY = BigDecimal.ZERO;

    /** 乐观锁版本初始值。 */
    Integer INIT_VERSION = 0;

    /** 排程版本号起始值。 */
    Integer FIRST_VERSION_NO = 1;

    /** 负荷率小数位，对应 decimal(10,4)。 */
    int LOAD_RATE_SCALE = 4;

}

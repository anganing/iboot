package com.iboot.pf.module.mes.enums;

import java.math.BigDecimal;

/**
 * MES 通用常量。
 *
 * <p>项目、项目任务、作业工单、工序任务这些可选维度在库表里是 NOT NULL DEFAULT 0，
 * 必须落成 0 而不是 NULL，否则唯一索引与统计口径都会失效。
 */
public interface MesConstants {

    /** 无项目、无项目任务、无作业工单、无工序任务的占位编号。 */
    Long NONE_ID = 0L;

    /** 数量字段的数据库精度，decimal(20,6)。 */
    int QTY_SCALE = 6;

    /** 数量归零值，创建单据时进度类数量统一用它。 */
    BigDecimal ZERO_QTY = BigDecimal.ZERO;

    /** 乐观锁版本初始值。 */
    Integer INIT_VERSION = 0;

    /** 明细行号起始值。 */
    Integer FIRST_LINE_NO = 1;

}

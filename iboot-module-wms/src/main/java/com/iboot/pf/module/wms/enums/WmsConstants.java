package com.iboot.pf.module.wms.enums;

/**
 * WMS 通用常量。
 *
 * <p>库存余额是多维唯一键，容器、项目、项目任务这些可选维度必须落成 0 而不是 NULL，
 * 否则唯一索引失效、同一批库存会被拆成多行。
 */
public interface WmsConstants {

    /** 无容器、无项目、无项目任务的占位编号。 */
    Long NONE_ID = 0L;

    /** 无批次物料使用的默认批次号。 */
    String DEFAULT_LOT_NO = "DEFAULT";

}

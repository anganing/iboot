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

    /**
     * 仓储策略适用于所有仓库时落的仓库编号。
     *
     * <p>用 0 而不是 NULL：{@code wms_strategy.wh_id} 是 NOT NULL 列，而且策略匹配要按
     * "先找本仓库的、没有再用全局的"来查，两种情况都落成数值比在 SQL 里判 NULL 更直白。
     */
    Long GLOBAL_WH_ID = 0L;

}

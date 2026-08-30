package com.iboot.pf.module.wms.api.inventory;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryBalanceRespDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryPageReqDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryQueryReqDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryTxnQueryReqDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryTxnRespDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 库存 API 门面。
 *
 * <p>向 MES / APS / Integration / UI 提供库存余额、可用量与流水的只读查询。
 * 库存变动只能走单据过账，本门面不提供直接修改余额的方法。
 */
public interface InventoryApi {

    /**
     * 查询库存余额列表。
     *
     * <p>可用量规则：{@code qty_available = qty_onhand - qty_reserved - qty_frozen}，
     * 只统计 {@code quality_status = QUALIFIED} 的库存（由 {@code qualifiedOnly} 控制）。
     *
     * @param query 查询条件
     * @return 库存余额列表
     */
    List<InventoryBalanceRespDTO> getInventoryBalanceList(InventoryQueryReqDTO query);

    /**
     * 查询可用量。
     *
     * @param query 查询条件
     * @return 汇总可用量
     */
    BigDecimal getAvailableQty(InventoryQueryReqDTO query);

    /**
     * 按物料编号汇总可用量，供批量补全使用，一次 SQL 不产生 N+1。
     *
     * @param query 查询条件
     * @return 物料编号 -> 汇总可用量
     */
    Map<Long, BigDecimal> getAvailableQtyMap(InventoryQueryReqDTO query);

    /**
     * 查询库存流水列表。
     *
     * @param query 查询条件
     * @return 库存流水列表
     */
    List<InventoryTxnRespDTO> getInventoryTxnList(InventoryTxnQueryReqDTO query);

    /**
     * 分页查询库存余额。
     *
     * @param pageReq 分页与过滤条件
     * @return 分页结果
     */
    PageResult<InventoryBalanceRespDTO> getInventoryBalancePage(InventoryPageReqDTO pageReq);

}

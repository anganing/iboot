package com.iboot.pf.module.wms.service.inventory;

import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvBalanceDO;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvTxnDO;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInventoryQueryBO;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInventoryTxnQueryBO;
import com.iboot.pf.module.wms.service.inventory.bo.WmsInvPostBO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** 库存 Service：库存余额与流水的唯一事实源。 */
public interface WmsInventoryService {

    /**
     * 计算一条余额的可用量：现存 - 预留 - 冻结。
     *
     * <p>可用量为负数时原样返回，由调用方判断是否异常。
     */
    static BigDecimal availableQtyOf(WmsInvBalanceDO balance) {
        BigDecimal onhand = balance.getQtyOnhand() == null ? BigDecimal.ZERO : balance.getQtyOnhand();
        BigDecimal reserved = balance.getQtyReserved() == null ? BigDecimal.ZERO : balance.getQtyReserved();
        BigDecimal frozen = balance.getQtyFrozen() == null ? BigDecimal.ZERO : balance.getQtyFrozen();
        return onhand.subtract(reserved).subtract(frozen);
    }

    List<WmsInvBalanceDO> getBalanceList(WmsInventoryQueryBO query);

    PageResult<WmsInvBalanceDO> getBalancePage(PageParam pageParam, WmsInventoryQueryBO query);

    /** 按查询条件汇总可用量。 */
    BigDecimal getAvailableQty(WmsInventoryQueryBO query);

    /** 按物料编号汇总可用量，供批量补全使用，一次 SQL 不产生 N+1。 */
    Map<Long, BigDecimal> getAvailableQtyMap(WmsInventoryQueryBO query);

    List<WmsInvTxnDO> getTxnList(WmsInventoryTxnQueryBO query);

    /** 判断某个过账幂等键是否已经产生过流水，供调用方做命令级判重。 */
    boolean posted(String idempotencyKey);

    /**
     * 库存过账：先按幂等键判重，再写流水，最后按版本号更新余额。
     *
     * @param bo 过账参数
     * @return 库存流水；幂等命中时返回原流水
     */
    WmsInvTxnDO post(WmsInvPostBO bo);

}

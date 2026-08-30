package com.iboot.pf.module.wms.service.count;

import com.iboot.pf.module.wms.dal.dataobject.count.WmsCountOrderDO;
import com.iboot.pf.module.wms.dal.dataobject.count.WmsCountOrderLineDO;
import com.iboot.pf.module.wms.service.count.bo.WmsConfirmCountDiffBO;
import com.iboot.pf.module.wms.service.count.bo.WmsCreateCountOrderBO;
import com.iboot.pf.module.wms.service.count.bo.WmsSubmitCountResultBO;

import java.util.List;

/** 盘点单 Service。 */
public interface WmsCountOrderService {

    /**
     * 创建盘点单并按仓库、库区、物料生成盘点行，幂等。
     *
     * @param bo 创建参数
     * @return 盘点单编号；幂等命中时返回原单据编号
     */
    Long createCountOrder(WmsCreateCountOrderBO bo);

    /** 提交实盘结果，计算差异数量 = 实盘数量 - 账面数量。 */
    void submitCountResult(WmsSubmitCountResultBO bo);

    /** 确认盘点差异：生成盘盈入库单或盘亏出库单并过账，禁止直接修改余额。 */
    void confirmCountDiff(WmsConfirmCountDiffBO bo);

    WmsCountOrderDO getCountOrder(Long id);

    List<WmsCountOrderLineDO> getCountOrderLineList(Long orderId);

}

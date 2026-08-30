package com.iboot.pf.module.wms.api.count;

import com.iboot.pf.module.wms.api.count.dto.ConfirmCountDiffReqDTO;
import com.iboot.pf.module.wms.api.count.dto.CreateCountOrderReqDTO;
import com.iboot.pf.module.wms.api.count.dto.SubmitCountResultReqDTO;

/**
 * 盘点单 API 门面。
 *
 * <p>向 MES / Integration / UI 提供盘点单创建、提交实盘结果、确认差异。
 * 盘点差异确认必须走单据过账（盘盈生成入库单、盘亏生成出库单），禁止直接修改余额。
 */
public interface CountOrderApi {

    /**
     * 创建盘点单并按仓库、库区、物料生成盘点行，幂等。
     *
     * @param reqDTO 创建命令
     * @return 盘点单编号；幂等命中时返回原编号
     */
    Long createCountOrder(CreateCountOrderReqDTO reqDTO);

    /**
     * 提交实盘结果，计算差异数量 = 实盘数量 - 账面数量。
     *
     * @param reqDTO 提交命令
     */
    void submitCountResult(SubmitCountResultReqDTO reqDTO);

    /**
     * 确认盘点差异：生成盘盈入库单或盘亏出库单并过账，禁止直接修改余额。
     *
     * @param reqDTO 确认命令
     */
    void confirmCountDiff(ConfirmCountDiffReqDTO reqDTO);

}

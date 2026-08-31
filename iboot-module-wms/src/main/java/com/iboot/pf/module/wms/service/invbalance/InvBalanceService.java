package com.iboot.pf.module.wms.service.invbalance;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.wms.controller.admin.invbalance.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.invbalance.InvBalanceDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * WMS-多维库存余额 Service 接口
 *
 * @author iboot
 */
public interface InvBalanceService {

    /**
     * 获得WMS-多维库存余额
     *
     * @param id 编号
     * @return WMS-多维库存余额
     */
    InvBalanceRespVO getInvBalance(Long id);

    /**
     * 获得WMS-多维库存余额分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-多维库存余额分页
     */
    PageResult<InvBalanceRespVO> getInvBalancePage(InvBalancePageReqVO pageReqVO);

}

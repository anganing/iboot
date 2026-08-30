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
     * 创建WMS-多维库存余额
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createInvBalance(@Valid InvBalanceSaveReqVO createReqVO);

    /**
     * 导入WMS-多维库存余额
     *
     * @param importList 导入信息
     * @return 导入结果
     */
    InvBalanceImportRespVO importInvBalanceList(List<InvBalanceImportExcelVO> importList);

    /**
     * 更新WMS-多维库存余额
     *
     * @param updateReqVO 更新信息
     */
    void updateInvBalance(@Valid InvBalanceSaveReqVO updateReqVO);

    /**
     * 删除WMS-多维库存余额
     *
     * @param id 编号
     */
    void deleteInvBalance(Long id);

    /**
    * 批量删除WMS-多维库存余额
    *
    * @param ids 编号
    */
    void deleteInvBalanceListByIds(List<Long> ids);

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
package com.iboot.pf.module.wms.service.invbalance;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import com.iboot.pf.module.wms.controller.admin.invbalance.vo.*;
import com.iboot.pf.framework.common.pojo.PageResult;

import com.iboot.pf.module.wms.dal.mysql.invbalance.InvBalanceMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_BALANCE_NOT_EXISTS;

/**
 * WMS-多维库存余额 Service 实现类。
 *
 * <p>余额只读：写入路径只有 {@code WmsInventoryService#post} 过账，这里不提供任何写方法。
 *
 * @author iboot
 */
@Service
@Validated
public class InvBalanceServiceImpl implements InvBalanceService {

    @Resource
    private InvBalanceMapper invBalanceMapper;

    @Override
    public InvBalanceRespVO getInvBalance(Long id) {
        InvBalanceRespVO balance = invBalanceMapper.selectJoinById(id);
        if (balance == null) {
            throw exception(INV_BALANCE_NOT_EXISTS);
        }
        return balance;
    }

    @Override
    public PageResult<InvBalanceRespVO> getInvBalancePage(InvBalancePageReqVO pageReqVO) {
        return invBalanceMapper.selectPage(pageReqVO);
    }

}

package com.iboot.pf.module.wms.service.invtxn;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import com.iboot.pf.module.wms.controller.admin.invtxn.vo.*;
import com.iboot.pf.framework.common.pojo.PageResult;

import com.iboot.pf.module.wms.dal.mysql.invtxn.InvTxnMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_TXN_NOT_EXISTS;

/**
 * WMS-库存流水 Service 实现类。
 *
 * <p>流水只读：由 {@code WmsInventoryService#post} 过账时一次写入，之后不可变更。
 *
 * @author iboot
 */
@Service
@Validated
public class InvTxnServiceImpl implements InvTxnService {

    @Resource
    private InvTxnMapper invTxnMapper;

    @Override
    public InvTxnRespVO getInvTxn(Long id) {
        InvTxnRespVO txn = invTxnMapper.selectJoinById(id);
        if (txn == null) {
            throw exception(INV_TXN_NOT_EXISTS);
        }
        return txn;
    }

    @Override
    public PageResult<InvTxnRespVO> getInvTxnPage(InvTxnPageReqVO pageReqVO) {
        return invTxnMapper.selectPage(pageReqVO);
    }

}

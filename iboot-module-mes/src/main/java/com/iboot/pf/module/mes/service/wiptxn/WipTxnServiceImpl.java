package com.iboot.pf.module.mes.service.wiptxn;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mes.controller.admin.wiptxn.vo.WipTxnPageReqVO;
import com.iboot.pf.module.mes.controller.admin.wiptxn.vo.WipTxnRespVO;
import com.iboot.pf.module.mes.dal.mysql.wiptxn.WipTxnMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * MES-在制流转流水 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class WipTxnServiceImpl implements WipTxnService {

    @Resource
    private WipTxnMapper wipTxnMapper;

    @Override
    public WipTxnRespVO getWipTxn(Long id) {
        return wipTxnMapper.selectJoinById(id);
    }

    @Override
    public PageResult<WipTxnRespVO> getWipTxnPage(WipTxnPageReqVO pageReqVO) {
        return wipTxnMapper.selectPage(pageReqVO);
    }

}

package com.iboot.pf.module.wms.service.invbalance;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.invbalance.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.invbalance.InvBalanceDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.wms.dal.mysql.invbalance.InvBalanceMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS-多维库存余额 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class InvBalanceServiceImpl implements InvBalanceService {

    @Resource
    private InvBalanceMapper invBalanceMapper;

    @Override
    public Long createInvBalance(InvBalanceSaveReqVO createReqVO) {
        // 插入
        InvBalanceDO invBalance = BeanUtils.toBean(createReqVO, InvBalanceDO.class);
        invBalanceMapper.insert(invBalance);

        // 返回
        return invBalance.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public InvBalanceImportRespVO importInvBalanceList(List<InvBalanceImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return InvBalanceImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        InvBalanceImportRespVO respVO = InvBalanceImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createInvBalance(BeanUtils.toBean(importItem, InvBalanceSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateInvBalance(InvBalanceSaveReqVO updateReqVO) {
        // 校验存在
        validateInvBalanceExists(updateReqVO.getId());
        // 更新
        InvBalanceDO updateObj = BeanUtils.toBean(updateReqVO, InvBalanceDO.class);
        invBalanceMapper.updateById(updateObj);
    }

    @Override
    public void deleteInvBalance(Long id) {
        // 校验存在
        validateInvBalanceExists(id);
        // 删除
        invBalanceMapper.deleteById(id);
    }

    @Override
        public void deleteInvBalanceListByIds(List<Long> ids) {
        // 删除
        invBalanceMapper.deleteByIds(ids);
        }


    private void validateInvBalanceExists(Long id) {
        if (invBalanceMapper.selectById(id) == null) {
            throw exception(INV_BALANCE_NOT_EXISTS);
        }
    }

    @Override
    public InvBalanceRespVO getInvBalance(Long id) {
        return invBalanceMapper.selectJoinById(id);
    }

    @Override
    public PageResult<InvBalanceRespVO> getInvBalancePage(InvBalancePageReqVO pageReqVO) {
        return invBalanceMapper.selectPage(pageReqVO);
    }

}
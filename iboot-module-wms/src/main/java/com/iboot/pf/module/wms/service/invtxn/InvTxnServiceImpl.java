package com.iboot.pf.module.wms.service.invtxn;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.wms.controller.admin.invtxn.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.invtxn.InvTxnDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.wms.dal.mysql.invtxn.InvTxnMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS-库存流水 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class InvTxnServiceImpl implements InvTxnService {

    @Resource
    private InvTxnMapper invTxnMapper;

    @Override
    public Long createInvTxn(InvTxnSaveReqVO createReqVO) {
        // 插入
        InvTxnDO invTxn = BeanUtils.toBean(createReqVO, InvTxnDO.class);
        invTxnMapper.insert(invTxn);

        // 返回
        return invTxn.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public InvTxnImportRespVO importInvTxnList(List<InvTxnImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return InvTxnImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        InvTxnImportRespVO respVO = InvTxnImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createInvTxn(BeanUtils.toBean(importItem, InvTxnSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateInvTxn(InvTxnSaveReqVO updateReqVO) {
        // 校验存在
        validateInvTxnExists(updateReqVO.getId());
        // 更新
        InvTxnDO updateObj = BeanUtils.toBean(updateReqVO, InvTxnDO.class);
        invTxnMapper.updateById(updateObj);
    }

    @Override
    public void deleteInvTxn(Long id) {
        // 校验存在
        validateInvTxnExists(id);
        // 删除
        invTxnMapper.deleteById(id);
    }

    @Override
        public void deleteInvTxnListByIds(List<Long> ids) {
        // 删除
        invTxnMapper.deleteByIds(ids);
        }


    private void validateInvTxnExists(Long id) {
        if (invTxnMapper.selectById(id) == null) {
            throw exception(INV_TXN_NOT_EXISTS);
        }
    }

    @Override
    public InvTxnRespVO getInvTxn(Long id) {
        return invTxnMapper.selectJoinById(id);
    }

    @Override
    public PageResult<InvTxnRespVO> getInvTxnPage(InvTxnPageReqVO pageReqVO) {
        return invTxnMapper.selectPage(pageReqVO);
    }

}
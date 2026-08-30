package com.iboot.pf.module.mes.service.wiptxn;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.mes.controller.admin.wiptxn.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.wiptxn.WipTxnDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mes.dal.mysql.wiptxn.WipTxnMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;

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
    public Long createWipTxn(WipTxnSaveReqVO createReqVO) {
        // 插入
        WipTxnDO wipTxn = BeanUtils.toBean(createReqVO, WipTxnDO.class);
        wipTxnMapper.insert(wipTxn);

        // 返回
        return wipTxn.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public WipTxnImportRespVO importWipTxnList(List<WipTxnImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return WipTxnImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        WipTxnImportRespVO respVO = WipTxnImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createWipTxn(BeanUtils.toBean(importItem, WipTxnSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateWipTxn(WipTxnSaveReqVO updateReqVO) {
        // 校验存在
        validateWipTxnExists(updateReqVO.getId());
        // 更新
        WipTxnDO updateObj = BeanUtils.toBean(updateReqVO, WipTxnDO.class);
        wipTxnMapper.updateById(updateObj);
    }

    @Override
    public void deleteWipTxn(Long id) {
        // 校验存在
        validateWipTxnExists(id);
        // 删除
        wipTxnMapper.deleteById(id);
    }

    @Override
        public void deleteWipTxnListByIds(List<Long> ids) {
        // 删除
        wipTxnMapper.deleteByIds(ids);
        }


    private void validateWipTxnExists(Long id) {
        if (wipTxnMapper.selectById(id) == null) {
            throw exception(WIP_TXN_NOT_EXISTS);
        }
    }

    @Override
    public WipTxnRespVO getWipTxn(Long id) {
        return wipTxnMapper.selectJoinById(id);
    }

    @Override
    public PageResult<WipTxnRespVO> getWipTxnPage(WipTxnPageReqVO pageReqVO) {
        return wipTxnMapper.selectPage(pageReqVO);
    }

}
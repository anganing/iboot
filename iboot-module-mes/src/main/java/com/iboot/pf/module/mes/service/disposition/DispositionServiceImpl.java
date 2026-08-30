package com.iboot.pf.module.mes.service.disposition;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.mes.controller.admin.disposition.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mes.dal.mysql.disposition.DispositionMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES-不合格品处置单 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class DispositionServiceImpl implements DispositionService {

    @Resource
    private DispositionMapper dispositionMapper;

    @Override
    public Long createDisposition(DispositionSaveReqVO createReqVO) {
        // 插入
        DispositionDO disposition = BeanUtils.toBean(createReqVO, DispositionDO.class);
        dispositionMapper.insert(disposition);

        // 返回
        return disposition.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public DispositionImportRespVO importDispositionList(List<DispositionImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return DispositionImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        DispositionImportRespVO respVO = DispositionImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createDisposition(BeanUtils.toBean(importItem, DispositionSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateDisposition(DispositionSaveReqVO updateReqVO) {
        // 校验存在
        validateDispositionExists(updateReqVO.getId());
        // 更新
        DispositionDO updateObj = BeanUtils.toBean(updateReqVO, DispositionDO.class);
        dispositionMapper.updateById(updateObj);
    }

    @Override
    public void deleteDisposition(Long id) {
        // 校验存在
        validateDispositionExists(id);
        // 删除
        dispositionMapper.deleteById(id);
    }

    @Override
        public void deleteDispositionListByIds(List<Long> ids) {
        // 删除
        dispositionMapper.deleteByIds(ids);
        }


    private void validateDispositionExists(Long id) {
        if (dispositionMapper.selectById(id) == null) {
            throw exception(DISPOSITION_NOT_EXISTS);
        }
    }

    @Override
    public DispositionRespVO getDisposition(Long id) {
        return dispositionMapper.selectJoinById(id);
    }

    @Override
    public PageResult<DispositionRespVO> getDispositionPage(DispositionPageReqVO pageReqVO) {
        return dispositionMapper.selectPage(pageReqVO);
    }

}
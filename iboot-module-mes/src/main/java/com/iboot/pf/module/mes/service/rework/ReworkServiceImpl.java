package com.iboot.pf.module.mes.service.rework;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.mes.controller.admin.rework.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.rework.ReworkDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mes.dal.mysql.rework.ReworkMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES-返工任务 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ReworkServiceImpl implements ReworkService {

    @Resource
    private ReworkMapper reworkMapper;

    @Override
    public Long createRework(ReworkSaveReqVO createReqVO) {
        // 插入
        ReworkDO rework = BeanUtils.toBean(createReqVO, ReworkDO.class);
        reworkMapper.insert(rework);

        // 返回
        return rework.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public ReworkImportRespVO importReworkList(List<ReworkImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return ReworkImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        ReworkImportRespVO respVO = ReworkImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createRework(BeanUtils.toBean(importItem, ReworkSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateRework(ReworkSaveReqVO updateReqVO) {
        // 校验存在
        validateReworkExists(updateReqVO.getId());
        // 更新
        ReworkDO updateObj = BeanUtils.toBean(updateReqVO, ReworkDO.class);
        reworkMapper.updateById(updateObj);
    }

    @Override
    public void deleteRework(Long id) {
        // 校验存在
        validateReworkExists(id);
        // 删除
        reworkMapper.deleteById(id);
    }

    @Override
        public void deleteReworkListByIds(List<Long> ids) {
        // 删除
        reworkMapper.deleteByIds(ids);
        }


    private void validateReworkExists(Long id) {
        if (reworkMapper.selectById(id) == null) {
            throw exception(REWORK_NOT_EXISTS);
        }
    }

    @Override
    public ReworkRespVO getRework(Long id) {
        return reworkMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ReworkRespVO> getReworkPage(ReworkPageReqVO pageReqVO) {
        return reworkMapper.selectPage(pageReqVO);
    }

}
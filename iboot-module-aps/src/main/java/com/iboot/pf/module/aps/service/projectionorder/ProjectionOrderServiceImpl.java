package com.iboot.pf.module.aps.service.projectionorder;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.aps.controller.admin.projectionorder.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.projectionorder.ProjectionOrderDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.aps.dal.mysql.projectionorder.ProjectionOrderMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.*;

/**
 * APS-MES需求工单任务投影 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ProjectionOrderServiceImpl implements ProjectionOrderService {

    @Resource
    private ProjectionOrderMapper projectionOrderMapper;

    @Override
    public Long createProjectionOrder(ProjectionOrderSaveReqVO createReqVO) {
        // 插入
        ProjectionOrderDO projectionOrder = BeanUtils.toBean(createReqVO, ProjectionOrderDO.class);
        projectionOrderMapper.insert(projectionOrder);

        // 返回
        return projectionOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public ProjectionOrderImportRespVO importProjectionOrderList(List<ProjectionOrderImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return ProjectionOrderImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        ProjectionOrderImportRespVO respVO = ProjectionOrderImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createProjectionOrder(BeanUtils.toBean(importItem, ProjectionOrderSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateProjectionOrder(ProjectionOrderSaveReqVO updateReqVO) {
        // 校验存在
        validateProjectionOrderExists(updateReqVO.getId());
        // 更新
        ProjectionOrderDO updateObj = BeanUtils.toBean(updateReqVO, ProjectionOrderDO.class);
        projectionOrderMapper.updateById(updateObj);
    }

    @Override
    public void deleteProjectionOrder(Long id) {
        // 校验存在
        validateProjectionOrderExists(id);
        // 删除
        projectionOrderMapper.deleteById(id);
    }

    @Override
        public void deleteProjectionOrderListByIds(List<Long> ids) {
        // 删除
        projectionOrderMapper.deleteByIds(ids);
        }


    private void validateProjectionOrderExists(Long id) {
        if (projectionOrderMapper.selectById(id) == null) {
            throw exception(PROJECTION_ORDER_NOT_EXISTS);
        }
    }

    @Override
    public ProjectionOrderRespVO getProjectionOrder(Long id) {
        return projectionOrderMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ProjectionOrderRespVO> getProjectionOrderPage(ProjectionOrderPageReqVO pageReqVO) {
        return projectionOrderMapper.selectPage(pageReqVO);
    }

}
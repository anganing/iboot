package com.iboot.pf.module.mes.service.workorder;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES-作业工单 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class WorkOrderServiceImpl implements WorkOrderService {

    @Resource
    private WorkOrderMapper workOrderMapper;

    @Override
    public Long createWorkOrder(WorkOrderSaveReqVO createReqVO) {
        // 插入
        WorkOrderDO workOrder = BeanUtils.toBean(createReqVO, WorkOrderDO.class);
        workOrderMapper.insert(workOrder);

        // 返回
        return workOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public WorkOrderImportRespVO importWorkOrderList(List<WorkOrderImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return WorkOrderImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        WorkOrderImportRespVO respVO = WorkOrderImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createWorkOrder(BeanUtils.toBean(importItem, WorkOrderSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateWorkOrder(WorkOrderSaveReqVO updateReqVO) {
        // 校验存在
        validateWorkOrderExists(updateReqVO.getId());
        // 更新
        WorkOrderDO updateObj = BeanUtils.toBean(updateReqVO, WorkOrderDO.class);
        workOrderMapper.updateById(updateObj);
    }

    @Override
    public void deleteWorkOrder(Long id) {
        // 校验存在
        validateWorkOrderExists(id);
        // 删除
        workOrderMapper.deleteById(id);
    }

    @Override
        public void deleteWorkOrderListByIds(List<Long> ids) {
        // 删除
        workOrderMapper.deleteByIds(ids);
        }


    private void validateWorkOrderExists(Long id) {
        if (workOrderMapper.selectById(id) == null) {
            throw exception(WORK_ORDER_NOT_EXISTS);
        }
    }

    @Override
    public WorkOrderRespVO getWorkOrder(Long id) {
        return workOrderMapper.selectJoinById(id);
    }

    @Override
    public List<WorkOrderDO> getWorkOrderListByProdOrderIds(Collection<Long> prodOrderIds) {
        if (CollUtil.isEmpty(prodOrderIds)) {
            return Collections.emptyList();
        }
        return workOrderMapper.selectList(new LambdaQueryWrapper<WorkOrderDO>()
                .in(WorkOrderDO::getProdOrderId, prodOrderIds));
    }

    @Override
    public List<WorkOrderDO> getWorkOrderList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return workOrderMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<WorkOrderRespVO> getWorkOrderPage(WorkOrderPageReqVO pageReqVO) {
        return workOrderMapper.selectPage(pageReqVO);
    }

}

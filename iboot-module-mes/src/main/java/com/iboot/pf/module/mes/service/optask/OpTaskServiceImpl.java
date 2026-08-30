package com.iboot.pf.module.mes.service.optask;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.mes.controller.admin.optask.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.service.workorder.WorkOrderService;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES-工序任务 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class OpTaskServiceImpl implements OpTaskService {

    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private WorkOrderService workOrderService;

    @Override
    public Long createOpTask(OpTaskSaveReqVO createReqVO) {
        // 插入
        OpTaskDO opTask = BeanUtils.toBean(createReqVO, OpTaskDO.class);
        opTaskMapper.insert(opTask);

        // 返回
        return opTask.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public OpTaskImportRespVO importOpTaskList(List<OpTaskImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return OpTaskImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        OpTaskImportRespVO respVO = OpTaskImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createOpTask(BeanUtils.toBean(importItem, OpTaskSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateOpTask(OpTaskSaveReqVO updateReqVO) {
        // 校验存在
        validateOpTaskExists(updateReqVO.getId());
        // 更新
        OpTaskDO updateObj = BeanUtils.toBean(updateReqVO, OpTaskDO.class);
        opTaskMapper.updateById(updateObj);
    }

    @Override
    public void deleteOpTask(Long id) {
        // 校验存在
        validateOpTaskExists(id);
        // 删除
        opTaskMapper.deleteById(id);
    }

    @Override
        public void deleteOpTaskListByIds(List<Long> ids) {
        // 删除
        opTaskMapper.deleteByIds(ids);
        }


    private void validateOpTaskExists(Long id) {
        if (opTaskMapper.selectById(id) == null) {
            throw exception(OP_TASK_NOT_EXISTS);
        }
    }

    @Override
    public OpTaskRespVO getOpTask(Long id) {
        return opTaskMapper.selectJoinById(id);
    }

    @Override
    public List<OpTaskDO> getOpTaskListByProdOrderIds(Collection<Long> prodOrderIds) {
        if (CollUtil.isEmpty(prodOrderIds)) {
            return Collections.emptyList();
        }
        List<WorkOrderDO> workOrders = workOrderService.getWorkOrderListByProdOrderIds(prodOrderIds);
        if (CollUtil.isEmpty(workOrders)) {
            return Collections.emptyList();
        }
        return opTaskMapper.selectList(new LambdaQueryWrapper<OpTaskDO>()
                .in(OpTaskDO::getWorkOrderId, convertList(workOrders, WorkOrderDO::getId)));
    }

    @Override
    public List<OpTaskDO> getOpTaskListByRouteOpIds(Collection<Long> routeOpIds) {
        if (CollUtil.isEmpty(routeOpIds)) {
            return Collections.emptyList();
        }
        return opTaskMapper.selectList(new LambdaQueryWrapper<OpTaskDO>()
                .in(OpTaskDO::getRouteOpId, routeOpIds));
    }

    @Override
    public PageResult<OpTaskRespVO> getOpTaskPage(OpTaskPageReqVO pageReqVO) {
        return opTaskMapper.selectPage(pageReqVO);
    }

}

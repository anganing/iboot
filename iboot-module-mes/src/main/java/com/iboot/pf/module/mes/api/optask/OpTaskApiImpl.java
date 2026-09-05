package com.iboot.pf.module.mes.api.optask;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.optask.dto.ApplyScheduleReqDTO;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskFinishReqDTO;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskRespDTO;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskSkipReqDTO;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskStatusReqDTO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.service.optask.OpTaskService;
import com.iboot.pf.module.mes.service.workorder.WorkOrderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertMap;

/** 工序任务 API 实现。 */
@Service
@Validated
public class OpTaskApiImpl implements OpTaskApi {

    @Resource
    private OpTaskService opTaskService;

    @Resource
    private WorkOrderService workOrderService;

    @Override
    @DataPermission(enable = false)
    public List<OpTaskRespDTO> getOpTaskListByOrderIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return List.of();
        }
        return convertWithProdOrder(opTaskService.getOpTaskListByProdOrderIds(ids));
    }

    @Override
    @DataPermission(enable = false)
    public List<OpTaskRespDTO> getOpTaskListByRouteOpIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return List.of();
        }
        return convertWithProdOrder(opTaskService.getOpTaskListByRouteOpIds(ids));
    }

    @Override
    @DataPermission(enable = false)
    public int applySchedule(ApplyScheduleReqDTO reqDTO) {
        // 关掉数据权限：调用方是 APS 的发布动作，不带用户上下文，按登录用户过滤会一条都更不到
        return opTaskService.applySchedule(reqDTO);
    }

    @Override
    public void startOpTask(OpTaskStatusReqDTO reqDTO) {
        throw new UnsupportedOperationException("工序开工命令尚未接入领域规则");
    }

    @Override
    public void pauseOpTask(OpTaskStatusReqDTO reqDTO) {
        throw new UnsupportedOperationException("工序暂停命令尚未接入领域规则");
    }

    @Override
    public void finishOpTask(OpTaskFinishReqDTO reqDTO) {
        throw new UnsupportedOperationException("工序完工命令尚未接入领域规则");
    }

    @Override
    public void skipOpTask(OpTaskSkipReqDTO reqDTO) {
        throw new UnsupportedOperationException("工序跳过命令尚未接入领域规则");
    }

    private List<OpTaskRespDTO> convertWithProdOrder(List<OpTaskDO> tasks) {
        List<OpTaskRespDTO> result = BeanUtils.toBean(tasks, OpTaskRespDTO.class);
        Map<Long, WorkOrderDO> workOrderMap = convertMap(
                workOrderService.getWorkOrderList(convertList(tasks, OpTaskDO::getWorkOrderId)),
                WorkOrderDO::getId);
        for (OpTaskRespDTO task : result) {
            WorkOrderDO workOrder = workOrderMap.get(task.getWorkOrderId());
            task.setProdOrderId(workOrder == null ? null : workOrder.getProdOrderId());
        }
        return result;
    }

}

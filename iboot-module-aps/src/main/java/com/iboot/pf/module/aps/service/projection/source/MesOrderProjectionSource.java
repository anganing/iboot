package com.iboot.pf.module.aps.service.projection.source;

import com.iboot.pf.module.aps.dal.dataobject.projection.ApsOrderProjectionDO;
import com.iboot.pf.module.mes.api.optask.OpTaskApi;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskRespDTO;
import com.iboot.pf.module.mes.api.order.ProdOrderApi;
import com.iboot.pf.module.mes.api.order.dto.ProdOrderRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;

/** 仅通过 MES API 拉取工单与工序任务，构建 APS 只读投影。 */
@Component
public class MesOrderProjectionSource implements OrderProjectionSource {

    @Resource
    private ProdOrderApi prodOrderApi;
    @Resource
    private OpTaskApi opTaskApi;

    @Override
    public List<ApsOrderProjectionDO> loadOrderProjection(Long scenarioId, Long plantId) {
        List<ProdOrderRespDTO> orders = prodOrderApi.getProdOrderListByPlantId(plantId);
        if (orders.isEmpty()) {
            return List.of();
        }
        Map<Long, ProdOrderRespDTO> orderMap = orders.stream()
                .collect(java.util.stream.Collectors.toMap(ProdOrderRespDTO::getId, Function.identity()));
        LocalDateTime now = LocalDateTime.now();
        return opTaskApi.getOpTaskListByOrderIds(convertList(orders, ProdOrderRespDTO::getId)).stream()
                .map(task -> toProjection(scenarioId, orderMap.get(task.getProdOrderId()), task, now))
                .toList();
    }

    private ApsOrderProjectionDO toProjection(Long scenarioId, ProdOrderRespDTO order,
                                               OpTaskRespDTO task, LocalDateTime now) {
        if (order == null) {
            throw new IllegalStateException("工序任务缺少生产工单归属");
        }
        return ApsOrderProjectionDO.builder()
                .scenarioId(scenarioId).sourceType("OP_TASK").sourceId(task.getId()).sourceNo(task.getTaskNo())
                .prodOrderId(order.getId()).workOrderId(task.getWorkOrderId()).opTaskId(task.getId())
                .plantId(order.getPlantId()).workshopId(order.getWorkshopId()).projectId(order.getProjectId())
                .projectTaskId(order.getProjectTaskId()).matId(order.getMatId()).matCode(order.getMatCode())
                .routeOpId(task.getRouteOpId()).resId(task.getResId()).qty(task.getPlannedQty())
                .completedQty(task.getCompletedQty()).dueDate(order.getDueDate()).priority(order.getPriority())
                .currentStatus(task.getStatus()).actualStartTime(task.getActualStartTime())
                .actualEndTime(task.getActualEndTime()).lockedFlag(false).sourceUpdateTime(now).snapshotTime(now)
                .build();
    }
}

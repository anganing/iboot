package com.iboot.pf.module.aps.service.projection.source;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsOrderProjectionDO;
import com.iboot.pf.module.aps.enums.ApsConstants;
import com.iboot.pf.module.aps.enums.ApsProjectionSourceTypeEnum;
import com.iboot.pf.module.mes.api.optask.OpTaskApi;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskRespDTO;
import com.iboot.pf.module.mes.api.order.ProdOrderApi;
import com.iboot.pf.module.mes.api.order.dto.ProdOrderRespDTO;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 通过 MES 门面拉取工单与工序任务，构建 APS 只读投影。
 *
 * <p>只调用 {@code ProdOrderApi} 与 {@code OpTaskApi}，不碰 mes 的表：跨库 JOIN 会把 aps 和 mes
 * 焊死在同一个库上。
 */
@Component
public class MesOrderProjectionSource implements OrderProjectionSource {

    @Resource
    private ProdOrderApi prodOrderApi;
    @Resource
    private OpTaskApi opTaskApi;

    @Override
    public List<ApsOrderProjectionDO> loadOrderProjection(Long scenarioId, Long plantId) {
        List<ProdOrderRespDTO> orders = prodOrderApi.getProdOrderListByPlantId(plantId);
        if (CollUtil.isEmpty(orders)) {
            return List.of();
        }
        Map<Long, ProdOrderRespDTO> orderMap = orders.stream()
                .collect(Collectors.toMap(ProdOrderRespDTO::getId, Function.identity()));
        LocalDateTime now = LocalDateTime.now();
        List<OpTaskRespDTO> tasks = opTaskApi.getOpTaskListByOrderIds(convertList(orders, ProdOrderRespDTO::getId));
        List<ApsOrderProjectionDO> rows = new ArrayList<>(tasks.size());
        for (OpTaskRespDTO task : tasks) {
            ProdOrderRespDTO order = orderMap.get(task.getProdOrderId());
            // 工单已被过滤掉（例如已关闭）时跳过对应工序任务，不能凭空造一条没有工单归属的投影
            if (order == null) {
                continue;
            }
            rows.add(toProjection(scenarioId, order, task, now));
        }
        return rows;
    }

    private ApsOrderProjectionDO toProjection(Long scenarioId, ProdOrderRespDTO order,
                                              OpTaskRespDTO task, LocalDateTime now) {
        return ApsOrderProjectionDO.builder()
                .scenarioId(scenarioId)
                .sourceType(ApsProjectionSourceTypeEnum.OP_TASK.name())
                .sourceId(task.getId())
                .sourceNo(task.getTaskNo())
                .prodOrderId(order.getId())
                .workOrderId(orNone(task.getWorkOrderId()))
                .opTaskId(task.getId())
                .plantId(order.getPlantId())
                .workshopId(orNone(order.getWorkshopId()))
                .projectId(orNone(order.getProjectId()))
                .projectTaskId(orNone(order.getProjectTaskId()))
                .matId(order.getMatId())
                .matCode(order.getMatCode())
                .routeOpId(orNone(task.getRouteOpId()))
                .resId(orNone(task.getResId()))
                .qty(task.getPlannedQty())
                .completedQty(task.getCompletedQty() == null ? ApsConstants.ZERO_QTY : task.getCompletedQty())
                .dueDate(order.getDueDate())
                .priority(order.getPriority())
                .currentStatus(task.getStatus())
                .actualStartTime(task.getActualStartTime())
                .actualEndTime(task.getActualEndTime())
                // 已经开工（不再是待排产/待开工）的工序不允许被重排挪走，投影阶段就打上锁定标记
                .lockedFlag(!MesOpTaskStatusEnum.isNotStarted(task.getStatus()))
                .sourceUpdateTime(now)
                .snapshotTime(now)
                .build();
    }

    /** 可选外键统一落 0。 */
    private Long orNone(Long value) {
        return value == null ? ApsConstants.NONE_ID : value;
    }

}

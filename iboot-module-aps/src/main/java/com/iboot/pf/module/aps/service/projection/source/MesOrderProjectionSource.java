package com.iboot.pf.module.aps.service.projection.source;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsOrderProjectionDO;
import com.iboot.pf.module.aps.enums.ApsConstants;
import com.iboot.pf.module.aps.enums.ApsDurationTypeEnum;
import com.iboot.pf.module.aps.enums.ApsProjectionSourceTypeEnum;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.op.OpApi;
import com.iboot.pf.module.mdm.api.op.dto.OpRespDTO;
import com.iboot.pf.module.mdm.api.route.RouteApi;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpRespDTO;
import com.iboot.pf.module.mes.api.optask.OpTaskApi;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskRespDTO;
import com.iboot.pf.module.mes.api.order.ProdOrderApi;
import com.iboot.pf.module.mes.api.order.dto.ProdOrderRespDTO;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 通过 MES 门面拉取工单与工序任务，构建 APS 只读投影。
 *
 * <p>只调用 {@code ProdOrderApi} 与 {@code OpTaskApi}，不碰 mes 的表：跨库 JOIN 会把 aps 和 mes
 * 焊死在同一个库上。
 *
 * <p>工时与换型参数在这一步一次性冻结进投影：求解器只读投影，不回查 mdm，
 * 否则主数据改一次工时，同一场景重复求解就会得出不同结果，追溯链断掉。
 */
@Component
public class MesOrderProjectionSource implements OrderProjectionSource {

    @Resource
    private ProdOrderApi prodOrderApi;
    @Resource
    private OpTaskApi opTaskApi;
    @Resource
    private RouteApi routeApi;
    @Resource
    private OpApi opApi;
    @Resource
    private MaterialApi materialApi;

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
        // 工时口径：路线工序 override 优先、回落标准工序。两张主数据各批量取一次，不在循环里逐条查
        Map<Long, RouteOpRespDTO> routeOpMap = routeApi.getRouteOpMap(collectRouteOpIds(tasks));
        Map<Long, OpRespDTO> opMap = opApi.getOpMap(collectOpIds(tasks, routeOpMap));
        Map<Long, MaterialRespDTO> matMap = materialApi.getMaterialMap(convertList(orders, ProdOrderRespDTO::getMatId));
        List<ApsOrderProjectionDO> rows = new ArrayList<>(tasks.size());
        for (OpTaskRespDTO task : tasks) {
            ProdOrderRespDTO order = orderMap.get(task.getProdOrderId());
            // 工单已被过滤掉（例如已关闭）时跳过对应工序任务，不能凭空造一条没有工单归属的投影
            if (order == null) {
                continue;
            }
            rows.add(toProjection(scenarioId, order, task, routeOpMap.get(task.getRouteOpId()),
                    resolveOp(task, routeOpMap, opMap), matMap.get(order.getMatId()), now));
        }
        return rows;
    }

    private ApsOrderProjectionDO toProjection(Long scenarioId, ProdOrderRespDTO order,
                                              OpTaskRespDTO task, RouteOpRespDTO routeOp,
                                              OpRespDTO op, MaterialRespDTO material, LocalDateTime now) {
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
                .seq(task.getSeq() == null ? 0 : task.getSeq())
                // 工序编号与编码名称一并冻结：求解按 opId 匹配资源能力，落库时又要拿编码名称做快照
                .opId(orNone(op == null ? task.getOpId() : op.getId()))
                .opCode(op == null ? blankToEmpty(task.getOpCode()) : blankToEmpty(op.getCode()))
                .opName(op == null ? blankToEmpty(task.getOpName()) : blankToEmpty(op.getName()))
                .resId(orNone(task.getResId()))
                .qty(task.getPlannedQty())
                .completedQty(task.getCompletedQty() == null ? ApsConstants.ZERO_QTY : task.getCompletedQty())
                .dueDate(order.getDueDate())
                .priority(order.getPriority())
                .setupMinutes(firstNonNull(routeOp == null ? null : routeOp.getOverrideSetupMinutes(),
                        op == null ? null : op.getSetupMinutes(), 0))
                .stdMinutesPerUnit(firstNonNull(routeOp == null ? null : routeOp.getOverrideStdMinutesPerUnit(),
                        op == null ? null : op.getStdMinutesPerUnit(), BigDecimal.ZERO))
                .teardownMinutes(firstNonNull(op == null ? null : op.getTeardownMinutes(), null, 0))
                .queueMinutes(firstNonNull(routeOp == null ? null : routeOp.getOverrideQueueMinutes(),
                        op == null ? null : op.getQueueMinutes(), 0))
                .moveMinutes(firstNonNull(routeOp == null ? null : routeOp.getOverrideMoveMinutes(),
                        op == null ? null : op.getMoveMinutes(), 0))
                .durationType(op == null || StrUtil.isBlank(op.getDurationType())
                        ? ApsDurationTypeEnum.PER_UNIT.name() : op.getDurationType())
                .stdDuration(firstNonNull(op == null ? null : op.getStdDuration(), null, 0))
                .manQty(firstNonNull(op == null ? null : op.getManQty(), null, 1))
                // 换型分组来自成品物料：换型损失按“上一道任务的分组 -> 本道任务的分组”查换型矩阵
                .changeoverGroup(material == null || material.getChangeoverGroup() == null
                        ? "" : material.getChangeoverGroup())
                .currentStatus(task.getStatus())
                .actualStartTime(task.getActualStartTime())
                .actualEndTime(task.getActualEndTime())
                // 已经开工（不再是待排产/待开工）的工序不允许被重排挪走，投影阶段就打上锁定标记
                .lockedFlag(!MesOpTaskStatusEnum.isNotStarted(task.getStatus()))
                .sourceUpdateTime(now)
                .snapshotTime(now)
                .build();
    }

    /** 路线工序编号集合，0 表示无路线工序，不参与查询。 */
    private Set<Long> collectRouteOpIds(List<OpTaskRespDTO> tasks) {
        Set<Long> ids = new LinkedHashSet<>();
        for (OpTaskRespDTO task : tasks) {
            if (task.getRouteOpId() != null && !ApsConstants.NONE_ID.equals(task.getRouteOpId())) {
                ids.add(task.getRouteOpId());
            }
        }
        return ids;
    }

    /**
     * 标准工序编号集合。
     *
     * <p>优先取工序任务自带的 opId；为空时回落到路线工序上的 opId，兼容 MES 未回填 opId 的历史数据。
     */
    private Set<Long> collectOpIds(List<OpTaskRespDTO> tasks, Map<Long, RouteOpRespDTO> routeOpMap) {
        Set<Long> ids = new LinkedHashSet<>();
        for (OpTaskRespDTO task : tasks) {
            Long opId = resolveOpId(task, routeOpMap);
            if (opId != null && !ApsConstants.NONE_ID.equals(opId)) {
                ids.add(opId);
            }
        }
        return ids;
    }

    private OpRespDTO resolveOp(OpTaskRespDTO task, Map<Long, RouteOpRespDTO> routeOpMap,
                                Map<Long, OpRespDTO> opMap) {
        Long opId = resolveOpId(task, routeOpMap);
        return opId == null ? null : opMap.get(opId);
    }

    private Long resolveOpId(OpTaskRespDTO task, Map<Long, RouteOpRespDTO> routeOpMap) {
        if (task.getOpId() != null && !ApsConstants.NONE_ID.equals(task.getOpId())) {
            return task.getOpId();
        }
        RouteOpRespDTO routeOp = task.getRouteOpId() == null ? null : routeOpMap.get(task.getRouteOpId());
        return routeOp == null ? null : routeOp.getOpId();
    }

    /** override 优先、标准值回落、都为空时取兜底值。 */
    private <T> T firstNonNull(T override, T standard, T fallback) {
        if (override != null) {
            return override;
        }
        return standard != null ? standard : fallback;
    }

    /** 可选外键统一落 0。 */
    private Long orNone(Long value) {
        return value == null ? ApsConstants.NONE_ID : value;
    }

    /** 快照列都是 NOT NULL，缺值落空串而不是 null。 */
    private String blankToEmpty(String value) {
        return value == null ? "" : value;
    }

}

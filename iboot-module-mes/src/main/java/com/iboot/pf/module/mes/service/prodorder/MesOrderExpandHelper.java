package com.iboot.pf.module.mes.service.prodorder;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.iboot.pf.module.mdm.api.op.OpApi;
import com.iboot.pf.module.mdm.api.op.dto.OpRespDTO;
import com.iboot.pf.module.mdm.api.route.RouteApi;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.enums.MesOpExecModeEnum;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcModeEnum;
import com.iboot.pf.module.mes.enums.MesSkipPolicyEnum;
import com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_ROUTE_OP_EMPTY;

/**
 * 生产工单下达时的工序展开。
 *
 * <p>下达做四件事，这里负责最后一件：按锁定的工艺路线版本，把一张生产工单摊成"作业工单 + 工序任务"两层，
 * 并把工序任务按顺序串起来。详见 {@code dev-docs/业务流程/04-MES生产执行流程.md} 的"流程二：工单下达"。
 *
 * <p>从 {@link MesProdOrderDomainServiceImpl} 里拆出来独立成类，原因是展开逻辑本身有三段
 * （建作业工单、建工序任务、串前后序），混在下达动作里会让那个方法长到看不清主干。
 */
@Component
public class MesOrderExpandHelper {

    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private RouteApi routeApi;
    @Resource
    private OpApi opApi;
    @Resource
    private MesCodeService codeService;

    /**
     * 按工单锁定的工艺路线展开作业工单与工序任务。
     *
     * <p>调用前工单的四个版本快照必须已经锁定：展开依据的是 {@code order.routeId}，
     * 拿未锁定的路线展开等于工单执行依据与展开结果对不上。
     *
     * @param order 已锁定版本快照的生产工单
     */
    @Transactional(rollbackFor = Exception.class)
    public void expand(ProdOrderDO order) {
        List<RouteOpRespDTO> routeOps = requireRouteOps(order.getRouteId());
        // 标准工序编码与名称一次 IN 查回，不在循环里逐道查（红线 5：禁止 N+1）
        Map<Long, OpRespDTO> opMap = opApi.getOpMap(
                routeOps.stream().map(RouteOpRespDTO::getOpId).distinct().toList());
        Map<Long, WorkOrderDO> workOrderByWc = createWorkOrders(order, routeOps);
        List<OpTaskDO> tasks = createOpTasks(order, routeOps, opMap, workOrderByWc);
        chainTasks(tasks);
    }

    /**
     * 取路线工序，按 seq 升序。
     *
     * <p>门面已声明按 seq 升序返回，这里仍再排一次：串联结果完全依赖顺序，让顺序在本类内部自洽，
     * 而不是依赖上游的口头约定。seq 相同（并行工序）时按编号兜底，保证同一条路线每次展开选出的
     * 代表工序一致，重复下达的结果可比。
     */
    private List<RouteOpRespDTO> requireRouteOps(Long routeId) {
        List<RouteOpRespDTO> routeOps = routeApi.getRouteOpList(routeId);
        if (CollUtil.isEmpty(routeOps)) {
            throw exception(PROD_ORDER_ROUTE_OP_EMPTY);
        }
        return routeOps.stream()
                .sorted(Comparator.comparing(RouteOpRespDTO::getSeq).thenComparing(RouteOpRespDTO::getId))
                .toList();
    }

    /**
     * 按工作中心分组建作业工单，一个工作中心一张。
     *
     * <p>同一工作中心出现多道工序（例如粗加工与精加工都在加工中心）时挂在同一张单上：作业工单是
     * 车间派工的单位，同一个工作中心拆成两张单会让主管在两张单之间找同一批活。
     *
     * @return 工作中心编号 -&gt; 作业工单，按工序顺序首次出现的次序排列
     */
    private Map<Long, WorkOrderDO> createWorkOrders(ProdOrderDO order, List<RouteOpRespDTO> routeOps) {
        Map<Long, WorkOrderDO> workOrderByWc = new LinkedHashMap<>();
        for (RouteOpRespDTO routeOp : routeOps) {
            if (workOrderByWc.containsKey(routeOp.getWcId())) {
                continue;
            }
            WorkOrderDO workOrder = WorkOrderDO.builder()
                    .workOrderNo(codeService.generate(MesCodeRuleConstants.WORK_ORDER, null))
                    .prodOrderId(order.getId())
                    .prodOrderNo(order.getOrderNo())
                    .plantId(order.getPlantId())
                    .workshopId(order.getWorkshopId())
                    .wcId(routeOp.getWcId())
                    .projectId(order.getProjectId())
                    .projectTaskId(order.getProjectTaskId())
                    // 计划数量等于工单数量：整单投产，分批投产是需求转单时拆的，不在这里拆
                    .plannedQty(order.getQty())
                    .completedQty(MesConstants.ZERO_QTY)
                    .kitStatus(MesKitStatusEnum.UNCHECKED.name())
                    // 下达只把作业工单摊出来，推进到 READY 由齐套或发料触发
                    .status(MesWorkOrderStatusEnum.PENDING.name())
                    .version(MesConstants.INIT_VERSION)
                    .build();
            workOrderMapper.insert(workOrder);
            workOrderByWc.put(routeOp.getWcId(), workOrder);
        }
        return workOrderByWc;
    }

    /**
     * 每道路线工序生成一条工序任务，按 seq 升序，与入参顺序一致。
     *
     * <p>工序任务锁定 {@code route_op_id} 并快照工序编码、名称、顺序：主数据之后改工序名，
     * 在制工单看到的仍是下达时的名字。
     */
    private List<OpTaskDO> createOpTasks(ProdOrderDO order, List<RouteOpRespDTO> routeOps,
                                         Map<Long, OpRespDTO> opMap, Map<Long, WorkOrderDO> workOrderByWc) {
        List<OpTaskDO> tasks = new ArrayList<>(routeOps.size());
        for (RouteOpRespDTO routeOp : routeOps) {
            OpRespDTO op = opMap.get(routeOp.getOpId());
            if (op == null) {
                throw exception(OP_NOT_EXISTS);
            }
            WorkOrderDO workOrder = workOrderByWc.get(routeOp.getWcId());
            OpTaskDO task = OpTaskDO.builder()
                    .taskNo(codeService.generate(MesCodeRuleConstants.OP_TASK, null))
                    .workOrderId(workOrder.getId())
                    .workOrderNo(workOrder.getWorkOrderNo())
                    .routeOpId(routeOp.getId())
                    .opId(routeOp.getOpId())
                    .opCode(op.getCode())
                    .opName(op.getName())
                    .seq(routeOp.getSeq())
                    .parallelGroup(routeOp.getParallelGroup())
                    // 前后序先落 0，全部工序建完后统一串联：串联要用对方的主键，必须等 id 都分配好
                    .prevTaskId(MesConstants.NONE_ID)
                    .nextTaskId(MesConstants.NONE_ID)
                    .execMode(resolveExecMode(routeOp))
                    .qcMode(MesQcModeEnum.NONE.name())
                    .skipPolicy(StrUtil.blankToDefault(
                            routeOp.getSkipPolicy(), MesSkipPolicyEnum.NOT_ALLOWED.name()))
                    .keyFlag(Boolean.TRUE.equals(routeOp.getKeyFlag()))
                    .plannedQty(order.getQty())
                    .completedQty(MesConstants.ZERO_QTY)
                    .scrapQty(MesConstants.ZERO_QTY)
                    // 资源与计划时间由 APS 下达计划时写入，下达阶段留空
                    .resId(MesConstants.NONE_ID)
                    .status(MesOpTaskStatusEnum.PENDING.name())
                    .version(MesConstants.INIT_VERSION)
                    .build();
            opTaskMapper.insert(task);
            tasks.add(task);
        }
        return tasks;
    }

    /**
     * 执行模式由并行组推导：{@code mdm_route_op} 上没有 exec_mode 列，有并行组就是可并行，否则必须按序。
     *
     * <p>OPTIONAL（可选工序）不在这里产生：可选与否由 {@code skip_policy} 表达，两个字段都用来
     * 表示"能不能不做"会出现互相矛盾的配置。
     */
    private String resolveExecMode(RouteOpRespDTO routeOp) {
        return StrUtil.isBlank(routeOp.getParallelGroup())
                ? MesOpExecModeEnum.SEQUENTIAL.name()
                : MesOpExecModeEnum.PARALLEL.name();
    }

    /**
     * 串 {@code prev_task_id} / {@code next_task_id}。
     *
     * <p>先把工序归成"步骤"：同一个 {@code parallel_group} 的多道工序合成一个步骤，无并行组的工序
     * 各自独立成一个步骤；然后相邻步骤两两相接。
     *
     * <p>前后序是单值列，而并行步骤里有多道工序，因此指向该步骤的<b>代表工序</b>（步骤内第一道，
     * 即 seq 最小、seq 相同时编号最小的那道）。这不是取巧：开工门禁沿 {@code prev_task_id} 找到前序后，
     * 发现它的 {@code parallel_group} 非空就展开同组全部工序判"是否全部完工"，
     * 单值列 + 按组展开正是 04 文档约定的编码方式（"后序工序的开工门禁要求并行组全部完工，不是任一完工"）。
     *
     * <p>结果满足三条：同一并行组内的工序 {@code prev} 指向同一个前序、{@code next} 指向同一个后序；
     * 首步骤 {@code prev} 为 0；末步骤 {@code next} 为 0。
     */
    private void chainTasks(List<OpTaskDO> tasks) {
        List<List<OpTaskDO>> steps = groupIntoSteps(tasks);
        for (int index = 0; index < steps.size(); index++) {
            Long prevTaskId = index == 0
                    ? MesConstants.NONE_ID : representative(steps.get(index - 1)).getId();
            Long nextTaskId = index == steps.size() - 1
                    ? MesConstants.NONE_ID : representative(steps.get(index + 1)).getId();
            for (OpTaskDO task : steps.get(index)) {
                task.setPrevTaskId(prevTaskId);
                task.setNextTaskId(nextTaskId);
                opTaskMapper.updateById(new OpTaskDO()
                        .setId(task.getId())
                        .setPrevTaskId(prevTaskId)
                        .setNextTaskId(nextTaskId));
            }
        }
    }

    /**
     * 把按 seq 升序的工序任务归成串行步骤序列。
     *
     * <p>并行组用第一次出现的位置定位整个步骤：喷漆与标识都是 {@code parallel_group=A}，它们合成一个
     * 步骤插在"加工"之后、"装配"之前，而不是各占一个步骤串成链——串成链就变成必须先喷漆再标识。
     */
    private List<List<OpTaskDO>> groupIntoSteps(List<OpTaskDO> tasks) {
        List<List<OpTaskDO>> steps = new ArrayList<>();
        Map<String, List<OpTaskDO>> stepByGroup = new HashMap<>();
        for (OpTaskDO task : tasks) {
            String parallelGroup = task.getParallelGroup();
            if (StrUtil.isBlank(parallelGroup)) {
                List<OpTaskDO> step = new ArrayList<>();
                step.add(task);
                steps.add(step);
                continue;
            }
            List<OpTaskDO> step = stepByGroup.get(parallelGroup);
            if (step == null) {
                step = new ArrayList<>();
                stepByGroup.put(parallelGroup, step);
                steps.add(step);
            }
            step.add(task);
        }
        return steps;
    }

    /** 步骤的代表工序：入参已按 seq 升序，取第一道即 seq 最小的那道。 */
    private OpTaskDO representative(List<OpTaskDO> step) {
        return step.get(0);
    }

}

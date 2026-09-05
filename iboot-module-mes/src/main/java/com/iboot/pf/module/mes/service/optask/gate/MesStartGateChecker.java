package com.iboot.pf.module.mes.service.optask.gate;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.exception.ServiceException;
import com.iboot.pf.module.mdm.api.route.RouteApi;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.dispatchtask.DispatchTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.mysql.dispatchtask.DispatchTaskMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesDispatchTaskStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 开工门禁校验链编排。
 *
 * <p>按 {@link MesStartGateItem#order()} 依次跑启用的校验项。两个入口对应两种问法：
 * {@link #check} 回答"能不能开工"，任一项失败立刻抛错，供 {@code start} 动作用；
 * {@link #inspect} 回答"现在为什么不能开工"，把所有不满足的项一次列全，供作业页预检用。
 *
 * <p>本类不推进任何状态：门禁只做判定，{@code READY -> RUNNING} 的推进在工序动作里。
 * 判定与推进混在一起的话，预检就会带副作用，作业页每刷一次都在改状态。
 */
@Service
public class MesStartGateChecker {

    @Resource
    private List<MesStartGateItem> gateItems;
    @Resource
    private DispatchTaskMapper dispatchTaskMapper;
    @Resource
    private RouteApi routeApi;

    /**
     * 校验能否开工，任一项不满足抛对应错误码。
     *
     * @param task 工序任务
     * @param ctx  上下文，可留空由本方法补全
     */
    public void check(OpTaskDO task, MesStartGateContext ctx) {
        MesStartGateContext context = prepare(task, ctx);
        for (MesStartGateItem item : sortedItems()) {
            if (item.enabled(task, context)) {
                item.check(task, context);
            }
        }
    }

    /**
     * 预检：跑完全部启用项并收集结果，不抛门禁类异常。
     *
     * <p>只吞 {@link ServiceException}——那是门禁给出的业务判定。门面调用失败、空指针这类
     * 系统故障继续往外抛：把它们当成"这一项没通过"会让车间对着"资源不可用"排查半天，
     * 实际是 mdm 挂了。
     */
    public MesStartGateResult inspect(OpTaskDO task, MesStartGateContext ctx) {
        MesStartGateContext context = prepare(task, ctx);
        MesStartGateResult result = new MesStartGateResult();
        for (MesStartGateItem item : sortedItems()) {
            if (!item.enabled(task, context)) {
                result.addSkipped(item.name());
                continue;
            }
            try {
                item.check(task, context);
                result.addPassed(item.name());
            } catch (ServiceException ex) {
                result.addFailed(item.name(), ex.getMessage());
            }
        }
        return result;
    }

    private List<MesStartGateItem> sortedItems() {
        return gateItems.stream().sorted(Comparator.comparingInt(MesStartGateItem::order)).toList();
    }

    /**
     * 补全上下文：路线工序加载一次，操作者没给就从派工任务取。
     *
     * <p>路线工序查不到时不抛错：路线工序被删或数据不全时，依赖它的校验项按"不启用"处理，
     * 让整条链在主数据缺失时退化成"不拦"而不是"全拦"——门禁的职责是拦住确定不满足的情况，
     * 不是拦住查不到数据的情况。
     */
    private MesStartGateContext prepare(OpTaskDO task, MesStartGateContext ctx) {
        MesStartGateContext context = ctx == null ? new MesStartGateContext() : ctx;
        if (context.getCheckDate() == null) {
            context.setCheckDate(LocalDate.now());
        }
        if (context.getRouteOp() == null && !isNone(task.getRouteOpId())) {
            Map<Long, RouteOpRespDTO> routeOpMap = routeApi.getRouteOpMap(List.of(task.getRouteOpId()));
            context.setRouteOp(routeOpMap == null ? null : routeOpMap.get(task.getRouteOpId()));
        }
        if (isNone(context.getOperatorId())) {
            context.setOperatorId(resolveOperatorId(task));
        }
        return context;
    }

    /**
     * 从派工任务取操作者：取未取消的派工里第一个指到具体人的。
     *
     * <p>派给班组（{@code operator_id = 0}）时取不到人，技能校验会因此拿不到操作者——
     * 这是对的：班组里谁来干还没定，就无法证明干活的人有资质。
     */
    private Long resolveOperatorId(OpTaskDO task) {
        List<DispatchTaskDO> dispatches =
                dispatchTaskMapper.selectList(DispatchTaskDO::getOpTaskId, task.getId());
        if (CollUtil.isEmpty(dispatches)) {
            return MesConstants.NONE_ID;
        }
        return dispatches.stream()
                .filter(item -> !MesDispatchTaskStatusEnum.CANCELLED.name().equals(item.getStatus()))
                .map(DispatchTaskDO::getOperatorId)
                .filter(operatorId -> operatorId != null && !MesConstants.NONE_ID.equals(operatorId))
                .findFirst().orElse(MesConstants.NONE_ID);
    }

    private boolean isNone(Long id) {
        return id == null || MesConstants.NONE_ID.equals(id);
    }

}

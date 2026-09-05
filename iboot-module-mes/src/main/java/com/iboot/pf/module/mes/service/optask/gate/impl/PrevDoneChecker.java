package com.iboot.pf.module.mes.service.optask.gate.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateContext;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateItem;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateNames;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_PREV_NOT_DONE;

/**
 * 门禁一：前序工序完成。
 *
 * <p>沿 {@code prev_task_id} 找前序。前序属于并行组时，同组全部工序都要完成——并行组的语义是
 * "这几道工序可以同时做"，后序要的是它们合起来的产出，只完成一道就往下走会拿到半成品。
 *
 * <p>只认 {@code DONE} 与 {@code SKIPPED} 两个状态。不认 {@code CANCELLED}：取消是异常终止，
 * 一道工序被取消通常意味着整条链要重排，把它当"已完成"会让后序在链已废弃的情况下照样开工。
 */
@Component
public class PrevDoneChecker implements MesStartGateItem {

    @Resource
    private OpTaskMapper opTaskMapper;

    @Override
    public String name() {
        return MesStartGateNames.PREV_DONE;
    }

    @Override
    public int order() {
        return 10;
    }

    /** 首道工序（{@code prev_task_id = 0}）没有前序可等，不启用。 */
    @Override
    public boolean enabled(OpTaskDO task, MesStartGateContext ctx) {
        return task.getPrevTaskId() != null && !MesConstants.NONE_ID.equals(task.getPrevTaskId());
    }

    @Override
    public void check(OpTaskDO task, MesStartGateContext ctx) {
        OpTaskDO prev = opTaskMapper.selectById(task.getPrevTaskId());
        if (prev == null) {
            // 前序被删或数据不全时不拦：门禁拦确定不满足的情况，不拦查不到数据的情况
            return;
        }
        List<OpTaskDO> required = requiredTasks(prev);
        List<OpTaskDO> unfinished = required.stream()
                .filter(item -> !isDoneOrSkipped(item.getStatus()))
                .sorted(Comparator.comparingInt(item -> item.getSeq() == null ? 0 : item.getSeq()))
                .toList();
        if (CollUtil.isNotEmpty(unfinished)) {
            throw exception(OP_TASK_PREV_NOT_DONE, describe(unfinished));
        }
    }

    /**
     * 要等的前序集合：前序不在并行组里就只等它，在并行组里就等同组全部。
     *
     * <p>同组范围限定在同一作业工单内：{@code parallel_group} 只是个字符串标记，
     * 不限工单的话别的工单里同名的并行组也会被算进来。
     */
    private List<OpTaskDO> requiredTasks(OpTaskDO prev) {
        if (StrUtil.isBlank(prev.getParallelGroup())) {
            return List.of(prev);
        }
        List<OpTaskDO> groupTasks = opTaskMapper.selectList(
                OpTaskDO::getWorkOrderId, prev.getWorkOrderId(),
                OpTaskDO::getParallelGroup, prev.getParallelGroup());
        return CollUtil.isEmpty(groupTasks) ? List.of(prev) : groupTasks;
    }

    private boolean isDoneOrSkipped(String status) {
        return MesOpTaskStatusEnum.DONE.name().equals(status)
                || MesOpTaskStatusEnum.SKIPPED.name().equals(status);
    }

    /** 消息给工序号与工序名称：车间按工序号就能在现场找到那道工序，给任务主键等于没给。 */
    private String describe(List<OpTaskDO> unfinished) {
        return unfinished.stream()
                .map(item -> "第" + item.getSeq() + "道 " + item.getOpName()
                        + "（" + statusText(item.getStatus()) + "）")
                .collect(Collectors.joining("、"));
    }

    /** 状态转中文。取值非法时原样输出，不让一个脏数据把门禁变成系统错误。 */
    private String statusText(String status) {
        for (MesOpTaskStatusEnum item : MesOpTaskStatusEnum.values()) {
            if (item.name().equals(status)) {
                return item.getName();
            }
        }
        return status;
    }

}

package com.iboot.pf.module.mes.service.optask.gate.impl;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.prodreport.ProdReportMapper;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
import com.iboot.pf.module.mes.dal.dataobject.prodreport.ProdReportDO;
import com.iboot.pf.module.mes.enums.MesQcConclusionEnum;
import com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateContext;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateItem;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateNames;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_FAI_NOT_PASSED;

/**
 * 门禁三：首件检验通过。
 *
 * <p>只在路线工序 {@code fai_flag} 为真时启用，且<b>本工序已经有过报工</b>才校验。
 *
 * <p>为什么要"已有过报工"这个前提：首件检验的对象是第一件产品，做出第一件必须先开工。
 * 如果开工就要求 FAI 通过，就成了死锁——要开工得先有 FAI，要有 FAI 得先做出首件，
 * 做首件得先开工。所以第一次开工（还没有任何报工）放行，首件报工之后再开工或恢复才校验。
 *
 * <p>判定要求 {@code status = DONE} 且结论合格两个条件同时成立：质检任务的结论与合格数量
 * 只有在完成时才落定，只看结论会读到检验中途的中间值。让步接收不算通过——
 * 让步接收要走不合格品处置单，门禁不替处置流程放行。
 */
@Component
public class FirstArticleChecker implements MesStartGateItem {

    @Resource
    private QcTaskMapper qcTaskMapper;
    @Resource
    private ProdReportMapper prodReportMapper;

    @Override
    public String name() {
        return MesStartGateNames.FIRST_ARTICLE;
    }

    @Override
    public int order() {
        return 30;
    }

    /**
     * 路线工序要求首件检验、且本工序已经有过报工时才启用。
     *
     * <p>把"已有报工"放在 {@code enabled} 而不是 {@code check} 里，是为了让预检结果如实显示
     * "这一项本次跳过"，而不是显示"通过"——车间看到"首件通过"会以为首件已经检过了。
     */
    @Override
    public boolean enabled(OpTaskDO task, MesStartGateContext ctx) {
        return ctx.faiRequired() && hasReported(task);
    }

    @Override
    public void check(OpTaskDO task, MesStartGateContext ctx) {
        List<QcTaskDO> faiTasks = qcTaskMapper.selectList(
                QcTaskDO::getBizNo, task.getTaskNo(),
                QcTaskDO::getTriggerPoint, MesQcTriggerPointEnum.FAI.name());
        if (CollUtil.isEmpty(faiTasks)) {
            throw exception(OP_TASK_FAI_NOT_PASSED, task.getOpName(), "没有首件检验任务");
        }
        boolean passed = faiTasks.stream().anyMatch(item ->
                MesQcTaskStatusEnum.isDone(item.getStatus())
                        && MesQcConclusionEnum.isQualified(item.getConclusion()));
        if (!passed) {
            throw exception(OP_TASK_FAI_NOT_PASSED, task.getOpName(), describe(faiTasks));
        }
    }

    private boolean hasReported(OpTaskDO task) {
        return prodReportMapper.selectCount(ProdReportDO::getOpTaskId, task.getId()) > 0;
    }

    /** 消息给首件任务号与它当前的状态或结论，车间知道该去催检验还是去处理不合格。 */
    private String describe(List<QcTaskDO> faiTasks) {
        return faiTasks.stream().map(item -> item.getTaskNo() + " "
                        + (MesQcTaskStatusEnum.isDone(item.getStatus())
                        ? conclusionText(item.getConclusion()) : statusText(item.getStatus())))
                .reduce((left, right) -> left + "、" + right).orElse("");
    }

    private String conclusionText(String conclusion) {
        for (MesQcConclusionEnum item : MesQcConclusionEnum.values()) {
            if (item.name().equals(conclusion)) {
                return item.getName();
            }
        }
        return "结论未落定";
    }

    private String statusText(String status) {
        for (MesQcTaskStatusEnum item : MesQcTaskStatusEnum.values()) {
            if (item.name().equals(status)) {
                return item.getName();
            }
        }
        return status;
    }

}

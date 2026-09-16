package com.iboot.pf.module.mes.service.prodreport;

import com.iboot.pf.module.mes.service.prodreport.bo.MesCancelReportBO;
import com.iboot.pf.module.mes.service.prodreport.bo.MesSubmitReportBO;

/**
 * 生产报工领域动作。
 *
 * <p>与 codegen 的 {@link ProdReportService} 并存，分工是硬的：codegen Service 负责报工记录的增删改查，
 * 本服务负责"报工推进链"——写报工记录、留在制流水、把数量推进到工序任务与两级工单。
 * 数量只能由这里推进，表单不许直填工序任务的 {@code completed_qty}。
 *
 * <p>数量推进口径见 {@link MesProgressPropagator}，那是本任务的核心：
 * 工序任务累加、两级工单取末道工序，写错一处进度链就不可信。
 *
 * <p>依据 {@code dev-docs/业务流程/04-MES生产执行流程.md} 的"流程六：报工推进链"与
 * {@code 08-跨模块协作与端到端时序.md} 的"2.3 报工到完工入库"。
 */
public interface MesProdReportDomainService {

    /**
     * 提交报工。
     *
     * <p>四件事：写 {@code mes_prod_report}（{@code status = SUBMITTED}）、写 {@code REPORT} 流水
     * （{@code qty} 记本次合格数）、把数量推进到三级、按数量与状态判定两级工单是否完工。
     *
     * <p>幂等走报工记录自己的 {@code idempotency_key} 列：重复提交直接返回首次那条报工的编号，
     * 不会产生第二条记录、也不会二次推进数量。
     *
     * @param bo 报工参数，幂等键必填，格式 {@code UI:{taskNo}:REPORT:{seq}}
     * @return 报工记录编号；重复提交时返回首次那条
     */
    Long submitReport(MesSubmitReportBO bo);

    /**
     * 取消报工：{@code SUBMITTED}/{@code CONFIRMED} -&gt; {@code CANCELLED}，同时把三级数量反向扣减并写反向流水。
     *
     * <p>已完工的工序不允许取消它的报工：完工时按"合格 + 报废 = 计划数"校验过，抽掉一笔报工会让工序
     * 停在"数量不够却已完工"的状态，而 {@code DONE} 是终态没有回退边，自己圆不回来。
     *
     * @param bo 取消参数，幂等键格式 {@code UI:{reportNo}:CANCEL_PROD_REPORT:0}
     */
    void cancelReport(MesCancelReportBO bo);

}

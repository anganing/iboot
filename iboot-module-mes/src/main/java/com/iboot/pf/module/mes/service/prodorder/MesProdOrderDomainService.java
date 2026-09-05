package com.iboot.pf.module.mes.service.prodorder;

import com.iboot.pf.module.mes.service.prodorder.bo.MesCloseOrderBO;
import com.iboot.pf.module.mes.service.prodorder.bo.MesOrderActionBO;
import com.iboot.pf.module.mes.service.prodorder.bo.MesOrderCloseResultBO;
import com.iboot.pf.module.mes.service.prodorder.bo.MesReleaseOrderBO;

/**
 * 生产工单领域动作。
 *
 * <p>与 codegen 的 {@link ProdOrderService} 并存，分工是硬的：codegen Service 负责建单、改单、
 * 删单与查询，本服务负责一切改状态的动作。工单状态只能由这里推进，表单不许直填——表单能填状态就意味着
 * 下达、齐套、领料、报工全部环节都可以被绕过。
 *
 * <p>四个动作共同遵守：状态由 {@link com.iboot.pf.module.mes.enums.MesProdOrderStateMachine} 裁决、
 * 更新带 {@code version} 乐观锁条件、每次变更经
 * {@link com.iboot.pf.module.mes.service.wiptxn.MesWipTxnRecorder} 留痕、
 * 幂等走 {@link com.iboot.pf.module.mes.service.command.MesCommandService}。
 */
public interface MesProdOrderDomainService {

    /**
     * 下达生产工单：{@code DRAFT} -&gt; {@code RELEASED}。
     *
     * <p>四件事缺一件后面全乱：锁生效 BOM 版本、锁生效工艺路线版本、按路线展开作业工单与工序任务、
     * 写下达时间与在制流水。
     *
     * @param bo 下达参数，幂等键格式 {@code UI:{orderNo}:RELEASE_PROD_ORDER:0}
     */
    void releaseProdOrder(MesReleaseOrderBO bo);

    /**
     * 挂起生产工单：{@code RUNNING} -&gt; {@code HOLD}。
     *
     * @param bo 动作参数，幂等键格式 {@code UI:{orderNo}:HOLD_PROD_ORDER:0}
     */
    void holdProdOrder(MesOrderActionBO bo);

    /**
     * 恢复生产工单：{@code HOLD} -&gt; {@code RUNNING}。
     *
     * @param bo 动作参数，幂等键格式 {@code UI:{orderNo}:RESUME_PROD_ORDER:0}
     */
    void resumeProdOrder(MesOrderActionBO bo);

    /**
     * 取消生产工单：开工前三态 -&gt; {@code CANCELLED}，并级联取消作业工单与工序任务。
     *
     * @param bo 动作参数，幂等键格式 {@code UI:{orderNo}:CANCEL_PROD_ORDER:0}
     */
    void cancelProdOrder(MesOrderActionBO bo);

    /**
     * 关闭生产工单：{@code DONE} -&gt; {@code CLOSED}，并做账目收尾。
     *
     * <p>四件事缺一件账就不平（{@link MesOrderCloseHelper}）：释放仍挂在未确认领料申请上的剩余预留、
     * 按实际产量给倒冲件反冲扣料、需求下全部工单收尾时回写需求 {@code DONE}、写一条 {@code COMPLETE} 流水。
     * 不释放预留，那部分可用量会被一张已收尾的工单永久占着；不反冲，倒冲件的消耗一分钱都不入账。
     *
     * <p><b>多余领料不自动退回</b>，只把清单返回给调用方。车间关单时料可能还在工位上，
     * 自动退料等于把还在用的料从账上收走。
     *
     * @param bo 关闭参数，幂等键格式 {@code UI:{orderNo}:CLOSE_PROD_ORDER:0}
     * @return 未退回的多余领料清单
     */
    MesOrderCloseResultBO closeProdOrder(MesCloseOrderBO bo);

}

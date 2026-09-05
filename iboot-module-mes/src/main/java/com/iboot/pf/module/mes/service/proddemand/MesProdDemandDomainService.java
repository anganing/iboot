package com.iboot.pf.module.mes.service.proddemand;

import com.iboot.pf.module.mes.service.proddemand.bo.MesConfirmDemandBO;
import com.iboot.pf.module.mes.service.proddemand.bo.MesDemandToOrderBO;

/**
 * 生产需求领域动作 Service。
 *
 * <p>与 codegen 的 {@link ProdDemandService} 并存、分工明确：codegen 那份只管建改删查，
 * 状态一律不由表单维护；需求状态只能由这里的三个动作推进，这是"一个业务事实只有一条写入路径"在需求域的落法。
 *
 * <p>状态链路（见 {@code dev-docs/业务流程/04-MES生产执行流程.md} 的"流程一：生产需求"）：
 * <pre>
 * DRAFT --confirm--> CONFIRMED --（APS 排程纳入）--> PLANNED --toProdOrder--> RELEASED --（工单关闭回写）--> DONE
 *   任一非终态 --cancel--> CANCELLED
 * </pre>
 *
 * <p>需求状态机只有这几条边，且没有分支合并，直接在实现里校验即可，不单独建状态机类：
 * 生产工单、作业工单、工序任务那三套才需要迁移表。
 */
public interface MesProdDemandDomainService {

    /**
     * 确认生产需求：{@code DRAFT} 到 {@code CONFIRMED}。
     *
     * <p>确认是"这批需求要投产"的承诺点：确认后需求主体字段不再允许改动，也才允许被 APS 排程读取与转工单。
     *
     * @param bo 动作参数，幂等键留空时按 {@code {来源模块}:{需求编号}:CONFIRM_DEMAND:0} 派生
     */
    void confirmDemand(MesConfirmDemandBO bo);

    /**
     * 取消生产需求：任一非终态到 {@code CANCELLED}。
     *
     * @param bo 动作参数，{@code remark} 记取消原因；幂等键留空时按
     *           {@code {来源模块}:{需求编号}:CANCEL_DEMAND:0} 派生
     */
    void cancelDemand(MesConfirmDemandBO bo);

    /**
     * 生产需求转生产工单：{@code CONFIRMED} 或 {@code PLANNED} 到 {@code RELEASED}。
     *
     * <p>支持部分转单，可多次调用直到累计转单量等于需求量：未转完时需求保持当前状态，转完才置
     * {@code RELEASED}。新建的工单落 {@code DRAFT}，BOM 与工艺版本要等下达动作那一刻才锁，这里不碰。
     *
     * @param bo 动作参数，部分转单时幂等键应由调用方为每一次操作生成
     * @return 本次创建的生产工单编号；同一幂等键重复下发时返回首次创建的那张工单
     */
    Long demandToProdOrder(MesDemandToOrderBO bo);

}

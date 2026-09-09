package com.iboot.pf.module.mes.service.qcplan;

import com.iboot.pf.module.mes.service.qcplan.bo.MesQcPlanActionBO;

/**
 * 检验方案领域动作。
 *
 * <p>与 codegen 的 {@link QcPlanService} 并存，分工与生产工单域相同：codegen Service 负责建单、
 * 改单、删单与查询，本服务负责一切改状态的动作。方案的发布与停用只能由这里推进，表单不许直填——
 * 方案的 {@code status} 被 T2-01 规则解析与 T2-02 任务生成用作判定口径的版本锁，
 * 表单能填状态就意味着检验项还没配齐就能发布、被规则引用着就能停用。
 *
 * <p>两个动作共同遵守：目标状态由动作名决定、迁移合法性按当前状态校验、
 * 幂等走 {@link com.iboot.pf.module.mes.service.command.MesCommandService}。
 */
public interface MesQcPlanDomainService {

    /**
     * 发布检验方案：{@code DRAFT} -&gt; {@code PUBLISHED}。
     *
     * <p>方案发布后进入版本锁定：质检规则只能引用已发布方案，质检任务生成时锁
     * {@code qc_plan_id + qc_plan_version}，此后方案与检验项都不允许再改（见 codegen
     * 两个 Service 的非 DRAFT 校验）。发布前校验至少有一个检验项——空方案发布了
     * 也生成不出任何 {@code mes_qc_result} 空行，检验流程在录入环节必然卡死。
     *
     * @param bo 动作参数，幂等键格式 {@code UI:{方案编码}:PUBLISH_QC_PLAN:0}
     */
    void publishPlan(MesQcPlanActionBO bo);

    /**
     * 停用检验方案：{@code PUBLISHED} -&gt; {@code DISABLED}。
     *
     * <p>停用是下线的第一步：停用后规则解析不再命中它（T2-01 只按 {@code PUBLISHED} 引用），
     * 但历史任务仍按锁定版本判定。有启用规则引用时拒绝停用——规则还在往这个方案上派活，
     * 停了它等于让下一次任务生成直接报错，应当先把规则改挂到别的方案或停用规则。
     *
     * @param bo 动作参数，幂等键格式 {@code UI:{方案编码}:DISABLE_QC_PLAN:0}
     */
    void disablePlan(MesQcPlanActionBO bo);

}

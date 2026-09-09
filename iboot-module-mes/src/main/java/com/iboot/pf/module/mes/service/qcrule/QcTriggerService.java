package com.iboot.pf.module.mes.service.qcrule;

import com.iboot.pf.module.mes.dal.dataobject.qcrule.QcRuleDO;
import com.iboot.pf.module.mes.service.qcrule.bo.QcTriggerContextBO;

import java.util.Optional;

/**
 * 质检规则触发解析。
 *
 * <p>与 codegen 的 {@link QcRuleService} 并存，分工是硬的：codegen Service 负责规则
 * 配置数据的增删改查，本服务负责把规则表"变成活的"——五个检验点（IQC / FAI / IPQC /
 * FQC / OQC）各自只是在业务动作里调一次 {@link #resolve}，解析逻辑收敛在这一处，
 * 改检验流程只改规则数据不改代码。
 *
 * <p>匹配语义见 {@code dev-docs/业务流程/05-MES质量子域流程.md} 的"规则解析"节：
 * 六个维度"与关系"，规则字段 0 表示不限；命中的规则由调用方记到
 * {@code mes_qc_task.rule_id}，事后能回答"为什么这批要检 / 不检"。
 *
 * @author iboot
 */
public interface QcTriggerService {

    /**
     * 按触发点与业务上下文解析应触发的质检规则。
     *
     * <p>解析流程：按 {@code trigger_point + status = 启用} 一次查询候选规则，
     * 逐维度"与关系"匹配（规则字段 0 表示不限，非 0 必须与上下文相等），
     * {@code object_type} 必须相等；命中多条按 {@code priority} 降序取第一条，
     * 同优先级取 {@code id} 小的。未命中返回空，业务动作直接放行。
     *
     * <p>命中后会校验规则引用的检验方案已发布，方案非 {@code PUBLISHED} 抛
     * {@code QC_PLAN_NOT_PUBLISHED}——规则配置校验挡不住方案发布后被停用的场景。
     *
     * @param triggerPoint 触发点，取值 {@link com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum}
     * @param ctx          业务对象匹配上下文
     * @return 命中的质检规则；未命中返回 {@link Optional#empty()}
     */
    Optional<QcRuleDO> resolve(String triggerPoint, QcTriggerContextBO ctx);

}

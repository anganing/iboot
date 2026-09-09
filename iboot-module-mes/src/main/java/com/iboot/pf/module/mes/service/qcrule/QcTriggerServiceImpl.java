package com.iboot.pf.module.mes.service.qcrule;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import com.iboot.pf.module.mes.dal.dataobject.qcrule.QcRuleDO;
import com.iboot.pf.module.mes.dal.mysql.qcplan.QcPlanMapper;
import com.iboot.pf.module.mes.dal.mysql.qcrule.QcRuleMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesQcPlanStatusEnum;
import com.iboot.pf.module.mes.service.qcrule.bo.QcTriggerContextBO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_NOT_PUBLISHED;

/**
 * 质检规则触发解析实现。
 *
 * <p>匹配在应用层做而不是压进 SQL：维度是"0 表示不限"的稀疏匹配，SQL 写出来是一长串
 * {@code OR (field = 0 OR field = ?)}，索引形同虚设；规则按触发点查询后单表数据量很小，
 * 内存过滤 + 排序更可解释——未命中时能精确说出哪个维度没匹配上，这正是本服务要的可解释性。
 *
 * @author iboot
 */
@Slf4j
@Service
public class QcTriggerServiceImpl implements QcTriggerService {

    @Resource
    private QcRuleMapper qcRuleMapper;
    @Resource
    private QcPlanMapper qcPlanMapper;

    @Override
    public Optional<QcRuleDO> resolve(String triggerPoint, QcTriggerContextBO ctx) {
        // 按 trigger_point + status = 启用一次查询，结果缓存在方法内，后面只做内存过滤
        List<QcRuleDO> candidates = qcRuleMapper.selectList(
                QcRuleDO::getTriggerPoint, triggerPoint,
                QcRuleDO::getStatus, CommonStatusEnum.ENABLE.getStatus());
        if (CollUtil.isEmpty(candidates)) {
            log.debug("[resolve][触发点 {} 无启用规则，检验放行]", triggerPoint);
            return Optional.empty();
        }

        // 最优 = 优先级最高；同优先级取 id 小的（先配置的）
        Optional<QcRuleDO> matched = candidates.stream()
                .filter(rule -> matchObjectAndDimensions(rule, ctx))
                .min(Comparator.comparing(QcRuleDO::getPriority).reversed()
                        .thenComparing(QcRuleDO::getId));
        if (matched.isEmpty()) {
            log.debug("[resolve][触发点 {} 候选规则 {} 条均因维度不匹配未命中，业务放行]", triggerPoint, candidates.size());
            return Optional.empty();
        }

        QcRuleDO rule = matched.get();
        validatePlanPublished(rule);
        log.info("[resolve][触发点 {} 命中质检规则 {}({})，优先级 {}，方案 {}]",
                triggerPoint, rule.getCode(), rule.getId(), rule.getPriority(), rule.getQcPlanId());
        return matched;
    }

    /**
     * 匹配对象类型与五个稀疏维度，全部为"与关系"。
     *
     * <p>规则字段为 {@link MesConstants#NONE_ID}（0）表示不限，非 0 时必须与上下文对应值相等；
     * 上下文字段为 null 或 0 视为"该维度不参与限定"，只有限定了该维度的规则才不命中。
     */
    private boolean matchObjectAndDimensions(QcRuleDO rule, QcTriggerContextBO ctx) {
        if (!Objects.equals(rule.getObjectType(), ctx.getObjectType())) {
            log.debug("[resolve][规则 {}({}) 未命中：object_type 规则 {} != 上下文 {}]",
                    rule.getCode(), rule.getId(), rule.getObjectType(), ctx.getObjectType());
            return false;
        }
        return matchDimension(rule, ctx, "mat_id", rule.getMatId(), ctx.getMatId())
                && matchDimension(rule, ctx, "mat_cat_id", rule.getMatCatId(), ctx.getMatCatId())
                && matchDimension(rule, ctx, "op_id", rule.getOpId(), ctx.getOpId())
                && matchDimension(rule, ctx, "wc_id", rule.getWcId(), ctx.getWcId())
                && matchDimension(rule, ctx, "supplier_id", rule.getSupplierId(), ctx.getSupplierId());
    }

    private boolean matchDimension(QcRuleDO rule, QcTriggerContextBO ctx,
                                   String dimension, Long ruleValue, Long ctxValue) {
        if (Objects.equals(ruleValue, MesConstants.NONE_ID)) {
            return true;
        }
        // 上下文没带该维度（null 或 0）时，任何限定了该维度的规则都不可能命中
        if (ctxValue == null || Objects.equals(ctxValue, MesConstants.NONE_ID)) {
            log.debug("[resolve][规则 {}({}) 未命中：维度 {} 规则限定 {} 但上下文未提供]",
                    rule.getCode(), rule.getId(), dimension, ruleValue);
            return false;
        }
        if (!Objects.equals(ruleValue, ctxValue)) {
            log.debug("[resolve][规则 {}({}) 未命中：维度 {} 规则限定 {} != 上下文 {}]",
                    rule.getCode(), rule.getId(), dimension, ruleValue, ctxValue);
            return false;
        }
        return true;
    }

    /**
     * 校验规则引用的检验方案已发布。
     *
     * <p>规则保存时校验过一次，但方案发布后可能被停用（T2-03 的 {@code disablePlan}），
     * 触发时不再校验就会拿停用方案生成任务，所以这里必须重验。
     */
    private void validatePlanPublished(QcRuleDO rule) {
        QcPlanDO plan = qcPlanMapper.selectById(rule.getQcPlanId());
        if (plan == null || !MesQcPlanStatusEnum.isPublished(plan.getStatus())) {
            log.warn("[resolve][规则 {}({}) 引用的方案 {} 不存在或未发布，实际状态 {}]",
                    rule.getCode(), rule.getId(), rule.getQcPlanId(), plan == null ? "不存在" : plan.getStatus());
            throw exception(QC_PLAN_NOT_PUBLISHED);
        }
    }

}

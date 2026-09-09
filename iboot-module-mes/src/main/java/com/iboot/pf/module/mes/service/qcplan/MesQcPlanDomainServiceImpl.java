package com.iboot.pf.module.mes.service.qcplan;

import cn.hutool.core.util.StrUtil;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.dal.dataobject.qcitem.QcItemDO;
import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import com.iboot.pf.module.mes.dal.dataobject.qcrule.QcRuleDO;
import com.iboot.pf.module.mes.dal.mysql.qcitem.QcItemMapper;
import com.iboot.pf.module.mes.dal.mysql.qcplan.QcPlanMapper;
import com.iboot.pf.module.mes.dal.mysql.qcrule.QcRuleMapper;
import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.module.mes.enums.MesCommandTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcPlanStatusEnum;
import com.iboot.pf.module.mes.service.command.MesCommandService;
import com.iboot.pf.module.mes.service.qcplan.bo.MesQcPlanActionBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_IN_USE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_PUBLISH_WITHOUT_ITEM;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_STATUS_INVALID;

/**
 * 检验方案领域动作实现。
 *
 * <p>两个动作共用一套骨架：查方案 -&gt; 命令幂等 -&gt; 当前状态校验目标迁移 -&gt; 落库。
 * 幂等键留空时按 {@code {来源模块}:{方案编码}:{命令类型}:0} 派生，与生产工单域动作的
 * 四段格式一致；派生用 {@link MesCommandTypeEnum} 的枚举名而不是字面量，命令改名后
 * 历史键仍然对得上。
 */
@Service
public class MesQcPlanDomainServiceImpl implements MesQcPlanDomainService {

    /** 幂等键的段分隔符，格式见 08 文档的幂等键全表。 */
    private static final String KEY_SEPARATOR = ":";
    /** 方案级动作没有行号，幂等键第四段固定 0。 */
    private static final String WHOLE_PLAN_LINE_NO = "0";

    @Resource
    private QcPlanMapper qcPlanMapper;
    @Resource
    private QcItemMapper qcItemMapper;
    @Resource
    private QcRuleMapper qcRuleMapper;
    @Resource
    private MesCommandService commandService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishPlan(MesQcPlanActionBO bo) {
        QcPlanDO plan = requireQcPlan(bo.getPlanId());
        commandService.executeVoid(MesCommandTypeEnum.PUBLISH_QC_PLAN,
                buildCommand(bo, MesCommandTypeEnum.PUBLISH_QC_PLAN, plan.getCode()), () -> {
                    requireDraft(plan.getStatus());
                    // 空方案发布了也展开不出检验项，录入环节必然卡死，发布前拦下
                    if (qcItemMapper.selectCount(QcItemDO::getPlanId, plan.getId()) == 0) {
                        throw exception(QC_PLAN_PUBLISH_WITHOUT_ITEM);
                    }
                    plan.setStatus(MesQcPlanStatusEnum.PUBLISHED.name());
                    qcPlanMapper.updateById(plan);
                });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disablePlan(MesQcPlanActionBO bo) {
        QcPlanDO plan = requireQcPlan(bo.getPlanId());
        commandService.executeVoid(MesCommandTypeEnum.DISABLE_QC_PLAN,
                buildCommand(bo, MesCommandTypeEnum.DISABLE_QC_PLAN, plan.getCode()), () -> {
                    // 停用只认已发布：草稿不叫"下线"，直接删；已停用没有再停一次的意义
                    if (!MesQcPlanStatusEnum.isPublished(plan.getStatus())) {
                        throw exception(QC_PLAN_STATUS_INVALID);
                    }
                    // 启用中的规则还在往方案上派活，停了它下一次任务生成就报错，先改规则再停方案
                    List<QcRuleDO> usingRules = qcRuleMapper.selectList(
                            QcRuleDO::getQcPlanId, plan.getId());
                    boolean enabledRuleExists = usingRules.stream()
                            .anyMatch(rule -> CommonStatusEnum.isEnable(rule.getStatus()));
                    if (enabledRuleExists) {
                        throw exception(QC_PLAN_IN_USE);
                    }
                    plan.setStatus(MesQcPlanStatusEnum.DISABLED.name());
                    qcPlanMapper.updateById(plan);
                });
    }

    /** 只有草稿允许发布，其余状态连"再发布一次"都不该有出边。 */
    private void requireDraft(String status) {
        if (!MesQcPlanStatusEnum.isDraft(status)) {
            throw exception(QC_PLAN_STATUS_INVALID);
        }
    }

    private QcPlanDO requireQcPlan(Long id) {
        QcPlanDO plan = id == null ? null : qcPlanMapper.selectById(id);
        if (plan == null) {
            throw exception(QC_PLAN_NOT_EXISTS);
        }
        return plan;
    }

    /**
     * 组装幂等命令。
     *
     * <p>幂等键调用方给了就以它为准，留空时按四段格式
     * {@code {来源模块}:{方案编码}:{命令类型}:{行号}} 派生。方案级动作没有行号，第四段固定 0。
     * 来源单号兜底成方案编码，因为 {@code mes_command_record.source_biz_no} 是 NOT NULL。
     */
    private MesCommandReqDTO buildCommand(MesQcPlanActionBO bo, MesCommandTypeEnum commandType, String planCode) {
        PlanCommand command = new PlanCommand();
        command.setSourceModule(bo.getSourceModule());
        command.setSourceBizNo(StrUtil.isBlank(bo.getSourceBizNo()) ? planCode : bo.getSourceBizNo());
        command.setSourceBizLineNo(bo.getSourceBizLineNo());
        command.setIdempotencyKey(StrUtil.isBlank(bo.getIdempotencyKey())
                ? String.join(KEY_SEPARATOR, bo.getSourceModule(), planCode,
                commandType.name(), WHOLE_PLAN_LINE_NO)
                : bo.getIdempotencyKey());
        return command;
    }

    /**
     * 幂等命令载体。
     *
     * <p>{@link MesCommandReqDTO} 是抽象类，方案域给它一个最小实现：既不必新建一个公共 DTO，
     * 也不用把派生出来的幂等键写回调用方的 BO——改调用方传进来的对象是隐蔽的副作用。
     */
    private static class PlanCommand extends MesCommandReqDTO {
    }

}

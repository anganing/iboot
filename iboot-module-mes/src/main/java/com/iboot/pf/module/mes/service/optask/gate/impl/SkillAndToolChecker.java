package com.iboot.pf.module.mes.service.optask.gate.impl;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.module.mdm.api.skill.SkillApi;
import com.iboot.pf.module.mdm.api.skill.dto.MemberSkillRespDTO;
import com.iboot.pf.module.mdm.api.skill.dto.OpSkillReqRespDTO;
import com.iboot.pf.module.mdm.api.skill.dto.SkillRespDTO;
import com.iboot.pf.module.mdm.api.tool.ToolApi;
import com.iboot.pf.module.mdm.api.tool.dto.RouteOpToolRespDTO;
import com.iboot.pf.module.mdm.api.tool.dto.ToolRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateContext;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateItem;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateNames;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_SKILL_NOT_MATCH;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_TOOL_NOT_READY;

/**
 * 门禁五：操作者技能与工装满足。
 *
 * <p>技能要求挂在<b>标准工序</b>（{@code mdm_op_skill_req.op_id}）而不是路线工序上，
 * 所以要顺着 {@code route_op.op_id} 去查——每条路线重复维护一遍技能要求是主数据设计要避免的事。
 *
 * <p>操作者取派工任务上的人或调用方入参。有技能要求但取不到操作者时直接拦：无法证明干活的人有资质，
 * 就等于不满足。放过去等于把资质校验变成"填了就查、不填就免检"。
 *
 * <p>技能有效性由本类判定（{@code getMemberSkillList} 不过滤状态与有效期，它是通用批量取数）：
 * 要求技能记录启用、生效日期不晚于基准日、失效日期为空或不早于基准日，且等级不低于要求。
 *
 * <p>工装只校验"必备的工装存在且启用"。{@code mdm_tool} 只记保有数量，没有当前占用量与寿命累计，
 * 所以做不到"这把刀还剩多少寿命"，那需要工装台账，不在本轮范围。
 */
@Component
public class SkillAndToolChecker implements MesStartGateItem {

    /** 启用状态值，与 {@code mdm_member_skill.status}、{@code mdm_op_skill_req.status}、{@code mdm_tool.status} 的 0-启用 一致。 */
    private static final Integer STATUS_ENABLED = 0;

    @Resource
    private SkillApi skillApi;
    @Resource
    private ToolApi toolApi;

    @Override
    public String name() {
        return MesStartGateNames.SKILL_AND_TOOL;
    }

    @Override
    public int order() {
        return 50;
    }

    /** 没挂路线工序就既查不到技能要求也查不到工装，无从判定。 */
    @Override
    public boolean enabled(OpTaskDO task, MesStartGateContext ctx) {
        return task.getRouteOpId() != null && !MesConstants.NONE_ID.equals(task.getRouteOpId());
    }

    @Override
    public void check(OpTaskDO task, MesStartGateContext ctx) {
        checkSkill(task, ctx);
        checkTool(task);
    }

    private void checkSkill(OpTaskDO task, MesStartGateContext ctx) {
        Long opId = ctx.opId();
        if (opId == null || MesConstants.NONE_ID.equals(opId)) {
            return;
        }
        List<OpSkillReqRespDTO> requirements = skillApi.getOpSkillReqList(List.of(opId)).stream()
                .filter(item -> STATUS_ENABLED.equals(item.getStatus())).toList();
        if (CollUtil.isEmpty(requirements)) {
            return;
        }
        Long operatorId = ctx.getOperatorId();
        if (operatorId == null || MesConstants.NONE_ID.equals(operatorId)) {
            throw exception(OP_TASK_SKILL_NOT_MATCH, task.getOpName(), "未指定作业人员，无法校验资质");
        }
        Map<Long, Integer> levelBySkill = effectiveLevels(operatorId, ctx.getCheckDate());
        List<OpSkillReqRespDTO> unmet = requirements.stream()
                .filter(item -> !meets(levelBySkill, item)).toList();
        if (CollUtil.isNotEmpty(unmet)) {
            throw exception(OP_TASK_SKILL_NOT_MATCH, task.getOpName(), describeSkill(unmet, levelBySkill));
        }
    }

    /** 满足一条要求：该技能当天有效且等级不低于要求等级。 */
    private boolean meets(Map<Long, Integer> levelBySkill, OpSkillReqRespDTO requirement) {
        Integer level = levelBySkill.get(requirement.getSkillId());
        if (level == null) {
            return false;
        }
        int minLevel = requirement.getMinLevel() == null ? 0 : requirement.getMinLevel();
        return level >= minLevel;
    }

    /**
     * 操作者在基准日有效的技能与等级，同一技能有多条时取最高等级。
     *
     * <p>取最高而不是取第一条：同一个人对同一技能可能有历史升级记录，按查询顺序取会随机拿到旧等级。
     */
    private Map<Long, Integer> effectiveLevels(Long operatorId, LocalDate checkDate) {
        List<MemberSkillRespDTO> skills = skillApi.getMemberSkillList(List.of(operatorId));
        Map<Long, Integer> levelBySkill = new LinkedHashMap<>();
        for (MemberSkillRespDTO skill : skills) {
            if (!effective(skill, checkDate)) {
                continue;
            }
            int level = skill.getLevel() == null ? 0 : skill.getLevel();
            levelBySkill.merge(skill.getSkillId(), level, Math::max);
        }
        return levelBySkill;
    }

    /** 技能记录在基准日是否有效：启用、已生效、未失效。失效日期为空表示长期有效。 */
    private boolean effective(MemberSkillRespDTO skill, LocalDate checkDate) {
        if (!STATUS_ENABLED.equals(skill.getStatus())) {
            return false;
        }
        if (skill.getEffectiveDate() != null && skill.getEffectiveDate().isAfter(checkDate)) {
            return false;
        }
        return skill.getExpiryDate() == null || !skill.getExpiryDate().isBefore(checkDate);
    }

    private void checkTool(OpTaskDO task) {
        List<RouteOpToolRespDTO> requiredTools =
                toolApi.getRouteOpToolList(List.of(task.getRouteOpId())).stream()
                        .filter(item -> Boolean.TRUE.equals(item.getRequiredFlag())).toList();
        if (CollUtil.isEmpty(requiredTools)) {
            return;
        }
        Map<Long, ToolRespDTO> toolMap = toolApi.getToolList().stream()
                .collect(Collectors.toMap(ToolRespDTO::getId, Function.identity(), (left, right) -> left));
        List<String> unavailable = new ArrayList<>();
        for (RouteOpToolRespDTO required : requiredTools) {
            ToolRespDTO tool = toolMap.get(required.getToolId());
            if (tool == null) {
                unavailable.add(required.getToolId() + " 工装不存在");
                continue;
            }
            if (!STATUS_ENABLED.equals(tool.getStatus())) {
                unavailable.add(tool.getCode() + " " + tool.getName() + " 已停用");
            }
        }
        if (CollUtil.isNotEmpty(unavailable)) {
            throw exception(OP_TASK_TOOL_NOT_READY, task.getOpName(), String.join("、", unavailable));
        }
    }

    /** 消息给技能名称、要求等级与实际等级，车间知道是该换人还是该去补资质。 */
    private String describeSkill(List<OpSkillReqRespDTO> unmet, Map<Long, Integer> levelBySkill) {
        Map<Long, String> skillNames = skillApi.getSkillList().stream()
                .collect(Collectors.toMap(SkillRespDTO::getId, SkillRespDTO::getName, (left, right) -> left));
        return unmet.stream().map(item -> {
            String skillText = skillNames.getOrDefault(item.getSkillId(), String.valueOf(item.getSkillId()));
            Integer level = levelBySkill.get(item.getSkillId());
            return skillText + " 要求等级 " + item.getMinLevel()
                    + "，实际 " + (level == null ? "无此技能" : String.valueOf(level));
        }).collect(Collectors.joining("、"));
    }

}

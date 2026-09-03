package com.iboot.pf.module.aps.service.solversnapshot;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.iboot.pf.framework.common.util.json.JsonUtils;
import com.iboot.pf.module.aps.controller.admin.solversnapshot.vo.SolverSnapshotConstraintMatchRespVO;
import com.iboot.pf.module.aps.controller.admin.solversnapshot.vo.SolverSnapshotRespVO;
import com.iboot.pf.module.aps.solver.constraint.ApsConstraintCatalog;
import com.iboot.pf.module.aps.solver.constraint.ApsConstraintDefinition;
import com.iboot.pf.module.aps.solver.constraint.ApsConstraintNames;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 把 {@code CONSTRAINT_MATCH} 快照 JSON 翻成前端能直接渲染的对照表。
 *
 * <p>放在 Service 层而不是前端：快照里的权重与得分是 Timefold 的 {@code HardSoftLongScore}
 * 字符串（形如 {@code 0hard/-1soft}），这个格式属于求解器细节。前端自己解析的话，
 * 将来分数类型从 {@code HardSoftLong} 换成别的，页面会静默显示空白。
 *
 * <p>历史快照兼容是刚需：{@code aps_solver_snapshot} 里存着结构不同的老数据（早期版本写的是
 * {@code matches/constraint/count/score} 扁平结构）。解析不出来时返回空清单，让前端回落到
 * 原始 JSON 展示，而不是抛错——快照是追溯证据，读不动某一条不该挡住看其余部分。
 */
public final class ApsConstraintMatchConverter {

    /** 分数字符串里的硬分后缀，例如 {@code -8845hard}。 */
    private static final String HARD_SUFFIX = "hard";

    /** 分数字符串里的软分后缀，例如 {@code -299562425soft}。 */
    private static final String SOFT_SUFFIX = "soft";

    private ApsConstraintMatchConverter() {
    }

    /**
     * 转换快照。
     *
     * <p>约束顺序按 {@link ApsConstraintNames#ALL} 重排，与策略配置页的权重编辑器对齐；
     * 快照里出现的未登记约束（改过名的历史约束）排在末尾，不丢弃。
     */
    public static SolverSnapshotConstraintMatchRespVO convert(SolverSnapshotRespVO snapshot) {
        SolverSnapshotConstraintMatchRespVO respVO = new SolverSnapshotConstraintMatchRespVO();
        respVO.setSnapshotId(snapshot.getId());
        respVO.setVersionId(snapshot.getVersionId());
        respVO.setVersionNo(snapshot.getVersionNo());
        respVO.setScenarioCode(snapshot.getScenarioCode());
        respVO.setScenarioName(snapshot.getScenarioName());
        respVO.setSnapshotTime(snapshot.getSnapshotTime());

        // parseMap 对空串与非法 JSON 都返回 null，历史快照结构不认识时直接给空清单
        Map<String, Object> payload = JsonUtils.parseMap(snapshot.getSnapshotJson());
        if (payload == null) {
            return respVO;
        }
        respVO.setHardScore(parseScore(payload.get("score"), HARD_SUFFIX));
        respVO.setSoftScore(parseScore(payload.get("score"), SOFT_SUFFIX));
        respVO.setConstraints(convertConstraints(payload.get("constraints")));
        return respVO;
    }

    private static List<SolverSnapshotConstraintMatchRespVO.Constraint> convertConstraints(Object raw) {
        List<SolverSnapshotConstraintMatchRespVO.Constraint> constraints = new ArrayList<>();
        if (!(raw instanceof List<?> items)) {
            return constraints;
        }
        for (Object item : items) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }
            String constraintName = StrUtil.toStringOrNull(map.get("constraintName"));
            if (StrUtil.isBlank(constraintName)) {
                continue;
            }
            constraints.add(buildConstraint(constraintName, map));
        }
        // 按约束目录顺序排，与策略配置页的权重编辑器一行行对得上；未登记的约束沉到末尾
        constraints.sort(Comparator.comparingInt(item -> {
            int index = ApsConstraintNames.ALL.indexOf(item.getConstraintName());
            return index < 0 ? ApsConstraintNames.ALL.size() : index;
        }));
        return constraints;
    }

    private static SolverSnapshotConstraintMatchRespVO.Constraint buildConstraint(String constraintName,
                                                                                  Map<?, ?> map) {
        SolverSnapshotConstraintMatchRespVO.Constraint constraint =
                new SolverSnapshotConstraintMatchRespVO.Constraint();
        constraint.setConstraintName(constraintName);
        ApsConstraintDefinition definition = ApsConstraintCatalog.definition(constraintName);
        if (definition == null) {
            // 历史快照里改过名的约束：中文名回落成原始键，比显示空白更有追溯价值
            constraint.setDisplayName(StrUtil.blankToDefault(
                    StrUtil.toStringOrNull(map.get("displayName")), constraintName));
        } else {
            constraint.setDisplayName(definition.displayName());
            constraint.setCategory(definition.category().name());
            constraint.setCategoryName(definition.category().getName());
            constraint.setDescription(definition.description());
        }
        // 权重取绝对值：Timefold 报的是罚分权重（负数），策略配置页上填的是正数
        constraint.setHardWeight(abs(parseScore(map.get("weight"), HARD_SUFFIX)));
        constraint.setSoftWeight(abs(parseScore(map.get("weight"), SOFT_SUFFIX)));
        constraint.setHardScore(parseScore(map.get("score"), HARD_SUFFIX));
        constraint.setSoftScore(parseScore(map.get("score"), SOFT_SUFFIX));
        constraint.setMatchCount(map.get("matchCount") instanceof Number number ? number.intValue() : null);
        return constraint;
    }

    /**
     * 从 {@code HardSoftLongScore} 的字符串形式里取出某一级分数。
     *
     * <p>不用 {@code HardSoftLongScore.parseScore}：那个方法遇到格式不符会抛异常，而这里读的是
     * 历史留痕数据，格式可能来自任何一个历史版本。读不出来返回 {@code null}，前端显示"-"。
     */
    private static Long parseScore(Object raw, String suffix) {
        if (!(raw instanceof String text) || StrUtil.isBlank(text)) {
            return null;
        }
        for (String part : StrUtil.split(text, '/')) {
            String trimmed = StrUtil.trim(part);
            if (!StrUtil.endWith(trimmed, suffix)) {
                continue;
            }
            String number = StrUtil.removeSuffix(trimmed, suffix);
            return NumberUtil.isLong(number) ? Long.parseLong(number) : null;
        }
        return null;
    }

    private static Long abs(Long value) {
        return value == null ? null : Math.abs(value);
    }

}

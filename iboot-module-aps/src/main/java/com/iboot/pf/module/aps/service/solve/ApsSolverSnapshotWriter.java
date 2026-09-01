package com.iboot.pf.module.aps.service.solve;

import ai.timefold.solver.core.api.score.analysis.ConstraintAnalysis;
import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import cn.hutool.crypto.digest.DigestUtil;
import com.iboot.pf.framework.common.util.json.JsonUtils;
import com.iboot.pf.module.aps.dal.dataobject.solversnapshot.ApsSolverSnapshotDO;
import com.iboot.pf.module.aps.dal.mysql.solversnapshot.ApsSolverSnapshotMapper;
import com.iboot.pf.module.aps.enums.ApsSnapshotTypeEnum;
import com.iboot.pf.module.aps.solver.constraint.ApsConstraintNames;
import com.iboot.pf.module.aps.solver.model.ApsSolution;
import com.iboot.pf.module.aps.solver.model.ApsSolverResource;
import com.iboot.pf.module.aps.solver.model.ApsSolverTask;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 写求解证据链。
 *
 * <p>四类快照各有分工：{@code INPUT} 回答“这次求解喂进去的是什么”，{@code SOLUTION} 回答
 * “排出来的分派是什么”，{@code CONSTRAINT_MATCH} 回答“为什么是这个分数、哪条约束在拖后腿”，
 * {@code ERROR} 回答“为什么没排出来”。缺了约束命中这一类，计划员只能看到一个分数，
 * 没法判断该调策略还是该补产能。
 *
 * <p>{@code checksum} 用内容的 SHA-256：同一版本重复落同样的快照会命中唯一索引
 * （uk_aps_solver_snapshot_deleted），这里先查一次跳过，避免重复写入撞唯一键报错。
 */
@Slf4j
@Service
public class ApsSolverSnapshotWriter {

    @Resource
    private ApsSolverSnapshotMapper solverSnapshotMapper;

    /**
     * 输入指纹快照。
     *
     * <p>不存整份输入：几千个任务的完整 JSON 会把 {@code snapshot_json} 撑爆，而且投影表本身就是
     * 输入的持久化形式。这里存的是“规模 + 策略 + 各投影快照时间”，足够判断两次求解的输入是否同一份。
     */
    public void writeInput(ApsSolution solution, Map<String, Object> projectionSnapshotTimes) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("scenarioId", solution.getScenarioId());
        payload.put("planTime", solution.getPlanTime());
        payload.put("resourceCount", solution.getResources().size());
        payload.put("taskCount", solution.getTasks().size());
        payload.put("pinnedTaskCount", solution.getTasks().stream().filter(ApsSolverTask::isPinned).count());
        payload.put("strategyConfig", solution.getStrategyConfig());
        payload.put("projectionSnapshotTimes", projectionSnapshotTimes);
        write(solution.getVersionId(), ApsSnapshotTypeEnum.INPUT, payload);
    }

    /** 结果快照：每道工序落在哪台资源、第几位、什么时间。 */
    public void writeSolution(ApsSolution solution) {
        List<Map<String, Object>> assignments = new ArrayList<>();
        for (ApsSolverResource resource : solution.getResources()) {
            int sequence = 0;
            for (ApsSolverTask task : resource.getTaskList()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("resCode", resource.getCode());
                item.put("sequenceNo", ++sequence);
                item.put("opTaskId", task.getId());
                item.put("opCode", task.getOpCode());
                item.put("startTime", task.getStartTime());
                item.put("endTime", task.getEndTime());
                item.put("durationMinutes", task.getDurationMinutes());
                item.put("changeoverMinutes", task.getChangeoverMinutes());
                assignments.add(item);
            }
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("score", solution.getScore() == null ? null : solution.getScore().toString());
        payload.put("assignments", assignments);
        write(solution.getVersionId(), ApsSnapshotTypeEnum.SOLUTION, payload);
    }

    /**
     * 约束命中快照。
     *
     * <p>{@code ScoreAnalysis} 在 Timefold 1.x Community 版是免费能力；2.x 把它划进了商业版
     * （{@code SCORE_ANALYSIS}），这也是版本钉在 1.34.0 的原因之一。
     */
    public void writeConstraintMatch(Long versionId, ScoreAnalysis<HardSoftLongScore> analysis) {
        if (analysis == null) {
            return;
        }
        List<Map<String, Object>> constraints = new ArrayList<>();
        for (ConstraintAnalysis<HardSoftLongScore> constraintAnalysis : analysis.constraintAnalyses()) {
            String constraintName = constraintAnalysis.constraintName();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("constraintName", constraintName);
            item.put("displayName", ApsConstraintNames.displayName(constraintName));
            item.put("weight", constraintAnalysis.weight() == null ? null : constraintAnalysis.weight().toString());
            item.put("score", constraintAnalysis.score() == null ? null : constraintAnalysis.score().toString());
            item.put("matchCount", constraintAnalysis.matchCount());
            constraints.add(item);
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("score", analysis.score() == null ? null : analysis.score().toString());
        payload.put("constraints", constraints);
        write(versionId, ApsSnapshotTypeEnum.CONSTRAINT_MATCH, payload);
    }

    /** 异常快照：记下失败原因，让“为什么没排出来”留痕。 */
    public void writeError(Long versionId, String message, String detail) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", message);
        payload.put("detail", detail);
        payload.put("occurTime", LocalDateTime.now());
        write(versionId, ApsSnapshotTypeEnum.ERROR, payload);
    }

    private void write(Long versionId, ApsSnapshotTypeEnum type, Object payload) {
        String json = JsonUtils.toJsonString(payload);
        String checksum = DigestUtil.sha256Hex(json);
        // 幂等：同一版本同类型同内容只落一条，重复写会撞唯一索引
        if (solverSnapshotMapper.selectByVersionIdAndTypeAndChecksum(versionId, type.name(), checksum) != null) {
            return;
        }
        solverSnapshotMapper.insert(ApsSolverSnapshotDO.builder()
                .versionId(versionId)
                .snapshotType(type.name())
                .snapshotJson(json)
                .checksum(checksum)
                .snapshotTime(LocalDateTime.now())
                .build());
    }

}

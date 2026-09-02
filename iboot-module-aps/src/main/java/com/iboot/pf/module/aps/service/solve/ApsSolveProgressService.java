package com.iboot.pf.module.aps.service.solve;

import cn.hutool.core.util.StrUtil;
import com.iboot.pf.framework.common.enums.UserTypeEnum;
import com.iboot.pf.module.aps.enums.ApsSolveStatusEnum;
import com.iboot.pf.module.aps.service.solve.dto.ApsSolveProgress;
import com.iboot.pf.module.aps.service.solve.dto.ApsSolveProgressLog;
import com.iboot.pf.module.infra.api.websocket.WebSocketSenderApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 求解进度的写入与推送。
 *
 * <p>双通路：写 Redis 留一份最新进度供 REST 补偿查询，同时按发起人定向 WebSocket 推送。
 * 只推不存的话，刷新页面或断线重连就什么都看不到了；只存不推则要靠前端轮询，
 * 长求解下轮询频率和实时性没法兼顾。
 *
 * <p>推送失败不影响求解：进度是观测手段，不是业务数据。这里捕获异常只记日志，
 * 绝不让一次 WebSocket 抖动把整轮求解带崩。
 */
@Slf4j
@Service
public class ApsSolveProgressService {

    /** 进度缓存键前缀。 */
    private static final String PROGRESS_KEY_PREFIX = "aps:solve:progress:";

    /** 进度保留 24 小时，够覆盖一个班次的复盘需求。 */
    private static final Duration PROGRESS_TTL = Duration.ofHours(24);

    /** WebSocket 消息类型，前端按它分发。 */
    public static final String MESSAGE_TYPE = "aps-solve-progress";

    /** 日志条数上限：只保留最近若干条，避免大算例把 Redis 里的进度对象撑爆。 */
    private static final int MAX_LOG_SIZE = 200;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired(required = false) // iboot.websocket.enable 可以关掉 WebSocket，这里不能强制注入
    private WebSocketSenderApi webSocketSenderApi;

    /** 初始化进度，覆盖同版本的历史记录。 */
    public ApsSolveProgress init(Long versionId, Long scenarioId, Long operatorId, int totalStep) {
        ApsSolveProgress progress = new ApsSolveProgress();
        progress.setVersionId(versionId);
        progress.setScenarioId(scenarioId);
        progress.setStatus(ApsSolveStatusEnum.PREPARING.name());
        progress.setRatio(0);
        progress.setStep(0);
        progress.setTotalStep(totalStep);
        progress.setStartTime(LocalDateTime.now());
        progress.setOccurTime(LocalDateTime.now());
        progress.setMessage(ApsSolveStatusEnum.PREPARING.getName());
        save(progress, operatorId);
        return progress;
    }

    /** 推进一步进度。 */
    public void report(ApsSolveProgress progress, Long operatorId, ApsSolveStatusEnum status,
                       int step, String message) {
        progress.setStatus(status.name());
        progress.setStep(step);
        progress.setRatio(calculateRatio(step, progress.getTotalStep()));
        progress.setMessage(message);
        progress.setOccurTime(LocalDateTime.now());
        appendLog(progress, status, message);
        save(progress, operatorId);
    }

    /** 求解过程中拿到更优解时刷新得分，不推进步骤。 */
    public void reportScore(ApsSolveProgress progress, Long operatorId, Long hardScore, Long softScore) {
        progress.setHardScore(hardScore);
        progress.setSoftScore(softScore);
        progress.setOccurTime(LocalDateTime.now());
        save(progress, operatorId);
    }

    /** 标记终态。 */
    public void finish(ApsSolveProgress progress, Long operatorId, ApsSolveStatusEnum status, String message) {
        progress.setStatus(status.name());
        // 成功时补满进度条；失败与取消保留当时的百分比，让人看清卡在哪一步
        if (status == ApsSolveStatusEnum.SUCCESS) {
            progress.setStep(progress.getTotalStep());
            progress.setRatio(100);
        }
        progress.setMessage(message);
        progress.setOccurTime(LocalDateTime.now());
        appendLog(progress, status, message);
        save(progress, operatorId);
    }

    /** 读取进度；不存在返回 {@code null}。 */
    public ApsSolveProgress get(Long versionId) {
        Object cached = redisTemplate.opsForValue().get(buildKey(versionId));
        return cached instanceof ApsSolveProgress progress ? progress : null;
    }

    private void save(ApsSolveProgress progress, Long operatorId) {
        redisTemplate.opsForValue().set(buildKey(progress.getVersionId()), progress, PROGRESS_TTL);
        push(progress, operatorId);
    }

    private void push(ApsSolveProgress progress, Long operatorId) {
        if (webSocketSenderApi == null || operatorId == null) {
            return;
        }
        try {
            webSocketSenderApi.sendObject(UserTypeEnum.ADMIN.getValue(), operatorId, MESSAGE_TYPE, progress);
        } catch (Exception ex) {
            // 进度是观测手段，推送失败不能影响求解本身
            log.warn("[push][推送排程进度失败 versionId({}) operatorId({})]", progress.getVersionId(), operatorId, ex);
        }
    }

    private void appendLog(ApsSolveProgress progress, ApsSolveStatusEnum status, String message) {
        if (StrUtil.isBlank(message)) {
            return;
        }
        List<ApsSolveProgressLog> logs = progress.getLogs() == null ? new ArrayList<>() : progress.getLogs();
        logs.add(new ApsSolveProgressLog(status.name(), message, LocalDateTime.now()));
        if (logs.size() > MAX_LOG_SIZE) {
            logs = new ArrayList<>(logs.subList(logs.size() - MAX_LOG_SIZE, logs.size()));
        }
        progress.setLogs(logs);
    }

    private int calculateRatio(int step, Integer totalStep) {
        if (totalStep == null || totalStep <= 0) {
            return 0;
        }
        return Math.min(step * 100 / totalStep, 100);
    }

    private String buildKey(Long versionId) {
        return PROGRESS_KEY_PREFIX + versionId;
    }

}

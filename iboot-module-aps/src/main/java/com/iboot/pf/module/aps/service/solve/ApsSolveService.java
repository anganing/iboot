package com.iboot.pf.module.aps.service.solve;

import com.iboot.pf.module.aps.api.version.dto.GenerateVersionReqDTO;
import com.iboot.pf.module.aps.service.solve.dto.ApsSolveProgress;
import jakarta.validation.Valid;

/**
 * APS 排程求解服务。
 *
 * <p>求解是长耗时动作，接口只负责建版本、占场景锁、把任务丢进求解线程池，然后立刻返回版本编号；
 * 进度与结果通过 WebSocket 推送与进度查询接口获取。同步等结果会把 Web 线程挂住几分钟。
 *
 * @author iboot
 */
public interface ApsSolveService {

    /**
     * 生成排程版本并提交求解
     *
     * @param reqDTO 命令，含场景、策略与操作人
     * @return 排程版本编号
     */
    Long generateAndSolve(@Valid GenerateVersionReqDTO reqDTO);

    /**
     * 提前终止某版本的求解，已取得的最优解不落库
     *
     * @param versionId 排程版本编号
     */
    void cancelSolve(Long versionId);

    /**
     * 查询求解进度，供页面刷新或断线重连补偿
     *
     * @param versionId 排程版本编号
     * @return 求解进度；无记录返回 {@code null}
     */
    ApsSolveProgress getProgress(Long versionId);

}

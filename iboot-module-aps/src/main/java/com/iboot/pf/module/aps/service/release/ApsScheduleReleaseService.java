package com.iboot.pf.module.aps.service.release;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.iboot.pf.module.aps.dal.dataobject.task.ApsTaskDO;
import com.iboot.pf.module.aps.dal.dataobject.version.ApsVersionDO;
import com.iboot.pf.module.aps.dal.mysql.task.ApsTaskMapper;
import com.iboot.pf.module.aps.dal.mysql.version.ApsVersionMapper;
import com.iboot.pf.module.aps.enums.ApsConstants;
import com.iboot.pf.module.aps.enums.ApsSourceModuleEnum;
import com.iboot.pf.module.aps.enums.ApsTaskStatusEnum;
import com.iboot.pf.module.mes.api.optask.OpTaskApi;
import com.iboot.pf.module.mes.api.optask.dto.ApplyScheduleReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 排程结果下达：把已发布版本的计划回写到 MES 工序任务。
 *
 * <p>这是排程闭环的最后一段。之前排程结果只躺在 {@code aps_task} 里，MES 派工拿不到，
 * 车间实际按旧的工单计划干活，排程等于白算。
 *
 * <p>跨模块只走 {@link OpTaskApi} 门面，不碰 {@code mes_op_task}：跨库 JOIN 或直接改对方的表
 * 会把 aps 和 mes 焊死在同一个库上，将来拆微服务无处下手。
 *
 * <p>与发布动作同事务：一个"发布"在业务上就是"这份计划从此生效"，回写失败还留下 PUBLISHED 版本，
 * 会让人以为车间已经收到新计划。宁可整个发布回滚，让人重试。
 */
@Service
@Slf4j
public class ApsScheduleReleaseService {

    @Resource
    private ApsTaskMapper taskMapper;
    @Resource
    private ApsVersionMapper versionMapper;
    @Resource
    private OpTaskApi opTaskApi;

    /**
     * 下达某个排程版本。
     *
     * <p>不自己开事务：由调用方 {@code publishVersion} 的事务覆盖，两者要么都成要么都不成。
     *
     * @param versionId 排程版本编号
     * @return 实际在 MES 生效的工序条数
     */
    public int release(Long versionId) {
        // 版本号随命令下传：MES 侧状态推进流水要用它做幂等键与来源单号，没有它下达无从对账
        ApsVersionDO version = versionMapper.selectById(versionId);
        Assert.notNull(version, "排程版本({})不存在", versionId);
        List<ApsTaskDO> tasks = taskMapper.selectListByVersionId(versionId);
        if (CollUtil.isEmpty(tasks)) {
            return 0;
        }
        List<ApplyScheduleReqDTO.Item> items = new ArrayList<>();
        List<ApsTaskDO> releasedTasks = new ArrayList<>();
        for (ApsTaskDO task : tasks) {
            // opTaskId 为占位 0 的任务没有 MES 对应工序，回写过去也落不到任何一行
            if (task.getOpTaskId() == null || ApsConstants.NONE_ID.equals(task.getOpTaskId())) {
                continue;
            }
            items.add(buildItem(task));
            // 已锁定、已开工、已完工的任务保持原状态，只把新排定的推进到已下达
            if (ApsTaskStatusEnum.PLANNED.name().equals(task.getStatus())) {
                releasedTasks.add(ApsTaskDO.builder()
                        .id(task.getId())
                        .status(ApsTaskStatusEnum.RELEASED.name())
                        .build());
            }
        }
        if (items.isEmpty()) {
            return 0;
        }
        int affected = opTaskApi.applySchedule(buildCommand(version, items));
        if (CollUtil.isNotEmpty(releasedTasks)) {
            taskMapper.updateBatch(releasedTasks);
        }
        // 差额是 MES 侧已开工或已终态的工序，属于正常情况，记日志便于对账
        if (affected < items.size()) {
            log.info("[release][版本({}) 下达 {} 道工序，MES 实际生效 {} 道，差额为已开工或已终态工序]",
                    versionId, items.size(), affected);
        }
        return affected;
    }

    private ApplyScheduleReqDTO.Item buildItem(ApsTaskDO task) {
        ApplyScheduleReqDTO.Item item = new ApplyScheduleReqDTO.Item();
        item.setOpTaskId(task.getOpTaskId());
        item.setResId(task.getResId());
        item.setPlannedStartTime(task.getStartTime());
        item.setPlannedEndTime(task.getEndTime());
        return item;
    }

    /**
     * 构建回写命令。
     *
     * <p>幂等键用固定的"版本 + 下达"：对同一个版本重复发布是同一次下达，键带时间戳反而会让重试
     * 变成第二次下达。{@code versionNo} 传场景内版本号（不是版本主键），MES 侧下达流水的
     * 幂等键与来源单号都从它派生。
     */
    private ApplyScheduleReqDTO buildCommand(ApsVersionDO version, List<ApplyScheduleReqDTO.Item> items) {
        ApplyScheduleReqDTO reqDTO = new ApplyScheduleReqDTO();
        reqDTO.setVersionNo(String.valueOf(version.getVersionNo()));
        reqDTO.setItems(items);
        reqDTO.setIdempotencyKey(ApsSourceModuleEnum.APS.name() + ":RELEASE_SCHEDULE:" + version.getId());
        reqDTO.setSourceModule(ApsSourceModuleEnum.APS.name());
        reqDTO.setSourceBizNo(String.valueOf(version.getId()));
        return reqDTO;
    }

}

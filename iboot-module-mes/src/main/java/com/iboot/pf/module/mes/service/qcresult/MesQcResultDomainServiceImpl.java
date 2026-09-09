package com.iboot.pf.module.mes.service.qcresult;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iboot.pf.module.mes.dal.dataobject.qcitem.QcItemDO;
import com.iboot.pf.module.mes.dal.dataobject.qcresult.QcResultDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.qcitem.QcItemMapper;
import com.iboot.pf.module.mes.dal.mysql.qcresult.QcResultMapper;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
import com.iboot.pf.module.mes.enums.MesQcConclusionEnum;
import com.iboot.pf.module.mes.enums.MesQcItemConclusionEnum;
import com.iboot.pf.module.mes.enums.MesQcTaskStateMachine;
import com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum;
import com.iboot.pf.module.mes.service.qcresult.bo.MesSaveQcResultBO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertMap;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertSet;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_ITEM_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RESULT_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RESULT_SAMPLE_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_CONCURRENT_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_NOT_INSPECTING;

/**
 * 检验结果领域动作实现。
 *
 * <p>{@link #batchSaveResult} 是检验录入作业页的保存动作：一整批格子（检验项 × 样本）
 * 一次提交，逐格判定落库，最后一个必检格子落下的那一刻任务自动进入"录完待判"。
 * 判定与推进在同一个事务里，推进失败整批回滚，不会出现"结果存了任务还停在检验中"的半截状态。
 *
 * <p>录入语义是"重复保存覆盖同格"而不是命令幂等：检验员录错改数、补录漏项是常规操作，
 * 幂等拦截会让第二次保存直接失败，录数就没法纠错了。追溯靠每行落的
 * {@code inspector_id + inspect_time}：最后一次保存是谁、什么时候，覆盖掉的是误录不是事实。
 *
 * @author iboot
 */
@Slf4j
@Service
public class MesQcResultDomainServiceImpl implements MesQcResultDomainService {

    @Resource
    private QcResultMapper qcResultMapper;
    @Resource
    private QcItemMapper qcItemMapper;
    @Resource
    private QcTaskMapper qcTaskMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSaveResult(MesSaveQcResultBO bo) {
        Assert.notNull(bo.getTaskId(), "质检任务编号不能为空");
        Assert.notNull(bo.getInspectorId(), "检验员编号不能为空");
        Assert.notEmpty(bo.getResults(), "录入的实测值行不能为空");
        // 同一批里同一格出现两次按脏数据拒掉：后写的覆盖先写的没有争议，但一批内谁先谁后无从考证
        assertNoDuplicateGrid(bo.getResults());

        QcTaskDO task = qcTaskMapper.selectById(bo.getTaskId());
        if (task == null) {
            throw exception(QC_TASK_NOT_EXISTS);
        }
        // 结果只在 INSPECTING 可改：RESULT_POSTING 意味着录入已经收口、结论待定，
        // 这时再改数，人看到的判定依据和落库的实测值就对不上了
        if (!MesQcTaskStatusEnum.INSPECTING.name().equals(task.getStatus())) {
            throw exception(QC_TASK_NOT_INSPECTING, task.getTaskNo());
        }

        // 预生成空行是格子的全集：格子不存在说明检验项不属于本任务或样本序号超出抽样数
        List<QcResultDO> rows = qcResultMapper.selectListByTaskId(task.getId());
        Map<String, QcResultDO> rowMap = rows.stream()
                .collect(Collectors.toMap(row -> gridKey(row.getItemId(), row.getSampleNo()), row -> row));
        Map<Long, QcItemDO> itemMap = convertMap(
                qcItemMapper.selectByIds(convertSet(bo.getResults(), MesSaveQcResultBO.Item::getItemId)),
                QcItemDO::getId);

        for (MesSaveQcResultBO.Item itemRow : bo.getResults()) {
            assertRowComplete(itemRow);
            QcResultDO row = rowMap.get(gridKey(itemRow.getItemId(), itemRow.getSampleNo()));
            if (row == null) {
                throw exception(QC_RESULT_NOT_EXISTS);
            }
            QcItemDO item = itemMap.get(itemRow.getItemId());
            if (item == null) {
                throw exception(QC_ITEM_NOT_EXISTS, itemRow.getItemId());
            }
            saveRow(row, item, itemRow.getActualValue(), bo.getInspectorId());
        }

        advanceIfRequiredItemsDone(task);
        return bo.getResults().size();
    }

    @Override
    public String suggestConclusion(Long taskId) {
        Assert.notNull(taskId, "质检任务编号不能为空");
        List<QcResultDO> rows = qcResultMapper.selectListByTaskId(taskId);
        // 只统计检验员实际录过的格子（actualValue 非空）：未录格子的 passFlag=false 是
        // 预生成占位值（NOT NULL 列的初始占位），不是检验结论，算进去会把没检过的项误判成不合格
        boolean hasFail = rows.stream()
                .filter(row -> row.getPassFlag() != null && row.getActualValue() != null)
                .anyMatch(row -> !row.getPassFlag());
        return hasFail ? MesQcConclusionEnum.REJECTED.name() : MesQcConclusionEnum.QUALIFIED.name();
    }

    /** 同一批内同一格（检验项 × 样本）出现两次，录入批次本身就是脏的。 */
    private void assertNoDuplicateGrid(List<MesSaveQcResultBO.Item> items) {
        Set<String> keys = convertSet(items, item -> gridKey(item.getItemId(), item.getSampleNo()));
        if (keys.size() != items.size()) {
            throw exception(QC_RESULT_SAMPLE_DUPLICATE);
        }
    }

    /** 单行参数完整性：定位列与实测值缺一不可，缺了定位不到格子、缺了值没有可判的内容。 */
    private void assertRowComplete(MesSaveQcResultBO.Item item) {
        Assert.notNull(item.getItemId(), "检验项目编号不能为空");
        Assert.notNull(item.getSampleNo(), "样本序号不能为空");
        Assert.notBlank(item.getActualValue(), "实测值不能为空");
    }

    /** 判定并覆盖写一个格子：实测值、判定结果、落款一次落齐。 */
    private void saveRow(QcResultDO row, QcItemDO item, String actualValue, Long inspectorId) {
        String conclusion = MesQcItemJudge.judge(item, actualValue);
        boolean pass = MesQcItemConclusionEnum.PASS.name().equals(conclusion);
        qcResultMapper.updateById(new QcResultDO()
                .setId(row.getId())
                .setActualValue(actualValue)
                .setPassFlag(pass)
                .setConclusion(conclusion)
                .setInspectorId(inspectorId)
                .setInspectTime(LocalDateTime.now()));
    }

    /**
     * 全部必检项都录入时推进任务 INSPECTING -&gt; RESULT_POSTING。
     *
     * <p>"必检项已录入"按格子全集算：必检检验项名下每个预生成样本的实测值都非空才算录完，
     * 只录第一个样本剩下悬空的话，抽了几件就有几件没有判定去向。非必检项的空格子不拦推进。
     *
     * <p>推进走带 {@code WHERE version = 读到的版本} 的条件更新（参照
     * {@code MesQcTaskDomainServiceImpl.advanceQcTask} 的写法在本域内实现）：
     * 检验员重复提交与录入动作可能同时到达，撞车的一方按 0 行更新抛
     * {@code QC_TASK_CONCURRENT_CONFLICT}，整批保存回滚，刷新后重交。
     */
    private void advanceIfRequiredItemsDone(QcTaskDO task) {
        List<QcResultDO> rows = qcResultMapper.selectListByTaskId(task.getId());
        Set<Long> requiredItemIds = convertSet(
                qcItemMapper.selectByIds(convertSet(rows, QcResultDO::getItemId)).stream()
                        .filter(item -> Boolean.TRUE.equals(item.getRequiredFlag()))
                        .toList(),
                QcItemDO::getId);
        boolean allRequiredEntered = rows.stream()
                .filter(row -> requiredItemIds.contains(row.getItemId()))
                .allMatch(row -> row.getActualValue() != null);
        if (!allRequiredEntered) {
            return;
        }

        String toStatus = MesQcTaskStateMachine.next(task.getStatus(), MesQcTaskStateMachine.ACTION_POST_RESULT);
        int currentVersion = task.getVersion() == null ? 0 : task.getVersion();
        int updated = qcTaskMapper.update(
                QcTaskDO.builder().status(toStatus).version(currentVersion + 1).build(),
                new LambdaUpdateWrapper<QcTaskDO>()
                        .eq(QcTaskDO::getId, task.getId())
                        .eq(QcTaskDO::getVersion, currentVersion));
        if (updated == 0) {
            throw exception(QC_TASK_CONCURRENT_CONFLICT);
        }
        log.info("[batchSaveResult] 全部必检项已录入，任务推进。taskNo={}, {} -> {}",
                task.getTaskNo(), task.getStatus(), toStatus);
    }

    /** 格子定位键：检验项 × 样本序号，与唯一索引 uk_mes_qc_result_sample_deleted 同口径。 */
    private static String gridKey(MesSaveQcResultBO.Item item) {
        return gridKey(item.getItemId(), item.getSampleNo());
    }

    private static String gridKey(Long itemId, Integer sampleNo) {
        return itemId + ":" + sampleNo;
    }

}

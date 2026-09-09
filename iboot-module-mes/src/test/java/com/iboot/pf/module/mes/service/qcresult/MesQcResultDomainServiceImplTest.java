package com.iboot.pf.module.mes.service.qcresult;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.dal.dataobject.qcitem.QcItemDO;
import com.iboot.pf.module.mes.dal.dataobject.qcresult.QcResultDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.qcitem.QcItemMapper;
import com.iboot.pf.module.mes.dal.mysql.qcresult.QcResultMapper;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesQcConclusionEnum;
import com.iboot.pf.module.mes.enums.MesQcItemConclusionEnum;
import com.iboot.pf.module.mes.enums.MesQcItemTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import com.iboot.pf.module.mes.service.qcresult.bo.MesSaveQcResultBO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RESULT_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RESULT_SAMPLE_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_CONCURRENT_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_NOT_INSPECTING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 检验结果录入与判定的领域测试。
 *
 * <p>判定全分支（三种 item_type 各通过/超差、NUMERIC 三种限位、解析失败）在
 * {@link MesQcItemJudgeTest} 用纯单测覆盖；本类验证落库链路：逐格判定写
 * actual_value / pass_flag / conclusion / inspector_id / inspect_time、全部必检项录完
 * 自动推进、RESULT_POSTING 后改结果抛错、覆盖保存语义。
 *
 * <p>空行桩按 T2-02 的预生成形态插：每个检验项 × 每个样本一行，实测值等列为空。
 */
@Import(MesQcResultDomainServiceImpl.class)
class MesQcResultDomainServiceImplTest extends BaseDbUnitTest {

    private static final Long INSPECTOR_ID = 666L;
    private static final String BIZ_NO = "ARR20260910001";

    /** 任务号计数器，同一用例里的任务号互不相同（生产上由编码规则分配）。 */
    private final AtomicInteger taskSeq = new AtomicInteger();
    /** 方案编号计数器，检验项挂在各自的方案下。 */
    private final AtomicInteger planSeq = new AtomicInteger();

    @Resource
    private MesQcResultDomainService qcResultDomainService;
    @Resource
    private QcResultMapper qcResultMapper;
    @Resource
    private QcItemMapper qcItemMapper;
    @Resource
    private QcTaskMapper qcTaskMapper;

    // ========== 三种 item_type 的判定落库（各 1 通过 + 1 超差） ==========

    @Test
    void testBatchSaveResult_numeric_passAndFail() {
        // 数值项（下限 0.05 上限 0.10）：样本 1 录 0.08 合格，样本 2 录 0.11 超差
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.INSPECTING.name(), 2, 1, false);
        Long itemId = requireOnlyItemId(task.getId());

        qcResultDomainService.batchSaveResult(saveBO(task.getId(),
                row(itemId, 1, "0.08"), row(itemId, 2, "0.11")));

        QcResultDO sample1 = qcResultMapper.selectBySample(task.getId(), itemId, 1);
        assertEquals("0.08", sample1.getActualValue());
        assertTrue(sample1.getPassFlag());
        assertEquals(MesQcItemConclusionEnum.PASS.name(), sample1.getConclusion());
        QcResultDO sample2 = qcResultMapper.selectBySample(task.getId(), itemId, 2);
        assertEquals("0.11", sample2.getActualValue());
        assertFalse(sample2.getPassFlag());
        assertEquals(MesQcItemConclusionEnum.FAIL.name(), sample2.getConclusion());
        // 落款：检验员与检验时间一次落齐
        assertEquals(INSPECTOR_ID, sample1.getInspectorId());
        assertNotNull(sample1.getInspectTime());
    }

    @Test
    void testBatchSaveResult_text_passAndFail() {
        // 文本项：标准值"无击穿"，样本 1 录对了（带空格也判过），样本 2 录错
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.INSPECTING.name(), 2, 1, false);
        Long itemId = requireOnlyItemId(task.getId());
        updateItem(itemId, MesQcItemTypeEnum.TEXT.name(), null, null, "无击穿");

        qcResultDomainService.batchSaveResult(saveBO(task.getId(),
                row(itemId, 1, " 无击穿 "), row(itemId, 2, "击穿")));

        QcResultDO sample1 = qcResultMapper.selectBySample(task.getId(), itemId, 1);
        assertEquals(MesQcItemConclusionEnum.PASS.name(), sample1.getConclusion());
        QcResultDO sample2 = qcResultMapper.selectBySample(task.getId(), itemId, 2);
        assertEquals(MesQcItemConclusionEnum.FAIL.name(), sample2.getConclusion());
        assertFalse(sample2.getPassFlag());
    }

    @Test
    void testBatchSaveResult_boolean_passAndFail() {
        // 布尔项：标准值 true，样本 1 录 1（等价写法）合格，样本 2 录 false 超差
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.INSPECTING.name(), 2, 1, false);
        Long itemId = requireOnlyItemId(task.getId());
        updateItem(itemId, MesQcItemTypeEnum.BOOLEAN.name(), null, null, "true");

        qcResultDomainService.batchSaveResult(saveBO(task.getId(),
                row(itemId, 1, "1"), row(itemId, 2, "false")));

        QcResultDO sample1 = qcResultMapper.selectBySample(task.getId(), itemId, 1);
        assertEquals(MesQcItemConclusionEnum.PASS.name(), sample1.getConclusion());
        QcResultDO sample2 = qcResultMapper.selectBySample(task.getId(), itemId, 2);
        assertEquals(MesQcItemConclusionEnum.FAIL.name(), sample2.getConclusion());
    }

    @Test
    void testBatchSaveResult_numericLowerOnly_fail() {
        // NUMERIC 只配下限（0.05）：经保存链路验证单侧限位，录 0.04 超差
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.INSPECTING.name(), 1, 1, false);
        Long itemId = requireOnlyItemId(task.getId());
        updateItem(itemId, MesQcItemTypeEnum.NUMERIC.name(), new BigDecimal("0.05"), null, null);

        qcResultDomainService.batchSaveResult(saveBO(task.getId(), row(itemId, 1, "0.04")));

        QcResultDO row = qcResultMapper.selectBySample(task.getId(), itemId, 1);
        assertEquals(MesQcItemConclusionEnum.FAIL.name(), row.getConclusion());
        // 该用例唯一的必检项录完后任务自动推进，限位判定与推进在同一次提交里完成
        assertEquals(MesQcTaskStatusEnum.RESULT_POSTING.name(),
                qcTaskMapper.selectById(task.getId()).getStatus());
    }

    @Test
    void testBatchSaveResult_numericUpperOnly_pass() {
        // NUMERIC 只配上限（0.10）：下限侧不限，负值也合格
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.INSPECTING.name(), 1, 1, false);
        Long itemId = requireOnlyItemId(task.getId());
        updateItem(itemId, MesQcItemTypeEnum.NUMERIC.name(), null, new BigDecimal("0.10"), null);

        qcResultDomainService.batchSaveResult(saveBO(task.getId(), row(itemId, 1, "-3")));

        assertEquals(MesQcItemConclusionEnum.PASS.name(),
                qcResultMapper.selectBySample(task.getId(), itemId, 1).getConclusion());
    }

    @Test
    void testBatchSaveResult_parseFailAsFail() {
        // 数值格录了非数字：解析失败视为 FAIL 不抛异常，FAIL 不放行下游，出错方向安全
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.INSPECTING.name(), 1, 1, false);
        Long itemId = requireOnlyItemId(task.getId());

        qcResultDomainService.batchSaveResult(saveBO(task.getId(), row(itemId, 1, "abc")));

        QcResultDO row = qcResultMapper.selectBySample(task.getId(), itemId, 1);
        assertEquals("abc", row.getActualValue());
        assertEquals(MesQcItemConclusionEnum.FAIL.name(), row.getConclusion());
        assertFalse(row.getPassFlag());
    }

    // ========== 任务推进 ==========

    @Test
    void testBatchSaveResult_allRequiredEntered_advanceToResultPosting() {
        // 2 个必检项各 1 样本，录完最后一个格子任务自动推进
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.INSPECTING.name(), 1, 2, false);
        List<Long> itemIds = itemIdsOf(task.getId());
        assertEquals(2, itemIds.size());

        qcResultDomainService.batchSaveResult(saveBO(task.getId(),
                row(itemIds.get(0), 1, "0.08"), row(itemIds.get(1), 1, "0.08")));

        QcTaskDO advanced = qcTaskMapper.selectById(task.getId());
        assertEquals(MesQcTaskStatusEnum.RESULT_POSTING.name(), advanced.getStatus());
        // 乐观锁版本自增，证明走的是带 version 条件的更新
        assertEquals(MesConstants.INIT_VERSION + 1, advanced.getVersion());
    }

    @Test
    void testBatchSaveResult_partialRequired_stayInspecting() {
        // 2 个必检项只录 1 个：任务留在 INSPECTING，版本不动
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.INSPECTING.name(), 1, 2, false);
        List<Long> itemIds = itemIdsOf(task.getId());

        qcResultDomainService.batchSaveResult(saveBO(task.getId(), row(itemIds.get(0), 1, "0.08")));

        QcTaskDO unchanged = qcTaskMapper.selectById(task.getId());
        assertEquals(MesQcTaskStatusEnum.INSPECTING.name(), unchanged.getStatus());
        assertEquals(MesConstants.INIT_VERSION, unchanged.getVersion());
    }

    @Test
    void testBatchSaveResult_optionalItemBlank_stillAdvance() {
        // 必检项录完、非必检项空着：非必检不拦推进
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.INSPECTING.name(), 1, 2, false);
        List<Long> itemIds = itemIdsOf(task.getId());
        Long optionalItem = insertItem(task.getQcPlanId(), "IT-OPT", MesQcItemTypeEnum.NUMERIC.name(),
                null, null, null, false);
        insertResultRows(task.getId(), optionalItem, 1);

        qcResultDomainService.batchSaveResult(saveBO(task.getId(),
                row(itemIds.get(0), 1, "0.08"), row(itemIds.get(1), 1, "0.08")));

        assertEquals(MesQcTaskStatusEnum.RESULT_POSTING.name(),
                qcTaskMapper.selectById(task.getId()).getStatus());
    }

    @Test
    void testBatchSaveResult_advanceConcurrentConflict() {
        // 乐观锁条件更新的另一面：条件永不命中时 0 行更新必须抛冲突而不是静默跳过。
        // 插一条 version 为 NULL 的 INSPECTING 任务，条件 WHERE version = 0 对 NULL 行永不命中，
        // 等价于"读取与写回之间被别人改掉版本"的并发撞车
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.INSPECTING.name(), 1, 2, true);
        List<Long> itemIds = itemIdsOf(task.getId());

        assertServiceException(() -> qcResultDomainService.batchSaveResult(saveBO(task.getId(),
                        row(itemIds.get(0), 1, "0.08"), row(itemIds.get(1), 1, "0.08"))),
                QC_TASK_CONCURRENT_CONFLICT);
        // 冲突整批回滚：格子没写、任务没推进
        assertNull(qcResultMapper.selectBySample(task.getId(), itemIds.get(0), 1).getActualValue());
        assertEquals(MesQcTaskStatusEnum.INSPECTING.name(),
                qcTaskMapper.selectById(task.getId()).getStatus());
    }

    // ========== 状态门禁与覆盖语义 ==========

    @Test
    void testBatchSaveResult_resultPosting_rejected() {
        // RESULT_POSTING 之后结果收口，再改数判定依据就对不上了
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.RESULT_POSTING.name(), 1, 1, false);
        Long itemId = requireOnlyItemId(task.getId());

        assertServiceException(() -> qcResultDomainService.batchSaveResult(saveBO(task.getId(),
                        row(itemId, 1, "0.08"))),
                QC_TASK_NOT_INSPECTING, task.getTaskNo());
        assertNull(qcResultMapper.selectBySample(task.getId(), itemId, 1).getActualValue());
    }

    @Test
    void testBatchSaveResult_assigned_rejected() {
        // 未开检（ASSIGNED）不能录结果，必须先 startInspect
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.ASSIGNED.name(), 1, 1, false);
        Long itemId = requireOnlyItemId(task.getId());

        assertServiceException(() -> qcResultDomainService.batchSaveResult(saveBO(task.getId(),
                        row(itemId, 1, "0.08"))),
                QC_TASK_NOT_INSPECTING, task.getTaskNo());
    }

    @Test
    void testBatchSaveResult_overwriteSameGrid() {
        // 录错改数重交是常规操作：同一格覆盖保存，实测值、判定、落款全部更新。
        // 另一个必检项始终空着，任务停在 INSPECTING，第二次保存不会被状态门禁拦
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.INSPECTING.name(), 1, 2, false);
        Long itemId = itemIdsOf(task.getId()).get(0);

        qcResultDomainService.batchSaveResult(saveBO(task.getId(), row(itemId, 1, "0.11")));
        assertEquals(MesQcItemConclusionEnum.FAIL.name(),
                qcResultMapper.selectBySample(task.getId(), itemId, 1).getConclusion());

        qcResultDomainService.batchSaveResult(saveBO(task.getId(), row(itemId, 1, "0.08")));
        QcResultDO after = qcResultMapper.selectBySample(task.getId(), itemId, 1);
        assertEquals("0.08", after.getActualValue());
        assertEquals(MesQcItemConclusionEnum.PASS.name(), after.getConclusion());
        assertTrue(after.getPassFlag());
        assertEquals(INSPECTOR_ID, after.getInspectorId());
        assertEquals(MesQcTaskStatusEnum.INSPECTING.name(),
                qcTaskMapper.selectById(task.getId()).getStatus());
    }

    // ========== 参数与数据校验 ==========

    @Test
    void testBatchSaveResult_duplicateGridInBatch_rejected() {
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.INSPECTING.name(), 1, 1, false);
        Long itemId = requireOnlyItemId(task.getId());

        // 同一批里同一格出现两次按脏数据拒掉：批内谁先谁后无从考证
        assertServiceException(() -> qcResultDomainService.batchSaveResult(saveBO(task.getId(),
                        row(itemId, 1, "0.08"), row(itemId, 1, "0.09"))),
                QC_RESULT_SAMPLE_DUPLICATE);
    }

    @Test
    void testBatchSaveResult_rowNotExists_rejected() {
        // 样本序号超出预生成范围：格子不存在说明抽样口径对不上
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.INSPECTING.name(), 1, 1, false);
        Long itemId = requireOnlyItemId(task.getId());

        assertServiceException(() -> qcResultDomainService.batchSaveResult(saveBO(task.getId(),
                        row(itemId, 9, "0.08"))),
                QC_RESULT_NOT_EXISTS);
    }

    @Test
    void testBatchSaveResult_taskNotExists_rejected() {
        assertServiceException(() -> qcResultDomainService.batchSaveResult(saveBO(999999L,
                        row(1L, 1, "0.08"))),
                QC_TASK_NOT_EXISTS);
    }

    // ========== 建议结论 ==========

    @Test
    void testSuggestConclusion_allPass_qualified() {
        // INSPECTING 录入合格值，录完自动进 RESULT_POSTING，正好是建议结论的使用时机
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.INSPECTING.name(), 1, 2, false);
        List<Long> itemIds = itemIdsOf(task.getId());

        qcResultDomainService.batchSaveResult(saveBO(task.getId(),
                row(itemIds.get(0), 1, "0.08"), row(itemIds.get(1), 1, "0.08")));

        assertEquals(MesQcConclusionEnum.QUALIFIED.name(),
                qcResultDomainService.suggestConclusion(task.getId()));
    }

    @Test
    void testSuggestConclusion_hasFail_rejected() {
        QcTaskDO task = insertTask(MesQcTaskStatusEnum.INSPECTING.name(), 1, 2, false);
        List<Long> itemIds = itemIdsOf(task.getId());

        qcResultDomainService.batchSaveResult(saveBO(task.getId(),
                row(itemIds.get(0), 1, "0.08"), row(itemIds.get(1), 1, "0.11")));

        // 有 FAIL 建议拒收；最终结论是人的决策，可以让步接收，这里只给建议
        assertEquals(MesQcConclusionEnum.REJECTED.name(),
                qcResultDomainService.suggestConclusion(task.getId()));
    }

    // ========== 桩数据 ==========

    /**
     * 插一条指定状态的任务 + 预生成空行组，按 T2-02 的落库形态。
     *
     * @param status      任务状态
     * @param sampleQty   每个检验项的样本数
     * @param itemCount   必检数值检验项个数（1 或 2），判定用例用单项、推进用例用双项
     * @param nullVersion version 插 NULL（并发冲突用例专用：条件 WHERE version = 0 永不命中）
     */
    private QcTaskDO insertTask(String status, int sampleQty, int itemCount, boolean nullVersion) {
        Long planId = (long) (2000 + planSeq.incrementAndGet());
        QcTaskDO task = QcTaskDO.builder()
                .taskNo("QCT-" + taskSeq.incrementAndGet())
                .ruleId(100L)
                .qcPlanId(planId)
                .qcPlanVersion("V1.0")
                .triggerPoint(MesQcTriggerPointEnum.IQC.name())
                .bizType(MesBizTypeEnum.ARRIVAL.name())
                .bizNo(BIZ_NO)
                .bizLineId(MesConstants.NONE_ID)
                .matId(1L)
                .matCode("MAT-001")
                .matName("定子铁芯")
                .batchId(MesConstants.NONE_ID)
                .qty(new BigDecimal("20"))
                .sampleQty(new BigDecimal(sampleQty))
                .uomId(1L)
                .uomCode("PCS")
                .assignedInspectorId(INSPECTOR_ID)
                .qtyQualified(MesConstants.ZERO_QTY)
                .qtyRejected(MesConstants.ZERO_QTY)
                .status(status)
                .version(nullVersion ? null : MesConstants.INIT_VERSION)
                .build();
        qcTaskMapper.insert(task);

        // 必检数值项（默认下限 0.05 上限 0.10），与 T2-02 展开形态一致：每项每样本一行空行
        for (int i = 1; i <= itemCount; i++) {
            Long itemId = insertItem(planId, "IT-" + String.format("%02d", i),
                    MesQcItemTypeEnum.NUMERIC.name(),
                    new BigDecimal("0.05"), new BigDecimal("0.10"), null, true);
            insertResultRows(task.getId(), itemId, sampleQty);
        }
        return task;
    }

    private Long insertItem(Long planId, String code, String itemType, BigDecimal lowerLimit,
                            BigDecimal upperLimit, String stdValue, boolean required) {
        QcItemDO item = QcItemDO.builder()
                .planId(planId)
                .itemCode(code)
                .itemName("检验项-" + code)
                .itemType(itemType)
                .stdValue(stdValue)
                .lowerLimit(lowerLimit)
                .upperLimit(upperLimit)
                .requiredFlag(required)
                .sortNo(1)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .build();
        qcItemMapper.insert(item);
        return item.getId();
    }

    /**
     * 改检验项的判定口径。
     *
     * <p>用 UpdateWrapper 显式 set 而不是 updateById：{@code updateById} 忽略 null 字段，
     * "只配下限 / 只配上限"的用例要把另一侧限位清成 null，写不进去就是假的双限位桩。
     */
    private void updateItem(Long itemId, String itemType, BigDecimal lowerLimit,
                            BigDecimal upperLimit, String stdValue) {
        qcItemMapper.update(null, new LambdaUpdateWrapper<QcItemDO>()
                .eq(QcItemDO::getId, itemId)
                .set(QcItemDO::getItemType, itemType)
                .set(QcItemDO::getLowerLimit, lowerLimit)
                .set(QcItemDO::getUpperLimit, upperLimit)
                .set(QcItemDO::getStdValue, stdValue));
    }

    /** 按样本数插预生成空行，实测值等列留空，即 T2-02 的产物形态。 */
    private void insertResultRows(Long taskId, Long itemId, int sampleQty) {
        QcItemDO item = qcItemMapper.selectById(itemId);
        List<QcResultDO> rows = new ArrayList<>();
        for (int sampleNo = 1; sampleNo <= sampleQty; sampleNo++) {
            rows.add(QcResultDO.builder()
                    .taskId(taskId)
                    .itemId(itemId)
                    .sampleNo(sampleNo)
                    .itemCode(item.getItemCode())
                    .itemName(item.getItemName())
                    .build());
        }
        qcResultMapper.insertBatch(rows);
    }

    private List<Long> itemIdsOf(Long taskId) {
        return qcResultMapper.selectListByTaskId(taskId).stream()
                .map(QcResultDO::getItemId)
                .distinct()
                .toList();
    }

    /** 只有一个检验项的桩任务，取它的检验项编号。 */
    private Long requireOnlyItemId(Long taskId) {
        List<Long> itemIds = itemIdsOf(taskId);
        assertEquals(1, itemIds.size());
        return itemIds.get(0);
    }

    private MesSaveQcResultBO saveBO(Long taskId, MesSaveQcResultBO.Item... rows) {
        MesSaveQcResultBO bo = new MesSaveQcResultBO();
        bo.setTaskId(taskId);
        bo.setInspectorId(INSPECTOR_ID);
        bo.setResults(List.of(rows));
        return bo;
    }

    private MesSaveQcResultBO.Item row(Long itemId, int sampleNo, String actualValue) {
        MesSaveQcResultBO.Item item = new MesSaveQcResultBO.Item();
        item.setItemId(itemId);
        item.setSampleNo(sampleNo);
        item.setActualValue(actualValue);
        return item;
    }

}

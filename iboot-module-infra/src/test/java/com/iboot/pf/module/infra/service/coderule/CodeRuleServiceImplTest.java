package com.iboot.pf.module.infra.service.coderule;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.infra.controller.admin.coderule.vo.CodeRuleSaveReqVO;
import com.iboot.pf.module.infra.dal.dataobject.coderule.CodeRuleDO;
import com.iboot.pf.module.infra.dal.dataobject.coderule.CodeSequenceDO;
import com.iboot.pf.module.infra.dal.mysql.coderule.CodeRuleMapper;
import com.iboot.pf.module.infra.dal.mysql.coderule.CodeSequenceMapper;
import com.iboot.pf.module.infra.enums.coderule.CodeResetCycleEnum;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.infra.enums.ErrorCodeConstants.CODE_RULE_DELETE_FAIL_USED;
import static com.iboot.pf.module.infra.enums.ErrorCodeConstants.CODE_RULE_RESET_CYCLE_INVALID;
import static com.iboot.pf.module.infra.enums.ErrorCodeConstants.CODE_RULE_SERIAL_OVERFLOW;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Import(CodeRuleServiceImpl.class)
public class CodeRuleServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CodeRuleService codeRuleService;
    @Resource
    private CodeRuleMapper codeRuleMapper;
    @Resource
    private CodeSequenceMapper codeSequenceMapper;

    @Test
    public void testGenerateCode_success() {
        CodeRuleDO rule = createRule("purchase-order", "PO", "-A", true, true, true,
                4, 1L, 2L, CodeResetCycleEnum.DAY.getType());

        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String actual1 = codeRuleService.generateCode(rule.getRuleCode());
        String actual2 = codeRuleService.generateCode(rule.getRuleCode());
        assertEquals("PO" + date + "0001-A", actual1);
        assertEquals("PO" + date + "0003-A", actual2);
    }

    @Test
    public void testPreviewCode_doesNotConsumeSerialNo() {
        CodeRuleDO rule = createRule("simple-order", "SO", "", false, false, false,
                4, 1L, 2L, CodeResetCycleEnum.NEVER.getType());

        assertEquals("SO0001", codeRuleService.previewCode(rule.getRuleCode()));
        assertEquals("SO0001", codeRuleService.generateCode(rule.getRuleCode()));
        assertEquals("SO0003", codeRuleService.previewCode(rule.getRuleCode()));
    }

    @Test
    public void testGenerateCode_periodKey() {
        assertPeriodKey("never-order", false, false, false, CodeResetCycleEnum.NEVER, "NONE");
        assertPeriodKey("year-order", true, false, false, CodeResetCycleEnum.YEAR,
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy")));
        assertPeriodKey("month-order", true, true, false, CodeResetCycleEnum.MONTH,
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")));
        assertPeriodKey("day-order", true, true, true, CodeResetCycleEnum.DAY,
                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
    }

    @Test
    public void testGenerateCode_overflow() {
        CodeRuleDO rule = createRule("overflow-order", "", "", false, false, false,
                1, 9L, 1L, CodeResetCycleEnum.NEVER.getType());
        assertEquals("9", codeRuleService.generateCode(rule.getRuleCode()));
        assertServiceException(() -> codeRuleService.generateCode(rule.getRuleCode()), CODE_RULE_SERIAL_OVERFLOW);
    }

    @Test
    public void testGenerateCode_concurrentlyNoDuplicate() throws Exception {
        CodeRuleDO rule = createRule("concurrent-order", "C", "", false, false, false,
                4, 1L, 1L, CodeResetCycleEnum.NEVER.getType());
        ExecutorService executor = Executors.newFixedThreadPool(20);
        try {
            List<Callable<String>> tasks = java.util.stream.IntStream.range(0, 20)
                    .<Callable<String>>mapToObj(i -> () -> codeRuleService.generateCode(rule.getRuleCode()))
                    .toList();
            Set<String> codes = new HashSet<>();
            for (Future<String> future : executor.invokeAll(tasks)) {
                codes.add(future.get());
            }
            assertEquals(20, codes.size());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void testUpdateCodeRule_takeEffectImmediately() {
        CodeRuleDO rule = createRule("update-order", "OLD", "", false, false, false,
                4, 1L, 1L, CodeResetCycleEnum.NEVER.getType());
        assertEquals("OLD0001", codeRuleService.generateCode(rule.getRuleCode()));

        CodeRuleSaveReqVO updateReqVO = buildRule(rule.getRuleCode(), "NEW", "", false, false, false,
                4, 1L, 1L, CodeResetCycleEnum.NEVER.getType());
        updateReqVO.setId(rule.getId());
        codeRuleService.updateCodeRule(updateReqVO);

        assertEquals("NEW0002", codeRuleService.generateCode(rule.getRuleCode()));
    }

    @Test
    public void testCreateCodeRule_invalidResetCycle() {
        CodeRuleSaveReqVO reqVO = buildRule("invalid-rule", "", "", false, false, false,
                4, 1L, 1L, CodeResetCycleEnum.DAY.getType());

        assertServiceException(() -> codeRuleService.createCodeRule(reqVO), CODE_RULE_RESET_CYCLE_INVALID);
    }

    @Test
    public void testDeleteCodeRule_failIfGenerated() {
        CodeRuleDO rule = createRule("used-rule", "", "", false, false, false,
                4, 1L, 1L, CodeResetCycleEnum.NEVER.getType());
        codeRuleService.generateCode(rule.getRuleCode());

        assertServiceException(() -> codeRuleService.deleteCodeRule(rule.getId()), CODE_RULE_DELETE_FAIL_USED);
    }

    private void assertPeriodKey(String ruleCode, boolean yearEnabled, boolean monthEnabled, boolean dayEnabled,
                                 CodeResetCycleEnum resetCycle, String expectedPeriodKey) {
        CodeRuleDO rule = createRule(ruleCode, "", "", yearEnabled, monthEnabled, dayEnabled,
                4, 1L, 1L, resetCycle.getType());
        codeRuleService.generateCode(ruleCode);
        CodeSequenceDO sequence = codeSequenceMapper.selectByRuleIdAndPeriodKey(rule.getId(), expectedPeriodKey);
        assertEquals(1L, sequence.getCurrentNo());
    }

    private CodeRuleDO createRule(String ruleCode, String prefix, String suffix, boolean yearEnabled,
                                  boolean monthEnabled, boolean dayEnabled, int serialLength,
                                  long serialStart, long serialStep, Integer resetCycle) {
        Long id = codeRuleService.createCodeRule(buildRule(ruleCode, prefix, suffix, yearEnabled, monthEnabled,
                dayEnabled, serialLength, serialStart, serialStep, resetCycle));
        return codeRuleMapper.selectById(id);
    }

    private CodeRuleSaveReqVO buildRule(String ruleCode, String prefix, String suffix, boolean yearEnabled,
                                        boolean monthEnabled, boolean dayEnabled, int serialLength,
                                        long serialStart, long serialStep, Integer resetCycle) {
        CodeRuleSaveReqVO reqVO = new CodeRuleSaveReqVO();
        reqVO.setRuleCode(ruleCode);
        reqVO.setName(ruleCode);
        reqVO.setPrefix(prefix);
        reqVO.setSuffix(suffix);
        reqVO.setYearEnabled(yearEnabled);
        reqVO.setMonthEnabled(monthEnabled);
        reqVO.setDayEnabled(dayEnabled);
        reqVO.setSerialLength(serialLength);
        reqVO.setSerialStart(serialStart);
        reqVO.setSerialStep(serialStep);
        reqVO.setResetCycle(resetCycle);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return reqVO;
    }

}

package com.iboot.pf.module.infra.service.coderule;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.infra.controller.admin.coderule.vo.CodeRulePageReqVO;
import com.iboot.pf.module.infra.controller.admin.coderule.vo.CodeRuleSaveReqVO;
import com.iboot.pf.module.infra.convert.coderule.CodeRuleConvert;
import com.iboot.pf.module.infra.dal.dataobject.coderule.CodeRuleDO;
import com.iboot.pf.module.infra.dal.dataobject.coderule.CodeSequenceDO;
import com.iboot.pf.module.infra.dal.mysql.coderule.CodeRuleMapper;
import com.iboot.pf.module.infra.dal.mysql.coderule.CodeSequenceMapper;
import com.iboot.pf.module.infra.enums.coderule.CodeResetCycleEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.infra.enums.ErrorCodeConstants.*;

@Service
@Validated
public class CodeRuleServiceImpl implements CodeRuleService {

    private static final String PERIOD_KEY_NEVER = "NONE";

    @Resource
    private CodeRuleMapper codeRuleMapper;
    @Resource
    private CodeSequenceMapper codeSequenceMapper;

    @Override
    public Long createCodeRule(CodeRuleSaveReqVO createReqVO) {
        validateRuleCodeUnique(null, createReqVO.getRuleCode());
        validateRuleConfig(createReqVO);
        CodeRuleDO codeRule = CodeRuleConvert.INSTANCE.convert(createReqVO);
        normalizeText(codeRule);
        codeRuleMapper.insert(codeRule);
        return codeRule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCodeRule(CodeRuleSaveReqVO updateReqVO) {
        validateCodeRuleExists(updateReqVO.getId());
        validateRuleCodeUnique(updateReqVO.getId(), updateReqVO.getRuleCode());
        validateRuleConfig(updateReqVO);
        CodeRuleDO updateObj = CodeRuleConvert.INSTANCE.convert(updateReqVO);
        normalizeText(updateObj);
        codeRuleMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCodeRule(Long id) {
        if (codeRuleMapper.selectByIdForUpdate(id) == null) {
            throw exception(CODE_RULE_NOT_EXISTS);
        }
        if (codeSequenceMapper.existsByRuleId(id)) {
            throw exception(CODE_RULE_DELETE_FAIL_USED);
        }
        codeRuleMapper.deleteById(id);
    }

    @Override
    public CodeRuleDO getCodeRule(Long id) {
        return codeRuleMapper.selectById(id);
    }

    @Override
    public PageResult<CodeRuleDO> getCodeRulePage(CodeRulePageReqVO pageReqVO) {
        return codeRuleMapper.selectPage(pageReqVO);
    }

    @Override
    public String previewCode(String ruleCode) {
        CodeRuleDO codeRule = validateCodeRuleEnabled(ruleCode);
        LocalDate date = LocalDate.now();
        String periodKey = buildPeriodKey(codeRule.getResetCycle(), date);
        CodeSequenceDO sequence = codeSequenceMapper.selectByRuleIdAndPeriodKey(codeRule.getId(), periodKey);
        long serialNo = sequence == null ? codeRule.getSerialStart() : sequence.getCurrentNo() + codeRule.getSerialStep();
        return formatCode(codeRule, date, serialNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateCode(String ruleCode) {
        // 锁定规则行，使规则更新与编号分配按同一规则串行执行。
        CodeRuleDO codeRule = validateCodeRuleEnabledForUpdate(ruleCode);
        LocalDate date = LocalDate.now();
        String periodKey = buildPeriodKey(codeRule.getResetCycle(), date);
        int affectedRows = codeSequenceMapper.allocateSerialNo(codeRule.getId(), periodKey,
                codeRule.getSerialStart(), codeRule.getSerialStep());
        long serialNo = affectedRows == 1 ? codeRule.getSerialStart()
                : codeSequenceMapper.selectLastAllocatedSerialNo();
        return formatCode(codeRule, date, serialNo);
    }

    private CodeRuleDO validateCodeRuleEnabled(String ruleCode) {
        CodeRuleDO codeRule = codeRuleMapper.selectByRuleCode(ruleCode);
        if (codeRule == null) {
            throw exception(CODE_RULE_NOT_EXISTS);
        }
        if (CommonStatusEnum.isDisable(codeRule.getStatus())) {
            throw exception(CODE_RULE_DISABLED);
        }
        return codeRule;
    }

    private CodeRuleDO validateCodeRuleEnabledForUpdate(String ruleCode) {
        CodeRuleDO codeRule = codeRuleMapper.selectByRuleCodeForUpdate(ruleCode);
        if (codeRule == null) {
            throw exception(CODE_RULE_NOT_EXISTS);
        }
        if (CommonStatusEnum.isDisable(codeRule.getStatus())) {
            throw exception(CODE_RULE_DISABLED);
        }
        return codeRule;
    }

    private void validateCodeRuleExists(Long id) {
        if (codeRuleMapper.selectById(id) == null) {
            throw exception(CODE_RULE_NOT_EXISTS);
        }
    }

    private void validateRuleCodeUnique(Long id, String ruleCode) {
        CodeRuleDO codeRule = codeRuleMapper.selectByRuleCode(ruleCode);
        if (codeRule != null && !codeRule.getId().equals(id)) {
            throw exception(CODE_RULE_CODE_DUPLICATE);
        }
    }

    private void validateRuleConfig(CodeRuleSaveReqVO rule) {
        long maxSerialNo = Long.parseLong("9".repeat(rule.getSerialLength()));
        if (rule.getSerialStart() > maxSerialNo || rule.getSerialStep() > maxSerialNo) {
            throw exception(CODE_RULE_SERIAL_CONFIG_INVALID);
        }
        if (CodeResetCycleEnum.YEAR.getType().equals(rule.getResetCycle()) && !rule.getYearEnabled()
                || CodeResetCycleEnum.MONTH.getType().equals(rule.getResetCycle())
                && (!rule.getYearEnabled() || !rule.getMonthEnabled())
                || CodeResetCycleEnum.DAY.getType().equals(rule.getResetCycle())
                && (!rule.getYearEnabled() || !rule.getMonthEnabled() || !rule.getDayEnabled())) {
            throw exception(CODE_RULE_RESET_CYCLE_INVALID);
        }
    }

    private String buildPeriodKey(Integer resetCycle, LocalDate date) {
        if (CodeResetCycleEnum.YEAR.getType().equals(resetCycle)) {
            return date.format(DateTimeFormatter.ofPattern("yyyy"));
        }
        if (CodeResetCycleEnum.MONTH.getType().equals(resetCycle)) {
            return date.format(DateTimeFormatter.ofPattern("yyyyMM"));
        }
        if (CodeResetCycleEnum.DAY.getType().equals(resetCycle)) {
            return date.format(DateTimeFormatter.BASIC_ISO_DATE);
        }
        return PERIOD_KEY_NEVER;
    }

    private String formatCode(CodeRuleDO rule, LocalDate date, long serialNo) {
        long maxSerialNo = Long.parseLong("9".repeat(rule.getSerialLength()));
        if (serialNo > maxSerialNo) {
            throw exception(CODE_RULE_SERIAL_OVERFLOW);
        }
        StringBuilder builder = new StringBuilder(rule.getPrefix());
        if (rule.getYearEnabled()) {
            builder.append(date.format(DateTimeFormatter.ofPattern("yyyy")));
        }
        if (rule.getMonthEnabled()) {
            builder.append(date.format(DateTimeFormatter.ofPattern("MM")));
        }
        if (rule.getDayEnabled()) {
            builder.append(date.format(DateTimeFormatter.ofPattern("dd")));
        }
        builder.append(String.format("%0" + rule.getSerialLength() + "d", serialNo));
        return builder.append(rule.getSuffix()).toString();
    }

    private void normalizeText(CodeRuleDO rule) {
        if (rule.getPrefix() == null) {
            rule.setPrefix("");
        }
        if (rule.getSuffix() == null) {
            rule.setSuffix("");
        }
    }

}

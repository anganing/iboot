package com.iboot.pf.module.mes.service.qcrule;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mes.controller.admin.qcrule.vo.QcRulePageReqVO;
import com.iboot.pf.module.mes.controller.admin.qcrule.vo.QcRuleRespVO;
import com.iboot.pf.module.mes.controller.admin.qcrule.vo.QcRuleSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import com.iboot.pf.module.mes.dal.dataobject.qcrule.QcRuleDO;
import com.iboot.pf.module.mes.dal.mysql.qcrule.QcRuleMapper;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesQcPlanStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.qcplan.QcPlanService;
import com.iboot.pf.module.mes.service.snapshot.MesRelationFiller;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RULE_CODE_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RULE_HAS_TASK;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RULE_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_PLAN_NOT_PUBLISHED;

/**
 * MES-质检触发规则 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class QcRuleServiceImpl implements QcRuleService {

    @Resource
    private QcRuleMapper qcRuleMapper;
    @Resource
    private QcTaskMapper qcTaskMapper;
    @Resource
    private QcPlanService qcPlanService;
    @Resource
    private MesCodeService codeService;
    @Resource
    private MesSnapshotService snapshotService;
    @Resource
    private MesRelationFiller relationFiller;

    @Override
    public Long createQcRule(QcRuleSaveReqVO createReqVO) {
        QcRuleDO qcRule = BeanUtils.toBean(createReqVO, QcRuleDO.class);
        qcRule.setId(null);
        qcRule.setCode(codeService.generate(MesCodeRuleConstants.QC_RULE, qcRule.getCode()));
        prepare(qcRule);
        validateCodeUnique(qcRule.getCode(), null);
        qcRuleMapper.insert(qcRule);
        return qcRule.getId();
    }

    @Override
    public void updateQcRule(QcRuleSaveReqVO updateReqVO) {
        QcRuleDO existed = validateQcRuleExists(updateReqVO.getId());
        QcRuleDO updateObj = BeanUtils.toBean(updateReqVO, QcRuleDO.class);
        // 编码由编码规则分配，更新时不允许改
        updateObj.setCode(existed.getCode());
        prepare(updateObj);
        qcRuleMapper.updateById(updateObj);
    }

    @Override
    public void deleteQcRule(Long id) {
        validateQcRuleExists(id);
        validateNoTask(id);
        qcRuleMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQcRuleListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            validateQcRuleExists(id);
            validateNoTask(id);
        }
        qcRuleMapper.deleteByIds(ids);
    }

    private QcRuleDO validateQcRuleExists(Long id) {
        QcRuleDO qcRule = id == null ? null : qcRuleMapper.selectById(id);
        if (qcRule == null) {
            throw exception(QC_RULE_NOT_EXISTS);
        }
        return qcRule;
    }

    /** 已命中生成过质检任务的规则不允许删除，否则任务追溯不到规则。 */
    private void validateNoTask(Long ruleId) {
        if (qcTaskMapper.selectCountByRuleId(ruleId) > 0) {
            throw exception(QC_RULE_HAS_TASK);
        }
    }

    /**
     * 落库前的公共处理：可选匹配维度归零并校验存在，引用的方案必须已发布。
     */
    private void prepare(QcRuleDO qcRule) {
        qcRule.setMatId(qcRule.getMatId() == null ? MesConstants.NONE_ID : qcRule.getMatId());
        qcRule.setMatCatId(qcRule.getMatCatId() == null ? MesConstants.NONE_ID : qcRule.getMatCatId());
        qcRule.setOpId(qcRule.getOpId() == null ? MesConstants.NONE_ID : qcRule.getOpId());
        qcRule.setWcId(qcRule.getWcId() == null ? MesConstants.NONE_ID : qcRule.getWcId());
        qcRule.setSupplierId(qcRule.getSupplierId() == null ? MesConstants.NONE_ID : qcRule.getSupplierId());
        if (!MesConstants.NONE_ID.equals(qcRule.getMatId())) {
            snapshotService.requireMaterial(qcRule.getMatId());
        }
        if (!MesConstants.NONE_ID.equals(qcRule.getOpId())) {
            snapshotService.requireOp(qcRule.getOpId());
        }
        if (!MesConstants.NONE_ID.equals(qcRule.getWcId())) {
            snapshotService.validateWc(qcRule.getWcId());
        }
        QcPlanDO qcPlan = qcPlanService.validateQcPlanExists(qcRule.getQcPlanId());
        if (!MesQcPlanStatusEnum.isPublished(qcPlan.getStatus())) {
            throw exception(QC_TASK_PLAN_NOT_PUBLISHED);
        }
    }

    /** 规则编码唯一：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。 */
    private void validateCodeUnique(String code, Long excludeId) {
        QcRuleDO existed = qcRuleMapper.selectByCode(code);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(QC_RULE_CODE_DUPLICATE);
    }

    @Override
    public QcRuleRespVO getQcRule(Long id) {
        QcRuleRespVO respVO = qcRuleMapper.selectJoinById(id);
        fillRelationNames(respVO == null ? Collections.emptyList() : Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public PageResult<QcRuleRespVO> getQcRulePage(QcRulePageReqVO pageReqVO) {
        PageResult<QcRuleRespVO> pageResult = qcRuleMapper.selectPage(pageReqVO);
        fillRelationNames(pageResult.getList());
        return pageResult;
    }

    /** 跨模块可读字段补全，放在 Service 以便 Excel 导出复用。 */
    private void fillRelationNames(List<QcRuleRespVO> list) {
        relationFiller.fillMaterialName(list, QcRuleRespVO::getMatId, QcRuleRespVO::setMatName);
        relationFiller.fillOpName(list, QcRuleRespVO::getOpId, QcRuleRespVO::setOpName);
        relationFiller.fillWcName(list, QcRuleRespVO::getWcId, QcRuleRespVO::setWcName);
    }

}

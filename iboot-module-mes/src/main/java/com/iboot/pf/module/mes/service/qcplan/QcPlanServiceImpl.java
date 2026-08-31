package com.iboot.pf.module.mes.service.qcplan;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.QcPlanPageReqVO;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.QcPlanRespVO;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.QcPlanSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import com.iboot.pf.module.mes.dal.mysql.qcitem.QcItemMapper;
import com.iboot.pf.module.mes.dal.mysql.qcplan.QcPlanMapper;
import com.iboot.pf.module.mes.dal.mysql.qcrule.QcRuleMapper;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesQcPlanStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcSampleTypeEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_AQL_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_CODE_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_HAS_RULE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_SAMPLE_VALUE_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_STATUS_INVALID;

/**
 * MES-检验方案 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class QcPlanServiceImpl implements QcPlanService {

    @Resource
    private QcPlanMapper qcPlanMapper;
    @Resource
    private QcItemMapper qcItemMapper;
    @Resource
    private QcRuleMapper qcRuleMapper;
    @Resource
    private MesCodeService codeService;

    @Override
    public Long createQcPlan(QcPlanSaveReqVO createReqVO) {
        QcPlanDO qcPlan = BeanUtils.toBean(createReqVO, QcPlanDO.class);
        qcPlan.setId(null);
        qcPlan.setCode(codeService.generate(MesCodeRuleConstants.QC_PLAN, qcPlan.getCode()));
        validateSampleConfig(qcPlan);
        validateCodeAndVersionUnique(qcPlan, null);
        qcPlanMapper.insert(qcPlan);
        return qcPlan.getId();
    }

    @Override
    public void updateQcPlan(QcPlanSaveReqVO updateReqVO) {
        QcPlanDO existed = validateQcPlanExists(updateReqVO.getId());
        QcPlanDO updateObj = BeanUtils.toBean(updateReqVO, QcPlanDO.class);
        // 编码由编码规则分配，更新时不允许改
        updateObj.setCode(existed.getCode());
        // 已发布的方案被质检任务锁定引用，只允许改状态与备注，抽样规则不能动
        if (!MesQcPlanStatusEnum.isDraft(existed.getStatus())) {
            existed.setStatus(updateObj.getStatus());
            existed.setRemark(updateObj.getRemark());
            qcPlanMapper.updateById(existed);
            return;
        }
        validateSampleConfig(updateObj);
        validateCodeAndVersionUnique(updateObj, updateReqVO.getId());
        qcPlanMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQcPlan(Long id) {
        QcPlanDO existed = validateQcPlanExists(id);
        // 已发布的方案可能被质检任务锁定，只有草稿允许删除
        if (!MesQcPlanStatusEnum.isDraft(existed.getStatus())) {
            throw exception(QC_PLAN_STATUS_INVALID);
        }
        validateNoRule(id);
        qcPlanMapper.deleteById(id);
        // 方案删除后检验项没有归属，必须一起清理
        qcItemMapper.deleteByPlanId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQcPlanListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            QcPlanDO existed = validateQcPlanExists(id);
            if (!MesQcPlanStatusEnum.isDraft(existed.getStatus())) {
                throw exception(QC_PLAN_STATUS_INVALID);
            }
            validateNoRule(id);
        }
        qcPlanMapper.deleteByIds(ids);
        qcItemMapper.deleteByPlanIds(ids);
    }

    @Override
    public QcPlanDO validateQcPlanExists(Long id) {
        QcPlanDO qcPlan = id == null ? null : qcPlanMapper.selectById(id);
        if (qcPlan == null) {
            throw exception(QC_PLAN_NOT_EXISTS);
        }
        return qcPlan;
    }

    /** 被质检规则引用的方案不允许删除。 */
    private void validateNoRule(Long qcPlanId) {
        if (qcRuleMapper.selectCountByQcPlanId(qcPlanId) > 0) {
            throw exception(QC_PLAN_HAS_RULE);
        }
    }

    /**
     * 抽样配置的跨字段校验。
     *
     * <p>固定数量、按比例抽检必须给抽样值；AQL 抽检必须给接收质量限，否则生成质检任务时算不出抽样数量。
     */
    private void validateSampleConfig(QcPlanDO qcPlan) {
        if (MesQcSampleTypeEnum.needSampleValue(qcPlan.getSampleType()) && qcPlan.getSampleValue() == null) {
            throw exception(QC_PLAN_SAMPLE_VALUE_REQUIRED);
        }
        if (MesQcSampleTypeEnum.isAql(qcPlan.getSampleType())
                && (qcPlan.getAql() == null || qcPlan.getAql().isBlank())) {
            throw exception(QC_PLAN_AQL_REQUIRED);
        }
    }

    /** 编码 + 版本唯一：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。 */
    private void validateCodeAndVersionUnique(QcPlanDO qcPlan, Long excludeId) {
        QcPlanDO existed = qcPlanMapper.selectByCodeAndVersion(qcPlan.getCode(), qcPlan.getVersionNo());
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(QC_PLAN_CODE_DUPLICATE);
    }

    @Override
    public QcPlanRespVO getQcPlan(Long id) {
        return qcPlanMapper.selectJoinById(id);
    }

    @Override
    public PageResult<QcPlanRespVO> getQcPlanPage(QcPlanPageReqVO pageReqVO) {
        return qcPlanMapper.selectPage(pageReqVO);
    }

}

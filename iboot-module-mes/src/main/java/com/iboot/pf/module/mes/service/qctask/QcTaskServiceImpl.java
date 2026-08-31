package com.iboot.pf.module.mes.service.qctask;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.qcresult.QcResultMapper;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesQcPlanStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum;
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
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.IDEMPOTENCY_KEY_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_HAS_RESULT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_NO_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_PLAN_NOT_PUBLISHED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_SAMPLE_QTY_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_STATUS_INVALID;

/**
 * MES-质检任务 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class QcTaskServiceImpl implements QcTaskService {

    @Resource
    private QcTaskMapper qcTaskMapper;
    @Resource
    private QcResultMapper qcResultMapper;
    @Resource
    private QcPlanService qcPlanService;
    @Resource
    private MesCodeService codeService;
    @Resource
    private MesSnapshotService snapshotService;
    @Resource
    private MesRelationFiller relationFiller;

    @Override
    public Long createQcTask(QcTaskSaveReqVO createReqVO) {
        QcTaskDO qcTask = BeanUtils.toBean(createReqVO, QcTaskDO.class);
        qcTask.setId(null);
        qcTask.setTaskNo(codeService.generate(MesCodeRuleConstants.QC_TASK, qcTask.getTaskNo()));
        prepare(qcTask);
        // 合格与不合格数量由检验结果汇总，创建时归零
        qcTask.setQtyQualified(MesConstants.ZERO_QTY);
        qcTask.setQtyRejected(MesConstants.ZERO_QTY);
        qcTask.setVersion(MesConstants.INIT_VERSION);
        validateTaskNoUnique(qcTask.getTaskNo(), null);
        validateIdempotencyKeyUnique(qcTask.getIdempotencyKey(), null);
        qcTaskMapper.insert(qcTask);
        return qcTask.getId();
    }

    @Override
    public void updateQcTask(QcTaskSaveReqVO updateReqVO) {
        QcTaskDO existed = validateQcTaskExists(updateReqVO.getId());
        // 已完成的任务结论与数量已经落定，不允许再改
        if (MesQcTaskStatusEnum.isFinal(existed.getStatus())) {
            throw exception(QC_TASK_STATUS_INVALID);
        }
        QcTaskDO updateObj = BeanUtils.toBean(updateReqVO, QcTaskDO.class);
        updateObj.setTaskNo(existed.getTaskNo());
        // 汇总数量与乐观锁版本不由表单维护
        updateObj.setQtyQualified(existed.getQtyQualified());
        updateObj.setQtyRejected(existed.getQtyRejected());
        updateObj.setVersion(existed.getVersion());
        prepare(updateObj);
        validateIdempotencyKeyUnique(updateObj.getIdempotencyKey(), updateReqVO.getId());
        qcTaskMapper.updateById(updateObj);
    }

    @Override
    public void deleteQcTask(Long id) {
        QcTaskDO existed = validateQcTaskExists(id);
        if (MesQcTaskStatusEnum.isDone(existed.getStatus())) {
            throw exception(QC_TASK_STATUS_INVALID);
        }
        validateNoResult(id);
        qcTaskMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQcTaskListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            QcTaskDO existed = validateQcTaskExists(id);
            if (MesQcTaskStatusEnum.isDone(existed.getStatus())) {
                throw exception(QC_TASK_STATUS_INVALID);
            }
            validateNoResult(id);
        }
        qcTaskMapper.deleteByIds(ids);
    }

    @Override
    public QcTaskDO validateQcTaskExists(Long id) {
        QcTaskDO qcTask = id == null ? null : qcTaskMapper.selectById(id);
        if (qcTask == null) {
            throw exception(QC_TASK_NOT_EXISTS);
        }
        return qcTask;
    }

    /** 已录入检验结果的任务不允许删除，否则结果成为孤儿数据。 */
    private void validateNoResult(Long taskId) {
        if (qcResultMapper.selectCountByTaskId(taskId) > 0) {
            throw exception(QC_TASK_HAS_RESULT);
        }
    }

    /**
     * 落库前的公共处理：锁定方案版本、补全物料与批次快照、校验抽样数量、可选外键归零。
     */
    private void prepare(QcTaskDO qcTask) {
        QcPlanDO qcPlan = qcPlanService.validateQcPlanExists(qcTask.getQcPlanId());
        // 只有已发布的方案才允许被任务锁定引用
        if (!MesQcPlanStatusEnum.isPublished(qcPlan.getStatus())) {
            throw exception(QC_TASK_PLAN_NOT_PUBLISHED);
        }
        qcTask.setQcPlanVersion(qcPlan.getVersionNo());
        MaterialRespDTO material = snapshotService.requireMaterial(qcTask.getMatId());
        qcTask.setMatCode(material.getCode());
        qcTask.setMatName(material.getName());
        qcTask.setMatSpec(material.getSpec());
        qcTask.setUomCode(snapshotService.requireUomCode(qcTask.getUomId()));
        qcTask.setRuleId(qcTask.getRuleId() == null ? MesConstants.NONE_ID : qcTask.getRuleId());
        qcTask.setBizLineId(qcTask.getBizLineId() == null ? MesConstants.NONE_ID : qcTask.getBizLineId());
        qcTask.setBatchId(qcTask.getBatchId() == null ? MesConstants.NONE_ID : qcTask.getBatchId());
        qcTask.setLotNo(snapshotService.lotNoOrNull(qcTask.getBatchId()));
        qcTask.setAssignedInspectorId(qcTask.getAssignedInspectorId() == null
                ? MesConstants.NONE_ID : qcTask.getAssignedInspectorId());
        snapshotService.validateUser(qcTask.getAssignedInspectorId());
        // 抽样数量不能大于送检数量，否则抽检口径本身就是错的
        if (qcTask.getSampleQty().compareTo(qcTask.getQty()) > 0) {
            throw exception(QC_TASK_SAMPLE_QTY_INVALID);
        }
    }

    /** 任务号唯一：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。 */
    private void validateTaskNoUnique(String taskNo, Long excludeId) {
        QcTaskDO existed = qcTaskMapper.selectByTaskNo(taskNo);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(QC_TASK_NO_DUPLICATE);
    }

    /** 幂等键唯一：同一业务重复触发质检时直接拒绝。 */
    private void validateIdempotencyKeyUnique(String idempotencyKey, Long excludeId) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }
        QcTaskDO existed = qcTaskMapper.selectByIdempotencyKey(idempotencyKey);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(IDEMPOTENCY_KEY_DUPLICATE);
    }

    @Override
    public QcTaskRespVO getQcTask(Long id) {
        QcTaskRespVO respVO = qcTaskMapper.selectJoinById(id);
        fillRelationNames(respVO == null ? Collections.emptyList() : Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public PageResult<QcTaskRespVO> getQcTaskPage(QcTaskPageReqVO pageReqVO) {
        PageResult<QcTaskRespVO> pageResult = qcTaskMapper.selectPage(pageReqVO);
        fillRelationNames(pageResult.getList());
        return pageResult;
    }

    /** 检验员在 system 模块，跨模块只能走门面批量补全。 */
    private void fillRelationNames(List<QcTaskRespVO> list) {
        relationFiller.fillUserNickname(list, QcTaskRespVO::getAssignedInspectorId,
                QcTaskRespVO::setAssignedInspectorName);
    }

}

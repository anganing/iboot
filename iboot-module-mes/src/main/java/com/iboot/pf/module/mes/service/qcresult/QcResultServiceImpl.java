package com.iboot.pf.module.mes.service.qcresult;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mes.controller.admin.qcresult.vo.QcResultPageReqVO;
import com.iboot.pf.module.mes.controller.admin.qcresult.vo.QcResultRespVO;
import com.iboot.pf.module.mes.controller.admin.qcresult.vo.QcResultSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.qcitem.QcItemDO;
import com.iboot.pf.module.mes.dal.dataobject.qcresult.QcResultDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.defect.DefectMapper;
import com.iboot.pf.module.mes.dal.mysql.qcitem.QcItemMapper;
import com.iboot.pf.module.mes.dal.mysql.qcresult.QcResultMapper;
import com.iboot.pf.module.mes.enums.MesQcItemConclusionEnum;
import com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum;
import com.iboot.pf.module.mes.service.qctask.QcTaskService;
import com.iboot.pf.module.mes.service.snapshot.MesRelationFiller;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_ITEM_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RESULT_CONCLUSION_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RESULT_HAS_DEFECT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RESULT_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RESULT_SAMPLE_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RESULT_TASK_DONE;

/**
 * MES-检验结果 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class QcResultServiceImpl implements QcResultService {

    @Resource
    private QcResultMapper qcResultMapper;
    @Resource
    private QcItemMapper qcItemMapper;
    @Resource
    private DefectMapper defectMapper;
    @Resource
    private QcTaskService qcTaskService;
    @Resource
    private MesSnapshotService snapshotService;
    @Resource
    private MesRelationFiller relationFiller;

    @Override
    public Long createQcResult(QcResultSaveReqVO createReqVO) {
        QcResultDO qcResult = BeanUtils.toBean(createReqVO, QcResultDO.class);
        qcResult.setId(null);
        prepare(qcResult);
        validateSampleUnique(qcResult, null);
        qcResultMapper.insert(qcResult);
        return qcResult.getId();
    }

    @Override
    public void updateQcResult(QcResultSaveReqVO updateReqVO) {
        validateQcResultExists(updateReqVO.getId());
        QcResultDO qcResult = BeanUtils.toBean(updateReqVO, QcResultDO.class);
        prepare(qcResult);
        // 改自己不能撞自己，所以排除自身编号
        validateSampleUnique(qcResult, updateReqVO.getId());
        qcResultMapper.updateById(qcResult);
    }

    @Override
    public void deleteQcResult(Long id) {
        QcResultDO existed = validateQcResultExists(id);
        validateTaskEditable(existed.getTaskId());
        validateNoDefect(id);
        qcResultMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQcResultListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            QcResultDO existed = validateQcResultExists(id);
            validateTaskEditable(existed.getTaskId());
            validateNoDefect(id);
        }
        qcResultMapper.deleteByIds(ids);
    }

    @Override
    public QcResultDO validateQcResultExists(Long id) {
        QcResultDO qcResult = id == null ? null : qcResultMapper.selectById(id);
        if (qcResult == null) {
            throw exception(QC_RESULT_NOT_EXISTS);
        }
        return qcResult;
    }

    /** 已挂缺陷记录的检验结果不允许删除，否则缺陷追溯不到判定依据。 */
    private void validateNoDefect(Long resultId) {
        if (defectMapper.selectCountByResultId(resultId) > 0) {
            throw exception(QC_RESULT_HAS_DEFECT);
        }
    }

    /**
     * 落库前的公共处理：任务必须未完成、检验项快照补全、结论与合格标记一致、检验员校验。
     */
    private void prepare(QcResultDO qcResult) {
        validateTaskEditable(qcResult.getTaskId());
        QcItemDO qcItem = qcItemMapper.selectById(qcResult.getItemId());
        if (qcItem == null) {
            throw exception(QC_ITEM_NOT_EXISTS);
        }
        qcResult.setItemCode(qcItem.getItemCode());
        qcResult.setItemName(qcItem.getItemName());
        snapshotService.validateUser(qcResult.getInspectorId());
        // 结论与是否合格标记必须一致，否则两个字段互相矛盾，报表统计口径就乱了
        if (!MesQcItemConclusionEnum.matches(qcResult.getConclusion(), qcResult.getPassFlag())) {
            throw exception(QC_RESULT_CONCLUSION_CONFLICT);
        }
        if (qcResult.getInspectTime() == null) {
            qcResult.setInspectTime(LocalDateTime.now());
        }
    }

    /** 质检任务已完成后结论已落定，不允许再维护检验结果。 */
    private void validateTaskEditable(Long taskId) {
        QcTaskDO qcTask = qcTaskService.validateQcTaskExists(taskId);
        if (MesQcTaskStatusEnum.isDone(qcTask.getStatus())) {
            throw exception(QC_RESULT_TASK_DONE);
        }
    }

    /** 同一任务下同一检验项的样本序号唯一，对应唯一索引 (task_id, item_id, sample_no, deleted)。 */
    private void validateSampleUnique(QcResultDO qcResult, Long excludeId) {
        QcResultDO existed = qcResultMapper.selectBySample(qcResult.getTaskId(), qcResult.getItemId(),
                qcResult.getSampleNo());
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(QC_RESULT_SAMPLE_DUPLICATE);
    }

    @Override
    public QcResultRespVO getQcResult(Long id) {
        QcResultRespVO respVO = qcResultMapper.selectJoinById(id);
        fillRelationNames(respVO == null ? Collections.emptyList() : Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public PageResult<QcResultRespVO> getQcResultPage(QcResultPageReqVO pageReqVO) {
        PageResult<QcResultRespVO> pageResult = qcResultMapper.selectPage(pageReqVO);
        fillRelationNames(pageResult.getList());
        return pageResult;
    }

    /** 检验员在 system 模块，跨模块只能走门面批量补全。 */
    private void fillRelationNames(List<QcResultRespVO> list) {
        relationFiller.fillUserNickname(list, QcResultRespVO::getInspectorId, QcResultRespVO::setInspectorName);
    }

}

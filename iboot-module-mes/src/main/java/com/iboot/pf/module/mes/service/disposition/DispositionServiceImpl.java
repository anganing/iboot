package com.iboot.pf.module.mes.service.disposition;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mes.controller.admin.disposition.vo.DispositionPageReqVO;
import com.iboot.pf.module.mes.controller.admin.disposition.vo.DispositionRespVO;
import com.iboot.pf.module.mes.controller.admin.disposition.vo.DispositionSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.disposition.DispositionMapper;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesDispositionStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
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
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_APPROVER_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_NO_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_QTY_EXCEEDED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.IDEMPOTENCY_KEY_DUPLICATE;

/**
 * MES-不合格品处置单 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class DispositionServiceImpl implements DispositionService {

    @Resource
    private DispositionMapper dispositionMapper;
    @Resource
    private QcTaskService qcTaskService;
    @Resource
    private MesCodeService codeService;
    @Resource
    private MesSnapshotService snapshotService;
    @Resource
    private MesRelationFiller relationFiller;

    @Override
    public Long createDisposition(DispositionSaveReqVO createReqVO) {
        DispositionDO disposition = BeanUtils.toBean(createReqVO, DispositionDO.class);
        disposition.setId(null);
        disposition.setDispositionNo(codeService.generate(MesCodeRuleConstants.DISPOSITION,
                disposition.getDispositionNo()));
        prepare(disposition);
        validateDispositionNoUnique(disposition.getDispositionNo(), null);
        validateIdempotencyKeyUnique(disposition.getIdempotencyKey(), null);
        dispositionMapper.insert(disposition);
        return disposition.getId();
    }

    @Override
    public void updateDisposition(DispositionSaveReqVO updateReqVO) {
        DispositionDO existed = validateDispositionExists(updateReqVO.getId());
        // 只有新建可改；提交后进入审批或执行链，改主体会让执行凭据与处置内容对不上
        if (!MesDispositionStatusEnum.isDraft(existed.getStatus())) {
            throw exception(DISPOSITION_STATUS_INVALID);
        }
        DispositionDO updateObj = BeanUtils.toBean(updateReqVO, DispositionDO.class);
        updateObj.setDispositionNo(existed.getDispositionNo());
        prepare(updateObj);
        validateIdempotencyKeyUnique(updateObj.getIdempotencyKey(), updateReqVO.getId());
        dispositionMapper.updateById(updateObj);
    }

    @Override
    public void deleteDisposition(Long id) {
        DispositionDO existed = validateDispositionExists(id);
        if (!MesDispositionStatusEnum.isDraft(existed.getStatus())) {
            throw exception(DISPOSITION_STATUS_INVALID);
        }
        dispositionMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDispositionListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            DispositionDO existed = validateDispositionExists(id);
            if (!MesDispositionStatusEnum.isDraft(existed.getStatus())) {
                throw exception(DISPOSITION_STATUS_INVALID);
            }
        }
        dispositionMapper.deleteByIds(ids);
    }

    private DispositionDO validateDispositionExists(Long id) {
        DispositionDO disposition = id == null ? null : dispositionMapper.selectById(id);
        if (disposition == null) {
            throw exception(DISPOSITION_NOT_EXISTS);
        }
        return disposition;
    }

    /**
     * 落库前的公共处理：处置数量不能超过任务不合格数量，审批结论已给出时审批人与审批时间必填。
     */
    private void prepare(DispositionDO disposition) {
        QcTaskDO qcTask = qcTaskService.validateQcTaskExists(disposition.getTaskId());
        if (disposition.getQty().compareTo(qcTask.getQtyRejected()) > 0) {
            throw exception(DISPOSITION_QTY_EXCEEDED);
        }
        disposition.setApprovedBy(disposition.getApprovedBy() == null
                ? MesConstants.NONE_ID : disposition.getApprovedBy());
        snapshotService.validateUser(disposition.getApprovedBy());
        if (MesDispositionStatusEnum.isApprovalDecided(disposition.getStatus())) {
            if (MesConstants.NONE_ID.equals(disposition.getApprovedBy())) {
                throw exception(DISPOSITION_APPROVER_REQUIRED);
            }
            if (disposition.getApproveTime() == null) {
                disposition.setApproveTime(LocalDateTime.now());
            }
        }
    }

    /** 处置单号唯一：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。 */
    private void validateDispositionNoUnique(String dispositionNo, Long excludeId) {
        DispositionDO existed = dispositionMapper.selectByDispositionNo(dispositionNo);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(DISPOSITION_NO_DUPLICATE);
    }

    /** 幂等键唯一。 */
    private void validateIdempotencyKeyUnique(String idempotencyKey, Long excludeId) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }
        DispositionDO existed = dispositionMapper.selectByIdempotencyKey(idempotencyKey);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(IDEMPOTENCY_KEY_DUPLICATE);
    }

    @Override
    public DispositionRespVO getDisposition(Long id) {
        DispositionRespVO respVO = dispositionMapper.selectJoinById(id);
        fillRelationNames(respVO == null ? Collections.emptyList() : Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public PageResult<DispositionRespVO> getDispositionPage(DispositionPageReqVO pageReqVO) {
        PageResult<DispositionRespVO> pageResult = dispositionMapper.selectPage(pageReqVO);
        fillRelationNames(pageResult.getList());
        return pageResult;
    }

    /** 审批人在 system 模块，跨模块只能走门面批量补全。 */
    private void fillRelationNames(List<DispositionRespVO> list) {
        relationFiller.fillUserNickname(list, DispositionRespVO::getApprovedBy,
                DispositionRespVO::setApprovedByName);
    }

}

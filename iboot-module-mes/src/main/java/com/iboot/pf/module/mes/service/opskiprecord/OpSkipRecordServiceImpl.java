package com.iboot.pf.module.mes.service.opskiprecord;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mes.controller.admin.opskiprecord.vo.OpSkipRecordPageReqVO;
import com.iboot.pf.module.mes.controller.admin.opskiprecord.vo.OpSkipRecordRespVO;
import com.iboot.pf.module.mes.controller.admin.opskiprecord.vo.OpSkipRecordSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.opskiprecord.OpSkipRecordDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.mysql.opskiprecord.OpSkipRecordMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesSkipApproveStatusEnum;
import com.iboot.pf.module.mes.enums.MesSkipPolicyEnum;
import com.iboot.pf.module.mes.enums.MesSkipRecordStatusEnum;
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
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_SKIP_APPROVER_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_SKIP_APPROVE_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_SKIP_NOT_ALLOWED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_SKIP_RECORD_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;

/**
 * MES-工序跳过记录 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class OpSkipRecordServiceImpl implements OpSkipRecordService {

    @Resource
    private OpSkipRecordMapper opSkipRecordMapper;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private MesSnapshotService snapshotService;
    @Resource
    private MesRelationFiller relationFiller;

    @Override
    public Long createOpSkipRecord(OpSkipRecordSaveReqVO createReqVO) {
        OpSkipRecordDO record = BeanUtils.toBean(createReqVO, OpSkipRecordDO.class);
        record.setId(null);
        prepare(record);
        opSkipRecordMapper.insert(record);
        return record.getId();
    }

    @Override
    public void updateOpSkipRecord(OpSkipRecordSaveReqVO updateReqVO) {
        OpSkipRecordDO existed = validateOpSkipRecordExists(updateReqVO.getId());
        OpSkipRecordDO updateObj = BeanUtils.toBean(updateReqVO, OpSkipRecordDO.class);
        // 已生效的跳过记录已经把工序置为已跳过，只允许补充影响说明与备注
        if (MesSkipRecordStatusEnum.isEffective(existed.getStatus())) {
            existed.setEffectRemark(updateObj.getEffectRemark());
            existed.setRemark(updateObj.getRemark());
            opSkipRecordMapper.updateById(existed);
            return;
        }
        prepare(updateObj);
        opSkipRecordMapper.updateById(updateObj);
    }

    @Override
    public void deleteOpSkipRecord(Long id) {
        validateOpSkipRecordExists(id);
        opSkipRecordMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOpSkipRecordListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            validateOpSkipRecordExists(id);
        }
        opSkipRecordMapper.deleteByIds(ids);
    }

    private OpSkipRecordDO validateOpSkipRecordExists(Long id) {
        OpSkipRecordDO record = id == null ? null : opSkipRecordMapper.selectById(id);
        if (record == null) {
            throw exception(OP_SKIP_RECORD_NOT_EXISTS);
        }
        return record;
    }

    /**
     * 落库前的公共处理：按工序任务的跳序策略校验，补默认值。
     *
     * <p>策略是 NOT_ALLOWED 直接拒绝；APPROVAL_REQUIRED 时未批准不允许生效；
     * 审批结论已给出时审批人必填，否则事后查不到是谁放的行。
     */
    private void prepare(OpSkipRecordDO record) {
        OpTaskDO opTask = opTaskMapper.selectById(record.getOpTaskId());
        if (opTask == null) {
            throw exception(OP_TASK_NOT_EXISTS);
        }
        if (MesSkipPolicyEnum.isNotAllowed(opTask.getSkipPolicy())) {
            throw exception(OP_SKIP_NOT_ALLOWED);
        }
        snapshotService.validateUser(record.getOperatorId());
        record.setApprovedBy(record.getApprovedBy() == null ? MesConstants.NONE_ID : record.getApprovedBy());
        snapshotService.validateUser(record.getApprovedBy());
        if (MesSkipApproveStatusEnum.isDecided(record.getApproveStatus())
                && MesConstants.NONE_ID.equals(record.getApprovedBy())) {
            throw exception(OP_SKIP_APPROVER_REQUIRED);
        }
        if (MesSkipRecordStatusEnum.isEffective(record.getStatus())
                && MesSkipPolicyEnum.isApprovalRequired(opTask.getSkipPolicy())
                && !MesSkipApproveStatusEnum.isApproved(record.getApproveStatus())) {
            throw exception(OP_SKIP_APPROVE_REQUIRED);
        }
        if (record.getSkipTime() == null) {
            record.setSkipTime(LocalDateTime.now());
        }
    }

    @Override
    public OpSkipRecordRespVO getOpSkipRecord(Long id) {
        OpSkipRecordRespVO respVO = opSkipRecordMapper.selectJoinById(id);
        fillRelationNames(respVO == null ? Collections.emptyList() : Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public PageResult<OpSkipRecordRespVO> getOpSkipRecordPage(OpSkipRecordPageReqVO pageReqVO) {
        PageResult<OpSkipRecordRespVO> pageResult = opSkipRecordMapper.selectPage(pageReqVO);
        fillRelationNames(pageResult.getList());
        return pageResult;
    }

    /** 跨模块可读字段补全，放在 Service 以便 Excel 导出复用。 */
    private void fillRelationNames(List<OpSkipRecordRespVO> list) {
        relationFiller.fillUserNickname(list, OpSkipRecordRespVO::getOperatorId,
                OpSkipRecordRespVO::setOperatorName);
        relationFiller.fillUserNickname(list, OpSkipRecordRespVO::getApprovedBy,
                OpSkipRecordRespVO::setApprovedByName);
    }

}

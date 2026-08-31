package com.iboot.pf.module.mes.service.dispatchtask;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.DispatchTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.DispatchTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.DispatchTaskSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.dispatchtask.DispatchTaskDO;
import com.iboot.pf.module.mes.dal.mysql.dispatchtask.DispatchTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesDispatchTaskStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.snapshot.MesRelationFiller;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPATCH_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPATCH_TASK_NO_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPATCH_TASK_OPERATOR_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPATCH_TASK_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPATCH_TASK_TIME_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;

/**
 * MES-派工任务 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class DispatchTaskServiceImpl implements DispatchTaskService {

    @Resource
    private DispatchTaskMapper dispatchTaskMapper;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private MesCodeService codeService;
    @Resource
    private MesSnapshotService snapshotService;
    @Resource
    private MesRelationFiller relationFiller;

    @Override
    public Long createDispatchTask(DispatchTaskSaveReqVO createReqVO) {
        DispatchTaskDO dispatchTask = BeanUtils.toBean(createReqVO, DispatchTaskDO.class);
        dispatchTask.setId(null);
        dispatchTask.setDispatchNo(codeService.generate(MesCodeRuleConstants.DISPATCH_TASK,
                dispatchTask.getDispatchNo()));
        prepare(dispatchTask);
        validateDispatchNoUnique(dispatchTask.getDispatchNo(), null);
        dispatchTaskMapper.insert(dispatchTask);
        return dispatchTask.getId();
    }

    @Override
    public void updateDispatchTask(DispatchTaskSaveReqVO updateReqVO) {
        DispatchTaskDO existed = validateDispatchTaskExists(updateReqVO.getId());
        // 已接单的派工不允许改派人员与资源，现场已经按它开工了
        if (!MesDispatchTaskStatusEnum.isPending(existed.getStatus())) {
            throw exception(DISPATCH_TASK_STATUS_INVALID);
        }
        DispatchTaskDO updateObj = BeanUtils.toBean(updateReqVO, DispatchTaskDO.class);
        updateObj.setDispatchNo(existed.getDispatchNo());
        prepare(updateObj);
        dispatchTaskMapper.updateById(updateObj);
    }

    @Override
    public void deleteDispatchTask(Long id) {
        DispatchTaskDO existed = validateDispatchTaskExists(id);
        if (!MesDispatchTaskStatusEnum.isPending(existed.getStatus())) {
            throw exception(DISPATCH_TASK_STATUS_INVALID);
        }
        dispatchTaskMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDispatchTaskListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            DispatchTaskDO existed = validateDispatchTaskExists(id);
            if (!MesDispatchTaskStatusEnum.isPending(existed.getStatus())) {
                throw exception(DISPATCH_TASK_STATUS_INVALID);
            }
        }
        dispatchTaskMapper.deleteByIds(ids);
    }

    private DispatchTaskDO validateDispatchTaskExists(Long id) {
        DispatchTaskDO dispatchTask = id == null ? null : dispatchTaskMapper.selectById(id);
        if (dispatchTask == null) {
            throw exception(DISPATCH_TASK_NOT_EXISTS);
        }
        return dispatchTask;
    }

    /** 落库前的公共处理：校验工序任务与人员班组资源，可选外键归零，校验计划时间。 */
    private void prepare(DispatchTaskDO dispatchTask) {
        if (opTaskMapper.selectById(dispatchTask.getOpTaskId()) == null) {
            throw exception(OP_TASK_NOT_EXISTS);
        }
        dispatchTask.setOperatorId(dispatchTask.getOperatorId() == null
                ? MesConstants.NONE_ID : dispatchTask.getOperatorId());
        dispatchTask.setTeamId(dispatchTask.getTeamId() == null
                ? MesConstants.NONE_ID : dispatchTask.getTeamId());
        dispatchTask.setShiftId(dispatchTask.getShiftId() == null
                ? MesConstants.NONE_ID : dispatchTask.getShiftId());
        // 人员与班组至少填一个，否则这条派工没有接收方
        if (MesConstants.NONE_ID.equals(dispatchTask.getOperatorId())
                && MesConstants.NONE_ID.equals(dispatchTask.getTeamId())) {
            throw exception(DISPATCH_TASK_OPERATOR_REQUIRED);
        }
        snapshotService.validateUser(dispatchTask.getOperatorId());
        snapshotService.validateTeam(dispatchTask.getTeamId());
        snapshotService.validateRes(dispatchTask.getResId());
        snapshotService.validateShift(dispatchTask.getShiftId());
        validatePlannedTime(dispatchTask);
    }

    /** 计划结束时间不能早于计划开始时间。 */
    private void validatePlannedTime(DispatchTaskDO dispatchTask) {
        if (dispatchTask.getPlannedStartTime() == null || dispatchTask.getPlannedEndTime() == null) {
            return;
        }
        if (dispatchTask.getPlannedEndTime().isBefore(dispatchTask.getPlannedStartTime())) {
            throw exception(DISPATCH_TASK_TIME_INVALID);
        }
    }

    /** 派工单号唯一：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。 */
    private void validateDispatchNoUnique(String dispatchNo, Long excludeId) {
        DispatchTaskDO existed = dispatchTaskMapper.selectByDispatchNo(dispatchNo);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(DISPATCH_TASK_NO_DUPLICATE);
    }

    @Override
    public DispatchTaskRespVO getDispatchTask(Long id) {
        DispatchTaskRespVO respVO = dispatchTaskMapper.selectJoinById(id);
        fillRelationNames(respVO == null ? Collections.emptyList() : Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public PageResult<DispatchTaskRespVO> getDispatchTaskPage(DispatchTaskPageReqVO pageReqVO) {
        PageResult<DispatchTaskRespVO> pageResult = dispatchTaskMapper.selectPage(pageReqVO);
        fillRelationNames(pageResult.getList());
        return pageResult;
    }

    /** 跨模块可读字段补全，放在 Service 以便 Excel 导出复用。 */
    private void fillRelationNames(List<DispatchTaskRespVO> list) {
        relationFiller.fillUserNickname(list, DispatchTaskRespVO::getOperatorId,
                DispatchTaskRespVO::setOperatorName);
        relationFiller.fillTeamName(list, DispatchTaskRespVO::getTeamId, DispatchTaskRespVO::setTeamName);
        relationFiller.fillResName(list, DispatchTaskRespVO::getResId, DispatchTaskRespVO::setResName);
        relationFiller.fillShiftName(list, DispatchTaskRespVO::getShiftId, DispatchTaskRespVO::setShiftName);
    }

}

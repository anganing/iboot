package com.iboot.pf.module.mes.service.rgvtask;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.RgvTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.RgvTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.RgvTaskSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.rgvtask.RgvTaskDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.rgvtask.RgvTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesRgvDispatchStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.snapshot.MesRelationFiller;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.IDEMPOTENCY_KEY_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.RGV_TASK_EXTERNAL_NO_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.RGV_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.RGV_TASK_NO_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.RGV_TASK_SAME_STATION;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.RGV_TASK_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_NOT_EXISTS;

/**
 * MES-RGV 工位流转任务 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class RgvTaskServiceImpl implements RgvTaskService {

    @Resource
    private RgvTaskMapper rgvTaskMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private MesCodeService codeService;
    @Resource
    private MesSnapshotService snapshotService;
    @Resource
    private MesRelationFiller relationFiller;

    @Override
    public Long createRgvTask(RgvTaskSaveReqVO createReqVO) {
        RgvTaskDO rgvTask = BeanUtils.toBean(createReqVO, RgvTaskDO.class);
        rgvTask.setId(null);
        rgvTask.setTaskNo(codeService.generate(MesCodeRuleConstants.RGV_TASK, rgvTask.getTaskNo()));
        prepare(rgvTask);
        rgvTask.setVersion(MesConstants.INIT_VERSION);
        validateTaskNoUnique(rgvTask.getTaskNo(), null);
        validateExternalTaskNoUnique(rgvTask, null);
        validateIdempotencyKeyUnique(rgvTask.getIdempotencyKey(), null);
        rgvTaskMapper.insert(rgvTask);
        return rgvTask.getId();
    }

    @Override
    public void updateRgvTask(RgvTaskSaveReqVO updateReqVO) {
        RgvTaskDO existed = validateRgvTaskExists(updateReqVO.getId());
        // 已下发的任务不允许改搬运起终点，否则与外部调度系统不一致
        if (MesRgvDispatchStatusEnum.isSent(existed.getDispatchStatus())
                && (!Objects.equals(existed.getFromStationId(), updateReqVO.getFromStationId())
                || !Objects.equals(existed.getToStationId(), updateReqVO.getToStationId()))) {
            throw exception(RGV_TASK_STATUS_INVALID);
        }
        RgvTaskDO updateObj = BeanUtils.toBean(updateReqVO, RgvTaskDO.class);
        updateObj.setTaskNo(existed.getTaskNo());
        // 完成时间由外部调度系统回报，乐观锁版本不由表单维护
        updateObj.setCompletedTime(existed.getCompletedTime());
        updateObj.setVersion(existed.getVersion());
        prepare(updateObj);
        validateExternalTaskNoUnique(updateObj, updateReqVO.getId());
        validateIdempotencyKeyUnique(updateObj.getIdempotencyKey(), updateReqVO.getId());
        rgvTaskMapper.updateById(updateObj);
    }

    @Override
    public void deleteRgvTask(Long id) {
        RgvTaskDO existed = validateRgvTaskExists(id);
        // 已下发的任务外部系统已经在执行，不允许删除
        if (MesRgvDispatchStatusEnum.isSent(existed.getDispatchStatus())) {
            throw exception(RGV_TASK_STATUS_INVALID);
        }
        rgvTaskMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRgvTaskListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            RgvTaskDO existed = validateRgvTaskExists(id);
            if (MesRgvDispatchStatusEnum.isSent(existed.getDispatchStatus())) {
                throw exception(RGV_TASK_STATUS_INVALID);
            }
        }
        rgvTaskMapper.deleteByIds(ids);
    }

    private RgvTaskDO validateRgvTaskExists(Long id) {
        RgvTaskDO rgvTask = id == null ? null : rgvTaskMapper.selectById(id);
        if (rgvTask == null) {
            throw exception(RGV_TASK_NOT_EXISTS);
        }
        return rgvTask;
    }

    /** 落库前的公共处理：校验工单与工序任务、工位资源，可选外键归零，起终点不能相同。 */
    private void prepare(RgvTaskDO rgvTask) {
        if (workOrderMapper.selectById(rgvTask.getWorkOrderId()) == null) {
            throw exception(WORK_ORDER_NOT_EXISTS);
        }
        if (opTaskMapper.selectById(rgvTask.getOpTaskId()) == null) {
            throw exception(OP_TASK_NOT_EXISTS);
        }
        rgvTask.setTargetOpTaskId(rgvTask.getTargetOpTaskId() == null
                ? MesConstants.NONE_ID : rgvTask.getTargetOpTaskId());
        if (!MesConstants.NONE_ID.equals(rgvTask.getTargetOpTaskId())
                && opTaskMapper.selectById(rgvTask.getTargetOpTaskId()) == null) {
            throw exception(OP_TASK_NOT_EXISTS);
        }
        snapshotService.validateRes(rgvTask.getFromStationId());
        snapshotService.validateRes(rgvTask.getToStationId());
        if (Objects.equals(rgvTask.getFromStationId(), rgvTask.getToStationId())) {
            throw exception(RGV_TASK_SAME_STATION);
        }
    }

    /** 本地任务号唯一。 */
    private void validateTaskNoUnique(String taskNo, Long excludeId) {
        RgvTaskDO existed = rgvTaskMapper.selectByTaskNo(taskNo);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(RGV_TASK_NO_DUPLICATE);
    }

    /** 同一外部系统下外部任务号唯一，对应唯一索引 (external_system_code, external_task_no, deleted)。 */
    private void validateExternalTaskNoUnique(RgvTaskDO rgvTask, Long excludeId) {
        if (rgvTask.getExternalTaskNo() == null || rgvTask.getExternalTaskNo().isBlank()) {
            return;
        }
        RgvTaskDO existed = rgvTaskMapper.selectByExternalTaskNo(rgvTask.getExternalSystemCode(),
                rgvTask.getExternalTaskNo());
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(RGV_TASK_EXTERNAL_NO_DUPLICATE);
    }

    /** 幂等键唯一。 */
    private void validateIdempotencyKeyUnique(String idempotencyKey, Long excludeId) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }
        RgvTaskDO existed = rgvTaskMapper.selectByIdempotencyKey(idempotencyKey);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(IDEMPOTENCY_KEY_DUPLICATE);
    }

    @Override
    public RgvTaskRespVO getRgvTask(Long id) {
        RgvTaskRespVO respVO = rgvTaskMapper.selectJoinById(id);
        fillRelationNames(respVO == null ? Collections.emptyList() : Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public PageResult<RgvTaskRespVO> getRgvTaskPage(RgvTaskPageReqVO pageReqVO) {
        PageResult<RgvTaskRespVO> pageResult = rgvTaskMapper.selectPage(pageReqVO);
        fillRelationNames(pageResult.getList());
        return pageResult;
    }

    /** 工位落在 mdm 的生产资源上，跨模块只能走门面批量补全。 */
    private void fillRelationNames(List<RgvTaskRespVO> list) {
        relationFiller.fillResName(list, RgvTaskRespVO::getFromStationId, RgvTaskRespVO::setFromStationName);
        relationFiller.fillResName(list, RgvTaskRespVO::getToStationId, RgvTaskRespVO::setToStationName);
    }

}

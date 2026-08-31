package com.iboot.pf.module.mes.service.rework;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mes.controller.admin.rework.vo.ReworkPageReqVO;
import com.iboot.pf.module.mes.controller.admin.rework.vo.ReworkRespVO;
import com.iboot.pf.module.mes.controller.admin.rework.vo.ReworkSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.rework.ReworkDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.rework.ReworkMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesReworkStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.REWORK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.REWORK_NO_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.REWORK_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_NOT_EXISTS;

/**
 * MES-返工任务 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ReworkServiceImpl implements ReworkService {

    @Resource
    private ReworkMapper reworkMapper;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private MesCodeService codeService;
    @Resource
    private MesSnapshotService snapshotService;

    @Override
    public Long createRework(ReworkSaveReqVO createReqVO) {
        ReworkDO rework = BeanUtils.toBean(createReqVO, ReworkDO.class);
        rework.setId(null);
        rework.setReworkNo(codeService.generate(MesCodeRuleConstants.REWORK, rework.getReworkNo()));
        prepare(rework);
        validateReworkNoUnique(rework.getReworkNo(), null);
        reworkMapper.insert(rework);
        return rework.getId();
    }

    @Override
    public void updateRework(ReworkSaveReqVO updateReqVO) {
        ReworkDO existed = validateReworkExists(updateReqVO.getId());
        // 只有新建状态允许改，已下达的返工任务已经在车间执行
        if (!MesReworkStatusEnum.isDraft(existed.getStatus())) {
            throw exception(REWORK_STATUS_INVALID);
        }
        ReworkDO updateObj = BeanUtils.toBean(updateReqVO, ReworkDO.class);
        updateObj.setReworkNo(existed.getReworkNo());
        prepare(updateObj);
        reworkMapper.updateById(updateObj);
    }

    @Override
    public void deleteRework(Long id) {
        ReworkDO existed = validateReworkExists(id);
        if (!MesReworkStatusEnum.isDraft(existed.getStatus())) {
            throw exception(REWORK_STATUS_INVALID);
        }
        reworkMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReworkListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            ReworkDO existed = validateReworkExists(id);
            if (!MesReworkStatusEnum.isDraft(existed.getStatus())) {
                throw exception(REWORK_STATUS_INVALID);
            }
        }
        reworkMapper.deleteByIds(ids);
    }

    private ReworkDO validateReworkExists(Long id) {
        ReworkDO rework = id == null ? null : reworkMapper.selectById(id);
        if (rework == null) {
            throw exception(REWORK_NOT_EXISTS);
        }
        return rework;
    }

    /** 落库前的公共处理：反查单据链、锁定返工路线版本、可选外键归零。 */
    private void prepare(ReworkDO rework) {
        OpTaskDO opTask = opTaskMapper.selectById(rework.getOpTaskId());
        if (opTask == null) {
            throw exception(OP_TASK_NOT_EXISTS);
        }
        WorkOrderDO workOrder = workOrderMapper.selectById(opTask.getWorkOrderId());
        if (workOrder == null) {
            throw exception(WORK_ORDER_NOT_EXISTS);
        }
        rework.setWorkOrderId(workOrder.getId());
        rework.setProdOrderId(workOrder.getProdOrderId());
        rework.setRouteVersion(snapshotService.requireRouteVersion(rework.getRouteId()));
        rework.setSourceQcTaskId(rework.getSourceQcTaskId() == null
                ? MesConstants.NONE_ID : rework.getSourceQcTaskId());
    }

    /** 返工任务编号唯一：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。 */
    private void validateReworkNoUnique(String reworkNo, Long excludeId) {
        ReworkDO existed = reworkMapper.selectByReworkNo(reworkNo);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(REWORK_NO_DUPLICATE);
    }

    @Override
    public ReworkRespVO getRework(Long id) {
        return reworkMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ReworkRespVO> getReworkPage(ReworkPageReqVO pageReqVO) {
        return reworkMapper.selectPage(pageReqVO);
    }

}

package com.iboot.pf.module.aps.service.taskdep;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.aps.controller.admin.taskdep.vo.TaskDepPageReqVO;
import com.iboot.pf.module.aps.controller.admin.taskdep.vo.TaskDepRespVO;
import com.iboot.pf.module.aps.controller.admin.taskdep.vo.TaskDepSaveReqVO;
import com.iboot.pf.module.aps.dal.dataobject.task.ApsTaskDO;
import com.iboot.pf.module.aps.dal.dataobject.taskdep.ApsTaskDepDO;
import com.iboot.pf.module.aps.dal.mysql.taskdep.ApsTaskDepMapper;
import com.iboot.pf.module.aps.service.task.ApsTaskService;
import com.iboot.pf.module.aps.service.version.ApsVersionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_DEP_DUPLICATE;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_DEP_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_DEP_SELF_REFERENCE;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_DEP_VERSION_MISMATCH;

/**
 * APS-排程任务依赖 Service 实现类。
 *
 * <p>依赖的版本编号由前置任务反推，不接受前端提交：跨版本连边会让求解器读到不属于本次排程的任务。
 *
 * @author iboot
 */
@Service
@Validated
public class ApsTaskDepServiceImpl implements ApsTaskDepService {

    @Resource
    private ApsTaskDepMapper taskDepMapper;
    @Resource
    private ApsTaskService taskService;
    @Resource
    private ApsVersionService versionService;

    @Override
    public Long createTaskDep(TaskDepSaveReqVO createReqVO) {
        ApsTaskDepDO taskDep = BeanUtils.toBean(createReqVO, ApsTaskDepDO.class);
        taskDep.setId(null);
        taskDep.setVersionId(validateTasks(createReqVO.getFromTaskId(), createReqVO.getToTaskId()));
        versionService.validateMutable(taskDep.getVersionId());
        validateTaskDepUnique(taskDep, null);
        taskDepMapper.insert(taskDep);
        return taskDep.getId();
    }

    @Override
    public void updateTaskDep(TaskDepSaveReqVO updateReqVO) {
        validateTaskDepExists(updateReqVO.getId());
        ApsTaskDepDO updateObj = BeanUtils.toBean(updateReqVO, ApsTaskDepDO.class);
        updateObj.setVersionId(validateTasks(updateReqVO.getFromTaskId(), updateReqVO.getToTaskId()));
        versionService.validateMutable(updateObj.getVersionId());
        validateTaskDepUnique(updateObj, updateReqVO.getId());
        taskDepMapper.updateById(updateObj);
    }

    @Override
    public void deleteTaskDep(Long id) {
        ApsTaskDepDO taskDep = validateTaskDepExists(id);
        versionService.validateMutable(taskDep.getVersionId());
        taskDepMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTaskDepListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            ApsTaskDepDO taskDep = validateTaskDepExists(id);
            versionService.validateMutable(taskDep.getVersionId());
        }
        taskDepMapper.deleteByIds(ids);
    }

    @Override
    public TaskDepRespVO getTaskDep(Long id) {
        return taskDepMapper.selectJoinById(id);
    }

    @Override
    public PageResult<TaskDepRespVO> getTaskDepPage(TaskDepPageReqVO pageReqVO) {
        return taskDepMapper.selectPage(pageReqVO);
    }

    private ApsTaskDepDO validateTaskDepExists(Long id) {
        ApsTaskDepDO taskDep = id == null ? null : taskDepMapper.selectById(id);
        if (taskDep == null) {
            throw exception(TASK_DEP_NOT_EXISTS);
        }
        return taskDep;
    }

    /**
     * 校验前后任务，并返回依赖所属的版本编号。
     *
     * <p>自依赖会让求解器陷入死循环；跨版本依赖会把别的排程结果拉进来，两者都直接拒绝。
     */
    private Long validateTasks(Long fromTaskId, Long toTaskId) {
        if (fromTaskId.equals(toTaskId)) {
            throw exception(TASK_DEP_SELF_REFERENCE);
        }
        ApsTaskDO fromTask = taskService.validateTaskExists(fromTaskId);
        ApsTaskDO toTask = taskService.validateTaskExists(toTaskId);
        if (!fromTask.getVersionId().equals(toTask.getVersionId())) {
            throw exception(TASK_DEP_VERSION_MISMATCH);
        }
        return fromTask.getVersionId();
    }

    /**
     * 依赖唯一：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。
     */
    private void validateTaskDepUnique(ApsTaskDepDO taskDep, Long excludeId) {
        ApsTaskDepDO existed = taskDepMapper.selectByTasksAndType(taskDep.getVersionId(), taskDep.getFromTaskId(),
                taskDep.getToTaskId(), taskDep.getDepType());
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(TASK_DEP_DUPLICATE);
    }

}

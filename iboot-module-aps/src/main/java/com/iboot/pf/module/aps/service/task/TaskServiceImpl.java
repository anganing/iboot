package com.iboot.pf.module.aps.service.task;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.aps.controller.admin.task.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.task.TaskDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.aps.dal.mysql.task.TaskMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.*;

/**
 * APS-工序排程任务 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class TaskServiceImpl implements TaskService {

    @Resource
    private TaskMapper taskMapper;

    @Override
    public Long createTask(TaskSaveReqVO createReqVO) {
        TaskDO task = BeanUtils.toBean(createReqVO, TaskDO.class);
        validateTaskSchedule(task);
        taskMapper.insert(task);
        return task.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public TaskImportRespVO importTaskList(List<TaskImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return TaskImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        TaskImportRespVO respVO = TaskImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createTask(BeanUtils.toBean(importItem, TaskSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateTask(TaskSaveReqVO updateReqVO) {
        validateTaskExists(updateReqVO.getId());
        TaskDO updateObj = BeanUtils.toBean(updateReqVO, TaskDO.class);
        validateTaskSchedule(updateObj);
        taskMapper.updateById(updateObj);
    }

    @Override
    public void deleteTask(Long id) {
        // 校验存在
        validateTaskExists(id);
        // 删除
        taskMapper.deleteById(id);
    }

    @Override
        public void deleteTaskListByIds(List<Long> ids) {
        // 删除
        taskMapper.deleteByIds(ids);
        }


    private void validateTaskExists(Long id) {
        if (taskMapper.selectById(id) == null) {
            throw exception(TASK_NOT_EXISTS);
        }
    }

    /** 校验排程任务的时间区间及同资源占用，避免错误计划直接进入排程版本。 */
    private void validateTaskSchedule(TaskDO task) {
        if (!task.getStartTime().isBefore(task.getEndTime())) {
            throw exception(TASK_TIME_INVALID);
        }
        Long overlapCount = taskMapper.selectCount(new LambdaQueryWrapper<TaskDO>()
                .eq(TaskDO::getResId, task.getResId())
                .ne(task.getId() != null, TaskDO::getId, task.getId())
                .lt(TaskDO::getStartTime, task.getEndTime())
                .gt(TaskDO::getEndTime, task.getStartTime()));
        if (overlapCount > 0) {
            throw exception(TASK_RESOURCE_OVERLAP);
        }
    }

    @Override
    public TaskRespVO getTask(Long id) {
        return taskMapper.selectJoinById(id);
    }

    @Override
    public PageResult<TaskRespVO> getTaskPage(TaskPageReqVO pageReqVO) {
        return taskMapper.selectPage(pageReqVO);
    }

}

package com.iboot.pf.module.aps.service.tasklock;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.aps.controller.admin.tasklock.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.tasklock.TaskLockDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.aps.dal.mysql.tasklock.TaskLockMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.*;

/**
 * 任务锁定 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class TaskLockServiceImpl implements TaskLockService {

    @Resource
    private TaskLockMapper taskLockMapper;

    @Override
    public Long createTaskLock(TaskLockSaveReqVO createReqVO) {
        // 插入
        TaskLockDO taskLock = BeanUtils.toBean(createReqVO, TaskLockDO.class);
        taskLockMapper.insert(taskLock);

        // 返回
        return taskLock.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public TaskLockImportRespVO importTaskLockList(List<TaskLockImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return TaskLockImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        TaskLockImportRespVO respVO = TaskLockImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createTaskLock(BeanUtils.toBean(importItem, TaskLockSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateTaskLock(TaskLockSaveReqVO updateReqVO) {
        // 校验存在
        validateTaskLockExists(updateReqVO.getId());
        // 更新
        TaskLockDO updateObj = BeanUtils.toBean(updateReqVO, TaskLockDO.class);
        taskLockMapper.updateById(updateObj);
    }

    @Override
    public void deleteTaskLock(Long id) {
        // 校验存在
        validateTaskLockExists(id);
        // 删除
        taskLockMapper.deleteById(id);
    }

    @Override
        public void deleteTaskLockListByIds(List<Long> ids) {
        // 删除
        taskLockMapper.deleteByIds(ids);
        }


    private void validateTaskLockExists(Long id) {
        if (taskLockMapper.selectById(id) == null) {
            throw exception(TASK_LOCK_NOT_EXISTS);
        }
    }

    @Override
    public TaskLockRespVO getTaskLock(Long id) {
        return taskLockMapper.selectJoinById(id);
    }

    @Override
    public PageResult<TaskLockRespVO> getTaskLockPage(TaskLockPageReqVO pageReqVO) {
        return taskLockMapper.selectPage(pageReqVO);
    }

}
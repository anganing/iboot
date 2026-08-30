package com.iboot.pf.module.aps.service.taskdep;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.aps.controller.admin.taskdep.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.taskdep.TaskDepDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.aps.dal.mysql.taskdep.TaskDepMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.*;

/**
 * 任务依赖 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class TaskDepServiceImpl implements TaskDepService {

    @Resource
    private TaskDepMapper taskDepMapper;

    @Override
    public Long createTaskDep(TaskDepSaveReqVO createReqVO) {
        // 插入
        TaskDepDO taskDep = BeanUtils.toBean(createReqVO, TaskDepDO.class);
        taskDepMapper.insert(taskDep);

        // 返回
        return taskDep.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public TaskDepImportRespVO importTaskDepList(List<TaskDepImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return TaskDepImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        TaskDepImportRespVO respVO = TaskDepImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createTaskDep(BeanUtils.toBean(importItem, TaskDepSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateTaskDep(TaskDepSaveReqVO updateReqVO) {
        // 校验存在
        validateTaskDepExists(updateReqVO.getId());
        // 更新
        TaskDepDO updateObj = BeanUtils.toBean(updateReqVO, TaskDepDO.class);
        taskDepMapper.updateById(updateObj);
    }

    @Override
    public void deleteTaskDep(Long id) {
        // 校验存在
        validateTaskDepExists(id);
        // 删除
        taskDepMapper.deleteById(id);
    }

    @Override
        public void deleteTaskDepListByIds(List<Long> ids) {
        // 删除
        taskDepMapper.deleteByIds(ids);
        }


    private void validateTaskDepExists(Long id) {
        if (taskDepMapper.selectById(id) == null) {
            throw exception(TASK_DEP_NOT_EXISTS);
        }
    }

    @Override
    public TaskDepRespVO getTaskDep(Long id) {
        return taskDepMapper.selectJoinById(id);
    }

    @Override
    public PageResult<TaskDepRespVO> getTaskDepPage(TaskDepPageReqVO pageReqVO) {
        return taskDepMapper.selectPage(pageReqVO);
    }

}
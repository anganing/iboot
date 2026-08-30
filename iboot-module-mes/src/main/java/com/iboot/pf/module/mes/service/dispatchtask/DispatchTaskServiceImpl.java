package com.iboot.pf.module.mes.service.dispatchtask;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.dispatchtask.DispatchTaskDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mes.dal.mysql.dispatchtask.DispatchTaskMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;

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

    @Override
    public Long createDispatchTask(DispatchTaskSaveReqVO createReqVO) {
        // 插入
        DispatchTaskDO dispatchTask = BeanUtils.toBean(createReqVO, DispatchTaskDO.class);
        dispatchTaskMapper.insert(dispatchTask);

        // 返回
        return dispatchTask.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public DispatchTaskImportRespVO importDispatchTaskList(List<DispatchTaskImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return DispatchTaskImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        DispatchTaskImportRespVO respVO = DispatchTaskImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createDispatchTask(BeanUtils.toBean(importItem, DispatchTaskSaveReqVO.class));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateDispatchTask(DispatchTaskSaveReqVO updateReqVO) {
        // 校验存在
        validateDispatchTaskExists(updateReqVO.getId());
        // 更新
        DispatchTaskDO updateObj = BeanUtils.toBean(updateReqVO, DispatchTaskDO.class);
        dispatchTaskMapper.updateById(updateObj);
    }

    @Override
    public void deleteDispatchTask(Long id) {
        // 校验存在
        validateDispatchTaskExists(id);
        // 删除
        dispatchTaskMapper.deleteById(id);
    }

    @Override
        public void deleteDispatchTaskListByIds(List<Long> ids) {
        // 删除
        dispatchTaskMapper.deleteByIds(ids);
        }


    private void validateDispatchTaskExists(Long id) {
        if (dispatchTaskMapper.selectById(id) == null) {
            throw exception(DISPATCH_TASK_NOT_EXISTS);
        }
    }

    @Override
    public DispatchTaskRespVO getDispatchTask(Long id) {
        return dispatchTaskMapper.selectJoinById(id);
    }

    @Override
    public PageResult<DispatchTaskRespVO> getDispatchTaskPage(DispatchTaskPageReqVO pageReqVO) {
        return dispatchTaskMapper.selectPage(pageReqVO);
    }

}
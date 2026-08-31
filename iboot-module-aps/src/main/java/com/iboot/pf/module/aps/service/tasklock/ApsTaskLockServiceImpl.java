package com.iboot.pf.module.aps.service.tasklock;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.aps.controller.admin.tasklock.vo.TaskLockPageReqVO;
import com.iboot.pf.module.aps.controller.admin.tasklock.vo.TaskLockRespVO;
import com.iboot.pf.module.aps.dal.mysql.tasklock.ApsTaskLockMapper;
import com.iboot.pf.module.aps.service.snapshot.ApsRelationFiller;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.TASK_LOCK_NOT_EXISTS;

/**
 * APS-排程任务锁定记录 Service 实现类。只读。
 *
 * @author iboot
 */
@Service
@Validated
public class ApsTaskLockServiceImpl implements ApsTaskLockService {

    @Resource
    private ApsTaskLockMapper taskLockMapper;
    @Resource
    private ApsRelationFiller relationFiller;

    @Override
    public TaskLockRespVO getTaskLock(Long id) {
        TaskLockRespVO respVO = id == null ? null : taskLockMapper.selectJoinById(id);
        if (respVO == null) {
            throw exception(TASK_LOCK_NOT_EXISTS);
        }
        fillRelationNames(Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public PageResult<TaskLockRespVO> getTaskLockPage(TaskLockPageReqVO pageReqVO) {
        PageResult<TaskLockRespVO> pageResult = taskLockMapper.selectPage(pageReqVO);
        fillRelationNames(pageResult.getList());
        return pageResult;
    }

    /**
     * 跨模块可读字段补全。
     *
     * <p>放在 Service 而非 Controller：Excel 导出复用这里的分页结果，写在 Controller 会丢字段。
     */
    private void fillRelationNames(List<TaskLockRespVO> list) {
        relationFiller.fillUserNickname(list, TaskLockRespVO::getLockedBy, TaskLockRespVO::setLockedByName);
    }

}

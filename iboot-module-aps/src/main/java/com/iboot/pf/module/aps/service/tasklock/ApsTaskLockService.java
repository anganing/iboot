package com.iboot.pf.module.aps.service.tasklock;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.aps.controller.admin.tasklock.vo.TaskLockPageReqVO;
import com.iboot.pf.module.aps.controller.admin.tasklock.vo.TaskLockRespVO;

/**
 * APS-排程任务锁定记录 Service 接口。
 *
 * <p>锁定记录是锁定/解锁动作的留痕表：只提供查询与导出，写入路径只有
 * {@code ApsTaskService#lockTask} 与 {@code #unlockTask}。放开人工写入等于允许伪造锁定原因与责任人。
 *
 * @author iboot
 */
public interface ApsTaskLockService {

    /**
     * 获得排程任务锁定记录
     *
     * @param id 编号
     * @return 锁定记录
     */
    TaskLockRespVO getTaskLock(Long id);

    /**
     * 获得排程任务锁定记录分页
     *
     * @param pageReqVO 分页查询
     * @return 锁定记录分页
     */
    PageResult<TaskLockRespVO> getTaskLockPage(TaskLockPageReqVO pageReqVO);

}

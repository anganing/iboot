package com.iboot.pf.module.aps.api.task;
import com.iboot.pf.framework.common.util.object.BeanUtils; import com.iboot.pf.framework.datapermission.core.annotation.DataPermission; import com.iboot.pf.module.aps.api.task.dto.*; import com.iboot.pf.module.aps.service.task.ApsScheduleTaskService; import jakarta.annotation.Resource; import org.springframework.stereotype.Service; import org.springframework.validation.annotation.Validated; import java.util.*;
/** 排程任务 API 实现。 */ @Service @Validated
public class ScheduleTaskApiImpl implements ScheduleTaskApi { @Resource private ApsScheduleTaskService service;
 @DataPermission(enable=false) public List<ScheduleTaskRespDTO> getTaskListByVersionId(Long id){return BeanUtils.toBean(service.getTaskListByVersionId(id),ScheduleTaskRespDTO.class);}
 @DataPermission(enable=false) public List<ScheduleTaskRespDTO> getTaskListByOrderIds(Collection<Long> ids){return BeanUtils.toBean(service.getTaskListByOrderIds(ids),ScheduleTaskRespDTO.class);}
 public void lockTask(LockTaskReqDTO r){service.lockTask(r);} public void unlockTask(LockTaskReqDTO r){service.unlockTask(r);} }

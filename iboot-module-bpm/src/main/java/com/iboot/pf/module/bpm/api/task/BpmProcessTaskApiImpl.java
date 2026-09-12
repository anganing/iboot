package com.iboot.pf.module.bpm.api.task;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.bpm.service.task.BpmTaskService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 流程任务 Api 实现类
 *
 * <p>类上一个 {@code @DataPermission(enable = false)} 兜住全部方法：门面是给业务模块调的，
 * 推进节点用的是流程配置里指定的办理人身份，与调用方登录用户在 bpm 表上的数据范围毫无关系，
 * 跨模块调用天生是系统身份而不是用户身份。打在类上而不是逐方法加，是为了不出现"加了一半"的漏洞。
 *
 * @author jason
 */
@Service
@Validated
@DataPermission(enable = false)
public class BpmProcessTaskApiImpl implements BpmProcessTaskApi {

    @Resource
    private BpmTaskService bpmTaskService;

    @Override
    public void triggerTask(String processInstanceId, String taskDefineKey) {
        bpmTaskService.triggerTask(processInstanceId, taskDefineKey);
    }

}

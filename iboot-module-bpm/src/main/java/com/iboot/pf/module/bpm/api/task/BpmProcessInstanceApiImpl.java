package com.iboot.pf.module.bpm.api.task;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import com.iboot.pf.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCancelReqVO;
import com.iboot.pf.module.bpm.service.task.BpmProcessInstanceService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Flowable 流程实例 Api 实现类
 *
 * <p>类上一个 {@code @DataPermission(enable = false)} 兜住全部方法：门面是给业务模块调的，
 * 发起与撤销流程用的是单据提交人的身份，与调用方登录用户在 bpm 表上的数据范围毫无关系，
 * 跨模块调用天生是系统身份而不是用户身份。打在类上而不是逐方法加，是为了不出现"加了一半"的漏洞。
 *
 * @author iboot
 * @author jason
 */
@Service
@Validated
@DataPermission(enable = false)
public class BpmProcessInstanceApiImpl implements BpmProcessInstanceApi {

    @Resource
    private BpmProcessInstanceService processInstanceService;

    @Override
    public String createProcessInstance(Long userId, @Valid BpmProcessInstanceCreateReqDTO reqDTO) {
        return processInstanceService.createProcessInstance(userId, reqDTO);
    }

    @Override
    public void cancelProcessInstanceByStartUser(Long userId, String processInstanceId, String reason) {
        processInstanceService.cancelProcessInstanceByStartUser(userId,
                new BpmProcessInstanceCancelReqVO().setId(processInstanceId).setReason(reason));
    }

}

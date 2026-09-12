package com.iboot.pf.module.infra.api.logger;

import com.iboot.pf.framework.common.biz.infra.logger.ApiAccessLogCommonApi;
import com.iboot.pf.framework.common.biz.infra.logger.dto.ApiAccessLogCreateReqDTO;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.infra.service.logger.ApiAccessLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * API 访问日志的 API 实现类
 *
 * <p>类上一个 {@code @DataPermission(enable = false)} 兜住全部方法：它实现的是 framework 定义的
 * {@link ApiAccessLogCommonApi}，由各模块的请求日志切面跨模块投递进来，写哪条日志与投递方登录用户
 * 的数据范围毫无关系，跨模块调用天生是系统身份而不是用户身份。打在类上而不是逐方法加，
 * 是为了不出现"加了一半"的漏洞。
 *
 * @author iboot
 */
@Service
@Validated
@DataPermission(enable = false)
public class ApiAccessLogApiImpl implements ApiAccessLogCommonApi {

    @Resource
    private ApiAccessLogService apiAccessLogService;

    @Override
    public void createApiAccessLog(ApiAccessLogCreateReqDTO createDTO) {
        apiAccessLogService.createApiAccessLog(createDTO);
    }

}

package com.iboot.pf.module.infra.api.config;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.infra.dal.dataobject.config.ConfigDO;
import com.iboot.pf.module.infra.service.config.ConfigService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 参数配置 API 实现类
 *
 * <p>类上一个 {@code @DataPermission(enable = false)} 兜住全部方法：门面是给别的模块调的，
 * 读参数配置与各业务模块登录用户的数据范围毫无关系，跨模块调用天生是系统身份而不是用户身份。
 * 打在类上而不是逐方法加，是为了不出现"加了一半"的漏洞。
 *
 * @author iboot
 */
@Service
@Validated
@DataPermission(enable = false)
public class ConfigApiImpl implements ConfigApi {

    @Resource
    private ConfigService configService;

    @Override
    public String getConfigValueByKey(String key) {
        ConfigDO config = configService.getConfigByKey(key);
        return config != null ? config.getValue() : null;
    }

}

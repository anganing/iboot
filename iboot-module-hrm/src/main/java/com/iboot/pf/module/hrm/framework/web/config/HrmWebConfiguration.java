package com.iboot.pf.module.hrm.framework.web.config;

import com.iboot.pf.framework.swagger.config.IbootSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * hrm 模块的 web 组件的 Configuration
 *
 * @author iboot
 */
@Configuration(proxyBeanMethods = false)
public class HrmWebConfiguration {

    /**
     * hrm 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi hrmGroupedOpenApi() {
        return IbootSwaggerAutoConfiguration.buildGroupedOpenApi("hrm");
    }

}

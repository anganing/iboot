package com.iboot.pf.module.infra.framework.web.config;

import com.iboot.pf.framework.swagger.config.IbootSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * infra 模块的 web 组件的 Configuration
 *
 * @author iboot
 */
@Configuration(proxyBeanMethods = false)
public class InfraWebConfiguration {

    /**
     * infra 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi infraGroupedOpenApi() {
        return IbootSwaggerAutoConfiguration.buildGroupedOpenApi("infra");
    }

}

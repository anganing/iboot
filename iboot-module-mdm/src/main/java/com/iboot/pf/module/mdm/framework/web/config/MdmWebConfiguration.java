package com.iboot.pf.module.mdm.framework.web.config;

import com.iboot.pf.framework.swagger.config.IbootSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** MDM 模块 Web 配置。 */
@Configuration(proxyBeanMethods = false)
public class MdmWebConfiguration {

    @Bean
    public GroupedOpenApi mdmGroupedOpenApi() {
        return IbootSwaggerAutoConfiguration.buildGroupedOpenApi("mdm");
    }

}

package com.iboot.pf.module.infra.framework.file.config;

import com.iboot.pf.module.infra.framework.file.core.client.FileClientFactory;
import com.iboot.pf.module.infra.framework.file.core.client.FileClientFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件配置类
 *
 * @author iboot
 */
@Configuration(proxyBeanMethods = false)
public class IbootFileAutoConfiguration {

    @Bean
    public FileClientFactory fileClientFactory() {
        return new FileClientFactoryImpl();
    }

}

package com.iboot.pf.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目的启动类
 *
 * @author iboot
 */
@SpringBootApplication(scanBasePackages = {"${iboot.info.base-package}.server", "${iboot.info.base-package}.module"})
public class IbootServerApplication {

    public static void main(String[] args) {

        SpringApplication.run(IbootServerApplication.class, args);

    }

}

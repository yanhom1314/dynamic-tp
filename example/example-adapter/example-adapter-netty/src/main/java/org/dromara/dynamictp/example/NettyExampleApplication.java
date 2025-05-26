package org.dromara.dynamictp.example;

import org.dromara.dynamictp.core.spring.EnableDynamicTp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * NettyExampleApplication for demonstrating Netty adapter functionality
 *
 * @author yanhom
 * @since 1.2.2
 */
@EnableDynamicTp
@SpringBootApplication
public class NettyExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(NettyExampleApplication.class, args);
    }
}

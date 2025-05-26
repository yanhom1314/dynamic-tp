package org.dromara.dynamictp.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller for demonstrating Netty adapter functionality
 *
 * @author yanhom
 * @since 1.2.2
 */
@RestController
@RequestMapping("/netty")
public class NettyTestController {

    @GetMapping("/info")
    public String getNettyInfo() {
        return "Netty adapter is running. Check the logs for EventLoopGroup monitoring information.";
    }

    @GetMapping("/status")
    public String getStatus() {
        return "Netty server is running on port 8080. You can connect using: telnet localhost 8080";
    }
}

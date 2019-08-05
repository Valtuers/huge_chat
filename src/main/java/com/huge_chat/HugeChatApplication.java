package com.huge_chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
// 扫描 所有需要的包, 包含一些自用的工具类包 所在的路径
@ComponentScan(basePackages= {"com.huge_chat", "org.n3r.idworker"})
public class HugeChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(HugeChatApplication.class, args);
    }

}

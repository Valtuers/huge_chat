package com.huge_chat.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.huge_chat.dao")
@tk.mybatis.spring.annotation.MapperScan("com.huge_chat.dao")
public class MyBatisConfig {
}

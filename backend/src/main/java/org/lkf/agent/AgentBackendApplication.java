package org.lkf.agent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("org.lkf.agent.mapper")
@EnableScheduling
public class AgentBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentBackendApplication.class, args);
    }
}

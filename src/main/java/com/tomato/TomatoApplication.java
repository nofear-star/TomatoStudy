package com.tomato;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@SpringBootApplication
@MapperScan("com.tomato.mapper")
public class TomatoApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(TomatoApplication.class, args);

        // 启动成功标识
        String port = context.getEnvironment().getProperty("server.port", "8090");
        System.out.println("\n==========================================");
        System.out.println("🚀 番茄自习室后端启动成功！");
        System.out.println("📍 访问地址: http://localhost:" + port);
        System.out.println("==========================================\n");
    }

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }
}


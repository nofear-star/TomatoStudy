package com.tomato.studyroom;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SpringBoot启动类
 */
@SpringBootApplication
@MapperScan("com.tomato.studyroom.mapper")
public class StudyRoomApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyRoomApplication.class, args);
    }
}

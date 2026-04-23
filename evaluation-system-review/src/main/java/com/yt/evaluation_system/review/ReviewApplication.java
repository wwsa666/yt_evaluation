package com.yt.evaluation_system.review;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.yt.evaluation_system.review", "com.yt.evaluation_system.common"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.yt.evaluation_system.review.mapper")
public class ReviewApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReviewApplication.class, args);
    }
}


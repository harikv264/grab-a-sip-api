package com.grabasip.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GrabASipApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(GrabASipApiApplication.class, args);
    }
}

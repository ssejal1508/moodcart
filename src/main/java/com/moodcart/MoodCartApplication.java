package com.moodcart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MoodCartApplication {
    public static void main(String[] args) {
        SpringApplication.run(MoodCartApplication.class, args);
    }
}

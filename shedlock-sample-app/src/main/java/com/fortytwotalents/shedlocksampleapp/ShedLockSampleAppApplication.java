package com.fortytwotalents.shedlocksampleapp;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT10s")
public class ShedLockSampleAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShedLockSampleAppApplication.class, args);
    }

}

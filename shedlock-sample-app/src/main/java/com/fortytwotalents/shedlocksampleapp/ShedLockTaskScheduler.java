package com.fortytwotalents.shedlocksampleapp;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ShedLockTaskScheduler {

    @Scheduled(cron = "*/2 * * * * *")
    @SchedulerLock(
            name = "UNIQUE_KEY_FOR_SHEDLOCK_SCHEDULER",
            lockAtLeastFor = "PT5S", // lock for at least a minute, overriding defaults
            lockAtMostFor = "PT10S" // lock for at most 7 minutes
    )
    public void scheduledTaskToRun() {
        // To assert that the lock is held (prevents misconfiguration errors)
        LockAssert.assertLocked();
        log.debug("Do other things ...");
    }

}
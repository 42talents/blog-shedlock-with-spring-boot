# blog-shedlock-with-spring-boot

[![Java CI with Maven](https://github.com/42talents/blog-shedlock-with-spring-boot/actions/workflows/maven.yml/badge.svg)](https://github.com/42talents/blog-shedlock-with-spring-boot/actions/workflows/maven.yml)

[ShedLock](https://github.com/lukas-krecan/ShedLock) for Spring Boot solves a very common problem, which occurs when running scheduled jobs in your application when deployed with a high availability setup into a production environment. You might be ending up with multiple job executions at the same time.

There is no way with Springs `@Scheduled` out of the box which solves this issue. Instead, frameworks like [ShedLock](https://github.com/lukas-krecan/ShedLock) or [Quartz](http://www.quartz-scheduler.org/) have their approaches.

ShedLock uses external storage to keep track of schedulers and locks. Your application needs to be connected to a database of your choice for this to work. This reaches from [JdbcTemplate](https://github.com/lukas-krecan/ShedLock#jdbctemplate) over [Mongo DB](https://github.com/lukas-krecan/ShedLock#mongo) to [Elastic Search](https://github.com/lukas-krecan/ShedLock#elasticsearch) and many more!

## Adding ShedLock's Dependency to the Project

Add a maven dependency for Spring Boot ShedLock to your pom.xml.

```xml
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-spring</artifactId>
    <version>4.26.0</version>
</dependency>
```

## Creating a ShedLock DB Table

Depending on your database, you need to create a database table for ShedLock. These scripts are typically executed via [Flyway](https://flywaydb.org/) or [Liquibase](https://www.liquibase.org/). Let's have a look at a PostgreSQL example:

```sql
# Postgres
CREATE TABLE shedlock(
    name VARCHAR(64) NOT NULL,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);
```

## Enable the SchedLock Scheduler

Enabling ShedLock and configure it with, e.g. the `defaultLockAtMostFor` would take a value as defined like `10s` or as in [java.time.Duration](https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-). It obtains a Duration from a text string on the form of `PnDTnHnMn.nS`.

```java
@EnableSchedulerLock(defaultLockAtMostFor = "PT10S")
public class ShedLockSampleAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShedLockSampleAppApplication.class, args);
    }
}
```

## Defineing a LockProvider Bean

Depending on the backing store we use, we need to define a corresponding LockBrovider bean.

```java
@Configuration
public class ShedLockConfig {
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(dataSource);
    }
}
```

Instead of setting up the database with your favourite DB migration tool, you can build it as well with the Lock Provider from ShedLock.

```java
@Configuration
public class ShedLockConfig {

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .usingDbTime() // Works on Postgres, MySQL, MariaDb, MS SQL, Oracle, DB2, HSQL and H2
                        .build()
        );
    }
}
```

By specifying `usingDbTime()`, the lock provider will use UTC time, based on the DB server clock. See more information in the [ShedLock documentation](https://github.com/lukas-krecan/ShedLock#configure-lockprovider).

## Annotating your Schedulers

The last thing to do is annotate the schedulers.

```java
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
```

You may have seen that we used `LockAssert` to check if the lock was really triggered in the production code. This is only to prevent misconfiguration errors, like AOP misconfiguration, missing annotation etc.

We made it through the example! Annotated schedulers will be automatically locked so that only one instance is executed, even when deployed multiple instances of your app in production. Enjoy experimenting!

The complete source code is available on our [Github Repository](https://github.com/42talents/blog-shedlock-with-spring-boot). We included [Testcontainers](https://www.testcontainers.org/) as well as [awaitility](https://github.com/awaitility/awaitility) to ensure ShedLock is running as expected.

If you are interested to learn more about Spring and Spring Boot, [get in touch and have a look at our training courses!](https://42talents.com/en/training/in-house/Spring-Core/)

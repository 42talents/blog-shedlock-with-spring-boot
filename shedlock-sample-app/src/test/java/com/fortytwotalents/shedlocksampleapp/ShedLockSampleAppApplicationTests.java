package com.fortytwotalents.shedlocksampleapp;

import net.javacrumbs.shedlock.core.LockAssert;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
@ExtendWith(OutputCaptureExtension.class)
class ShedLockSampleAppApplicationTests {

    // will be started before and stopped after each test method
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("shedlocksampleapp")
            .withUsername("postgres")
            .withPassword("postgres")
            .withExposedPorts(5432);

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> String.format("jdbc:postgresql://localhost:%d/%s", postgres.getFirstMappedPort(), postgres.getDatabaseName()));
        registry.add("spring.datasource.username", () -> postgres.getUsername());
        registry.add("spring.datasource.password", () -> postgres.getPassword());
    }

    @Test
    void contextLoads() {
    }

    @Test
    void testThatPostgresqlContainerIsRunning() {
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void testThatShedLockIsExecuted(CapturedOutput output) {
        LockAssert.TestHelper.makeAllAssertsPass(true);

        await().atMost(4, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(output.getOut()).contains(Lists.newArrayList("Locked 'UNIQUE_KEY_FOR_SHEDLOCK_SCHEDULER'", "Do other things ..."));
        });

    }

}
